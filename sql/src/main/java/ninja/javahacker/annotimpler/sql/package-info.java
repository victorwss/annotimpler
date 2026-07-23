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
/// - [ExecuteSql @ExecuteSql] — DML operations such as
///   `INSERT`, `UPDATE`, or `DELETE`.
/// - [GenerateSql @GenerateSql] — DML operations that also
///   return auto-generated keys.
/// - [QuerySql @QuerySql] — `SELECT` operations that
///   return results mapped to Java types.
///
/// ## SQL-source annotations
///
/// A separate annotation specifies where the SQL string comes from:
///
/// - [Sql @Sql] — inline SQL literal.
/// - [SqlFromFile @SqlFromFile] — filesystem file.
/// - [SqlFromResource @SqlFromResource] — classpath resource.
/// - [SqlFromUrl @SqlFromUrl] — HTTP/HTTPS URL.
/// - [SqlFromClass @SqlFromClass] — custom
///   [SqlSupplier] implementation.
///
/// ## Entry points
///
/// - [ConnectionFactory] — creates [Connection]
///   instances and is the starting point for implementing annotated interfaces.
/// - [Transactor] — wraps an object's method calls in
///   database transactions, committing on success and rolling back on failure.
package ninja.javahacker.annotimpler.sql;

import module java.sql;
import module ninja.javahacker.annotimpler.sql;
