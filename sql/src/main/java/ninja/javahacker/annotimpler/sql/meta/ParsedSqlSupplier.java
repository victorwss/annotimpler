package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface ParsedSqlSupplier {
    public ParsedQuery get() throws SQLException;

    @NonNull
    public static ParsedSqlSupplier find(boolean strict, @NonNull ParameterSet pset) throws BadImplementationException {
        var m = pset.getMethod();
        var name = NameDictionary.global().getSimplifiedGenericString(m, true);
        var annos = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(SqlSource.class)).toList();
        if (annos.isEmpty()) throw new BadImplementationException("No SQL annotation found on " + name, m.getDeclaringClass());
        if (annos.size() > 1) throw new BadImplementationException("More than one SQL annotation found on " + name, m.getDeclaringClass());
        var sqls = annos.getFirst().annotationType().getAnnotation(SqlSource.class);
        if (sqls == null) throw new AssertionError();
        var cls = sqls.value();
        SqlFactory factory;
        try {
            factory = MagicFactory.of(cls).create();
        } catch (MagicFactory.CreatorSelectionException | MagicFactory.CreationException x) {
            throw new BadImplementationException("Can't instantiate " + cls.getSimpleName() + " to handle " + name, m.getDeclaringClass());
        }
        var sup = factory.prepare(m);
        return () -> {
            checkNotNull(pset);
            var sql = sup.get();
            var pq = ParsedQuery.parse(sql);
            if (strict) {
                if (pq.hasErrors()) throw new SQLException("Malformed SQL for " + name);
                var names1 = new HashSet<>(pset.paramNames());
                var names2 = pq.params().keySet();
                if (!Objects.equals(names1, names2)) throw new SQLException("Method parameters mismatches SQL for " + name);
            }
            return pq;
        };
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
