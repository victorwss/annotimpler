package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for Microsoft Access databases.
///
/// @param filename The path to the Microsoft Access database file.
@ConnectorJsonKey("access")
public record AccessConnector(
        @NonNull String filename
) implements Connector.NoAuthConnector
{
    /// Creates an `AccessConnector` with the given database file path.
    ///
    /// @param filename The path to the Microsoft Access database file.
    /// @throws IllegalArgumentException If `filename` is `null`.
    public AccessConnector {}

    private static final AccessConnector STD = new AccessConnector("");

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `AccessConnector` instance.
    @NonNull
    public static AccessConnector std() {
        return STD;
    }

    /// Creates a `AccessConnector` from an optional filename value, applying the present value over the default
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param filename The optional database file path to override the default; if absent, the default path is used.
    /// @return A new `AccessConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `filename` is `null`.
    @NonNull
    @JsonCreator
    public static AccessConnector create(
            @NonNull Optional<String> filename)
    {
        var r = new AccessConnector[] {STD};
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The non-null JDBC URL string.
    @NonNull
    @Override
    public String url() {
        return "jdbc:ucanaccess:" + filename;
    }

    /// Returns a copy of this connector with the `filename` field replaced by the given value.
    ///
    /// @param filename The new path to the Microsoft Access database file.
    /// @return A new `AccessConnector` with the updated filename.
    /// @throws IllegalArgumentException If `filename` is `null`.
    @NonNull
    public AccessConnector withFilename(@NonNull String filename) {
        return new AccessConnector(filename);
    }
}
