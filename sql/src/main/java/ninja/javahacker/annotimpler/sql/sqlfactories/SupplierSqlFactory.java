package ninja.javahacker.annotimpler.sql.sqlfactories;

import java.lang.reflect.Method;
import lombok.NonNull;
import ninja.javahacker.annotimpler.magicfactory.ConstructionException;
import ninja.javahacker.annotimpler.magicfactory.MagicFactory;
import ninja.javahacker.annotimpler.sql.SqlFromClass;
import ninja.javahacker.annotimpler.sql.meta.SqlFactory;
import ninja.javahacker.annotimpler.sql.meta.SqlSupplier;

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
