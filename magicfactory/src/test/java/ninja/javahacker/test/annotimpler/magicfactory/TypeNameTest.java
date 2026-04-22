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
            Map<List<String>, Set<Integer>> f,
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
            .of(ConstructionExceptionTest.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("noop"))
            .map(m -> m.getParameters())
            .flatMap(Stream::of)
            .map(Parameter::getParameterizedType)
            .toList();

    private static final List<String> TYPE_NAMES = List.of(
            "Float", "Thread", "int", "List<String>", "List<List<Thread>>", "Map<List<String>, Set<Integer>>", "E", "List<E>", "List<?>",
            "List<? extends Number>", "E[]", "E[][]", "List<E[]>", "int[]", "Inner2", "java.sql.Date", "java.util.Date"
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
        return TYPES.stream().map(t -> n("" + t, () -> Assertions.assertEquals(TYPE_MAP.get(t), TypeName.of(t))));
    }

    private static final class Integer {}

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testTypeNamesConflict() {
        var conflict = Set.of(Integer.class, java.lang.Integer.class);
        var all = Stream.concat(TYPES.stream(), conflict.stream());
        return all.map(t -> n(
                "" + t,
                () -> {
                    if (t == Integer.class) {
                        Assertions.assertEquals(Integer.class.getName(), TypeName.of(t, conflict));
                    } else if (t == java.lang.Integer.class) {
                        Assertions.assertEquals(java.lang.Integer.class.getName(), TypeName.of(t, conflict));
                    } else {
                        Assertions.assertEquals(TYPE_MAP.get(t), TypeName.of(t, conflict));
                    }
                }
        ));
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testTypeNamesConflictWithStringBuilder() {
        var conflict = Set.of(Integer.class, java.lang.Integer.class);
        var all = Stream.concat(TYPES.stream(), conflict.stream());
        return all.map(t -> n(
                "" + t,
                () -> {
                    var sb = new StringBuilder(25);
                    TypeName.formatType(t, conflict, sb);
                    if (t == Integer.class) {
                        Assertions.assertEquals(Integer.class.getName(), sb.toString());
                    } else if (t == java.lang.Integer.class) {
                        Assertions.assertEquals(java.lang.Integer.class.getName(), sb.toString());
                    } else {
                        Assertions.assertEquals(TYPE_MAP.get(t), sb.toString());
                    }
                }
        ));
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                DynamicTest.dynamicTest("TypeName.of(1) - what", () -> ForTests.testNull("what", () -> TypeName.of(null))),
                DynamicTest.dynamicTest("TypeName.of(2) - what", () -> ForTests.testNull("what", () -> TypeName.of(null, Set.<Class<?>>of()))),
                DynamicTest.dynamicTest("TypeName.of(2) - fullNameNeeded", () -> ForTests.testNull("fullNameNeeded", () -> TypeName.of(Float.class, null))),
                DynamicTest.dynamicTest("TypeName.formatType - what", () -> ForTests.testNull("what", () -> TypeName.formatType(null, Set.<Class<?>>of(), new StringBuilder(1)))),
                DynamicTest.dynamicTest("TypeName.formatType - fullNameNeeded", () -> ForTests.testNull("fullNameNeeded", () -> TypeName.formatType(Float.class, null, new StringBuilder(1)))),
                DynamicTest.dynamicTest("TypeName.formatType - sb", () -> ForTests.testNull("sb", () -> TypeName.formatType(Float.class, Set.<Class<?>>of(), null)))
        );
    }
}
