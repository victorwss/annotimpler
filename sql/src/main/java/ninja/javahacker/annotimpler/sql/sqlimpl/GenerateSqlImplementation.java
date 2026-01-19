package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public final class GenerateSqlImplementation implements Implementation {

    private final ConnectionFactory connect;

    public GenerateSqlImplementation(@NonNull PropertyBag props) {
        connect = props.get(SqlKeyProperty.INSTANCE);
    }

    private Connection getConnection() throws SQLException {
        return connect.get();
    }

    private static String nome(@NonNull Method m) {
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @FunctionalInterface
    interface SpecialFunc {
        public Object operate(SqlWorker work) throws SQLException;
    }

    private static SpecialFunc findWork(@NonNull Method m) throws ConstructionException {
        if (m == null) throw new AssertionError();

        var rtb = m.getGenericReturnType();
        var raw = m.getReturnType();

        if (Methods.isSimple(m)) {
            throw new ConstructionException("Unsupported annotation @Generate on method: " + nome(m), m.getDeclaringClass());
        }
        if (rtb == long.class) return work -> work.gerarLong().getAsLong();
        if (rtb == Long.class) return work -> getOrNull(work.gerarLong());
        if (rtb == OptionalLong.class) return work -> work.gerarLong();
        if (rtb == int.class) return work -> work.gerar().getAsInt();
        if (rtb == Integer.class) return work -> getOrNull(work.gerar());
        if (rtb == OptionalInt.class) return work -> work.gerar();
        if (rtb instanceof ParameterizedType pt && pt.getRawType() == List.class) {
            var p2 = pt.getActualTypeArguments()[0];
            if (p2 == Integer.class) return work -> work.gerarLista();
            if (p2 == Long.class) return work -> work.gerarListaLong();
            throw new ConstructionException("Unsupported return @Generate list type on: " + nome(m), m.getDeclaringClass());
        }
        if (raw == List.class) {
            throw new ConstructionException("Incomplete return @Generate list type on: " + nome(m), m.getDeclaringClass());
        }
        throw new ConstructionException("Unsupported return @Generate type on: " + nome(m), m.getDeclaringClass());
    }

    @Override
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws ConstructionException {
        var g = m.getAnnotation(GenerateSql.class);
        if (g == null) throw new IllegalArgumentException();
        var ret = findWork(m);
        var supplier = SqlFactory.find(m);

        return (@NonNull E instance, @NonNull Object... a) -> {
            var params = supplier.get().associar(a);
            var work = new SqlWorker(getConnection(), params);
            return ret.operate(work);
        };
    }

    @Nullable
    private static Integer getOrNull(@NonNull OptionalInt opt) {
        if (opt == null) throw new AssertionError();
        return opt.isEmpty() ? null : opt.getAsInt();
    }

    @Nullable
    private static Long getOrNull(@NonNull OptionalLong opt) {
        if (opt == null) throw new AssertionError();
        return opt.isEmpty() ? null : opt.getAsLong();
    }
}
