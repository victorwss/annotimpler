package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module ninja.javahacker.annotimpler.sql;

public enum SupplierSqlFactory implements SqlFactory {
    INSTANCE;

    @Override
    public SqlSupplier prepare(@NonNull Method m) throws BadImplementationException {
        var anno = m.getAnnotation(SqlFromClass.class);
        if (anno == null) throw new UnsupportedOperationException();
        var ref = anno.value();
        try {
            var implClass = MagicFactory.of(ref);
            return implClass.create();
        } catch (MagicFactory.CreationException | MagicFactory.CreatorSelectionException e) {
            throw new BadImplementationException("Can't create a SqlSupplier.", e, ref);
        }
    }
}
