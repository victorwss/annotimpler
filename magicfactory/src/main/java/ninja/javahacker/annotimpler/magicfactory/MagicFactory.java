package ninja.javahacker.annotimpler.magicfactory;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public final class MagicFactory<E> {

    @NonNull
    private final Class<E> klass;

    @NonNull
    private final MethodWrapper<E, Object> wrapper;

    private MagicFactory(@NonNull Class<E> klass) throws CreatorSelectionException {
        checkNotNull(klass);
        this.klass = klass;
        this.wrapper = creatorFor(klass);
    }

    @NonNull
    public static <E> MagicFactory<E> of(@NonNull Class<E> klass) throws CreatorSelectionException {
        return new MagicFactory<>(klass);
    }

    @NonNull
    private <W extends MethodWrapper<?, ?>> W checkOk(@NonNull W wrapper) throws CreatorSelectionException {
        checkNotNull(wrapper);
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
        checkNotNull(klass);
        var methods = klass.getDeclaredMethods();
        var fields = klass.getDeclaredFields();
        var constructors = klass.getDeclaredConstructors();

        // Busca construtores, métodos e campos anotados com @Creator.
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

        // Para Records e beans, normalmente há apenas um construtor canônico.
        if (constructors.length == 1) {
            return checkOk(MethodWrapper.<E>of(constructors[0])).eraseU();
        }

        // Se houver múltiplos construtores, tenta encontrar o canônico.
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

        // Tenta o construtor padrão.
        try {
            var wrap = MethodWrapper.of(klass.getDeclaredConstructor());
            return checkOk(wrap).eraseU();
        } catch (NoSuchMethodException e) {
            var msg = "Failed to determine how to create an instance of " + klass.getSimpleName() + ".";
            throw new CreatorSelectionException(msg, klass);
        }
    }

    @NonNull
    public E create(@NonNull Object... args) throws CreationException {
        try {
            var ret = wrapper.call(args);
            if (ret == null) throw new CreationException("Creator of " + klass.getSimpleName() + " produced null.", klass);
            return ret;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new CreationException("Creator of " + klass.getSimpleName() + " doesn't work.", e, klass);
        } catch (IllegalArgumentException e) {
            throw new CreationException("Creator of " + klass.getSimpleName() + " was called with the wrong arguments.", e, klass);
        } catch (InvocationTargetException e) {
            throw new CreationException("The instantiation of " + klass.getSimpleName() + " threw an exception.", e.getCause(), klass);
        }
    }

    @NonNull
    public Class<E> getReturnType() {
        return klass;
    }

    @NonNull
    public List<Type> getParameterTypes() {
        return wrapper.getParameterTypes();
    }

    @NonNull
    public List<Parameter> getParameters() {
        return wrapper.getParameters();
    }

    @NonNull
    public int arity() {
        return wrapper.arity();
    }

    public static class CreatorSelectionException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        @NonNull
        private final Class<?> root;

        public CreatorSelectionException(@NonNull String message, @NonNull Class<?> root) {
            List.of(message, root); // Force lombok put the null-checks before the constructor call.
            super(message);
            this.root = root;
        }

        public CreatorSelectionException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> root) {
            List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
            super(message, cause);
            this.root = root;
        }

        @NonNull
        public Class<?> getRoot() {
            return root;
        }
    }

    public static class CreationException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        @NonNull
        private final Class<?> root;

        public CreationException(@NonNull String message, @NonNull Class<?> root) {
            List.of(message, root); // Force lombok put the null-checks before the constructor call.
            super(message);
            this.root = root;
        }

        public CreationException(@NonNull String message, @NonNull Throwable cause, @NonNull Class<?> root) {
            List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
            super(message, cause);
            this.root = root;
        }

        @NonNull
        public Class<?> getRoot() {
            return root;
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
