package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.sql;

/// A supplier of SQL strings that may throw [SQLException].
///
/// Implementations of this interface produce a SQL string on each invocation.
/// The string may be constant or dynamically constructed.
/// Throwing [SQLException] allows implementations to signal failures
/// that arise during SQL retrieval (e.g., loading from a database resource).
///
/// @see ParsedSqlSupplier
@FunctionalInterface
public interface SqlSupplier {

    /// Returns the SQL string for this supplier.
    ///
    /// @return The SQL string; never `null`.
    /// @throws SQLException If the SQL string cannot be obtained.
    @NonNull
    public String get() throws SQLException;
}
