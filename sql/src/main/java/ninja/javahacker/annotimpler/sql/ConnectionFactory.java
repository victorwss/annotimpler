package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Factory that creates [Connection] instances and serves as the entry point for
/// implementing annotated SQL interfaces.
///
/// As a functional interface, any lambda or method reference that produces a [Connection]
/// can be used directly as a `ConnectionFactory`. The most common source is one of the
/// concrete [ninja.javahacker.annotimpler.sql.conn.Connector] implementations:
///
/// ```java
/// ConnectionFactory factory = MySqlConnector.std().withDatabase("mydb").withAuth("u", "p");
/// MyDao dao = factory.create(MyDao.class);
/// ```
///
/// Connections produced by [ninja.javahacker.annotimpler.sql.conn.UrlConnector#get()]
/// have [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE] isolation and autocommit disabled.
@FunctionalInterface
public interface ConnectionFactory {

    /// Opens a new [Connection].
    ///
    /// @return A new, open, non-null database connection.
    /// @throws SQLException If a database access error occurs.
    @NonNull
    public Connection get() throws SQLException;

    /// Creates an annotation-driven implementation of the given interface, using this factory
    /// as the connection source and [ConverterFactory#STD] as the type converter.
    ///
    /// @param <E> The interface type to implement.
    /// @param iface The interface class to implement.
    /// @return A non-null proxy instance implementing `iface`.
    /// @throws BadImplementationException If any annotated method on `iface` is malformed.
    /// @throws IllegalArgumentException If `iface` is `null`.
    public default <E> E create(@NonNull Class<E> iface) throws BadImplementationException {
        var m = PropertyBag.root()
                .add(ConnectionFactoryKeyProperty.INSTANCE, this)
                .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.STD)
                .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);
        return AnnotationsImplementor.implement(iface, m);
    }
}
