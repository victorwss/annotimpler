package ninja.javahacker.typeser;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    /// Tracks, per thread, the `Type` instances whose surrogate is currently being built by an
    /// in-progress, still-unfinished call to [#from(Type)] on that same thread.
    ///
    /// Identity (not [Object#equals(Object)]) is used to recognize a previously-seen `Type`, since
    /// a malicious implementation could otherwise define `equals` in a way that defeats the
    /// cycle check (or that itself misbehaves, e.g. by recursing back into the very type graph
    /// being serialized).
    ///
    /// This state should not be considered public, despite the `public` modifier. But, since [SerializableType]
    /// itself is not public, hence this field isn't either. This should be considered in future refactorings
    /// if someone considers making [SerializableType] public (though unlikely).
    @NonNull
    public static final ThreadLocal<Set<Type>> VISITING = ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    /// Threshold where we should give up serializing a too-deeply nested type.
    /// Specially useful when the nested type is maliciously crafted to be infinitely deep.
    public static int TOO_LARGE_TYPE = 256;

    /// Tracks, per thread, the number of `Type` instances being visited in order to build an
    /// in-progress, still-unfinished root `Type` from a call to [#from(Type)] on that same thread.
    ///
    /// This state should not be considered public, despite the `public` modifier. But, since [SerializableType]
    /// itself is not public, hence this field isn't either. This should be considered in future refactorings
    /// if someone considers making [SerializableType] public (though unlikely).
    @NonNull
    public static final ThreadLocal<Integer> VISIT_COUNT = ThreadLocal.withInitial(() -> 0);

    /// Reconstructs the original `Type`.
    /// @return The original `Type`.
    /// @throws UnsupportedOperationException If this represents a type that can't be reconstructed.
    ///         This never happens when the type only contains combinations of [Class], [ParameterizedType], [WildcardType],
    ///         [GenericArrayType] and [TypeVariable].
    ///         Hence, it is not expected to happen with any real-case type data, only with ill-defined, corrupted, malformed or maliciously
    ///         constructed types.
    @NonNull
    public Type toType();

    /// Converts a supported `Type` into its serializable surrogate form.
    /// @param type The type to be serialized into a surrogate form.
    /// @return The surrogate form. Never `null`.
    /// @throws IllegalArgumentException If `type` is `null`, or if `type` is part of a cyclic type graph
    ///         (i.e. `type` directly or indirectly refers back to itself through some combination of
    ///         raw types, type arguments, owner types, array component types or wildcard bounds).
    ///         This can only happen with a hand-written or maliciously-crafted `Type` implementation,
    ///         never with a `Type` obtained through the reflection API.
    @NonNull
    public static SerializableType from(@NonNull Type type) {
        var visiting = VISITING.get();
        if (!visiting.add(type)) {
            throw new IllegalArgumentException("Cyclic type graph detected involving " + type + ".");
        }
        try {
            var howMany = VISIT_COUNT.get();
            if (howMany > TOO_LARGE_TYPE) {
                throw new IllegalArgumentException("Too deeply nested type graph detected involving " + type + ".");
            }
            VISIT_COUNT.set(howMany + 1);
            return switch (type) {
                case Class<?> c -> ClassSer.create(c);
                case ParameterizedType p -> ParameterizedTypeSer.create(p);
                case WildcardType w -> WildcardTypeSer.create(w);
                case GenericArrayType g -> GenericArrayTypeSer.create(g);
                case TypeVariable<?> v -> TypeVariableSer.create(v);
                default -> UnknownTypeSer.create(type);
            };
        } finally {
            visiting.remove(type);
            if (visiting.isEmpty()) {
                VISIT_COUNT.set(0);
            }
        }
    }

    public record ClassSer<E>(@NonNull Class<E> clazz) implements SerializableType {

        /// {@inheritDoc}
        @NonNull
        @Override
        public Class<E> toType() {
            return clazz;
        }

        /// Creates a surrogate for a `Class`.
        /// @param <E> The type represented by `clazz`.
        /// @param clazz The `Class` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `clazz` is `null`.
        @NonNull
        public static <E> ClassSer<E> create(@NonNull Class<E> clazz) {
            checkNotNull(clazz); // Check recognized by lombok.
            return new ClassSer<>(clazz);
        }
    }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "SerializableType is not public the arrays are always copied in the toType() method."
    )
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
        /// @throws IllegalArgumentException If `p` is `null`, or if `p.getActualTypeArguments()` returns `null`
        ///         (which never happens with a `ParameterizedType` obtained through the reflection API,
        ///         only with a hand-written or maliciously-crafted implementation).
        @NonNull
        public static ParameterizedTypeSer create(@NonNull ParameterizedType p) {
            checkNotNull(p); // Check recognized by lombok.
            var typeArguments = p.getActualTypeArguments();
            if (typeArguments == null) {
                throw new IllegalArgumentException(p + ".getActualTypeArguments() returned null.");
            }
            return new ParameterizedTypeSer(
                    from(p.getRawType()),
                    Arrays.stream(typeArguments).map(SerializableType::from).toArray(SerializableType[]::new),
                    p.getOwnerType() == null ? null : from(p.getOwnerType())
            );
        }
    }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "SerializableType is not public the arrays are always copied in the toType() method."
    )
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
        /// @throws IllegalArgumentException If `w` is `null`, or if `w.getUpperBounds()` or `w.getLowerBounds()`
        ///         returns `null` (which never happens with a `WildcardType` obtained through the reflection API,
        ///         only with a hand-written or maliciously-crafted implementation).
        @NonNull
        public static WildcardTypeSer create(@NonNull WildcardType w) {
            checkNotNull(w); // Check recognized by lombok.
            var upperBounds = w.getUpperBounds();
            var lowerBounds = w.getLowerBounds();
            if (upperBounds == null) throw new IllegalArgumentException(w + ".getUpperBounds() returned null.");
            if (lowerBounds == null) throw new IllegalArgumentException(w + ".getLowerBounds() returned null.");
            return new WildcardTypeSer(
                    Arrays.stream(upperBounds).map(SerializableType::from).toArray(SerializableType[]::new),
                    Arrays.stream(lowerBounds).map(SerializableType::from).toArray(SerializableType[]::new)
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
        @NonNull
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
        @NonNull
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
            throw new UnsupportedOperationException("Unknown type.");
        }

        /// Creates a placeholder surrogate for a `Type` that is none of [Class], [ParameterizedType], [WildcardType],
        /// [GenericArrayType] or [TypeVariable]. The resulting surrogate can be serialized but never reconstructed back.
        /// @param t The unsupported type. Kept only for the `@NonNull` check; its identity is otherwise discarded.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `t` is `null`.
        @NonNull
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