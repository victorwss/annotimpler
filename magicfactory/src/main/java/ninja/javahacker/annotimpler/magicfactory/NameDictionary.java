package ninja.javahacker.annotimpler.magicfactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;

public final class NameDictionary {
    private static final NameDictionary GLOBAL_INSTANCE = new NameDictionary();

    private final Object lock;
    private final Map<Class<?>, ClassDictionary<?>> map;

    public NameDictionary() {
        this.lock = new Object();
        this.map = new HashMap<>(10);
    }

    private static final class ClassDictionary<T> {
        private final Class<T> klass;
        private final Map<Class<?>, String> map1;
        private Map<String, Class<?>> map2;

        private ClassDictionary(@NonNull Class<T> k) {
            this.klass = k;
            this.map1 = new HashMap<>(20);
            this.map2 = new HashMap<>(20);
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
            add(new HashSet<>(10), ts);
        }

        private void add(@NonNull Type t) {
            add(new HashSet<>(10), t);
        }

        private void add(@NonNull Set<Type> partial, @NonNull Type[] ts) {
            for (var t : ts) {
                add(partial, t);
            }
        }

        private void add(@NonNull Set<Type> partial, @Nullable Type t) {
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
                    default -> throw new UnsupportedOperationException();
                }
            } finally {
                partial.remove(t);
            }
        }

        private void addClass(@NonNull Set<Type> partial, @NonNull Class<?> klass) {
            if (klass.isArray()) {
                add(partial, klass.getComponentType());
                return;
            }
            if (map1.containsKey(klass)) return;
            var simple = klass.getSimpleName();
            if (map2.containsKey(simple)) {
                var conflict = map2.get(simple);
                map2.remove(simple);
                map2.put(conflict.getName(), conflict);
                map1.put(conflict, conflict.getName());
                map2.put(klass.getName(), klass);
                map1.put(klass, klass.getName());
            } else {
                map2.put(simple, klass);
                map1.put(klass, simple);
            }
        }

        @NonNull
        public Class<T> getKlass() {
            return klass;
        }

        @Override
        public int hashCode() {
            return klass.hashCode();
        }

        @NonNull
        @Override
        public String toString() {
            return klass.toString();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof ClassDictionary<?> n && n.klass == this.klass;
        }

        @NonNull
        public Optional<String> getNameFor(@NonNull Class<?> otherClass) {
            var name = map1.get(otherClass);
            return Optional.ofNullable(name);
        }

        @NonNull
        private void formatParameterTypes(@NonNull Executable method, @NonNull StringBuilder sb) {
            var paramTypes = method.getGenericParameterTypes();
            for (var i = 0; i < paramTypes.length; i++) {
                if (i > 0) sb.append(", ");
                formatType(paramTypes[i], sb);
            }
        }

        @NonNull
        private void formatType(@NonNull Type type, @NonNull StringBuilder sb) {
            switch (type) {
                case Class<?> clazz -> {
                    if (clazz.isArray()) {
                        formatType(clazz.getComponentType(), sb);
                        sb.append("[]");
                    } else {
                        var c = map1.get(clazz);
                        if (c == null) throw new AssertionError(klass + "---" + type);
                        sb.append(c);
                    }
                }
                case ParameterizedType paramType -> {
                    formatType(paramType.getRawType(), sb);
                    sb.append("<");
                    var typeArgs = paramType.getActualTypeArguments();
                    for (var i = 0; i < typeArgs.length; i++) {
                        if (i > 0) sb.append(", ");
                        formatType(typeArgs[i], sb);
                    }
                    sb.append(">");
                }
                case TypeVariable<?> tv -> sb.append(tv.getName());
                case GenericArrayType arrayType -> {
                    formatType(arrayType.getGenericComponentType(), sb);
                    sb.append("[]");
                }
                case WildcardType wildcardType -> {
                    sb.append("?");

                    var upperBounds = wildcardType.getUpperBounds();
                    var lowerBounds = wildcardType.getLowerBounds();

                    if (upperBounds.length > 0 && upperBounds[0] != Object.class) {
                        sb.append(" extends ");
                        for (var i = 0; i < upperBounds.length; i++) {
                            if (i > 0) sb.append(" & ");
                            formatType(upperBounds[i], sb);
                        }
                    } else if (lowerBounds.length > 0) {
                        sb.append(" super ");
                        for (var i = 0; i < lowerBounds.length; i++) {
                            if (i > 0) sb.append(" & ");
                            formatType(lowerBounds[i], sb);
                        }
                    }
                }
                default -> throw new AssertionError();
            }
        }

        @NonNull
        public String getSimplifiedGenericString(@NonNull Executable what, boolean withClassName) {
            if (what.getDeclaringClass() != klass) throw new IllegalArgumentException();
            var sb = new StringBuilder(256);

            if (withClassName) {
                formatType(klass, sb);
                sb.append("/");
            }

            // Parâmetros de tipo.
            if (what.getTypeParameters().length > 0) {
                sb.append("<");
                var typeParams = what.getTypeParameters();
                for (var i = 0; i < typeParams.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(typeParams[i].getName());
                    var bounds = typeParams[i].getBounds();
                    if (bounds.length > 0 && !(bounds.length == 1 && bounds[0] == Object.class)) {
                        sb.append(" extends ");
                        for (var j = 0; j < bounds.length; j++) {
                            if (j > 0) sb.append(" & ");
                            formatType(bounds[j], sb);
                        }
                    }
                }
                sb.append("> ");
            }

            // Tipo de retorno.
            formatType(Methods.getReturnType(what), sb);

            if (what instanceof Method) {
                sb.append(" ");

                // Nome da classe.
                sb.append(what.getDeclaringClass().getSimpleName()).append(".");

                // Nome do método.
                sb.append(what.getName());
            }

            // Parâmetros.
            sb.append("(");
            formatParameterTypes(what, sb);
            sb.append(")");

            return sb.toString();
        }

        @NonNull
        public String getSimplifiedGenericString(@NonNull Field field, boolean withClassName) {
            if (field.getDeclaringClass() != klass) throw new IllegalArgumentException();
            var sb = new StringBuilder(256);

            if (withClassName) {
                formatType(klass, sb);
                sb.append("/");
            }

            // Tipo de retorno.
            formatType(Methods.getReturnType(field), sb);
            sb.append(" ");

            // Nome da classe.
            formatType(field.getDeclaringClass(), sb);
            sb.append(".");

            // Nome do método.
            sb.append(field.getName());

            return sb.toString();
        }

        @NonNull
        public String getSimplifiedClassName(@NonNull Class<?> what) {
            var sb = new StringBuilder(256);
            formatType(what, sb);
            return sb.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ClassDictionary<T> getFor(@NonNull Class<T> klass) {
        synchronized (lock) {
            var d = (ClassDictionary<T>) map.get(klass);
            if (d != null) return d;
            d = new ClassDictionary<>(klass); // TODO: Slow inside synchronized!
            map.put(klass, d);
            return d;
        }
    }

    @NonNull
    public String getSimplifiedGenericString(@NonNull Executable what, boolean withClassName) {
        var cl = getFor(what.getDeclaringClass());
        return cl.getSimplifiedGenericString(what, withClassName);
    }

    @NonNull
    public String getSimplifiedGenericString(@NonNull Field field, boolean withClassName) {
        var cl = getFor(field.getDeclaringClass());
        return cl.getSimplifiedGenericString(field, withClassName);
    }

    public static NameDictionary global() {
        return GLOBAL_INSTANCE;
    }
}
