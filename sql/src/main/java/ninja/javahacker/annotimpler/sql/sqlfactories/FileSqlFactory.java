package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public enum FileSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Method m) {
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
