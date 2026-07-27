/// Serializes supported `java.lang.reflect.Type` implementations by converting them
/// into immutable, serializable surrogates and reconstructing them on demand.
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.typeser {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    exports ninja.javahacker.typeser;
}