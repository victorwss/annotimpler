package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

public enum FileSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromFile.class);
        if (anno == null) throw new UnsupportedOperationException();
        var value = anno.value();
        return anno.policy().prepare(FileSqlFactory::read, value);
    }

    private static String read(@NonNull String value) throws IOException {
        return Files.readString(Path.of(value), StandardCharsets.UTF_8);
    }
}
