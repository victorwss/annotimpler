package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("hsqldb")
public record HsqldbConnector(
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename,
        boolean memory
) implements Connector.MandatoryAuthConnector<HsqldbConnector>
{
    private static final HsqldbConnector STD = new HsqldbConnector("SA", "password", "", false);

    @NonNull
    public static HsqldbConnector std() {
        return STD;
    }

    @NonNull
    @JsonCreator
    public static HsqldbConnector create(
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> filename,
            @NonNull Optional<Boolean> memory)
    {
        var r = new HsqldbConnector[] {STD};
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        memory.ifPresent(v -> r[0] = r[0].withMemory(v));
        return r[0];
    }

    @NonNull
    @Override
    public String url() {
        return "jdbc:hsqldb:" + (memory ? "mem:" : "file://") + filename;
    }

    @NonNull
    @Override
    public HsqldbConnector withUser(@NonNull String user) {
        return new HsqldbConnector(user, password, filename, memory);
    }

    @NonNull
    @Override
    public HsqldbConnector withPassword(@NonNull String password) {
        return new HsqldbConnector(user, password, filename, memory);
    }

    @NonNull
    public HsqldbConnector withFilename(@NonNull String filename) {
        return new HsqldbConnector(user, password, filename, memory);
    }

    @NonNull
    public HsqldbConnector withMemory(boolean memory) {
        return new HsqldbConnector(user, password, filename, memory);
    }
}
