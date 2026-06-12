package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Singleton [SqlFactory] that loads the SQL string from a file on the filesystem, as specified by a
/// [SqlFromFile]-annotated method.
/// The reading strategy (eager, lazy, etc.) is controlled by [SqlFromFile#policy()].
public enum FileSqlFactory implements SqlFactory {

    /// The sole instance of this factory.
    INSTANCE;

    /// Reads the [SqlFromFile] annotation from `m` and returns a [SqlSupplier] that loads the file
    /// according to the configured policy.
    ///
    /// @param m The method carrying the [SqlFromFile] annotation.
    /// @return A [SqlSupplier] that supplies the content of the specified file.
    /// @throws BadImplementationException If the file cannot be found (for eager read policies).
    /// @throws UnsupportedOperationException If `m` has no [SqlFromFile] annotation.
    /// @throws IllegalArgumentException If `m` is `null`.
    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromFile.class);
        if (anno == null) throw new UnsupportedOperationException();
        return anno.policy().prepare(FileSqlFactory::read, anno);
    }

    private static String read(@NonNull SqlFromFile anno) throws IOException {
        checkNotNull(anno);
        var value = anno.value();
        var path = Path.of(value).normalize().toAbsolutePath();
        var content = Files.readAllBytes(path);
        return CharsetSpec.instance(anno.encoding()).decode(content);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
