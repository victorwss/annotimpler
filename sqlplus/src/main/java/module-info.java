@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.sqlplus {
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

    exports ninja.javahacker.annotimpler;
    exports ninja.javahacker.magicfactory;
    exports ninja.javahacker.sqlplus;
    exports ninja.javahacker.sqlplus.conn;
    exports ninja.javahacker.sqlplus.meta;
    exports ninja.javahacker.sqlplus.sqlimpl;
    exports ninja.javahacker.sqlplus.stmt;
}