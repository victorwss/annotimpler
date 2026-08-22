@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.limited {
    requires transitive ninja.javahacker.annotimpler.limited;

    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;

    requires static lombok;
    requires static com.github.spotbugs.annotations;
}
