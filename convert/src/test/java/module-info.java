@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.annotimpler.convert {
    requires ninja.javahacker.annotimpler.convert;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires static lombok;
    requires static com.github.spotbugs.annotations;
}