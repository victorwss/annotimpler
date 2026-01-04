package ninja.javahacker.annotimpler.core;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Proxy;
import lombok.NonNull;

public final class AnnotationsImplementor {

    private static final List<Method> OBJECT_DEFAULT = XSupplier.wrap(() -> {
        var a = Object.class.getMethod("hashCode");
        var b = Object.class.getMethod("toString");
        var c = Object.class.getMethod("equals", Object.class);
        return List.of(a, b, c);
    }).get();

    private AnnotationsImplementor() {
        throw new UnsupportedOperationException();
    }

    private enum DefaultImplementation implements Implementation {
        INSTANCE;

        @NonNull
        @Override
        @SuppressWarnings("PMD.CompareObjectsWithEquals")
        public <E> ImplementationExecutor<E> prepare(Class<E> iface, Method m, PropertyBag props) throws ConstructionException {
            if (iface == null) throw new AssertionError();
            if (m == null) throw new AssertionError();
            if (props == null) throw new AssertionError();

            if (!Methods.isSimple(m) && !m.isDefault()) {
                var msg = MethodWrapper.of(m).toStringUp() + " lacks annotation-defined implementation.";
                throw new ConstructionException(msg, iface);
            }
            return (E instance, Object... a) -> {
                if (instance == null) throw new AssertionError();
                if (a == null) throw new AssertionError();
                if (Methods.isToString(m)) return "impl[" + iface.getName() + "]-" + System.identityHashCode(instance);
                if (Methods.isHashCode(m)) return System.identityHashCode(instance);
                if (Methods.isEquals(m)) return a[0] == instance;
                if (m.isDefault()) return InvocationHandler.invokeDefault(instance, m, a);
                throw new AssertionError();
            };
        }
    }

    @NonNull
    private static <E> Implementation.ImplementationExecutor<E> findImplementation(
            Class<E> iface,
            Method m,
            PropertyBag props)
            throws ConstructionException
    {
        if (iface == null) throw new AssertionError();
        if (m == null) throw new AssertionError();
        if (props == null) throw new AssertionError();

        var impls = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(ImplementedBy.class)).toList();
        if (impls.size() > 1) {
            var nome = NameDictionary.global().getSimplifiedGenericString(m, true);
            throw new ConstructionException("Annotations on: " + nome, iface);
        }
        Implementation impl;

        if (impls.isEmpty()) {
            impl = DefaultImplementation.INSTANCE;
        } else {
            var implClass = impls.getFirst().annotationType().getAnnotation(ImplementedBy.class).value();
            var magic = MagicFactory.of(implClass);
            impl = magic.create();
        }
        var c = impl.prepare(iface, m, props);
        if (c == null) {
            var nome = NameDictionary.global().getSimplifiedGenericString(m, true);
            throw new ConstructionException("Implementation was null on: " + nome, iface);
        }
        return c;
    }

    @FunctionalInterface
    private static interface XSupplier<E> {
        public E get() throws Throwable;

        @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.AvoidCatchingThrowable"})
        public static <E> Supplier<E> wrap(XSupplier<E> x) {
            return () -> {
                try {
                    return x.get();
                } catch (Throwable t) {
                    throw new ImplementationFailedException(t);
                }
            };
        }
    }

    public static class ImplementationFailedException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public ImplementationFailedException(@NonNull Throwable cause) {
            //List.of(cause); // Force lombok put the null-checks before the constructor call.
            super(cause);
        }
    }

    @NonNull
    public static <E> E implement(@NonNull Class<E> iface) {
        return implement(iface, null);
    }

    @NonNull
    public static <E> E implement(@NonNull Class<E> iface, @Nullable PropertyBag props) {
        if (!iface.isInterface()) throw new UnsupportedOperationException();

        @NonNull
        var props2 = props == null ? PropertyBag.root() : props;

        var ifaceMeths = Stream.of(iface.getMethods());

        var meths = Stream.concat(OBJECT_DEFAULT.stream(), ifaceMeths)
                .collect(Collectors.toMap(m -> m, m -> XSupplier.wrap(() -> findImplementation(iface, m, props2)).get()));

        InvocationHandler ih = (p, m, a) -> {
            var impl = meths.get(m);
            if (impl == null) throw new AssertionError();
            return impl.execute(iface.cast(p), a == null ? new Object[0] : a);
        };

        var obj = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { iface }, ih);
        return iface.cast(obj);
    }
}