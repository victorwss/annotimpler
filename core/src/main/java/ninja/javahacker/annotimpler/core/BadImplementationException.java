package ninja.javahacker.annotimpler.core;

import lombok.NonNull;

import module java.base;

/// Signals that an interface method could not be implemented by [AnnotationsImplementor].
///
/// This exception is thrown when a method carries more than one implementation annotation,
/// when it has no such annotation and no `default` implementation, when the designated
/// [Implementation] class cannot be instantiated or used with no arguments, or when
/// [Implementation#prepare] returns `null`.
/// Custom [Implementation] classes may also throw it as part of their own validation.
public class BadImplementationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Type root;

    /// Creates a new exception with the given detail message and root type.
    ///
    /// @param message the detail message; must not be null
    /// @param root the type (usually the interface or the declaring class of the method)
    ///             where the failure originated; must not be null
    /// @throws IllegalArgumentException if `message` or `root` is null
    public BadImplementationException(@NonNull String message, @NonNull Type root) {
        List.of(message, root); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.root = root;
    }

    /// Creates a new exception with the given detail message, cause, and root type.
    ///
    /// @param message the detail message; must not be null
    /// @param cause the exception that triggered this failure; must not be null
    /// @param root the type (usually the interface or the declaring class of the method)
    ///             where the failure originated; must not be null
    /// @throws IllegalArgumentException if `message`, `cause`, or `root` is null
    public BadImplementationException(@NonNull String message, @NonNull Throwable cause, @NonNull Type root) {
        List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
        super(message, cause);
        this.root = root;
    }

    /// Returns the type where the implementation failure originated.
    ///
    /// This is typically the interface or the declaring class of the method that
    /// could not be implemented.
    ///
    /// @return the root type; never null
    @NonNull
    public Type getRoot() {
        return root;
    }
}
