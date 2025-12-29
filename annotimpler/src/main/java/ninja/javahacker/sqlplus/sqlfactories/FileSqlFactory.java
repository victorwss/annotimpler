package ninja.javahacker.sqlplus.sqlfactories;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import lombok.NonNull;
import ninja.javahacker.sqlplus.SqlFromFile;
import ninja.javahacker.sqlplus.meta.SqlFactory;
import ninja.javahacker.sqlplus.meta.SqlSupplier;

public enum FileSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) {
        var anno = m.getAnnotation(SqlFromFile.class);
        if (anno == null) throw new UnsupportedOperationException();
        var value = anno.value();
        return () -> {
            try {
                return Files.readString(Path.of(value), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        };
    }
}
