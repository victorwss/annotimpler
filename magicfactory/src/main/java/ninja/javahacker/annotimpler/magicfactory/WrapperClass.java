package ninja.javahacker.annotimpler.magicfactory;

import lombok.NonNull;

import module java.base;

/// Utility class for mapping between primitive types and their corresponding wrapper classes.
///
/// The JVM distinguishes between primitive types (e.g. `int.class`) and their wrapper
/// counterparts (e.g. `Integer.class`). This class provides bidirectional conversions for all
/// eight primitive types plus `void`.
///
/// This class is not instantiable.
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

    private static final Map<Class<?>, Class<?>> PRIMITIVES =
            WRAPPERS.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private WrapperClass() {
        throw new UnsupportedOperationException();
    }

    /// Returns the wrapper class for the given class, or the class itself if it is not a
    /// primitive type.
    ///
    /// For example, `wrap(int.class)` returns `Integer.class`, and `wrap(String.class)` returns
    /// `String.class` unchanged.
    ///
    /// @param <E> the class token type
    /// @param in  the class to map; must not be `null`
    /// @return the corresponding wrapper class, or `in` itself if not primitive; never `null`
    /// @throws IllegalArgumentException if `in` is `null`
    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> Class<E> wrap(@NonNull Class<E> in) {
        return (Class<E>) WRAPPERS.getOrDefault(in, in);
    }

    /// Returns the primitive type for the given wrapper class, or the class itself if it is
    /// not a wrapper type.
    ///
    /// For example, `unwrap(Integer.class)` returns `int.class`, and `unwrap(String.class)`
    /// returns `String.class` unchanged.
    ///
    /// @param <E> the class token type
    /// @param in  the class to map; must not be `null`
    /// @return the corresponding primitive type, or `in` itself if not a wrapper; never `null`
    /// @throws IllegalArgumentException if `in` is `null`
    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> Class<E> unwrap(@NonNull Class<E> in) {
        return (Class<E>) PRIMITIVES.getOrDefault(in, in);
    }
}
