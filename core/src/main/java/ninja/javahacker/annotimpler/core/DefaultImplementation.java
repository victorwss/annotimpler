package ninja.javahacker.annotimpler.core;

import lombok.NonNull;
import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class DefaultImplementation {

    public static final Method TO_STRING = XSupplier.wrap(() -> Object.class.getMethod("toString")).get();

    public static final Method HASH_CODE = XSupplier.wrap(() -> Object.class.getMethod("hashCode")).get();

    public static final Method EQUALS = XSupplier.wrap(() -> Object.class.getMethod("equals", Object.class)).get();

    public static final Set<Method> OBJECT_DEFAULT = Set.of(HASH_CODE, TO_STRING, EQUALS);

    private DefaultImplementation() {
        throw new UnsupportedOperationException();
    }

    private static <E> int hashCode(@NonNull E instance, @NonNull Object... args) {
        if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException();
        if (args.length != 0) throw new IllegalArgumentException();
        return System.identityHashCode(instance);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private static <E> boolean equals(@NonNull E instance, @NonNull Object... args) {
        if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException();
        if (args.length != 1) throw new IllegalArgumentException();
        return args[0] == instance;
    }

    private static <E> String toString(@NonNull E instance, @NonNull Object... args) {
        if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException();
        if (args.length != 0) throw new IllegalArgumentException();
        return "impl[" + classIfaceName(instance) + "]-" + System.identityHashCode(instance);
    }

    private static record DefaultMethodImpl<E>(Method m) implements CallContext<E>{

        DefaultMethodImpl {
            if (m == null) throw new AssertionError();
        }

        @Override
        public Object execute(@NonNull E instance, @NonNull Object... args) throws Throwable {
            if (!Proxy.isProxyClass(instance.getClass())) throw new IllegalArgumentException();
            return InvocationHandler.invokeDefault(instance, m, args);
        }
    }

    public static String classIfaceName(@NonNull Object obj) {
        var k = obj.getClass();
        if (!Proxy.isProxyClass(k) && !k.isHidden() && !k.isSynthetic() && !k.isAnonymousClass()) return k.getName();
        var ifaces = List.of(k.getInterfaces());
        var zuper = k.getSuperclass();
        while (zuper != Object.class && !Proxy.isProxyClass(zuper) && !zuper.isHidden() && !zuper.isSynthetic()) {
            zuper = zuper.getSuperclass();
        }
        var all = ifaces.stream();
        if (zuper != Object.class || ifaces.isEmpty()) all = Stream.concat(Stream.of(zuper), all);
        return all.map(Class::getName).collect(Collectors.joining(","));
    }

    @NonNull
    public static <E> Optional<CallContext<E>> of(@NonNull Method m) {
        if (Methods.isHashCode(m)) return Optional.of(DefaultImplementation::hashCode);
        if (Methods.isEquals(m)) return Optional.of(DefaultImplementation::equals);
        if (Methods.isToString(m)) return Optional.of(DefaultImplementation::toString);
        var m2 = Methods.findDefaultImplementation(m);
        if (m2.isPresent()) return Optional.of(new DefaultMethodImpl<>(m2.get()));
        return Optional.empty();
    }
}
