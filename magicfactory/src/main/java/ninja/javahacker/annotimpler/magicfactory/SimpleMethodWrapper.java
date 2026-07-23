package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.annotation.Annotation;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// A simple implementation of [MethodWrapper].
/// This wraps a callable object (a method, constructor or lambda acting as a getter)
/// and decorates it with some cached properties.
///
/// @param <E> {@inheritDoc}
/// @param <U> {@inheritDoc}
@PackagePrivate
final class SimpleMethodWrapper<E, U> implements MethodWrapper<E, U> {

    /// An empty list of parameters.
    @NonNull
    public static final List<Parameter> EMPTY1 = List.of();

    /// An empty list of types.
    @NonNull
    public static final List<Type> EMPTY2 = List.of();

    /// An annotator that does nothing and returns `null`.
    @NonNull
    @SuppressWarnings("Convert2Lambda") // Can't use a lambda because the return type is generic.
    public static final Annotator NULL_ANNOTATOR = new Annotator() {
        /// Always returns `null`, regardless of `annoClass`.
        @Nullable
        @Override
        public <A extends Annotation> A getAnnotation(@NonNull Class<A> annoClass) {
            checkNotNull(annoClass); // Check recognized by lombok.
            return null;
        }
    };

    /// The object wrapped in this instance. Likely a method, a constructor or a lambda acting as a getter.
    @NonNull
    private final U what;

    /// The parameters of the wrapped object.
    @NonNull
    private final List<Parameter> params;

    /// The parameter types of the wrapped object.
    @NonNull
    private final List<Type> types;

    /// The return type of the wrapped object.
    @NonNull
    private final Type rt;

    /// The instance type of the wrapped object, if not static. I.e., on which object the method is called.
    @NonNull
    private final Optional<Class<?>> it;

    /// Stores the output for [#toString()] method.
    @NonNull
    private final String str;

    /// Stores the output for [#toStringUp()] method. Frequently the same as [#str], but not always.
    @NonNull
    private final String upStr;

    /// Flags if the wrapped object is static.
    private final boolean staticModifier;

    /// Flags if the wrapped object is abstract.
    private final boolean abstractModifier;

    /// Flags if the wrapped object is public.
    private final boolean publicModifier;

    /// An object responsible for invoking the wrapped object (be it a method, a constructor, or a getter-like lambda).
    @NonNull
    private final Invoker<E> caller;

    /// An object responsible for retrieving the annotations of the wrapped object.
    @NonNull
    private final Annotator annotator;

    /// Encapsulates an invocation to a method, constructor or getter-like lambda.
    @FunctionalInterface
    public static interface Invoker<E> {

        /// Makes a reflective the call to the represented method, constructor or getter-like lambda.
        ///
        /// @param params The parameters used in the call.
        /// @return The result of the call, or `null` if the method is `void`-typed.
        /// @throws IllegalAccessException If this object wraps a non-public method or constructor or is encapsulated
        ///         in an innacessible module.
        /// @throws InstantiationException If this object wraps a constructor of an abstract class.
        /// @throws IllegalArgumentException If `params` is `null` or if there is a mismatch between the received
        ///         argument and the expected ones.
        /// @throws InvocationTargetException If the invoked method or constructor throws an arbitrary exception.
        @Nullable
        public E call(@NonNull Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException;
    }

    /// Encapsulates the retrieval operation of annotations of a method, constructor or getter-like lambda.
    @FunctionalInterface
    public static interface Annotator {

        /// Retrieve an annotation instance from the wrapped method or constructor from a given annotation class.
        ///
        /// @param <A> The type of the annotation that should be retrieved.
        /// @param annoClass The annotation type that should be retrieved.
        /// @return The retrieved annotation, or `null` if none found because it doesn't exist.
        /// @throws IllegalArgumentException If `annoClass` is `null`.
        @Nullable
        public <A extends Annotation> A getAnnotation(@NonNull Class<A> annoClass);
    }

    /// Creates a new instance by receiving a bunch of parameters to populate it.
    ///
    /// @param what The object wrapped in this instance. Likely a method, a constructor or a lambda acting as a getter.
    /// @param params The parameters of the wrapped object.
    /// @param types The parameter types of the wrapped object.
    /// @param rt The return type of the wrapped object.
    /// @param it The instance type of the wrapped object, if not static. I.e., on which object the method is called.
    /// @param str The output for [#toString()] method.
    /// @param capitalize If the `str` should be capitalized for the [#toStringUp()] method.
    /// @param staticModifier Flags if the wrapped object is static.
    /// @param abstractModifier Flags if the wrapped object is abstract.
    /// @param publicModifier Flags if the wrapped object is public.
    /// @param caller An object responsible for invoking the wrapped object.
    /// @param annotator An object responsible for retrieving the annotations of the wrapped object.
    /// @throws IllegalArgumentException If any parameter is `null`.
    public SimpleMethodWrapper(
            @NonNull U what,
            @NonNull List<Parameter> params,
            @NonNull List<Type> types,
            @NonNull Type rt,
            @NonNull Optional<Class<?>> it,
            @NonNull String str,
            boolean capitalize,
            boolean staticModifier,
            boolean abstractModifier,
            boolean publicModifier,
            @NonNull Invoker<E> caller,
            @NonNull Annotator annotator
    )
    {
        checkNotNull(what); // Check recognized by lombok.
        checkNotNull(params); // Check recognized by lombok.
        checkNotNull(types); // Check recognized by lombok.
        checkNotNull(rt); // Check recognized by lombok.
        checkNotNull(it); // Check recognized by lombok.
        checkNotNull(str); // Check recognized by lombok.
        checkNotNull(caller); // Check recognized by lombok.
        checkNotNull(annotator); // Check recognized by lombok.

        this.what = what;
        this.params = List.copyOf(params);
        this.types = List.copyOf(types);
        this.rt = rt;
        this.it = it;
        this.str = str;
        this.upStr = capitalize ? str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1) : str;
        this.staticModifier = staticModifier;
        this.abstractModifier = abstractModifier;
        this.publicModifier = publicModifier;
        this.caller = caller;
        this.annotator = annotator;
    }

    /// {@inheritDoc}
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public E call(@NonNull Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        this.paramMap(params);
        return caller.call(params);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public U unwrap() {
        return what;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Parameter> getParameters() {
        return params;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Type> getParameterTypes() {
        return types;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Type getReturnType() {
        return rt;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Class<?>> getInstanceType() {
        return it;
    }

    /// {@inheritDoc}
    @Override
    public boolean isPublic() {
        return publicModifier;
    }

    /// {@inheritDoc}
    @Override
    public boolean isStatic() {
        return staticModifier;
    }

    /// {@inheritDoc}
    @Override
    public boolean isAbstract() {
        return abstractModifier;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> annoClass) {
        return Optional.ofNullable(annotator.getAnnotation(annoClass));
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    public boolean equals(@Nullable Object other) {
        return other instanceof SimpleMethodWrapper<?, ?> g && Objects.equals(g.unwrap(), what);
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return what.hashCode();
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return str;
    }

    /// {@inheritDoc}
    @Override
    public String toStringUp() {
        return upStr;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
