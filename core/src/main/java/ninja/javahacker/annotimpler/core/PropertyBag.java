package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// An immutable, type-safe bag of properties identified by [KeyProperty] keys.
///
/// A `PropertyBag` associates [KeyProperty] instances with values of the appropriate type.
/// All operations that modify the bag ([add], [remove]) return a new `PropertyBag` rather
/// than modifying the receiver.
///
/// The [root] factory method returns the unique empty bag. Two bags are equal if and only if
/// they contain the same key-value pairs.
///
/// @see KeyProperty
public final class PropertyBag {

    @NonNull
    private static final PropertyBag ROOT_INSTANCE = new PropertyBag();

    @NonNull
    private final Map<KeyProperty<?>, Object> properties;

    private PropertyBag() {
        this.properties = Map.of();
    }

    private PropertyBag(@NonNull Map<KeyProperty<?>, Object> properties) {
        checkNotNull(properties);
        this.properties = Map.copyOf(properties);
    }

    /// Returns the root (empty) [PropertyBag].
    ///
    /// @return the shared empty bag; never null
    @NonNull
    public static PropertyBag root() {
        return ROOT_INSTANCE;
    }

    /// Returns a new [PropertyBag] containing all properties of this bag plus the given key-value pair.
    ///
    /// If a value for the same key already exists, it is replaced in the returned bag.
    ///
    /// @param <T> the value type
    /// @param key the property key; must not be null
    /// @param value the property value; must not be null and must be an instance of `key.valueType()`
    /// @return a new bag with the added or replaced property
    /// @throws IllegalPropertyValueException if `value` is not an instance of `key.valueType()`
    /// @throws IllegalArgumentException if `key` or `value` is null
    @NonNull
    public <T> PropertyBag add(@NonNull KeyProperty<T> key, @NonNull T value) {
        var kv = key.valueType();
        if (!kv.isInstance(value)) throw new IllegalPropertyValueException(key);
        Map<KeyProperty<?>, Object> map = new HashMap<>(properties.size() + 1);
        map.putAll(properties);
        map.put(key, value);
        return new PropertyBag(map);
    }

    /// Returns a new [PropertyBag] with the given key and its value removed.
    ///
    /// If the key is not present, an equivalent bag (with the same contents) is returned.
    /// If the result would be empty, [root] is returned.
    ///
    /// @param <T> the value type
    /// @param key the property key to remove; must not be null
    /// @return a new bag without the given key
    /// @throws IllegalArgumentException if `key` is null
    @NonNull
    public <T> PropertyBag remove(@NonNull KeyProperty<T> key) {
        Map<KeyProperty<?>, Object> map = new HashMap<>(properties.size() + 1);
        map.putAll(properties);
        map.remove(key);
        return map.isEmpty() ? ROOT_INSTANCE : new PropertyBag(map);
    }

    /// Returns the value associated with the given key.
    ///
    /// @param <V> the value type
    /// @param key the property key; must not be null
    /// @return the value associated with `key`; never null
    /// @throws PropertyNotFoundException if this bag contains no value for `key`
    /// @throws IllegalArgumentException if `key` is null
    @NonNull
    public <V> V get(@NonNull KeyProperty<V> key) {
        var obj = properties.get(key);
        if (obj == null) throw new PropertyNotFoundException(key);
        var kv = key.valueType();
        assertTrue(kv.isInstance(obj));
        return kv.cast(obj);
    }

    /// Returns a hash code consistent with [equals], derived from the underlying property map.
    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    /// Returns a string representation of the properties in this bag.
    @NonNull
    @Override
    public String toString() {
        return properties.toString();
    }

    /// Returns `true` if `other` is a [PropertyBag] with the same key-value pairs as this bag.
    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof PropertyBag pb && Objects.equals(pb.properties, this.properties);
    }

    /// Thrown by [PropertyBag#get] when the requested key has no associated value in the bag.
    public static class PropertyNotFoundException extends NoSuchElementException {
        private static final long serialVersionUID = 1L;

        @NonNull
        private final KeyProperty<?> property;

        /// Creates a new exception for the given missing key.
        ///
        /// @param property the key that was not found; must not be null
        /// @throws IllegalArgumentException if `property` is null
        public PropertyNotFoundException(@NonNull KeyProperty<?> property) {
            this.property = property;
        }

        /// Returns the key that was not found in the [PropertyBag].
        ///
        /// @return the missing key; never null
        @NonNull
        public KeyProperty<?> getProperty() {
            return property;
        }
    }

    /// Thrown by [PropertyBag#add] when the provided value is not an instance of the key's value type.
    public static class IllegalPropertyValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        @NonNull
        private final KeyProperty<?> property;

        /// Creates a new exception for the given key whose value type constraint was violated.
        ///
        /// @param property the key whose value type was violated; must not be null
        /// @throws IllegalArgumentException if `property` is null
        public IllegalPropertyValueException(@NonNull KeyProperty<?> property) {
            this.property = property;
        }

        /// Returns the key whose value type constraint was violated.
        ///
        /// @return the offending key; never null
        @NonNull
        public KeyProperty<?> getProperty() {
            return property;
        }
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
