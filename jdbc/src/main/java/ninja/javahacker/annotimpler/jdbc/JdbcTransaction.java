package ninja.javahacker.annotimpler.jdbc;

import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// Holds the active database connection and its unique identifier for one transaction.
///
/// @param connection The open database connection for the current transaction.
/// @param id The unique string identifier assigned to this transaction.
@PackagePrivate
record JdbcTransaction(@NonNull Connection connection, @NonNull String id) implements Transactor.Transaction<Connection> {

    /// Creates a `JdbcTransaction` with the given connection and identifier.
    ///
    /// @param connection The open database connection for the current transaction.
    /// @param id The unique string identifier assigned to this transaction.
    public JdbcTransaction {
        checkNotNull(connection); // Check recognized by lombok.
        checkNotNull(id); // Check recognized by lombok.
    }

    /// {@inheritDoc}
    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    /// {@inheritDoc}
    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /// {@inheritDoc}
    @Override
    public void close() throws SQLException {
        connection.close();
    }

    /// {@inheritDoc}
    @Override
    public Connection unwrap() {
        return connection;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
