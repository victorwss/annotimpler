package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;
import lombok.SneakyThrows;

import module java.base;

public final class Methods {

    @NonNull
    private static final Set<Method> INTRINSICS;

    @NonNull
    public static final Method TO_STRING = named("toString");

    @NonNull
    public static final Method HASH_CODE = named("hashCode");

    @NonNull
    public static final Method EQUALS = named("equals", Object.class);

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

    public static boolean isStatic(@NonNull Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    public static boolean isVirtual(@NonNull Method m) {
        return !Modifier.isStatic(m.getModifiers());
    }

    public static boolean isPublic(@NonNull Method m) {
        return Modifier.isPublic(m.getModifiers());
    }

    public static boolean isPrivate(@NonNull Method m) {
        return Modifier.isPrivate(m.getModifiers());
    }

    public static boolean isProtected(@NonNull Method m) {
        return Modifier.isProtected(m.getModifiers());
    }

    public static boolean isPackageProtected(@NonNull Method m) {
        return !isPublic(m) && !isPrivate(m) && !isProtected(m);
    }

    public static boolean isAbstract(@NonNull Method m) {
        return Modifier.isAbstract(m.getModifiers());
    }

    public static boolean isConcrete(@NonNull Method m) {
        return !Modifier.isAbstract(m.getModifiers());
    }

    public static boolean isFinal(@NonNull Method m) {
        return Modifier.isFinal(m.getModifiers())
                || Modifier.isPrivate(m.getModifiers())
                || Modifier.isStatic(m.getModifiers())
                || Modifier.isFinal(m.getDeclaringClass().getModifiers());
    }

    public static boolean isOverridable(@NonNull Method m) {
        return !isFinal(m);
    }

    public static boolean isDefault(@NonNull Method m) {
        return m.isDefault();
    }

    public static boolean isSynthetic(@NonNull Method m) {
        return m.isSynthetic();
    }

    public static boolean isVarArgs(@NonNull Method m) {
        return m.isVarArgs();
    }

    public static boolean isObjectIntrinsic(@NonNull Method m) {
        return INTRINSICS.contains(m);
    }

    public static boolean isFinalize(@NonNull Method m) {
        return m.getParameterCount() == 0 && "finalize".equals(m.getName());
    }

    public static boolean isClone(@NonNull Method m) {
        return m.getParameterCount() == 0 && "clone".equals(m.getName());
    }

    public static boolean isToString(@NonNull Method m) {
        return m.getParameterCount() == 0 && "toString".equals(m.getName());
    }

    public static boolean isHashCode(@NonNull Method m) {
        return m.getParameterCount() == 0 && "hashCode".equals(m.getName());
    }

    public static boolean isEquals(@NonNull Method m) {
        return m.getParameterCount() == 1 && "equals".equals(m.getName()) && m.getParameterTypes()[0] == Object.class;
    }

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

    @NonNull
    public static <E> Class<E> getReturnType(@NonNull Constructor<E> what) {
        return what.getDeclaringClass();
    }

    @NonNull
    public static Type getReturnType(@NonNull Method what) {
        return what.getGenericReturnType();
    }

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