/// Root package of the Annotimpler SQL module.
///
/// This package contains SQL-source annotations.
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
package ninja.javahacker.annotimpler.sql;

import module ninja.javahacker.annotimpler.sql;
