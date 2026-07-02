package ninja.javahacker.annotimpler.magicfactory;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// Reflectively discovers and caches the *creator* of a given class, then allows repeated
/// instantiation via [#create(Object...)].
///
/// A *creator* is the single public, static (or constructor-based) entry point that produces
/// instances of the target class. `MagicFactory` selects the creator using the following
/// priority order:
///
/// 1. A member explicitly annotated with [@Creator] — constructor, static method, or static
///    field. Exactly one such annotation is allowed; multiple annotations cause
///    [CreatorSelectionException].
/// 2. For **enums** with a single constant, that constant is used as the creator.
/// 3. For classes with exactly **one declared constructor**, that constructor is used.
/// 4. For **records**, the canonical constructor (matching all record components) is used.
/// 5. The **default (no-arg) constructor**, if one exists.
///
/// If none of the above can be resolved, or if the resolved candidate fails validation
/// (e.g. it is not public, not static, or returns the wrong type), a
/// [CreatorSelectionException] is thrown.
///
/// ## Thread safety
///
/// `MagicFactory` instances are **immutable and thread-safe** after construction.
///
/// @param <E> The type of objects produced by this factory.
public final class MagicFactory<E> {

    @NonNull
    private final Class<E> klass;

    @NonNull
    private final MethodWrapper<E, Object> wrapper;

    private MagicFactory(@NonNull Class<E> klass) throws CreatorSelectionException {
        checkNotNull(klass); // Check recognized by lombok.
        this.klass = klass;
        this.wrapper = creatorFor(klass);
    }

    /// Creates a `MagicFactory` for the given class by inspecting its members and selecting
    /// the appropriate creator.
    ///
    /// @param <E> The type produced by the factory.
    /// @param klass The class to inspect; must be public; must not be `null`.
    /// @return A new `MagicFactory` for `klass`.
    /// @throws CreatorSelectionException If no suitable creator can be determined, if more
    ///         than one [@Creator]-annotated member exists, or if the resolved creator fails
    ///         validation.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    public static <E> MagicFactory<E> of(@NonNull Class<E> klass) throws CreatorSelectionException {
        return new MagicFactory<>(klass);
    }

    @NonNull
    private <W extends MethodWrapper<?, ?>> W checkOk(@NonNull W wrapper) throws CreatorSelectionException {
        checkNotNull(wrapper); // Check recognized by lombok.
        if (!wrapper.isStatic()) {
            var msg = "Instance " + wrapper + " can't have @Creator.";
            throw new CreatorSelectionException(msg, klass);
        }
        if (!wrapper.isPublic()) {
            var msg = "The " + wrapper + " can't have @Creator, it isn't public.";
            throw new CreatorSelectionException(msg, klass);
        }
        if (wrapper.isAbstract()) { // Abstract class constructor.
            var msg = wrapper.isAnnotationPresent(Creator.class)
                    ? "The " + wrapper + " can't have @Creator, the class is abstract."
                    : "The " + wrapper + " can't be a creator, the class is abstract.";
            throw new CreatorSelectionException(msg, klass);
        }

        var rt = wrapper.getReturnType();
        if (rt instanceof ParameterizedType p) {
            rt = p.getRawType();
            assertTrue(rt instanceof Class<?>);
        } else if (!(rt instanceof Class<?>)) {
            var msg = "Bad type for " + wrapper + ". Should be " + klass.getSimpleName() + ".";
            throw new CreatorSelectionException(msg, klass);
        }
        var rtt = (Class<?>) rt;
        if (!klass.isAssignableFrom(rtt)) {
            var msg = "Bad type for " + wrapper + ". Should be " + klass.getSimpleName() + ".";
            throw new CreatorSelectionException(msg, klass);
        }
        return wrapper;
    }

    @NonNull
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private <E> MethodWrapper<E, Object> creatorFor(@NonNull Class<E> klass) throws CreatorSelectionException {
        checkNotNull(klass); // Check recognized by lombok.

        if (!Modifier.isPublic(klass.getModifiers())) {
            var name = klass.isAnonymousClass() || klass.isHidden() ? klass.getName() : klass.getSimpleName();
            var msg = "The class " + name + " is not public.";
            throw new CreatorSelectionException(msg, klass);
        }

        var methods = klass.getDeclaredMethods();
        var fields = klass.getDeclaredFields();
        var constructors = klass.getDeclaredConstructors();

        // Find constructors, methods and fields annotated with @Creator.
        var s = Stream.<MethodWrapper<E, ?>>empty();
        s = Stream.concat(s, Stream.of(constructors).map(MethodWrapper::<E>of));
        s = Stream.concat(s, Stream.of(methods).map(MethodWrapper::of));
        s = Stream.concat(s, Stream.of(fields).map(MethodWrapper::getter));
        var annotated = s.filter(x -> x.isAnnotationPresent(Creator.class)).distinct().toList();

        if (annotated.size() > 1) {
            var msg = "Can't have @Creator more than once in class " + klass.getSimpleName() + ".";
            throw new CreatorSelectionException(msg, klass);
        }
        if (annotated.size() == 1) {
            return checkOk(annotated.getFirst()).eraseU();
        }

        if (klass.isEnum()) {
            var consts = klass.getEnumConstants();
            if (consts.length == 1) {
                var constant = klass.cast(consts[0]);
                return checkOk(MethodWrapper.value(constant)).eraseU();
            }
            var msg = "No preferred enum value for class " + klass.getSimpleName() + ".";
            throw new CreatorSelectionException(msg, klass);
        }

        // For records e beans, normally there is only one canonical constructor.
        if (constructors.length == 1) {
            return checkOk(MethodWrapper.<E>of(constructors[0])).eraseU();
        }

        // If there are multiple constructors, try to find the canonical.
        if (klass.isRecord()) {
            try {
                var components = klass.getRecordComponents();
                var componentTypes = Arrays.stream(components)
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
                var ctor = klass.getDeclaredConstructor(componentTypes);
                return checkOk(MethodWrapper.of(ctor)).eraseU();
            } catch (NoSuchMethodException e) {
                throw new CreatorSelectionException("Failed to determine canonical constructor for record.", e, klass);
            }
        }

        // Try the default no-arg constructor.
        try {
            var wrap = MethodWrapper.of(klass.getDeclaredConstructor());
            return checkOk(wrap).eraseU();
        } catch (NoSuchMethodException e) {
            var msg = "Failed to determine how to create an instance of " + klass.getSimpleName() + ".";
            throw new CreatorSelectionException(msg, e, klass);
        }
    }

    /// Invokes the selected creator with the given arguments and returns a new instance.
    ///
    /// The argument list must match the creator's parameter list in count and types. For
    /// instance methods (unusual in this context), the first argument must be the receiver.
    ///
    /// @param args The arguments to pass to the creator; must not be `null`.
    /// @return A newly created, non-null instance of type `E`.
    /// @throws CreationException If invocation fails for any reason (access denied, wrong
    ///         arguments, or the creator itself threw an exception).
    /// @throws IllegalArgumentException If `args` is `null`.
    @NonNull
    public E create(@NonNull Object... args) throws CreationException {
        try {
            var ret = wrapper.call(args);
            if (ret == null) throw new CreationException("Creator of " + klass.getSimpleName() + " produced null.", klass);
            return ret;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new CreationException("Creator of " + klass.getSimpleName() + " doesn't work.", e, klass);
        } catch (IllegalArgumentException e) {
            var msg = "Creator of " + klass.getSimpleName()
                    + " (" + wrapper + ") was called with the wrong arguments " + Arrays.asList(args) + ".";
            throw new CreationException(msg, e, klass);
        } catch (InvocationTargetException e) {
            throw new CreationException("The instantiation of " + klass.getSimpleName() + " threw an exception.", e.getCause(), klass);
        }
    }

    /// Returns the class that this factory produces.
    ///
    /// @return The target class; never `null`.
    @NonNull
    public Class<E> getReturnType() {
        return klass;
    }

    /// Returns the generic parameter types of the selected creator, in declaration order.
    ///
    /// @return An unmodifiable list of parameter types; never `null`.
    @NonNull
    public List<Type> getParameterTypes() {
        return wrapper.getParameterTypes();
    }

    /// Returns the [Parameter] objects of the selected creator, in declaration order.
    ///
    /// @return An unmodifiable list of parameters; never `null`.
    @NonNull
    public List<Parameter> getParameters() {
        return wrapper.getParameters();
    }

    /// Returns the number of parameters required by the selected creator.
    ///
    /// Equivalent to `getParameterTypes().size()`.
    ///
    /// @return The arity (number of parameters), zero or positive.
    @NonNull
    public int arity() {
        return wrapper.arity();
    }

    /// Thrown when [MagicFactory#of(Class)] cannot determine a unique, valid creator for the
    /// target class.
    ///
    /// Common causes include:
    /// - The class is not public.
    /// - More than one member is annotated with [@Creator].
    /// - The [@Creator]-annotated member is not public, not static, abstract, or returns the
    ///   wrong type.
    /// - The class has multiple constructors and none of the heuristics applies.
    public static class CreatorSelectionException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        @NonNull
        private final Class<?> root;

        /// Constructs a `CreatorSelectionException` with a detail message and the root class.
        ///
        /// @param message A human-readable description of the problem; must not be `null`.
        /// @param root The class whose creator selection failed; must not be `null`.
        /// @throws IllegalArgumentException If `message` or `root` is `null`.
        public CreatorSelectionException(@NonNull String message, @NonNull Class<?> root) {
            List.of(message, root); // Force lombok to put the null-checks before the constructor call.
            super(message);
            this.root = root;
        }

        /// Constructs a `CreatorSelectionException` with a detail message, a cause, and the
        /// root class.
        ///
        /// @param message A human-readable description of the problem; must not be `null`.
        /// @param cause The underlying exception that triggered this failure; must not be `null`.
        /// @param root The class whose creator selection failed; must not be `null`.
        /// @throws IllegalArgumentException If `message`, `cause`, or `root` is `null`.
        public CreatorSelectionException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> root) {
            List.of(message, cause, root); // Force lombok to put the null-checks before the constructor call.
            super(message, cause);
            this.root = root;
        }

        /// Returns the class whose creator could not be determined.
        ///
        /// @return The root class; never `null`.
        @NonNull
        public Class<?> getRoot() {
            return root;
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

    /// Thrown when [MagicFactory#create(Object...)] fails to invoke the selected creator.
    ///
    /// Common causes include insufficient access rights, mismatched argument types or counts,
    /// or an exception thrown by the creator itself.
    public static class CreationException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        @NonNull
        private final Class<?> root;

        /// Constructs a `CreationException` with a detail message and the root class.
        ///
        /// @param message A human-readable description of the failure; must not be `null`.
        /// @param root The class that could not be instantiated; must not be `null`.
        /// @throws IllegalArgumentException If `message` or `root` is `null`.
        public CreationException(@NonNull String message, @NonNull Class<?> root) {
            List.of(message, root); // Force lombok to put the null-checks before the constructor call.
            super(message);
            this.root = root;
        }

        /// Constructs a `CreationException` with a detail message, a cause, and the root class.
        ///
        /// @param message A human-readable description of the failure; must not be `null`.
        /// @param cause The underlying exception; must not be `null`.
        /// @param root The class that could not be instantiated; must not be `null`.
        /// @throws IllegalArgumentException If `message`, `cause`, or `root` is `null`.
        public CreationException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> root) {
            List.of(message, cause, root); // Force lombok to put the null-checks before the constructor call.
            super(message, cause);
            this.root = root;
        }

        /// Returns the class that could not be instantiated.
        ///
        /// @return The root class; never `null`.
        @NonNull
        public Class<?> getRoot() {
            return root;
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

    @Generated
    private static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
