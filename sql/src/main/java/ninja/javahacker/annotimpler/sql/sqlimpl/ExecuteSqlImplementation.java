package ninja.javahacker.annotimpler.sql.sqlimpl;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public final class ExecuteSqlImplementation implements Implementation {

    private final ConnectionFactory connect;

    public ExecuteSqlImplementation(@NonNull PropertyBag props) {
        connect = props.get(SqlKeyProperty.INSTANCE);
    }

    private Connection getConnection() throws SQLException {
        return connect.get();
    }

    private static String nome(@NonNull Method m) {
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    private static LongFunction<Object> findWork(@NonNull Method m) throws ConstructionException {
        if (m == null) throw new AssertionError();
        var rtb = m.getReturnType();

        if (Methods.isSimple(m)) {
            throw new ConstructionException("Unsupported annotation @Execute on " + nome(m), m.getDeclaringClass());
        }
        if (rtb == long.class || rtb == Long.class) return qtd -> qtd;
        if (rtb == int.class || rtb == Integer.class) return qtd -> (int) Long.min(qtd, Integer.MAX_VALUE);
        if (rtb == void.class || rtb == Void.class) return qtd -> null;
        throw new ConstructionException("Unsupported return @Execute type on " + nome(m), m.getDeclaringClass());
    }

    @Override
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws ConstructionException {
        var e = m.getAnnotation(ExecuteSql.class);
        if (e == null) throw new IllegalArgumentException();

        if (Methods.isSimple(m)) {
            throw new ConstructionException("Annotations on " + MethodWrapper.of(m), m.getDeclaringClass());
        }
        var ret = findWork(m);
        var supplier = SqlFactory.find(m);

        return (@NonNull E instance, @NonNull Object... a) -> {
            var params = supplier.get().associar(a);
            var work = new SqlWorker(getConnection(), params);
            var qtd = work.executar();
            if (qtd == 0L && !e.aceitaZero()) throw new SQLException("Nenhuma linha foi afetada.");
            if (qtd > 1L && !e.aceitaMulti()) throw new SQLException("Múltiplas linhas foram afetadas.");
            return ret.apply(qtd);
        };
    }
}
