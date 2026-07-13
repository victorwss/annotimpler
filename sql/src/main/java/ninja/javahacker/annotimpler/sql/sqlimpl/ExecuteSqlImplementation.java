package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

/// [Implementation] that backs DAO methods annotated with [@ExecuteSql][ExecuteSql].
///
/// At preparation time (i.e., when the DAO interface is first instantiated via
/// [AnnotationsImplementor]) this class validates the method's return type and builds a
/// compiled [CallContext]. On every invocation the context executes the DML statement,
/// checks the number of affected rows against the `acceptsZero` and `acceptsMulti` flags
/// declared on the annotation, and returns the row count in the requested type.
///
/// **Supported return types**
///
/// | Return type | Value returned |
/// |---|---|
/// | `void` / `Void` | `null` (no value) |
/// | `long` / `Long` | Exact row count as a `long`. |
/// | `int` / `Integer` | Row count capped at [Integer#MAX_VALUE]. |
///
/// Any other return type causes [BadImplementationException] to be thrown at preparation time.
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
public final class ExecuteSqlImplementation implements Implementation {

    @NonNull
    private final ConnectionFactory connect;

    @NonNull
    private final ConverterFactory cvt;

    /// The locale used for case-insensitive column name matching.
    @NonNull
    private final Locale localizer;

    /// Creates a new instance, extracting the required dependencies from the given
    /// [PropertyBag].
    ///
    /// @param dependencies The property bag from which the [ConnectionFactory],
    ///        [ConverterFactory] and [Locale] (localizer) are extracted.
    /// @throws PropertyBag.PropertyNotFoundException If the `dependencies` does not contain the right properties.
    /// @throws IllegalArgumentException If `dependencies` is `null`.
    @SuppressFBWarnings("DRE_DECLARED_RUNTIME_EXCEPTION")
    public ExecuteSqlImplementation(@NonNull PropertyBag dependencies) throws PropertyBag.PropertyNotFoundException {
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
        checkNotNull(m); // Check recognized by lombok.
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @NonNull
    private static LongFunction<Object> selectOperation(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.
        var rtb = m.getReturnType();

        if (Methods.isSimple(m)) {
            throw new BadImplementationException("Unsupported annotation @Execute on " + name(m), m.getDeclaringClass());
        }
        if (rtb == long.class || rtb == Long.class) return qtd -> qtd;
        if (rtb == int.class || rtb == Integer.class) return qtd -> (int) Long.min(qtd, Integer.MAX_VALUE);
        if (rtb == void.class || rtb == Void.class) return qtd -> null;
        throw new BadImplementationException("Unsupported return @Execute type on " + name(m), m.getDeclaringClass());
    }

    /// Compiles the [@ExecuteSql][ExecuteSql]-annotated method `m` into a [CallContext] that
    /// executes the corresponding DML statement on every invocation.
    ///
    /// @param <E> The DAO interface type.
    /// @param k The DAO interface class.
    /// @param m The annotated method to compile.
    /// @param props The property bag used to resolve runtime dependencies.
    /// @return A [CallContext] ready for repeated invocation; never `null`.
    /// @throws BadImplementationException If the method's return type is not one of the
    ///         supported types (`void`, `Void`, `long`, `Long`, `int`, `Integer`).
    /// @throws IllegalArgumentException If any argument is `null`, or if `m` is not declared
    ///         on `k` or a supertype of `k`, or if `m` is not annotated with [@ExecuteSql][ExecuteSql].
    @NonNull
    @Override
    @SuppressWarnings("Convert2Lambda") // Because there is @NonNull on lambda parameters.
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
            public Object execute(@NonNull E instance, @NonNull Object... a) throws SQLException, ParameterReceiver.IllegalValueException {
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
