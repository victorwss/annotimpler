package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for PostgreSQL databases.
///
/// @param host The database server hostname or IP address.
/// @param port The TCP port number on which the PostgreSQL server listens.
/// @param user The database username.
/// @param password The database password.
/// @param database The name of the database to connect to.
/// @param ssl Whether to enable SSL/TLS encryption for the connection.
@ConnectorJsonKey("postgresql")
public record PostgreSqlConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database,
        boolean ssl
) implements Connector.MandatoryAuthConnector<PostgreSqlConnector>, Connector.HostConnector<PostgreSqlConnector>
{
    /// Creates a `PostgreSqlConnector` with the given connection parameters.
    ///
    /// @param host The database server hostname or IP address.
    /// @param port The TCP port number on which the PostgreSQL server listens.
    /// @param user The database username.
    /// @param password The database password.
    /// @param database The name of the database to connect to.
    /// @param ssl Whether to enable SSL/TLS encryption for the connection.
    /// @throws IllegalArgumentException If `host`, `user`, `password`, or `database` is `null`.
    public PostgreSqlConnector {}

    /// The standard TCP port for PostgreSQL (5432).
    public static final int STD_PORT = 5432;

    private static final PostgreSqlConnector STD = new PostgreSqlConnector("localhost", STD_PORT, "admin", "admin", "", true);

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `PostgreSqlConnector` instance.
    @NonNull
    public static PostgreSqlConnector std() {
        return STD;
    }

    /// Creates a `PostgreSqlConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param host The optional hostname to override the default; if absent, the default hostname is used.
    /// @param port The optional port to override the default; if absent, the default port is used.
    /// @param user The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param database The optional database name to override the default; if absent, the default database name is used.
    /// @param ssl The optional SSL flag to override the default; if absent, the default SSL setting is used.
    /// @return A new `PostgreSqlConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `host`, `port`, `user`, `password`, `database`, or `ssl` is `null`.
    @NonNull
    @JsonCreator
    public static PostgreSqlConnector create(
        @NonNull Optional<String> host,
        @NonNull OptionalInt port,
        @NonNull Optional<String> user,
        @NonNull Optional<String> password,
        @NonNull Optional<String> database,
        @NonNull Optional<Boolean> ssl)
    {
        var r = new PostgreSqlConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        database.ifPresent(v -> r[0] = r[0].withDatabase(v));
        ssl.ifPresent(v -> r[0] = r[0].withSsl(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The non-null JDBC URL string.
    @NonNull
    @Override
    public String url() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database + (ssl ? "?ssl=true" : "");
    }

    /// Returns a copy of this connector with the `host` field replaced by the given value.
    ///
    /// @param host The new database server hostname or IP address.
    /// @return A new `PostgreSqlConnector` with the updated host.
    /// @throws IllegalArgumentException If `host` is `null`.
    @NonNull
    @Override
    public PostgreSqlConnector withHost(@NonNull String host) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `PostgreSqlConnector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public PostgreSqlConnector withUser(@NonNull String user) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `PostgreSqlConnector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public PostgreSqlConnector withPassword(@NonNull String password) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    /// Returns a copy of this connector with the `database` field replaced by the given value.
    ///
    /// @param database The new database name.
    /// @return A new `PostgreSqlConnector` with the updated database.
    /// @throws IllegalArgumentException If `database` is `null`.
    @NonNull
    public PostgreSqlConnector withDatabase(@NonNull String database) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    /// Returns a copy of this connector with the `port` field replaced by the given value.
    ///
    /// @param port The new TCP port number.
    /// @return A new `PostgreSqlConnector` with the updated port.
    @NonNull
    @Override
    public PostgreSqlConnector withPort(int port) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    /// Returns a copy of this connector with the `ssl` field replaced by the given value.
    ///
    /// @param ssl `true` to enable SSL/TLS encryption, `false` to disable it.
    /// @return A new `PostgreSqlConnector` with the updated SSL setting.
    @NonNull
    public PostgreSqlConnector withSsl(boolean ssl) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }
}
