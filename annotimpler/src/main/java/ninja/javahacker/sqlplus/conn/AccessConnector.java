package ninja.javahacker.sqlplus.conn;

import module com.fasterxml.jackson.annotation;
import module java.base;
import lombok.NonNull;

@ConnectorJsonKey("access")
public record AccessConnector(
        @NonNull String filename
) implements Connector.NoAuthConnector
{
    private static final AccessConnector STD = new AccessConnector("");

    public static AccessConnector std() {
        return STD;
    }

    @JsonCreator
    public static AccessConnector create(
            @NonNull Optional<String> filename)
    {
        var r = new AccessConnector[] {STD};
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        return r[0];
    }

    @Override
    public String url() {
        return "jdbc:ucanaccess:" + filename;
    }

    public AccessConnector withFilename(@NonNull String filename) {
        return new AccessConnector(filename);
    }
}
