package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

/// Represents the execution logic for a single interface method dispatch.
///
/// A [CallContext] encapsulates the behavior that should run when a particular method is
/// called on a proxy implementation created by [AnnotationsImplementor]. It receives the
/// proxy instance (typed as `E`) and the method arguments, and returns the method's result.
///
/// Instances are produced by [Implementation#prepare] during proxy construction and
/// are invoked by the proxy's invocation handler on each method call.
///
/// @param <E> the interface type whose method this context handles.
///
/// @see Implementation
/// @see AnnotationsImplementor
@FunctionalInterface
public interface CallContext<E> {
    /// Executes this context for the given proxy instance and method arguments.
    ///
    /// @param instance The proxy instance on which the method was called; must not be `null`.
    /// @param args The method arguments; must not be `null` (use an empty array for no-arg methods).
    /// @return The return value of the method, or `null` for `void` methods.
    /// @throws Throwable Any exception the method may declare or propagate.
    /// @throws IllegalArgumentException If `instance` or `args` is `null`.
    @Nullable
    public Object execute(@NonNull E instance, @NonNull Object... args) throws Throwable;
}
