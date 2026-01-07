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
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> annoClass);

    @Nullable
    public U unwrap();

    public boolean isPublic();

    public boolean isStatic();

    public boolean isAbstract();

    public default int arity() {
        return getParameterTypes().size();
    }

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
        var ar = arity();
        if (pp.size() != ar) throw new IllegalArgumentException();
        var map = new LinkedHashMap<String, Object>(ar);
        for (var i = 0; i < ar; i++) {
            var p = pp.get(i);
            map.put(p.getName(), args[i]);
        }
        return map;
    }

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
        return new SimpleMethodWrapper<>(what, params, types, rt, str, stt, abs, pub, args -> {
            if (args.length == 0 && !stt) throw new IllegalArgumentException("Bad parameter count.");
            var inst = stt ? null : args[0];
            Object[] nargs;
            if (stt) {
                nargs = args;
            } else {
                nargs = new Object[args.length - 1];
                System.arraycopy(args, 1, nargs, 0, nargs.length);
            }
            return (E) what.invoke(stt ? null : args[0], nargs);
        }, what::getAnnotation);
    }

    @NonNull
    public static <E> MethodWrapper<E, Constructor<E>> of(@NonNull Constructor<E> what) {
        var params = List.of(what.getParameters());
        var types = List.of(what.getGenericParameterTypes());
        var rt = what.getDeclaringClass();
        var str = "constructor " + NameDictionary.global().getSimplifiedGenericString(what, false);
        var pub = Modifier.isPublic(what.getModifiers());
        var abs = Modifier.isAbstract(what.getDeclaringClass().getModifiers());
        return new SimpleMethodWrapper<>(what, params, types, rt, str, true, abs, pub, args -> what.newInstance(args), what::getAnnotation);
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
        var rt = what.getGenericType();
        var str = "field " + NameDictionary.global().getSimplifiedGenericString(what, false);
        var stt = Modifier.isStatic(what.getModifiers());
        var pub = Modifier.isPublic(what.getModifiers());
        return new SimpleMethodWrapper<>(what, SimpleMethodWrapper.EMPTY1, SimpleMethodWrapper.EMPTY2, rt, str, stt, false, pub, args -> {
            if (args.length != (stt ? 0 : 1)) throw new IllegalArgumentException("Bad parameter count.");
            return (E) what.get(stt ? null : args[0]);
        }, what::getAnnotation);
    }

    @NonNull
    public static <E> MethodWrapper<E, E> value(@NonNull E what) {
        var rt = what.getClass();
        var str = String.valueOf(what);
        return new SimpleMethodWrapper<>(what, SimpleMethodWrapper.EMPTY1, SimpleMethodWrapper.EMPTY2, rt, str, true, false, true, args -> {
            if (args.length != 0) throw new IllegalArgumentException("Bad parameter count.");
            return what;
        }, SimpleMethodWrapper.NULL_ANNOTATOR) {
            @Override
            public String toStringUp() {
                return this.toString();
            }
        };
    }
}
