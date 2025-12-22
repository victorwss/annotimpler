package ninja.javahacker.sqlplus.sqlfactories;

import java.lang.reflect.Method;
import lombok.NonNull;
import ninja.javahacker.sqlplus.Sql;
import ninja.javahacker.sqlplus.meta.SqlFactory;
import ninja.javahacker.sqlplus.meta.SqlSupplier;

public enum StringSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) {
        var anno = m.getAnnotation(Sql.class);
        if (anno == null) throw new UnsupportedOperationException();
        return () -> anno.value();
    }
}
