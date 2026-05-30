package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@PackagePrivate
record NameHandlerStrategy<T>(@NonNull NamedHandler<T> h, @NonNull Predicate<Object> p, @NonNull NameTester tester) {

    public void handle(@NonNull NamedParameterStatement ps, @Nullable T value) throws SQLException {
        checkNotNull(ps);
        if (!test(value)) throw new IllegalArgumentException();
        h.handle(ps, value);
    }

    public boolean test(@Nullable Object value) {
        return p.test(value);
    }

    public boolean testName(@NonNull Set<String> keys) {
        checkNotNull(keys);
        return tester.test(keys);
    }

    @NonNull
    public static <E extends Record> NameHandlerStrategy<E> forRecord(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        checkNotNull(k);
        checkNotNull(name);

        var rcs = k.getRecordComponents();
        var single = rcs.length == 1;
        var all = Stream.of(rcs).map(rc -> forComponent(rc, name, single, flat)).toList();

        NamedHandler<E> h = (@NonNull NamedParameterStatement ps, @Nullable E value) -> {
            checkNotNull(ps);
            for (var each : all) {
                each.handle(ps, value);
            }
        };
        return new NameHandlerStrategy<>(h, acceptor(k), keys -> keys.contains(name));
    }

    @NonNull
    public static NameHandlerStrategy<Object> forComponent(@NonNull RecordComponent rc, @NonNull String name, boolean single, boolean flat) {
        checkNotNull(rc);
        checkNotNull(name);

        var rct = rc.getGenericType();

        var paramName = single ? name
                : flat ? rc.getName()
                : name + "::" + rc.getName();

        @SuppressWarnings("unchecked")
        var inner = (NameHandlerStrategy<Object>) makeStrategy(rct, paramName, flat);

        NamedHandler<Object> h = (@NonNull NamedParameterStatement ps, @Nullable Object value) -> {
            checkNotNull(ps);
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
    public static <E extends Enum<E>> NameHandlerStrategy<E> forEnum(@NonNull Class<E> k, @NonNull String name) {
        checkNotNull(k);
        checkNotNull(name);

        NamedHandler<E> h = (@NonNull NamedParameterStatement ps, @Nullable E value) -> {
            checkNotNull(ps);
            if (value == null) {
                ps.setNull(name, Types.INTEGER);
            } else {
                ps.setInt(name, value.ordinal());
            }
        };
        return new NameHandlerStrategy<>(h, acceptor(k), keys -> keys.contains(name));
    }

    @NonNull
    public static <E> NameHandlerStrategy<Optional<E>> forOptional(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        checkNotNull(k);
        checkNotNull(name);

        var in = forClass(k, name, flat);
        NamedHandler<Optional<E>> h = (@NonNull NamedParameterStatement ps, @Nullable Optional<E> value) -> {
            checkNotNull(ps);
            in.handle(ps, value == null ? null : value.get());
        };

        Predicate<Object> p2 = v -> v == null || (v instanceof Optional<?> opt && in.test(opt.orElse(null)));
        return new NameHandlerStrategy<>(h, p2, in.tester());
    }

    @NonNull
    public static NameHandlerStrategy<Void> forNull(@NonNull String name) {
        checkNotNull(name);

        NamedHandler<Void> h = (@NonNull NamedParameterStatement ps, @Nullable Void value) -> {
            checkNotNull(ps);
            ps.setNull(name, Types.NULL);
        };
        return new NameHandlerStrategy<>(h, v -> v == null, keys -> keys.contains(name));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> NameHandlerStrategy<E> forClass(@NonNull Class<E> k, @NonNull String name, boolean flat) {
        checkNotNull(k);
        checkNotNull(name);

        if (k == void.class || k == Void.class) return (NameHandlerStrategy<E>) forNull(name);
        if (k.isRecord()) return (NameHandlerStrategy<E>) forRecord(k.asSubclass(Record.class), name, flat);
        if (k.isEnum()) return (NameHandlerStrategy<E>) forEnum(k.asSubclass(Enum.class), name);

        var h = (Handler<E>) Handler.ENTRIES.get(k);
        if (h == null) throw new UnsupportedOperationException();
        return new NameHandlerStrategy<>(h.named(name), acceptor(k), keys -> keys.contains(name));
    }

    @NonNull
    public static Predicate<Object> acceptor(@NonNull Class<?> k) {
        checkNotNull(k);
        if (k == void.class || k == Void.class) return v -> v == null;
        if (k.isPrimitive()) {
            var wrapper = WrapperClass.wrap(k);
            return wrapper::isInstance;
        }
        if (Handler.ENTRIES.keySet().contains(k) || k.isRecord() || k.isEnum()) {
            return v -> v == null || k.isInstance(v);
        }
        throw new IllegalArgumentException();
    }

    @NonNull
    public static NameHandlerStrategy<?> makeStrategy(@NonNull Type type, @NonNull String name, boolean flat) {
        checkNotNull(type);
        checkNotNull(name);

        if (type instanceof Class<?> kk) {
            return forClass(kk, name, flat);
        }
        if (type instanceof ParameterizedType pt && pt.getRawType() == Optional.class) {
            var ptt = pt.getActualTypeArguments();
            if (ptt.length == 1 && ptt[0] instanceof Class<?> ok) return forOptional(ok, name, flat);
        }
        throw new UnsupportedOperationException("" + type);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}