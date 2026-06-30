package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.annotation.Annotation;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

@PackagePrivate
final class SimpleMethodWrapper<E, U> implements MethodWrapper<E, U> {

    @NonNull
    public static final List<Parameter> EMPTY1 = List.of();

    @NonNull
    public static final List<Type> EMPTY2 = List.of();

    @NonNull
    @SuppressWarnings("Convert2Lambda") // Can't use a lambda because the return type is generic.
    public static final Annotator NULL_ANNOTATOR = new Annotator() {
        @Nullable
        @Override
        public <A extends Annotation> A getAnnotation(@NonNull Class<A> annoClass) {
            checkNotNull(annoClass); // Check recognized by lombok.
            return null;
        }
    };

    @NonNull
    private final U what;

    @NonNull
    private final List<Parameter> params;

    @NonNull
    private final List<Type> types;

    @NonNull
    private final Type rt;

    @NonNull
    private final Optional<Class<?>> it;

    @NonNull
    private final String str;

    @NonNull
    private final String upStr;

    private final boolean staticModifier;

    private final boolean abstractModifier;

    private final boolean publicModifier;

    @NonNull
    private final Call<E> caller;

    @NonNull
    private final Annotator annotator;

    @FunctionalInterface
    public static interface Call<E> {
        @Nullable
        public E call(@NonNull Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException;
    }

    @FunctionalInterface
    public static interface Annotator {
        @Nullable
        public <A extends Annotation> A getAnnotation(@NonNull Class<A> annoClass);
    }

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
            @NonNull Call<E> caller,
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

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public E call(@NonNull Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        this.paramMap(params);
        return caller.call(params);
    }

    @NonNull
    @Override
    public U unwrap() {
        return what;
    }

    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Parameter> getParameters() {
        return params;
    }

    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<Type> getParameterTypes() {
        return types;
    }

    @NonNull
    @Override
    public Type getReturnType() {
        return rt;
    }

    @NonNull
    @Override
    public Optional<Class<?>> getInstanceType() {
        return it;
    }

    @Override
    public boolean isPublic() {
        return publicModifier;
    }

    @Override
    public boolean isStatic() {
        return staticModifier;
    }

    @Override
    public boolean isAbstract() {
        return abstractModifier;
    }

    @NonNull
    @Override
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> annoClass) {
        return Optional.ofNullable(annotator.getAnnotation(annoClass));
    }

    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    public boolean equals(@Nullable Object other) {
        return other instanceof SimpleMethodWrapper<?, ?> g && Objects.equals(g.unwrap(), what);
    }

    @Override
    public int hashCode() {
        return what.hashCode();
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public String toStringUp() {
        return upStr;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
