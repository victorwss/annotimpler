package ninja.javahacker.annotimpler.core;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

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

    @NonNull
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
        assertTrue(kv.isInstance(obj));
        return kv.cast(obj);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @NonNull
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

    @Generated
    private static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
