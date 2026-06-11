package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;
import lombok.SneakyThrows;

import module java.base;

/// Utility class providing predicate methods and invocation helpers for
/// [java.lang.reflect.Method], [java.lang.reflect.Constructor], and
/// [java.lang.reflect.Field].
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

    @NonNull
    private static final Set<Method> INTRINSICS;

    /// The [Object#toString()] method.
    @NonNull
    public static final Method TO_STRING = named("toString");

    /// The [Object#hashCode()] method.
    @NonNull
    public static final Method HASH_CODE = named("hashCode");

    /// The [Object#equals(Object)] method.
    @NonNull
    public static final Method EQUALS = named("equals", Object.class);

    /// An unmodifiable set containing [#HASH_CODE], [#TO_STRING], and [#EQUALS].
    @NonNull
    public static final Set<Method> OBJECT_DEFAULT = Set.of(HASH_CODE, TO_STRING, EQUALS);

    static {
        var names = Set.of("wait", "notify", "notifyAll", "getClass", "clone", "finalize");
        var sketch = new HashSet<Method>(8);
        for (var m : Object.class.getDeclaredMethods()) {
            if (names.contains(m.getName())) sketch.add(m);
        }
        INTRINSICS = Set.copyOf(sketch);
    }

    private Methods() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Generated
    @SneakyThrows
    private static Method named(String name, Class<?>... params) {
        return Object.class.getMethod(name, params);
    }

    /// Returns `true` if `m` has the `static` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if static
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isStatic(@NonNull Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    /// Returns `true` if `m` does **not** have the `static` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if not static
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isVirtual(@NonNull Method m) {
        return !Modifier.isStatic(m.getModifiers());
    }

    /// Returns `true` if `m` has the `public` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if public
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isPublic(@NonNull Method m) {
        return Modifier.isPublic(m.getModifiers());
    }

    /// Returns `true` if `m` has the `private` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if private
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isPrivate(@NonNull Method m) {
        return Modifier.isPrivate(m.getModifiers());
    }

    /// Returns `true` if `m` has the `protected` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if protected
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isProtected(@NonNull Method m) {
        return Modifier.isProtected(m.getModifiers());
    }

    /// Returns `true` if `m` has package-private (default) access — i.e. it is neither
    /// `public`, `protected`, nor `private`.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if package-private
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isPackageProtected(@NonNull Method m) {
        return !isPublic(m) && !isPrivate(m) && !isProtected(m);
    }

    /// Returns `true` if `m` has the `abstract` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if abstract
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isAbstract(@NonNull Method m) {
        return Modifier.isAbstract(m.getModifiers());
    }

    /// Returns `true` if `m` does **not** have the `abstract` modifier.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if not abstract
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isConcrete(@NonNull Method m) {
        return !Modifier.isAbstract(m.getModifiers());
    }

    /// Returns `true` if `m` cannot be overridden — i.e. it is `final`, `private`, `static`,
    /// or declared in a `final` class.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if the method cannot be overridden
    /// @throws IllegalArgumentException if `m` is `null`
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
    /// @param m the method to test; must not be `null`
    /// @return `true` if overridable
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isOverridable(@NonNull Method m) {
        return !isFinal(m);
    }

    /// Returns `true` if `m` is a default interface method.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if the method is a default interface method
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isDefault(@NonNull Method m) {
        return m.isDefault();
    }

    /// Returns `true` if `m` is a synthetic (compiler-generated) method.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if synthetic
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isSynthetic(@NonNull Method m) {
        return m.isSynthetic();
    }

    /// Returns `true` if `m` accepts a variable number of arguments.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if varargs
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isVarArgs(@NonNull Method m) {
        return m.isVarArgs();
    }

    /// Returns `true` if `m` is one of the non-overridable intrinsic methods of [Object]
    /// (`wait`, `notify`, `notifyAll`, `getClass`, `clone`, `finalize`).
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if the method is an [Object] intrinsic
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isObjectIntrinsic(@NonNull Method m) {
        return INTRINSICS.contains(m);
    }

    /// Returns `true` if `m` is a no-arg method named `"finalize"`.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if finalize
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isFinalize(@NonNull Method m) {
        return m.getParameterCount() == 0 && "finalize".equals(m.getName());
    }

    /// Returns `true` if `m` is a no-arg method named `"clone"`.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if clone
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isClone(@NonNull Method m) {
        return m.getParameterCount() == 0 && "clone".equals(m.getName());
    }

    /// Returns `true` if `m` is a no-arg method named `"toString"`.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if toString
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isToString(@NonNull Method m) {
        return m.getParameterCount() == 0 && "toString".equals(m.getName());
    }

    /// Returns `true` if `m` is a no-arg method named `"hashCode"`.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if hashCode
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isHashCode(@NonNull Method m) {
        return m.getParameterCount() == 0 && "hashCode".equals(m.getName());
    }

    /// Returns `true` if `m` is a single-[Object]-parameter method named `"equals"`.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if equals
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isEquals(@NonNull Method m) {
        return m.getParameterCount() == 1 && "equals".equals(m.getName()) && m.getParameterTypes()[0] == Object.class;
    }

    /// Returns `true` if `m` is considered *simple* and can be handled generically.
    ///
    /// A method is simple if it is synthetic, static, non-public, is `toString`, `hashCode`,
    /// `equals`, or is one of the [Object] intrinsics. Simple methods typically do not require
    /// annotation-driven behaviour in the `core` module.
    ///
    /// @param m the method to test; must not be `null`
    /// @return `true` if the method is simple
    /// @throws IllegalArgumentException if `m` is `null`
    public static boolean isSimple(@NonNull Method m) {
        var mods = m.getModifiers();
        return m.isSynthetic()
                || Modifier.isStatic(mods)
                || !Modifier.isPublic(mods)
                || isToString(m)
                || isHashCode(m)
                || isEquals(m)
                || isObjectIntrinsic(m);
    }

    /// Builds a parameter name-to-argument map for the given executable and arguments.
    ///
    /// Delegates to [MethodWrapper#paramMap(Object...)].
    ///
    /// @param what the executable; must not be `null`
    /// @param args the arguments (and optional receiver); must not be `null`
    /// @return a map from parameter names to argument values; never `null`
    /// @throws IllegalArgumentException if `what` or `args` is `null`, or if the argument
    ///         list is incompatible with the executable's signature
    @NonNull
    public static Map<String, Object> paramMap(@NonNull Executable what, @NonNull Object... args) {
        return MethodWrapper.of(what).paramMap(args);
    }

    @NonNull
    @Generated
    public static Type getReturnType(@NonNull Executable what) {
        if (what instanceof Method m) return getReturnType(m);
        if (what instanceof Constructor<?> c) return getReturnType(c);
        throw new AssertionError();
    }

    /// Returns the generic return type of a [Constructor] — its declaring class.
    ///
    /// @param <E>  the constructed type
    /// @param what the constructor; must not be `null`
    /// @return the declaring class; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
    @NonNull
    public static <E> Class<E> getReturnType(@NonNull Constructor<E> what) {
        return what.getDeclaringClass();
    }

    /// Returns the generic return type of the given [Method].
    ///
    /// @param what the method; must not be `null`
    /// @return the generic return type; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
    @NonNull
    public static Type getReturnType(@NonNull Method what) {
        return what.getGenericReturnType();
    }

    /// Returns the generic type of the given [Field].
    ///
    /// @param field the field; must not be `null`
    /// @return the generic field type; never `null`
    /// @throws IllegalArgumentException if `field` is `null`
    @NonNull
    public static Type getReturnType(@NonNull Field field) {
        return field.getGenericType();
    }

    @Nullable
    @Generated
    public static Object invoke(@NonNull Executable what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if (what instanceof Method m) return invoke(m, args);
        if (what instanceof Constructor<?> c) return invoke(c, args);
        throw new AssertionError();
    }

    /// Invokes the given [Constructor] with the supplied arguments.
    ///
    /// @param <E>  the constructed type
    /// @param what the constructor to invoke; must not be `null`
    /// @param args the constructor arguments; must not be `null`
    /// @return the newly created instance; never `null`
    /// @throws IllegalAccessException    if the constructor is not accessible
    /// @throws InvocationTargetException if the constructor throws an exception
    /// @throws InstantiationException    if the class is abstract
    /// @throws IllegalArgumentException  if `what` or `args` is `null`
    @NonNull
    public static <E> E invoke(@NonNull Constructor<E> what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return what.newInstance(args);
    }

    @NonNull
    @Generated
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

    /// Invokes the given [Method] with the supplied arguments.
    ///
    /// For **static** methods, `args` are the method arguments.
    /// For **instance** methods, `args[0]` is the receiver and subsequent elements are the
    /// method arguments. If no args are supplied for an instance method, a
    /// [NullPointerException] is thrown.
    ///
    /// @param what the method to invoke; must not be `null`
    /// @param args the arguments (and optional receiver); must not be `null`
    /// @return the return value, or `null` for `void` methods or `null` results
    /// @throws IllegalAccessException    if the method is not accessible
    /// @throws InvocationTargetException if the method throws an exception
    /// @throws InstantiationException    not thrown; declared for API uniformity
    /// @throws IllegalArgumentException  if `what` or `args` is `null`
    @Nullable
    public static Object invoke(@NonNull Method what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if (Modifier.isStatic(what.getModifiers())) return what.invoke(null, args);
        if (args.length == 0) throw npe(what);
        var inst = args[0];
        var rest = new Object[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);
        return what.invoke(inst, rest);
    }
}