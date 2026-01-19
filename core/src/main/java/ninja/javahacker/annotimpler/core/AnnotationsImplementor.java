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

    private static String name(@NonNull Method m) {
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    @NonNull
    private static CallContext<Object> findImplementation(@NonNull Method m, @NonNull PropertyBag props) throws ConstructionException {
        if (m == null) throw new AssertionError();
        if (props == null) throw new AssertionError();

        var impls = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(ImplementedBy.class)).toList();
        if (impls.size() > 1) {
            throw new ConstructionException("Too many implementations by annotations on: " + name(m), m.getDeclaringClass());
        }
        if (impls.isEmpty()) {
            return DefaultImplementation.of(m).orElseThrow(() -> {
                var msg = MethodWrapper.of(m).toStringUp() + " lacks annotation-defined implementation.";
                return new ConstructionException(msg, m.getDeclaringClass());
            });
        }
        var implClass = impls.getFirst().annotationType().getAnnotation(ImplementedBy.class).value();
        var c = MagicFactory.of(implClass).create().prepare(m, props);
        if (c == null) throw new ConstructionException("Implementation was null on: " + name(m), m.getDeclaringClass());
        return c;
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

        var meths = Stream.concat(DefaultImplementation.OBJECT_DEFAULT.stream(), ifaceMeths)
                .collect(Collectors.toMap(m -> m, m -> XSupplier.wrap(() -> findImplementation(m, props2)).get()));

        InvocationHandler ih = (p, m, a) -> {
            var impl = meths.get(m);
            if (impl == null) throw new AssertionError();
            return impl.execute(iface.cast(p), a == null ? new Object[0] : a);
        };

        var obj = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { iface }, ih);
        return iface.cast(obj);
    }
}