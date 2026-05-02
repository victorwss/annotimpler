package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.magicfactory;

public class TypeNameTest {
    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    private static class Inner {
        private class Inner2 {
        }
    }

    @SuppressWarnings("unused")
    private static <E> void noop(
            Float a,
            Thread b,
            int c,
            List<String> d,
            List<List<Thread>> e,
            Map<List<String>, Set<java.lang.Integer>> f,
            E g,
            List<E> h,
            List<?> i,
            List<? extends Number> j,
            E[] k,
            E[][] l,
            List<E[]> m,
            int[] n,
            Inner.Inner2 o,
            java.sql.Date p,
            java.util.Date q)
    {
        throw new AssertionError();
    }

    private static final List<Type> TYPES = Stream
            .of(TypeNameTest.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("noop"))
            .map(m -> m.getParameters())
            .flatMap(Stream::of)
            .map(Parameter::getParameterizedType)
            .toList();

    private static final List<String> TYPE_NAMES = List.of(
            "Float", "Thread", "int", "List<String>", "List<List<Thread>>", "Map<List<String>, Set<Integer>>", "E", "List<E>", "List<?>",
            "List<? extends Number>", "E[]", "E[][]", "List<E[]>", "int[]", "Inner2", "Date", "Date"
    );

    private static final Map<Type, String> TYPE_MAP = new HashMap<>();

    static {
        for (var i = 0; i < TYPES.size(); i++) {
            TYPE_MAP.put(TYPES.get(i), TYPE_NAMES.get(i));
        }
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testTypeNames() {
        return TYPES.stream().map(t -> n("[testTypeNames] " + t, () -> Assertions.assertEquals(TYPE_MAP.get(t), TypeName.of(t))));
    }

    private static final class Integer {}

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testTypeNamesConflict() {
        var conflict = Set.of(Integer.class, java.lang.Integer.class);
        var all = Stream.concat(TYPES.stream(), conflict.stream());
        return all.map(t -> n(
                "[testTypeNamesConflict] " + (t == Integer.class ? "Fake Integer" : t.getTypeName()),
                () -> {
                    var name = conflict.contains(t) ? ((Class<?>) t).getName() : TYPE_MAP.get(t);
                    if (name.startsWith("Map<")) name = "Map<List<String>, Set<java.lang.Integer>>";
                    Assertions.assertEquals(name, TypeName.of(t, conflict));
                }
        ));
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testTypeNamesConflictWithStringBuilder() {
        var conflict = Set.of(Integer.class, java.lang.Integer.class);
        var all = Stream.concat(TYPES.stream(), conflict.stream());
        return all.map(t -> n(
                "[testTypeNamesConflictWithStringBuilder] " + (t == Integer.class ? "Fake Integer" : t.getTypeName()),
                () -> {
                    var name = conflict.contains(t) ? ((Class<?>) t).getName() : TYPE_MAP.get(t);
                    if (name.startsWith("Map<")) name = "Map<List<String>, Set<java.lang.Integer>>";
                    var sb = new StringBuilder(25);
                    TypeName.formatType(t, conflict, sb);
                    Assertions.assertEquals(name, sb.toString());
                }
        ));
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public List<DynamicTest> testAnonymousClassAndLambdas() {
        var x1 = new Thread() {};
        Runnable y1 = () -> {};
        var x2 = new Thread() {};
        Runnable y2 = () -> {};

        var s = Set.of(String.class);
        var u1 = Set.of(x1.getClass(), x2.getClass());
        var u2 = Set.of(y1.getClass(), y2.getClass());

        var a = n("Anonymous without conflict"       , () -> Assertions.assertEquals(x1.getClass().getName(), TypeName.of(x1.getClass())));
        var b = n("Lambda without conflict"          , () -> Assertions.assertEquals(y1.getClass().getName(), TypeName.of(y1.getClass())));
        var c = n("Anonymous with unrelated conflict", () -> Assertions.assertEquals(x1.getClass().getName(), TypeName.of(x1.getClass(), s)));
        var d = n("Lambda with unrelated conflict"   , () -> Assertions.assertEquals(y1.getClass().getName(), TypeName.of(y1.getClass(), s)));
        var e = n("Anonymous with direct conflict"   , () -> Assertions.assertEquals(x1.getClass().getName(), TypeName.of(x1.getClass(), u1)));
        var f = n("Lambda with direct conflict"      , () -> Assertions.assertEquals(y1.getClass().getName(), TypeName.of(y1.getClass(), u2)));
        return List.of(a, b, c, d, e, f);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                DynamicTest.dynamicTest("TypeName.of(1) - what", () -> ForTests.testNull("what", () -> TypeName.of(null))),
                DynamicTest.dynamicTest("TypeName.of(2) - what", () -> ForTests.testNull("what", () -> TypeName.of(null, Set.<Class<?>>of()))),
                DynamicTest.dynamicTest("TypeName.of(2) - fullNameNeeded", () -> ForTests.testNull("fullNameNeeded", () -> TypeName.of(Float.class, null))),
                DynamicTest.dynamicTest("TypeName.formatType - type", () -> ForTests.testNull("type", () -> TypeName.formatType(null, Set.<Class<?>>of(), new StringBuilder(1)))),
                DynamicTest.dynamicTest("TypeName.formatType - fullNameNeeded", () -> ForTests.testNull("fullNameNeeded", () -> TypeName.formatType(Float.class, null, new StringBuilder(1)))),
                DynamicTest.dynamicTest("TypeName.formatType - sb", () -> ForTests.testNull("sb", () -> TypeName.formatType(Float.class, Set.<Class<?>>of(), null)))
        );
    }

    @Test
    public void testNoInstance() throws Exception {
        ForTests.testNonInstantiable(TypeName.class);
    }
}
