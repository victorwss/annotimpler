@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.annotimpler.core {
    requires transitive ninja.javahacker.annotimpler.core;

    requires transitive org.junit.jupiter.api;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.paramnames;
    requires static lombok;
    requires static com.github.spotbugs.annotations;

    exports ninja.javahacker.test;
    exports ninja.javahacker.test.annotimpler.core;
}