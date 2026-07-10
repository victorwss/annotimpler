package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;
import module java.sql;

/// Immutable JDBC connector that accepts an explicit JDBC URL and optional authentication.
///
/// @param url The JDBC connection URL.
/// @param optAuth The optional authentication credentials; [Optional#empty()] means no credentials are supplied.
@ConnectorJsonKey("url")
public record UrlConnector(
        @NonNull String url,
        @NonNull Optional<Auth> optAuth
) implements Connector
{

    /// Standard unconfigured instance to act as the base of a builder.
    private static final UrlConnector STD = new UrlConnector("", Optional.empty());

    /// Creates a `UrlConnector` with the given URL and optional authentication.
    ///
    /// @param url The JDBC connection URL.
    /// @param optAuth The optional authentication credentials.
    /// @throws IllegalArgumentException If `url` or `optAuth` is `null`.
    public UrlConnector {}

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `UrlConnector` instance.
    @NonNull
    public static UrlConnector std() {
        return STD;
    }

    /// Creates a `UrlConnector` from an optional URL value, applying the present value over the default
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param url The optional URL to override the default; if absent, the default URL is used.
    /// @return A new `UrlConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `url` is `null`.
    @NonNull
    @JsonCreator
    public static UrlConnector create(
            @NonNull Optional<String> url)
    {
        var r = new UrlConnector[] {STD};
        url.ifPresent(v -> r[0] = r[0].withUrl(v));
        return r[0];
    }

    /// Creates a `UrlConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param url The optional URL to override the default; if absent, the default URL is used.
    /// @param auth The optional authentication credentials to override the default; if absent, the default auth is used.
    /// @return A new `UrlConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `url` or `auth` is `null`.
    @NonNull
    @JsonCreator
    public static UrlConnector create(
            @NonNull Optional<String> url,
            @NonNull Optional<Auth> auth)
    {
        var r = new UrlConnector[] {STD};
        url.ifPresent(v -> r[0] = r[0].withUrl(v));
        auth.ifPresent(v -> r[0] = r[0].withAuth(v));
        return r[0];
    }

    /// Creates a `UrlConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    /// The `user` and `password` optionals must be either both present or both absent.
    ///
    /// @param url The optional URL to override the default; if absent, the default URL is used.
    /// @param user The optional username; if absent, the default user is used.
    /// @param password The optional password; if absent, the default password is used.
    /// @return A new `UrlConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `url`, `user`, or `password` is `null`, or if
    ///         exactly one of `user` and `password` is present.
    @NonNull
    @JsonCreator
    public static UrlConnector create(
            @NonNull Optional<String> url,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password)
    {
        if (user.isPresent() != password.isPresent()) throw new IllegalArgumentException("User and password can't be separated.");
        var auth = user.map(u -> new Auth(u, password.get()));
        return create(url, auth);
    }

    /// Creates a connector with the given URL and no authentication.
    ///
    /// @param url The JDBC connection URL.
    /// @throws IllegalArgumentException If `url` is `null`.
    public UrlConnector(@NonNull String url) {
        List.of(url); // Force lombok put the null-checks before the constructor call.
        this(url, Optional.empty());
    }

    /// Creates a connector with the given URL and the given authentication.
    ///
    /// @param url  The JDBC connection URL.
    /// @param auth The authentication credentials.
    /// @throws IllegalArgumentException If `url` or `auth` is `null`.
    public UrlConnector(@NonNull String url, @NonNull Auth auth) {
        List.of(url, auth); // Force lombok put the null-checks before the constructor call.
        this(url, Optional.of(auth));
    }

    /// Creates a connector with the given URL and username/password credentials.
    ///
    /// @param url The JDBC connection URL.
    /// @param user The database username.
    /// @param password The database password.
    /// @throws IllegalArgumentException If `url`, `user`, or `password` is `null`.
    public UrlConnector(@NonNull String url, @NonNull String user, @NonNull String password) {
        List.of(url, user, password); // Force lombok put the null-checks before the constructor call.
        this(url, Optional.of(new Auth(user, password)));
    }

    @NonNull
    private Connection makeConnection() throws SQLException {
        if (optAuth.isEmpty()) return DriverManager.getConnection(url);
        var a = optAuth.get();
        return DriverManager.getConnection(url, a.user(), a.password());
    }

    /// Opens a new connection using the JDBC URL and credentials of this connector.
    /// The connection has [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE] isolation and autocommit disabled.
    ///
    /// @return A new [Connection] configured for serializable transactions with autocommit disabled.
    /// @throws SQLException If a database access error occurs.
    @NonNull
    @Override
    public Connection get() throws SQLException {
        var con = makeConnection();
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        con.setAutoCommit(false);
        return con;
    }

    /// Returns a copy of this connector with the `url` field replaced by the given value.
    ///
    /// @param url The new JDBC connection URL.
    /// @return A new `UrlConnector` with the updated url.
    /// @throws IllegalArgumentException If `url` is `null`.
    @NonNull
    public UrlConnector withUrl(@NonNull String url) {
        return new UrlConnector(url, optAuth());
    }

    /// Returns a copy of this connector with the authentication replaced by the given username and password.
    ///
    /// @param user The new database username.
    /// @param password The new database password.
    /// @return A new `UrlConnector` with the updated authentication.
    /// @throws IllegalArgumentException If `user` or `password` is `null`.
    @NonNull
    public UrlConnector withAuth(@NonNull String user, @NonNull String password) {
        return withOptAuth(Optional.of(new Auth(user, password)));
    }

    /// Returns a copy of this connector with the authentication replaced by the given [Auth].
    ///
    /// @param auth The new authentication credentials.
    /// @return A new `UrlConnector` with the updated authentication.
    /// @throws IllegalArgumentException If `auth` is `null`.
    @NonNull
    public UrlConnector withAuth(@NonNull Auth auth) {
        return withOptAuth(Optional.of(auth));
    }

    /// Returns a copy of this connector with no authentication credentials.
    ///
    /// @return A new `UrlConnector` with the authentication removed.
    @NonNull
    public UrlConnector withNoAuth() {
        return withOptAuth(Optional.empty());
    }

    /// Returns a copy of this connector with the `optAuth` field replaced by the given value.
    ///
    /// @param optAuth The new optional authentication credentials.
    /// @return A new `UrlConnector` with the updated optional authentication.
    /// @throws IllegalArgumentException If `optAuth` is `null`.
    @NonNull
    public UrlConnector withOptAuth(@NonNull Optional<Auth> optAuth) {
        return new UrlConnector(url(), optAuth);
    }
}
