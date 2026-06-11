package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
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

    @NonNull
    private static final NameDictionary GLOBAL_INSTANCE = new NameDictionary();

    @NonNull
    private final Object lock;

    @NonNull
    private final Map<Class<?>, ClassDictionary<?>> map;

    /// Creates a new, empty `NameDictionary`. Per-class dictionaries are built lazily on
    /// first access.
    public NameDictionary() {
        this.lock = new Object();
        this.map = new HashMap<>(10);
    }

    private static final class ClassDictionary<T> {
        @NonNull
        private final Class<T> klass;

        @NonNull
        private final Set<Class<?>> seen;

        @NonNull
        private final Set<Class<?>> fullNameNeeded;

        @NonNull
        private final Map<String, Class<?>> simpleNamesSeen;

        private ClassDictionary(@NonNull Class<T> k) {
            checkNotNull(k); // Check recognized by lombok.
            this.klass = k;
            this.seen = new HashSet<>(20);
            this.fullNameNeeded = new HashSet<>(20);
            this.simpleNamesSeen = new HashMap<>(20);
            var a = Stream.of(k.getDeclaredMethods());
            var b = Stream.of(k.getMethods());
            var c = Stream.of(k.getDeclaredConstructors());
            var d = Stream.of(k.getConstructors());
            var e = Stream.concat(Stream.concat(a, b), Stream.concat(c, d));
            var f = Stream.of(k.getDeclaredFields());
            var g = Stream.of(k.getFields());
            var h = Stream.concat(f, g);
            var t = Stream.of(k.getTypeParameters());
            add(k);
            t.forEach(x -> {
                add(x);
            });
            e.forEach(x -> {
                add(Methods.getReturnType(x));
                add(x.getGenericExceptionTypes());
                add(x.getGenericParameterTypes());
                add(x.getTypeParameters());
            });
            h.forEach(x -> {
                add(Methods.getReturnType(x));
            });
        }

        private void add(@NonNull Type[] ts) {
            checkNotNull(ts); // Check recognized by lombok.
            add(new HashSet<>(10), ts);
        }

        private void add(@NonNull Type t) {
            checkNotNull(t); // Check recognized by lombok.
            add(new HashSet<>(10), t);
        }

        private void add(@NonNull Set<Type> partial, @NonNull Type[] ts) {
            checkNotNull(partial); // Check recognized by lombok.
            checkNotNull(ts); // Check recognized by lombok.
            for (var t : ts) {
                add(partial, t);
            }
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
                        add(partial, p.getActualTypeArguments());
                    }
                    case GenericArrayType g -> add(partial, g.getGenericComponentType());
                    case TypeVariable<?> v -> {
                        add(partial, v.getBounds());
                    }
                    case WildcardType w -> {
                        add(partial, w.getLowerBounds());
                        add(partial, w.getUpperBounds());
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
            if (seen.contains(klass)) return;
            var simple = klass.getSimpleName();
            if (simpleNamesSeen.containsKey(simple)) {
                var conflict = simpleNamesSeen.get(simple);
                if (conflict != void.class) {
                    simpleNamesSeen.put(simple, void.class);
                    simpleNamesSeen.put(conflict.getName(), conflict);
                    seen.add(conflict);
                    fullNameNeeded.add(conflict);
                }
                simpleNamesSeen.put(klass.getName(), klass);
                seen.add(klass);
                fullNameNeeded.add(klass);
            } else {
                simpleNamesSeen.put(simple, klass);
                seen.add(klass);
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

        @NonNull
        private String getSimplifiedGenericString(@NonNull Executable what, boolean withClassName) {
            checkNotNull(what); // Check recognized by lombok.
            assertSame(what.getDeclaringClass(), klass);
            var sb = new StringBuilder(256);

            if (withClassName) {
                formatType(klass, sb);
                sb.append("/");
            }

            // Type parameters.
            if (what.getTypeParameters().length > 0) {
                sb.append("<");
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

            // Return type.
            formatType(Methods.getReturnType(what), sb);

            if (what instanceof Method) {
                sb.append(" ");

                // Class name.
                sb.append(what.getDeclaringClass().getSimpleName()).append(".");

                // Method name.
                sb.append(what.getName());
            }

            // Parameters.
            sb.append("(");
            formatParameterTypes(what, sb);
            sb.append(")");

            return sb.toString();
        }

        @NonNull
        private String getSimplifiedGenericString(@NonNull Field field, boolean withClassName) {
            checkNotNull(field); // Check recognized by lombok.
            assertSame(field.getDeclaringClass(), klass);
            var sb = new StringBuilder(256);

            if (withClassName) {
                formatType(klass, sb);
                sb.append("/");
            }

            // Return type.
            formatType(Methods.getReturnType(field), sb);
            sb.append(" ");

            // Class name.
            formatType(field.getDeclaringClass(), sb);
            sb.append(".");

            // Method name.
            sb.append(field.getName());

            return sb.toString();
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T> ClassDictionary<T> getFor(@NonNull Class<T> klass) {
        checkNotNull(klass); // Check recognized by lombok.
        synchronized (lock) {
            var d = (ClassDictionary<T>) map.get(klass);
            if (d != null) return d;
            d = new ClassDictionary<>(klass); // TODO: Slow inside synchronized!
            map.put(klass, d);
            return d;
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
