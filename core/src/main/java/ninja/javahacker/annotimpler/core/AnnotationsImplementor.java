package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Proxy;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class AnnotationsImplementor {

    private AnnotationsImplementor() {
        throw new UnsupportedOperationException();
    }

    private static void asserts(boolean b) {
        if (!b) throw new AssertionError();
    }

    private static String name(@NonNull Method m) {
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @NonNull
    private static <E> CallContext<E> findImplementation(
            @NonNull Class<E> iface,
            @NonNull Method m,
            @NonNull PropertyBag props)
            throws ConstructionException
    {
        if (iface == null) throw new AssertionError();
        if (m == null) throw new AssertionError();
        if (props == null) throw new AssertionError();
        asserts(!Modifier.isStatic(m.getModifiers()));
        asserts(Set.of(iface.getMethods()).contains(m));

        var impls = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(ImplementedBy.class)).toList();
        if (impls.size() > 1) {
            throw new ConstructionException("Too many implementations by annotations on: " + name(m), m.getDeclaringClass());
        }
        if (impls.isEmpty()) {
            if (!m.isDefault()) {
                var msg = MethodWrapper.of(m).toStringUp() + " lacks annotation-defined implementation.";
                throw new ConstructionException(msg, m.getDeclaringClass());
            }
            return (instance, args) -> {
                asserts(instance != null);
                asserts(args != null);
                return InvocationHandler.invokeDefault(instance, m, args);
            };
        }
        var implClass = impls.getFirst().annotationType().getAnnotation(ImplementedBy.class).value();
        var c = MagicFactory.of(implClass).create().<E>prepare(m, props);
        if (c == null) throw new ConstructionException("Implementation was null on: " + name(m), m.getDeclaringClass());
        return c;
    }

    @NonNull
    public static <E> E implement(@NonNull Class<E> iface) throws ConstructionException {
        return implement(iface, null);
    }

    @NonNull
    public static <E> E implement(@NonNull Class<E> iface, @Nullable PropertyBag props) throws ConstructionException {
        if (!iface.isInterface()) throw new UnsupportedOperationException();

        @NonNull
        var props2 = props == null ? PropertyBag.root() : props;

        var ifaceMeths = Stream.of(iface.getMethods()).filter(m -> !Modifier.isStatic(m.getModifiers())).toList();

        Map<Method, CallContext<E>> meths = new HashMap<>(ifaceMeths.size() + 3);
        for (var m : ifaceMeths) {
            var impl = findImplementation(iface, m, props2);
            meths.put(m, impl);
        }
        meths.put(Methods.EQUALS, DefaultImplementation.forEquals());
        meths.put(Methods.HASH_CODE, DefaultImplementation.forHashCode());
        meths.put(Methods.TO_STRING, DefaultImplementation.forToString(iface));

        InvocationHandler ih = (p, m, a) -> {
            asserts(p != null);
            asserts(m != null);
            var impl = meths.get(m);
            if (impl == null) throw new AssertionError();
            return impl.execute(iface.cast(p), a == null ? new Object[0] : a);
        };

        var obj = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { iface }, ih);
        return iface.cast(obj);
    }
}