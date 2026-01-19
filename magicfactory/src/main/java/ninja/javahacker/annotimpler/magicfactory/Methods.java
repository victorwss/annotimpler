package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

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

    public static boolean isOverride(@NonNull Method m) {
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

    @Nullable
    public static Object invoke(@NonNull Method what, @NonNull Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if (Modifier.isStatic(what.getModifiers()) || args.length == 0) return what.invoke(null, args);
        var inst = args[0];
        var rest = new Object[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);
        return what.invoke(inst, rest);
    }

    @NonNull
    public static Optional<Method> findDefaultImplementation(@NonNull Method m) {
        var declaring = m.getDeclaringClass();
        if (!declaring.isInterface()) {
            return Optional.empty();
        }

        var name = m.getName();
        var params = m.getParameterTypes();

        // 1. Coleta todos os métodos default compatíveis
        List<Method> candidates = new ArrayList<>();

        for (var iface : getAllInterfaces(declaring)) {
            for (var im : iface.getMethods()) {
                if (im.isDefault() && im.getName().equals(name) && Arrays.equals(im.getParameterTypes(), params)) {
                    candidates.add(im);
                }
            }
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        // 2. Mantém apenas os métodos default mais específicos
        Set<Method> mostSpecific = new HashSet<>(candidates);

        for (var m2 : candidates) {
            if (!mostSpecific.contains(m2)) continue;
            for (var m1 : candidates) {
                if (!mostSpecific.contains(m1)) continue;
                if (m1 != m2 && m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass())) {
                    mostSpecific.remove(m1);
                    break;
                }
            }
        }

        // 3. Diamante real ou inexistência
        if (mostSpecific.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(mostSpecific.iterator().next());
    }

    @NonNull
    public static Set<Class<?>> getAllInterfaces(@NonNull Class<?> iface) {
        Set<Class<?>> result = new LinkedHashSet<>();
        Deque<Class<?>> stack = new ArrayDeque<>();

        stack.push(iface);

        while (!stack.isEmpty()) {
            var current = stack.pop();
            if (current.isInterface() && result.add(current)) {
                for (var parent : current.getInterfaces()) {
                    stack.push(parent);
                }
            }
        }

        return result;
    }
}