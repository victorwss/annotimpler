/// Provides flexible multi-format date and time parsing and formatting utilities.
///
/// The central type is [ninja.javahacker.datetime.MultiFormatters], an enum whose constants
/// represent different date notation styles (field order and separator character).
/// Each constant can parse and format all standard `java.time` date/time types,
/// accepting flexible input with optional time, sub-second, and timezone components.
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.datetime {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    exports ninja.javahacker.datetime;
}