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
    /// A `EclipselinkConnectionFactory` with all fields left unspecified.
    private static final EclipselinkConnectionFactory STD = new EclipselinkConnectionFactory("", "", "", "", Map.of());

    /// Creates a `EclipselinkConnectionFactory` with the given field values.
    /// @param persistenceUnitName The JPA persistence-unit name.
    /// @param url JDBC URL.
    /// @param user Database user.
    /// @param password Database password.
    /// @param extras Extra provider properties merged last.
    /// @throws IllegalArgumentException If any argument is `null`.
    public EclipselinkConnectionFactory {}

    /// Returns a `EclipselinkConnectionFactory` with all fields left unspecified.
    /// @return A `EclipselinkConnectionFactory` with all fields left unspecified.
    @NonNull
    public static EclipselinkConnectionFactory std() {
        return STD;
    }

    // Builds the map of EclipseLink/JPA provider properties derived from this configuration.
    @NonNull
    private Map<String, String> properties() {
        var out = new HashMap<String, String>();
        out.put("jakarta.persistence.jdbc.url", url);
        out.put("jakarta.persistence.jdbc.user", user);
        out.put("jakarta.persistence.jdbc.password", password);
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
    public EclipselinkConnectionFactory withPersistenceUnitName(@NonNull String persistenceUnitName) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, extras);
    }

    /// Returns a copy of this connection factory with the JDBC URL replaced by the given value.
    /// @param url The new JDBC URL.
    /// @return A new connection factory with the updated URL.
    /// @throws IllegalArgumentException If `url` is `null`.
    @NonNull
    public EclipselinkConnectionFactory withUrl(@NonNull String url) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, extras);
    }

    /// Returns a copy of this connection factory with the authentication credentials replaced by
    /// the given username and password.
    /// @param user The new database username.
    /// @param password The new database password.
    /// @return A new connection factory with the updated credentials.
    /// @throws IllegalArgumentException If either argument is `null`.
    @NonNull
    public EclipselinkConnectionFactory withAuth(@NonNull String user, @NonNull String password) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, extras);
    }

    /// Returns a copy of this connection factory with the extra provider properties replaced by the given map.
    /// @param extras The new extra provider properties merged last.
    /// @return A new connection factory with the updated extra properties.
    /// @throws IllegalArgumentException If `extras` is `null`.
    @NonNull
    public EclipselinkConnectionFactory withExtras(@NonNull Map<String, String> extras) {
        return new EclipselinkConnectionFactory(persistenceUnitName, url, user, password, Map.copyOf(extras));
    }
}
