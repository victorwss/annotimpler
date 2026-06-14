package ninja.javahacker.annotimpler.sql.conn;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module com.fasterxml.jackson.annotation;

/// Singleton connector for an in-memory SQLite database.
/// Use [#STD] or [#std()] to obtain the sole instance.
@ConnectorJsonKey("sqlite-memory")
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum SqliteMemoryConnector implements Connector.NoAuthConnector {

    /// The sole instance of this connector.
    STD;

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `SqliteMemoryConnector` instance.
    @NonNull
    public static SqliteMemoryConnector std() {
        return STD;
    }

    /// Returns the sole instance of this connector.
    ///
    /// @return The [#STD] instance.
    @NonNull
    @JsonCreator
    public static SqliteMemoryConnector create() {
        return STD;
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The JDBC URL string; never `null`.
    @NonNull
    @Override
    public String url() {
        return "jdbc:sqlite::memory:";
    }
}
