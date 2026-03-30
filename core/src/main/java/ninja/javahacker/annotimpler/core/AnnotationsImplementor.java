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

    @NonNull
    private static String name(@NonNull Method m) {
        checkNotNull(m);
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @Nullable
    private static <E> CallContext<E> findImplementation(
            @NonNull Class<E> iface,
            @NonNull Method m,
            @NonNull PropertyBag props)
            throws BadImplementationException
    {
        checkNotNull(iface);
        checkNotNull(m);
        checkNotNull(props);

        assertTrue(Stream.of(iface.getMethods(), iface.getDeclaredMethods()).flatMap(Stream::of).toList().contains(m));

        var mc = m.getDeclaringClass();
        var impls = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(ImplementedBy.class)).toList();
        if (impls.size() > 1) {
            throw new BadImplementationException("Too many implementations by annotations on: " + name(m), mc);
        }
        if (impls.isEmpty()) {
            if (Methods.isPrivate(m) || Methods.isStatic(m) || Methods.isEquals(m) || Methods.isHashCode(m) || Methods.isToString(m)) {
                return null;
            }
            if (!m.isDefault()) {
                var msg = MethodWrapper.of(m).toStringUp() + " lacks annotation-defined implementation.";
                throw new BadImplementationException(msg, m.getDeclaringClass());
            }
            return (instance, args) -> {
                checkNotNull(instance);
                checkNotNull(args);
                return InvocationHandler.invokeDefault(instance, m, args);
            };
        }
        var implAnnon = impls.getFirst().annotationType();
        var implName = implAnnon.getSimpleName();
        if (Methods.isStatic(m)) {
            throw new BadImplementationException("Can't use @" + implName + " annotation on static methods.", mc);
        }
        if (Methods.isPrivate(m)) {
            throw new BadImplementationException("Can't use @" + implName+ " annotation on private methods.", mc);
        }
        if (Methods.isEquals(m)) {
            throw new BadImplementationException("Can't use @" + implName + " annotation on equals(Object) method.", mc);
        }
        if (Methods.isHashCode(m)) {
            throw new BadImplementationException("Can't use @" + implName + " annotation on hashCode() method.", mc);
        }
        if (Methods.isToString(m)) {
            throw new BadImplementationException("Can't use @" + implName + " annotation on toString() method.", mc);
        }
        var implClass = implAnnon.getAnnotation(ImplementedBy.class).value();
        try {
            var mf = MagicFactory.of(implClass);
            if (mf.arity() != 0) {
                throw new BadImplementationException("Don't know how to build " + implClass.getSimpleName() + " with no arguments.", mc);
            }
            var c = mf.create().<E>prepare(m, props);
            if (c == null) throw new BadImplementationException("Implementation was null on: " + name(m), mc);
            return c;
        } catch (MagicFactory.CreatorSelectionException | MagicFactory.CreationException e) {
            throw new BadImplementationException(e.getMessage(), e, m.getDeclaringClass());
        }
    }

    @NonNull
    public static <E> E implement(@NonNull Class<E> iface) throws BadImplementationException {
        return implement(iface, null);
    }

    @NonNull
    public static <E> E implement(@NonNull Class<E> iface, @Nullable PropertyBag props) throws BadImplementationException {
        if (!iface.isInterface()) throw new UnsupportedOperationException();

        var props2 = props == null ? PropertyBag.root() : props;

        var ifacePublicMeths = Stream.of(iface.getMethods());
        var ifaceDeclaredMeths = Stream.of(iface.getDeclaredMethods());
        var ifaceMeths = Stream.concat(ifacePublicMeths, ifaceDeclaredMeths).distinct().toList();

        Map<Method, CallContext<E>> meths = new HashMap<>(ifaceMeths.size() + 3);
        for (var m : ifaceMeths) {
            var impl = findImplementation(iface, m, props2);
            if (impl != null) meths.put(m, impl);
        }
        meths.put(Methods.EQUALS, DefaultImplementation.forEquals());
        meths.put(Methods.HASH_CODE, DefaultImplementation.forHashCode());
        meths.put(Methods.TO_STRING, DefaultImplementation.forToString(iface));

        InvocationHandler ih = (p, m, a) -> {
            checkNotNull(p);
            checkNotNull(m);
            var impl = meths.get(m);
            checkNotNull(impl);
            return impl.execute(iface.cast(p), a == null ? new Object[0] : a);
        };

        var obj = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { iface }, ih);
        return iface.cast(obj);
    }

    @Generated
    private static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}