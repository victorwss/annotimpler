/**
 * Hibernate provider for JPA Simple Transactions.
 */
@SuppressWarnings({
    "requires-automatic", "requires-transitive-automatic" // com.github.spotbugs.annotations, org.hibernate.orm.core
})
module ninja.javahacker.annotimpler.hibernate {
    requires transitive static lombok;
    requires transitive static com.github.spotbugs.annotations;
    requires transitive jakarta.persistence;
    requires transitive ninja.javahacker.annotimpler.core;
    requires transitive org.hibernate.orm.core;
    exports ninja.javahacker.annotimpler.hibernate;
    provides ninja.javahacker.annotimpler.jpa.ProviderAdapter
            with ninja.javahacker.annotimpler.hibernate.HibernateAdapter;
}