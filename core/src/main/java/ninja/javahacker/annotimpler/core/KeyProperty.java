package ninja.javahacker.annotimpler.core;

import java.io.Serializable;

public interface KeyProperty<V> extends Serializable {
    public Class<V> valueType();
}
