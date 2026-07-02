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

    private static final String NOT_PROXY = "Should be a proxy.";

    private static final String BAD_ARITY = "Bad method arity.";

    private DefaultImplementation() {
        throw new UnsupportedOperationException();
    }

    private static <E> int hashCode(@NonNull E instance, @NonNull Object... args) {
        if (args.length != 0) throw new IllegalArgumentException(BAD_ARITY);
        if (!(Proxy.isProxyClass(instance.getClass()))) throw new IllegalArgumentException(NOT_PROXY);
        return System.identityHashCode(instance);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private static <E> boolean equals(@NonNull E instance, @NonNull Object... args) {
        if (args.length != 1) throw new IllegalArgumentException(BAD_ARITY);
        if (!(Proxy.isProxyClass(instance.getClass()))) throw new IllegalArgumentException(NOT_PROXY);
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

            @Override
            public Object execute(@NonNull E instance, @NonNull Object... args) {
                if (args.length != 0) throw new IllegalArgumentException(BAD_ARITY);
                if (!(Proxy.isProxyClass(instance.getClass()))) throw new IllegalArgumentException(NOT_PROXY);
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
