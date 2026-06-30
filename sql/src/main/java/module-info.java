/// The Annotimpler SQL module — annotation-driven DAO generation over JDBC.
///
/// This module provides the infrastructure to turn annotated Java interfaces into fully
/// functional database access objects (DAOs) without writing boilerplate JDBC code.
/// Annotations on interface methods describe the SQL to execute and the expected return type;
/// the framework takes care of parameter binding, result-set mapping, type conversion, and
/// connection management.
///
/// **Key packages**
///
/// | Package | Purpose |
/// |---|---|
/// | [ninja.javahacker.annotimpler.sql] | Public API: annotations, policies, DAO creation. |
/// | [ninja.javahacker.annotimpler.sql.conn] | JDBC [java.sql.Connection] factories for common databases. |
/// | [ninja.javahacker.annotimpler.sql.sqlfactories] | Implementations of [ninja.javahacker.annotimpler.sql.meta.SqlFactory] for file, URL, and string sources. |
/// | [ninja.javahacker.annotimpler.sql.sqlimpl] | Runtime handlers for `@ExecuteSql`, `@GenerateSql`, and `@QuerySql`. |
/// | [ninja.javahacker.annotimpler.sql.meta] | SQL loading, parameter binding, and factory-resolution infrastructure. |
/// | [ninja.javahacker.annotimpler.sql.jdbcstmt] | Named-parameter statements, type-aware result sets, and operation executors. |
/// | [ninja.javahacker.annotimpler.limited] | Stream wrappers that enforce a maximum byte/character read limit. |
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.sql {
    requires transitive java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.paramnames;

    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.magicfactory;
    requires transitive ninja.javahacker.annotimpler.convert;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive ninja.javahacker.datetime;

    exports ninja.javahacker.annotimpler.limited;
    exports ninja.javahacker.annotimpler.sql;
    exports ninja.javahacker.annotimpler.sql.conn;
    exports ninja.javahacker.annotimpler.sql.meta;
    exports ninja.javahacker.annotimpler.sql.sqlfactories;
    exports ninja.javahacker.annotimpler.sql.sqlimpl;
    exports ninja.javahacker.annotimpler.sql.jdbcstmt;
}