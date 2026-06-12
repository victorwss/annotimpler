package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Singleton [SqlFactory] that loads the SQL string from a classpath resource, as specified by a
/// [SqlFromResource]-annotated method.
/// The resource is always read eagerly at prepare time using [ReadPolicy#ON_STARTUP].
public enum ResourceSqlFactory implements SqlFactory {

    /// The sole instance of this factory.
    INSTANCE;

    /// Reads the [SqlFromResource] annotation from `m` and returns a [SqlSupplier] that supplies
    /// the content of the classpath resource.
    /// The class used to locate the resource is [SqlFromResource#fromClass()] when specified, or the declaring
    /// class of `m` otherwise.
    ///
    /// @param m The method carrying the [SqlFromResource] annotation.
    /// @return A [SqlSupplier] that supplies the content of the specified classpath resource.
    /// @throws BadImplementationException If the resource cannot be found or read.
    /// @throws UnsupportedOperationException If `m` has no [SqlFromResource] annotation.
    /// @throws IllegalArgumentException If `m` is `null`.
    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromResource.class);
        if (anno == null) throw new UnsupportedOperationException();
        var r = new Resource(anno, m);
        return ReadPolicy.ON_STARTUP.prepare(ResourceSqlFactory::read, r);
    }

    private static String read(@NonNull Resource res) throws IOException {
        checkNotNull(res);
        var anno = res.anno();
        var value = anno.value();
        var fromClass = anno.fromClass();
        var from = fromClass != void.class ? fromClass : res.m().getDeclaringClass();
        try (var stream = from.getResourceAsStream(value)) {
            if (stream == null) throw new FileNotFoundException(from.getName() + " - " + value);
            return CharsetSpec.instance(anno.encoding()).decode(stream.readAllBytes());
        }
    }

    private static record Resource(SqlFromResource anno, Method m) {}

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
