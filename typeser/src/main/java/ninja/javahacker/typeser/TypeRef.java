package ninja.javahacker.typeser;

import lombok.NonNull;

import module java.base;

/// Serializable wrapper around a supported `java.lang.reflect.Type`.
///
/// Instances convert the supplied `Type` to an internal serializable surrogate so
/// the type can be written to and read from an object stream.
public final class TypeRef implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final SerializableType proxy;

    /// Creates a new wrapper for `type`.
    ///
    /// @param type the type to wrap; must not be `null`.
    public TypeRef(Type type) {
        this.proxy = SerializableType.from(type);
    }

    /// Reconstructs the wrapped `Type`.
    ///
    /// @return the wrapped type; never `null`.
    @NonNull
    public Type type() {
        return proxy.toType();
    }

    /// Creates a serializable wrapper for `type`.
    ///
    /// @param type the type to wrap; must not be `null`.
    /// @return a wrapper that can later reconstruct the same type.
    @NonNull
    public static TypeRef wrap(@NonNull Type type) {
        return new TypeRef(type);
    }

    /// Writes a wrapped `Type` to an object stream.
    ///
    /// @param out the destination stream; must not be `null`.
    /// @param type the type to serialize; must not be `null`.
    /// @throws IOException if the stream cannot be written.
    public static void write(@NonNull ObjectOutput out, @NonNull Type type) throws IOException {
        out.writeObject(new TypeRef(type));
    }

    /// Reads a wrapped `Type` from an object stream.
    ///
    /// @param in the source stream; must not be `null`.
    /// @return the reconstructed type; never `null`.
    /// @throws IOException if the stream cannot be read.
    /// @throws ClassNotFoundException if the stream does not contain a `TypeRef`.
    @NonNull
    public static Type read(@NonNull ObjectInput in) throws IOException, ClassNotFoundException {
        return ((TypeRef) in.readObject()).type();
    }
}