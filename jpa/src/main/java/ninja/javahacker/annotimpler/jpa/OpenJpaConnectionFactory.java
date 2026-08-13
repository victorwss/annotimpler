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
/// @param dynamicEnhancementAgent The value of the´property `openjpa.DynamicEnhancementAgent`.
/// @param runtimeUnenhancedClasses The value of the´property `openjpa.RuntimeUnenhancedClasses`.
/// @param dataCache The value of the´property `openjpa.DataCache`.
/// @param queryCache The value of the´property `openjpa.QueryCache`.
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

    public OpenJpaConnectionFactory {}

    @NonNull
    public static OpenJpaConnectionFactory std() {
        return STD;
    }

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

    @NonNull
    @Override
    public ExtendedEntityManager get() {
        return JpaEntityManagers.createExtended(e -> false, persistenceUnitName, properties());
    }

    @NonNull
    public OpenJpaConnectionFactory withPersistenceUnitName(@NonNull String persistenceUnitName) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withUrl(@NonNull String url) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withAuth(@NonNull String user, @NonNull String password) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withDynamicEnhancementAgent(@NonNull OptionalBoolean dynamicEnhancementAgent) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withDynamicEnhancementAgent(boolean dynamicEnhancementAgent) {
        return withDynamicEnhancementAgent(OptionalBoolean.from(dynamicEnhancementAgent));
    }

    @NonNull
    public OpenJpaConnectionFactory withRuntimeUnenhancedClasses(@NonNull Support runtimeUnenhancedClasses) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withDataCache(@NonNull OptionalBoolean dataCache) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withDataCache(boolean dataCache) {
        return withDataCache(OptionalBoolean.from(dataCache));
    }

    @NonNull
    public OpenJpaConnectionFactory withQueryCache(@NonNull OptionalBoolean queryCache) {
        return new OpenJpaConnectionFactory(
                persistenceUnitName, url, user, password, dynamicEnhancementAgent, runtimeUnenhancedClasses, dataCache, queryCache, extras
        );
    }

    @NonNull
    public OpenJpaConnectionFactory withQueryCache(boolean queryCache) {
        return withQueryCache(OptionalBoolean.from(queryCache));
    }

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
