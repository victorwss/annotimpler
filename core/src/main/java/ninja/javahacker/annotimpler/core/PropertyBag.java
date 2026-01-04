package ninja.javahacker.annotimpler.core;

import module java.base;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

public final class PropertyBag {

    private static final PropertyBag ROOT_INSTANCE = new PropertyBag();

    private final Map<KeyProperty<?>, Object> properties;

    private PropertyBag() {
        this.properties = Map.of();
    }

    private PropertyBag(Map<KeyProperty<?>, Object> properties) {
        if (properties == null) throw new AssertionError();
        this.properties = Map.copyOf(properties);
    }

    public static PropertyBag root() {
        return ROOT_INSTANCE;
    }

    @NonNull
    public <T> PropertyBag add(@NonNull KeyProperty<T> key, @NonNull T value) {
        var kv = key.valueType();
        if (!kv.isInstance(value)) throw new IllegalPropertyValueException(key);
        Map<KeyProperty<?>, Object> map = new HashMap<>(properties.size() + 1);
        map.putAll(properties);
        map.put(key, value);
        return new PropertyBag(map);
    }

    @NonNull
    public <T> PropertyBag remove(@NonNull KeyProperty<T> key) {
        Map<KeyProperty<?>, Object> map = new HashMap<>(properties.size() + 1);
        map.putAll(properties);
        map.remove(key);
        return map.isEmpty() ? ROOT_INSTANCE : new PropertyBag(map);
    }

    @NonNull
    public <V> V get(@NonNull KeyProperty<V> key) {
        var obj = properties.get(key);
        if (obj == null) throw new PropertyNotFoundException(key);
        var kv = key.valueType();
        if (!kv.isInstance(obj)) throw new AssertionError(key);
        return kv.cast(obj);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public String toString() {
        return properties.toString();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof PropertyBag pb && Objects.equals(pb.properties, this.properties);
    }

    public static class PropertyNotFoundException extends NoSuchElementException {
        private static final long serialVersionUID = 1L;

        @NonNull
        private final KeyProperty<?> property;

        public PropertyNotFoundException(@NonNull KeyProperty<?> property) {
            this.property = property;
        }

        @NonNull
        public KeyProperty<?> getProperty() {
            return property;
        }
    }

    public static class IllegalPropertyValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        @NonNull
        private final KeyProperty<?> property;

        public IllegalPropertyValueException(@NonNull KeyProperty<?> property) {
            this.property = property;
        }

        @NonNull
        public KeyProperty<?> getProperty() {
            return property;
        }
    }
}
