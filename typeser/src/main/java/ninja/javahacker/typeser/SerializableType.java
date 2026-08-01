package ninja.javahacker.typeser;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// Internal serializable surrogate for supported `Type` implementations.
@PackagePrivate
sealed interface SerializableType extends Serializable permits
        SerializableType.ClassSer, SerializableType.ParameterizedTypeSer, SerializableType.WildcardTypeSer,
        SerializableType.GenericArrayTypeSer, SerializableType.TypeVariableSer, SerializableType.UnknownTypeSer
{

    /// Reconstructs the original `Type`.
    /// @return The original `Type`.
    /// @throw UnsupportedOperationException If this represents a type that can't be reconstructed.
    ///        This never happens when the type only contains combinations of [Class], [ParameterizedType], [WildcardType],
    ///        [GenericArrayType] and [TypeVariable].
    ///        Hence, it is not expected to happen with any real-case type data, only with ill-defined, corrupted, malformed or maliciously
    ///        constructed types.
    @NonNull
    public Type toType();

    /// Converts a supported `Type` into its serializable surrogate form.
    /// @param type The type to be serialized into a surrogate form.
    /// @return The surrogate form. Never `null`.
    /// @throws IllegalArgumentException If `type` is `null`.
    @NonNull
    public static SerializableType from(@NonNull Type type) {
        checkNotNull(type); // Check recognized by lombok.
        return switch (type) {
            case Class<?> c -> ClassSer.create(c);
            case ParameterizedType p -> ParameterizedTypeSer.create(p);
            case WildcardType w -> WildcardTypeSer.create(w);
            case GenericArrayType g -> GenericArrayTypeSer.create(g);
            case TypeVariable<?> v -> TypeVariableSer.create(v);
            default -> UnknownTypeSer.create(type);
        };
    }

    public record ClassSer<E>(@NonNull Class<E> clazz) implements SerializableType {

        /// {@inheritDoc}
        @Override
        public Class<E> toType() {
            return clazz;
        }

        /// Creates a surrogate for a `Class`.
        /// @param <E> The type represented by `clazz`.
        /// @param clazz The `Class` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `clazz` is `null`.
        public static <E> ClassSer<E> create(@NonNull Class<E> clazz) {
            checkNotNull(clazz); // Check recognized by lombok.
            return new ClassSer<>(clazz);
        }
    }

    public record ParameterizedTypeSer(@NonNull SerializableType raw, @NonNull SerializableType[] args, @Nullable SerializableType owner)
            implements SerializableType
    {

        /// {@inheritDoc}
        @NonNull
        @Override
        public ParameterizedType toType() {
            return new ParameterizedType() {
                /// {@inheritDoc}
                @NonNull
                @Override
                public Type[] getActualTypeArguments() {
                    return Arrays.stream(args).map(SerializableType::toType).toArray(Type[]::new);
                }

                /// {@inheritDoc}
                @NonNull
                @Override
                public Type getRawType() {
                    return raw.toType();
                }

                /// {@inheritDoc}
                @Nullable
                @Override
                public Type getOwnerType() {
                    return owner == null ? null : owner.toType();
                }
            };
        }

        /// Creates a surrogate for a `ParameterizedType`.
        /// @param p The `ParameterizedType` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `p` is `null`.
        public static ParameterizedTypeSer create(@NonNull ParameterizedType p) {
            checkNotNull(p); // Check recognized by lombok.
            return new ParameterizedTypeSer(
                    from(p.getRawType()),
                    Arrays.stream(p.getActualTypeArguments()).map(SerializableType::from).toArray(SerializableType[]::new),
                    p.getOwnerType() == null ? null : from(p.getOwnerType())
            );
        }
    }

    public record WildcardTypeSer(@NonNull SerializableType[] upper, @NonNull SerializableType[] lower) implements SerializableType {

        /// {@inheritDoc}
        @NonNull
        @Override
        public WildcardType toType() {
            return new WildcardType() {
                /// {@inheritDoc}
                @NonNull
                @Override
                public Type[] getUpperBounds() {
                    return Arrays.stream(upper).map(SerializableType::toType).toArray(Type[]::new);
                }

                /// {@inheritDoc}
                @NonNull
                @Override
                public Type[] getLowerBounds() {
                    return Arrays.stream(lower).map(SerializableType::toType).toArray(Type[]::new);
                }
            };
        }

        /// Creates a surrogate for a `WildcardType`.
        /// @param w The `WildcardType` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `w` is `null`.
        public static WildcardTypeSer create(@NonNull WildcardType w) {
            checkNotNull(w); // Check recognized by lombok.
            return new WildcardTypeSer(
                    Arrays.stream(w.getUpperBounds()).map(SerializableType::from).toArray(SerializableType[]::new),
                    Arrays.stream(w.getLowerBounds()).map(SerializableType::from).toArray(SerializableType[]::new)
            );
        }
    }

    public record GenericArrayTypeSer(@NonNull SerializableType component) implements SerializableType {

        /// {@inheritDoc}
        @NonNull
        @Override
        public GenericArrayType toType() {
            return new GenericArrayType() {

                /// {@inheritDoc}
                @NonNull
                @Override
                public Type getGenericComponentType() {
                    return component.toType();
                }
            };
        }

        /// Creates a surrogate for a `GenericArrayType`.
        /// @param g The `GenericArrayType` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `g` is `null`.
        public static GenericArrayTypeSer create(@NonNull GenericArrayType g) {
            checkNotNull(g); // Check recognized by lombok.
            return new GenericArrayTypeSer(from(g.getGenericComponentType()));
        }
    }

    public record TypeVariableSer<D extends GenericDeclaration>(@NonNull GenericDeclarationSer declaration, @NonNull String name)
            implements SerializableType
    {

        /// Reconstructs the original `TypeVariable`.
        ///
        /// This works by locating, among the type parameters declared by the reconstructed [GenericDeclaration], the
        /// one whose name matches the serialized name, thus yielding back the very same `TypeVariable` instance that
        /// was originally serialized (bounds, annotations and all).
        ///
        /// @return The original `TypeVariable`.
        /// @throws IllegalStateException If no type parameter with the serialized name exists in the reconstructed
        ///         [GenericDeclaration]. This should never happen for a `TypeVariable` obtained from [#create(TypeVariable)].
        @NonNull
        @Override
        public TypeVariable<?> toType() {
            var decl = declaration.toGenericDeclaration();
            return Arrays.stream(decl.getTypeParameters())
                    .filter(tv -> tv.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Type variable \"" + name + "\" not found in " + decl + "."));
        }

        /// Creates a surrogate for a `TypeVariable`.
        /// @param <D> The kind of the [GenericDeclaration] which declares `v`.
        /// @param v The `TypeVariable` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `v` is `null`.
        /// @throws UnsupportedOperationException If the [GenericDeclaration] of `v` is not a [Class],
        ///         [java.lang.reflect.Method] or [java.lang.reflect.Constructor].
        public static <D extends GenericDeclaration> TypeVariableSer<D> create(@NonNull TypeVariable<D> v) {
            checkNotNull(v); // Check recognized by lombok.
            return new TypeVariableSer<>(GenericDeclarationSer.from(v.getGenericDeclaration()), v.getName());
        }
    }

    public record UnknownTypeSer() implements SerializableType {

        /// Always fails, since an unrecognized `Type` implementation carries no reconstructable information.
        /// @return Never returns.
        /// @throws UnsupportedOperationException Always.
        @NonNull
        @Override
        public Type toType() {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        /// Creates a placeholder surrogate for a `Type` that is none of [Class], [ParameterizedType], [WildcardType],
        /// [GenericArrayType] or [TypeVariable]. The resulting surrogate can be serialized but never reconstructed back.
        /// @param t The unsupported type. Kept only for the `@NonNull` check; its identity is otherwise discarded.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `t` is `null`.
        public static UnknownTypeSer create(@NonNull Type t) {
            checkNotNull(t); // Check recognized by lombok.
            return new UnknownTypeSer();
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}