package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

@FunctionalInterface
public interface CallContext<E> {
    @Nullable
    public Object execute(@NonNull E instance, @NonNull Object... args) throws Throwable;
}
