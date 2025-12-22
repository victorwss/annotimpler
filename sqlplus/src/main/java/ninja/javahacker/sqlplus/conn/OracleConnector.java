package ninja.javahacker.sqlplus.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

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
    public static final int STD_PORT = 1521;

    private static final OracleConnector STD = new OracleConnector("localhost", STD_PORT, "admin", "admin", "", false);

    public static OracleConnector std() {
        return STD;
    }

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

    @Override
    public String url() {
        return rac
                ? "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database
                : "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
    }

    @Override
    public OracleConnector withHost(@NonNull String host) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    @Override
    public OracleConnector withUser(@NonNull String user) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    @Override
    public OracleConnector withPassword(@NonNull String password) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    public OracleConnector withDatabase(@NonNull String database) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    @Override
    public OracleConnector withPort(int port) {
        return new OracleConnector(host, port, user, password, database, rac);
    }

    public OracleConnector withRac(boolean rac) {
        return new OracleConnector(host, port, user, password, database, rac);
    }
}
