/// Provides converters for transforming arbitrary input values into typed Java objects,
/// including primitives, wrappers, date/time types, SQL types, enums, records, and collections.
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.convert {
    requires transitive java.sql;

    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    requires transitive ninja.javahacker.magicfactory;
    requires ninja.javahacker.datetime;
    requires ninja.javahacker.typeser;

    exports ninja.javahacker.annotimpler.convert;
}