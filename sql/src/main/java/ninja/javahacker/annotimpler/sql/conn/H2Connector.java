package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for H2 databases.
///
/// @param user The database username.
/// @param password The database password.
/// @param filename The name of the database file, used as the database identifier.
/// @param memory Whether to use an in-memory database (`true`) or a file-based database at `~/filename` (`false`).
/// @param timezone The timezone identifier appended to the URL as `;TIME ZONE=...`, or an empty string for no timezone override.
@ConnectorJsonKey("h2")
public record H2Connector(
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename,
        boolean memory,
        @NonNull String timezone
) implements Connector.MandatoryAuthConnector<H2Connector>
{

    /// Standard partially configured instance filled with default values to act as the base of a builder.
    private static final H2Connector STD = new H2Connector("sa", "password", "", false, "");

    /// Creates an `H2Connector` with the given connection parameters.
    ///
    /// @param user The database username.
    /// @param password The database password.
    /// @param filename The name of the database file.
    /// @param memory Whether to use an in-memory database.
    /// @param timezone The timezone identifier, or an empty string for no timezone override.
    /// @throws IllegalArgumentException If `user`, `password`, `filename`, or `timezone` is `null`.
    public H2Connector {}

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `H2Connector` instance.
    @NonNull
    public static H2Connector std() {
        return STD;
    }

    /// Creates a `H2Connector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param user The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param filename The optional database filename to override the default; if absent, the default filename is used.
    /// @param memory The optional memory flag to override the default; if absent, the default flag is used.
    /// @param timezone The optional timezone to override the default; if absent, the default timezone is used.
    /// @return A new `H2Connector` with the applied overrides.
    /// @throws IllegalArgumentException If `user`, `password`, `filename`, `memory`, or `timezone` is `null`.
    @NonNull
    @JsonCreator
    public static H2Connector create(
            @NonNull Optional<String> user,
            @NonNull Optional<String> password,
            @NonNull Optional<String> filename,
            @NonNull Optional<Boolean> memory,
            @NonNull Optional<String> timezone)
    {
        var r = new H2Connector[] {STD};
        user.ifPresent(v -> r[0] = r[0].withUser(v));
        password.ifPresent(v -> r[0] = r[0].withPassword(v));
        filename.ifPresent(v -> r[0] = r[0].withFilename(v));
        memory.ifPresent(v -> r[0] = r[0].withMemory(v));
        timezone.ifPresent(v -> r[0] = r[0].withTimezone(v));
        return r[0];
    }

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The JDBC URL string; never `null`.
    @NonNull
    @Override
    public String url() {
        return "jdbc:h2:" + (memory ? "mem:" : "~/") + filename + (timezone.isEmpty() ? "" : ";TIME ZONE=" + timezone);
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `H2Connector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public H2Connector withUser(@NonNull String user) {
        return new H2Connector(user, password, filename, memory, timezone);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `H2Connector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public H2Connector withPassword(@NonNull String password) {
        return new H2Connector(user, password, filename, memory, timezone);
    }

    /// Returns a copy of this connector with the `filename` field replaced by the given value.
    ///
    /// @param filename The new database filename.
    /// @return A new `H2Connector` with the updated filename.
    /// @throws IllegalArgumentException If `filename` is `null`.
    @NonNull
    public H2Connector withFilename(@NonNull String filename) {
        return new H2Connector(user, password, filename, memory, timezone);
    }

    /// Returns a copy of this connector with the `memory` field replaced by the given value.
    ///
    /// @param memory `true` for an in-memory database, `false` for a file-based database.
    /// @return A new `H2Connector` with the updated memory flag.
    @NonNull
    public H2Connector withMemory(boolean memory) {
        return new H2Connector(user, password, filename, memory, timezone);
    }

    /// Returns a copy of this connector with the `timezone` field replaced by the given value.
    ///
    /// @param timezone The new timezone identifier, or an empty string for no timezone override.
    /// @return A new `H2Connector` with the updated timezone.
    /// @throws IllegalArgumentException If `timezone` is `null`.
    @NonNull
    public H2Connector withTimezone(@NonNull String timezone) {
        return new H2Connector(user, password, filename, memory, timezone);
    }
}
