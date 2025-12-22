package ninja.javahacker.sqlplus.sqlfactories;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import lombok.NonNull;
import ninja.javahacker.sqlplus.SqlFromResource;
import ninja.javahacker.sqlplus.meta.SqlFactory;
import ninja.javahacker.sqlplus.meta.SqlSupplier;

public enum ResourceSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) {
        var anno = m.getAnnotation(SqlFromResource.class);
        if (anno == null) throw new UnsupportedOperationException();
        var value = anno.value();
        return () -> {
            try {
                var bs = iface.getResourceAsStream(value).readAllBytes();
                return new String(bs, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        };
    }
}
