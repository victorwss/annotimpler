package ninja.javahacker.sqlplus.sqlimpl;

import module java.base;
import module ninja.javahacker.sqlplus;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

public final class GenerateSqlImplementation implements Implementation {

    private final ConnectionFactory connect;

    public GenerateSqlImplementation(@NonNull PropertyBag props) {
        connect = props.get(SqlKeyProperty.INSTANCE);
    }

    private Connection getConnection() throws SQLException {
        return connect.get();
    }

    @Override
    public <E> ImplementationExecutor<E> prepare(@NonNull Class<E> iface, @NonNull Method m, @NonNull PropertyBag props)
            throws ConstructionException
    {
        var g = m.getAnnotation(GenerateSql.class);
        if (g == null) throw new IllegalArgumentException();
        var nome = NameDictionary.global().getSimplifiedGenericString(m, true);
        if (Methods.isSimple(m)) throw new UnsupportedOperationException("Annotations on: " + nome);
        var ts2 = List.of(OptionalInt.class, OptionalLong.class, int.class, Integer.class, long.class, Long.class, List.class);
        if (!ts2.contains(m.getReturnType())) {
            throw new ConstructionException("Unsupported return Generate type on: " + nome, iface);
        }
        if (List.class == m.getReturnType()) {
            var p = m.getGenericReturnType();
            if (!(p instanceof ParameterizedType pp)) {
                throw new ConstructionException("Incomplete return @Generate list type on: " + nome, iface);
            }
            var ppp = pp.getActualTypeArguments()[0];
            if (ppp != Integer.class && ppp != Long.class) {
                throw new ConstructionException("Unsupported return @Generate list type on: " + nome, iface);
            }
        }
        var supplier = SqlFactory.find(iface, m);

        return (@NonNull E instance, @NonNull Object... a) -> {
            var mps = supplier.get();
            var sql = mps.getQuery().parsed();
            var params = mps.associar(a);
            var rtb = m.getGenericReturnType();
            if (rtb == long.class) return params.gerarLong(getConnection(), sql).getAsLong();
            if (rtb == Long.class) return getOrNull(params.gerarLong(getConnection(), sql));
            if (rtb == OptionalLong.class) return params.gerarLong(getConnection(), sql);
            if (rtb == int.class) return params.gerar(getConnection(), sql).getAsInt();
            if (rtb == Integer.class) return getOrNull(params.gerar(getConnection(), sql));
            if (rtb == OptionalInt.class) return params.gerar(getConnection(), sql);
            if (rtb instanceof ParameterizedType pt && pt.getRawType() == List.class) {
                var p2 = pt.getActualTypeArguments()[0];
                if (p2 == Integer.class) return params.gerarLista(getConnection(), sql);
                if (p2 == Long.class) return params.gerarListaLong(getConnection(), sql);
            }
            throw new AssertionError();
        };
    }

    @Nullable
    private static Integer getOrNull(@NonNull OptionalInt opt) {
        return opt.isEmpty() ? null : opt.getAsInt();
    }

    @Nullable
    private static Long getOrNull(@NonNull OptionalLong opt) {
        return opt.isEmpty() ? null : opt.getAsLong();
    }
}
