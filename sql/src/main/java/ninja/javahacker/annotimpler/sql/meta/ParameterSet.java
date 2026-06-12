package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module ninja.javahacker.annotimpler.magicfactory;

/// Wraps a [java.lang.reflect.Method] together with its compiled parameter-binding strategy.
///
/// An instance of `ParameterSet` is created once per annotated interface method during
/// setup.  It holds the [Method] itself and an internal [ParameterReceiver.NamedAcceptor1]
/// that knows how to bind an argument array to the method's named SQL parameters.
///
/// Use [withValues] at call time to bind a concrete argument array and obtain an
/// [ParameterReceiver.Acceptor2] that can stream all parameter values to a
/// [ParameterReceiver].
///
/// @see ParameterReceiver
/// @see ParsedSqlSupplier#find
public final class ParameterSet {

    @Getter
    @NonNull
    private final Method method;

    @NonNull
    private final ParameterReceiver.NamedAcceptor1 strategy;

    /// Creates a new [ParameterSet] for the given method.
    ///
    /// The constructor analyses the method's parameter annotations to build the
    /// internal binding strategy.
    ///
    /// @param method The method to wrap; must not be `null`.
    /// @throws BadImplementationException If the method parameters cannot be mapped
    ///         to a valid SQL parameter-binding strategy.
    /// @throws IllegalArgumentException If `method` is `null`.
    public ParameterSet(@NonNull Method method) throws BadImplementationException {
        this.method = method;
        this.strategy = ParameterSetStrategy.makeStrategy(method);
    }

    /// Returns a string representation of this [ParameterSet] for debugging.
    ///
    /// @return A non-`null` string of the form `"ParameterSet - <methodName>"`.
    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + methodName();
    }

    @NonNull
    private List<Object> state() {
        return List.of(method);
    }

    /// Returns a hash code derived from the underlying [java.lang.reflect.Method].
    ///
    /// @return A hash code consistent with [equals].
    @Override
    public int hashCode() {
        return state().hashCode();
    }

    /// Returns `true` if `other` is a [ParameterSet] wrapping the same [java.lang.reflect.Method].
    ///
    /// @param other The object to compare against this instance.
    /// @return `true` if `other` is a [ParameterSet] with the same underlying method.
    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof ParameterSet ps && Objects.equals(this.state(), ps.state());
    }

    /// Returns the simplified generic name of the wrapped method.
    ///
    /// The name is produced by [ninja.javahacker.annotimpler.magicfactory.NameDictionary] and is
    /// suitable for use in diagnostic messages.
    ///
    /// @return The simplified generic method name; never `null`.
    @NonNull
    public String methodName() {
        return NameDictionary.global().getSimplifiedGenericString(method, false);
    }

    /// Binds `args` to the method parameters and returns an [ParameterReceiver.Acceptor2].
    ///
    /// The returned [ParameterReceiver.Acceptor2] can subsequently be used to stream
    /// each bound parameter value to a [ParameterReceiver].
    ///
    /// @param args The argument values to bind; must not be `null`.
    /// @return An [ParameterReceiver.Acceptor2] holding the bound parameter values;
    ///         never `null`.
    /// @throws ParameterReceiver.IllegalValueException If any argument value is incompatible
    ///         with the corresponding parameter type.
    /// @throws IllegalArgumentException If `args` is `null`.
    @NonNull
    public ParameterReceiver.Acceptor2 withValues(@NonNull Object... args) throws ParameterReceiver.IllegalValueException {
        return strategy.handle(args);
    }

    /// Returns the ordered list of SQL parameter names derived from the method's parameters.
    ///
    /// The names correspond to the `:name` placeholders that will appear in the SQL string
    /// produced by the associated [SqlFactory].
    ///
    /// @return An unmodifiable list of parameter names in declaration order; never `null`.
    @NonNull
    public List<String> paramNames() {
        return strategy.paramNames();
    }
}
