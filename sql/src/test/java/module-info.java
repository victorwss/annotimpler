@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.annotimpler.sql {
    requires transitive ninja.javahacker.annotimpler.sql;

    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.xerial.sqlitejdbc;
    requires com.h2database;
    requires static lombok;
    requires static com.github.spotbugs.annotations;

    exports ninja.javahacker.test.rsc;
}