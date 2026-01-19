package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public final class QuerySqlImplementation implements Implementation {

    private final ConnectionFactory connect;

    public QuerySqlImplementation(@NonNull PropertyBag props) {
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
        public Object operate(SqlWorker work) throws SQLException, ConstructionException;
    }

    private static SpecialFunc findWork(@NonNull Method m, @NonNull QuerySql q) throws ConstructionException {
        if (m == null) throw new AssertionError();
        if (q == null) throw new AssertionError();

        var campos = q.campos();
        var rtb = m.getGenericReturnType();

        Class<?> rt;
        if (rtb instanceof ParameterizedType pt) {
            var base = pt.getRawType();
            if (base != List.class && base != Optional.class) {
                throw new UnsupportedOperationException(nome(m) + " - " + base);
            }
            if (!(pt.getActualTypeArguments()[0] instanceof Class<?> rtb2)) {
                throw new ConstructionException("Usage of @Query doesn't support return type on: " + nome(m), m.getDeclaringClass());
            }
            rt = rtb2;
        } else if (rtb instanceof Class<?> rtb2) {
            rt = rtb2;
        } else {
            throw new ConstructionException("Usage of @Query doesn't support return type on: " + nome(m), m.getDeclaringClass());
        }

        if (rt.isRecord()) {
            if (campos.length != 0 && campos.length != rt.getRecordComponents().length) {
                throw new ConstructionException("Mismatch multi-field return @Query record on: " + nome(m), m.getDeclaringClass());
            }
        } else if (campos.length > 1) {
            throw new ConstructionException("Mismatch single-field return @Query record on: " + nome(m), m.getDeclaringClass());
        }
        if (rtb == List.class) return work -> work.listar(rt, campos);
        if (rtb == Optional.class) return work -> work.ler(rt, campos);
        if (rtb == OptionalInt.class) return work -> asInt(work.ler(Integer.class, campos));
        if (rtb == OptionalLong.class) return work -> asLong(work.ler(Long.class, campos));
        if (rtb == OptionalDouble.class) return work -> asDouble(work.ler(Double.class, campos));
        return work -> work.ler(rt, campos).orElse(null);
    }

    @NonNull
    @Override
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws ConstructionException {
        var q = m.getAnnotation(QuerySql.class);
        if (q == null) throw new IllegalArgumentException();
        var ret = findWork(m, q);
        var supplier = SqlFactory.find(m);

        return (@NonNull E instance, @NonNull Object... a) -> {
            var params = supplier.get().associar(a);
            var work = new SqlWorker(getConnection(), params);
            return ret.operate(work);
        };
    }

    @Nullable
    private static OptionalInt asInt(@NonNull Optional<Integer> opt) {
        if (opt == null) throw new AssertionError();
        return opt.isEmpty() ? OptionalInt.empty() : OptionalInt.of(opt.get());
    }

    @Nullable
    private static OptionalLong asLong(@NonNull Optional<Long> opt) {
        if (opt == null) throw new AssertionError();
        return opt.isEmpty() ? OptionalLong.empty() : OptionalLong.of(opt.get());
    }

    @Nullable
    private static OptionalDouble asDouble(@NonNull Optional<Double> opt) {
        if (opt == null) throw new AssertionError();
        return opt.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(opt.get());
    }
}
