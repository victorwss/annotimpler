package ninja.javahacker.sqlplus;

import module java.base;
import module ninja.javahacker.sqlplus;

import lombok.NonNull;
import ninja.javahacker.annotimpler.AnnotationsImplementor;

@FunctionalInterface
public interface ConnectionFactory {

    @NonNull
    public Connection get() throws SQLException;

    public default <E> E create(@NonNull Class<E> iface) throws AnnotationsImplementor.ImplementationFailedException {
        var m = PropertyBag.root().add(SqlKeyProperty.INSTANCE, this);
        return AnnotationsImplementor.implement(iface, m);
    }
}
