package ninja.javahacker.annotimpler.sql.sqlfactories;

import java.lang.reflect.Method;
import lombok.NonNull;
import ninja.javahacker.annotimpler.sql.Sql;
import ninja.javahacker.annotimpler.sql.meta.SqlFactory;
import ninja.javahacker.annotimpler.sql.meta.SqlSupplier;

public enum StringSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) {
        var anno = m.getAnnotation(Sql.class);
        if (anno == null) throw new UnsupportedOperationException();
        return () -> anno.value();
    }
}
