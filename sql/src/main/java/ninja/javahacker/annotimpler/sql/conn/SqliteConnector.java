package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("sqlite")
public record SqliteConnector(
        @NonNull String filename
) implements Connector.NoAuthConnector
{
    private static final SqliteConnector STD = new SqliteConnector("");

    @NonNull
    public static SqliteConnector std() {
        return STD;
    }

    @NonNull
    @JsonCreator
    public static SqliteConnector create(
            @NonNull Optional<String> filename)
    {
        var r = new SqliteConnector[] {STD};
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        return r[0];
    }

    @NonNull
    @Override
    public String url() {
        return "jdbc:sqlite:" + filename;
    }

    @NonNull
    public SqliteConnector withFilename(@NonNull String filename) {
        return new SqliteConnector(filename);
    }
}
