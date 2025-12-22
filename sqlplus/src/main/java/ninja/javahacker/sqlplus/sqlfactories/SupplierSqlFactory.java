package ninja.javahacker.sqlplus.sqlfactories;

import java.lang.reflect.Method;
import lombok.NonNull;
import ninja.javahacker.magicfactory.ConstructionException;
import ninja.javahacker.magicfactory.MagicFactory;
import ninja.javahacker.sqlplus.SqlFromClass;
import ninja.javahacker.sqlplus.meta.SqlFactory;
import ninja.javahacker.sqlplus.meta.SqlSupplier;

public enum SupplierSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Class<?> iface, @NonNull Method m) throws ConstructionException {
        var anno = m.getAnnotation(SqlFromClass.class);
        if (anno == null) throw new UnsupportedOperationException();
        var implClass = MagicFactory.of(anno.value());
        return implClass.create();
    }
}
