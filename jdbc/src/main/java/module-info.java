import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// The Annotimpler JDBC module — JDBC-specific SQL execution layer.
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
/// | [ninja.javahacker.annotimpler.jdbc] | Public API: annotations, policies, DAO creation. |
/// | [ninja.javahacker.annotimpler.jdbc.conn] | JDBC [Connection] factories for common databases. |
/// | [ninja.javahacker.annotimpler.jdbc.sqlimpl] | Runtime handlers for `@ExecuteSql`, `@GenerateSql`, and `@QuerySql`. |
/// | [ninja.javahacker.annotimpler.jdbc.stmt] | Named-parameter statements, type-aware result sets, and operation executors. |
/// | [ninja.javahacker.annotimpler.limited] | Stream wrappers that enforce a maximum byte/character read limit. |
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.jdbc {
    requires transitive java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.magicfactory;
    requires transitive ninja.javahacker.annotimpler.convert;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive ninja.javahacker.annotimpler.sql;
    requires transitive ninja.javahacker.transaction;
    requires ninja.javahacker.datetime;
    requires ninja.javahacker.typeser;

    exports ninja.javahacker.annotimpler.limited;
    exports ninja.javahacker.annotimpler.jdbc;
    exports ninja.javahacker.annotimpler.jdbc.conn;
    exports ninja.javahacker.annotimpler.jdbc.sqlimpl;
    exports ninja.javahacker.annotimpler.jdbc.stmt;
}