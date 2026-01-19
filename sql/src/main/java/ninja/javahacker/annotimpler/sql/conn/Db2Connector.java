package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("db2")
public record Db2Connector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String database
) implements Connector.MandatoryAuthConnector<Db2Connector>, Connector.HostConnector<Db2Connector>
{
    public static final int STD_PORT = 50000;

    private static final Db2Connector STD = new Db2Connector("localhost", STD_PORT, "admin", "admin", "");

    @NonNull
    public static Db2Connector std() {
        return STD;
    }

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

    @NonNull
    @Override
    public String url() {
        return "jdbc:db2://" + host + ":" + port + "/" + database;
    }

    @NonNull
    @Override
    public Db2Connector withHost(@NonNull String host) {
        return new Db2Connector(host, port, user, password, database);
    }

    @NonNull
    @Override
    public Db2Connector withUser(@NonNull String user) {
        return new Db2Connector(host, port, user, password, database);
    }

    @NonNull
    @Override
    public Db2Connector withPassword(@NonNull String password) {
        return new Db2Connector(host, port, user, password, database);
    }

    @NonNull
    public Db2Connector withDatabase(@NonNull String database) {
        return new Db2Connector(host, port, user, password, database);
    }

    @NonNull
    @Override
    public Db2Connector withPort(int port) {
        return new Db2Connector(host, port, user, password, database);
    }
}
