package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;

@ConnectorJsonKey("sqlite-memory")
public enum SqliteMemoryConnector implements Connector.NoAuthConnector {
    STD;

    @NonNull
    public static SqliteMemoryConnector std() {
        return STD;
    }

    @NonNull
    @JsonCreator
    public static SqliteMemoryConnector create() {
        return STD;
    }

    @NonNull
    @Override
    public String url() {
        return "jdbc:sqlite::memory:";
    }
}
