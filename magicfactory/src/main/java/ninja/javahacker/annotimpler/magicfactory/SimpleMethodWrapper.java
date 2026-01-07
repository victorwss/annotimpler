package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.annotation.Annotation;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

@PackagePrivate
class SimpleMethodWrapper<E, U> implements MethodWrapper<E, U> {

    public static final List<Parameter> EMPTY1 = List.of();

    public static final List<Type> EMPTY2 = List.of();

    @PackagePrivate
    static final Annotator NULL_ANNOTATOR = new Annotator() {
        @Nullable
        @Override
        public <A extends Annotation> A getAnnotation(@NonNull Class<A> annoClass) {
            if (annoClass == null) throw new AssertionError();
            return null;
        }
    };

    private final U what;
    private final List<Parameter> params;
    private final List<Type> types;
    private final Type rt;
    private final String str;
    private final boolean staticModifier;
    private final boolean abstractModifier;
    private final boolean publicModifier;
    private final Call<E> caller;
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

    @PackagePrivate
    SimpleMethodWrapper(
            @NonNull U what,
            @NonNull List<Parameter> params,
            @NonNull List<Type> types,
            @NonNull Type rt,
            @NonNull String str,
            boolean staticModifier,
            boolean abstractModifier,
            boolean publicModifier,
            @NonNull Call<E> caller,
            @NonNull Annotator annotator
    )
    {
        if (what == null) throw new AssertionError();
        if (params == null) throw new AssertionError();
        if (types == null) throw new AssertionError();
        if (rt == null) throw new AssertionError();
        if (str == null) throw new AssertionError();
        if (caller == null) throw new AssertionError();
        if (annotator == null) throw new AssertionError();
        this.what = what;
        this.params = List.copyOf(params);
        this.types = List.copyOf(types);
        this.rt = rt;
        this.str = str;
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
}
