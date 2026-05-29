package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.Getter;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public final class SqlNamedParameter<T> {

    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    @Getter
    private final int index;

    @Getter
    @NonNull
    private final Type type;

    @Getter
    @NonNull
    private final String name;

    @Getter
    private final boolean flat;

    @NonNull
    private final NameHandlerStrategy<T> strategy;

    @SuppressWarnings("unchecked")
    private SqlNamedParameter(int index, @NonNull Type type, @NonNull String name, boolean flat) {
        checkNotNull(type);
        checkNotNull(name);
        this.index = index;
        this.type = type;
        this.name = name;
        this.flat = flat;
        this.strategy = (NameHandlerStrategy<T>) makeStrategy(type, name, flat);
    }

    @NonNull
    private static SqlNamedParameter<?> forParam(int index, @NonNull Parameter p) {
        checkNotNull(p);
        return new SqlNamedParameter<>(index, p.getParameterizedType(), p.getName(), p.isAnnotationPresent(Flat.class));
    }

    @NonNull
    public static List<? extends SqlNamedParameter<?>> forMethod(@NonNull Method m) {
        var pp = m.getParameters();
        return IntStream.range(0, pp.length).mapToObj(i -> SqlNamedParameter.forParam(i, pp[i])).toList();
    }

    @NonNull
    private List<Object> state() {
        return List.of(index, type, name, flat);
    }

    @Override
    public int hashCode() {
        return state().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) return true;
        if (!(other instanceof SqlNamedParameter<?> p)) return false;
        return Objects.equals(this.state(), p.state());
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + state();
    }

    @FunctionalInterface
    private static interface NameTester {
        public boolean test(@NonNull Set<String> names);
    }

    @FunctionalInterface
    @SuppressWarnings({"checkstyle:ParenPad"})
    private static interface Handler<T> {

        public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;

        @NonNull
        public default NamedHandler<T> named(@NonNull String name) {
            return (ps, value) -> handle(ps, name, value);
        }
    }

    @FunctionalInterface
    private static interface NamedHandler<T> {
        public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException;
    }

    private record NameHandlerStrategy<T>(@NonNull NamedHandler<T> h, @NonNull Predicate<Object> p, @NonNull NameTester tester) {
        public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException {
            if (!test(value)) throw new IllegalArgumentException();
            h.handle(ps, value);
        }

        public boolean test(@Nullable Object value) {
            return p.test(value);
        }

        public boolean testName(@NonNull Set<String> keys) {
            return tester.test(keys);
        }
    }

    private static final Map<Class<?>, Handler<?>> ENTRIES = Map.ofEntries(
            entry(boolean       .class, NamedParameterStatement::setBoolean   , Types.BOOLEAN ),
            entry(Boolean       .class, NamedParameterStatement::setBoolean   , Types.BOOLEAN ),
            entry(byte          .class, NamedParameterStatement::setByte      , Types.TINYINT ),
            entry(Byte          .class, NamedParameterStatement::setByte      , Types.TINYINT ),
            entry(short         .class, NamedParameterStatement::setShort     , Types.SMALLINT),
            entry(Short         .class, NamedParameterStatement::setShort     , Types.SMALLINT),
            entry(int           .class, NamedParameterStatement::setInt       , Types.INTEGER ),
            entry(Integer       .class, NamedParameterStatement::setInt       , Types.INTEGER ),
            entry(long          .class, NamedParameterStatement::setLong      , Types.BIGINT  ),
            entry(Long          .class, NamedParameterStatement::setLong      , Types.BIGINT  ),
            entry(float         .class, NamedParameterStatement::setFloat     , Types.FLOAT   ),
            entry(Float         .class, NamedParameterStatement::setFloat     , Types.FLOAT   ),
            entry(double        .class, NamedParameterStatement::setDouble    , Types.DOUBLE  ),
            entry(Double        .class, NamedParameterStatement::setDouble    , Types.DOUBLE  ),
            entry(BigDecimal    .class, NamedParameterStatement::setBigDecimal                ),
            entry(String        .class, NamedParameterStatement::setString                    ),
            entry(LocalDate     .class, NamedParameterStatement::setLocalDate                 ),
            entry(LocalDateTime .class, NamedParameterStatement::setLocalDateTime             ),
            entry(LocalTime     .class, NamedParameterStatement::setLocalTime                 ),
            entry(OptionalInt   .class, NamedParameterStatement::setInt                       ),
            entry(OptionalLong  .class, NamedParameterStatement::setLong                      ),
            entry(OptionalDouble.class, NamedParameterStatement::setDouble                    )
    );

    @NonNull
    private static <E> Map.Entry<Class<?>, Handler<?>> entry(@NonNull Class<E> k, @NonNull Handler<E> h, int type) {
        checkNotNull(k);
        checkNotNull(h);
        return Map.entry(k, (@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable E value) -> {
            if (value == null) {
                ps.setNull(name, type);
            } else {
                h.handle(ps, name, value);
            }
        });
    }

    @NonNull
    private static <E> Map.Entry<Class<?>, Handler<?>> entry(@NonNull Class<E> k, @NonNull Handler<E> h) {
        checkNotNull(k);
        checkNotNull(h);
        return Map.entry(k, h);
    }

    private void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException {
        strategy.handle(ps, value);
    }

    public boolean testParameter(@NonNull Set<String> keys) {
        return keys.contains(this.name);
    }

    public boolean accept(@Nullable Object value) {
        return strategy.test(value);
    }

    @NonNull
    private static <E extends Record> NameHandlerStrategy<E> forRecord(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        checkNotNull(k);
        checkNotNull(name);

        var rcs = k.getRecordComponents();
        var single = rcs.length == 1;
        var all = Stream.of(rcs).map(rc -> forComponent(rc, name, single, flat)).toList();

        NamedHandler<E> h = (@NonNull NamedParameterStatement ps, @Nullable E value) -> {
            for (var each : all) {
                each.handle(ps, value);
            }
        };
        return new NameHandlerStrategy<>(h, acceptor(k), keys -> keys.contains(name));
    }

    @NonNull
    private static NameHandlerStrategy<Object> forComponent(@NonNull RecordComponent rc, @NonNull String name, boolean single, boolean flat) {
        checkNotNull(rc);
        checkNotNull(name);

        var rct = rc.getGenericType();

        var paramName = single ? name
                : flat ? rc.getName()
                : name + "::" + rc.getName();

        @SuppressWarnings("unchecked")
        var inner = (NameHandlerStrategy<Object>) makeStrategy(rct, paramName, flat);

        NamedHandler<Object> h = (@NonNull NamedParameterStatement ps, @Nullable Object value) -> {
            Object innerValue;
            try {
                innerValue = rc.getAccessor().invoke(value);
            } catch (IllegalAccessException | InvocationTargetException x) {
                throw new AssertionError(x);
            }
            inner.handle(ps, innerValue);
        };
        return new NameHandlerStrategy<>(h, inner::test, inner::testName);
    }

    @NonNull
    private static <E extends Enum<E>> NameHandlerStrategy<E> forEnum(@NonNull Class<E> k, @NonNull String name) {
        checkNotNull(k);
        checkNotNull(name);

        NamedHandler<E> h = (@NonNull NamedParameterStatement ps, @Nullable E value) -> {
            if (value == null) {
                ps.setNull(name, Types.INTEGER);
            } else {
                ps.setInt(name, value.ordinal());
            }
        };
        return new NameHandlerStrategy<>(h, acceptor(k), keys -> keys.contains(name));
    }

    @NonNull
    private static <E> NamedHandler<Optional<E>> forOptional(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        checkNotNull(k);
        checkNotNull(name);
        var in = forClass(k, name, flat);
        return (@NonNull NamedParameterStatement ps, @Nullable Optional<E> value) -> in.handle(ps, value == null ? null : value.get());
    }

    @NonNull
    private static NameHandlerStrategy<Void> forNull(@NonNull String name) {
        checkNotNull(name);

        NamedHandler<Void> h = (@NonNull NamedParameterStatement ps, @Nullable Void value) -> {
            ps.setNull(name, Types.NULL);
        };
        return new NameHandlerStrategy<>(h, v -> v == null, keys -> keys.contains(name));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private static <E> NameHandlerStrategy<E> forClass(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        checkNotNull(k);
        checkNotNull(name);

        if (k == void.class || k == Void.class) return (NameHandlerStrategy<E>) forNull(name);
        if (k.isRecord()) return (NameHandlerStrategy<E>) forRecord(k.asSubclass(Record.class), name, flat);
        if (k.isEnum()) return (NameHandlerStrategy<E>) forEnum(k.asSubclass(Enum.class), name);

        var h = ENTRIES.get(k);
        if (h == null) throw new UnsupportedOperationException();
        return (NameHandlerStrategy<E>) new NameHandlerStrategy<>(h.named(name), acceptor(k), keys -> keys.contains(name));
    }

    @NonNull
    private static Predicate<Object> acceptor(@NonNull Class<?> k) {
        checkNotNull(k);
        if (k == void.class || k == Void.class) return v -> v == null;
        if (k.isPrimitive()) {
            var wrapper = wrap(k);
            return wrapper::isInstance;
        }
        if (ENTRIES.keySet().contains(k) || k.isRecord() || k.isEnum()) {
            return v -> v == null || k.isInstance(v);
        }
        throw new IllegalArgumentException();
    }

    @NonNull
    private static NameHandlerStrategy<?> makeStrategy(@NonNull Type type, @NonNull String name, boolean flat) {
        checkNotNull(type);
        checkNotNull(name);

        if (type instanceof Class<?> kk) {
            return forClass(kk, name, flat);
        }
        if (type instanceof ParameterizedType pt && pt.getRawType() == Optional.class) {
            var ptt = pt.getActualTypeArguments();
            if (ptt.length == 1 && ptt[0] instanceof Class<?> ok) {
                var in = forOptional(ok, name, flat);
                Predicate<Object> p = v -> v == null || (v instanceof Optional<?> opt && (opt.isEmpty() || ok.isInstance(opt.get())));
                return new NameHandlerStrategy<>(in, p, keys -> keys.contains(name));
            }
        }
        throw new UnsupportedOperationException("" + type);
    }

    @NonNull
    public SqlNamedParameterWithValue<T> withValue(@Nullable T value) {
        return new SqlNamedParameterWithValue<>(this, value);
    }

    @NonNull
    public static Class<?> wrap(@NonNull Class<?> k) {
        checkNotNull(k);
        return WRAPPERS.get(k);
    }

    public static final class SqlNamedParameterWithValue<T> {
        @NonNull
        private final SqlNamedParameter<T> inner;

        @Getter
        @Nullable
        private final T value;

        private SqlNamedParameterWithValue(@NonNull SqlNamedParameter<T> inner, @Nullable T value) {
            checkNotNull(inner);
            if (!inner.accept(value)) throw new IllegalArgumentException();
            this.inner = inner;
            this.value = value;
        }

        public void handle(@NonNull NamedParameterStatement ps) throws SQLException {
            inner.handle(ps, value);
        }

        public int getIndex() {
            return inner.getIndex();
        }

        @NonNull
        public Type getType() {
            return inner.getType();
        }

        @NonNull
        public String getName() {
            return inner.getName();
        }

        public boolean isFlat() {
            return inner.isFlat();
        }

        @NonNull
        private List<Object> state() {
            return Stream.concat(inner.state().stream(), Stream.of(value)).toList();
        }

        @Override
        public int hashCode() {
            return state().hashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof SqlNamedParameterWithValue<?> ps && Objects.equals(this.state(), ps.state());
        }

        @NonNull
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + state();
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
