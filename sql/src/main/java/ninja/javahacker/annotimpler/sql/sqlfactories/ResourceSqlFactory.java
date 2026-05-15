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
            var bs = stream.readAllBytes();
            var charset = CharsetSpec.from(anno.encoding());
            var cb = charset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bs));
            return new String(cb.array());
        }
    }

    private static record Resource(SqlFromResource anno, Method m) {}

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }

    @Generated
    private static void checkNotEquals(Object a, Object b) {
        if (a == b) throw new AssertionError();
    }
}
