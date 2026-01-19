package ninja.javahacker.annotimpler.core;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface Implementation {
    @NonNull
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws ConstructionException;
}
