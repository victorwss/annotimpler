package ninja.javahacker.typeser;

import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// Internal serializable surrogate for a `GenericDeclaration` (the [Class], [Method] or
/// [Constructor] that declares a [TypeVariable]).
@PackagePrivate
sealed interface GenericDeclarationSer extends Serializable permits
        GenericDeclarationSer.ClassDeclarationSer,
        GenericDeclarationSer.MethodDeclarationSer,
        GenericDeclarationSer.ConstructorDeclarationSer
{

    /// Reconstructs the original `GenericDeclaration`.
    /// @return The original `GenericDeclaration`.
    /// @throws IllegalStateException If the declaring method or constructor can't be found anymore
    ///         (e.g. its signature changed since serialization).
    @NonNull
    public GenericDeclaration toGenericDeclaration();

    /// Converts a supported `GenericDeclaration` into its serializable surrogate form.
    /// @param declaration The declaration to be serialized into a surrogate form.
    /// @return The surrogate form. Never `null`.
    /// @throws IllegalArgumentException If `declaration` is `null`.
    /// @throws UnsupportedOperationException If `declaration` is not a [Class], [Method] or [Constructor].
    @NonNull
    public static GenericDeclarationSer from(@NonNull GenericDeclaration declaration) {
        return switch (declaration) {
            case Class<?> c -> ClassDeclarationSer.create(c);
            case Method m -> MethodDeclarationSer.create(m);
            case Constructor<?> c -> ConstructorDeclarationSer.create(c);
            default -> throw new UnsupportedOperationException("Unsupported GenericDeclaration: " + declaration.getClass());
        };
    }

    public record ClassDeclarationSer<E>(@NonNull Class<E> clazz) implements GenericDeclarationSer {

        /// {@inheritDoc}
        @NonNull
        @Override
        public Class<E> toGenericDeclaration() {
            return clazz;
        }

        /// Creates a surrogate for a `Class` acting as a `GenericDeclaration`.
        /// @param <E> The type represented by `clazz`.
        /// @param clazz The `Class` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `clazz` is `null`.
        @NonNull
        public static <E> ClassDeclarationSer<E> create(@NonNull Class<E> clazz) {
            checkNotNull(clazz); // Check recognized by lombok.
            return new ClassDeclarationSer<>(clazz);
        }
    }

    public record MethodDeclarationSer(@NonNull Class<?> declaringClass, @NonNull String name, @NonNull Class<?>[] parameterTypes)
            implements GenericDeclarationSer
    {

        /// Reconstructs the original `Method`.
        /// @return The original `Method`.
        /// @throws IllegalStateException If no method named `name()` with parameter types `parameterTypes()`
        ///         exists (anymore) in `declaringClass()`.
        @NonNull
        @Override
        public Method toGenericDeclaration() {
            try {
                return declaringClass.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Method \"" + name + "\" not found in " + declaringClass + ".", e);
            }
        }

        /// Creates a surrogate for a `Method`.
        /// @param m The `Method` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `m` is `null`.
        public static MethodDeclarationSer create(@NonNull Method m) {
            checkNotNull(m); // Check recognized by lombok.
            return new MethodDeclarationSer(m.getDeclaringClass(), m.getName(), m.getParameterTypes());
        }
    }

    public record ConstructorDeclarationSer(@NonNull Class<?> declaringClass, @NonNull Class<?>[] parameterTypes)
            implements GenericDeclarationSer
    {

        /// Reconstructs the original `Constructor`.
        /// @return The original `Constructor`.
        /// @throws IllegalStateException If no constructor with parameter types `parameterTypes()`
        ///         exists (anymore) in `declaringClass()`.
        @NonNull
        @Override
        public Constructor<?> toGenericDeclaration() {
            try {
                return declaringClass.getDeclaredConstructor(parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Constructor not found in " + declaringClass + ".", e);
            }
        }

        /// Creates a surrogate for a `Constructor`.
        /// @param c The `Constructor` to be serialized into a surrogate form.
        /// @return The surrogate form. Never `null`.
        /// @throws IllegalArgumentException If `c` is `null`.
        public static ConstructorDeclarationSer create(@NonNull Constructor<?> c) {
            checkNotNull(c); // Check recognized by lombok.
            return new ConstructorDeclarationSer(c.getDeclaringClass(), c.getParameterTypes());
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
