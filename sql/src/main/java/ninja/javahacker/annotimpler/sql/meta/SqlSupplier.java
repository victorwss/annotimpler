package ninja.javahacker.annotimpler.sql.meta;

import java.sql.SQLException;

/// A supplier of SQL strings that may throw [java.sql.SQLException].
///
/// Implementations of this interface produce a SQL string on each invocation.
/// The string may be constant or dynamically constructed.
/// Throwing [java.sql.SQLException] allows implementations to signal failures
/// that arise during SQL retrieval (e.g., loading from a database resource).
///
/// @see ParsedSqlSupplier
@FunctionalInterface
public interface SqlSupplier {

    /// Returns the SQL string for this supplier.
    ///
    /// @return The SQL string; never `null`.
    /// @throws java.sql.SQLException If the SQL string cannot be obtained.
    public String get() throws SQLException;
}
