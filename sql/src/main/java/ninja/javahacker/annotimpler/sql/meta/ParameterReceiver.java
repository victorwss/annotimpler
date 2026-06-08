package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module java.sql;

public interface ParameterReceiver {

    public void receiveNull(String name, Class<?> type) throws SQLException;

    public void receive(String name, Object value) throws SQLException;

    @FunctionalInterface
    public static interface Acceptor2 {
        public void accept(@NonNull ParameterReceiver pr) throws SQLException;
    }

    public static interface Acceptor1 {
        public Acceptor2 handle(@Nullable Object value) throws IllegalValueException;
    }

    public static interface NamedAcceptor1 extends Acceptor1 {
        public List<String> paramNames();
    }

    public static class IllegalValueException extends Exception {
        private static final long serialVersionUID = 1L;

        public IllegalValueException() {
        }
    }

    public static Acceptor1 forMethod(@NonNull Method method) {
        return ParameterSetStrategy.makeStrategy(method);
    }
}
