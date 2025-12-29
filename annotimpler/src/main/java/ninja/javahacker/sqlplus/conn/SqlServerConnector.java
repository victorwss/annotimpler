package ninja.javahacker.sqlplus.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

@ConnectorJsonKey("sqlserver")
public record SqlServerConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database
) implements Connector.MandatoryAuthConnector<SqlServerConnector>, Connector.HostConnector<SqlServerConnector>
{
    public static final int STD_PORT = 1433;

    private static final SqlServerConnector STD = new SqlServerConnector("localhost", STD_PORT, "admin", "admin", "");

    public static SqlServerConnector std() {
        return STD;
    }

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

    @Override
    public String url() {
        return "jdbc:hyperion:sqlserver://" + host + ":" + port + ";DatabaseName=" + database;
    }

    @Override
    public SqlServerConnector withHost(@NonNull String host) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    @Override
    public SqlServerConnector withUser(@NonNull String user) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    @Override
    public SqlServerConnector withPassword(@NonNull String password) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    public SqlServerConnector withDatabase(@NonNull String database) {
        return new SqlServerConnector(host, port, user, password, database);
    }

    @Override
    public SqlServerConnector withPort(int port) {
        return new SqlServerConnector(host, port, user, password, database);
    }
}
