package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;

/// A factory that produces a [SqlSupplier] for a given method.
///
/// Implementations are associated with a specific SQL annotation type via the
/// [SqlSource] meta-annotation. When [ParsedSqlSupplier#find] resolves a method,
/// it instantiates the appropriate `SqlFactory` and calls [prepare] to obtain the
/// [SqlSupplier] that will produce the SQL string for every subsequent invocation.
///
/// @see SqlSource
/// @see ParsedSqlSupplier#find
@FunctionalInterface
public interface SqlFactory {

    /// Prepares a [SqlSupplier] for the given method.
    ///
    /// This method is called once per method during set-up. The returned [SqlSupplier]
    /// may then be invoked repeatedly, once per actual SQL execution.
    ///
    /// @param m The method for which to prepare the supplier; must not be `null`.
    /// @return A [SqlSupplier] that will produce the SQL string for `m`; never `null`.
    /// @throws BadImplementationException If the method is missing required annotations or
    ///         cannot be used to construct a valid [SqlSupplier].
    /// @throws IllegalArgumentException If `m` is `null`.
    @NonNull
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException;

}
