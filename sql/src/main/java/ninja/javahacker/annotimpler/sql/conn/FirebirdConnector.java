package ninja.javahacker.annotimpler.sql.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

@ConnectorJsonKey("firebird")
public record FirebirdConnector(
        @NonNull String host,
        int port,
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename,
        @NonNull String encoding
) implements Connector.MandatoryAuthConnector<FirebirdConnector>, Connector.HostConnector<FirebirdConnector>
{
    public static final int STD_PORT = 3050;

    private static final FirebirdConnector STD = new FirebirdConnector("localhost", STD_PORT, "SYSDBA", "masterkey", "", "UTF8");

    public static FirebirdConnector std() {
        return STD;
    }

    @JsonCreator
    public static FirebirdConnector create(
            @NonNull Optional<String> host,
            @NonNull OptionalInt port,
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> filename,
            @NonNull Optional<String> encoding)
    {
        var r = new FirebirdConnector[] {STD};
        host.ifPresent(v -> r[0] = r[0].withHost(v));
        port.ifPresent(v -> r[0] = r[0].withPort(v));
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        encoding.ifPresent(v -> r[0] = r[0].withEncoding(v));
        return r[0];
    }

    @Override
    public String url() {
        return "jdbc:firebird://" + host + ":" + port + "/" + filename + (encoding.isEmpty() ? "" : "?encoding=" + encoding);
    }

    @Override
    public FirebirdConnector withHost(@NonNull String host) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    @Override
    public FirebirdConnector withUser(@NonNull String user) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    @Override
    public FirebirdConnector withPassword(@NonNull String password) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    public FirebirdConnector withFilename(@NonNull String filename) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    @Override
    public FirebirdConnector withPort(int port) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }

    public FirebirdConnector withEncoding(@NonNull String encoding) {
        return new FirebirdConnector(host, port, user, password, filename, encoding);
    }
}
