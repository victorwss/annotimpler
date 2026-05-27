package ninja.javahacker.annotimpler.sql;

import java.lang.reflect.Proxy;
import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;

public final class Transactor {

    private final ConnectionFactory factory;
    private final ThreadLocal<Transaction> local = new ThreadLocal<>();
    private final Supplier<String> generateIds;

    public Transactor(@NonNull ConnectionFactory factory, @NonNull Supplier<String> generateIds) {
        this.factory = factory;
        this.generateIds = generateIds;
    }

    public static record Transaction(@NonNull Connection connection, @NonNull String id) {
    }

    @FunctionalInterface
    public static interface XSupplier<E> {
        public E get() throws Throwable;
    }

    private <T> XSupplier<T> operate(@NonNull XSupplier<T> operation) {
        return () -> {
            var alreadyHas = local.get() != null;
            if (alreadyHas) return operation.get();

            try (var con = factory.get()) {
                local.set(new Transaction(con, generateIds.get()));
                var ok = false;
                try {
                    var ret = operation.get();
                    ok = true;
                    return ret;
                } finally {
                    if (ok) {
                        con.commit();
                    } else {
                        con.rollback();
                    }
                }
            } finally {
                local.remove();
            }
        };
    }

    private static <A> A unwrap(@NonNull XSupplier<A> input) throws Throwable {
        checkNotNull(input);
        try {
            return input.get();
        } catch (InvocationTargetException | UndeclaredThrowableException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    public <E, F extends E> E transact(@NonNull F impl) {
        if (impl instanceof Marker) throw new IllegalArgumentException();

        InvocationHandler ih = (p, m, a) -> {
            if (Methods.isToString(m) || Methods.isHashCode(m) || Methods.isEquals(m)) return unwrap(() -> m.invoke(impl, a));
            return operate(() -> unwrap(() -> m.invoke(impl, a))).get();
        };

        var cl = Thread.currentThread().getContextClassLoader();
        var ifs = impl.getClass().getInterfaces();
        var ifs2 = new Class<?>[ifs.length + 1];
        ifs2[0] = Marker.class;
        System.arraycopy(ifs, 0, ifs2, 1, ifs.length);

        return (E) Proxy.newProxyInstance(cl, ifs2, ih);
    }

    public static interface Marker {}

    public Connection connection() {
        return currentTransaction().connection();
    }

    public Transaction currentTransaction() {
        var ret = local.get();
        if (ret == null) throw new IllegalStateException();
        return ret;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
