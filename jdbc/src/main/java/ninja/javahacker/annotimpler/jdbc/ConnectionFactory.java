package ninja.javahacker.annotimpler.jdbc;

import ninja.javahacker.annotimpler.jdbc.JdbcTransaction;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import ninja.javahacker.annotimpler.sql.Transactor;

/// Factory that creates [Connection] instances and serves as the entry point for
/// implementing annotated SQL interfaces.
///
/// As a functional interface, any lambda or method reference that produces a [Connection]
/// can be used directly as a `ConnectionFactory`. The most common source is one of the
/// concrete [ninja.javahacker.annotimpler.jdbc.conn.Connector] implementations:
///
/// ```java
/// ConnectionFactory factory = MySqlConnector.std().withDatabase("mydb").withAuth("u", "p");
/// MyDao dao = factory.create(MyDao.class);
/// ```
///
/// Connections produced by [ninja.javahacker.annotimpler.jdbc.conn.UrlConnector#get()]
/// have [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE] isolation and autocommit disabled.
@FunctionalInterface
public interface ConnectionFactory extends Transactor.TransactionFactory<Connection> {

    /// Opens a new [Connection].
    ///
    /// @return A new and open database connection; never `null`.
    /// @throws SQLException If a database access error occurs.
    @NonNull
    public Connection get() throws SQLException;

    /// Creates an annotation-driven implementation of the given interface, using this factory
    /// as the connection source and [ConverterFactory#std()] as the type converter.
    ///
    /// The properties given are [ConnectionFactoryKeyProperty] valued with `this`,
    /// [ConverterFactoryKeyProperty] valued with the standard [ConverterFactory] and [LocalizerKeyProperty] valued with the root locale.
    ///
    /// @param <E> The interface type to implement.
    /// @param iface The interface class to implement.
    /// @return A proxy instance implementing `iface`; never `null`.
    /// @throws BadImplementationException If any annotated method on `iface` is malformed or if the implementation doesn't accept the
    ///         default properties.
    /// @throws IllegalArgumentException If `iface` is `null`.
    @NonNull
    public default <E> E create(@NonNull Class<E> iface) throws BadImplementationException {
        var bag = PropertyBag.root()
                .add(ConnectionFactoryKeyProperty.INSTANCE, this)
                .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.std())
                .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);
        try {
            return AnnotationsImplementor.implement(iface, bag);
        } catch (PropertyBag.PropertyNotFoundException e) {
            throw new BadImplementationException("The implementation refused the default properties.", e, iface);
        }
    }

    /// Begins a new [JdbcTransaction] wrapping a freshly-opened [Connection] from [#get()].
    ///
    /// @param id The unique string identifier assigned to the new transaction.
    /// @return The newly-begun transaction; never `null`.
    /// @throws SQLException If a database access error occurs while opening the connection.
    /// @throws IllegalArgumentException If `id` is `null`.
    @Override
    public default Transactor.Transaction<Connection> begin(@NonNull String id) throws SQLException {
        return new JdbcTransaction(get(), id);
    }
}
