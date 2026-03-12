package ninja.javahacker.annotimpler.core;

import lombok.NonNull;

import module java.base;

public class BadImplementationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Class<?> root;

    public BadImplementationException(@NonNull String message, @NonNull Class<?> root) {
        List.of(message, root); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.root = root;
    }

    public BadImplementationException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> root) {
        List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
        super(message, cause);
        this.root = root;
    }

    @NonNull
    public Class<?> getRoot() {
        return root;
    }
}
