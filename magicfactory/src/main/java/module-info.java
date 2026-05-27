@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.magicfactory {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    exports ninja.javahacker.annotimpler.magicfactory;
}