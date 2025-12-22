package ninja.javahacker.magicfactory;

import module java.base;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

public final class Methods {

    private static final Set<Method> INTRINSICS;

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
    public static Type getReturnType(@NonNull Executable what) {
        if (what instanceof Method m) return m.getGenericReturnType();
        if (what instanceof Constructor<?> c) return c.getDeclaringClass();
        throw new IllegalArgumentException();
    }

    @NonNull
    public static Type getReturnType(@NonNull Field field) {
        return field.getGenericType();
    }

    @Nullable
    public static Object invoke(@NonNull Executable what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if (what instanceof Constructor<?> c) return c.newInstance(args);
        if (what instanceof Method m) {
            if (Modifier.isStatic(m.getModifiers()) || args.length == 0) return m.invoke(null, args);
            var inst = args[0];
            var rest = new Object[args.length - 1];
            System.arraycopy(args, 1, rest, 0, rest.length);
            return m.invoke(inst, rest);
        }
        throw new AssertionError();
    }
}