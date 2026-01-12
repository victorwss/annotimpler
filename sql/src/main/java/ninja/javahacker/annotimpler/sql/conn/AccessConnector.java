package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("access")
public record AccessConnector(
        @NonNull String filename
) implements Connector.NoAuthConnector
{
    private static final AccessConnector STD = new AccessConnector("");

    @NonNull
    public static AccessConnector std() {
        return STD;
    }

    @NonNull
    @JsonCreator
    public static AccessConnector create(
            @NonNull Optional<String> filename)
    {
        var r = new AccessConnector[] {STD};
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        return r[0];
    }

    @NonNull
    @Override
    public String url() {
        return "jdbc:ucanaccess:" + filename;
    }

    @NonNull
    public AccessConnector withFilename(@NonNull String filename) {
        return new AccessConnector(filename);
    }
}
