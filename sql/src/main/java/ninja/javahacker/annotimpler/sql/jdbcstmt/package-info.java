/// JDBC statement execution infrastructure for the Annotimpler SQL module.
///
/// This package provides the low-level building blocks used internally to execute SQL operations
/// against a live [java.sql.Connection]:
///
/// - [NamedParameterStatement] — a [java.sql.PreparedStatement] extension that supports
///   named parameters (e.g. `:userId`) in place of positional `?` markers, and provides
///   convenience `setXxx(String name, ...)` overloads for every JDBC setter.
/// - [SmartResultSet] — a [java.sql.ResultSet] wrapper that adds type-aware column value
///   retrieval, case-insensitive column name lookup, and automatic mapping of rows to Java
///   `record` types.
/// - [SqlWorker] — a high-level, single-use executor that ties the above together:
///   it prepares the statement, binds the parameters, and returns results as Java objects.
package ninja.javahacker.annotimpler.sql.jdbcstmt;