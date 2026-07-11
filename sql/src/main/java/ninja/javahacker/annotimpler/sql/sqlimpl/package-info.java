/// Runtime implementations that back the [@ExecuteSql][ninja.javahacker.annotimpler.sql.ExecuteSql],
/// [@GenerateSql][ninja.javahacker.annotimpler.sql.GenerateSql]
/// and [@QuerySql][ninja.javahacker.annotimpler.sql.QuerySql] annotations.
///
/// Each class in this package is an [Implementation][ninja.javahacker.annotimpler.core.Implementation]
/// that is selected at DAO creation time by [AnnotationsImplementor] and is responsible for compiling a particular kind of SQL
/// operation from the annotated method signature and for executing it against the database on
/// every invocation.
///
/// Instances are obtained indirectly through [AnnotationsImplementor]; they are not normally
/// constructed by application code.
package ninja.javahacker.annotimpler.sql.sqlimpl;

import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;
