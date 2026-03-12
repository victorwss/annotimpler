package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface SqlFactory {

    @NonNull
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException;

    @NonNull
    public static ParsedSqlSupplier find(@NonNull Method m)
            throws BadImplementationException, MagicFactory.CreatorSelectionException, MagicFactory.CreationException
    {
        var annos = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(SqlSource.class)).toList();
        var nome = NameDictionary.global().getSimplifiedGenericString(m, true);
        if (annos.isEmpty()) throw new BadImplementationException("No SQL annotation found on " + nome, m.getDeclaringClass());
        if (annos.size() > 1) throw new BadImplementationException("More than one SQL annotation found on " + nome, m.getDeclaringClass());
        var sqls = annos.getFirst().annotationType().getAnnotation(SqlSource.class);
        if (sqls == null) throw new AssertionError();
        var cls = sqls.factory();
        var magic = MagicFactory.of(cls);
        var factory = magic.create();
        var sup = factory.prepare(m);
        ParsedSqlSupplier checked = () -> {
            var sql = sup.get();
            var pq = ParsedQuery.parse(sql);
            if (pq.hasErrors()) throw new SQLException("Malformed SQL for " + nome);
            return ParameterSet.parameters(pq, m);
        };
        if (sqls.lazy()) return checked;
        try {
            var eager = checked.get();
            return () -> eager;
        } catch (SQLException x) {
            throw new BadImplementationException("Malformed SQL for " + nome, m.getDeclaringClass());
        }
    }

    @FunctionalInterface
    public interface ParsedSqlSupplier {
        public ParameterSet get() throws SQLException;
    }
}
