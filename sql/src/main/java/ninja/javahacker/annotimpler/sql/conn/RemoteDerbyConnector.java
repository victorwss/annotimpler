package ninja.javahacker.annotimpler.sql.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

@ConnectorJsonKey("remote-derby")
public record RemoteDerbyConnector(
        @NonNull String host,
        int port,
        @NonNull String directory,
        boolean create
) implements Connector.NoAuthConnector, Connector.HostConnector<RemoteDerbyConnector>
{
    public static final int STD_PORT = 1527;

    private static final RemoteDerbyConnector STD = new RemoteDerbyConnector("localhost", STD_PORT, "", false);

    public static RemoteDerbyConnector std() {
        return STD;
    }

    @JsonCreator
    public static RemoteDerbyConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> directory,
            @NonNull Optional<Boolean> create)
    {
        var r = new RemoteDerbyConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        directory.ifPresent(v -> r[0] = r[0].withDirectory(v));
        create.ifPresent(v -> r[0] = r[0].withCreate(v));
        return r[0];
    }

    @Override
    public String url() {
        return "jdbc:derby://" + host + ":" + port + "/" + directory + (create ? "?create=true" : "");
    }

    @Override
    public RemoteDerbyConnector withHost(@NonNull String host) {
        return new RemoteDerbyConnector(host, port, directory, create);
    }

    @Override
    public RemoteDerbyConnector withPort(int port) {
        return new RemoteDerbyConnector(host, port, directory, create);
    }

    public RemoteDerbyConnector withDirectory(@NonNull String directory) {
        return new RemoteDerbyConnector(host, port, directory, create);
    }

    public RemoteDerbyConnector withCreate(boolean create) {
        return new RemoteDerbyConnector(host, port, directory, create);
    }
}
