package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;

import module jakarta.persistence;
import module java.base;

/// Immutable [EntityManagerSupplier] configured for EclipseLink.
///
/// @param persistenceUnitName The JPA persistence-unit name.
/// @param url JDBC URL.
/// @param user Database user.
/// @param password Database password.
/// @param extras Extra provider properties merged last.
public record EclipselinkConnectionFactory(
        @NonNull String persistenceUnitName,
        @NonNull String url,
        @NonNull String user,
        @NonNull String password,
        @NonNull Map<String, String> extras
) implements EntityManagerSupplier
{
    private static final EclipselinkConnectionFactory STD = new EclipselinkConnectionFactory("", "", "", "", Map.of());

    public EclipselinkConnectionFactory {}

    @NonNull
    public static EclipselinkConnectionFactory std() {
        return STD;
    }

    @NonNull
    private Map<String, String> properties() {
        var out = new HashMap<String, String>();
        out.put("jakarta.persistence.jdbc.url", url);
        out.put("jakarta.persistence.jdbc.user", user);
        out.put("jakarta.persistence.jdbc.password", password);
        out.putAll(extras);
        return Map.copyOf(out);
    }

    @NonNull
    @Override
    public ExtendedEntityManager get() {
        return JpaEntityManagers.createExtended(e -> false, persistenceUnitName, properties());
    }

    @NonNull
    public EclipselinkConnectionFactory withPersistenceUnitName(@NonNull String persistenceUnitName) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, extras);
    }

    @NonNull
    public EclipselinkConnectionFactory withUrl(@NonNull String url) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, extras);
    }

    @NonNull
    public EclipselinkConnectionFactory withAuth(@NonNull String user, @NonNull String password) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, extras);
    }

    @NonNull
    public EclipselinkConnectionFactory withExtras(@NonNull Map<String, String> extras) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, Map.copyOf(extras));
    }
}
