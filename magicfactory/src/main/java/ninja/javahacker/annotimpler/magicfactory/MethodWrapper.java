package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.annotation.Annotation;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

public interface MethodWrapper<E, U> {

    @Nullable
    public E call(@NonNull Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException;

    @NonNull
    public List<Type> getParameterTypes();

    @NonNull
    public List<Parameter> getParameters();

    @NonNull
    public Type getReturnType();

    @NonNull
    public Optional<Class<?>> getInstanceType();

    @NonNull
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> annoClass);

    @Nullable
    public U unwrap();

    public boolean isPublic();

    public boolean isStatic();

    public boolean isAbstract();

    public default int arity() {
        return getParameterTypes().size();
    }

    @NonNull
    public default String toStringUp() {
        var x = this.toString();
        return x.substring(0, 1).toUpperCase(Locale.ROOT) + x.substring(1);
    }

    public default boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annoClass) {
        return getAnnotation(annoClass).isPresent();
    }

    @NonNull
    public default Map<String, Object> paramMap(@NonNull Object... args) {
        var pp = getParameters();
        var base = isStatic() ? 0 : 1;
        if (pp.size() + base != args.length) throw new IllegalArgumentException();
        var map = new LinkedHashMap<String, Object>(args.length + base);
        if (base == 1) {
            var it = getInstanceType().orElseThrow(AssertionError::new);
            var a = args[0];
            if (!WrapperClass.wrap(it).isInstance(a)) throw new IllegalArgumentException(it + "---" + a);
            map.put("this", a);
        }
        for (var i = base; i < args.length; i++) {
            var p = pp.get(i - base);
            var pt = p.getType();
            var a = args[i];
            if (a == null ? pt.isPrimitive() : !WrapperClass.wrap(pt).isInstance(a)) throw new IllegalArgumentException();
            map.put(p.getName(), a);
        }
        return map;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public default MethodWrapper<E, Object> eraseU() {
        return (MethodWrapper<E, Object>) this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> MethodWrapper<E, Method> of(@NonNull Method what) {
        var params = List.of(what.getParameters());
        var types = List.of(what.getGenericParameterTypes());
        var rt = what.getGenericReturnType();
        var str = "method " + NameDictionary.global().getSimplifiedGenericString(what, false);
        var stt = Modifier.isStatic(what.getModifiers());
        var abs = Modifier.isAbstract(what.getModifiers());
        var pub = Modifier.isPublic(what.getModifiers());
        Optional<Class<?>> it = stt ? Optional.empty() : Optional.of(what.getDeclaringClass());
        SimpleMethodWrapper.Call<E> icall = args -> {
            var inst = args[0];
            var nargs = new Object[args.length - 1];
            System.arraycopy(args, 1, nargs, 0, nargs.length);
            return (E) what.invoke(inst, nargs);
        };
        SimpleMethodWrapper.Call<E> scall = args -> {
            return (E) what.invoke(null, args);
        };
        return new SimpleMethodWrapper<>(what, params, types, rt, it, str, stt, abs, pub, stt ? scall : icall, what::getAnnotation);
    }

    @NonNull
    public static <E> MethodWrapper<E, Constructor<E>> of(@NonNull Constructor<E> what) {
        var params = List.of(what.getParameters());
        var types = List.of(what.getGenericParameterTypes());
        var rt = what.getDeclaringClass();
        var str = "constructor " + NameDictionary.global().getSimplifiedGenericString(what, false);
        var pub = Modifier.isPublic(what.getModifiers());
        var abs = Modifier.isAbstract(what.getDeclaringClass().getModifiers());
        SimpleMethodWrapper.Call<E> call = args -> what.newInstance(args);
        return new SimpleMethodWrapper<>(what, params, types, rt, Optional.empty(), str, true, abs, pub, call, what::getAnnotation);
    }

    @NonNull
    @Generated
    @SuppressWarnings("unchecked")
    public static <E> MethodWrapper<E, ?> of(@NonNull Executable what) {
        if (what instanceof Method m) return MethodWrapper.<E>of(m);
        if (what instanceof Constructor<?> c) return of((Constructor<E>) c);
        throw new AssertionError();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> MethodWrapper<E, Field> getter(@NonNull Field what) {
        var params = SimpleMethodWrapper.EMPTY1;
        var types = SimpleMethodWrapper.EMPTY2;
        var rt = what.getGenericType();
        var str = "field " + NameDictionary.global().getSimplifiedGenericString(what, false);
        var stt = Modifier.isStatic(what.getModifiers());
        var pub = Modifier.isPublic(what.getModifiers());
        Optional<Class<?>> it = stt ? Optional.empty() : Optional.of(what.getDeclaringClass());
        SimpleMethodWrapper.Call<E> call = args -> {
            if (args.length != (stt ? 0 : 1)) throw new AssertionError();
            return (E) what.get(stt ? null : args[0]);
        };
        return new SimpleMethodWrapper<>(what, params, types, rt, it, str, stt, false, pub, call, what::getAnnotation);
    }

    @NonNull
    public static <E> MethodWrapper<E, E> value(@NonNull E what) {
        var params = SimpleMethodWrapper.EMPTY1;
        var types = SimpleMethodWrapper.EMPTY2;
        var ann = SimpleMethodWrapper.NULL_ANNOTATOR;
        var rt = what.getClass();
        var str = String.valueOf(what);
        SimpleMethodWrapper.Call<E> call = args -> {
            if (args.length != 0) throw new AssertionError();
            return what;
        };
        return new SimpleMethodWrapper<>(what, params, types, rt, Optional.empty(), str, true, false, true, call, ann) {
            @Override
            public String toStringUp() {
                return this.toString();
            }
        };
    }
}
