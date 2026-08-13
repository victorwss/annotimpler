package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;

import module ninja.javahacker.annotimpler.sql;

/// Supplies newly-opened [ExtendedEntityManager] instances and begins [JpaTransaction]s from them.
///
/// As a functional interface, any lambda or method reference that produces an
/// [ExtendedEntityManager] can be used directly as an `EntityManagerSupplier`.
@FunctionalInterface
public interface EntityManagerSupplier extends Transactor.TransactionFactory<ExtendedEntityManager> {

    /// Opens a new [ExtendedEntityManager].
    ///
    /// @return A new and open entity manager; never `null`.
    @NonNull
    public ExtendedEntityManager get();

    /// Begins a new [JpaTransaction] wrapping a freshly-opened [ExtendedEntityManager] from [#get()].
    ///
    /// @param id The unique string identifier assigned to the new transaction.
    /// @return The newly-begun transaction; never `null`.
    /// @throws IllegalArgumentException If `id` is `null`.
    @NonNull
    @Override
    public default Transactor.Transaction<ExtendedEntityManager> begin(@NonNull String id) {
        return new JpaTransaction(get(), id);
    }
}
