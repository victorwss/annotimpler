package ninja.javahacker.annotimpler;

import java.io.Serializable;

public interface KeyProperty<V> extends Serializable {
    public Class<V> valueType();
}
