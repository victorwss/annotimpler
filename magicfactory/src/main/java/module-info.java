/// Module providing the `MagicFactory` API for reflective instance creation.
///
/// The core entry point is `ninja.javahacker.annotimpler.magicfactory.MagicFactory`,
/// which discovers and caches the appropriate constructor, factory method, or constant
/// field for a given public class, then allows repeated instantiation through
/// `MagicFactory.create(Object...)`.
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
open module ninja.javahacker.annotimpler.magicfactory {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive static lombok;

    exports ninja.javahacker.annotimpler.magicfactory;
}