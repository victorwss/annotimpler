package ninja.javahacker.annotimpler.sql.sqlimpl;

import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

/// [Implementation] that backs DAO methods annotated with [@GenerateSql][GenerateSql].
///
/// At preparation time this class validates the method's return type and builds a compiled
/// [CallContext]. On every invocation the context executes the INSERT statement and retrieves
/// the auto-generated key(s) produced by the database, delivering them in the return type
/// requested by the method.
///
/// **Supported return types**
///
/// | Return type | Value returned |
/// |---|---|
/// | `int` / `Integer` | First generated key as `int`, or `null` if absent. |
/// | `OptionalInt` | First generated key wrapped in [OptionalInt], or [OptionalInt#empty()]. |
/// | `long` / `Long` | First generated key as `long`, or `null` if absent. |
/// | `OptionalLong` | First generated key wrapped in [OptionalLong], or [OptionalLong#empty()]. |
/// | `List<Integer>` | All generated keys as a list of [Integer]. |
/// | `List<Long>` | All generated keys as a list of [Long]. |
///
/// Any other return type (including raw `List`, `List<String>`, or arbitrary types) causes
/// [BadImplementationException] to be thrown at preparation time.
public enum GenerateSqlImplementation implements Implementation {
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
    private static SpecialFunc selectOperation(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.

        if (Methods.isSimple(m)) {
            throw new BadImplementationException("Unsupported annotation @Generate on " + name(m), m.getDeclaringClass());
        }

        var rtb = m.getGenericReturnType();

        if (rtb == long.class) return SqlWorker::generateLong;
        if (rtb == Long.class) return SqlWorker::generateLongOrNull;
        if (rtb == OptionalLong.class) return SqlWorker::generateOptionalLong;
        if (rtb == int.class) return SqlWorker::generate;
        if (rtb == Integer.class) return SqlWorker::generateOrNull;
        if (rtb == OptionalInt.class) return SqlWorker::generateOptional;

        if (rtb instanceof ParameterizedType pt && pt.getRawType() == List.class) {
            var p2 = pt.getActualTypeArguments()[0];
            if (p2 == Integer.class) return SqlWorker::generateList;
            if (p2 == Long.class) return SqlWorker::generateLongList;
            throw new BadImplementationException("Unsupported return @Generate list type on: " + name(m), m.getDeclaringClass());
        }

        var raw = m.getReturnType();
        if (raw == List.class) {
            throw new BadImplementationException("Incomplete return @Generate list type on: " + name(m), m.getDeclaringClass());
        }

        throw new BadImplementationException("Unsupported return @Generate type on: " + name(m), m.getDeclaringClass());
    }

    /// Compiles the [@GenerateSql][GenerateSql]-annotated method `m` into a [CallContext]
    /// that executes the corresponding INSERT statement and returns the auto-generated key(s)
    /// on every invocation.
    ///
    /// The expected properties are [ConnectionFactoryKeyProperty], [ConverterFactoryKeyProperty] and [LocalizerKeyProperty].
    ///
    /// @param <E> The DAO interface type.
    /// @param k The DAO interface class.
    /// @param m The annotated method to compile.
    /// @param props The property bag used to resolve runtime dependencies.
    /// @return A [CallContext] ready for repeated invocation; never `null`.
    /// @throws BadImplementationException If the method's return type is not one of the
    ///         supported types (`int`, `Integer`, `OptionalInt`, `long`, `Long`, `OptionalLong`,
    ///         `List<Integer>`, `List<Long>`).
    /// @throws PropertyBag.PropertyNotFoundException If the `dependencies` does not contain the right properties.
    /// @throws IllegalArgumentException If any argument is `null`, or if `m` is not declared
    ///         on `k` or a supertype of `k`, or if `m` is not annotated with [@GenerateSql][GenerateSql].
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
        var g = m.getAnnotation(GenerateSql.class);
        if (g == null) throw new IllegalArgumentException();

        var operation = selectOperation(m);
        var parset = new ParameterSet(m);
        var strict = g.validate();
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

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
