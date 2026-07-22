package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

/// [Implementation] that backs DAO methods annotated with [@QuerySql][QuerySql].
///
/// At preparation time this class validates the method's return type and `fields` configuration,
/// then builds a compiled [CallContext]. On every invocation the context executes the SELECT
/// statement and maps each result row to the requested return type.
///
/// **Supported return types**
///
/// | Return type | Behaviour |
/// |---|---|
/// | `Optional<T>` | First row mapped to `T`, or [Optional#empty()] if no rows. |
/// | `T` (bare type) | First row mapped to `T`, or `null` if no rows. |
/// | `OptionalInt` | First column of first row as `int`, or [OptionalInt#empty()]. |
/// | `OptionalLong` | First column of first row as `long`, or [OptionalLong#empty()]. |
/// | `OptionalDouble` | First column of first row as `double`, or [OptionalDouble#empty()]. |
/// | `List<T>` | All rows mapped to `T`, in result-set order. |
///
/// The element type `T` may be any scalar type supported by [ConverterFactory], or a Java
/// `record`. When `T` is a record, each result column is mapped to the corresponding record
/// component. The mapping order can be overridden by supplying explicit column indices via
/// the `fields` attribute of [@QuerySql][QuerySql].
///
/// Wildcard type parameters (`Optional<?>`, `List<?>`) are rejected at preparation time.
/// When `fields` contains more than one index, `T` must be a record and the number of indices
/// must exactly match the number of record components.
public enum QuerySqlImplementation implements Implementation {
    /// Sole instance.
    INSTANCE;

    @NonNull
    private static String name(@NonNull Method m) {
        checkNotNull(m); // Check recognized by lombok.
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    /// Wraps the implementation of a method annotated with [QuerySql].
    @FunctionalInterface
    private static interface SpecialFunc {

        /// Executes the implementation receiving an object containg the connection, query, converters and locale.
        /// @param work Speciefies the connection, SQL, converters and locale useful for the work represented by this instance.
        /// @return Whatever is the result of the query SQL execution in the database.
        /// @throws SQLException If the database produces some failure.
        /// @throws IllegalArgumentException If `work` is `null`.
        public Object operate(@NonNull SqlWorker work) throws SQLException;
    }

    @NonNull
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    private static SpecialFunc selectOperation(@NonNull Method m, @NonNull QuerySql q) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.
        checkNotNull(q); // Check recognized by lombok.

        if (Methods.isSimple(m)) {
            throw new BadImplementationException("Unsupported annotation @Query on " + name(m), m.getDeclaringClass());
        }

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

        var raw = m.getReturnType();
        if (raw == List.class) return work -> work.list(rt, fields);
        if (raw == Optional.class) return work -> work.read(rt, fields);
        if (rtb == OptionalInt.class) return work -> asInt(work.read(Integer.class, fields));
        if (rtb == OptionalLong.class) return work -> asLong(work.read(Long.class, fields));
        if (rtb == OptionalDouble.class) return work -> asDouble(work.read(Double.class, fields));
        return work -> work.read(rt, fields).orElse(null);
    }

    /// Compiles the [@QuerySql][QuerySql]-annotated method `m` into a [CallContext] that
    /// executes the corresponding SELECT statement and maps the result rows on every invocation.
    ///
    /// The expected properties are [ConnectionFactoryKeyProperty], [ConverterFactoryKeyProperty] and [LocalizerKeyProperty].
    ///
    /// @param <E> The DAO interface type.
    /// @param k The DAO interface class.
    /// @param m The annotated method to compile.
    /// @param props The property bag used to resolve runtime dependencies.
    /// @return A [CallContext] ready for repeated invocation; never `null`.
    /// @throws BadImplementationException If the method's return type uses a wildcard type
    ///         parameter; if `fields` specifies more than one index for a non-record element
    ///         type; or if the number of indices in `fields` does not match the number of
    ///         components in the target record type.
    /// @throws PropertyBag.PropertyNotFoundException If the `dependencies` does not contain the right properties.
    /// @throws IllegalArgumentException If any argument is `null`, or if `m` is not declared
    ///         on `k` or a supertype of `k`, or if `m` is not annotated with [@QuerySql][QuerySql].
    @NonNull
    @Override
    @SuppressWarnings("Convert2Lambda") // Because there is @NonNull on lambda parameters.
    public <E> CallContext<E> prepare(
            @NonNull Class<E> k,
            @NonNull Method m,
            @NonNull PropertyBag props)
            throws BadImplementationException, PropertyBag.PropertyNotFoundException
    {
        if (!k.isAssignableFrom(m.getDeclaringClass())) throw new IllegalArgumentException();
        var q = m.getAnnotation(QuerySql.class);
        if (q == null) throw new IllegalArgumentException();

        var operation = selectOperation(m, q);
        var parset = new ParameterSet(m);
        var strict = q.validate();
        var supplier = ParsedSqlSupplier.find(strict, parset);

        var connect = props.get(ConnectionFactoryKeyProperty.INSTANCE);
        var cvt = props.get(ConverterFactoryKeyProperty.INSTANCE);
        var localizer = props.get(LocalizerKeyProperty.INSTANCE);

        return new CallContext<>() {

            /// {@inheritDoc}
            @Override
            public Object execute(@NonNull E instance, @NonNull Object... a) throws SQLException, ParameterReceiver.IllegalValueException {
                var query = supplier.get();
                var params = parset.withValues(a);
                var work = new SqlWorker(connect.get(), params, query, cvt, localizer);
                return operation.operate(work);
            }
        };
    }

    @Nullable
    private static OptionalInt asInt(@NonNull Optional<Integer> opt) {
        checkNotNull(opt); // Check recognized by lombok.
        return opt.isEmpty() ? OptionalInt.empty() : OptionalInt.of(opt.get());
    }

    @Nullable
    private static OptionalLong asLong(@NonNull Optional<Long> opt) {
        checkNotNull(opt); // Check recognized by lombok.
        return opt.isEmpty() ? OptionalLong.empty() : OptionalLong.of(opt.get());
    }

    @Nullable
    private static OptionalDouble asDouble(@NonNull Optional<Double> opt) {
        checkNotNull(opt); // Check recognized by lombok.
        return opt.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(opt.get());
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
