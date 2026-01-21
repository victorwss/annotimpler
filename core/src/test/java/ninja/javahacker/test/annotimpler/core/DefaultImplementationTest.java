package ninja.javahacker.test.annotimpler.core;

import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module org.junit.jupiter.api;

@SuppressWarnings("missing-explicit-ctor")
public class DefaultImplementationTest {

    public static class Foo {
        @Override
        public String toString() {
            throw new AssertionError();
        }

        @Override
        public boolean equals(Object other) {
            throw new AssertionError();
        }

        @Override
        public int hashCode() {
            throw new AssertionError();
        }
    }

    public static interface Bar {
        @Override
        public String toString();

        @Override
        public boolean equals(Object other);

        @Override
        public int hashCode();
    }

    private static Runnable r() {
        InvocationHandler ih = (i, m, a) -> {
            throw new AssertionError();
        };
        var cl = Thread.currentThread().getContextClassLoader();
        return (Runnable) Proxy.newProxyInstance(cl, new Class<?>[] {Runnable.class}, ih);
    }

    private static Bar t() {
        InvocationHandler ih = (i, m, a) -> {
            throw new AssertionError();
        };
        var cl = Thread.currentThread().getContextClassLoader();
        return (Bar) Proxy.newProxyInstance(cl, new Class<?>[] {Bar.class}, ih);
    }

    @Test
    @SuppressWarnings("null")
    public void testHashCodeBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("hashCode"), Bar.class.getMethod("hashCode"), Methods.HASH_CODE);
        var inst = r();
        Runnable s = () -> {};
        var a = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "instance",
                () -> DefaultImplementation.forHashCode().execute(null, new Object[0]),
                "hashCode-instance-" + m.getDeclaringClass().getSimpleName()
        ));
        var b = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "args",
                () -> DefaultImplementation.forHashCode().execute(inst, (Object[]) null),
                "hashCode-args" + m.getDeclaringClass().getSimpleName()
        ));
        var c = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.forHashCode().execute(s),
                "Should be a proxy."
        ));
        var d = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.forHashCode().execute(inst, "a"),
                "Bad arity."
        ));
        var all = Stream.of(a, b, c, d).flatMap(x -> x).toList();
        Assertions.assertAll(all);
    }

    @Test
    @SuppressWarnings("null")
    public void testToStringBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("toString"), Bar.class.getMethod("toString"), Methods.TO_STRING);
        var inst = r();
        var a = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "instance",
                () -> DefaultImplementation.forToString(Runnable.class).execute(null, new Object[0]),
                "toString-instance-" + m.getDeclaringClass().getSimpleName()
        ));
        var b = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "args",
                () -> DefaultImplementation.forToString(Runnable.class).execute(inst, (Object[]) null),
                "toString-args" + m.getDeclaringClass().getSimpleName()
        ));
        var c = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.forToString(Runnable.class).execute(() -> {}),
                "Should be a proxy."
        ));
        var d = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.forToString(Runnable.class).execute(inst, "a"),
                "Bad arity."
        ));
        var all = Stream.of(a, b, c, d).flatMap(x -> x).toList();
        Assertions.assertAll(all);
    }

    @Test
    @SuppressWarnings("null")
    public void testEqualsBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("equals", Object.class), Bar.class.getMethod("equals", Object.class), Methods.EQUALS);
        var inst = r();
        Runnable s = () -> {};
        var a = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "instance",
                () -> DefaultImplementation.forEquals().execute(null, 42),
                "equals-instance-" + m.getDeclaringClass().getSimpleName()
        ));
        var b = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "args",
                () -> DefaultImplementation.forEquals().execute(inst, (Object[]) null),
                "equals-args" + m.getDeclaringClass().getSimpleName()
        ));
        var c = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.forEquals().execute(s),
                "Should be a proxy."
        ));
        var d = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.forEquals().execute(inst, 42, 63),
                "Bad arity."
        ));
        var all = Stream.of(a, b, c, d).flatMap(x -> x).toList();
        Assertions.assertAll(all);
    }

    @Test
    public void testNonInstantiable() throws Exception {
        ForTests.testNonInstantiable(DefaultImplementation.class);
    }

    @Test
    @SuppressWarnings({"null", "AssertEqualsBetweenInconvertibleTypes"})
    public void testHashCode() throws Throwable {
        var a = r();
        var b = r();
        var c = t();
        var ha1 = DefaultImplementation.forHashCode().execute(a);
        var ha2 = DefaultImplementation.forHashCode().execute(a);
        var hb = DefaultImplementation.forHashCode().execute(b);
        var hc = DefaultImplementation.forHashCode().execute(c);
        Assertions.assertAll(
                () -> Assertions.assertEquals(Integer.class, ha1.getClass()),
                () -> Assertions.assertEquals(Integer.class, ha2.getClass()),
                () -> Assertions.assertEquals(Integer.class, hb.getClass()),
                () -> Assertions.assertEquals(Integer.class, hc.getClass()),
                () -> Assertions.assertEquals(ha1, ha2)
        );
    }

    @Test
    @SuppressWarnings({"null", "AssertEqualsBetweenInconvertibleTypes"})
    public void testToString() throws Throwable {
        var a = r();
        var b = r();
        var c = t();
        var ha = System.identityHashCode(a);
        var hb = System.identityHashCode(b);
        var hc = System.identityHashCode(c);
        var s1a = DefaultImplementation.forToString(Runnable.class).execute(a);
        var s1b = DefaultImplementation.forToString(Runnable.class).execute(a);
        var s2 = DefaultImplementation.forToString(Runnable.class).execute(b);
        var s3 = DefaultImplementation.forToString(Bar.class).execute(c);
        Assertions.assertAll(
                () -> Assertions.assertEquals("impl[" + Runnable.class.getName() + "]-" + ha, s1a),
                () -> Assertions.assertEquals("impl[" + Runnable.class.getName() + "]-" + ha, s1b),
                () -> Assertions.assertEquals("impl[" + Runnable.class.getName() + "]-" + hb, s2),
                () -> Assertions.assertEquals("impl[" + Bar.class.getName() + "]-" + hc, s3)
        );
    }

    @Test
    @SuppressWarnings({"null", "AssertEqualsBetweenInconvertibleTypes"})
    public void testEquals() throws Throwable {
        var a = r();
        var b = r();
        var c = t();
        var eq1 = DefaultImplementation.forEquals().execute(a, a);
        var eq2 = DefaultImplementation.forEquals().execute(b, b);
        var eq3 = DefaultImplementation.forEquals().execute(c, c);
        var eq4 = DefaultImplementation.forEquals().execute(a, b);
        var eq5 = DefaultImplementation.forEquals().execute(b, a);
        var eq6 = DefaultImplementation.forEquals().execute(a, c);
        var eq7 = DefaultImplementation.forEquals().execute(c, a);
        var eq8 = DefaultImplementation.forEquals().execute(b, c);
        var eq9 = DefaultImplementation.forEquals().execute(a, (Object) null);
        var eq10 = DefaultImplementation.forEquals().execute(b, (Object) null);
        var eq11 = DefaultImplementation.forEquals().execute(c, (Object) null);
        var eq12 = DefaultImplementation.forEquals().execute(a, "x");
        var eq13 = DefaultImplementation.forEquals().execute(b, "y");
        var eq14 = DefaultImplementation.forEquals().execute(c, "z");
        Assertions.assertAll(
                () -> Assertions.assertEquals(Boolean.TRUE, eq1),
                () -> Assertions.assertEquals(Boolean.TRUE, eq2),
                () -> Assertions.assertEquals(Boolean.TRUE, eq3),
                () -> Assertions.assertEquals(Boolean.FALSE, eq4),
                () -> Assertions.assertEquals(Boolean.FALSE, eq5),
                () -> Assertions.assertEquals(Boolean.FALSE, eq6),
                () -> Assertions.assertEquals(Boolean.FALSE, eq7),
                () -> Assertions.assertEquals(Boolean.FALSE, eq8),
                () -> Assertions.assertEquals(Boolean.FALSE, eq9),
                () -> Assertions.assertEquals(Boolean.FALSE, eq10),
                () -> Assertions.assertEquals(Boolean.FALSE, eq11),
                () -> Assertions.assertEquals(Boolean.FALSE, eq12),
                () -> Assertions.assertEquals(Boolean.FALSE, eq13),
                () -> Assertions.assertEquals(Boolean.FALSE, eq14)
        );
    }
}
