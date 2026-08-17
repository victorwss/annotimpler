@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.transaction {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.annotimpler.magicfactory;

    exports ninja.javahacker.transaction;
}