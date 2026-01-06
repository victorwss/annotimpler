package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;
import module java.sql;

@ConnectorJsonKey("url")
public record UrlConnector(
        @NonNull String url,
        @NonNull Optional<Auth> optAuth
) implements Connector
{
    private static final UrlConnector STD = new UrlConnector("", Optional.empty());

    public static UrlConnector std() {
        return STD;
    }

    @JsonCreator
    public static UrlConnector create(
            @NonNull Optional<String> url)
    {
        var r = new UrlConnector[] {STD};
        url.ifPresent(v -> r[0] = r[0].withUrl(v));
        return r[0];
    }

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

    public UrlConnector(@NonNull String url) {
        List.of(url); // Force lombok put the null-checks before the constructor call.
        this(url, Optional.empty());
    }

    public UrlConnector(@NonNull String url, @NonNull Auth auth) {
        List.of(url, auth); // Force lombok put the null-checks before the constructor call.
        this(url, Optional.of(auth));
    }

    public UrlConnector(@NonNull String url, @NonNull String user, @NonNull String password) {
        List.of(url, user, password); // Force lombok put the null-checks before the constructor call.
        this(url, Optional.of(new Auth(user, password)));
    }

    private Connection makeConnection() throws SQLException {
        if (optAuth.isEmpty()) return DriverManager.getConnection(url);
        var a = optAuth.get();
        return DriverManager.getConnection(url, a.user(), a.password());
    }

    @Override
    public Connection get() throws SQLException {
        var con = makeConnection();
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        con.setAutoCommit(false);
        return con;
    }

    public UrlConnector withUrl(@NonNull String url) {
        return new UrlConnector(url, optAuth());
    }

    public UrlConnector withAuth(@NonNull String user, @NonNull String password) {
        return withOptAuth(Optional.of(new Auth(user, password)));
    }

    public UrlConnector withNoAuth() {
        return withOptAuth(Optional.empty());
    }

    public UrlConnector withAuth(@NonNull Auth auth) {
        return withOptAuth(Optional.of(auth));
    }

    public UrlConnector withOptAuth(@NonNull Optional<Auth> optAuth) {
        return new UrlConnector(url(), optAuth);
    }
}
