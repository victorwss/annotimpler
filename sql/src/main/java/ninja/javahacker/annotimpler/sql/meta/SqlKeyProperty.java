package ninja.javahacker.annotimpler.sql.meta;

import ninja.javahacker.annotimpler.core.KeyProperty;
import ninja.javahacker.annotimpler.sql.ConnectionFactory;

public enum SqlKeyProperty implements KeyProperty<ConnectionFactory> {
    INSTANCE;

    @Override
    public Class<ConnectionFactory> valueType() {
        return ConnectionFactory.class;
    }
}
