package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("h2")
public record H2Connector(
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename,
        boolean memory
) implements Connector.MandatoryAuthConnector<H2Connector>
{
    private static final H2Connector STD = new H2Connector("sa", "password", "", false);

    @NonNull
    public static H2Connector std() {
        return STD;
    }

    @NonNull
    @JsonCreator
    public static H2Connector create(
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> filename,
            @NonNull Optional<Boolean> memory)
    {
        var r = new H2Connector[] {STD};
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        memory.ifPresent(v -> r[0] = r[0].withMemory(v));
        return r[0];
    }

    @NonNull
    @Override
    public String url() {
        return "jdbc:h2:" + (memory ? "mem:" : "~/") + filename;
    }

    @NonNull
    @Override
    public H2Connector withUser(@NonNull String user) {
        return new H2Connector(user, password, filename, memory);
    }

    @NonNull
    @Override
    public H2Connector withPassword(@NonNull String password) {
        return new H2Connector(user, password, filename, memory);
    }

    @NonNull
    public H2Connector withFilename(@NonNull String filename) {
        return new H2Connector(user, password, filename, memory);
    }

    @NonNull
    public H2Connector withMemory(boolean memory) {
        return new H2Connector(user, password, filename, memory);
    }
}
