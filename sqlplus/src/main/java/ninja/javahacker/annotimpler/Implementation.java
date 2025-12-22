package ninja.javahacker.annotimpler;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Method;
import lombok.NonNull;
import ninja.javahacker.magicfactory.ConstructionException;

@FunctionalInterface
public interface Implementation {

    @NonNull
    public <E> ImplementationExecutor<E> prepare(@NonNull Class<E> iface, @NonNull Method m, @NonNull PropertyBag props)
            throws ConstructionException;

    @FunctionalInterface
    public static interface ImplementationExecutor<E> {
        @Nullable
        public Object execute(@NonNull E instance, @NonNull Object... a) throws Throwable;
    }
}
