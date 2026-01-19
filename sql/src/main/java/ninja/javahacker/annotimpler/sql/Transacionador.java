package ninja.javahacker.annotimpler.sql;

import java.lang.reflect.Proxy;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;

public final class Transacionador {

    private final ConnectionFactory factory;
    private final ThreadLocal<Transacao> local = new ThreadLocal<>();
    private final Supplier<String> geraIds;

    public Transacionador(@NonNull ConnectionFactory factory, @NonNull Supplier<String> geraIds) {
        this.factory = factory;
        this.geraIds = geraIds;
    }

    public static record Transacao(@NonNull Connection conexao, @NonNull String id) {
    }

    public <T> XSupplier<T> transacionar(@NonNull XSupplier<T> operacao) {
        return () -> {
            var jaTem = local.get() != null;
            if (jaTem) return operacao.get();

            try (var con = factory.get()) {
                local.set(new Transacao(con, geraIds.get()));
                var ok = false;
                try {
                    var ret = operacao.get();
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

    private static record It<E>(Method m, Transacionador t, E delegate) implements CallContext<E> {

        @Override
        public Object execute(@NonNull E instance, @NonNull Object... args) throws Throwable {
            return t.transacionar(() -> m.invoke(delegate, args));
        }
    }

    @NonNull
    private <E> CallContext<E> findImplementation(@NonNull Method m, @NonNull E delegate) throws ConstructionException {
        if (m == null) throw new AssertionError();
        return DefaultImplementation.<E>of(m).orElse(new It<>(m, this, delegate));
    }

    public <E> E transacionar(@NonNull Class<E> iface, @NonNull E delegate) {
        if (!iface.isInterface()) throw new UnsupportedOperationException();

        var ifaceMeths = Stream.of(iface.getMethods());

        var meths = Stream.concat(DefaultImplementation.OBJECT_DEFAULT.stream(), ifaceMeths)
                .collect(Collectors.toMap(m -> m, m -> XSupplier.wrap(() -> findImplementation(m, delegate)).get()));

        InvocationHandler ih = (p, m, a) -> {
            var impl = meths.get(m);
            if (impl == null) throw new AssertionError();
            return impl.execute(iface.cast(p), a == null ? new Object[0] : a);
        };

        var obj = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { iface }, ih);
        return iface.cast(obj);
    }

    @SuppressWarnings("unchecked")
    public <E, F extends E> E transacionar(@NonNull F impl) {
        if (impl instanceof Marcador) throw new IllegalArgumentException();

        InvocationHandler ih = (p, m, a) -> {
            if (Methods.isToString(m) || Methods.isHashCode(m) || Methods.isEquals(m)) return m.invoke(impl, a);
            return transacionar(() -> m.invoke(impl, a)).get();
        };

        var cl = Thread.currentThread().getContextClassLoader();
        var ifs = impl.getClass().getInterfaces();
        var ifs2 = new Class<?>[ifs.length + 1];
        ifs2[0] = Marcador.class;
        System.arraycopy(ifs, 0, ifs2, 1, ifs.length);

        return (E) Proxy.newProxyInstance(cl, ifs2, ih);
    }

    private static interface Marcador {}

    public Connection conexao() {
        return transacao().conexao();
    }

    public Transacao transacao() {
        var ret = local.get();
        if (ret == null) throw new IllegalStateException();
        return ret;
    }
}
