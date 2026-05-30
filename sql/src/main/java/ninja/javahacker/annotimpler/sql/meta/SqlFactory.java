package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;

@FunctionalInterface
public interface SqlFactory {

    @NonNull
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException;

}
