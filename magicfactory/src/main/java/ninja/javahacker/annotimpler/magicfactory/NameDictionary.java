package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// Caches per-class dictionaries that determine which type names need fully qualified names
/// to avoid ambiguity when formatting generic signatures.
///
/// When two classes used in the signature of a given declaring class share the same simple
/// name, `NameDictionary` marks both as requiring fully qualified names. This ensures that
/// generated human-readable signatures (e.g. for error messages) are unambiguous.
///
/// A process-wide singleton is available via [#global()]. Additional instances can be created
/// with the public constructor if isolation is required.
///
/// ## Thread safety
///
/// The [#global()] singleton and any explicitly constructed instance are **thread-safe**: the
/// per-class dictionary is built lazily under a `synchronized` lock.
public final class NameDictionary {

    /// The default global instance. Can be used as a singleton, although additional instances can be created.
    @NonNull
    private static final NameDictionary GLOBAL_INSTANCE = new NameDictionary();

    /// The synchronization lock for mutating this instance.
    @NonNull
    private final ReentrantLock lock;

    /// The set of subdictionaries for each class.
    @NonNull
    private final Map<Class<?>, ClassDictionary<?>> map;

    /*
    /// The set of partial subdictionaries, used for locking without blocking when not needed.
    @NonNull
    private final Set<Class<?>> partial;
    */

    /// Creates a new, empty `NameDictionary`. Per-class dictionaries are built lazily on first access.
    public NameDictionary() {
        this.lock = new ReentrantLock();
        this.map = new HashMap<>(10);
        //this.partial = new HashSet<>(10);
    }

    /// Internal dictionary representing a single class.
    private static final class ClassDictionary<T> {

        /// The class represented.
        @NonNull
        private final Class<T> klass;

        /// 
        @NonNull
        private final Set<Class<?>> seen;

        @NonNull
        private final Set<Class<?>> fullNameNeeded;

        @NonNull
        private final Map<String, Class<?>> simpleNamesSeen;

        @SuppressFBWarnings("FII_USE_FUNCTION_IDENTITY") // Can't use Function.identity() without making a mess with generics.
        private ClassDictionary(@NonNull Class<T> k) {
            checkNotNull(k); // Check recognized by lombok.
            this.klass = k;
            this.seen = new HashSet<>(20);
            this.fullNameNeeded = new HashSet<>(20);
            this.simpleNamesSeen = new HashMap<>(20);

            add(k);

            var t = Stream.of(k.getTypeParameters());
            t.forEach(x -> {
                add(x);
            });

            var a = Stream.of(k.getDeclaredMethods());
            var b = Stream.of(k.getMethods());
            var c = Stream.of(k.getDeclaredConstructors());
            var d = Stream.of(k.getConstructors());
            var e = Stream.of(a, b, c, d).flatMap(s -> s);
            e.forEach(x -> {
                add(Methods.getReturnType(x));
                addAll(x.getGenericExceptionTypes());
                addAll(x.getGenericParameterTypes());
                addAll(x.getTypeParameters());
            });

            var f = Stream.of(k.getDeclaredFields());
            var g = Stream.of(k.getFields());
            var h = Stream.concat(f, g);
            h.forEach(x -> {
                add(Methods.getReturnType(x));
            });
        }

        private void addAll(@NonNull Set<Type> partial, @NonNull Type... ts) {
            checkNotNull(partial); // Check recognized by lombok.
            checkNotNull(ts); // Check recognized by lombok.
            for (var t : ts) {
                add(partial, t);
            }
        }

        private void addAll(@NonNull Type... ts) {
            checkNotNull(ts); // Check recognized by lombok.
            addAll(new HashSet<>(10), ts);
        }

        private void add(@NonNull Type t) {
            checkNotNull(t); // Check recognized by lombok.
            add(new HashSet<>(10), t);
        }

        private void add(@NonNull Set<Type> partial, @Nullable Type t) {
            checkNotNull(partial); // Check recognized by lombok.
            if (t == null || partial.contains(t)) return;
            partial.add(t);
            try {
                switch (t) {
                    case Class<?> c -> addClass(partial, c);
                    case ParameterizedType p -> {
                        add(partial, p.getOwnerType());
                        add(partial, p.getRawType());
                        addAll(partial, p.getActualTypeArguments());
                    }
                    case GenericArrayType g -> add(partial, g.getGenericComponentType());
                    case TypeVariable<?> v -> {
                        addAll(partial, v.getBounds());
                    }
                    case WildcardType w -> {
                        addAll(partial, w.getLowerBounds());
                        addAll(partial, w.getUpperBounds());
                    }
                    default -> throw new AssertionError();
                }
            } finally {
                partial.remove(t);
            }
        }

        private void addClass(@NonNull Set<Type> partial, @NonNull Class<?> klass) {
            checkNotNull(partial); // Check recognized by lombok.
            checkNotNull(klass); // Check recognized by lombok.
            if (klass.isArray()) {
                add(partial, klass.getComponentType());
                return;
            }
            if (!seen.add(klass)) return;
            var simple = klass.getSimpleName();
            var conflict = simpleNamesSeen.get(simple);
            if (conflict == null) {
                simpleNamesSeen.put(simple, klass);
            } else {
                if (conflict != void.class) {
                    simpleNamesSeen.put(simple, void.class);
                    simpleNamesSeen.put(conflict.getName(), conflict);
                    fullNameNeeded.add(conflict);
                }
                simpleNamesSeen.put(klass.getName(), klass);
                fullNameNeeded.add(klass);
            }
        }

        private void formatParameterTypes(@NonNull Executable method, @NonNull StringBuilder sb) {
            checkNotNull(method); // Check recognized by lombok.
            checkNotNull(sb); // Check recognized by lombok.
            var paramTypes = method.getGenericParameterTypes();
            for (var i = 0; i < paramTypes.length; i++) {
                if (i > 0) sb.append(", ");
                formatType(paramTypes[i], sb);
            }
        }

        private void formatType(@NonNull Type type, @NonNull StringBuilder sb) {
            checkNotNull(type); // Check recognized by lombok.
            checkNotNull(sb); // Check recognized by lombok.
            TypeName.formatType(type, fullNameNeeded, sb);
        }

        private void appendTypeParameters(@NonNull Executable what, @NonNull StringBuilder sb) {
            checkNotNull(what); // Check recognized by lombok.
            checkNotNull(sb); // Check recognized by lombok.
            sb.append('<');
            var typeParams = what.getTypeParameters();
            for (var i = 0; i < typeParams.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(typeParams[i].getName());
                var bounds = typeParams[i].getBounds();
                assertNonZero(bounds.length);
                var s = (bounds[0] == Object.class) ? 1 : 0;
                if (bounds.length - s > 0) {
                    sb.append(" extends ");
                    for (var j = s; j < bounds.length; j++) {
                        if (j > s) sb.append(" & ");
                        formatType(bounds[j], sb);
                    }
                }
            }
            sb.append("> ");
        }

        @NonNull
        private String getSimplifiedGenericString(@NonNull Executable what, boolean withClassName) {
            checkNotNull(what); // Check recognized by lombok.
            assertSame(what.getDeclaringClass(), klass);
            var sb = new StringBuilder(256);

            if (withClassName) {
                formatType(klass, sb);
                sb.append('/');
            }

            // Type parameters.
            if (what.getTypeParameters().length > 0) appendTypeParameters(what, sb);

            // Return type.
            formatType(Methods.getReturnType(what), sb);

            // Add the class name and method name if needed.
            if (what instanceof Method) {
                var className = what.getDeclaringClass().getSimpleName();
                var methodName = what.getName();
                sb.append(' ').append(className).append('.').append(methodName);
            }

            // Parameters.
            sb.append('(');
            formatParameterTypes(what, sb);
            sb.append(')');

            return sb.toString();
        }

        @NonNull
        private String getSimplifiedGenericString(@NonNull Field field, boolean withClassName) {
            checkNotNull(field); // Check recognized by lombok.
            assertSame(field.getDeclaringClass(), klass);
            var sb = new StringBuilder(256);

            if (withClassName) {
                formatType(klass, sb);
                sb.append('/');
            }

            // Return type.
            formatType(Methods.getReturnType(field), sb);
            sb.append(' ');

            // Class name.
            formatType(field.getDeclaringClass(), sb);

            // Method name and return.
            return sb.append('.').append(field.getName()).toString();
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T> ClassDictionary<T> getFor(@NonNull Class<T> klass) {
        checkNotNull(klass); // Check recognized by lombok.
        try {
            lock.lock();
            var d = (ClassDictionary<T>) map.get(klass);
            if (d != null) return d;
            d = new ClassDictionary<>(klass); // TODO: Slow inside synchronized!
            map.put(klass, d);
            return d;
        } finally {
            lock.unlock();
        }
    }

    /// Returns a simplified generic signature string for the given method or constructor.
    ///
    /// The signature includes type parameters, return type, and parameter types, using simple
    /// class names where unambiguous. If `withClassName` is `true`, the declaring class name
    /// is prepended followed by `/`.
    ///
    /// @param what The method or constructor; must not be `null`.
    /// @param withClassName Whether to prefix the result with the declaring class name.
    /// @return A human-readable signature string; never `null`.
    /// @throws IllegalArgumentException If `what` is `null`.
    @NonNull
    public String getSimplifiedGenericString(@NonNull Executable what, boolean withClassName) {
        var cl = getFor(what.getDeclaringClass());
        return cl.getSimplifiedGenericString(what, withClassName);
    }

    /// Returns a simplified generic signature string for the given field.
    ///
    /// The signature includes the field type and name, using simple class names where
    /// unambiguous. If `withClassName` is `true`, the declaring class name is prepended
    /// followed by `/`.
    ///
    /// @param field The field; must not be `null`.
    /// @param withClassName Whether to prefix the result with the declaring class name.
    /// @return A human-readable field signature string; never `null`.
    /// @throws IllegalArgumentException If `field` is `null`.
    @NonNull
    public String getSimplifiedGenericString(@NonNull Field field, boolean withClassName) {
        var cl = getFor(field.getDeclaringClass());
        return cl.getSimplifiedGenericString(field, withClassName);
    }

    /// Returns the process-wide singleton `NameDictionary`.
    ///
    /// @return The global instance; never `null`.
    @NonNull
    public static NameDictionary global() {
        return GLOBAL_INSTANCE;
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    public boolean equals(@Nullable Object other) {
        return other == this;
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return "NameDictionary";
    }

    @Generated
    private static void assertSame(Object a, Object b) {
        if (a != b) throw new AssertionError();
    }

    @Generated
    private static void assertNonZero(int a) {
        if (a == 0) throw new AssertionError();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
