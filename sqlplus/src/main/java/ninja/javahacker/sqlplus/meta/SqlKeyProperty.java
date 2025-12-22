package ninja.javahacker.sqlplus.meta;

import ninja.javahacker.annotimpler.KeyProperty;
import ninja.javahacker.sqlplus.ConnectionFactory;

public enum SqlKeyProperty implements KeyProperty<ConnectionFactory> {
    INSTANCE;

    @Override
    public Class<ConnectionFactory> valueType() {
        return ConnectionFactory.class;
    }
}
