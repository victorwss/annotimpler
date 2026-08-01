package ninja.test.javahacker.typeser;

import ninja.javahacker.typeser.TypeRef;
import ninja.test.ForTests;

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

