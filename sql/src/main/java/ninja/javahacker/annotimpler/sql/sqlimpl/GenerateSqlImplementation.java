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

    public GenerateSqlImplementation(@NonNull PropertyBag props, @NonNull ConverterFactory cvt, @NonNull Locale localizer) {
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

    @FunctionalInterface
    private static interface SpecialFunc {
        public Object operate(SqlWorker work) throws SQLException;
    }

    @NonNull
    private static SpecialFunc findWork(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m);

        var rtb = m.getGenericReturnType();
        var raw = m.getReturnType();

        if (Methods.isSimple(m)) {
            throw new BadImplementationException("Unsupported annotation @Generate on method: " + name(m), m.getDeclaringClass());
        }
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
        var ret = findWork(m);
        try {
            var supplier = SqlFactory.find(m);

            return (@NonNull E instance, @NonNull Object... a) -> {
                var params = supplier.get().withValues(a);
                var work = new SqlWorker(getConnection(), params, cvt, localizer);
                return ret.operate(work);
            };
        } catch (BadImplementationException | MagicFactory.CreationException | MagicFactory.CreatorSelectionException e) {
            throw new BadImplementationException("", e, QuerySqlImplementation.class);
        }
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
