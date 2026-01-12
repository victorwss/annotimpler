package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public enum ResourceSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Method m) {
        var anno = m.getAnnotation(SqlFromResource.class);
        if (anno == null) throw new UnsupportedOperationException();
        var value = anno.value();
        var fromClass = anno.fromClass();
        var from = fromClass != void.class ? fromClass : m.getDeclaringClass();
        return () -> {
            try {
                var bs = from.getResourceAsStream(value).readAllBytes();
                return new String(bs, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        };
    }
}
