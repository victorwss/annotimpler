package ninja.javahacker.annotimpler.jpa;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module jakarta.persistence;
import module java.base;

/// Internal helper utilities for creating managed [EntityManager] instances.
@PackagePrivate
final class JpaEntityManagers {

    /// Sole private constructor to prevent instantiation of this utility class.
    /// @throws AssertionError Always, since this class should never be instantiated.
    @Generated
    private JpaEntityManagers() {
        throw new AssertionError();
    }

    @NonNull
    private static EntityManager createManaged(@NonNull String persistenceUnitName, @NonNull Map<String, String> props) {
        checkNotNull(persistenceUnitName); // Check recognized by lombok.
        checkNotNull(props); // Check recognized by lombok.

        var emf = props.isEmpty()
                ? Persistence.createEntityManagerFactory(persistenceUnitName)
                : Persistence.createEntityManagerFactory(persistenceUnitName, props);
        var em = emf.createEntityManager();

        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName()) && method.getParameterCount() == 0) {
                try {
                    return method.invoke(em, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                } finally {
                    emf.close();
                }
            }
            try {
                return method.invoke(em, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };

        return (EntityManager) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {EntityManager.class},
                handler
        );
    }

    /// Creates an [ExtendedEntityManager] backed by a managed [EntityManager] for the given persistence unit and properties.
    /// @param reconnect A predicate that tells whether a transaction begin failure should trigger a reconnection attempt.
    /// @param persistenceUnitName The JPA persistence-unit name.
    /// @param props The properties used to create the [EntityManager].
    /// @return An [ExtendedEntityManager] wrapping a freshly created managed [EntityManager].
    /// @throws IllegalArgumentException If any argument is `null`.
    @NonNull
    public static ExtendedEntityManager createExtended(
            @NonNull Predicate<RuntimeException> reconnect,
            @NonNull String persistenceUnitName,
            @NonNull Map<String, String> props)
    {
        return new SpecialEntityManager(reconnect, persistenceUnitName, () -> createManaged(persistenceUnitName, props));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
