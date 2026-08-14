package ninja.javahacker.typeser;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;

/// Serializable wrapper around a supported `java.lang.reflect.Type`.
///
/// Instances convert the supplied `Type` to an internal serializable surrogate so
/// the type can be written to and read from an object stream.
@SuppressFBWarnings(
        value = {"NFF_NON_FUNCTIONAL_FIELD", "OBJECT_DESERIALIZATION", "SE_TRANSIENT_FIELD_NOT_RESTORED"},
        justification = "Those fields are not set on purpose. The proxy field is used instead only on sreialization."
)
public final class TypeRef implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// A surrogate wrapper for the real type implementation.
    @NonNull
    private final SerializableType proxy;

    /// The very same `Type` instance given to the constructor, kept only for as long as this
    /// object lives in the JVM that created it. It is deliberately `transient` so that it never
    /// survives an actual serialization/deserialization round-trip through an object stream,
    /// in which case `type()` falls back to reconstructing an equivalent `Type` from `proxy`.
    ///
    /// Keeping this reference around lets same-JVM callers (e.g. exceptions that merely wrap a
    /// `Type` for informational purposes and never truly get serialized) recover the exact
    /// original instance instead of a merely-equal reconstructed surrogate, which both preserves
    /// reference identity and avoids the always-throwing reconstruction of unrecognized `Type`
    /// implementations that {@link SerializableType} cannot rebuild from scratch.
    @Nullable
    private final transient Type original;

    /// Creates a new wrapper for `type`.
    ///
    /// @param type the type to wrap; must not be `null`.
    /// @throws IllegalArgumentException If `type` is `null`.
    public TypeRef(@NonNull Type type) {
        this.proxy = SerializableType.from(type);
        this.original = type;
    }

    /// Reconstructs the wrapped `Type`.
    ///
    /// Returns the very same instance that was originally wrapped if this object has not gone
    /// through an actual serialization round-trip; otherwise reconstructs an equivalent `Type`
    /// from the internal surrogate.
    ///
    /// @return the wrapped type; never `null`.
    /// @throws UnsupportedOperationException If this represents a type that can't be reconstructed.
    ///         This never happens when the type only contains combinations of [Class], [ParameterizedType], [WildcardType],
    ///         [GenericArrayType] and [TypeVariable].
    ///         Hence, it is not expected to happen with any real-case type data, only with ill-defined, corrupted, malformed or maliciously
    ///         constructed types.
    @NonNull
    public Type type() {
        return original != null ? original : proxy.toType();
    }

    /// Creates a serializable wrapper for `type`.
    ///
    /// @param type the type to wrap; must not be `null`.
    /// @return a wrapper that can later reconstruct the same type.
    /// @throws IllegalArgumentException If `type` is `null`.
    @NonNull
    public static TypeRef wrap(@NonNull Type type) {
        return new TypeRef(type);
    }

    /// Writes a wrapped `Type` to an object stream.
    ///
    /// @param out the destination stream; must not be `null`.
    /// @param type the type to serialize; must not be `null`.
    /// @throws IOException if the stream cannot be written.
    /// @throws IllegalArgumentException If any of `out` or `type` are `null`.
    public static void write(@NonNull ObjectOutput out, @NonNull Type type) throws IOException {
        out.writeObject(new TypeRef(type));
    }

    /// Reads a wrapped `Type` from an object stream.
    ///
    /// @param in the source stream; must not be `null`.
    /// @return the reconstructed type; never `null`.
    /// @throws IOException if the stream cannot be read.
    /// @throws ClassNotFoundException if the stream does not contain a `TypeRef`.
    /// @throws IllegalArgumentException If `in` is `null`.
    @NonNull
    public static Type read(@NonNull ObjectInput in) throws IOException, ClassNotFoundException {
        return ((TypeRef) in.readObject()).type();
    }
}