/**
 * OpenJPA provider for JPA Simple Transactions.
 */
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations, org.apache.openjpa
})
module ninja.javahacker.annotimpler.openjpa {
    requires transitive static lombok;
    requires transitive static com.github.spotbugs.annotations;
    requires transitive jakarta.persistence;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive org.apache.openjpa;
    exports ninja.javahacker.annotimpler.openjpa;
    provides ninja.javahacker.annotimpler.jpa.ProviderAdapter
            with ninja.javahacker.annotimpler.openjpa.OpenJpaAdapter;
}