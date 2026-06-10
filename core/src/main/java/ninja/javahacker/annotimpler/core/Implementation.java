package ninja.javahacker.annotimpler.core;

import lombok.NonNull;

import module java.base;

/// Prepares a [CallContext] for a specific interface method.
///
/// An `Implementation` is the bridge between an annotation (whose type is marked with
/// [@ImplementedBy][ImplementedBy]) and the runtime behavior of the annotated method.
/// When [AnnotationsImplementor] encounters such a method, it instantiates the designated
/// `Implementation` class and calls [prepare] to obtain the [CallContext] for that method.
/// The returned context is then invoked by the proxy on every call to that method.
///
/// @see ImplementedBy
/// @see CallContext
/// @see AnnotationsImplementor
@FunctionalInterface
public interface Implementation {
    /// Prepares the [CallContext] for the given interface method.
    ///
    /// This method is called once per annotated method during proxy construction.
    /// The returned [CallContext] is cached and reused for all subsequent calls to that method.
    ///
    /// @param m the interface method for which a context is being prepared; must not be null
    /// @param props the property bag provided to [AnnotationsImplementor#implement]; must not be null
    /// @return the [CallContext] to use when `m` is invoked on the proxy; must not be null
    /// @throws BadImplementationException if the method cannot be prepared for this implementation
    /// @throws IllegalArgumentException if `m` or `props` is null
    @NonNull
    public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) throws BadImplementationException;
}
