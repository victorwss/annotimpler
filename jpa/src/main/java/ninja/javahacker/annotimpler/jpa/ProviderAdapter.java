package ninja.javahacker.annotimpler.jpa;

import module jakarta.persistence;
import module java.sql;
import module java.base;

import lombok.NonNull;

/// Service Provider Interface used to get access to vendor-specific features of JPA Providers.
///
/// Implementations of this class are expected to be instantiated by the means of the [ServiceLoader]
/// mechanism. Instances can be obtained via the [#findFor(EntityManager)] static method.
///
/// @author Victor Williams Stafusa da Silva
public interface ProviderAdapter {

    /// Tests if the persistence provider represented by `this` recognizes an [EntityManager]
    /// as one of theirs [EntityManager].
    /// @param em The [EntityManager] to be tested.
    /// @return `true` if the [EntityManager] is recognized, `false` otherwise.
    /// @throws IllegalArgumentException If `em` is `null`.
    public default boolean recognizes(@NonNull EntityManager em) {
        return recognizes(em.getEntityManagerFactory());
    }

    /// Tests if the persistence provider represented by `this` recognizes an [EntityManagerFactory]
    /// as one of theirs [EntityManagerFactory].
    /// @param emf The [EntityManagerFactory] to be tested.
    /// @return `true` if the [EntityManagerFactory] is recognized, `false` otherwise.
    /// @throws IllegalArgumentException If `emf` is `null`.
    public boolean recognizes(@NonNull EntityManagerFactory emf);

    /// Tests if the persistence provider represented by `this` recognizes an [EntityManager]
    /// as one of theirs [EntityManager] and return it if it indeed does.
    /// @param em The [EntityManager] to be tested.
    /// @return The given `em` parameter object.
    /// @throws IllegalArgumentException If `em` is `null` or is not recognized.
    public default EntityManager ensureRecognition(@NonNull EntityManager em) {
        if (recognizes(em)) return em;
        var a = em.getClass().getName();
        var b = getClass().getName();
        throw new IllegalArgumentException("That EntityManager (" + a + ") is not recognized by this ProviderAdapter (" + b + ").");
    }

    /// Tests if the persistence provider represented by `this` recognizes an [EntityManagerFactory]
    /// as one of theirs [EntityManagerFactory] and return it if it indeed does.
    /// @param emf The [EntityManagerFactory] to be tested.
    /// @return The given `emf` parameter object.
    /// @throws IllegalArgumentException If `emf` is `null` or is not recognized.
    public default EntityManagerFactory ensureRecognition(@NonNull EntityManagerFactory emf) {
        if (recognizes(emf)) return emf;
        var a = emf.getClass().getName();
        var b = getClass().getName();
        throw new IllegalArgumentException("That EntityManagerFactory (" + a + ") is not recognized by this ProviderAdapter (" + b + ").");
    }

    /// Obtains the underlying [Connection] used by an [EntityManager].
    /// This makes this method useful to use JDBC directly inside JPA transactions.
    /// @param em The [EntityManager] to acquire the underlying [Connection].
    /// @return The underlying [Connection] used by the given [EntityManager].
    /// @throws IllegalArgumentException If `em` is `null`.
    public default Connection getConnection(@NonNull EntityManager em) {
        return em.unwrap(Connection.class);
    }

    /// Obtains the underlying [PersistenceProvider] wrapped by this `ProviderAdapter`.
    /// @return The underlying [PersistenceProvider] wrapped by this `ProviderAdapter`.
    public PersistenceProvider getJpaProvider();

    /// Determines if a reconnection should be automatically tried if the underlying connection is lost in the case of the given
    /// exception happening.
    /// @param e Some exception that could be handled by automatically reconnecting to the database.
    /// @return `true` if a an automatic reconnection could possibly handle the exception, `false` if this is impossible
    ///     or unlikely.
    /// @throws IllegalArgumentException If `e` is `null`.
    public default boolean shouldTryToReconnect(@NonNull RuntimeException e) {
        return false;
    }

    /// Finds a suitable [ProviderAdapter] for the given [EntityManagerFactory].
    /// @implSpec The list of knows [ProviderAdapter] is reloaded in every call to this method.
    /// @param emf The given [EntityManagerFactory].
    /// @return The [ProviderAdapter] found.
    /// @throws UnsupportedOperationException No known [ProviderAdapter] recognized the given [EntityManagerFactory].
    /// @throws IllegalArgumentException If `emf` is `null`.
    public static ProviderAdapter findFor(@NonNull EntityManagerFactory emf) {
        for (ProviderAdapter impl : ServiceLoader.load(ProviderAdapter.class)) {
            if (impl.recognizes(emf)) return impl;
        }
        var a = emf.getClass().getName();
        throw new UnsupportedOperationException("That EntityManagerFactory (" + a + ") is not recognized by any know ProviderAdapter.");
    }

    /// Finds a suitable [ProviderAdapter] for the given [EntityManager].
    /// @implSpec The list of knows [ProviderAdapter] is reloaded in every call to this method.
    /// @param em The given [EntityManager].
    /// @return The [ProviderAdapter] found.
    /// @throws UnsupportedOperationException No known [ProviderAdapter] recognized the given [EntityManager].
    /// @throws IllegalArgumentException If `em` is `null`.
    public static ProviderAdapter findFor(@NonNull EntityManager em) {
        for (ProviderAdapter impl : ServiceLoader.load(ProviderAdapter.class)) {
            if (impl.recognizes(em)) return impl;
        }
        var a = em.getClass().getName();
        throw new UnsupportedOperationException("That EntityManager (" + a + ") is not recognized by any known ProviderAdapter.");
    }

    /// Streams all of the known [ProviderAdapter]s.
    /// @return All the [ProviderAdapter]s found, wrapped inside [Supplier]s of [Maybe]s
    ///     because some of them might fail to load.
    /// @implSpec The list of known [ProviderAdapter]s is reloaded in every call to this method.
    public static Stream<Supplier<Maybe<ProviderAdapter>>> all() {
        return ServiceLoader.load(ProviderAdapter.class).stream().map(Maybe::wrap);
    }

    // ///
    // /// Produces an object for configuring a persistence unit in order to create [Connector]s.
    // /// @return An object for configuring a persistence unit in order to create [Connector]s.
    // ///
    // public ProviderConnectorFactory<?> config();
}