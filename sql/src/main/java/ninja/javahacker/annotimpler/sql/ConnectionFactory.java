package ninja.javahacker.annotimpler.sql;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;
import lombok.NonNull;

@FunctionalInterface
public interface ConnectionFactory {

    @NonNull
    public Connection get() throws SQLException;

    public default <E> E create(@NonNull Class<E> iface) throws AnnotationsImplementor.ImplementationFailedException {
        var m = PropertyBag.root().add(SqlKeyProperty.INSTANCE, this);
        return AnnotationsImplementor.implement(iface, m);
    }
}
