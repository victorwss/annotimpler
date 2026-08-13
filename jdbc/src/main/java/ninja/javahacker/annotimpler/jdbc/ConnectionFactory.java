package ninja.javahacker.annotimpler.jdbc;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// Factory that creates [Connection] instances.
///
/// As a functional interface, any lambda or method reference that produces a [Connection]
/// can be used directly as a `ConnectionFactory`. The most common source is one of the
/// concrete [ninja.javahacker.annotimpler.jdbc.conn.Connector] implementations:
///
/// ```java
/// ConnectionFactory factory = MySqlConnector.std().withDatabase("mydb").withAuth("username", "password");
/// Connection newConnection = factory.get();
/// ```
///
/// Connections produced by most [ninja.javahacker.annotimpler.jdbc.conn.Connector]s implementations
/// have [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE] isolation and autocommit disabled.
@FunctionalInterface
public interface ConnectionFactory extends Transactor.TransactionFactory<Connection> {

    /// Opens a new [Connection].
    ///
    /// @return A new and open database connection; never `null`.
    /// @throws SQLException If a database access error occurs.
    @NonNull
    public Connection get() throws SQLException;

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
