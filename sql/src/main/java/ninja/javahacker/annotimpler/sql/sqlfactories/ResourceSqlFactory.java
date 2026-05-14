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
        var bs = from.getResourceAsStream(value).readAllBytes();
        var encoding = CharsetSpec.from(anno.encoding());
        return new String(bs, encoding);
    }

    private static record Resource(SqlFromResource anno, Method m) {}

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
