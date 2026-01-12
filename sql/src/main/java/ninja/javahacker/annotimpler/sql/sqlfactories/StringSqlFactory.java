package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

public enum StringSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Method m) {
        var anno = m.getAnnotation(Sql.class);
        if (anno == null) throw new UnsupportedOperationException();
        return () -> anno.value();
    }
}
