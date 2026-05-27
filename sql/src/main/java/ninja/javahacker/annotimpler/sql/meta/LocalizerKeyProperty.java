package ninja.javahacker.annotimpler.sql.meta;

import module java.base;
import module ninja.javahacker.annotimpler.core;

public enum LocalizerKeyProperty implements KeyProperty<Locale> {
    INSTANCE;

    @Override
    public Class<Locale> valueType() {
        return Locale.class;
    }
}
