package ninja.javahacker.annotimpler.core;

import lombok.NonNull;

import module java.base;

///
/// This exception signals that something intended to be implementated with [AnnotationsImplementor] couldn't be implemented.
///
/// This exception might be thrown directly by [AnnotationsImplementor] or indirectly by other stuff that validates inputs or outputs
/// of [AnnotationsImplementor].
///
public class BadImplementationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Type root;

    public BadImplementationException(@NonNull String message, @NonNull Type root) {
        List.of(message, root); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.root = root;
    }

    public BadImplementationException(@NonNull String message, @NonNull Throwable cause, @NonNull Type root) {
        List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
        super(message, cause);
        this.root = root;
    }

    @NonNull
    public Type getRoot() {
        return root;
    }
}
