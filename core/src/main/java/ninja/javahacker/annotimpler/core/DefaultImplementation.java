package ninja.javahacker.annotimpler.core;

import java.lang.reflect.Proxy;
import lombok.NonNull;

/// Provides default [CallContext] implementations for [Object#equals], [Object#hashCode],
/// and [Object#toString] for proxy objects created by [AnnotationsImplementor].
///
/// - **`equals`** uses identity comparison (`==`).
/// - **`hashCode`** delegates to [System#identityHashCode].
/// - **`toString`** produces a string of the form `impl[fully.qualified.InterfaceName]-hashCode`.
///
/// All returned contexts require the `instance` argument to be a [java.lang.reflect.Proxy].
/// This class is not instantiable.
public final class DefaultImplementation {

    /// Error message for `equals`, `hashCode` and `toString` methods used in the wrong instance.
    private static final String NOT_PROXY = "Should be a proxy.";

    /// Error message for `equals`, `hashCode` and `toString` methods used with the wrong parameter count.
    private static final String BAD_ARITY = "Bad method arity.";

    /// This class is not instantiable.
    private DefaultImplementation() {
        throw new UnsupportedOperationException();
    }

    /// Represents a [CallContext] that implements [Object#hashCode] using [System#identityHashCode].
    ///
    /// @param <E> The interface type.
    /// @return args The reflective call arguments. Must be empty.
    /// @throws IllegalArgumentException If `instance` or `args` is `null`;
    ///         if `instance` is not a proxy; or if `args` is not an empty array.
    private static <E> int hashCode(@NonNull E instance, @NonNull Object... args) {
        if (args.length != 0) throw new IllegalArgumentException(BAD_ARITY);
        if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException(NOT_PROXY);
        return System.identityHashCode(instance);
    }

    /// Represents a [CallContext] that implements [Object#equals] using identity comparison (`==`).
    ///
    /// Two proxy instances are equal only if they are the same object reference.
    ///
    /// @param <E> The interface type.
    /// @return args The reflective call arguments. Must be an array of length 1.
    /// @throws IllegalArgumentException If `instance` or `args` is `null`;
    ///         if `instance` is not a proxy; or if `args` is not an array of length 1.
    private static <E> boolean equals(@NonNull E instance, @NonNull Object... args) {
        if (args.length != 1) throw new IllegalArgumentException(BAD_ARITY);
        if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException(NOT_PROXY);
        return args[0] == instance;
    }

    /// Returns a [CallContext] that implements [Object#toString] for proxy objects of the given interface.
    ///
    /// The string is of the form `impl[fully.qualified.InterfaceName]-identityHashCode`.
    ///
    /// @param <E> The interface type.
    /// @param iface The interface whose proxies this context will serve; must not be `null`.
    /// @return A [CallContext] implementing [Object#toString].
    /// @throws IllegalArgumentException If `iface` is `null`.
    @SuppressWarnings("Convert2Lambda") // Lombok won't insert code to handle @NonNull inside a lambda, but an anonymous class is ok.
    public static <E> CallContext<E> forToString(@NonNull Class<E> iface) {
        return new CallContext<>() {

            /// {@inheritDoc}
            @Override
            public Object execute(@NonNull E instance, @NonNull Object... args) {
                if (args.length != 0) throw new IllegalArgumentException(BAD_ARITY);
                if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException(NOT_PROXY);
                return "impl[" + iface.getName() + "]-" + System.identityHashCode(instance);
            }
        };
    }

    /// Returns a [CallContext] that implements [Object#equals] using identity comparison (`==`).
    ///
    /// Two proxy instances are equal only if they are the same object reference.
    ///
    /// @param <E> The interface type.
    /// @return A [CallContext] implementing [Object#equals].
    public static <E> CallContext<E> forEquals() {
        return DefaultImplementation::equals;
    }

    /// Returns a [CallContext] that implements [Object#hashCode] using [System#identityHashCode].
    ///
    /// @param <E> The interface type.
    /// @return A [CallContext] implementing [Object#hashCode].
    public static <E> CallContext<E> forHashCode() {
        return DefaultImplementation::hashCode;
    }
}
