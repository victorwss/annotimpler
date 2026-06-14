package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;

/// A supplier of [ParsedQuery] objects that may throw [java.sql.SQLException].
///
/// This functional interface is the primary abstraction for obtaining a fully-parsed SQL
/// query at call time. Each invocation of [get] retrieves the raw SQL string, parses it
/// into a [ParsedQuery], and (in strict mode) validates that the SQL is well-formed and
/// that its parameter names exactly match the enclosing method's parameters.
///
/// Use [find] to construct an instance from a [ParameterSet] by scanning the method's
/// annotations for the single [SqlSource]-meta-annotated annotation.
///
/// @see SqlSource
/// @see SqlFactory
/// @see ParsedQuery
@FunctionalInterface
public interface ParsedSqlSupplier {

    /// Returns the [ParsedQuery] for the current invocation.
    ///
    /// @return The parsed SQL query; never `null`.
    /// @throws java.sql.SQLException If the SQL string cannot be obtained or is malformed
    ///         (in strict mode).
    public ParsedQuery get() throws SQLException;

    /// Scans `pset.getMethod()` for an SQL annotation and returns a [ParsedSqlSupplier] for it.
    ///
    /// The method performs the following steps:
    /// 1. Collects all annotations on the method whose type is meta-annotated with [SqlSource].
    /// 2. Verifies that exactly one such annotation is present.
    /// 3. Reads the [SqlSource#value] attribute to determine the [SqlFactory] class.
    /// 4. Instantiates the [SqlFactory] via [ninja.javahacker.annotimpler.magicfactory.MagicFactory].
    /// 5. Calls [SqlFactory#prepare] to get the underlying [SqlSupplier].
    /// 6. Returns a [ParsedSqlSupplier] that, on each [get] call, retrieves the SQL string,
    ///    parses it into a [ParsedQuery], and — when `strict` is `true` — validates that the
    ///    query has no errors and that its named parameters match the method's parameters exactly.
    ///
    /// @param strict If `true`, each call to the returned supplier validates that the SQL has
    ///               no errors and that its parameter names match the method parameters exactly.
    /// @param pset The parameter set whose method will be inspected for SQL annotations;
    ///             must not be `null`.
    /// @return A [ParsedSqlSupplier] backed by the factory resolved from `pset.getMethod()`;
    ///         never `null`.
    /// @throws BadImplementationException If no SQL annotation is found, more than one is found,
    ///         or the [SqlFactory] cannot be instantiated.
    /// @throws IllegalArgumentException If `pset` is `null`.
    @NonNull
    public static ParsedSqlSupplier find(boolean strict, @NonNull ParameterSet pset) throws BadImplementationException {
        var m = pset.getMethod();
        var name = NameDictionary.global().getSimplifiedGenericString(m, true);
        var annos = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(SqlSource.class)).toList();
        if (annos.isEmpty()) throw new BadImplementationException("No SQL annotation found on " + name, m.getDeclaringClass());
        if (annos.size() > 1) throw new BadImplementationException("More than one SQL annotation found on " + name, m.getDeclaringClass());
        var sqls = annos.getFirst().annotationType().getAnnotation(SqlSource.class);
        if (sqls == null) throw new AssertionError();
        var cls = sqls.value();
        SqlFactory factory;
        try {
            factory = MagicFactory.of(cls).create();
        } catch (MagicFactory.CreatorSelectionException | MagicFactory.CreationException x) {
            throw new BadImplementationException(
                    "Can't instantiate " + cls.getSimpleName() + " to handle " + name,
                    x,
                    m.getDeclaringClass()
            );
        }
        var sup = factory.prepare(m);
        return () -> {
            checkNotNull(pset);
            var sql = sup.get();
            var pq = ParsedQuery.parse(sql);
            if (strict) {
                if (pq.hasErrors()) throw new SQLException("Malformed SQL for " + name);
                var names1 = new HashSet<>(pset.paramNames());
                var names2 = pq.params().keySet();
                if (!Objects.equals(names1, names2)) throw new SQLException("Method parameters mismatches SQL for " + name);
            }
            return pq;
        };
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
