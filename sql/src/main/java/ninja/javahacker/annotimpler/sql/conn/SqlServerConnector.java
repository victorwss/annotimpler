package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for Microsoft SQL Server databases.
///
/// @param host The database server hostname or IP address.
/// @param port The TCP port number on which the SQL Server listens.
/// @param user The database username.
/// @param password The database password.
/// @param database The name of the database to connect to.
@ConnectorJsonKey("sqlserver")
public record SqlServerConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database
) implements Connector.MandatoryAuthConnector<SqlServerConnector>, Connector.HostConnector<SqlServerConnector>
{

    /// The standard TCP port for SQL Server (1433).
    public static final int STD_PORT = 1433;

    /// Standard partially configured instance filled with default values to act as the base of a builder.
    private static final SqlServerConnector STD = new SqlServerConnector("localhost", STD_PORT, "admin", "admin", "");

    /// Creates a `SqlServerConnector` with the given connection parameters.
    ///
    /// @param host The database server hostname or IP address.
    /// @param port The TCP port number on which the SQL Server listens.
    /// @param user The database username.
    /// @param password The database password.
    /// @param database The name of the database to connect to.
    /// @throws IllegalArgumentException If `host`, `user`, `password`, or `database` is `null`.
    public SqlServerConnector {}

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `SqlServerConnector` instance.
    @NonNull
    public static SqlServerConnector std() {
        return STD;
    }

    /// Creates a `SqlServerConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param host The optional hostname to override the default; if absent, the default hostname is used.
    /// @param port The optional port to override the default; if absent, the default port is used.
    /// @param user The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param database The optional database name to override the default; if absent, the default database name is used.
    /// @return A new `SqlServerConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `host`, `port`, `user`, `password`, or `database` is `null`.
    @NonNull
    @JsonCreator
    public static SqlServerConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> database)
    {
        var r = new SqlServerConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        database.ifPresent(v -> r[0] = r[0].withDatabase(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The JDBC URL string; never `null`.
    @NonNull
    @Override
    public String url() {
        return "jdbc:hyperion:sqlserver://" + host + ":" + port + ";DatabaseName=" + database;
    }

    /// Returns a copy of this connector with the `host` field replaced by the given value.
    ///
    /// @param host The new database server hostname or IP address.
    /// @return A new `SqlServerConnector` with the updated host.
    /// @throws IllegalArgumentException If `host` is `null`.
    @NonNull
    @Override
    public SqlServerConnector withHost(@NonNull String host) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `SqlServerConnector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public SqlServerConnector withUser(@NonNull String user) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `SqlServerConnector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public SqlServerConnector withPassword(@NonNull String password) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `database` field replaced by the given value.
    ///
    /// @param database The new database name.
    /// @return A new `SqlServerConnector` with the updated database.
    /// @throws IllegalArgumentException If `database` is `null`.
    @NonNull
    public SqlServerConnector withDatabase(@NonNull String database) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `port` field replaced by the given value.
    ///
    /// @param port The new TCP port number.
    /// @return A new `SqlServerConnector` with the updated port.
    @NonNull
    @Override
    public SqlServerConnector withPort(int port) {
        return new SqlServerConnector(host, port, user, password, database);
    }
}
