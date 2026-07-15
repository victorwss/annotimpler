package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.annotation.Annotation;
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
///   the designated [Implementation] class is instantiated via `MagicFactory`. If its creator takes a single
///   [PropertyBag] argument, the current property bag is passed to it; if it takes no arguments, it is
///   invoked with no arguments. Its [Implementation#prepare] method is then called to obtain the
///   [CallContext].
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

    /// This class is not instantiable.
    private AnnotationsImplementor() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    private static String name(@NonNull Method m) {
        checkNotNull(m); // Check recognized by lombok.
        return NameDictionary.global().getSimplifiedGenericString(m, true);
    }

    private static void validate(@NonNull Method m, @NonNull Class<? extends Annotation> implAnnon) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.
        checkNotNull(implAnnon); // Check recognized by lombok.
        var mc = m.getDeclaringClass();
        var implName = implAnnon.getSimpleName();
        var prefix = "Can't use @" + implName + " annotation on ";
        if (Methods.isStatic(m)) {
            throw new BadImplementationException(prefix + "static methods.", mc);
        }
        if (Methods.isPrivate(m)) {
            throw new BadImplementationException(prefix + "private methods.", mc);
        }
        if (Methods.isEquals(m)) {
            throw new BadImplementationException(prefix + "equals(Object) method.", mc);
        }
        if (Methods.isHashCode(m)) {
            throw new BadImplementationException(prefix + "hashCode() method.", mc);
        }
        if (Methods.isToString(m)) {
            throw new BadImplementationException(prefix + "toString() method.", mc);
        }
        if (Methods.isClone(m)) {
            throw new BadImplementationException(prefix + "clone() method.", mc);
        }
        if (Methods.isFinalize(m)) {
            throw new BadImplementationException(prefix + "finalize() method.", mc);
        }
    }

    @Nullable
    private static <E> CallContext<E> findSimpleImplementation(@NonNull Method m) throws BadImplementationException {
        checkNotNull(m); // Check recognized by lombok.

        if (Methods.isStatic(m)
                || Methods.isPrivate(m)
                || Methods.isEquals(m)
                || Methods.isHashCode(m)
                || Methods.isToString(m)
                || Methods.isClone(m)
                || Methods.isFinalize(m))
        {
            return null;
        }

        if (!m.isDefault()) {
            var msg = MethodWrapper.of(m).toStringUp() + " lacks annotation-defined implementation.";
            throw new BadImplementationException(msg, m.getDeclaringClass());
        }
        return (@NonNull E instance, @NonNull Object... args) -> {
            checkNotNull(instance); // Check recognized by lombok.
            checkNotNull(args); // Check recognized by lombok.
            return InvocationHandler.invokeDefault(instance, m, args);
        };
    }

    @Nullable
    @SuppressFBWarnings(
            // Any sane implementation of Implementation.prepare should be @NonNull and not needed to be checked against it.
            // However, we don't trust that, since malicious or ill-defined implementations might exist. So we check it anyway.
            "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"
    )
    private static <E> CallContext<E> findImplementation(
            @NonNull Class<E> iface,
            @NonNull Method m,
            @NonNull PropertyBag props)
            throws BadImplementationException
    {
        checkNotNull(iface); // Check recognized by lombok.
        checkNotNull(m); // Check recognized by lombok.
        checkNotNull(props); // Check recognized by lombok.

        assertTrue(Stream.of(iface.getMethods(), iface.getDeclaredMethods()).flatMap(Stream::of).toList().contains(m));

        var mc = m.getDeclaringClass();
        var impls = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(ImplementedBy.class)).toList();
        if (impls.size() > 1) {
            throw new BadImplementationException("Too many implementations by annotations on: " + name(m), mc);
        }
        if (impls.isEmpty()) return findSimpleImplementation(m);
        var implAnnon = impls.getFirst().annotationType();
        validate(m, implAnnon);
        var implClass = implAnnon.getAnnotation(ImplementedBy.class).value();
        try {
            var mf = MagicFactory.of(implClass);
            if (mf.arity() != 0) {
                throw new BadImplementationException("Don't know how to build " + implClass.getSimpleName() + " with arguments.", mc);
            }

            var instance = mf.create(new Object[0]);
            var context = instance.prepare(iface, m, props);

            // Should never happen if implClass is properly implemented, but we shouldn't trust that.
            if (context == null) throw new BadImplementationException("Implementation was null on: " + name(m), mc);

            return context;
        } catch (MagicFactory.CreatorSelectionException | MagicFactory.CreationException e) {
            throw new BadImplementationException(e.getMessage(), e, m.getDeclaringClass());
        }
    }

    /// Creates a proxy implementation of the given interface with no initial properties.
    ///
    /// Equivalent to `implement(iface, null)`.
    ///
    /// @param <E> The interface type.
    /// @param iface The interface to implement; must not be `null` and must be an interface type.
    /// @return A proxy object implementing `iface`.
    /// @throws BadImplementationException If any method of `iface` cannot be implemented.
    /// @throws UnsupportedOperationException If `iface` is not an interface.
    /// @throws IllegalArgumentException If `iface` is `null`.
    @NonNull
    public static <E> E implement(@NonNull Class<E> iface) throws BadImplementationException {
        return implement(iface, null);
    }

    /// Creates a proxy implementation of the given interface using the given property bag.
    ///
    /// The [PropertyBag] is passed to [Implementation#prepare] for each annotated method,
    /// allowing implementations to be parameterized at construction time.
    ///
    /// @param <E> The interface type.
    /// @param iface The interface to implement; must not be `null` and must be an interface type.
    /// @param props The property bag to pass to each [Implementation#prepare] call;
    ///              if `null`, the root (empty) bag is used.
    /// @return A proxy object implementing `iface`.
    /// @throws BadImplementationException If any method of `iface` cannot be implemented.
    /// @throws UnsupportedOperationException If `iface` is not an interface.
    /// @throws IllegalArgumentException If `iface` is `null`.
    @NonNull
    public static <E> E implement(@NonNull Class<E> iface, @Nullable PropertyBag props) throws BadImplementationException {
        if (!iface.isInterface()) throw new UnsupportedOperationException();

        var props2 = props == null ? PropertyBag.root() : props;

        var ifacePublicMeths = Stream.of(iface.getMethods());
        var ifaceDeclaredMeths = Stream.of(iface.getDeclaredMethods());
        var ifaceMeths = Stream.concat(ifacePublicMeths, ifaceDeclaredMeths).distinct().toList();

        Map<Methods.MethodId, CallContext<E>> meths = new HashMap<>(ifaceMeths.size() + 3);
        for (var m : ifaceMeths) {
            var impl = findImplementation(iface, m, props2);
            if (impl != null) meths.put(new Methods.MethodId(m), impl);
        }
        meths.put(Methods.EQUALS, DefaultImplementation.forEquals());
        meths.put(Methods.HASH_CODE, DefaultImplementation.forHashCode());
        meths.put(Methods.TO_STRING, DefaultImplementation.forToString(iface));
        meths.put(Methods.FINALIZE, DefaultImplementation.forFinalize());
        meths.put(Methods.CLONE, DefaultImplementation.forClone());

        InvocationHandler ih = (@NonNull Object p, @NonNull Method m, @Nullable Object... a) -> {
            checkNotNull(p); // Check recognized by lombok.
            checkNotNull(m); // Check recognized by lombok.
            var impl = meths.get(new Methods.MethodId(m));
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