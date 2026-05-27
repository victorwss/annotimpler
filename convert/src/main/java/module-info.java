@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.convert {
    requires transitive java.sql;

    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires ninja.javahacker.datetime;
    requires transitive ninja.javahacker.annotimpler.magicfactory;

    exports ninja.javahacker.annotimpler.convert;
}