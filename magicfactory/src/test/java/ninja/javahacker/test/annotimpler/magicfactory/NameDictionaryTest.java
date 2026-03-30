package ninja.javahacker.test.annotimpler.magicfactory;

import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.params;
import module ninja.javahacker.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;

public class NameDictionaryTest {

    private static Arguments n(String name, Executable ctx) {
        return Arguments.of(name, ctx);
    }

    public static class Something {

        private int a;
        private java.sql.Date b;
        private java.util.Date c;
        private String d;

        public Something() {
            throw new AssertionError();
        }

        public Something(int x) {
            throw new AssertionError();
        }

        public static int staticMethod2(int c3po, String r2d2) {
            throw new AssertionError();
        }

        public java.util.Date ambig1() {
            throw new AssertionError();
        }

        public java.sql.Date ambig2() {
            throw new AssertionError();
        }

        public int ambig3(java.util.Date a, java.sql.Date b, String c, Something d) {
            throw new AssertionError();
        }
    }

    public static interface Weird<A, B, C extends Serializable & Cloneable, D extends Cloneable, E extends Enum<E>, F extends Comparable<F>> {
        public
        <
                U extends Thread,
                X extends Map<C, D>,
                Y extends Cloneable & Serializable,
                Z extends Object,
                W extends List<? extends Object>,
                T extends Object & Cloneable,
                V extends Object & Cloneable & Serializable
        >
        Map<A, Map<C[], Map<String, ? extends List<? super E>>>>
        crazy(
                D param,
                F foo,
                U[] uh,
                X zu,
                List<? extends Cloneable> oh,
                List<? extends Object> of);
    }

    private static record PartEx(java.lang.reflect.Executable a, String b, String c) {
    }

    private static record PartFi(Field a, String b, String c) {
    }

    private static Stream<Arguments> testSimplifiedExecutableName() throws NoSuchMethodException, NoSuchFieldException {
        var crazyA = "<U extends Thread, X extends Map<C, D>, Y extends Cloneable & Serializable, Z, W extends List<?>, T extends Cloneable, V extends Cloneable & Serializable>";
        var crazyB = "Map<A, Map<C[], Map<String, ? extends List<? super E>>>>";
        var crazyC = "Weird.crazy(D, F, U[], X, List<? extends Cloneable>, List<?>)";
        var crazyX = crazyA + " " + crazyB + " " + crazyC;
        var ambig3 = Something.class.getMethod("ambig3", java.util.Date.class, java.sql.Date.class, String.class, Something.class);
        var crazy = Stream.of(Weird.class.getMethods()).filter(m -> m.getName().equals("crazy")).findAny().get();
        var parts = List.of(
                new PartEx(Something.class.getConstructor(), "Something", "Something()"),
                new PartEx(Something.class.getConstructor(int.class), "Something", "Something(int)"),
                new PartEx(Something.class.getMethod("toString"), "Object", "String Object.toString()"),
                new PartEx(Something.class.getMethod("ambig1"), "Something", "java.util.Date Something.ambig1()"),
                new PartEx(Something.class.getMethod("ambig2"), "Something", "java.sql.Date Something.ambig2()"),
                new PartEx(ambig3, "Something", "int Something.ambig3(java.util.Date, java.sql.Date, String, Something)"),
                new PartEx(Something.class.getMethod("staticMethod2", int.class, String.class), "Something", "int Something.staticMethod2(int, String)"),
                new PartEx(crazy, "Weird", crazyX)
        );
        var q = parts.stream()
                .map(e -> Arguments.of(e.c, ForTests.checkEquals(e.a, e.b + "/" + e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, true))));
        var p = parts.stream()
                .map(e -> Arguments.of(e.c, ForTests.checkEquals(e.a, e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, false))));
        return Stream.concat(p, q);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimplifiedExecutableName {0}")
    public void testSimplifiedExecutableName(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testSimplifiedFieldName() throws NoSuchMethodException, NoSuchFieldException {
        var parts = List.of(
                new PartFi(Something.class.getDeclaredField("a"), "Something", "int Something.a"),
                new PartFi(Something.class.getDeclaredField("b"), "Something", "java.sql.Date Something.b"),
                new PartFi(Something.class.getDeclaredField("c"), "Something", "java.util.Date Something.c"),
                new PartFi(Something.class.getDeclaredField("d"), "Something", "String Something.d")
        );
        var p = parts.stream()
                .map(e -> Arguments.of(e.c, ForTests.checkEquals(e.a, e.b + "/" + e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, true))));
        var q = parts.stream()
                .map(e -> Arguments.of(e.c, ForTests.checkEquals(e.a, e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, false))));
        return Stream.concat(p, q);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimplifiedFieldName {0}")
    public void testSimplifiedFieldName(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testNulls() throws Exception {
        return Stream.of(
            n(
                    "getSimplifiedGenericString(Method)",
                    () -> ForTests.testNull("what", () -> NameDictionary.global().getSimplifiedGenericString((Method) null, false))
            ),
            n(
                    "getSimplifiedGenericString(Field)",
                    () -> ForTests.testNull("field", () -> NameDictionary.global().getSimplifiedGenericString((Field) null, false))
            ),
            n(
                    "getSimplifiedGenericString(Method)",
                    () -> ForTests.testNull("what", () -> NameDictionary.global().getSimplifiedGenericString((Method) null, true))
            ),
            n(
                    "getSimplifiedGenericString(Field)",
                    () -> ForTests.testNull("field", () -> NameDictionary.global().getSimplifiedGenericString((Field) null, true))
            )
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testNulls {0}")
    public void testNulls(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
