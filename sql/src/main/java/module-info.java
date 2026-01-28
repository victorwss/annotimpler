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
    requires transitive ninja.javahacker.annotimpler.core;

    exports ninja.javahacker.annotimpler.sql;
    exports ninja.javahacker.annotimpler.sql.conn;
    exports ninja.javahacker.annotimpler.sql.meta;
    exports ninja.javahacker.annotimpler.sql.sqlfactories;
    exports ninja.javahacker.annotimpler.sql.sqlimpl;
    exports ninja.javahacker.annotimpler.sql.stmt;
}