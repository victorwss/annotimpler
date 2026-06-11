package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.annotation.Annotation;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// A unified reflective wrapper over a [Constructor], [Method], or [Field] that presents a
/// consistent API for invocation, type inspection, and annotation lookup.
///
/// Instances are created via the static factory methods:
/// - [#of(Method)] — wraps a static or instance method
/// - [#of(Constructor)] — wraps a constructor
/// - [#of(Executable)] — dispatches to the appropriate overload
/// - [#getter(Field)] — wraps a field as a zero-argument getter
/// - [#value(Object)] — wraps a constant value as a no-arg "method"
///
/// The type parameter `E` is the return type of the wrapped member. The type parameter `U`
/// is the underlying reflective object type (e.g. `Method`, `Constructor<E>`, or `Field`).
///
/// @param <E> the return type of the wrapped member
/// @param <U> the underlying reflective object type
public interface MethodWrapper<E, U> {

    /// Invokes the wrapped member with the given arguments.
    ///
    /// For **static** members, `params` must match the declared parameter list.
    /// For **instance** members, `params[0]` must be the receiver object and subsequent
    /// elements the method arguments.
    ///
    /// @param params the arguments (and optional receiver); must not be `null`
    /// @return the result of the invocation, or `null` if the member has a `void` return type
    /// @throws IllegalAccessException     if the underlying member is not accessible
    /// @throws InstantiationException     if a constructor target class is abstract
    /// @throws InvocationTargetException  if the underlying member throws an exception
    /// @throws IllegalArgumentException   if `params` is `null` or the arguments do not match
    ///         the member's signature
    @Nullable
    public E call(@NonNull Object... params) throws IllegalAccessException, InstantiationException, InvocationTargetException;

    /// Returns the generic parameter types of this wrapper, in declaration order.
    ///
    /// @return an unmodifiable list of parameter types; never `null`
    @NonNull
    public List<Type> getParameterTypes();

    /// Returns the [Parameter] objects of this wrapper, in declaration order.
    ///
    /// @return an unmodifiable list of parameters; never `null`
    @NonNull
    public List<Parameter> getParameters();

    /// Returns the generic return type of the wrapped member.
    ///
    /// For constructors, this is the declaring class. For fields, this is the field's generic
    /// type.
    ///
    /// @return the return type; never `null`
    @NonNull
    public Type getReturnType();

    /// Returns the declaring class of the wrapped member if it is an instance (non-static)
    /// member, or an empty optional if it is static.
    ///
    /// @return an optional containing the receiver type, or empty for static members;
    ///         never `null`
    @NonNull
    public Optional<Class<?>> getInstanceType();

    /// Returns the annotation of the specified type on the wrapped member, if present.
    ///
    /// @param <A>       the annotation type
    /// @param annoClass the annotation class token; must not be `null`
    /// @return an optional containing the annotation, or empty if not present; never `null`
    /// @throws IllegalArgumentException if `annoClass` is `null`
    @NonNull
    public <A extends Annotation> Optional<A> getAnnotation(@NonNull Class<A> annoClass);

    /// Returns the underlying reflective object (e.g. a [Method], [Constructor], or [Field]).
    ///
    /// @return the wrapped reflective object, or `null` for constant-value wrappers
    @Nullable
    public U unwrap();

    /// Returns `true` if the wrapped member has the `public` modifier.
    ///
    /// @return `true` if public
    public boolean isPublic();

    /// Returns `true` if the wrapped member is static, or always `true` for constructors.
    ///
    /// @return `true` if static
    public boolean isStatic();

    /// Returns `true` if the wrapped member is abstract (only applicable to methods in
    /// abstract or interface types), or `true` for constructors of abstract classes.
    ///
    /// @return `true` if abstract
    public boolean isAbstract();

    /// Returns the number of formal parameters of this wrapper.
    ///
    /// Equivalent to `getParameterTypes().size()`.
    ///
    /// @return the arity, zero or positive
    public default int arity() {
        return getParameterTypes().size();
    }

    /// Returns a capitalized form of [Object#toString()], suitable for use at the start of a
    /// sentence.
    ///
    /// @return the string representation with the first character upper-cased; never `null`
    @NonNull
    public default String toStringUp() {
        var x = this.toString();
        return x.substring(0, 1).toUpperCase(Locale.ROOT) + x.substring(1);
    }

    /// Returns `true` if the annotation of the given type is present on the wrapped member.
    ///
    /// Equivalent to `getAnnotation(annoClass).isPresent()`.
    ///
    /// @param annoClass the annotation class token; must not be `null`
    /// @return `true` if the annotation is present
    /// @throws IllegalArgumentException if `annoClass` is `null`
    public default boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annoClass) {
        return getAnnotation(annoClass).isPresent();
    }

    /// Builds a map from parameter name (or `"this"` for the receiver) to the corresponding
    /// argument value, validating counts and types.
    ///
    /// For static members, `args.length` must equal [#arity()].
    /// For instance members, `args.length` must equal `arity() + 1`, and `args[0]` must be
    /// assignable to the receiver type.
    ///
    /// @param args the arguments (including optional receiver); must not be `null`
    /// @return a [LinkedHashMap] mapping names to values; never `null`
    /// @throws IllegalArgumentException if `args` is `null`, if the length does not match,
    ///         or if any argument type is incompatible
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
            if (a == null) {
                if (pt.isPrimitive()) throw new IllegalArgumentException();
            } else {
                if (!WrapperClass.wrap(pt).isInstance(a)) throw new IllegalArgumentException();
            }
            map.put(p.getName(), a);
        }
        return map;
    }

    /// Returns this wrapper cast to `MethodWrapper<E, Object>`, erasing the `U` type
    /// parameter.
    ///
    /// This unchecked cast is safe for use in contexts where the underlying type `U` is not
    /// needed.
    ///
    /// @return this wrapper with `U` erased to `Object`; never `null`
    @NonNull
    @SuppressWarnings("unchecked")
    public default MethodWrapper<E, Object> eraseU() {
        return (MethodWrapper<E, Object>) this;
    }

    /// Creates a wrapper for the given [Method].
    ///
    /// If the method is **static**, `call` expects `args` matching the declared parameters.
    /// If it is an **instance** method, `args[0]` must be the receiver.
    ///
    /// @param <E>  the return type of the method
    /// @param what the method to wrap; must not be `null`
    /// @return a new `MethodWrapper`; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
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

    /// Creates a wrapper for the given [Constructor].
    ///
    /// @param <E>  the type constructed
    /// @param what the constructor to wrap; must not be `null`
    /// @return a new `MethodWrapper`; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
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

    /// Creates a wrapper for the given [Executable], dispatching to [#of(Method)] or
    /// [#of(Constructor)] as appropriate.
    ///
    /// @param <E>  the return type
    /// @param what the executable to wrap; must not be `null`
    /// @return a new `MethodWrapper`; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
    @NonNull
    @Generated
    @SuppressWarnings("unchecked")
    public static <E> MethodWrapper<E, ?> of(@NonNull Executable what) {
        if (what instanceof Method m) return MethodWrapper.<E>of(m);
        if (what instanceof Constructor<?> c) return of((Constructor<E>) c);
        throw new AssertionError();
    }

    /// Creates a getter wrapper for the given [Field].
    ///
    /// For a **static** field, `call` expects an empty `args` array.
    /// For an **instance** field, `args[0]` must be the field's declaring class instance.
    ///
    /// @param <E>  the field type
    /// @param what the field to wrap as a getter; must not be `null`
    /// @return a new `MethodWrapper`; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
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
            assertEquals(args.length, stt ? 0 : 1);
            return (E) what.get(stt ? null : args[0]);
        };
        return new SimpleMethodWrapper<>(what, params, types, rt, it, str, stt, false, pub, call, what::getAnnotation);
    }

    /// Creates a no-argument constant wrapper that always returns `what`.
    ///
    /// Typically used for enum singletons. The wrapper reports itself as public and static,
    /// with zero parameters and the runtime class of `what` as its return type.
    ///
    /// @param <E>  the value type
    /// @param what the constant value to wrap; must not be `null`
    /// @return a new `MethodWrapper`; never `null`
    /// @throws IllegalArgumentException if `what` is `null`
    @NonNull
    public static <E> MethodWrapper<E, E> value(@NonNull E what) {
        var params = SimpleMethodWrapper.EMPTY1;
        var types = SimpleMethodWrapper.EMPTY2;
        var ann = SimpleMethodWrapper.NULL_ANNOTATOR;
        var rt = what.getClass();
        var str = String.valueOf(what);
        SimpleMethodWrapper.Call<E> call = args -> {
            assertEquals(args.length, 0);
            return what;
        };
        return new SimpleMethodWrapper<>(what, params, types, rt, Optional.empty(), str, true, false, true, call, ann) {
            @Override
            public String toStringUp() {
                return this.toString();
            }
        };
    }

    @Generated
    private static void assertEquals(int a, int b) {
        if (a != b) throw new AssertionError();
    }
}
