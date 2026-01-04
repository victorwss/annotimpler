package ninja.javahacker.annotimpler.magicfactory;

import module java.base;
import lombok.NonNull;

public class ConstructionException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Type root;

    public ConstructionException(@NonNull String message, @NonNull Type root) {
        List.of(message, root); // Force lombok put the null-checks before the constructor call.
        super(message);
        this.root = root;
    }

    public ConstructionException(@NonNull String message, @NonNull Throwable cause, @NonNull Type root) {
        List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
        super(message, cause);
        this.root = root;
    }

    @NonNull
    public Type getRoot() {
        return root;
    }
}
