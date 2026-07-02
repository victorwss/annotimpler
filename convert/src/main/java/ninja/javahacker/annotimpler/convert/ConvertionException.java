package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

/// Thrown when a value cannot be converted from its source type to the desired target type.
///
/// Carries the source [Class] (`in`) and the target [Type] (`out`) of the failed conversion.
public class ConvertionException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Class<?> in;

    @NonNull
    private final Type out;

    /// Constructs a [ConvertionException] with a default message derived from the target type name.
    ///
    /// @param in The source type that failed to convert.
    /// @param out The target type of the failed conversion.
    /// @throws IllegalArgumentException If `in` is `null`.
    /// @throws IllegalArgumentException If `out` is `null`.
    public ConvertionException(@NonNull Class<?> in, @NonNull Type out) {
        List.of(in, out); // Force lombok to put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", TypeName.of(out)), in, out);
    }

    /// Constructs a [ConvertionException] with the given message, source type, and target type.
    ///
    /// @param message The detail message describing the conversion failure.
    /// @param in The source type that failed to convert.
    /// @param out The target type of the failed conversion.
    /// @throws IllegalArgumentException If `message` is `null`.
    /// @throws IllegalArgumentException If `in` is `null`.
    /// @throws IllegalArgumentException If `out` is `null`.
    public ConvertionException(@NonNull String message, @NonNull Class<?> in, @NonNull Type out) {
        List.of(message, in, out); // Force lombok to put the null-checks before the constructor call.
        super(message);
        this.in = in;
        this.out = out;
    }

    /// Constructs a [ConvertionException] with a default message, wrapping the given cause.
    ///
    /// @param cause The underlying exception that caused the conversion failure.
    /// @param in The source type that failed to convert.
    /// @param out The target type of the failed conversion.
    /// @throws IllegalArgumentException If `cause` is `null`.
    /// @throws IllegalArgumentException If `in` is `null`.
    /// @throws IllegalArgumentException If `out` is `null`.
    public ConvertionException(@NonNull Throwable cause, @NonNull Class<?> in, @NonNull Type out) {
        List.of(cause, in, out); // Force lombok to put the null-checks before the constructor call.
        this("Can't read value as $$$.".replace("$$$", TypeName.of(out)), cause, in, out);
    }

    /// Constructs a [ConvertionException] with the given message, cause, source type, and target type.
    ///
    /// @param message The detail message describing the conversion failure.
    /// @param cause The underlying exception that caused the conversion failure.
    /// @param in The source type that failed to convert.
    /// @param out The target type of the failed conversion.
    /// @throws IllegalArgumentException If `message` is `null`.
    /// @throws IllegalArgumentException If `cause` is `null`.
    /// @throws IllegalArgumentException If `in` is `null`.
    /// @throws IllegalArgumentException If `out` is `null`.
    public ConvertionException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> in, @NonNull Type out) {
        List.of(message, cause, in, out); // Force lombok to put the null-checks before the constructor call.
        super(message, cause);
        this.in = in;
        this.out = out;
    }

    /// Returns the source type that could not be converted.
    ///
    /// @return The [Class] of the value that failed to convert.
    @NonNull
    public Class<?> getIn() {
        return in;
    }

    /// Returns the target type of the failed conversion.
    ///
    /// @return The [Type] that the converter was attempting to produce.
    @NonNull
    public Type getOut() {
        return out;
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
