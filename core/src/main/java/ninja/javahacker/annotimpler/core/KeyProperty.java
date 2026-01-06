package ninja.javahacker.annotimpler.core;

import module java.base;

public interface KeyProperty<V> extends Serializable {
    public Class<V> valueType();
}
