/// Root package of the Annotimpler JDBC module.
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
/// ## Entry points
///
/// - [ConnectionFactory] — creates [Connection]
///   instances and is the starting point for implementing annotated interfaces.
/// - [ConnectionFactoryKeyProperty], [ConverterFactoryKeyProperty], and
///   [LocalizerKeyProperty] — typed [ninja.javahacker.annotimpler.core.KeyProperty] singletons
///   used to store configuration values in a [ninja.javahacker.annotimpler.core.PropertyBag].
package ninja.javahacker.annotimpler.jdbc;

import module java.sql;
import module ninja.javahacker.annotimpler.sql;
