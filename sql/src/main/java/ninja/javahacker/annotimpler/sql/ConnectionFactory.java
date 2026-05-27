package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

@FunctionalInterface
public interface ConnectionFactory {

    @NonNull
    public Connection get() throws SQLException;

    public default <E> E create(@NonNull Class<E> iface) throws BadImplementationException {
        var m = PropertyBag.root()
                .add(ConnectionFactoryKeyProperty.INSTANCE, this)
                .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.STD)
                .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);
        return AnnotationsImplementor.implement(iface, m);
    }
}
