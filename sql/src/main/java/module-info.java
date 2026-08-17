import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// The Annotimpler SQL core module — reusable SQL-source and transaction abstractions.
///
/// This module provides backend-agnostic infrastructure used by concrete execution layers
/// (such as JDBC and JPA-oriented modules). It focuses on SQL-source resolution, parameter
/// binding metadata, and generic transaction orchestration.
///
/// **Key packages**
///
/// | Package | Purpose |
/// |---|---|
/// | [ninja.javahacker.annotimpler.sql] | Public API: SQL-source annotations, read policies, and transaction wrapper abstractions. |
/// | [ninja.javahacker.annotimpler.sql.sqlfactories] | Implementations of [SqlFactory] for file, URL, and string sources. |
/// | [ninja.javahacker.annotimpler.sql.meta] | SQL loading, parameter binding, and factory-resolution infrastructure. |
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.sql {
    requires transitive java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.convert;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive ninja.javahacker.annotimpler.magicfactory;
    requires transitive ninja.javahacker.transaction;
    requires ninja.javahacker.datetime;
    requires ninja.javahacker.typeser;

    exports ninja.javahacker.annotimpler.sql;
    exports ninja.javahacker.annotimpler.sql.meta;
    exports ninja.javahacker.annotimpler.sql.sqlfactories;
}