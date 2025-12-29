package ninja.javahacker.test.magicfactory;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.sqlplus;

import ninja.javahacker.test.ForTests;

public class NameDictionaryTest {

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
        public <U extends Thread> Map<A, Map<C[], Map<String, ? extends List<? super E>>>> crazy(D param, F foo, U uh);
    }

    private static record PartEx(java.lang.reflect.Executable a, String b, String c) {
    }

    private static record PartFi(Field a, String b, String c) {
    }

    @Test
    public void testSimplifiedExecutableName() throws NoSuchMethodException, NoSuchFieldException {
        var ambig3 = Something.class.getMethod("ambig3", java.util.Date.class, java.sql.Date.class, String.class, Something.class);
        var crazy = Weird.class.getMethod("crazy", Cloneable.class, Comparable.class, Thread.class);
        var parts = List.of(
                new PartEx(Something.class.getConstructor(), "Something", "Something()"),
                new PartEx(Something.class.getConstructor(int.class), "Something", "Something(int)"),
                new PartEx(Something.class.getMethod("toString"), "Object", "String Object.toString()"),
                new PartEx(Something.class.getMethod("ambig1"), "Something", "java.util.Date Something.ambig1()"),
                new PartEx(Something.class.getMethod("ambig2"), "Something", "java.sql.Date Something.ambig2()"),
                new PartEx(ambig3, "Something", "int Something.ambig3(java.util.Date, java.sql.Date, String, Something)"),
                new PartEx(Something.class.getMethod("staticMethod2", int.class, String.class), "Something", "int Something.staticMethod2(int, String)"),
                new PartEx(crazy, "Weird", "<U extends Thread> Map<A, Map<C[], Map<String, ? extends List<? super E>>>> Weird.crazy(D, F, U)")
        );
        var q = parts.stream()
                .map(e -> ForTests.checkEquals(e.a, e.b + "/" + e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, true)));
        var p = parts.stream()
                .map(e -> ForTests.checkEquals(e.a, e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, false)));
        var r = Stream.concat(p, q).toList();
        Assertions.assertAll(r);
    }

    @Test
    public void testSimplifiedFieldName() throws NoSuchMethodException, NoSuchFieldException {
        var parts = List.of(
                new PartFi(Something.class.getDeclaredField("a"), "Something", "int Something.a"),
                new PartFi(Something.class.getDeclaredField("b"), "Something", "java.sql.Date Something.b"),
                new PartFi(Something.class.getDeclaredField("c"), "Something", "java.util.Date Something.c"),
                new PartFi(Something.class.getDeclaredField("d"), "Something", "String Something.d")
        );
        var p = parts.stream()
                .map(e -> ForTests.checkEquals(e.a, e.b + "/" + e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, true)));
        var q = parts.stream()
                .map(e -> ForTests.checkEquals(e.a, e.c, x -> NameDictionary.global().getSimplifiedGenericString(x, false)));
        var r = Stream.concat(p, q).toList();
        Assertions.assertAll(r);
    }

    @Test
    @SuppressWarnings("null")
    public void testNulls() throws Exception {
        Assertions.assertAll(
            () -> ForTests.testNull(
                    "what",
                    () -> NameDictionary.global().getSimplifiedGenericString((Method) null, false),
                    "getSimplifiedGenericString(Method)"
            ),
            () -> ForTests.testNull(
                    "field",
                    () -> NameDictionary.global().getSimplifiedGenericString((Field) null, false),
                    "getSimplifiedGenericString(Field)"
            ),
            () -> ForTests.testNull(
                    "what",
                    () -> NameDictionary.global().getSimplifiedGenericString((Method) null, true),
                    "getSimplifiedGenericString(Method)"
            ),
            () -> ForTests.testNull(
                    "field",
                    () -> NameDictionary.global().getSimplifiedGenericString((Field) null, true),
                    "getSimplifiedGenericString(Field)"
            )
        );
    }
}
