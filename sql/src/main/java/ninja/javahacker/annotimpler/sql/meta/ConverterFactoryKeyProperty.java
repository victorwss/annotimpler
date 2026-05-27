package ninja.javahacker.annotimpler.sql.meta;

import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

public enum ConverterFactoryKeyProperty implements KeyProperty<ConverterFactory> {
    INSTANCE;

    @Override
    public Class<ConverterFactory> valueType() {
        return ConverterFactory.class;
    }
}
