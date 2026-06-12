package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for Oracle databases.
///
/// @param host     The database server hostname or IP address.
/// @param port     The TCP port number on which the Oracle server listens.
/// @param user     The database username.
/// @param password The database password.
/// @param database The database service name or SID.
/// @param rac      Whether to use Oracle RAC (Real Application Clusters) URL format (`@//host:port/service`)
///                 instead of the standard SID format (`@host:port:sid`).
@ConnectorJsonKey("oracle")
public record OracleConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database,
        boolean rac
) implements Connector.MandatoryAuthConnector<OracleConnector>, Connector.HostConnector<OracleConnector>
{
    /// The standard TCP port for Oracle (1521).
    public static final int STD_PORT = 1521;

    private static final OracleConnector STD = new OracleConnector("localhost", STD_PORT, "admin", "admin", "", false);

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `OracleConnector` instance.
    @NonNull
    public static OracleConnector std() {
        return STD;
    }

    /// Creates a `OracleConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param host     The optional hostname to override the default; if absent, the default hostname is used.
    /// @param port     The optional port to override the default; if absent, the default port is used.
    /// @param user     The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param database The optional database service name or SID to override the default; if absent, the default is used.
    /// @param rac      The optional RAC flag to override the default; if absent, the default RAC setting is used.
    /// @return A new `OracleConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `host`, `port`, `user`, `password`, `database`, or `rac` is `null`.
    @JsonCreator
    public static OracleConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> database,
            @NonNull Optional<Boolean> rac)
    {
        var r = new OracleConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        database.ifPresent(v -> r[0] = r[0].withDatabase(v));
        rac.ifPresent(v -> r[0] = r[0].withRac(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The non-null JDBC URL string.
    @NonNull
    @Override
    public String url() {
        return rac
                ? "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database
                : "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
    }

    /// Returns a copy of this connector with the `host` field replaced by the given value.
    ///
    /// @param host The new database server hostname or IP address.
    /// @return A new `OracleConnector` with the updated host.
    /// @throws IllegalArgumentException If `host` is `null`.
    @NonNull
    @Override
    public OracleConnector withHost(@NonNull String host) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `OracleConnector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public OracleConnector withUser(@NonNull String user) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `OracleConnector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public OracleConnector withPassword(@NonNull String password) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    /// Returns a copy of this connector with the `database` field replaced by the given value.
    ///
    /// @param database The new database service name or SID.
    /// @return A new `OracleConnector` with the updated database.
    /// @throws IllegalArgumentException If `database` is `null`.
    @NonNull
    public OracleConnector withDatabase(@NonNull String database) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    /// Returns a copy of this connector with the `port` field replaced by the given value.
    ///
    /// @param port The new TCP port number.
    /// @return A new `OracleConnector` with the updated port.
    @NonNull
    @Override
    public OracleConnector withPort(int port) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    /// Returns a copy of this connector with the `rac` field replaced by the given value.
    ///
    /// @param rac `true` to use Oracle RAC URL format, `false` for standard SID format.
    /// @return A new `OracleConnector` with the updated RAC setting.
    @NonNull
    public OracleConnector withRac(boolean rac) {
        return new OracleConnector(host, port, user, password, database, rac);
    }
}
