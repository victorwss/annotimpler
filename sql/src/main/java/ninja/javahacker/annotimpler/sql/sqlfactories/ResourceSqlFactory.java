package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

public enum ResourceSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromResource.class);
        if (anno == null) throw new UnsupportedOperationException();
        var value = anno.value();
        var fromClass = anno.fromClass();
        var from = fromClass != void.class ? fromClass : m.getDeclaringClass();
        var r = new Resource(from, value);
        return ReadPolicy.ON_STARTUP.prepare(ResourceSqlFactory::read, r);
    }

    private static String read(@NonNull Resource res) throws IOException {
        var bs = res.from.getResourceAsStream(res.value).readAllBytes();
        return new String(bs, StandardCharsets.UTF_8);
    }

    private static record Resource(Class<?> from, String value) {}
}
