package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for IBM Db2 databases.
///
/// @param host     The database server hostname or IP address.
/// @param port     The TCP port number on which the Db2 server listens.
/// @param user     The database username.
/// @param password The database password.
/// @param database The name of the database to connect to.
@ConnectorJsonKey("db2")
public record Db2Connector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database
) implements Connector.MandatoryAuthConnector<Db2Connector>, Connector.HostConnector<Db2Connector>
{
    /// The standard TCP port for Db2 (50000).
    public static final int STD_PORT = 50000;

    private static final Db2Connector STD = new Db2Connector("localhost", STD_PORT, "admin", "admin", "");

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `Db2Connector` instance.
    @NonNull
    public static Db2Connector std() {
        return STD;
    }

    /// Creates a `Db2Connector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param host     The optional hostname to override the default; if absent, the default hostname is used.
    /// @param port     The optional port to override the default; if absent, the default port is used.
    /// @param user     The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param database The optional database name to override the default; if absent, the default database name is used.
    /// @return A new `Db2Connector` with the applied overrides.
    /// @throws IllegalArgumentException If `host`, `port`, `user`, `password`, or `database` is `null`.
    @JsonCreator
    public static Db2Connector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> database)
    {
        var r = new Db2Connector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        database.ifPresent(v -> r[0] = r[0].withDatabase(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The non-null JDBC URL string.
    @NonNull
    @Override
    public String url() {
        return "jdbc:db2://" + host + ":" + port + "/" + database;
    }

    /// Returns a copy of this connector with the `host` field replaced by the given value.
    ///
    /// @param host The new database server hostname or IP address.
    /// @return A new `Db2Connector` with the updated host.
    /// @throws IllegalArgumentException If `host` is `null`.
    @NonNull
    @Override
    public Db2Connector withHost(@NonNull String host) {
        return new Db2Connector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `Db2Connector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public Db2Connector withUser(@NonNull String user) {
        return new Db2Connector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `Db2Connector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public Db2Connector withPassword(@NonNull String password) {
        return new Db2Connector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `database` field replaced by the given value.
    ///
    /// @param database The new database name.
    /// @return A new `Db2Connector` with the updated database.
    /// @throws IllegalArgumentException If `database` is `null`.
    @NonNull
    public Db2Connector withDatabase(@NonNull String database) {
        return new Db2Connector(host, port, user, password, database);
    }

    /// Returns a copy of this connector with the `port` field replaced by the given value.
    ///
    /// @param port The new TCP port number.
    /// @return A new `Db2Connector` with the updated port.
    @NonNull
    @Override
    public Db2Connector withPort(int port) {
        return new Db2Connector(host, port, user, password, database);
    }
}
