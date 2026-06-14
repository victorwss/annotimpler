package ninja.javahacker.annotimpler.sql.sqlimpl;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
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
public final class GenerateSqlImplementation implements Implementation {

    @NonNull
    private final ConnectionFactory connect;

    @NonNull
    private final ConverterFactory cvt;

    @NonNull
    private final Locale localizer;

    /// Creates a new instance, extracting the required dependencies from the given
    /// [PropertyBag].
    ///
    /// @param dependencies The property bag from which the [ConnectionFactory],
    ///        [ConverterFactory] and [Locale] (localizer) are extracted.
    /// @throws IllegalArgumentException If `dependencies` is `null`.
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
        checkNotNull(m); // Check recognized by lombok.
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @FunctionalInterface
    private static interface SpecialFunc {
        public Object operate(SqlWorker work) throws SQLException;
    }

    @NonNull
    private static SpecialFunc selectOperation(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.

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

    /// Compiles the [@GenerateSql][GenerateSql]-annotated method `m` into a [CallContext]
    /// that executes the corresponding INSERT statement and returns the auto-generated key(s)
    /// on every invocation.
    ///
    /// @param <E> The DAO interface type.
    /// @param k The DAO interface class.
    /// @param m The annotated method to compile.
    /// @param props The property bag used to resolve runtime dependencies.
    /// @return A [CallContext] ready for repeated invocation; never `null`.
    /// @throws BadImplementationException If the method's return type is not one of the
    ///         supported types (`int`, `Integer`, `OptionalInt`, `long`, `Long`, `OptionalLong`,
    ///         `List<Integer>`, `List<Long>`).
    /// @throws IllegalArgumentException If any argument is `null`, or if `m` is not declared
    ///         on `k` or a supertype of `k`, or if `m` is not annotated with [@GenerateSql][GenerateSql].
    @NonNull
    @Override
    public <E> CallContext<E> prepare(
            @NonNull Class<E> k,
            @NonNull Method m,
            @NonNull PropertyBag props)
            throws BadImplementationException
    {
        if (!k.isAssignableFrom(m.getDeclaringClass())) throw new IllegalArgumentException();
        var g = m.getAnnotation(GenerateSql.class);
        if (g == null) throw new IllegalArgumentException();

        var operation = selectOperation(m);
        var parset = new ParameterSet(m);
        var strict = g.validate();
        var supplier = ParsedSqlSupplier.find(strict, parset);

        return new CallContext<>() {
            @Override
            public Object execute(@NonNull E instance, @NonNull Object... a) throws SQLException, ParameterReceiver.IllegalValueException {
                var query = supplier.get();
                var params = parset.withValues(a);
                var work = new SqlWorker(getConnection(), params, query, cvt, localizer);
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
