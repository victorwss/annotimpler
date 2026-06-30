package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

/// Thrown when no [Converter] is available for a requested [Type].
///
/// Carries the root [Type] for which no converter could be found.
public class UnavailableConverterException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Type root;

    /// Constructs an [UnavailableConverterException] with the given message and root type.
    ///
    /// @param message The detail message describing why no converter is available.
    /// @param root The type for which no converter is available.
    /// @throws IllegalArgumentException If `message` or `root` is `null`.
    public UnavailableConverterException(@NonNull String message, @NonNull Type root) {
        List.of(message, root); // Force lombok to put the null-checks before the constructor call.
        super(message);
        this.root = root;
    }

    /// Constructs an [UnavailableConverterException] with the given message, cause, and root type.
    ///
    /// @param message The detail message describing why no converter is available.
    /// @param cause The underlying exception that triggered this exception.
    /// @param root The type for which no converter is available.
    /// @throws IllegalArgumentException If `message`, `cause` or `root` is `null`.
    public UnavailableConverterException(@NonNull String message, @NonNull Throwable cause, @NonNull Type root) {
        List.of(message, cause, root); // Force lombok to put the null-checks before the constructor call.
        super(message, cause);
        this.root = root;
    }

    /// Creates an [UnavailableConverterException] with an appropriate message for the given type.
    ///
    /// For multidimensional array types, the message is "No converter for multidimensional arrays."
    /// For all other types, the message uses the type's name.
    ///
    /// @param root The type for which no converter is available.
    /// @return A new [UnavailableConverterException] describing the missing converter.
    /// @throws IllegalArgumentException If `root` is `null`.
    @NonNull
    public static UnavailableConverterException noConverterFor(@NonNull Type root) {
        if (root instanceof Class<?> k && k.isArray() && k.getComponentType().isArray()) {
            return new UnavailableConverterException("No converter for multidimensional arrays.", root);
        }
        return new UnavailableConverterException("No converter for " + root.getTypeName() + ".", root);
    }

    /// Returns the type for which no converter is available.
    ///
    /// @return The root [Type] that could not be mapped to a converter.
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
    @SuppressWarnings({"override", "removal", "FinalizeDoesntCallSuperFinalize", "FinalizeDeclaration", "PMD.EmptyFinalizer"})
    protected final void finalize() {
    }
}
