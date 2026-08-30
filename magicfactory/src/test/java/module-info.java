@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.test.magicfactory {
    requires java.sql;
    requires ninja.javahacker.magicfactory;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires static lombok;
    requires static com.github.spotbugs.annotations;
}