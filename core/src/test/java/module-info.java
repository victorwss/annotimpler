@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.annotimpler.core {
    requires transitive ninja.javahacker.annotimpler.core;

    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;

    requires static lombok;
    requires static com.github.spotbugs.annotations;

    exports ninja.javahacker.test;
    exports ninja.javahacker.test.annotimpler.core;
}