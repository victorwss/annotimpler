package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Proxy;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

/// Creates proxy implementations of interfaces whose method behaviors are defined by annotations.
///
/// When [implement] is called for an interface type, each of its methods is resolved to a
/// [CallContext] using the following rules:
///
/// - If the method carries an annotation whose type is itself annotated with [@ImplementedBy][ImplementedBy],
///   the designated [Implementation] class is instantiated (via `MagicFactory` with no arguments)
///   and its [Implementation#prepare] method is called to obtain the [CallContext].
/// - If the method has no such annotation but provides a `default` implementation, that default is invoked.
/// - Private, static, [Object#equals], [Object#hashCode], and [Object#toString] methods are
///   handled specially and are not subject to annotation-driven dispatch.
///
/// The resulting object is a [java.lang.reflect.Proxy] instance. Its [Object#equals] uses
/// identity comparison (`==`), [Object#hashCode] uses [System#identityHashCode], and
/// [Object#toString] returns a string of the form `impl[fully.qualified.InterfaceName]-hashCode`.
///
/// This class is not instantiable.
///
/// @see ImplementedBy
/// @see Implementation
/// @see CallContext
/// @see PropertyBag
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

    /// Creates a proxy implementation of the given interface with no initial properties.
    ///
    /// Equivalent to `implement(iface, null)`.
    ///
    /// @param <E> the interface type
    /// @param iface the interface to implement; must not be null and must be an interface type
    /// @return a proxy object implementing `iface`
    /// @throws BadImplementationException if any method of `iface` cannot be implemented
    /// @throws UnsupportedOperationException if `iface` is not an interface
    /// @throws IllegalArgumentException if `iface` is null
    @NonNull
    public static <E> E implement(@NonNull Class<E> iface) throws BadImplementationException {
        return implement(iface, null);
    }

    /// Creates a proxy implementation of the given interface using the given property bag.
    ///
    /// The [PropertyBag] is passed to [Implementation#prepare] for each annotated method,
    /// allowing implementations to be parameterized at construction time.
    ///
    /// @param <E> the interface type
    /// @param iface the interface to implement; must not be null and must be an interface type
    /// @param props the property bag to pass to each [Implementation#prepare] call;
    ///              if `null`, the root (empty) bag is used
    /// @return a proxy object implementing `iface`
    /// @throws BadImplementationException if any method of `iface` cannot be implemented
    /// @throws UnsupportedOperationException if `iface` is not an interface
    /// @throws IllegalArgumentException if `iface` is null
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