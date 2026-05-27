package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public final class QuerySqlImplementation implements Implementation {

    @NonNull
    private final ConnectionFactory connect;

    @NonNull
    private final ConverterFactory cvt;

    @NonNull
    private final Locale localizer;

    public QuerySqlImplementation(@NonNull PropertyBag dependencies) {
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
    private static SpecialFunc findWork(@NonNull Method m, @NonNull QuerySql q) throws BadImplementationException {
        checkNotNull(m);
        checkNotNull(q);

        var fields = q.fields();
        var rtb = m.getGenericReturnType();

        Class<?> rt;
        if (rtb instanceof ParameterizedType pt) {
            var base = pt.getRawType();
            if (base != List.class && base != Optional.class) {
                throw new UnsupportedOperationException(name(m) + " - " + base);
            }
            if (!(pt.getActualTypeArguments()[0] instanceof Class<?> rtb2)) {
                throw new BadImplementationException("Usage of @Query doesn't support return type on: " + name(m), m.getDeclaringClass());
            }
            rt = rtb2;
        } else if (rtb instanceof Class<?> rtb2) {
            rt = rtb2;
        } else {
            throw new BadImplementationException("Usage of @Query doesn't support return type on: " + name(m), m.getDeclaringClass());
        }

        if (rt.isRecord()) {
            if (fields.length != 0 && fields.length != rt.getRecordComponents().length) {
                throw new BadImplementationException("Mismatch multi-field return @Query record on: " + name(m), m.getDeclaringClass());
            }
        } else if (fields.length > 1) {
            throw new BadImplementationException("Mismatch single-field return @Query record on: " + name(m), m.getDeclaringClass());
        }
        if (rtb == List.class) return work -> work.listar(rt, fields);
        if (rtb == Optional.class) return work -> work.read(rt, fields);
        if (rtb == OptionalInt.class) return work -> asInt(work.read(Integer.class, fields));
        if (rtb == OptionalLong.class) return work -> asLong(work.read(Long.class, fields));
        if (rtb == OptionalDouble.class) return work -> asDouble(work.read(Double.class, fields));
        return work -> work.read(rt, fields).orElse(null);
    }

    @NonNull
    @Override
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws BadImplementationException {
        var q = m.getAnnotation(QuerySql.class);
        if (q == null) throw new IllegalArgumentException();
        var ret = findWork(m, q);

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
    private static OptionalInt asInt(@NonNull Optional<Integer> opt) {
        checkNotNull(opt);
        return opt.isEmpty() ? OptionalInt.empty() : OptionalInt.of(opt.get());
    }

    @Nullable
    private static OptionalLong asLong(@NonNull Optional<Long> opt) {
        checkNotNull(opt);
        return opt.isEmpty() ? OptionalLong.empty() : OptionalLong.of(opt.get());
    }

    @Nullable
    private static OptionalDouble asDouble(@NonNull Optional<Double> opt) {
        checkNotNull(opt);
        return opt.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(opt.get());
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
