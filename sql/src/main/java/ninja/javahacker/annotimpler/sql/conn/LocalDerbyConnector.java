package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

@ConnectorJsonKey("local-derby")
public record LocalDerbyConnector(
        @NonNull String directory,
        @NonNull DerbyType subsubprotocol,
        boolean create
) implements Connector.NoAuthConnector
{
    private static final LocalDerbyConnector STD = new LocalDerbyConnector("", DerbyType.DEFAULT, false);

    @NonNull
    public static LocalDerbyConnector std() {
        return STD;
    }

    @NonNull
    @JsonCreator
    public static LocalDerbyConnector create(
            @NonNull Optional<String> directory,
            @NonNull Optional<DerbyType> subsubprotocol,
            @NonNull Optional<Boolean> create)
    {
        var r = new LocalDerbyConnector[] {STD};
        directory.ifPresent(v -> r[0] = r[0].withDirectory(v));
        subsubprotocol.ifPresent(v -> r[0] = r[0].withSubsubprotocol(v));
        create.ifPresent(v -> r[0] = r[0].withCreate(v));
        return r[0];
    }

    @NonNull
    @Override
    public String url() {
        return "jdbc:derby:" + subsubprotocol.part + directory + (create ? "?create=true" : "");
    }

    public static enum DerbyType {
        DEFAULT, DIRECTORY, MEMORY, CLASSPATH, JAR;

        @NonNull
        private final String part;

        private DerbyType() {
            this.part = (ordinal() == 0 ? "" : name().toLowerCase(Locale.ROOT) + ":");
        }
    }

    @NonNull
    public LocalDerbyConnector withDirectory(@NonNull String directory) {
        return new LocalDerbyConnector(directory, subsubprotocol, create);
    }

    @NonNull
    public LocalDerbyConnector withSubsubprotocol(@NonNull DerbyType subsubprotocol) {
        return new LocalDerbyConnector(directory, subsubprotocol, create);
    }

    @NonNull
    public LocalDerbyConnector withCreate(boolean create) {
        return new LocalDerbyConnector(directory, subsubprotocol, create);
    }
}
