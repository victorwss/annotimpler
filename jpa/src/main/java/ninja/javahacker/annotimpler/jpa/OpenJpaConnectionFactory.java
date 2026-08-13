package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;

import module jakarta.persistence;
import module java.base;

/// Immutable [EntityManagerSupplier] configured for OpenJPA.
///
/// @param persistenceUnitName The JPA persistence-unit name.
/// @param url JDBC URL.
/// @param user Database user.
/// @param password Database password.
/// @param dynamicEnhancementAgent The value of the property `openjpa.DynamicEnhancementAgent`.
/// @param runtimeUnenhancedClasses The value of the property `openjpa.RuntimeUnenhancedClasses`.
/// @param dataCache The value of the property `openjpa.DataCache`.
/// @param queryCache The value of the property `openjpa.QueryCache`.
/// @param extras Extra provider properties merged last.
public record OpenJpaConnectionFactory(
        @NonNull String persistenceUnitName,
        @NonNull String url,
        @NonNull String user,
        @NonNull String password,
        @NonNull OptionalBoolean dynamicEnhancementAgent,
        @NonNull Support runtimeUnenhancedClasses,
        @NonNull OptionalBoolean dataCache,
        @NonNull OptionalBoolean queryCache,
        @NonNull Map<String, String> extras
) implements EntityManagerSupplier
{
    /// A `OpenJpaConnectionFactory` with all fields left unspecified.
    private static final OpenJpaConnectionFactory STD = new OpenJpaConnectionFactory(
            "",
            "",
            "",
            "",
            OptionalBoolean.UNSPECIFIED,
            Support.UNSPECIFIED,
            OptionalBoolean.UNSPECIFIED,
            OptionalBoolean.UNSPECIFIED,
            Map.of()
    );

    /// Creates a `OpenJpaConnectionFactory` with the given field values.
    /// @param persistenceUnitName The JPA persistence-unit name.
    /// @param url JDBC URL.
    /// @param user Database user.
    /// @param password Database password.
    /// @param dynamicEnhancementAgent The value of the property `openjpa.DynamicEnhancementAgent`.
    /// @param runtimeUnenhancedClasses The value of the property `openjpa.RuntimeUnenhancedClasses`.
    /// @param dataCache The value of the property `openjpa.DataCache`.
    /// @param queryCache The value of the property `openjpa.QueryCache`.
    /// @param extras Extra provider properties merged last.
    /// @throws IllegalArgumentException If any argument is `null`.
    public OpenJpaConnectionFactory {}

    /// Returns an `OpenJpaConnectionFactory` with all fields left unspecified.
    /// @return An `OpenJpaConnectionFactory` with all fields left unspecified.
    @NonNull
    public static OpenJpaConnectionFactory std() {
        return STD;
    }

    /// Builds the map of OpenJPA/JPA provider properties derived from this configuration.
    /// @return The derived map.
    @NonNull
    private Map<String, String> properties() {
        var out = new HashMap<String, String>();
        out.put("jakarta.persistence.jdbc.url", url);
        out.put("jakarta.persistence.jdbc.user", user);
        out.put("jakarta.persistence.jdbc.password", password);

        var dynamic = dynamicEnhancementAgent.getCode();
        if (!dynamic.isEmpty()) out.put("openjpa.DynamicEnhancementAgent", dynamic);
        var uc = runtimeUnenhancedClasses.getCode();
        if (!uc.isEmpty()) out.put("openjpa.RuntimeUnenhancedClasses", uc);
        var dc = dataCache.getCode();
        if (!dc.isEmpty()) out.put("openjpa.DataCache", dc);
        var qc = queryCache.getCode();
        if (!qc.isEmpty()) out.put("openjpa.QueryCache", qc);

        out.putAll(extras);
        return Map.copyOf(out);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedEntityManager get() {
        return JpaEntityManagers.createExtended(e -> false, persistenceUnitName, properties());
    }

    /// Returns a copy of this connection factory with the persistence-unit name replaced by the given value.
    /// @param persistenceUnitName The new JPA persistence-unit name.
    /// @return A new connection factory with the updated persistence-unit name.
    /// @throws IllegalArgumentException If `persistenceUnitName` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withPersistenceUnitName(@NonNull String persistenceUnitName) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the JDBC URL replaced by the given value.
    /// @param url The new JDBC URL.
    /// @return A new connection factory with the updated URL.
    /// @throws IllegalArgumentException If `url` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withUrl(@NonNull String url) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the authentication credentials replaced by
    /// the given username and password.
    /// @param user The new database username.
    /// @param password The new database password.
    /// @return A new connection factory with the updated credentials.
    /// @throws IllegalArgumentException If either argument is `null`.
    @NonNull
    public OpenJpaConnectionFactory withAuth(@NonNull String user, @NonNull String password) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the `openjpa.DynamicEnhancementAgent` setting replaced by the given value.
    /// @param dynamicEnhancementAgent The new value of the property `openjpa.DynamicEnhancementAgent`.
    /// @return A new connection factory with the updated setting.
    /// @throws IllegalArgumentException If `dynamicEnhancementAgent` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withDynamicEnhancementAgent(@NonNull OptionalBoolean dynamicEnhancementAgent) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the `openjpa.DynamicEnhancementAgent` setting replaced by the given value.
    /// @param dynamicEnhancementAgent The new value of the property `openjpa.DynamicEnhancementAgent`.
    /// @return A new connection factory with the updated setting.
    @NonNull
    public OpenJpaConnectionFactory withDynamicEnhancementAgent(boolean dynamicEnhancementAgent) {
        return withDynamicEnhancementAgent(OptionalBoolean.from(dynamicEnhancementAgent));
    }

    /// Returns a copy of this connection factory with the `openjpa.RuntimeUnenhancedClasses` setting replaced by the given value.
    /// @param runtimeUnenhancedClasses The new value of the property `openjpa.RuntimeUnenhancedClasses`.
    /// @return A new connection factory with the updated setting.
    /// @throws IllegalArgumentException If `runtimeUnenhancedClasses` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withRuntimeUnenhancedClasses(@NonNull Support runtimeUnenhancedClasses) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the `openjpa.DataCache` setting replaced by the given value.
    /// @param dataCache The new value of the property `openjpa.DataCache`.
    /// @return A new connection factory with the updated setting.
    /// @throws IllegalArgumentException If `dataCache` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withDataCache(@NonNull OptionalBoolean dataCache) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the `openjpa.DataCache` setting replaced by the given value.
    /// @param dataCache The new value of the property `openjpa.DataCache`.
    /// @return A new connection factory with the updated setting.
    @NonNull
    public OpenJpaConnectionFactory withDataCache(boolean dataCache) {
        return withDataCache(OptionalBoolean.from(dataCache));
    }

    /// Returns a copy of this connection factory with the `openjpa.QueryCache` setting replaced by the given value.
    /// @param queryCache The new value of the property `openjpa.QueryCache`.
    /// @return A new connection factory with the updated setting.
    /// @throws IllegalArgumentException If `queryCache` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withQueryCache(@NonNull OptionalBoolean queryCache) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    /// Returns a copy of this connection factory with the `openjpa.QueryCache` setting replaced by the given value.
    /// @param queryCache The new value of the property `openjpa.QueryCache`.
    /// @return A new connection factory with the updated setting.
    @NonNull
    public OpenJpaConnectionFactory withQueryCache(boolean queryCache) {
        return withQueryCache(OptionalBoolean.from(queryCache));
    }

    /// Returns a copy of this connection factory with the extra provider properties replaced by the given map.
    /// @param extras The new extra provider properties merged last.
    /// @return A new connection factory with the updated extra properties.
    /// @throws IllegalArgumentException If `extras` is `null`.
    @NonNull
    public OpenJpaConnectionFactory withExtras(@NonNull Map<String, String> extras) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName,
                url,
                user,
                password,
                dynamicEnhancementAgent,
                runtimeUnenhancedClasses,
                dataCache,
                queryCache,
                Map.copyOf(extras)
        );
    }
}
