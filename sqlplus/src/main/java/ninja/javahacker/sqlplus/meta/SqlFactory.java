package ninja.javahacker.sqlplus.meta;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.stream.Stream;
import lombok.NonNull;
import ninja.javahacker.magicfactory.ConstructionException;
import ninja.javahacker.magicfactory.MagicFactory;
import ninja.javahacker.magicfactory.NameDictionary;
import ninja.javahacker.sqlplus.meta.ParameterSet.MethodParameterSet;

@FunctionalInterface
public interface SqlFactory {

    @NonNull
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) throws ConstructionException;

    @NonNull
    public static ParsedSqlSupplier find(@NonNull Class<?> iface, @NonNull Method m) throws ConstructionException {
        var annos = Stream.of(m.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(SqlSource.class)).toList();
        var nome = NameDictionary.global().getSimplifiedGenericString(m, true);
        if (annos.isEmpty()) throw new ConstructionException("No Sql annotation found on " + nome, iface);
        if (annos.size() > 1) throw new ConstructionException("More than one Sql annotation found on " + nome, iface);
        var sqls = annos.getFirst().annotationType().getAnnotation(SqlSource.class);
        if (sqls == null) throw new AssertionError();
        var cls = sqls.factory();
        var magic = MagicFactory.of(cls);
        var factory = magic.create();
        var sup = factory.prepare(iface, m);
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
            throw new ConstructionException("Malformed SQL for " + nome, iface);
        }
    }

    @FunctionalInterface
    public interface ParsedSqlSupplier {
        public MethodParameterSet get() throws SQLException;
    }
}
