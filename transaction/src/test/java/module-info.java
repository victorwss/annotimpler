@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.transaction {
    requires transitive ninja.javahacker.transaction;

    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;

    requires static lombok;
    requires static com.github.spotbugs.annotations;
    requires java.sql;
}