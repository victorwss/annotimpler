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
        return anno.policy().prepare(FileSqlFactory::read, anno);
    }

    private static String read(@NonNull SqlFromFile anno) throws IOException {
        checkNotNull(anno);
        var value = anno.value();
        var charset = CharsetSpec.from(anno.encoding());
        var path = Path.of(value).normalize().toAbsolutePath();
        var content = Files.readAllBytes(path);
        return BytesToStringSupport.make(content, charset);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
