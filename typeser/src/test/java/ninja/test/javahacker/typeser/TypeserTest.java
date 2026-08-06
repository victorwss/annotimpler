package ninja.test.javahacker.typeser;

import ninja.javahacker.typeser.TypeRef;
import ninja.test.ForTests;

import java.lang.annotation.Annotation;

import module java.base;
import module org.junit.jupiter.api;

@SuppressWarnings({"unused", "unchecked"})
public class TypeserTest {

    // ── Helper types for non-null owner-type test ────────────────────────────

    private static class Outer<T> {
        class Inner<U> {}
    }

    // ── Sample fields — never instantiated, only used for reflection ──────────

    @SuppressWarnings("unused")
    private static final class Sample {
        private String someClass;
        private List<String> list;
        private List<? extends Number> upper;
        private List<? super Integer> lower;
        private List<?> any;
        private List<String>[] genericArray;
        private Map<String, List<Integer>> nested;
        private Map.Entry<String, Integer> entry;
        private Outer<String>.Inner<Integer> inner;
    }

    // ── Round-trip helper (tests TypeRef.write + TypeRef.read) ───────────────

    private static Type roundTrip(Type type) throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            TypeRef.write(oos, type);
        }
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            return TypeRef.read(ois);
        }
    }

    // ── Tests: serialisation round-trip ─────────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testSerials() throws Exception {
        var pf = "[testSerials] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    var copy = roundTrip(original);
                    Assertions.assertEquals(original, copy);
                }));
    }

    // ── Tests: TypeRef.wrap() and type() without serialisation ───────────────

    @TestFactory
    public Stream<DynamicTest> testWrap() throws Exception {
        var pf = "[testWrap] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    Assertions.assertEquals(original, TypeRef.wrap(original).type());
                }));
    }

    // ── Tests: TypeVariable support ───────────────────────────────────────────

    private static <T> T genericMethod(T arg) {
        return arg;
    }

    private static final class GenericConstructor {
        private <C> GenericConstructor(C arg) {
        }
    }

    @TestFactory
    public Stream<DynamicTest> testTypeVariable() throws Exception {
        var pf = "[testTypeVariable] ";
        var classVar = List.class.getTypeParameters()[0];
        var methodVar = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        var constructorVar = GenericConstructor.class.getDeclaredConstructors()[0].getTypeParameters()[0];
        return Stream.of(classVar, methodVar, constructorVar)
                .map(typeVar -> DynamicTest.dynamicTest(pf + typeVar.getGenericDeclaration(), () -> {
                    Assertions.assertEquals(typeVar, TypeRef.wrap(typeVar).type());
                    Assertions.assertEquals(typeVar, roundTrip(typeVar));
                }));
    }

    // ── Tests: malformed/malicious `Type` implementations ────────────────────
    //
    // These types can never come from real reflection, but nothing stops a hand-written or
    // maliciously-crafted `ParameterizedType`/`WildcardType`/`GenericArrayType`/`TypeVariable`
    // implementation from violating their documented contracts (e.g. returning `null` where an
    // array or component is expected, or forming a cycle). `TypeRef` must not misbehave (crash
    // with a raw `NullPointerException`/`StackOverflowError`, corrupt state, hang, etc.) when fed
    // such implementations; it must fail predictably with `IllegalArgumentException`.

    private static final class LinkedParameterizedType implements ParameterizedType {
        private Type raw;
        private Type[] args = new Type[0];
        private Type owner;

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return owner;
        }
    }

    private static final class SelfComponentArrayType implements GenericArrayType {
        @Override
        public Type getGenericComponentType() {
            return this;
        }
    }

    private abstract static class AbstractFakeTypeVariable implements TypeVariable<GenericDeclaration> {
        @Override
        public Type[] getBounds() {
            return new Type[0];
        }

        @Override
        public String getName() {
            return "T";
        }

        @Override
        public AnnotatedType[] getAnnotatedBounds() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

    private static final class ForeignGenericDeclaration implements GenericDeclaration {
        @Override
        public TypeVariable<?>[] getTypeParameters() {
            return new TypeVariable<?>[0];
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMalformedTypes() {
        var pf = "[testMalformedTypes] ";

        var nullArgsArray = new LinkedParameterizedType();
        nullArgsArray.raw = List.class;
        nullArgsArray.args = null;

        var nullArgsElement = new LinkedParameterizedType();
        nullArgsElement.raw = List.class;
        nullArgsElement.args = new Type[] {String.class, null};

        var nullRawType = new LinkedParameterizedType();
        nullRawType.raw = null;

        var selfRawType = new LinkedParameterizedType();
        selfRawType.raw = selfRawType;

        var mutualA = new LinkedParameterizedType();
        var mutualB = new LinkedParameterizedType();
        mutualA.raw = List.class;
        mutualA.args = new Type[] {mutualB};
        mutualB.raw = mutualA;

        WildcardType nullUpperBounds = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return null;
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };

        WildcardType nullLowerBoundsElement = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[] {Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[] {null};
            }
        };

        GenericArrayType nullComponent = () -> null;
        var selfComponent = new SelfComponentArrayType();

        var nullDeclaration = new AbstractFakeTypeVariable() {
            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };

        var foreignDeclaration = new AbstractFakeTypeVariable() {
            @Override
            public GenericDeclaration getGenericDeclaration() {
                return new ForeignGenericDeclaration();
            }
        };

        return Stream.of(
                DynamicTest.dynamicTest(
                        pf + "parameterized type with null type-argument array",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.wrap(nullArgsArray))
                ),
                DynamicTest.dynamicTest(
                        pf + "parameterized type with a null type argument",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullArgsElement))
                ),
                DynamicTest.dynamicTest(
                        pf + "parameterized type with null raw type",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullRawType))
                ),
                DynamicTest.dynamicTest(
                        pf + "parameterized type whose raw type is itself",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.wrap(selfRawType))
                ),
                DynamicTest.dynamicTest(
                        pf + "two parameterized types cyclically referencing each other",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.wrap(mutualA))
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with null upper-bounds array",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.wrap(nullUpperBounds))
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with a null lower bound",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullLowerBoundsElement))
                ),
                DynamicTest.dynamicTest(
                        pf + "generic array type with null component",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullComponent))
                ),
                DynamicTest.dynamicTest(
                        pf + "generic array type whose component is itself",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.wrap(selfComponent))
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable with null generic declaration",
                        () -> ForTests.testNull("declaration", () -> TypeRef.wrap(nullDeclaration))
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable declared by a foreign GenericDeclaration",
                        () -> Assertions.assertThrows(
                                UnsupportedOperationException.class,
                                () -> TypeRef.wrap(foreignDeclaration)
                        )
                )
        );
    }

    // ── Tests: @NonNull violations → IllegalArgumentException ───────────────

    @TestFactory
    public Stream<DynamicTest> testNulls() throws Exception {
        var pf = "[testNulls] ";
        var out = new ObjectOutputStream(new ByteArrayOutputStream());
        return Stream.of(
            DynamicTest.dynamicTest(pf + "a", () -> ForTests.testNull("type", () -> new TypeRef(null))),
            DynamicTest.dynamicTest(pf + "b", () -> ForTests.testNull("type", () -> TypeRef.wrap(null))),
            DynamicTest.dynamicTest(pf + "c", () -> ForTests.testNull("in", () -> TypeRef.read(null))),
            DynamicTest.dynamicTest(pf + "d", () -> ForTests.testNull("out", () -> TypeRef.write(null, String.class))),
            DynamicTest.dynamicTest(pf + "e", () -> ForTests.testNull("type", () -> TypeRef.write(out, null)))
        );
    }
}

