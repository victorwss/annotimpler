package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public final class GenerateSqlImplementation implements Implementation {

    @NonNull
    private final ConnectionFactory connect;

    @NonNull
    private final ConverterFactory cvt;

    @NonNull
    private final Locale localizer;

    public GenerateSqlImplementation(@NonNull PropertyBag dependencies) {
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

    @FunctionalInterface
    private static interface SpecialFunc {
        public Object operate(SqlWorker work) throws SQLException;
    }

    @NonNull
    private static SpecialFunc selectOperation(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m);

        var rtb = m.getGenericReturnType();
        var raw = m.getReturnType();

        if (rtb == long.class) return work -> work.generateLong().getAsLong();
        if (rtb == Long.class) return work -> getOrNull(work.generateLong());
        if (rtb == OptionalLong.class) return work -> work.generateLong();
        if (rtb == int.class) return work -> work.generate().getAsInt();
        if (rtb == Integer.class) return work -> getOrNull(work.generate());
        if (rtb == OptionalInt.class) return work -> work.generate();
        if (rtb instanceof ParameterizedType pt && pt.getRawType() == List.class) {
            var p2 = pt.getActualTypeArguments()[0];
            if (p2 == Integer.class) return work -> work.generateList();
            if (p2 == Long.class) return work -> work.generateLongList();
            throw new BadImplementationException("Unsupported return @Generate list type on: " + name(m), m.getDeclaringClass());
        }
        if (raw == List.class) {
            throw new BadImplementationException("Incomplete return @Generate list type on: " + name(m), m.getDeclaringClass());
        }
        throw new BadImplementationException("Unsupported return @Generate type on: " + name(m), m.getDeclaringClass());
    }

    @NonNull
    @Override
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws BadImplementationException {
        var g = m.getAnnotation(GenerateSql.class);
        if (g == null) throw new IllegalArgumentException();

        var operation = selectOperation(m);
        var parset = new ParameterSet(g.validate() == SqlPreValidation.ON_LOAD, m);

        return new CallContext<>() {
            @Override
            public Object execute(@NonNull E instance, @NonNull Object... a) throws Throwable {
                var params = parset.withValues(g.validate() == SqlPreValidation.ON_EXECUTE, a);
                var work = new SqlWorker(getConnection(), params, cvt, localizer);
                return operation.operate(work);
            }
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

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
