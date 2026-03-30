package ninja.javahacker.test.annotimpler.core;

import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module org.junit.jupiter.api;

@SuppressWarnings("missing-explicit-ctor")
public class DefaultImplementationTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    public static class Foo {
        @Override
        public String toString() {
            throw new AssertionError();
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
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

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testHashCodeBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("hashCode"), Bar.class.getMethod("hashCode"), Methods.HASH_CODE);
        var inst = r();
        Runnable s = () -> {};
        var a = mtds.stream().map(m -> n(
                "instance-" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull(
                        "instance",
                        () -> DefaultImplementation.forHashCode().execute(null, new Object[0])
                )
        ));
        var b = mtds.stream().map(m -> n(
                "args-" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull(
                        "args",
                        () -> DefaultImplementation.forHashCode().execute(inst, (Object[]) null)
                )
        ));
        var c = mtds.stream().map(m -> n(
                "bad-instance" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forHashCode().execute(s),
                        "Should be a proxy."
                )
        ));
        var d = mtds.stream().map(m -> n(
                "bad-args-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forHashCode().execute(inst, "a"),
                        "Bad arity."
                )
        ));
        return Stream.of(a, b, c, d).flatMap(x -> x);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testToStringBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("toString"), Bar.class.getMethod("toString"), Methods.TO_STRING);
        var inst = r();
        var a = mtds.stream().map(m -> n(
                "instance-" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull(
                        "instance",
                        () -> DefaultImplementation.forToString(Runnable.class).execute(null, new Object[0])
                )
        ));
        var b = mtds.stream().map(m -> n(
                "args-" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull(
                        "args",
                        () -> DefaultImplementation.forToString(Runnable.class).execute(inst, (Object[]) null)
                )
        ));
        var c = mtds.stream().map(m -> n(
                "bad-instance-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forToString(Runnable.class).execute(() -> {}),
                        "Should be a proxy."
                )
        ));
        var d = mtds.stream().map(m -> n(
                "bad-args-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forToString(Runnable.class).execute(inst, "a"),
                        "Bad arity."
                )
        ));
        var e = mtds.stream().map(m -> n(
                "toString-args" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull("iface", () -> DefaultImplementation.forToString(null))
        ));
        return Stream.of(a, b, c, d, e).flatMap(x -> x);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testEqualsBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("equals", Object.class), Bar.class.getMethod("equals", Object.class), Methods.EQUALS);
        var inst = r();
        var a = mtds.stream().map(m -> n(
                "instance-" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull("instance", () -> DefaultImplementation.forEquals().execute(null, 42))
        ));
        var b = mtds.stream().map(m -> n(
                "args-" + m.getDeclaringClass().getSimpleName(),
                () -> ForTests.testNull("args", () -> DefaultImplementation.forEquals().execute(inst, (Object[]) null))
        ));
        var c = mtds.stream().map(m -> n(
                "bad-instance-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute("y", "y"),
                        "Should be a proxy."
                )
        ));
        var d = mtds.stream().map(m -> n(
                "bad-args-0-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute(inst),
                        "Bad arity."
                )
        ));
        var e = mtds.stream().map(m -> n(
                "bad-instance-args-0-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute("a"),
                        "Bad arity."
                )
        ));
        var f = mtds.stream().map(m -> n(
                "bad-instance-args-null-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute(null),
                        "Bad arity."
                )
        ));
        var g = mtds.stream().map(m -> n(
                "bad-args-2-" + m.getDeclaringClass().getSimpleName(),
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute(inst, 42, 63),
                        "Bad arity."
                )
        ));
        return Stream.of(a, b, c, d, e, f, g).flatMap(x -> x);
    }

    @Test
    public void testNonInstantiable() throws Exception {
        ForTests.testNonInstantiable(DefaultImplementation.class);
    }

    @TestFactory
    @SuppressWarnings({"null", "AssertEqualsBetweenInconvertibleTypes"})
    public Stream<DynamicTest> testHashCode() throws Throwable {
        var a = r();
        var b = r();
        var c = t();
        var ha1 = DefaultImplementation.forHashCode().execute(a);
        var ha2 = DefaultImplementation.forHashCode().execute(a);
        var hb = DefaultImplementation.forHashCode().execute(b);
        var hc = DefaultImplementation.forHashCode().execute(c);
        return Stream.of(
                n("a", () -> Assertions.assertEquals(Integer.class, ha1.getClass())),
                n("b", () -> Assertions.assertEquals(Integer.class, ha2.getClass())),
                n("c", () -> Assertions.assertEquals(Integer.class, hb.getClass())),
                n("d", () -> Assertions.assertEquals(Integer.class, hc.getClass())),
                n("e", () -> Assertions.assertEquals(ha1, ha2))
        );
    }

    @TestFactory
    @SuppressWarnings({"null", "AssertEqualsBetweenInconvertibleTypes"})
    public Stream<DynamicTest> testToString() throws Throwable {
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
        return Stream.of(
                n("a", () -> Assertions.assertEquals("impl[" + Runnable.class.getName() + "]-" + ha, s1a)),
                n("b", () -> Assertions.assertEquals("impl[" + Runnable.class.getName() + "]-" + ha, s1b)),
                n("c", () -> Assertions.assertEquals("impl[" + Runnable.class.getName() + "]-" + hb, s2)),
                n("d", () -> Assertions.assertEquals("impl[" + Bar.class.getName() + "]-" + hc, s3))
        );
    }

    @TestFactory
    @SuppressWarnings({"null", "AssertEqualsBetweenInconvertibleTypes"})
    public Stream<DynamicTest> testEquals() throws Throwable {
        var a = r();
        var b = r();
        var c = t();
        return Stream.of(
                n("a-a", () -> Assertions.assertEquals(Boolean.TRUE, DefaultImplementation.forEquals().execute(a, a))),
                n("b-b", () -> Assertions.assertEquals(Boolean.TRUE, DefaultImplementation.forEquals().execute(b, b))),
                n("c-c", () -> Assertions.assertEquals(Boolean.TRUE, DefaultImplementation.forEquals().execute(c, c))),
                n("a-b", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(a, b))),
                n("b-a", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(b, a))),
                n("a-c", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(a, c))),
                n("c-a", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(c, a))),
                n("b-c", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(b, c))),
                n("c-b", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(c, b))),
                n("a-n", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(a, (Object) null))),
                n("b-n", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(b, (Object) null))),
                n("c-n", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(c, (Object) null))),
                n("a-x", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(a, "x"))),
                n("b-x", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(b, "x"))),
                n("c-x", () -> Assertions.assertEquals(Boolean.FALSE, DefaultImplementation.forEquals().execute(c, "x")))
        );
    }
}
