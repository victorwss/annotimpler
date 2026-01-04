package ninja.javahacker.annotimpler.sql.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

@ConnectorJsonKey("hsqldb")
public record HsqldbConnector(
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename
) implements Connector.MandatoryAuthConnector<HsqldbConnector>
{
    private static final HsqldbConnector STD = new HsqldbConnector("SA", "password", "");

    public static HsqldbConnector std() {
        return STD;
    }

    @JsonCreator
    public static HsqldbConnector create(
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> filename)
    {
        var r = new HsqldbConnector[] {STD};
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        return r[0];
    }

    @Override
    public String url() {
        return "jdbc:hsqldb:file://" + filename;
    }

    @Override
    public HsqldbConnector withUser(@NonNull String user) {
        return new HsqldbConnector(user, password, filename);
    }

    @Override
    public HsqldbConnector withPassword(@NonNull String password) {
        return new HsqldbConnector(user, password, filename);
    }

    public HsqldbConnector withFilename(@NonNull String filename) {
        return new HsqldbConnector(user, password, filename);
    }
}
