package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// Utility class providing predicate methods and invocation helpers for
/// [Method], [Constructor], and [Field].
///
/// Predicate methods follow the naming convention `isXxx(Method)`, operating on modifier
/// bits or method identity. Invocation helpers provide a unified calling convention for both
/// static and instance reflective members.
///
/// Well-known [Object] methods are exposed as constants ([#TO_STRING], [#HASH_CODE],
/// [#EQUALS]) and grouped in [#OBJECT_DEFAULT] for convenient identity comparisons.
///
/// This class is not instantiable.
public final class Methods {

    /// Identifies a [Method] by name and signature.
    /// @param name A method name.
    /// @param params A method signature. Always an immutable list. Generics are erased.
    public static record MethodId(@NonNull String name, @NonNull List<Class<?>> params) {

        /// Instantiates a [MethodId] by its components.
        /// @param name The method name.
        /// @param params The method signature. An immutable copy is stored instead of the original.
        /// @throws IllegalArgumentException If `name` or `params` is `null`.
        public MethodId {
            params = List.copyOf(params);
        }

        /// Instantiates a [MethodId] from a method.
        /// @param m The given [Method] to get an id.
        /// @throws IllegalArgumentException If `m` is `null`.
        public MethodId(@NonNull Method m) {
            this(m.getName(), Stream.of(m.getParameterTypes()).toList());
        }
    }

    /// The [Object] class methods methods for `wait`, `notify`, `notifyAll`, `getClass`, `clone` and `finalize` (if not removed yet).
    @NonNull
    private static final Set<Method> INTRINSICS;

    /// The [Object#toString()] method id.
    @NonNull
    public static final MethodId TO_STRING = new MethodId("toString", List.of());

    /// The [Object#hashCode()] method id.
    @NonNull
    public static final MethodId HASH_CODE = new MethodId("hashCode", List.of());

    /// The [Object#equals(Object)] method id.
    @NonNull
    public static final MethodId EQUALS = new MethodId("equals", List.of(Object.class));

    /// The [Object#clone()] method id.
    @NonNull
    public static final MethodId CLONE = new MethodId("clone", List.of());

    /// The [Object#finalize()] method id.
    @NonNull
    public static final MethodId FINALIZE = new MethodId("finalize", List.of());

    /// An unmodifiable set containing [#HASH_CODE], [#TO_STRING], and [#EQUALS].
    @NonNull
    public static final Set<MethodId> OBJECT_DEFAULT = Set.of(HASH_CODE, TO_STRING, EQUALS);

    static {
        var names = Set.of("wait", "notify", "notifyAll", "getClass", "clone", "finalize");
        var sketch = new HashSet<Method>(8);
        for (var m : Object.class.getDeclaredMethods()) {
            if (names.contains(m.getName())) sketch.add(m);
        }
        INTRINSICS = Set.copyOf(sketch);
    }

    /// This class can't be instantiated.
    private Methods() {
        throw new UnsupportedOperationException();
    }

    /// Returns `true` if `m` has the `static` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if static.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isStatic(@NonNull Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    /// Returns `true` if `m` does **not** have the `static` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` If not static.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isVirtual(@NonNull Method m) {
        return !Modifier.isStatic(m.getModifiers());
    }

    /// Returns `true` if `m` has the `public` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if public.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isPublic(@NonNull Method m) {
        return Modifier.isPublic(m.getModifiers());
    }

    /// Returns `true` if `m` has the `private` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if private.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isPrivate(@NonNull Method m) {
        return Modifier.isPrivate(m.getModifiers());
    }

    /// Returns `true` if `m` has the `protected` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if protected.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isProtected(@NonNull Method m) {
        return Modifier.isProtected(m.getModifiers());
    }

    /// Returns `true` if `m` has package-private (default) access — i.e. it is neither
    /// `public`, `protected`, nor `private`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if package-private.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isPackageProtected(@NonNull Method m) {
        return !isPublic(m) && !isPrivate(m) && !isProtected(m);
    }

    /// Returns `true` if `m` has the `abstract` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if abstract.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isAbstract(@NonNull Method m) {
        return Modifier.isAbstract(m.getModifiers());
    }

    /// Returns `true` if `m` does **not** have the `abstract` modifier.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if not abstract.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isConcrete(@NonNull Method m) {
        return !Modifier.isAbstract(m.getModifiers());
    }

    /// Returns `true` if `m` cannot be overridden — i.e. it is `final`, `private`, `static`,
    /// or declared in a `final` class.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if the method cannot be overridden.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isFinal(@NonNull Method m) {
        return Modifier.isFinal(m.getModifiers())
                || Modifier.isPrivate(m.getModifiers())
                || Modifier.isStatic(m.getModifiers())
                || Modifier.isFinal(m.getDeclaringClass().getModifiers());
    }

    /// Returns `true` if `m` can be overridden — i.e. it is not final, private, static, or
    /// in a final class.
    ///
    /// Equivalent to `!isFinal(m)`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if overridable.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isOverridable(@NonNull Method m) {
        return !isFinal(m);
    }

    /// Returns `true` if `m` is a default interface method.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if the method is a default interface method.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isDefault(@NonNull Method m) {
        return m.isDefault();
    }

    /// Returns `true` if `m` is a synthetic (compiler-generated) method.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if synthetic.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isSynthetic(@NonNull Method m) {
        return m.isSynthetic();
    }

    /// Returns `true` if `m` accepts a variable number of arguments.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if varargs.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isVarArgs(@NonNull Method m) {
        return m.isVarArgs();
    }

    /// Returns `true` if `m` is one of the un-overrided intrinsic methods of [Object]
    /// (`wait`, `notify`, `notifyAll`, `getClass`, `clone`, `finalize`).
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if the method is an [Object] intrinsic.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isObjectIntrinsic(@NonNull Method m) {
        return INTRINSICS.contains(m);
    }

    /// Returns `true` if `m` is a no-arg method named `"finalize"`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if finalize.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isFinalize(@NonNull Method m) {
        return m.getParameterCount() == 0 && "finalize".equals(m.getName());
    }

    /// Returns `true` if `m` is a no-arg method named `"clone"`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if clone.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isClone(@NonNull Method m) {
        return m.getParameterCount() == 0 && "clone".equals(m.getName());
    }

    /// Returns `true` if `m` is a no-arg method named `"toString"`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if toString.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isToString(@NonNull Method m) {
        return m.getParameterCount() == 0 && "toString".equals(m.getName());
    }

    /// Returns `true` if `m` is a no-arg method named `"hashCode"`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if hashCode.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isHashCode(@NonNull Method m) {
        return m.getParameterCount() == 0 && "hashCode".equals(m.getName());
    }

    /// Returns `true` if `m` is a single-[Object]-parameter method named `"equals"`.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if equals.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isEquals(@NonNull Method m) {
        var t = m.getParameterTypes();
        return t.length == 1 && t[0] == Object.class && "equals".equals(m.getName());
    }

    /// Returns `true` if `m` is considered *simple* and can be handled generically.
    ///
    /// A method is simple if it is synthetic, static, non-public, is `toString`, `hashCode`,
    /// `equals`, `clone`, `finalize` or is one of the [Object] intrinsics. Simple methods
    /// typically do not require annotation-driven behaviour in the `core` module.
    ///
    /// @param m The method to test; must not be `null`.
    /// @return `true` if the method is simple.
    /// @throws IllegalArgumentException If `m` is `null`.
    public static boolean isSimple(@NonNull Method m) {
        var mods = m.getModifiers();
        return m.isSynthetic()
                || Modifier.isStatic(mods)
                || !Modifier.isPublic(mods)
                || isToString(m)
                || isHashCode(m)
                || isEquals(m)
                || isClone(m)
                || isFinalize(m)
                || isObjectIntrinsic(m);
    }

    /// Builds a parameter name-to-argument map for the given executable and arguments.
    ///
    /// Delegates to [MethodWrapper#paramMap(Object...)].
    ///
    /// @param what The executable; must not be `null`.
    /// @param args The arguments (and optional receiver); must not be `null`.
    /// @return A map from parameter names to argument values; never `null`.
    /// @throws IllegalArgumentException If `what` or `args` is `null`, or if the argument.
    ///         list is incompatible with the executable's signature.
    @NonNull
    public static Map<String, Object> paramMap(@NonNull Executable what, @NonNull Object... args) {
        return MethodWrapper.of(what).paramMap(args);
    }

    /// Returns the generic return type of the given [Method] or [Constructor].
    ///
    /// In the case of a constructor, the return type is the declaring class.
    ///
    /// @param what The method or constructor; must not be `null`.
    /// @return The generic return type; never `null`.
    /// @throws IllegalArgumentException If `what` is `null`.
    @NonNull
    @Generated
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    public static Type getReturnType(@NonNull Executable what) {
        if (what instanceof Method m) return getReturnType(m);
        if (what instanceof Constructor<?> c) return getReturnType(c);
        throw new AssertionError();
    }

    /// Returns the generic return type of a [Constructor] — its declaring class.
    ///
    /// @param <E> The constructed type.
    /// @param what The constructor; must not be `null`.
    /// @return The declaring class; never `null`.
    /// @throws IllegalArgumentException If `what` is `null`.
    @NonNull
    public static <E> Class<E> getReturnType(@NonNull Constructor<E> what) {
        return what.getDeclaringClass();
    }

    /// Returns the generic return type of the given [Method].
    ///
    /// @param what The method; must not be `null`.
    /// @return The generic return type; never `null`.
    /// @throws IllegalArgumentException If `what` is `null`.
    @NonNull
    public static Type getReturnType(@NonNull Method what) {
        return what.getGenericReturnType();
    }

    /// Returns the generic type of the given [Field].
    ///
    /// @param field The field; must not be `null`.
    /// @return The generic field type; never `null`.
    /// @throws IllegalArgumentException If `field` is `null`.
    @NonNull
    public static Type getReturnType(@NonNull Field field) {
        return field.getGenericType();
    }

    /// Invokes the given [Method] with the supplied arguments.
    ///
    /// For **static** methods and constructors, `args` are the method or constructor arguments.
    /// For **instance** methods, `args[0]` is the receiver and subsequent elements are the
    /// method arguments. If no `args` are supplied for an instance method, a
    /// [NullPointerException] is thrown.
    ///
    /// @param what The method or constructor to invoke; must not be `null`.
    /// @param args The arguments (and optional receiver); must not be `null`.
    /// @return The return value, or `null` for `void` methods or `null` results.
    /// @throws IllegalAccessException If the method or constructor is not accessible.
    /// @throws InvocationTargetException If the method or constructor throws an exception.
    /// @throws InstantiationException If the class is abstract and `what` is a constructor.
    /// @throws NullPointerException If the method is an instance method and the receiver is `null` or `args` is zero-length.
    /// @throws IllegalArgumentException If `what` or `args` is `null` or if the `args` do not match the `what` signature.
    /// @throws ExceptionInInitializerError If the invocation of `what` tries to initialize a class, but the initialization fails.
    /// @see Method#invoke(Object, Object...)
    /// @see Constructor#newInstance(Object...)
    @Nullable
    @Generated
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    public static Object invoke(@NonNull Executable what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if (what instanceof Method m) return invoke(m, args);
        if (what instanceof Constructor<?> c) return invoke(c, args);
        throw new AssertionError();
    }

    /// Invokes the given [Constructor] with the supplied arguments.
    ///
    /// @param <E> The constructed type.
    /// @param what The constructor to invoke; must not be `null`.
    /// @param args The constructor arguments; must not be `null`.
    /// @return The newly created instance; never `null` since constructors can't return `null`.
    /// @throws IllegalAccessException If the constructor is not accessible.
    /// @throws InvocationTargetException If the constructor throws an exception.
    /// @throws InstantiationException If the class is abstract.
    /// @throws IllegalArgumentException If `what` or `args` is `null` or if the `args` do not match the `what` signature.
    /// @throws ExceptionInInitializerError If the invocation of `what` tries to initialize a class, but the initialization fails.
    /// @see Constructor#newInstance(Object...)
    @NonNull
    public static <E> E invoke(@NonNull Constructor<E> what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return what.newInstance(args);
    }

    /// Invokes the given [Method] with the supplied arguments.
    ///
    /// For **static** methods, `args` are the method arguments.
    /// For **instance** methods, `args[0]` is the receiver and subsequent elements are the
    /// method arguments. If no `args` are supplied for an instance method, a
    /// [NullPointerException] is thrown.
    ///
    /// @param what The method to invoke; must not be `null`.
    /// @param args The arguments (and optional receiver); must not be `null`.
    /// @return The return value, or `null` for `void` methods or `null` results.
    /// @throws IllegalAccessException If the method is not accessible.
    /// @throws InvocationTargetException If the method throws an exception.
    /// @throws NullPointerException If the method is an instance method and the receiver is `null` or `args` is zero-length.
    /// @throws IllegalArgumentException If `what` or `args` is `null` or if the `args` do not match the `what` signature.
    /// @throws ExceptionInInitializerError If the invocation of `what` tries to initialize a class, but the initialization fails.
    /// @see Method#invoke(Object, Object...)
    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    @Nullable
    public static Object invoke(@NonNull Method what, @NonNull Object... args) throws IllegalAccessException, InvocationTargetException {
        if (Modifier.isStatic(what.getModifiers())) return what.invoke(null, args);
        if (args.length == 0) throw npe(what);
        var inst = args[0];
        var rest = new Object[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);
        return what.invoke(inst, rest);
    }

    @NonNull
    @Generated
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static NullPointerException npe(@NonNull Method what) {
        if (what == null) throw new AssertionError();
        try {
            what.invoke(null);
            throw new AssertionError();
        } catch (NullPointerException npe) {
            return npe;
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }
}