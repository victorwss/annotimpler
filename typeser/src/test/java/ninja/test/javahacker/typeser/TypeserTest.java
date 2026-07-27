package ninja.test.javahacker.typeser;

import ninja.javahacker.typeser.TypeRef;
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

    // ── Tests: unsupported TypeVariable → AssertionError ─────────────────────

    @Test
    public void testTypeVariableThrows() {
        // List<E>'s type parameter E is a TypeVariable — must not be serialised.
        var typeVar = List.class.getTypeParameters()[0];
        Assertions.assertThrows(UnsupportedOperationException.class, () -> TypeRef.wrap(typeVar));
    }

    // ── Tests: @NonNull violations → IllegalArgumentException ───────────────

    @Test
    @SuppressWarnings("null")
    public void testNulls() throws Exception {
        // new TypeRef(null) calls SerializableType.from(null) which carries @NonNull.
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TypeRef(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.wrap(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.read(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.write(null, String.class));
        var out = new ObjectOutputStream(new ByteArrayOutputStream());
        Assertions.assertThrows(IllegalArgumentException.class, () -> TypeRef.write(out, null));
    }
}

