package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class MethodWrapperTest {

    private static Arguments n(String name, Executable ctx) {
        return Arguments.of(name, ctx);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Bar {
        public String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Foo {
        public String value();
    }

    public static class Something {
        @Bar("hoo")
        public static final long XXX = 44L;

        @Bar("doo")
        public final int x;

        @Foo("bar")
        public Something(int xpto) {
            this.x = xpto;
        }

        @Foo("bar")
        public static int foo(int v, int x) {
            return v * x;
        }

        @Bar("foo")
        public String bar(int v, double y, String z) {
            return z + (v * x + y);
        }
    }

    private static void testParam(java.lang.reflect.Parameter p, String name, Type expectedType) {
        Assertions.assertAll(
            () -> Assertions.assertEquals(name, p.getName()),
            () -> Assertions.assertEquals(expectedType, p.getType())
        );
    }

    private static Stream<Arguments> testConstructorWrapper() throws Exception {
        var c = Something.class.getConstructor(int.class);
        var w = MethodWrapper.of(c);
        return Stream.of(
            n("a", () -> Assertions.assertEquals(1, w.arity())),
            n("b", () -> Assertions.assertEquals(false, w.isAbstract())),
            n("c", () -> Assertions.assertEquals(true, w.isPublic())),
            n("d", () -> Assertions.assertEquals(true, w.isStatic())),
            n("e", () -> Assertions.assertEquals(c, w.unwrap())),
            n("f", () -> Assertions.assertEquals(w, w.eraseU())),
            n("g", () -> Assertions.assertEquals(Something.class, w.getReturnType())),
            n("h", () -> Assertions.assertEquals(List.of(int.class), w.getParameterTypes())),
            n("i", () -> {
                var ps = w.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(1, ps.size()),
                    () -> testParam(ps.get(0), "xpto", int.class)
                );
            }),
            n("j", () -> Assertions.assertTrue(w.isAnnotationPresent(Foo.class))),
            n("k", () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null))),
            n("l", () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null))),
            n("m", () -> Assertions.assertEquals("bar", w.getAnnotation(Foo.class).orElseThrow(AssertionError::new).value())),
            n("n", () -> Assertions.assertFalse(w.isAnnotationPresent(Bar.class))),
            n("o", () -> Assertions.assertTrue(w.getAnnotation(Bar.class).isEmpty())),
            n("p", () -> Assertions.assertEquals(Map.of("xpto", 5), w.paramMap(5))),
            n("q", () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null))),
            n("r", () -> {
                var u = w.call(777);
                Assertions.assertEquals(777, u.x);
            }),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call())),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("xxx"))),
            n("u", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777, "xxx"))),
            n("v", () -> ForTests.testNull("params", () -> w.call((Object[]) null))),
            n("w", () -> Assertions.assertEquals("constructor Something(int)", w.toString())),
            n("x", () -> Assertions.assertEquals("Constructor Something(int)", w.toStringUp()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testConstructorWrapper {0}")
    public void testConstructorWrapper(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testStaticMethodWrapper() throws Exception {
        var c = Something.class.getMethod("foo", int.class, int.class);
        var w = MethodWrapper.of(c);
        return Stream.of(
            n("a", () -> Assertions.assertEquals(2, w.arity())),
            n("b", () -> Assertions.assertEquals(false, w.isAbstract())),
            n("c", () -> Assertions.assertEquals(true, w.isPublic())),
            n("d", () -> Assertions.assertEquals(true, w.isStatic())),
            n("e", () -> Assertions.assertEquals(c, w.unwrap())),
            n("f", () -> Assertions.assertEquals(w, w.eraseU())),
            n("g", () -> Assertions.assertEquals(int.class, w.getReturnType())),
            n("h", () -> Assertions.assertEquals(List.of(int.class, int.class), w.getParameterTypes())),
            n("i", () -> {
                var ps = w.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.get(0), "v", int.class),
                    () -> testParam(ps.get(1), "x", int.class)
                );
            }),
            n("j", () -> Assertions.assertTrue(w.isAnnotationPresent(Foo.class))),
            n("k", () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null))),
            n("l", () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null))),
            n("m", () -> Assertions.assertEquals("bar", w.getAnnotation(Foo.class).orElseThrow(AssertionError::new).value())),
            n("n", () -> Assertions.assertFalse(w.isAnnotationPresent(Bar.class))),
            n("o", () -> Assertions.assertTrue(w.getAnnotation(Bar.class).isEmpty())),
            n("p", () -> Assertions.assertEquals(Map.of("v", 1, "x", 2), w.paramMap(1, 2))),
            n("q", () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null))),
            n("r", () -> {
                var u = w.call(3, 4);
                Assertions.assertEquals(12, u);
            }),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call((Object) null))),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x"))),
            n("u", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call())),
            n("v", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777))),
            n("w", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777, "xxx"))),
            n("x", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777, 888, 999))),
            n("y", () -> ForTests.testNull("params", () -> w.call((Object[]) null))),
            n("z", () -> Assertions.assertEquals("method int Something.foo(int, int)", w.toString())),
            n("aa", () -> Assertions.assertEquals("Method int Something.foo(int, int)", w.toStringUp()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testStaticMethodWrapper {0}")
    public void testStaticMethodWrapper(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testInstanceMethodWrapper() throws Exception {
        var c = Something.class.getMethod("bar", int.class, double.class, String.class);
        var w = MethodWrapper.of(c);
        return Stream.of(
            n("a", () -> Assertions.assertEquals(3, w.arity())),
            n("b", () -> Assertions.assertEquals(false, w.isAbstract())),
            n("c", () -> Assertions.assertEquals(true, w.isPublic())),
            n("d", () -> Assertions.assertEquals(false, w.isStatic())),
            n("e", () -> Assertions.assertEquals(c, w.unwrap())),
            n("f", () -> Assertions.assertEquals(w, w.eraseU())),
            n("g", () -> Assertions.assertEquals(String.class, w.getReturnType())),
            n("h", () -> Assertions.assertEquals(List.of(int.class, double.class, String.class), w.getParameterTypes())),
            n("i", () -> {
                var ps = w.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(3, ps.size()),
                    () -> testParam(ps.get(0), "v", int.class),
                    () -> testParam(ps.get(1), "y", double.class),
                    () -> testParam(ps.get(2), "z", String.class)
                );
            }),
            n("j", () -> Assertions.assertTrue(w.isAnnotationPresent(Bar.class))),
            n("k", () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null))),
            n("l", () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null))),
            n("m", () -> Assertions.assertEquals("foo", w.getAnnotation(Bar.class).orElseThrow(AssertionError::new).value())),
            n("n", () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class))),
            n("o", () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty())),
            n("p", () -> Assertions.assertEquals(Map.of("v", 1, "y", 2.0, "z", "kkk"), w.paramMap(1, 2.0, "kkk"))),
            n("q", () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null))),
            n("r", () -> {
                var u = w.call(new Something(16), 3, 4.0, "ttt");
                Assertions.assertEquals("ttt52.0", u);
            }),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call())),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x"))),
            n("u", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16)))),
            n("v", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777))),
            n("w", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777, "xxx", 888.0))),
            n("x", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777, "xxx", 888.0, 999))),
            n("y", () -> Assertions.assertThrows(NullPointerException.class, () -> w.call((Object) null))),
            n("z", () -> Assertions.assertThrows(NullPointerException.class, () -> w.call(null, 777, 888.0, "xxx"))),
            n("aa", () -> Assertions.assertThrows(NullPointerException.class, () -> w.call(null, "xxx"))),
            n("ab", () -> ForTests.testNull("params", () -> w.call((Object[]) null))),
            n("ac", () -> Assertions.assertEquals("method String Something.bar(int, double, String)", w.toString())),
            n("ad", () -> Assertions.assertEquals("Method String Something.bar(int, double, String)", w.toStringUp()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testInstanceMethodWrapper {0}")
    public void testInstanceMethodWrapper(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testInstanceFieldGetterWrapper() throws Exception {
        var c = Something.class.getField("x");
        var w = MethodWrapper.getter(c);
        return Stream.of(
            n("a", () -> Assertions.assertEquals(0, w.arity())),
            n("b", () -> Assertions.assertEquals(false, w.isAbstract())),
            n("c", () -> Assertions.assertEquals(true, w.isPublic())),
            n("d", () -> Assertions.assertEquals(false, w.isStatic())),
            n("e", () -> Assertions.assertEquals(c, w.unwrap())),
            n("f", () -> Assertions.assertEquals(w, w.eraseU())),
            n("g", () -> Assertions.assertEquals(int.class, w.getReturnType())),
            n("h", () -> Assertions.assertEquals(List.of(), w.getParameterTypes())),
            n("i", () -> Assertions.assertEquals(0, w.getParameters().size())),
            n("j", () -> Assertions.assertTrue(w.isAnnotationPresent(Bar.class))),
            n("k", () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null))),
            n("l", () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null))),
            n("m", () -> Assertions.assertEquals("doo", w.getAnnotation(Bar.class).orElseThrow(AssertionError::new).value())),
            n("n", () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class))),
            n("o", () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty())),
            n("p", () -> Assertions.assertEquals(Map.of(), w.paramMap())),
            n("q", () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null))),
            n("r", () -> {
                var u = w.call(new Something(16));
                Assertions.assertEquals(16, u);
            }),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call())),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x"))),
            n("u", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777))),
            n("v", () -> Assertions.assertThrows(NullPointerException.class, () -> w.call((Object) null))),
            n("w", () -> ForTests.testNull("params", () -> w.call((Object[]) null))),
            n("x", () -> Assertions.assertEquals("field int Something.x", w.toString())),
            n("y", () -> Assertions.assertEquals("Field int Something.x", w.toStringUp()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testInstanceFieldGetterWrapper {0}")
    public void testInstanceFieldGetterWrapper(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testStaticFieldGetterWrapper() throws Exception {
        var c = Something.class.getField("XXX");
        var w = MethodWrapper.getter(c);
        return Stream.of(
            n("a", () -> Assertions.assertEquals(0, w.arity())),
            n("b", () -> Assertions.assertEquals(false, w.isAbstract())),
            n("c", () -> Assertions.assertEquals(true, w.isPublic())),
            n("d", () -> Assertions.assertEquals(true, w.isStatic())),
            n("e", () -> Assertions.assertEquals(c, w.unwrap())),
            n("f", () -> Assertions.assertEquals(w, w.eraseU())),
            n("g", () -> Assertions.assertEquals(long.class, w.getReturnType())),
            n("h", () -> Assertions.assertEquals(List.of(), w.getParameterTypes())),
            n("i", () -> Assertions.assertEquals(0, w.getParameters().size())),
            n("j", () -> Assertions.assertTrue(w.isAnnotationPresent(Bar.class))),
            n("k", () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null))),
            n("l", () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null))),
            n("m", () -> Assertions.assertEquals("hoo", w.getAnnotation(Bar.class).orElseThrow(AssertionError::new).value())),
            n("n", () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class))),
            n("o", () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty())),
            n("p", () -> Assertions.assertEquals(Map.of(), w.paramMap())),
            n("q", () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null))),
            n("r", () -> {
                var u = w.call();
                Assertions.assertEquals(44L, u);
            }),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call((Object) null))),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x"))),
            n("u", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16)))),
            n("v", () -> ForTests.testNull("params", () -> w.call((Object[]) null))),
            n("w", () -> Assertions.assertEquals("field long Something.XXX", w.toString())),
            n("x", () -> Assertions.assertEquals("Field long Something.XXX", w.toStringUp()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testStaticFieldGetterWrapper {0}")
    public void testStaticFieldGetterWrapper(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testValueWrapper() throws Exception {
        var w = MethodWrapper.value("alohomora");
        return Stream.of(
            n("a", () -> Assertions.assertEquals(0, w.arity())),
            n("b", () -> Assertions.assertEquals(false, w.isAbstract())),
            n("c", () -> Assertions.assertEquals(true, w.isPublic())),
            n("d", () -> Assertions.assertEquals(true, w.isStatic())),
            n("e", () -> Assertions.assertEquals("alohomora", w.unwrap())),
            n("f", () -> Assertions.assertEquals(w, w.eraseU())),
            n("g", () -> Assertions.assertEquals(String.class, w.getReturnType())),
            n("h", () -> Assertions.assertEquals(List.of(), w.getParameterTypes())),
            n("i", () -> Assertions.assertEquals(0, w.getParameters().size())),
            n("j", () -> Assertions.assertFalse(w.isAnnotationPresent(Bar.class))),
            n("k", () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null))),
            n("l", () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null))),
            n("m", () -> Assertions.assertTrue(w.getAnnotation(Bar.class).isEmpty())),
            n("n", () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class))),
            n("o", () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty())),
            n("p", () -> Assertions.assertEquals(Map.of(), w.paramMap())),
            n("q", () -> {
                var u = w.call();
                Assertions.assertEquals("alohomora", u);
            }),
            n("r", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call((Object) null))),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x"))),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16)))),
            n("u", () -> ForTests.testNull("params", () -> w.call((Object[]) null))),
            n("v", () -> Assertions.assertEquals("alohomora", w.toString())),
            n("w", () -> Assertions.assertEquals("alohomora", w.toStringUp()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testValueWrapper {0}")
    public void testValueWrapper(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testNulls() throws Exception {
        return Stream.of(
            n("method", () -> ForTests.testNull("what", () -> MethodWrapper.of((Method) null))),
            n("constructor", () -> ForTests.testNull("what", () -> MethodWrapper.of((Constructor<?>) null))),
            n("executable", () -> ForTests.testNull("what", () -> MethodWrapper.of((java.lang.reflect.Executable) null))),
            n("getter", () -> ForTests.testNull("what", () -> MethodWrapper.getter(null))),
            n("value", () -> ForTests.testNull("what", () -> MethodWrapper.value(null)))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testNulls {0}")
    public void testNulls(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
