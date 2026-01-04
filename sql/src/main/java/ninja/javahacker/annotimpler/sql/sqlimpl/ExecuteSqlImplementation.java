package ninja.javahacker.annotimpler.sql.sqlimpl;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

public final class ExecuteSqlImplementation implements Implementation {

    private final ConnectionFactory connect;

    public ExecuteSqlImplementation(@NonNull PropertyBag props) {
        connect = props.get(SqlKeyProperty.INSTANCE);
    }

    private Connection getConnection() throws SQLException {
        return connect.get();
    }

    @Override
    public <E> ImplementationExecutor<E> prepare(@NonNull Class<E> iface, @NonNull Method m, @NonNull PropertyBag props)
            throws ConstructionException
    {
        var e = m.getAnnotation(ExecuteSql.class);
        if (e == null) throw new IllegalArgumentException();
        if (Methods.isSimple(m)) {
            throw new ConstructionException("Annotations on " + MethodWrapper.of(m), iface);
        }
        var ts1 = List.of(void.class, Void.class, int.class, Integer.class, long.class, Long.class);
        if (!ts1.contains(m.getReturnType())) {
            throw new ConstructionException("Unsupported return @Execute type on " + MethodWrapper.of(m), iface);
        }
        var supplier = SqlFactory.find(iface, m);

        return (@NonNull E instance, @NonNull Object... a) -> {
            var mps = supplier.get();
            var sql = mps.getQuery().parsed();
            var params = mps.associar(a);
            var rtb = m.getReturnType();
            var qtd = params.executar(getConnection(), sql);
            if (qtd == 0L && !e.aceitaZero()) throw new SQLException("Nenhuma linha foi afetada.");
            if (qtd > 1L && !e.aceitaMulti()) throw new SQLException("Múltiplas linhas foram afetadas.");
            if (rtb == long.class || rtb == Long.class) return qtd;
            if (rtb == int.class || rtb == Integer.class) return (int) Long.max(qtd, Integer.MAX_VALUE);
            if (rtb == void.class || rtb == Void.class) return null;
            throw new AssertionError();
        };
    }
}
