package ninja.javahacker.annotimpler.jpa;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module jakarta.persistence;
import module java.base;

/// Implementation of the [ExtendedEntityManager] interface that delegates to some other [EntityManager].
/// @author Victor Williams Stafusa da Silva
@PackagePrivate
final class SpecialEntityManager implements ExtendedEntityManager {

    /// The wrapped entity manager, replaced on reconnection.
    @Getter
    @Delegate(types = EntityManager.class, excludes = DoNotDelegateEntityManager.class)
    @NonNull
    private EntityManager wrapped;

    /// Predicate that decides whether a `RuntimeException` thrown on transaction begin should trigger a reconnection attempt.
    @NonNull
    private final Predicate<RuntimeException> reconnect;

    /// The JPA persistence-unit name used to (re)create the wrapped entity manager.
    @NonNull
    private final String persistenceUnitName;

    /// Supplier used to (re)create the wrapped [EntityManager].
    @NonNull
    private final Supplier<EntityManager> emf;

    /// The cached [SpecialEntityTransaction], if any was already created for the current wrapped entity manager.
    @NonNull
    private Optional<SpecialEntityTransaction> trans;

    /// Creates a `SpecialEntityManager` delegating to a managed [EntityManager] created from the given supplier.
    /// @param reconnect A predicate that tells whether a transaction begin failure should trigger a reconnection attempt.
    /// @param persistenceUnitName The JPA persistence-unit name.
    /// @param emf Supplier used to (re)create the wrapped [EntityManager].
    /// @throws IllegalArgumentException If any argument is `null`.
    public SpecialEntityManager(
            @NonNull Predicate<RuntimeException> reconnect,
            @NonNull String persistenceUnitName,
            @NonNull Supplier<EntityManager> emf)
    {
        this.persistenceUnitName = persistenceUnitName;
        this.reconnect = reconnect;
        this.trans = Optional.empty();
        this.emf = emf;
        this.wrapped = emf.get();
    }

    private void recreateEntityManager() {
        this.wrapped.close();
        this.wrapped = emf.get();
    }

    /// {@inheritDoc}
    @Override
    public void remove(@Nullable Object obj) {
        if (obj != null && !isNew(obj)) wrapped.remove(obj);
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public <T extends Object> ExtendedTypedQuery<T> createQuery(@NonNull CriteriaQuery<T> criteriaQuery) {
        return ExtendedTypedQuery.wrap(wrapped.createQuery(criteriaQuery));
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings(
            value = "SQL_INJECTION_JPA",
            justification = "False alarm, we're just delegating it untouched."
    )
    @NonNull
    public <T extends Object> ExtendedTypedQuery<T> createQuery(@NonNull String qlString, @NonNull Class<T> resultClass) {
        return ExtendedTypedQuery.wrap(wrapped.createQuery(qlString, resultClass));
    }

    /// {@inheritDoc}
    @Override
    @NonNull
    public <T extends Object> ExtendedTypedQuery<T> createNamedQuery(@NonNull String name, @NonNull Class<T> resultClass) {
        return ExtendedTypedQuery.wrap(wrapped.createNamedQuery(name, resultClass));
    }

    /// {@inheritDoc}
    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @NonNull
    public EntityTransaction getTransaction() {
        var inner = wrapped.getTransaction(); // Relays exceptions.
        if (inner == null) throw new IllegalStateException(); // Should never happen with a sane wrapped EntityManager.

        // Return the cached transaction.
        if (!trans.isEmpty()) {
            var w = trans.get();
            if (w.wrapped == inner) return w;
        }

        // Create a new SpecialEntityTransaction and caches it.
        var t = new SpecialEntityTransaction(this, inner);
        trans = Optional.of(t);
        return t;
    }

    /// Exists only to suppress lombok's delegation on a few methods.
    private static interface DoNotDelegateEntityManager {

        /// Don't care.
        /// @param obj Don't care.
        public void remove(@Nullable Object obj);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param cq Don't care.
        /// @return Don't care.
        @NonNull
        public <T extends Object> TypedQuery<T> createQuery(@NonNull CriteriaQuery<T> cq);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param string Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        @NonNull
        public <T extends Object> TypedQuery<T> createQuery(@NonNull String string, @NonNull Class<T> type);

        /// Don't care.
        /// @param <T> Don't care.
        /// @param string Don't care.
        /// @param type Don't care.
        /// @return Don't care.
        @NonNull
        public <T extends Object> TypedQuery<T> createNamedQuery(@NonNull String string, @NonNull Class<T> type);

        /// Don't care.
        /// @return Don't care.
        @NonNull
        public EntityTransaction getTransaction();
    }

    /// [EntityTransaction] implementation that tries to reconnect at the [#begin()] method.
    private static class SpecialEntityTransaction implements EntityTransaction {

        /// The wrapped transaction, delegated to for every operation except [#begin()].
        @Delegate(types = EntityTransaction.class, excludes = DoNotDelegateEntityTransaction.class)
        @NonNull
        private final EntityTransaction wrapped;

        /// The entity manager that owns this transaction, used to trigger reconnection attempts.
        @NonNull
        private final SpecialEntityManager parent;

        /// Creates a `SpecialEntityTransaction` wrapping the given transaction on behalf of the given entity manager.
        /// @param parent The entity manager that owns this transaction.
        /// @param wrapped The transaction to delegate to.
        /// @throws IllegalArgumentException If either argument is `null`.
        public SpecialEntityTransaction(@NonNull SpecialEntityManager parent, @NonNull EntityTransaction wrapped) {
            this.parent = parent;
            this.wrapped = wrapped;
        }

        /// {@inheritDoc}
        @Override
        @SuppressWarnings("PMD.AvoidCatchingGenericException") // Needed for testing an exception fixable by reconnecting.
        public void begin() {
            try {
                wrapped.begin();
            } catch (RuntimeException e) {
                if (!parent.reconnect.test(e)) throw e;
                parent.recreateEntityManager();
                wrapped.begin();
            }
        }
    }

    /// Exists only to suppress lombok's delegation on a few methods.
    @SuppressWarnings("PMD.ImplicitFunctionalInterface")
    private static interface DoNotDelegateEntityTransaction {
        /// Don't care.
        public void begin();
    }
}