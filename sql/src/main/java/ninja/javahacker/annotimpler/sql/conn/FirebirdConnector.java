package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for Firebird databases.
///
/// @param host The database server hostname or IP address.
/// @param port The TCP port number on which the Firebird server listens.
/// @param user The database username.
/// @param password The database password.
/// @param filename The path to the Firebird database file.
/// @param encoding The character encoding for the connection, e.g. `UTF8`, or an empty string for the default encoding.
@ConnectorJsonKey("firebird")
public record FirebirdConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename,
        @NonNull String encoding
) implements Connector.MandatoryAuthConnector<FirebirdConnector>, Connector.HostConnector<FirebirdConnector>
{
    /// Creates a `FirebirdConnector` with the given connection parameters.
    ///
    /// @param host The database server hostname or IP address.
    /// @param port The TCP port number on which the Firebird server listens.
    /// @param user The database username.
    /// @param password The database password.
    /// @param filename The path to the Firebird database file.
    /// @param encoding The character encoding for the connection, or an empty string for the default encoding.
    /// @throws IllegalArgumentException If `host`, `user`, `password`, `filename`, or `encoding` is `null`.
    public FirebirdConnector {}

    /// The standard TCP port for Firebird (3050).
    public static final int STD_PORT = 3050;

    private static final FirebirdConnector STD = new FirebirdConnector("localhost", STD_PORT, "SYSDBA", "masterkey", "", "UTF8");

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `FirebirdConnector` instance.
    @NonNull
    public static FirebirdConnector std() {
        return STD;
    }

    /// Creates a `FirebirdConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param host The optional hostname to override the default; if absent, the default hostname is used.
    /// @param port The optional port to override the default; if absent, the default port is used.
    /// @param user The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param filename The optional database file path to override the default; if absent, the default path is used.
    /// @param encoding The optional character encoding to override the default; if absent, the default encoding is used.
    /// @return A new `FirebirdConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `host`, `port`, `user`, `password`, `filename`, or `encoding` is `null`.
    @NonNull
    @JsonCreator
    public static FirebirdConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> filename,
            @NonNull Optional<String> encoding)
    {
        var r = new FirebirdConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        encoding.ifPresent(v -> r[0] = r[0].withEncoding(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The non-null JDBC URL string.
    @NonNull
    @Override
    public String url() {
        return "jdbc:firebird://" + host + ":" + port + "/" + filename + (encoding.isEmpty() ? "" : "?encoding=" + encoding);
    }

    /// Returns a copy of this connector with the `host` field replaced by the given value.
    ///
    /// @param host The new database server hostname or IP address.
    /// @return A new `FirebirdConnector` with the updated host.
    /// @throws IllegalArgumentException If `host` is `null`.
    @NonNull
    @Override
    public FirebirdConnector withHost(@NonNull String host) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `FirebirdConnector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public FirebirdConnector withUser(@NonNull String user) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `FirebirdConnector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public FirebirdConnector withPassword(@NonNull String password) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    /// Returns a copy of this connector with the `filename` field replaced by the given value.
    ///
    /// @param filename The new path to the Firebird database file.
    /// @return A new `FirebirdConnector` with the updated filename.
    /// @throws IllegalArgumentException If `filename` is `null`.
    @NonNull
    public FirebirdConnector withFilename(@NonNull String filename) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    /// Returns a copy of this connector with the `port` field replaced by the given value.
    ///
    /// @param port The new TCP port number.
    /// @return A new `FirebirdConnector` with the updated port.
    @NonNull
    @Override
    public FirebirdConnector withPort(int port) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    /// Returns a copy of this connector with the `encoding` field replaced by the given value.
    ///
    /// @param encoding The new character encoding, or an empty string for the default encoding.
    /// @return A new `FirebirdConnector` with the updated encoding.
    /// @throws IllegalArgumentException If `encoding` is `null`.
    @NonNull
    public FirebirdConnector withEncoding(@NonNull String encoding) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }
}
