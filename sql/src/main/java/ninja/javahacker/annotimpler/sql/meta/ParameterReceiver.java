package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.core;

/// A sink that accepts named SQL parameter values to be set on a prepared statement.
///
/// Implementations receive individual parameter values by name. There are three flavours
/// of delivery: a SQL NULL without a type hint ([receiveNull(String)]), a SQL NULL with a
/// type hint ([receiveNull(String, Class)]), and a non-null value ([receive(String, Object)]).
///
/// The nested [Acceptor2] functional interface represents a bound set of parameter values
/// that can be streamed to any `ParameterReceiver`. [Acceptor1] and [NamedAcceptor1]
/// describe factories that produce [Acceptor2] instances from a single argument value or
/// argument array.
///
/// @see ParameterSet
/// @see ParsedSqlSupplier
public interface ParameterReceiver {

    /// Sends a SQL NULL for the named parameter without a SQL-type hint.
    ///
    /// Delegates to [receiveNull(String, Class)] with `void.class` as the type.
    ///
    /// @param name The parameter name; must not be `null`.
    /// @throws SQLException If the null value cannot be set on the prepared statement.
    /// @throws IllegalArgumentException If `name` is `null`.
    public default void receiveNull(@NonNull String name) throws SQLException {
        receiveNull(name, void.class);
    }

    /// Sends a SQL NULL for the named parameter, using `type` as a SQL-type hint.
    ///
    /// The `type` argument allows the underlying JDBC driver to choose an appropriate
    /// SQL type when setting the null value on the prepared statement.
    /// Pass `void.class` when no type hint is available.
    ///
    /// @param name The parameter name; must not be `null`.
    /// @param type A Java type used as a SQL-type hint; must not be `null`.
    /// @throws SQLException If the null value cannot be set on the prepared statement.
    /// @throws IllegalArgumentException If `name` or `type` is `null`.
    public void receiveNull(@NonNull String name, @NonNull Class<?> type) throws SQLException;

    /// Sends a non-null value for the named parameter.
    ///
    /// @param name The parameter name; must not be `null`.
    /// @param value The parameter value; must not be `null`.
    /// @throws SQLException If the value cannot be set on the prepared statement.
    /// @throws IllegalArgumentException If `name` or `value` is `null`.
    public void receive(@NonNull String name, @NonNull Object value) throws SQLException;

    /// A functional interface that accepts a [ParameterReceiver] and streams all bound
    /// parameter values to it.
    ///
    /// An `Acceptor2` is created by [Acceptor1#handle] and consumed by the SQL execution layer.
    @FunctionalInterface
    public static interface Acceptor2 {

        /// Streams all bound parameter values to the given [ParameterReceiver].
        ///
        /// @param pr The receiver to which parameter values will be sent; must not be `null`.
        /// @throws SQLException If any parameter value cannot be set on the prepared statement.
        /// @throws IllegalArgumentException If `pr` is `null`.
        public void accept(@NonNull ParameterReceiver pr) throws SQLException;
    }

    /// A factory that produces an [Acceptor2] from a single argument value (or argument array).
    ///
    /// Implementations bind the supplied value to one or more named SQL parameters and return
    /// an [Acceptor2] that can stream those bindings to a [ParameterReceiver].
    public static interface Acceptor1 {

        /// Binds `value` to the parameter(s) managed by this acceptor and returns an [Acceptor2].
        ///
        /// @param value The argument value to bind; may be `null`.
        /// @return An [Acceptor2] holding the bound parameter values; never `null`.
        /// @throws IllegalValueException If `value` is incompatible with the expected parameter type.
        public Acceptor2 handle(@Nullable Object value) throws IllegalValueException;
    }

    /// An [Acceptor1] that additionally exposes the ordered list of SQL parameter names
    /// it manages.
    ///
    /// Implementations are produced by [ParameterReceiver#forMethod] and are used by
    /// [ParameterSet] to support [ParameterSet#paramNames].
    public static interface NamedAcceptor1 extends Acceptor1 {

        /// Returns the ordered list of SQL parameter names managed by this acceptor.
        ///
        /// @return An unmodifiable list of parameter names in declaration order; never `null`.
        public List<String> paramNames();
    }

    /// Thrown by [Acceptor1#handle] when the supplied value is not compatible with the
    /// expected parameter type.
    public static class IllegalValueException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a new [IllegalValueException] with no detail message.
        public IllegalValueException() {
        }
    }

    /// Creates a [NamedAcceptor1] that handles all parameters of the given method.
    ///
    /// The returned acceptor introspects the method's parameters and their annotations
    /// to build a complete binding strategy.
    ///
    /// @param method The method whose parameters will be managed; must not be `null`.
    /// @return A [NamedAcceptor1] for `method`; never `null`.
    /// @throws BadImplementationException If the method parameters cannot be mapped
    ///         to a valid SQL parameter-binding strategy.
    /// @throws IllegalArgumentException If `method` is `null`.
    public static Acceptor1 forMethod(@NonNull Method method) throws BadImplementationException {
        return ParameterSetStrategy.makeStrategy(method);
    }
}
