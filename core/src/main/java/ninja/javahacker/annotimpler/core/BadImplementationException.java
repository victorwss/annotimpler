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
    /// @param message The detail message; must not be `null`.
    /// @param root The type (usually the interface or the declaring class of the method)
    ///             where the failure originated; must not be `null`.
    /// @throws IllegalArgumentException If `message` or `root` is `null`.
    public BadImplementationException(@NonNull String message, @NonNull Type root) {
        List.of(message, root); // Force lombok to put the null-checks before the constructor call.
        super(message);
        this.root = root;
    }

    /// Creates a new exception with the given detail message, cause, and root type.
    ///
    /// @param message The detail message; must not be `null`.
    /// @param cause The exception that triggered this failure; must not be `null`.
    /// @param root The type (usually the interface or the declaring class of the method)
    ///             where the failure originated; must not be `null`.
    /// @throws IllegalArgumentException If `message`, `cause`, or `root` is `null`.
    public BadImplementationException(@NonNull String message, @NonNull Throwable cause, @NonNull Type root) {
        List.of(message, cause, root); // Force lombok to put the null-checks before the constructor call.
        super(message, cause);
        this.root = root;
    }

    /// Returns the type where the implementation failure originated.
    ///
    /// This is typically the interface or the declaring class of the method that could not be implemented.
    ///
    /// @return The root type; never `null`.
    @NonNull
    public Type getRoot() {
        return root;
    }

    /// Disabled. Should not be used. Does nothing.
    ///
    /// This method exists with the sole purpose of fixing SpotBugs' CT_CONSTRUCTOR_THROW
    /// by disabling the ability to override the `finalize()` method that should not even exist to start with.
    ///
    /// @deprecated Finalization was deprecated. This method is intentionally unused, unusable and disabled.
    @Deprecated
    @SuppressWarnings({
        "override", "removal", "FinalizeDoesntCallSuperFinalize", "FinalizeDeclaration", "PMD.EmptyFinalizer", "checkstyle:NoFinalizer"
    })
    protected final void finalize() {
    }
}
