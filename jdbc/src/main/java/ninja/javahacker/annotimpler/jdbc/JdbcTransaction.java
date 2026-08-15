package ninja.javahacker.annotimpler.jdbc;

import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// Holds the active database connection and its unique identifier for one transaction.
///
/// @param connection The open database connection for the current transaction.
/// @param uniqueId The unique string identifier assigned to this transaction.
@PackagePrivate
record JdbcTransaction(@NonNull Connection connection, @NonNull String uniqueId) implements Transactor.Transaction<Connection> {

    /// Creates a `JdbcTransaction` with the given connection and identifier.
    ///
    /// @param connection The open database connection for the current transaction.
    /// @param id The unique string identifier assigned to this transaction.
    public JdbcTransaction {
        checkNotNull(connection); // Check recognized by lombok.
        checkNotNull(uniqueId); // Check recognized by lombok.
    }

    /// {@inheritDoc}
    @Override
    public void commit() throws Transactor.TransactionException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new Transactor.TransactionException(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public void rollback() throws Transactor.TransactionException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new Transactor.TransactionException(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public void close() throws Transactor.TransactionException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new Transactor.TransactionException(e);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Connection unwrap() {
        return connection;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
