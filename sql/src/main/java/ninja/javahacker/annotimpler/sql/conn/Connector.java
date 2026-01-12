package ninja.javahacker.annotimpler.sql.conn;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public interface Connector extends ConnectionFactory {

    @NonNull
    public String url();

    @NonNull
    public Optional<Auth> optAuth();

    @NonNull
    public default Optional<String> optUser() {
        return optAuth().map(Auth::user);
    }

    @NonNull
    public default Optional<String> optPassword() {
        return optAuth().map(Auth::password);
    }

    @NonNull
    public default UrlConnector asUrl() {
        return this instanceof UrlConnector me
                ? me
                : new UrlConnector(url(), optAuth());
    }

    @Override
    public default Connection get() throws SQLException {
        return asUrl().get();
    }

    public static record Auth(@NonNull String user, @NonNull String password) {
    }

    public static interface HostConnector<THIS extends HostConnector<THIS>> extends Connector {

        @NonNull
        public String host();

        @NonNull
        public int port();

        @NonNull
        public THIS withHost(@NonNull String host);

        @NonNull
        public THIS withPort(int port);
    }

    public static interface MandatoryAuthConnector<THIS extends MandatoryAuthConnector<THIS>> extends Connector {

        @NonNull
        public String user();

        @NonNull
        public String password();

        @NonNull
        public default Auth auth() {
            return new Auth(user(), password());
        }

        @Override
        @NonNull
        public default Optional<Auth> optAuth() {
            return Optional.of(auth());
        }

        @NonNull
        public default THIS withAuth(@NonNull Auth auth) {
            return withUser(auth.user()).withPassword(auth.password());
        }

        @NonNull
        public default THIS withAuth(@NonNull String user, @NonNull String password) {
            return withUser(user).withPassword(password);
        }

        @NonNull
        public THIS withUser(@NonNull String username);

        @NonNull
        public THIS withPassword(@NonNull String password);
    }

    public static interface NoAuthConnector extends Connector {

        @NonNull
        @Override
        public default Optional<Auth> optAuth() {
            return Optional.empty();
        }
    }
}
