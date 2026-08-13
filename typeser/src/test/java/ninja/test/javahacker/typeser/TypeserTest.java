package ninja.test.javahacker.typeser;

import java.lang.annotation.Annotation;
import ninja.javahacker.typeser.TypeRef;
import ninja.test.ForTests;
import org.junit.jupiter.api.function.Executable;

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
        return readFromBytes(writeToBytes(type));
    }

    private static byte[] writeToBytes(Type type) throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            TypeRef.write(oos, type);
        }
        return baos.toByteArray();
    }

    private static Type readFromBytes(byte[] content) throws Exception {
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(content))) {
            return TypeRef.read(ois);
        }
    }

    private static byte[] replaceAsciiOnce(byte[] source, String before, String after) {
        Assertions.assertEquals(before.length(), after.length(), "Replacement strings must have same length.");
        var from = before.getBytes(StandardCharsets.UTF_8);
        var to = after.getBytes(StandardCharsets.UTF_8);
        var out = source.clone();
        int hits = 0;
        for (int i = 0; i <= out.length - from.length; i++) {
            boolean match = true;
            for (int j = 0; j < from.length; j++) {
                if (out[i + j] != from[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                System.arraycopy(to, 0, out, i, to.length);
                hits++;
                i += from.length - 1;
            }
        }
        Assertions.assertEquals(1, hits, "Expected exactly one serialized occurrence of \"" + before + "\".");
        return out;
    }

    private static Type roundTrip(Type type, String before, String after) throws Exception {
        var bytes = writeToBytes(type);
        var changed = replaceAsciiOnce(bytes, before, after);
        return readFromBytes(changed);
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

    private static final class AltGenericCtor0000 {
        private AltGenericCtor0000() {
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

    /// Asserts that `exec` throws an exception of the given `type` whose message contains `expectedSubstring`.
    ///
    /// Checking the message (not just the exception type) matters here because several completely different
    /// malformed-type scenarios (a `null` sub-component, a cyclic type graph, an oversized type graph, and so
    /// on) all surface as the very same {@link IllegalArgumentException} class. Without pinning down the
    /// message, a test for one scenario (say, a cycle) could pass for the wrong reason (say, because the
    /// "too deeply nested" check tripped instead), silently letting a regression slip through.
    /// @param <T> The expected exception type.
    /// @param type The expected exception type.
    /// @param expectedSubstring A substring that must be present in the thrown exception's message.
    /// @param exec The code expected to throw.
    /// @return The thrown exception, in case further assertions are needed.
    private static <T extends Throwable> T assertThrowsContaining(Class<T> type, String expectedSubstring, Executable exec) {
        var ex = Assertions.assertThrows(type, exec);
        Assertions.assertNotNull(ex.getMessage(), () -> "Expected a non-null message on " + ex);
        Assertions.assertTrue(
                ex.getMessage().contains(expectedSubstring),
                () -> "Expected message to contain \"" + expectedSubstring + "\" but was \"" + ex.getMessage() + "\""
        );
        return ex;
    }

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

    private static final class UnsupportedType implements Type {
        @Override
        public String toString() {
            return "UnsupportedType";
        }
    }

    // ── Tests: DoS protection against oversized/maliciously-exploding type graphs ────────────

    /// Builds a chain of {@code depth} distinct (non-cyclic!) {@link ParameterizedType}s linked through
    /// {@link ParameterizedType#getOwnerType()}, where each link is a brand new object. Since every object is
    /// distinct, this never triggers the cycle detector, yet a sufficiently long chain represents an unboundedly
    /// deep type graph, exactly like a maliciously-crafted {@code getOwnerType()} that keeps fabricating new
    /// owner instances instead of reflecting a real enclosing type.
    private static LinkedParameterizedType deepOwnerChain(int depth) {
        LinkedParameterizedType previous = null;
        for (int i = 0; i < depth; i++) {
            var current = new LinkedParameterizedType();
            current.raw = List.class;
            current.owner = previous;
            previous = current;
        }
        return previous;
    }

    /// Builds a single {@link ParameterizedType} with {@code width} distinct type arguments, each of which is
    /// itself a small but distinct {@link ParameterizedType}. This represents a maliciously wide (as opposed to
    /// deep) type graph, exactly like a {@code getActualTypeArguments()} fabricating hundreds of forged
    /// sub-types instead of reflecting real type arguments.
    private static LinkedParameterizedType wideTypeArguments(int width) {
        var top = new LinkedParameterizedType();
        top.raw = List.class;
        var args = new Type[width];
        for (int i = 0; i < width; i++) {
            var leaf = new LinkedParameterizedType();
            leaf.raw = Object.class;
            args[i] = leaf;
        }
        top.args = args;
        return top;
    }

    @TestFactory
    public Stream<DynamicTest> testTooLargeType() {
        var pf = "[testTooLargeType] ";
        return Stream.of(
                DynamicTest.dynamicTest(
                        pf + "moderately deep but legitimate-sized owner chain succeeds",
                        () -> Assertions.assertDoesNotThrow(() -> TypeRef.wrap(deepOwnerChain(30)))
                ),
                DynamicTest.dynamicTest(
                        pf + "moderately wide but legitimate-sized type-argument list succeeds",
                        () -> Assertions.assertDoesNotThrow(() -> TypeRef.wrap(wideTypeArguments(30)))
                ),
                DynamicTest.dynamicTest(
                        pf + "unboundedly deep owner-type chain is rejected",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Too deeply nested type graph detected involving",
                                () -> TypeRef.wrap(deepOwnerChain(10_000))
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "excessively wide type-argument list is rejected",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Too deeply nested type graph detected involving",
                                () -> TypeRef.wrap(wideTypeArguments(10_000))
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "the type graph size limit is tracked per call, not accumulated across calls",
                        () -> {
                            // Trigger the size limit a few times in a row...
                            for (int i = 0; i < 5; i++) {
                                assertThrowsContaining(
                                        IllegalArgumentException.class,
                                        "Too deeply nested type graph detected involving",
                                        () -> TypeRef.wrap(deepOwnerChain(10_000))
                                );
                            }
                            // ...and then confirm that ordinary, legitimately-sized types still serialize fine
                            // afterwards, proving that no leftover state from the rejected oversized attempts
                            // is leaking into (and permanently poisoning) later, unrelated calls.
                            Assertions.assertDoesNotThrow(() -> TypeRef.wrap(deepOwnerChain(10)));
                            Assertions.assertEquals(String.class, TypeRef.wrap(String.class).type());
                        }
                )
        );
    }

    @TestFactory
    public Stream<DynamicTest> testMalformedTypes() {
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

        WildcardType nullLowerBoundsArray = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[] {Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return null;
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

        var missingTypeVariable = new AbstractFakeTypeVariable() {
            @Override
            public GenericDeclaration getGenericDeclaration() {
                return List.class;
            }

            @Override
            public String getName() {
                return "THIS_NAME_DOES_NOT_EXIST";
            }
        };

        var unknownType = new UnsupportedType();

        var pf = "[testMalformedTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(
                        pf + "parameterized type with null type-argument array",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                ".getActualTypeArguments() returned null.",
                                () -> TypeRef.wrap(nullArgsArray)
                        )
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
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Cyclic type graph detected involving",
                                () -> TypeRef.wrap(selfRawType)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "two parameterized types cyclically referencing each other",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Cyclic type graph detected involving",
                                () -> TypeRef.wrap(mutualA)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with null upper-bounds array",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                ".getUpperBounds() returned null.",
                                () -> TypeRef.wrap(nullUpperBounds)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with null lower-bounds array",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                ".getLowerBounds() returned null.",
                                () -> TypeRef.wrap(nullLowerBoundsArray)
                        )
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
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Cyclic type graph detected involving",
                                () -> TypeRef.wrap(selfComponent)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable with null generic declaration",
                        () -> ForTests.testNull("declaration", () -> TypeRef.wrap(nullDeclaration))
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable declared by a foreign GenericDeclaration",
                        () -> assertThrowsContaining(
                                UnsupportedOperationException.class,
                                "Unsupported GenericDeclaration:",
                                () -> TypeRef.wrap(foreignDeclaration)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable whose name is missing in declaration",
                        () -> assertThrowsContaining(
                                IllegalStateException.class,
                                "Type variable \"THIS_NAME_DOES_NOT_EXIST\" not found in",
                                () -> roundTrip(missingTypeVariable)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "unknown type implementation is preserved before serialization",
                        () -> Assertions.assertSame(unknownType, TypeRef.wrap(unknownType).type())
                ),
                DynamicTest.dynamicTest(
                        pf + "unknown type implementation cannot be reconstructed after serialization",
                        () -> assertThrowsContaining(
                                UnsupportedOperationException.class,
                                "Unknown type.",
                                () -> roundTrip(unknownType)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "stale method declaration in serialized stream",
                        () -> {
                            var methodVar = Stream.of(TypeserTest.class.getDeclaredMethods())
                                    .filter(m -> m.getName().equals("genericMethod"))
                                    .findFirst()
                                    .orElseThrow()
                                    .getTypeParameters()[0];
                            assertThrowsContaining(
                                    IllegalStateException.class,
                                    "Method \"missingMethod\" not found in",
                                    () -> roundTrip(methodVar, "genericMethod", "missingMethod")
                            );
                        }
                ),
                DynamicTest.dynamicTest(
                        pf + "stale constructor declaration in serialized stream",
                        () -> {
                            var constructorVar = GenericConstructor.class.getDeclaredConstructors()[0].getTypeParameters()[0];
                            assertThrowsContaining(
                                    IllegalStateException.class,
                                    "Constructor not found in class ninja.test.javahacker.typeser.TypeserTest$AltGenericCtor0000.",
                                    () -> roundTrip(
                                            constructorVar,
                                            "GenericConstructor",
                                            "AltGenericCtor0000"
                                    )
                            );
                        }
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
