package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface Implementation {

    @NonNull
    public <E> ImplementationExecutor<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws ConstructionException;

    @FunctionalInterface
    public static interface ImplementationExecutor<E> {
        @Nullable
        public Object execute(@NonNull E instance, @NonNull Object... a) throws Throwable;
    }
}
