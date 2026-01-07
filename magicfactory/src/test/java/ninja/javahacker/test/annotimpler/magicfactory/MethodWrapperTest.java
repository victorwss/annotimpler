package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class MethodWrapperTest {

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

    private void testParam(Parameter p, String name, Type expectedType) {
        Assertions.assertAll(
            () -> Assertions.assertEquals(name, p.getName()),
            () -> Assertions.assertEquals(expectedType, p.getType())
        );
    }

    @Test
    public void testConstructorWrapper() throws Exception {
        var c = Something.class.getConstructor(int.class);
        var w = MethodWrapper.of(c);
        Assertions.assertAll(
            () -> Assertions.assertEquals(1, w.arity()),
            () -> Assertions.assertEquals(false, w.isAbstract()),
            () -> Assertions.assertEquals(true, w.isPublic()),
            () -> Assertions.assertEquals(true, w.isStatic()),
            () -> Assertions.assertEquals(c, w.unwrap()),
            () -> Assertions.assertEquals(w, w.eraseU()),
            () -> Assertions.assertEquals(Something.class, w.getReturnType()),
            () -> Assertions.assertEquals(List.of(int.class), w.getParameterTypes()),
            () -> {
                var ps = w.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(1, ps.size()),
                    () -> testParam(ps.get(0), "xpto", int.class)
                );
            },
            () -> Assertions.assertTrue(w.isAnnotationPresent(Foo.class)),
            () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null), "isAnnotationPresent"),
            () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null), "getAnnotation"),
            () -> Assertions.assertEquals("bar", w.getAnnotation(Foo.class).orElseThrow(AssertionError::new).value()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Bar.class)),
            () -> Assertions.assertTrue(w.getAnnotation(Bar.class).isEmpty()),
            () -> Assertions.assertEquals(Map.of("xpto", 5), w.paramMap(5)),
            () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null), "paramMap-args"),
            () -> {
                var u = w.call(777);
                Assertions.assertEquals(777, u.x);
            },
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call()),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("xxx")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777, "xxx")),
            () -> ForTests.testNull("params", () -> w.call((Object[]) null), "call"),
            () -> Assertions.assertEquals("constructor Something(int)", w.toString()),
            () -> Assertions.assertEquals("Constructor Something(int)", w.toStringUp())
        );
    }

    @Test
    public void testStaticMethodWrapper() throws Exception {
        var c = Something.class.getMethod("foo", int.class, int.class);
        var w = MethodWrapper.of(c);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, w.arity()),
            () -> Assertions.assertEquals(false, w.isAbstract()),
            () -> Assertions.assertEquals(true, w.isPublic()),
            () -> Assertions.assertEquals(true, w.isStatic()),
            () -> Assertions.assertEquals(c, w.unwrap()),
            () -> Assertions.assertEquals(w, w.eraseU()),
            () -> Assertions.assertEquals(int.class, w.getReturnType()),
            () -> Assertions.assertEquals(List.of(int.class, int.class), w.getParameterTypes()),
            () -> {
                var ps = w.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.get(0), "v", int.class),
                    () -> testParam(ps.get(1), "x", int.class)
                );
            },
            () -> Assertions.assertTrue(w.isAnnotationPresent(Foo.class)),
            () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null), "isAnnotationPresent"),
            () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null), "getAnnotation"),
            () -> Assertions.assertEquals("bar", w.getAnnotation(Foo.class).orElseThrow(AssertionError::new).value()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Bar.class)),
            () -> Assertions.assertTrue(w.getAnnotation(Bar.class).isEmpty()),
            () -> Assertions.assertEquals(Map.of("v", 1, "x", 2), w.paramMap(1, 2)),
            () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null), "paramMap-args"),
            () -> {
                var u = w.call(3, 4);
                Assertions.assertEquals(12, u);
            },
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call((Object) null)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call()),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777, "xxx")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(777, 888, 999)),
            () -> ForTests.testNull("params", () -> w.call((Object[]) null), "call"),
            () -> Assertions.assertEquals("method int Something.foo(int, int)", w.toString()),
            () -> Assertions.assertEquals("Method int Something.foo(int, int)", w.toStringUp())
        );
    }

    @Test
    public void testInstanceMethodWrapper() throws Exception {
        var c = Something.class.getMethod("bar", int.class, double.class, String.class);
        var w = MethodWrapper.of(c);
        Assertions.assertAll(
            () -> Assertions.assertEquals(3, w.arity()),
            () -> Assertions.assertEquals(false, w.isAbstract()),
            () -> Assertions.assertEquals(true, w.isPublic()),
            () -> Assertions.assertEquals(false, w.isStatic()),
            () -> Assertions.assertEquals(c, w.unwrap()),
            () -> Assertions.assertEquals(w, w.eraseU()),
            () -> Assertions.assertEquals(String.class, w.getReturnType()),
            () -> Assertions.assertEquals(List.of(int.class, double.class, String.class), w.getParameterTypes()),
            () -> {
                var ps = w.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(3, ps.size()),
                    () -> testParam(ps.get(0), "v", int.class),
                    () -> testParam(ps.get(1), "y", double.class),
                    () -> testParam(ps.get(2), "z", String.class)
                );
            },
            () -> Assertions.assertTrue(w.isAnnotationPresent(Bar.class)),
            () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null), "isAnnotationPresent"),
            () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null), "getAnnotation"),
            () -> Assertions.assertEquals("foo", w.getAnnotation(Bar.class).orElseThrow(AssertionError::new).value()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class)),
            () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty()),
            () -> Assertions.assertEquals(Map.of("v", 1, "y", 2.0, "z", "kkk"), w.paramMap(1, 2.0, "kkk")),
            () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null), "paramMap-args"),
            () -> {
                var u = w.call(new Something(16), 3, 4.0, "ttt");
                Assertions.assertEquals("ttt52.0", u);
            },
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call()),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16))),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777, "xxx", 888.0)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777, "xxx", 888.0, 999)),
            () -> Assertions.assertThrows(NullPointerException.class, () -> w.call((Object) null)),
            () -> Assertions.assertThrows(NullPointerException.class, () -> w.call(null, 777, 888.0, "xxx")),
            () -> Assertions.assertThrows(NullPointerException.class, () -> w.call(null, "xxx")),
            () -> ForTests.testNull("params", () -> w.call((Object[]) null), "call"),
            () -> Assertions.assertEquals("method String Something.bar(int, double, String)", w.toString()),
            () -> Assertions.assertEquals("Method String Something.bar(int, double, String)", w.toStringUp())
        );
    }

    @Test
    public void testInstanceFieldGetterWrapper() throws Exception {
        var c = Something.class.getField("x");
        var w = MethodWrapper.getter(c);
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, w.arity()),
            () -> Assertions.assertEquals(false, w.isAbstract()),
            () -> Assertions.assertEquals(true, w.isPublic()),
            () -> Assertions.assertEquals(false, w.isStatic()),
            () -> Assertions.assertEquals(c, w.unwrap()),
            () -> Assertions.assertEquals(w, w.eraseU()),
            () -> Assertions.assertEquals(int.class, w.getReturnType()),
            () -> Assertions.assertEquals(List.of(), w.getParameterTypes()),
            () -> Assertions.assertEquals(0, w.getParameters().size()),
            () -> Assertions.assertTrue(w.isAnnotationPresent(Bar.class)),
            () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null), "isAnnotationPresent"),
            () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null), "getAnnotation"),
            () -> Assertions.assertEquals("doo", w.getAnnotation(Bar.class).orElseThrow(AssertionError::new).value()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class)),
            () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty()),
            () -> Assertions.assertEquals(Map.of(), w.paramMap()),
            () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null), "paramMap-args"),
            () -> {
                var u = w.call(new Something(16));
                Assertions.assertEquals(16, u);
            },
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call()),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16), 777)),
            () -> Assertions.assertThrows(NullPointerException.class, () -> w.call((Object) null)),
            () -> ForTests.testNull("params", () -> w.call((Object[]) null), "call"),
            () -> Assertions.assertEquals("field int Something.x", w.toString()),
            () -> Assertions.assertEquals("Field int Something.x", w.toStringUp())
        );
    }

    @Test
    public void testStaticFieldGetterWrapper() throws Exception {
        var c = Something.class.getField("XXX");
        var w = MethodWrapper.getter(c);
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, w.arity()),
            () -> Assertions.assertEquals(false, w.isAbstract()),
            () -> Assertions.assertEquals(true, w.isPublic()),
            () -> Assertions.assertEquals(true, w.isStatic()),
            () -> Assertions.assertEquals(c, w.unwrap()),
            () -> Assertions.assertEquals(w, w.eraseU()),
            () -> Assertions.assertEquals(long.class, w.getReturnType()),
            () -> Assertions.assertEquals(List.of(), w.getParameterTypes()),
            () -> Assertions.assertEquals(0, w.getParameters().size()),
            () -> Assertions.assertTrue(w.isAnnotationPresent(Bar.class)),
            () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null), "isAnnotationPresent"),
            () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null), "getAnnotation"),
            () -> Assertions.assertEquals("hoo", w.getAnnotation(Bar.class).orElseThrow(AssertionError::new).value()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class)),
            () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty()),
            () -> Assertions.assertEquals(Map.of(), w.paramMap()),
            () -> ForTests.testNull("args", () -> w.paramMap((Object[]) null), "paramMap-args"),
            () -> {
                var u = w.call();
                Assertions.assertEquals(44L, u);
            },
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call((Object) null)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16))),
            () -> ForTests.testNull("params", () -> w.call((Object[]) null), "call"),
            () -> Assertions.assertEquals("field long Something.XXX", w.toString()),
            () -> Assertions.assertEquals("Field long Something.XXX", w.toStringUp())
        );
    }

    @Test
    public void testValueWrapper() throws Exception {
        var w = MethodWrapper.value("alohomora");
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, w.arity()),
            () -> Assertions.assertEquals(false, w.isAbstract()),
            () -> Assertions.assertEquals(true, w.isPublic()),
            () -> Assertions.assertEquals(true, w.isStatic()),
            () -> Assertions.assertEquals("alohomora", w.unwrap()),
            () -> Assertions.assertEquals(w, w.eraseU()),
            () -> Assertions.assertEquals(String.class, w.getReturnType()),
            () -> Assertions.assertEquals(List.of(), w.getParameterTypes()),
            () -> Assertions.assertEquals(0, w.getParameters().size()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Bar.class)),
            () -> ForTests.testNull("annoClass", () -> w.isAnnotationPresent(null), "isAnnotationPresent"),
            () -> ForTests.testNull("annoClass", () -> w.getAnnotation(null), "getAnnotation"),
            () -> Assertions.assertTrue(w.getAnnotation(Bar.class).isEmpty()),
            () -> Assertions.assertFalse(w.isAnnotationPresent(Foo.class)),
            () -> Assertions.assertTrue(w.getAnnotation(Foo.class).isEmpty()),
            () -> Assertions.assertEquals(Map.of(), w.paramMap()),
            () -> {
                var u = w.call();
                Assertions.assertEquals("alohomora", u);
            },
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call((Object) null)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call("x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> w.call(new Something(16))),
            () -> ForTests.testNull("params", () -> w.call((Object[]) null), "call"),
            () -> Assertions.assertEquals("alohomora", w.toString()),
            () -> Assertions.assertEquals("alohomora", w.toStringUp())
        );
    }

    @Test
    public void testNulls() throws Exception {
        Assertions.assertAll(
            () -> ForTests.testNull("what", () -> {
                MethodWrapper.of((Method) null);
            }, "method"),
            () -> ForTests.testNull("what", () -> {
                MethodWrapper.of((Constructor<?>) null);
            }, "constructor"),
            () -> ForTests.testNull("what", () -> {
                MethodWrapper.of((java.lang.reflect.Executable) null);
            }, "executable"),
            () -> ForTests.testNull("what", () -> {
                MethodWrapper.getter(null);
            }, "getter"),
            () -> ForTests.testNull("what", () -> {
                MethodWrapper.value(null);
            }, "value")
        );
    }
}
