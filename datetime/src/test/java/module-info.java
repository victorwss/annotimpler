@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.datetime {
    requires ninja.javahacker.datetime;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires static lombok;
    requires static com.github.spotbugs.annotations;
}