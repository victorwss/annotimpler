package ninja.javahacker.sqlplus.sqlimpl;

import module java.base;
import module ninja.javahacker.sqlplus;

import lombok.NonNull;

public final class QuerySqlImplementation implements Implementation {

    private final ConnectionFactory connect;

    public QuerySqlImplementation(@NonNull PropertyBag props) {
        connect = props.get(SqlKeyProperty.INSTANCE);
    }

    private Connection getConnection() throws SQLException {
        return connect.get();
    }

    @NonNull
    @Override
    public <E> ImplementationExecutor<E> prepare(@NonNull Class<E> iface, @NonNull Method m, @NonNull PropertyBag props)
            throws ConstructionException
    {
        var q = m.getAnnotation(QuerySql.class);
        if (q == null) throw new IllegalArgumentException();
        var rt = relevantType(m);
        var campos = q.campos();
        var nome = NameDictionary.global().getSimplifiedGenericString(m, true);
        if (rt.isRecord()) {
            if (campos.length != 0 && campos.length != rt.getRecordComponents().length) {
                throw new ConstructionException("Mismatch multi-field return @Query record on: " + nome, iface);
            }
        } else if (campos.length > 1) {
            throw new ConstructionException("Mismatch single-field return @Query record on: " + nome, iface);
        }
        var supplier = SqlFactory.find(iface, m);

        return (@NonNull E instance, @NonNull Object... a) -> {
            var mps = supplier.get();
            var sql = mps.getQuery().parsed();
            var params = mps.associar(a);
            var rtb = m.getReturnType();
            java.lang.Class<?> rt1 = relevantType(m);
            if (rtb == List.class) {
                return params.listar(getConnection(), rt1, sql, q.campos());
            }
            java.util.Optional<?> opt = params.ler(getConnection(), rt1, sql, q.campos());
            if (rtb == Optional.class) return opt;
            if (rtb == OptionalInt.class) return opt.isEmpty() ? OptionalInt.empty() : OptionalInt.of((int) opt.get());
            if (rtb == OptionalLong.class) return opt.isEmpty() ? OptionalLong.empty() : OptionalLong.of((long) opt.get());
            if (rtb == OptionalDouble.class) return opt.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of((double) opt.get());
            return opt.orElse(null);
        };
    }

    @NonNull
    private static Class<?> relevantType(@NonNull Method m) {
        Type t = switch (m.getGenericReturnType()) {
            case Class<?> c -> c;
            case ParameterizedType p -> {
                var base = p.getRawType();
                if (base != List.class && base != Optional.class) {
                    throw new UnsupportedOperationException(NameDictionary.global().getSimplifiedGenericString(m, true) + " - " + base);
                }
                yield p.getActualTypeArguments()[0];
            }
            default -> throw new UnsupportedOperationException(NameDictionary.global().getSimplifiedGenericString(m, true));
        };
        if (t == null) throw new AssertionError();
        if (t == OptionalInt.class) return Integer.class;
        if (t == OptionalLong.class) return Long.class;
        if (t == OptionalDouble.class) return Double.class;
        if (t instanceof Class<?> r) return r;
        throw new UnsupportedOperationException(t.getTypeName());
    }
}
