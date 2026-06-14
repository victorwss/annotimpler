package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// Base interface for JDBC connection factories that expose a URL and optional authentication
/// credentials.
///
/// Connections produced by [#get()] have
/// [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE] isolation and autocommit disabled.
/// All connector implementations are immutable — every `withXxx` method returns a new
/// instance rather than mutating the receiver.
///
/// Three sub-interfaces capture the common structural patterns found across database connectors:
/// - [HostConnector] for engines addressed by a hostname and port,
/// - [MandatoryAuthConnector] for engines that always require credentials, and
/// - [NoAuthConnector] for engines that require no authentication.
public interface Connector extends ConnectionFactory {

    /// Returns the JDBC connection URL for this connector.
    ///
    /// @return The JDBC URL string; never `null`.
    @NonNull
    public String url();

    /// Returns the authentication credentials for this connector.
    ///
    /// @return An [Optional] containing the [Auth] credentials, or an empty optional
    ///         if no authentication is required.
    @NonNull
    public Optional<Auth> optAuth();

    /// Returns the database username, if any.
    ///
    /// @return An [Optional] containing the username, or an empty optional if no
    ///         authentication credentials are configured.
    @NonNull
    public default Optional<String> optUser() {
        return optAuth().map(Auth::user);
    }

    /// Returns the database password, if any.
    ///
    /// @return An [Optional] containing the password, or an empty optional if no
    ///         authentication credentials are configured.
    @NonNull
    public default Optional<String> optPassword() {
        return optAuth().map(Auth::password);
    }

    /// Returns a [UrlConnector] equivalent to this connector.
    ///
    /// If this connector is already a `UrlConnector`, it is returned unchanged.
    /// Otherwise, a new `UrlConnector` is constructed from the URL and credentials
    /// exposed by this connector.
    ///
    /// @return An [UrlConnector] representing this connector; never `null`.
    @NonNull
    public default UrlConnector asUrl() {
        return this instanceof UrlConnector me
                ? me
                : new UrlConnector(url(), optAuth());
    }

    /// Opens a new [Connection] using this connector.
    ///
    /// The returned connection has [Connection#TRANSACTION_SERIALIZABLE SERIALIZABLE]
    /// isolation level and autocommit disabled.
    ///
    /// @return A new and open database connection; never `null`.
    /// @throws SQLException If a database access error occurs or the connection URL is invalid.
    @Override
    public default Connection get() throws SQLException {
        return asUrl().get();
    }

    /// Holds the authentication credentials required to open a database connection.
    ///
    /// @param user     The database username.
    /// @param password The database password.
    public static record Auth(@NonNull String user, @NonNull String password) {

        /// Creates an `Auth` record with the given credentials.
        ///
        /// @param user The database username.
        /// @param password The database password.
        /// @throws IllegalArgumentException If `user` or `password` is `null`.
        public Auth {}
    }

    /// Extension of [Connector] for database engines that are addressed by a hostname
    /// and a port number.
    ///
    /// @param <THIS> The concrete connector type returned by the fluent `withXxx` methods.
    public static interface HostConnector<THIS extends HostConnector<THIS>> extends Connector {

        /// Returns the database server hostname or IP address.
        ///
        /// @return The hostname string; never `null`.
        @NonNull
        public String host();

        /// Returns the TCP port number on which the database server listens.
        ///
        /// @return The port number.
        @NonNull
        public int port();

        /// Returns a copy of this connector with the host field replaced by the given value.
        ///
        /// @param host The new database server hostname or IP address.
        /// @return A new connector of the same concrete type with the updated host.
        /// @throws IllegalArgumentException If `host` is `null`.
        @NonNull
        public THIS withHost(@NonNull String host);

        /// Returns a copy of this connector with the port field replaced by the given value.
        ///
        /// @param port The new TCP port number.
        /// @return A new connector of the same concrete type with the updated port.
        @NonNull
        public THIS withPort(int port);
    }

    /// Extension of [Connector] for database engines that always require a username
    /// and password.
    ///
    /// @param <THIS> The concrete connector type returned by the fluent `withXxx` methods.
    public static interface MandatoryAuthConnector<THIS extends MandatoryAuthConnector<THIS>> extends Connector {

        /// Returns the database username.
        ///
        /// @return The username string; never `null`.
        @NonNull
        public String user();

        /// Returns the database password.
        ///
        /// @return The password string; never `null`.
        @NonNull
        public String password();

        /// Returns the authentication credentials as an [Auth] object.
        ///
        /// @return An [Auth] containing the current username and password; never `null`.
        @NonNull
        public default Auth auth() {
            return new Auth(user(), password());
        }

        /// Returns an [Optional] always containing the authentication credentials of this connector.
        ///
        /// @return A non-empty [Optional] wrapping the result of [#auth()]; never `null`.
        @Override
        @NonNull
        public default Optional<Auth> optAuth() {
            return Optional.of(auth());
        }

        /// Returns a copy of this connector with the authentication credentials replaced by
        /// those in the given [Auth] object.
        ///
        /// @param auth The new authentication credentials.
        /// @return A new connector of the same concrete type with the updated credentials.
        /// @throws IllegalArgumentException If `auth` is `null`.
        @NonNull
        public default THIS withAuth(@NonNull Auth auth) {
            return withUser(auth.user()).withPassword(auth.password());
        }

        /// Returns a copy of this connector with the authentication credentials replaced by
        /// the given username and password.
        ///
        /// @param user The new database username.
        /// @param password The new database password.
        /// @return A new connector of the same concrete type with the updated credentials.
        /// @throws IllegalArgumentException If `user` or `password` is `null`.
        @NonNull
        public default THIS withAuth(@NonNull String user, @NonNull String password) {
            return withUser(user).withPassword(password);
        }

        /// Returns a copy of this connector with the username replaced by the given value.
        ///
        /// @param username The new database username.
        /// @return A new connector of the same concrete type with the updated username.
        /// @throws IllegalArgumentException If `username` is `null`.
        @NonNull
        public THIS withUser(@NonNull String username);

        /// Returns a copy of this connector with the password replaced by the given value.
        ///
        /// @param password The new database password.
        /// @return A new connector of the same concrete type with the updated password.
        /// @throws IllegalArgumentException If `password` is `null`.
        @NonNull
        public THIS withPassword(@NonNull String password);
    }

    /// Extension of [Connector] for database engines that do not require authentication,
    /// such as file-based or in-memory databases.
    ///
    /// The [#optAuth()] method always returns an empty optional.
    public static interface NoAuthConnector extends Connector {

        /// Returns an empty optional, since connectors of this type require no authentication.
        ///
        /// @return An always-empty [Optional].
        @NonNull
        @Override
        public default Optional<Auth> optAuth() {
            return Optional.empty();
        }
    }
}
