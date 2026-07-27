package ninja.javahacker.typeser;

import lombok.NonNull;

import module java.base;

public final class TypeRef implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NonNull
    private final SerializableType proxy;

    public TypeRef(Type type) {
        this.proxy = SerializableType.from(type);
    }

    @NonNull
    public Type type() {
        return proxy.toType();
    }

    @NonNull
    public static TypeRef wrap(@NonNull Type type) {
        return new TypeRef(type);
    }

    @NonNull
    public static void write(@NonNull ObjectOutput out, @NonNull Type type) throws IOException {
        out.writeObject(new TypeRef(type));
    }

    @NonNull
    public static Type read(ObjectInput in) throws IOException, ClassNotFoundException {
        return ((TypeRef) in.readObject()).type();
    }
}