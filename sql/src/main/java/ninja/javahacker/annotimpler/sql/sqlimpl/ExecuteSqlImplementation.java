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

    public ExecuteSqlImplementation(@NonNull PropertyBag dependencies) {
        this.connect = dependencies.get(ConnectionFactoryKeyProperty.INSTANCE);
        this.cvt = dependencies.get(ConverterFactoryKeyProperty.INSTANCE);
        this.localizer = dependencies.get(LocalizerKeyProperty.INSTANCE);
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
    private static LongFunction<Object> selectOperation(@NonNull Method m) throws BadImplementationException {
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
    public <E> CallContext<E> prepare(
            @NonNull Class<E> k,
            @NonNull Method m,
            @NonNull PropertyBag props)
            throws BadImplementationException
    {
        if (!k.isAssignableFrom(m.getDeclaringClass())) throw new IllegalArgumentException();
        var es = m.getAnnotation(ExecuteSql.class);
        if (es == null) throw new IllegalArgumentException();

        var operation = selectOperation(m);
        var parset = new ParameterSet(m);
        var strict = es.validate();
        var supplier = ParsedSqlSupplier.find(strict, parset);

        return new CallContext<>() {
            @Override
            public Object execute(@NonNull E instance, @NonNull Object... a) throws Throwable {
                //if (es.validate() == SqlPreValidation.ON_EXECUTE) parset.prevalidate();
                var params = parset.withValues(a);
                var query = supplier.get();
                var work = new SqlWorker(getConnection(), params, query, cvt, localizer);
                var qtd = work.execute();
                if (qtd == 0L && !es.acceptsZero()) throw new SQLException("No line was affected.");
                if (qtd > 1L && !es.acceptsMulti()) throw new SQLException("Multipe lines were affected.");
                return operation.apply(qtd);
            }
        };
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
