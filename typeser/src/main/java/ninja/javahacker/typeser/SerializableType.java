package ninja.javahacker.typeser;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// Internal serializable surrogate for supported `Type` implementations.
@PackagePrivate
sealed interface SerializableType extends Serializable permits ClassSer, ParameterizedTypeSer, WildcardTypeSer, GenericArrayTypeSer {

    /// Reconstructs the original `Type`.
    @NonNull
    public Type toType();

    /// Converts a supported `Type` into its serializable surrogate form.
    @NonNull
    public static SerializableType from(@NonNull Type type) {
        return switch (type) {
            case Class<?> c ->
                    new ClassSer(c);

            case ParameterizedType p ->
                    new ParameterizedTypeSer(
                            from(p.getRawType()),
                            Arrays.stream(p.getActualTypeArguments())
                                  .map(SerializableType::from)
                                  .toArray(SerializableType[]::new),
                            p.getOwnerType() == null
                                    ? null
                                    : from(p.getOwnerType())
                    );

            case WildcardType w ->
                    new WildcardTypeSer(
                            Arrays.stream(w.getUpperBounds())
                                  .map(SerializableType::from)
                                  .toArray(SerializableType[]::new),
                            Arrays.stream(w.getLowerBounds())
                                  .map(SerializableType::from)
                                  .toArray(SerializableType[]::new)
                    );

            case GenericArrayType g ->
                    new GenericArrayTypeSer(from(g.getGenericComponentType()));

            default ->
                    throw new UnsupportedOperationException("Unsupported Type: " + type.getClass());
        };
    }
}

@PackagePrivate
record ClassSer(@NonNull Class<?> clazz) implements SerializableType {
    @Override
    public Type toType() {
        return clazz;
    }
}

@PackagePrivate
record ParameterizedTypeSer(@NonNull SerializableType raw, @NonNull SerializableType[] args, @Nullable SerializableType owner)
        implements SerializableType
{

    @NonNull
    @Override
    public Type toType() {
        return new ParameterizedType() {
            @NonNull
            @Override
            public Type[] getActualTypeArguments() {
                return Arrays.stream(args).map(SerializableType::toType).toArray(Type[]::new);
            }

            @NonNull
            @Override
            public Type getRawType() {
                return raw.toType();
            }

            @Nullable
            @Override
            public Type getOwnerType() {
                return owner == null ? null : owner.toType();
            }
        };
    }
}

@PackagePrivate
record WildcardTypeSer(@NonNull SerializableType[] upper, @NonNull SerializableType[] lower) implements SerializableType {

    @NonNull
    @Override
    public Type toType() {
        return new WildcardType() {
            @NonNull
            @Override
            public Type[] getUpperBounds() {
                return Arrays.stream(upper).map(SerializableType::toType).toArray(Type[]::new);
            }

            @NonNull
            @Override
            public Type[] getLowerBounds() {
                return Arrays.stream(lower).map(SerializableType::toType).toArray(Type[]::new);
            }
        };
    }
}

@PackagePrivate
record GenericArrayTypeSer(@NonNull SerializableType component) implements SerializableType {

    @NonNull
    @Override
    public Type toType() {
        return new GenericArrayType() {

            @NonNull
            @Override
            public Type getGenericComponentType() {
                return component.toType();
            }
        };
    }
}