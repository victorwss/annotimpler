/**
 * Eclipselink provider for JPA Simple Transactions.
 */
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations
})
module ninja.javahacker.annotimpler.eclipselink {
    requires transitive static lombok;
    requires transitive static com.github.spotbugs.annotations;
    requires transitive jakarta.persistence;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive org.eclipse.persistence.jpa;
    exports ninja.javahacker.annotimpler.eclipselink;
    provides ninja.javahacker.annotimpler.jpa.ProviderAdapter
            with ninja.javahacker.annotimpler.eclipselink.EclipselinkAdapter;
}