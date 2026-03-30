package ninja.javahacker.annotimpler.magicfactory;

import lombok.NonNull;

import module java.base;

public final class WrapperClass {

    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    private WrapperClass() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> Class<E> wrap(@NonNull Class<E> in) {
        return (Class<E>) WRAPPERS.getOrDefault(in, in);
    }
}
