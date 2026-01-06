package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module ninja.javahacker.annotimpler.sql;

@FunctionalInterface
public interface ConnectionFactory {

    @NonNull
    public Connection get() throws SQLException;

    public default <E> E create(@NonNull Class<E> iface) throws AnnotationsImplementor.ImplementationFailedException {
        var m = PropertyBag.root().add(SqlKeyProperty.INSTANCE, this);
        return AnnotationsImplementor.implement(iface, m);
    }
}
