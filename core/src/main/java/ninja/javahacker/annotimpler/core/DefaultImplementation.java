package ninja.javahacker.annotimpler.core;

import lombok.NonNull;
import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

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

    public static <E> CallContext<E> forToString(@NonNull Class<E> iface) {
        return new CallContext<>() {

            @Override
            public Object execute(@NonNull E instance, @NonNull Object... args) throws Throwable {
                if (args.length != 0) throw new IllegalArgumentException(BAD_ARITY);
                if (!(Proxy.isProxyClass(instance.getClass()))) throw new IllegalArgumentException(NOT_PROXY);
                return "impl[" + iface.getName() + "]-" + System.identityHashCode(instance);
            }
        };
    }

    public static <E> CallContext<E> forEquals() {
        return DefaultImplementation::equals;
    }

    public static <E> CallContext<E> forHashCode() {
        return DefaultImplementation::hashCode;
    }
}
