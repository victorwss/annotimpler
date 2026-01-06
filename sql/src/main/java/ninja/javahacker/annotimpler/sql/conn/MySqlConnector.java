package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("mysql")
public record MySqlConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database
) implements Connector.MandatoryAuthConnector<MySqlConnector>, Connector.HostConnector<MySqlConnector>
{
    public static final int STD_PORT = 3306;

    private static final MySqlConnector STD = new MySqlConnector("localhost", STD_PORT, "admin", "admin", "");

    public static MySqlConnector std() {
        return STD;
    }

    @JsonCreator
    public static MySqlConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> database)
    {
        var r = new MySqlConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        database.ifPresent(v -> r[0] = r[0].withDatabase(v));
        return r[0];
    }

    @Override
    public String url() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }

    @Override
    public MySqlConnector withHost(@NonNull String host) {
        return new MySqlConnector(host, port, user, password, database);
    }

    @Override
    public MySqlConnector withUser(@NonNull String user) {
        return new MySqlConnector(host, port, user, password, database);
    }

    @Override
    public MySqlConnector withPassword(@NonNull String password) {
        return new MySqlConnector(host, port, user, password, database);
    }

    public MySqlConnector withDatabase(@NonNull String database) {
        return new MySqlConnector(host, port, user, password, database);
    }

    @Override
    public MySqlConnector withPort(int port) {
        return new MySqlConnector(host, port, user, password, database);
    }
}
