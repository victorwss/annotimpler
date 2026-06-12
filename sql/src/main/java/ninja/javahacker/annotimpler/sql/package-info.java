/// Root package of the Annotimpler SQL module.
///
/// This package contains the public-facing annotation types and supporting infrastructure used
/// to define SQL-backed method implementations through annotation-driven programming.
/// Annotated interface methods are implemented automatically at runtime by the framework —
/// no manual JDBC code is required in calling code.
///
/// ## SQL-operation annotations
///
/// Three mutually exclusive annotations mark the kind of SQL operation a method performs:
///
/// - [ninja.javahacker.annotimpler.sql.ExecuteSql @ExecuteSql] — DML operations such as
///   `INSERT`, `UPDATE`, or `DELETE`.
/// - [ninja.javahacker.annotimpler.sql.GenerateSql @GenerateSql] — DML operations that also
///   return auto-generated keys.
/// - [ninja.javahacker.annotimpler.sql.QuerySql @QuerySql] — `SELECT` operations that
///   return results mapped to Java types.
///
/// ## SQL-source annotations
///
/// A separate annotation specifies where the SQL string comes from:
///
/// - [ninja.javahacker.annotimpler.sql.Sql @Sql] — inline SQL literal.
/// - [ninja.javahacker.annotimpler.sql.SqlFromFile @SqlFromFile] — filesystem file.
/// - [ninja.javahacker.annotimpler.sql.SqlFromResource @SqlFromResource] — classpath resource.
/// - [ninja.javahacker.annotimpler.sql.SqlFromUrl @SqlFromUrl] — HTTP/HTTPS URL.
/// - [ninja.javahacker.annotimpler.sql.SqlFromClass @SqlFromClass] — custom
///   [ninja.javahacker.annotimpler.sql.meta.SqlSupplier] implementation.
///
/// ## Entry points
///
/// - [ninja.javahacker.annotimpler.sql.ConnectionFactory] — creates [java.sql.Connection]
///   instances and is the starting point for implementing annotated interfaces.
/// - [ninja.javahacker.annotimpler.sql.Transactor] — wraps an object's method calls in
///   database transactions, committing on success and rolling back on failure.
package ninja.javahacker.annotimpler.sql;