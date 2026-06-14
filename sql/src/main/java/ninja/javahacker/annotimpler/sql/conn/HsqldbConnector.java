package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module com.fasterxml.jackson.annotation;
import module java.base;

/// Immutable JDBC connector for HSQLDB databases.
///
/// @param user The database username.
/// @param password The database password.
/// @param filename The database file path, used when `memory` is `false`.
/// @param memory Whether to use an in-memory database (`true`) or a file-based database (`false`).
@ConnectorJsonKey("hsqldb")
public record HsqldbConnector(
        @NonNull String user,
        @NonNull String password,
        @NonNull String filename,
        boolean memory
) implements Connector.MandatoryAuthConnector<HsqldbConnector>
{
    /// Creates an `HsqldbConnector` with the given connection parameters.
    ///
    /// @param user The database username.
    /// @param password The database password.
    /// @param filename The database file path.
    /// @param memory Whether to use an in-memory database.
    /// @throws IllegalArgumentException If `user`, `password`, or `filename` is `null`.
    public HsqldbConnector {}

    private static final HsqldbConnector STD = new HsqldbConnector("SA", "password", "", false);

    /// Returns the standard pre-configured instance with default values suitable for local development.
    ///
    /// @return The standard `HsqldbConnector` instance.
    @NonNull
    public static HsqldbConnector std() {
        return STD;
    }

    /// Creates a `HsqldbConnector` from optional field values, applying each present value over the defaults
    /// returned by [#std()]. Any absent optional keeps the corresponding default.
    ///
    /// @param user The optional username to override the default; if absent, the default username is used.
    /// @param password The optional password to override the default; if absent, the default password is used.
    /// @param filename The optional database file path to override the default; if absent, the default path is used.
    /// @param memory The optional memory flag to override the default; if absent, the default flag is used.
    /// @return A new `HsqldbConnector` with the applied overrides.
    /// @throws IllegalArgumentException If `user`, `password`, `filename`, or `memory` is `null`.
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

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The JDBC URL string; never `null`.
    @NonNull
    @Override
    public String url() {
        return "jdbc:hsqldb:" + (memory ? "mem:" : "file://") + filename;
    }

    /// Returns a copy of this connector with the `user` field replaced by the given value.
    ///
    /// @param user The new database username.
    /// @return A new `HsqldbConnector` with the updated user.
    /// @throws IllegalArgumentException If `user` is `null`.
    @NonNull
    @Override
    public HsqldbConnector withUser(@NonNull String user) {
        return new HsqldbConnector(user, password, filename, memory);
    }

    /// Returns a copy of this connector with the `password` field replaced by the given value.
    ///
    /// @param password The new database password.
    /// @return A new `HsqldbConnector` with the updated password.
    /// @throws IllegalArgumentException If `password` is `null`.
    @NonNull
    @Override
    public HsqldbConnector withPassword(@NonNull String password) {
        return new HsqldbConnector(user, password, filename, memory);
    }

    /// Returns a copy of this connector with the `filename` field replaced by the given value.
    ///
    /// @param filename The new database file path.
    /// @return A new `HsqldbConnector` with the updated filename.
    /// @throws IllegalArgumentException If `filename` is `null`.
    @NonNull
    public HsqldbConnector withFilename(@NonNull String filename) {
        return new HsqldbConnector(user, password, filename, memory);
    }

    /// Returns a copy of this connector with the `memory` field replaced by the given value.
    ///
    /// @param memory `true` for an in-memory database, `false` for a file-based database.
    /// @return A new `HsqldbConnector` with the updated memory flag.
    @NonNull
    public HsqldbConnector withMemory(boolean memory) {
        return new HsqldbConnector(user, password, filename, memory);
    }
}
