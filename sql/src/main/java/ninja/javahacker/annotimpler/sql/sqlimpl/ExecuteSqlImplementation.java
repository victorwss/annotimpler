package ninja.javahacker.annotimpler.sql.sqlimpl;

import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public final class ExecuteSqlImplementation implements Implementation {

    @NonNull
    private final ConnectionFactory connect;

    @NonNull
    private final ConverterFactory cvt;

    @NonNull
    private final Locale localizer;

    public ExecuteSqlImplementation(@NonNull PropertyBag props, @NonNull ConverterFactory cvt, @NonNull Locale localizer) {
        connect = props.get(SqlKeyProperty.INSTANCE);
        this.cvt = cvt;
        this.localizer = localizer;
    }

    @NonNull
    private Connection getConnection() throws SQLException {
        return connect.get();
    }

    @NonNull
    private static String name(@NonNull Method m) {
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @NonNull
    private static LongFunction<Object> findWork(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m);
        var rtb = m.getReturnType();

        if (Methods.isSimple(m)) {
            throw new BadImplementationException("Unsupported annotation @Execute on " + name(m), m.getDeclaringClass());
        }
        if (rtb == long.class || rtb == Long.class) return qtd -> qtd;
        if (rtb == int.class || rtb == Integer.class) return qtd -> (int) Long.min(qtd, Integer.MAX_VALUE);
        if (rtb == void.class || rtb == Void.class) return qtd -> null;
        throw new BadImplementationException("Unsupported return @Execute type on " + name(m), m.getDeclaringClass());
    }

    @NonNull
    @Override
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws BadImplementationException {
        var es = m.getAnnotation(ExecuteSql.class);
        if (es == null) throw new IllegalArgumentException();

        if (Methods.isSimple(m)) {
            throw new BadImplementationException("Annotations on " + MethodWrapper.of(m), m.getDeclaringClass());
        }
        var ret = findWork(m);

        try {
            var supplier = SqlFactory.find(m);

            return (@NonNull E instance, @NonNull Object... a) -> {
                var params = supplier.get().withValues(a);
                var work = new SqlWorker(getConnection(), params, cvt, localizer);
                var qtd = work.execute();
                if (qtd == 0L && !es.acceptsZero()) throw new SQLException("No line was affected.");
                if (qtd > 1L && !es.acceptsMulti()) throw new SQLException("Multipe lines were affected.");
                return ret.apply(qtd);
            };
        } catch (BadImplementationException | MagicFactory.CreationException | MagicFactory.CreatorSelectionException e) {
            throw new BadImplementationException("", e, QuerySqlImplementation.class);
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
