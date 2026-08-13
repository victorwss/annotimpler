package ninja.javahacker.annotimpler.jpa;

import lombok.NonNull;

import module ninja.javahacker.annotimpler.sql;

@FunctionalInterface
public interface EntityManagerSupplier extends Transactor.TransactionFactory<ExtendedEntityManager> {

    @NonNull
    public ExtendedEntityManager get();

    @Override
    public default Transactor.Transaction<ExtendedEntityManager> begin(@NonNull String id) {
        return new JpaTransaction(get(), id);
    }
}
