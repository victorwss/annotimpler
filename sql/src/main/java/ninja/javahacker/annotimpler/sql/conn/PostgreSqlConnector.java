package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

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
    public static final int STD_PORT = 5432;

    private static final PostgreSqlConnector STD = new PostgreSqlConnector("localhost", STD_PORT, "admin", "admin", "", true);

    public static PostgreSqlConnector std() {
        return STD;
    }

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

    @Override
    public String url() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database + (ssl ? "?ssl=true" : "");
    }

    @Override
    public PostgreSqlConnector withHost(@NonNull String host) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    @Override
    public PostgreSqlConnector withUser(@NonNull String user) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    @Override
    public PostgreSqlConnector withPassword(@NonNull String password) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    public PostgreSqlConnector withDatabase(@NonNull String database) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    @Override
    public PostgreSqlConnector withPort(int port) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }

    public PostgreSqlConnector withSsl(boolean ssl) {
        return new PostgreSqlConnector(host, port, user, password, database, ssl);
    }
}
