package ninja.javahacker.annotimpler.jdbc;

import java.util.Locale;
import lombok.NonNull;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.jdbc;

/// Serves as the entry point for implementing annotated SQL interfaces.
///
/// Example usage:
///
/// ```java
/// ConnectionFactory factory = MySqlConnector.std().withDatabase("mydb").withAuth("username", "password");
/// MyDao dao = JdbcAnnotimpler.create(factory, MyDao.class);
/// ```
///
/// Connections produced by most [Connector]s implementations
/// have [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE] isolation and autocommit disabled.
public class JdbcAnnotimpler {

    /// This class is not instantiable.
    /// @throws UnsupportedOperationException Always.
    private JdbcAnnotimpler() {
        throw new UnsupportedOperationException();
    }

    /// Creates an annotation-driven implementation of the given interface, using a [ConnectionFactory]
    /// as the connection source and [ConverterFactory#std()] as the type converter.
    ///
    /// The properties given are [ConnectionFactoryKeyProperty] valued with the given [ConnectionFactory],
    /// [ConverterFactoryKeyProperty] valued with the standard [ConverterFactory] and [LocalizerKeyProperty] valued with the root locale.
    ///
    /// @param <E> The interface type to implement.
    /// @param iface The interface class to implement.
    /// @param factory The connection source.
    /// @return A proxy instance implementing `iface`; never `null`.
    /// @throws BadImplementationException If any annotated method on `iface` is malformed or if the implementation doesn't accept the
    ///         default properties.
    /// @throws IllegalArgumentException If `iface` is `null`.
    @NonNull
    public static <E> E create(@NonNull ConnectionFactory factory, @NonNull Class<E> iface) throws BadImplementationException {
        var bag = PropertyBag.root()
                .add(ConnectionFactoryKeyProperty.INSTANCE, factory)
                .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.std())
                .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);
        try {
            return AnnotationsImplementor.implement(iface, bag);
        } catch (PropertyBag.PropertyNotFoundException e) {
            throw new BadImplementationException("The implementation refused the default properties.", e, iface);
        }
    }
}
