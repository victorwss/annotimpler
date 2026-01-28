package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.Getter;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public final class SqlNamedParameter<T> {

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

    private final NameHandlerStrategy<T> strategy;

    @SuppressWarnings("unchecked")
    private SqlNamedParameter(int index, @NonNull Type type, @NonNull String name, boolean flat) {
        if (type == null) throw new AssertionError();
        if (name == null) throw new AssertionError();
        this.index = index;
        this.type = type;
        this.name = name;
        this.flat = flat;
        this.strategy = (NameHandlerStrategy<T>) makeStrategy(type, name, flat);
    }

    private static SqlNamedParameter<?> forParam(int index, @NonNull Parameter p) {
        return new SqlNamedParameter<>(index, p.getParameterizedType(), p.getName(), p.isAnnotationPresent(Flat.class));
    }

    public static List<? extends SqlNamedParameter<?>> forMethod(@NonNull Method m) {
        var pp = m.getParameters();
        return IntStream.range(0, pp.length).mapToObj(i -> SqlNamedParameter.forParam(i, pp[i])).toList();
    }

    @FunctionalInterface
    @SuppressWarnings({"checkstyle:ParenPad"})
    interface Handler<T> {

        public void handle(@NonNull NamedParameterStatement ps, @NonNull String name, @Nullable T value) throws SQLException;

        public default NamedHandler<T> named(@NonNull String name) {
            return (ps, value) -> handle(ps, name, value);
        }
    }

    @FunctionalInterface
    private interface NamedHandler<T> {
        public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException;
    }

    private record NameHandlerStrategy<T>(NamedHandler<T> h, Predicate<Object> p) {
        public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException {
            if (!test(value)) throw new IllegalArgumentException();
            h.handle(ps, value);
        }

        public boolean test(@Nullable Object value) {
            return p.test(value);
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
        if (k == null) throw new AssertionError();
        if (h == null) throw new AssertionError();
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
        if (k == null) throw new AssertionError();
        if (h == null) throw new AssertionError();
        return Map.entry(k, h);
    }

    private void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException {
        strategy.handle(ps, value);
    }

    public boolean accept(@Nullable Object value) {
        return strategy.test(value);
    }

    @NonNull
    private static <E extends Record> NamedHandler<E> forRecord(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        if (k == null) throw new AssertionError();
        if (name == null) throw new AssertionError();

        var rcs = k.getRecordComponents();
        var single = rcs.length == 1;
        var all = Stream.of(rcs).map(rc -> forComponent(rc, name, single, flat)).toList();
        return (@NonNull NamedParameterStatement ps, @Nullable E value) -> {
            for (var each : all) {
                each.handle(ps, value);
            }
        };
    }

    @NonNull
    private static NamedHandler<Object> forComponent(@NonNull RecordComponent rc, @NonNull String name, boolean single, boolean flat) {
        if (rc == null) throw new AssertionError();
        if (name == null) throw new AssertionError();

        var rct = rc.getGenericType();

        var paramName = single ? name
                : flat ? rc.getName()
                : name + "::" + rc.getName();

        @SuppressWarnings("unchecked")
        var inner = (NameHandlerStrategy<Object>) makeStrategy(rct, paramName, flat);

        return (@NonNull NamedParameterStatement ps, @Nullable Object value) -> {
            Object innerValue;
            try {
                innerValue = rc.getAccessor().invoke(value);
            } catch (IllegalAccessException | InvocationTargetException x) {
                throw new AssertionError(x);
            }
            inner.handle(ps, innerValue);
        };
    }

    @NonNull
    private static <E extends Enum<E>> NamedHandler<E> forEnum(@NonNull Class<E> k, @NonNull String name) {
        if (k == null) throw new AssertionError();
        if (name == null) throw new AssertionError();

        return (@NonNull NamedParameterStatement ps, @Nullable E value) -> {
            if (value == null) {
                ps.setNull(name, Types.INTEGER);
            } else {
                ps.setInt(name, value.ordinal());
            }
        };
    }

    @NonNull
    private static <E> NamedHandler<Optional<E>> forOptional(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        if (k == null) throw new AssertionError();
        if (name == null) throw new AssertionError();
        var in = forClass(k, name, flat);
        return (@NonNull NamedParameterStatement ps, @Nullable Optional<E> value) -> in.handle(ps, value == null ? null : value.get());
    }

    @NonNull
    private static NamedHandler<Void> forNull(@NonNull String name) {
        if (name == null) throw new AssertionError();

        return (@NonNull NamedParameterStatement ps, @Nullable Void value) -> {
            ps.setNull(name, Types.NULL);
        };
    }

    @SuppressWarnings("unchecked")
    private static <E> NamedHandler<E> forClass(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        if (k == null) throw new AssertionError();
        if (name == null) throw new AssertionError();

        if (k == void.class || k == Void.class) return (NamedHandler<E>) forNull(name);
        if (k.isRecord()) return (NamedHandler<E>) forRecord(k.asSubclass(Record.class), name, flat);
        if (k.isEnum()) return (NamedHandler<E>) forEnum(k.asSubclass(Enum.class), name);

        var h = ENTRIES.get(k);
        if (h == null) throw new UnsupportedOperationException();
        return (NamedHandler<E>) h.named(name);
    }

    private static Predicate<Object> acceptor(@NonNull Class<?> k) {
        if (k == null) throw new AssertionError();
        if (k == void.class || k == Void.class) return v -> v == null;
        if (k.isPrimitive()) {
            var wrapper = MagicConverter.wrap(k);
            return wrapper::isInstance;
        }
        if (ENTRIES.keySet().contains(k) || k.isRecord() || k.isEnum()) return v -> v == null || k.isInstance(v);
        throw new IllegalArgumentException();
    }

    private static NameHandlerStrategy<?> makeStrategy(@NonNull Type type, @NonNull String name, boolean flat) {
        if (type == null) throw new AssertionError();
        if (name == null) throw new AssertionError();

        if (type instanceof Class<?> kk) return new NameHandlerStrategy<>(forClass(kk, name, flat), acceptor(kk));
        if (type instanceof ParameterizedType pt && pt.getRawType() == Optional.class) {
            var ptt = pt.getActualTypeArguments();
            if (ptt.length == 1 && ptt[0] instanceof Class<?> ok) {
                var in = forOptional(ok, name, flat);
                Predicate<Object> p = v -> v == null || (v instanceof Optional<?> opt && (opt.isEmpty() || ok.isInstance(opt.get())));
                return new NameHandlerStrategy<>(in, p);
            }
        }
        throw new UnsupportedOperationException("" + type);
    }

    public static final class SqlNamedParameterWithValue<T> {
        @NonNull
        private final SqlNamedParameter<T> inner;

        @Getter
        @Nullable
        private final T value;

        private SqlNamedParameterWithValue(@NonNull SqlNamedParameter<T> inner, @Nullable T value) {
            if (inner == null) throw new AssertionError();
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
    }

    @NonNull
    public SqlNamedParameterWithValue<T> withValue(@Nullable T value) {
        return new SqlNamedParameterWithValue<>(this, value);
    }
}
