package ninja.javahacker.sqlplus.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

@ConnectorJsonKey("mariadb")
public record MariaDbConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database
) implements Connector.MandatoryAuthConnector<MariaDbConnector>, Connector.HostConnector<MariaDbConnector>
{
    public static final int STD_PORT = 3306;

    private static final MariaDbConnector STD = new MariaDbConnector("localhost", STD_PORT, "admin", "admin", "");

    public static MariaDbConnector std() {
        return STD;
    }

    @JsonCreator
    public static MariaDbConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> database)
    {
        var r = new MariaDbConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        database.ifPresent(v -> r[0] = r[0].withDatabase(v));
        return r[0];
    }

    @Override
    public String url() {
        return "jdbc:mariadb://" + host + ":" + port + "/" + database;
    }

    @Override
    public MariaDbConnector withHost(@NonNull String host) {
        return new MariaDbConnector(host, port, user, password, database);
    }

    @Override
    public MariaDbConnector withUser(@NonNull String user) {
        return new MariaDbConnector(host, port, user, password, database);
    }

    @Override
    public MariaDbConnector withPassword(@NonNull String password) {
        return new MariaDbConnector(host, port, user, password, database);
    }

    public MariaDbConnector withDatabase(@NonNull String database) {
        return new MariaDbConnector(host, port, user, password, database);
    }

    @Override
    public MariaDbConnector withPort(int port) {
        return new MariaDbConnector(host, port, user, password, database);
    }
}
