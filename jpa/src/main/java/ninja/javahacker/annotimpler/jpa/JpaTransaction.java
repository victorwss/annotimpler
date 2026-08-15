package ninja.javahacker.annotimpler.jpa;

import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module ninja.javahacker.annotimpler.sql;

/// Holds the active [ExtendedEntityManager] and its unique identifier for one transaction.
///
/// @param manager The open [ExtendedEntityManager] for the current transaction.
/// @param id The unique string identifier assigned to this transaction.
@PackagePrivate
record JpaTransaction(
        @NonNull ExtendedEntityManager manager,
        @NonNull String uniqueId)
        implements Transactor.Transaction<ExtendedEntityManager>
{

    /// Creates a `JpaTransaction` with the given connection and identifier.
    ///
    /// @param manager The open [EntityManager] for the current transaction.
    /// @param id The unique string identifier assigned to this transaction.
    public JpaTransaction {
        checkNotNull(manager); // Check recognized by lombok.
        checkNotNull(uniqueId); // Check recognized by lombok.
        manager.getTransaction().begin();
    }

    /// {@inheritDoc}
    @Override
    public void commit() {
        manager.getTransaction().commit();
    }

    /// {@inheritDoc}
    @Override
    public void rollback() {
        manager.getTransaction().rollback();
    }

    /// {@inheritDoc}
    @Override
    public void close() {
        manager.close();
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public ExtendedEntityManager unwrap() {
        return manager;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
