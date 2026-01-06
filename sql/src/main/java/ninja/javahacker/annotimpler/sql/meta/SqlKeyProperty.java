package ninja.javahacker.annotimpler.sql.meta;

import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

public enum SqlKeyProperty implements KeyProperty<ConnectionFactory> {
    INSTANCE;

    @Override
    public Class<ConnectionFactory> valueType() {
        return ConnectionFactory.class;
    }
}
