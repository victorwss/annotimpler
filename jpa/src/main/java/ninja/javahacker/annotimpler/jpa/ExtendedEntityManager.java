package ninja.javahacker.annotimpler.jpa;

import jakarta.persistence.metamodel.Attribute;
import lombok.NonNull;

import module jakarta.persistence;
import module java.base;
import module java.sql;

/// Extends the [EntityManager] interface adding several useful methods into it.
/// @author Victor Williams Stafusa da Silva
public interface ExtendedEntityManager extends EntityManager, AutoCloseable, PersistenceUnitUtil {

    /// Tells if the given object already have a defined identity or not.
    /// @param entity Instance whose load state is a new entity.
    /// @return `true` if the given object is new, `false` if it isn't.
    /// @throws IllegalArgumentException If the object is found not
    ///     to be an entity or is `null`.
    public default boolean isNew(@NonNull Object entity) throws IllegalArgumentException {
        return getIdentifier(entity) == null;
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException if the given object is not an
    ///     instance of an entity class belonging to the persistence unit
    /// @throws PersistenceException if the entity is not associated.
    ///     with an open persistence context or cannot be loaded from the
    ///     database.
    @Override
    public default <T> Class<? extends T> getClass(@NonNull T entity) throws IllegalArgumentException, PersistenceException {
        return getEntityManagerFactory().getPersistenceUnitUtil().getClass(entity);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException if the object is found not
    ///     to be an entity.
    @Override
    public default Object getIdentifier(@NonNull Object entity) throws IllegalArgumentException {
        return getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException If the object is found not
    ///     to be an entity or is `null`.
    @Override
    public default Object getVersion(@NonNull Object entity) throws IllegalArgumentException {
        return getEntityManagerFactory().getPersistenceUnitUtil().getVersion(entity);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException If the argument is `null`.
    @Override
    public default boolean isLoaded(@NonNull Object entity) throws IllegalArgumentException {
        return getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entity);
    }

    /// {@inheritDoc}
    /// @param <E> {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @param attribute {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException If either argument is `null`.
    @Override
    public default <E> boolean isLoaded(@NonNull E entity, @NonNull Attribute<? super E, ?> attribute) throws IllegalArgumentException {
        return getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entity, attribute);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @param attributeName {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException If either argument is `null`.
    @Override
    public default boolean isLoaded(@NonNull Object entity, @NonNull String attributeName) throws IllegalArgumentException {
        return getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entity, attributeName);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @param entityClass {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException if the given object is not an
    ///     instance of an entity class belonging to the persistence unit
    ///     or if the given class is not an entity class belonging to the
    ///     persistence unit.
    /// @throws PersistenceException if the entity is not associated
    ///     with an open persistence context or cannot be loaded from the
    ///     database.
    @Override
    public default boolean isInstance(@NonNull Object entity, @NonNull Class<?> entityClass)
            throws IllegalArgumentException, PersistenceException
    {
        return getEntityManagerFactory().getPersistenceUnitUtil().isInstance(entity, entityClass);
    }

    /// {@inheritDoc}
    /// @param <E> {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @param attribute {@inheritDoc}
    /// @throws IllegalArgumentException if the given object is not an
    ///     instance of an entity class belonging to the persistence unit.
    /// @throws PersistenceException if the entity is not associated
    ///     with an open persistence context or cannot be loaded from the
    ///     database.
    @Override
    public default <E> void load(@NonNull E entity, @NonNull Attribute<? super E, ?> attribute)
            throws IllegalArgumentException, PersistenceException
    {
        getEntityManagerFactory().getPersistenceUnitUtil().load(entity, attribute);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @throws IllegalArgumentException if the given object is not an
    ///     instance of an entity class belonging to the persistence unit.
    /// @throws PersistenceException if the entity is not associated
    ///     with an open persistence context or cannot be loaded from the
    ///     database.
    @Override
    public default void load(@NonNull Object entity) throws IllegalArgumentException, PersistenceException {
        getEntityManagerFactory().getPersistenceUnitUtil().load(entity);
    }

    /// {@inheritDoc}
    /// @param entity {@inheritDoc}
    /// @param attributeName {@inheritDoc}
    /// @throws IllegalArgumentException if the given object is not an
    ///     instance of an entity class belonging to the persistence unit.
    /// @throws PersistenceException if the entity is not associated
    ///     with an open persistence context or cannot be loaded from the
    ///     database.
    @Override
    public default void load(@NonNull Object entity, @NonNull String attributeName) throws IllegalArgumentException, PersistenceException {
        getEntityManagerFactory().getPersistenceUnitUtil().load(entity, attributeName);
    }

    /// Save a given object in the database regardless the fact of it being a new entity or an existing one.
    /// This will only insert it in the database if it is a new entity.
    /// @param <T> The type of the entity to save.
    /// @param entity The entity to save.
    /// @return The saved instance.
    /// @throws IllegalArgumentException If the argument is `null`.
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public default <T> T save(@NonNull T entity) throws IllegalArgumentException {
        if (!isNew(entity)) {
            T other = merge(entity);
            if (entity != other) refresh(entity);
        } else if (!contains(entity)) {
            persist(entity);
        } else {
            // Do nothing.
        }
        return entity;
    }

    /*public static ExtendedEntityManager wrap(@NonNull EntityManager em) {
        return em instanceof SpecialEntityManager
                ? (ExtendedEntityManager) em
                : new SpecialEntityManager(em, ProviderAdapter.findFor(em));
    }*/

    /// Unwraps an [EntityManager] that has been decorated as an `ExtendedEntityManager`.
    /// @param em The [EntityManager] to unwrap.
    /// @return The undecorated [EntityManager] or `em` as is if not recognized as decorated.
    /// @throws IllegalArgumentException If `em` is `null`.
    @SuppressWarnings("checkstyle:javadocmethod") // Checkstyle complains about AssertionError.
    public static EntityManager unwrap(@NonNull EntityManager em) throws IllegalArgumentException {
        var r = em instanceof SpecialEntityManager special ? special.getWrapped() : em;
        if (r instanceof SpecialEntityManager) throw new AssertionError();
        return r;
    }

    /// Find by primary key. Search for an entity of the specified class and primary key.
    /// If the entity instance is contained in the persistence context, it is returned from there wrapped into an [Optional].
    /// @param <T> The type of the entity class.
    /// @param entityClass The entity class.
    /// @param primaryKey The primary key.
    /// @return An [Optional] containing the found entity instance or an empty one if the entity does not exist.
    public default <T> Optional<T> findOptional(Class<T> entityClass, Object primaryKey) {
        return Optional.ofNullable(find(entityClass, primaryKey));
    }

    /// Find by primary key and lock. Search for an entity of the specified class and primary key and lock it with respect
    /// to the specified lock type. If the entity instance is contained in the persistence context, it is returned from there,
    /// and the effect of this method is the same as if the lock method had been called on the entity.
    ///
    /// If the entity is found within the persistence context and the lock mode type is pessimistic and the entity has a
    /// version attribute, the persistence provider must perform optimistic version checks when obtaining the database lock.
    /// If these checks fail, the [OptimisticLockException] will be thrown.
    ///
    /// The lock mode type is pessimistic and the entity instance is found but cannot be locked:
    /// - The [PessimisticLockException] will be thrown if the database locking failure causes transaction-level rollback.
    /// - The [LockTimeoutException] will be thrown if the database locking failure causes only statement-level rollback.
    ///
    /// @param <T> The type of the entity class.
    /// @param entityClass The entity class.
    /// @param primaryKey The primary key.
    /// @param lockMode The lock mode.
    /// @return An [Optional] containing the found entity instance or an empty one if the entity does not exist.
    /// @throws IllegalArgumentException If the first argument does not denote an entity type or the second argument is not a valid type for
    ///     that entity's primary key or is `null`.
    /// @throws TransactionRequiredException If there is no transaction and a lock mode other than NONE is specified or if invoked on an
    ///     entity manager which has not been joined to the current transaction and a lock mode other than NONE is specified.
    /// @throws OptimisticLockException If the optimistic version check fails.
    /// @throws PessimisticLockException If pessimistic locking fails and the transaction is rolled back.
    /// @throws LockTimeoutException If pessimistic locking fails and only the statement is rolled back.
    /// @throws PersistenceException If an unsupported lock call is made.
    public default <T> Optional<T> findOptional(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
            throws IllegalArgumentException, TransactionRequiredException, OptimisticLockException,
            PessimisticLockException, LockTimeoutException, PersistenceException
    {
        return Optional.ofNullable(find(entityClass, primaryKey, lockMode));
    }

    /// Find by primary key, using the specified properties. Search for an entity of the specified class and primary key.
    /// If the entity instance is contained in the persistence context, it is returned from there wrapped into an [Optional].
    /// If a vendor-specific property or hint is not recognized, it is silently ignored.
    /// @param <T> The type of the entity class.
    /// @param entityClass The entity class.
    /// @param primaryKey The primary key.
    /// @param properties Standard and vendor-specific properties and hints.
    /// @return An [Optional] containing the found entity instance or an empty one if the entity does not exist.
    public default <T> Optional<T> findOptional(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return Optional.ofNullable(find(entityClass, primaryKey, properties));
    }

    /// Find by primary key and lock. Search for an entity of the specified class and primary key and lock it with respect
    /// to the specified lock type. If the entity instance is contained in the persistence context, it is returned from there,
    /// and the effect of this method is the same as if the lock method had been called on the entity.
    ///
    /// If the entity is found within the persistence context and the lock mode type is pessimistic and the entity has a
    /// version attribute, the persistence provider must perform optimistic version checks when obtaining the database lock.
    /// If these checks fail, the [OptimisticLockException] will be thrown.
    ///
    /// The lock mode type is pessimistic and the entity instance is found but cannot be locked:
    /// - The [PessimisticLockException] will be thrown if the database locking failure causes transaction-level rollback.
    /// - The [LockTimeoutException] will be thrown if the database locking failure causes only statement-level rollback.
    ///
    /// If a vendor-specific property or hint is not recognized, it is silently ignored.
    ///
    /// Portable applications should not rely on the standard timeout hint.
    /// Depending on the database in use and the locking mechanisms used by the provider, the hint may or may not be observed.
    /// @param <T> The type of the entity class.
    /// @param entityClass The entity class.
    /// @param primaryKey The primary key.
    /// @param lockMode The lock mode.
    /// @param properties Standard and vendor-specific properties and hints.
    /// @return An [Optional] containing the found entity instance or an empty one if the entity does not exist.
    /// @throws IllegalArgumentException If the first argument does not denote an entity type or the second argument is not a valid type for
    ///     that entity's primary key or is `null`.
    /// @throws TransactionRequiredException If there is no transaction and a lock mode other than NONE is specified or if invoked on an
    ///     entity manager which has not been joined to the current transaction and a lock mode other than NONE is specified.
    /// @throws OptimisticLockException If the optimistic version check fails.
    /// @throws PessimisticLockException If pessimistic locking fails and the transaction is rolled back.
    /// @throws LockTimeoutException If pessimistic locking fails and only the statement is rolled back.
    /// @throws PersistenceException If an unsupported lock call is made.
    public default <T> Optional<T> findOptional(
            Class<T> entityClass,
            Object primaryKey,
            LockModeType lockMode,
            Map<String, Object> properties)
            throws IllegalArgumentException, TransactionRequiredException, OptimisticLockException,
            PessimisticLockException, LockTimeoutException, PersistenceException
    {
        return Optional.ofNullable(find(entityClass, primaryKey, lockMode, properties));
    }

    /// {@inheritDoc}
    /// @param qlString {@inheritDoc}
    /// @param resultClass {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException If a query has not been
    ///     defined with the given name or if the query string is
    ///     found to be invalid or if the query result is found to
    ///     not be assignable to the specified type.
    @Override
    public <T> ExtendedTypedQuery<T> createNamedQuery(String qlString, Class<T> resultClass) throws IllegalArgumentException;

    /// Create a query selecting all the entities typed as `resultClass` ordered by the `orders` criterions.
    /// @param <T> The type of the entity to be queried.
    /// @param resultClass The entity type of the result.
    /// @param orders Ordering criteria for the results.
    /// @return `this`.
    public default <T> ExtendedTypedQuery<T> createQuery(@NonNull Class<T> resultClass, @NonNull By... orders) {
        return this.createQuery(resultClass, Collections.emptyMap(), orders);
    }

    /// {@inheritDoc}
    /// @param cq {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException if the criteria query is
    ///     found to be invalid.
    @Override
    public <T> ExtendedTypedQuery<T> createQuery(CriteriaQuery<T> cq) throws IllegalArgumentException;

    /// {@inheritDoc}
    /// @param qlString {@inheritDoc}
    /// @param resultClass {@inheritDoc}
    /// @return {@inheritDoc}
    /// @throws IllegalArgumentException if the query string is
    ///     found to be invalid or if the query result is
    ///     found to not be assignable to the specified type.
    @Override
    public <T> ExtendedTypedQuery<T> createQuery(String qlString, Class<T> resultClass) throws IllegalArgumentException;

    /// Create a query selecting all the entities typed as `resultClass`, where their fields match the ones
    /// given in the `where` map and ordered by the `orders` criteria.
    /// @param <T> The type of the entity to be queried.
    /// @param resultClass The entity type of the result.
    /// @param where A map relating fields to their expected values.
    /// @param orders Ordering criteria for the results.
    /// @return `this`.
    public default <T> ExtendedTypedQuery<T> createQuery(
            @NonNull Class<T> resultClass,
            @NonNull Map<String, Object> where,
            @NonNull By... orders)
    {
        var jpql = new StringBuilder("SELECT c FROM ").append(resultClass.getName()).append(" c");
        if (!where.isEmpty()) {
            jpql.append(" WHERE ");
            StringJoiner sj = new StringJoiner(" AND ");
            where.keySet().stream().map(k -> "c." + k + " = :" + k).forEach(sj::add);
            jpql.append(sj);
        }
        if (orders.length > 0) {
            jpql.append(" ORDER BY ");
            StringJoiner sj = new StringJoiner(", ");
            Stream.of(orders).map(k -> "c." + k.field() + (k.descending() ? " DESC" : "")).forEach(sj::add);
            jpql.append(sj);
        }
        var query = this.createQuery(jpql.toString(), resultClass);
        where.forEach(query::setParameter);
        return query;
    }

    /// Obtains the [Connection] used by this [EntityManager].
    /// @return The [Connection] used by this [EntityManager].
    public default Connection getConnection() {
        return this.unwrap(Connection.class);
    }

    /// Describe one of the fields within the ordering of an `order by` statement in JPQL queries.
    /// @param field The name of the field within the `order by` statement.
    /// @param descending Whether the field is sorted by descending order or not.
    public static record By(@NonNull String field, boolean descending) {

        /// Creates an instance descending-ordered by the given field name.
        /// @param field The name of the field within the `order by` statement.
        /// @return An instance descending-ordered by the given field name.
        public static By desc(@NonNull String field) {
            return new By(field, true);
        }

        /// Creates an instance ascending-ordered by the given field name.
        /// @param field The name of the field within the `order by` statement.
        /// @return An instance ascending-ordered by the given field name.
        public static By asc(@NonNull String field) {
            return new By(field, false);
        }
    }
}