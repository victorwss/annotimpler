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

        @Override
        @SuppressWarnings("all")
        public Foo clone() {
            throw new AssertionError();
        }

        @Deprecated
        @SuppressWarnings({"all", "removal"})
        public void finalize() {
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

        public Bar clone();

        @Deprecated
        @SuppressWarnings({"all", "removal"})
        public void finalize();
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
        var inst = r();
        Runnable s = () -> {};
        var a = n(
                "instance",
                () -> ForTests.testNull(
                        "instance",
                        () -> DefaultImplementation.forHashCode().execute(null, new Object[0])
                )
        );
        var b = n(
                "args",
                () -> ForTests.testNull(
                        "args",
                        () -> DefaultImplementation.forHashCode().execute(inst, (Object[]) null)
                )
        );
        var c = n(
                "bad-instance",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forHashCode().execute(s),
                        "Should be a proxy."
                )
        );
        var d = n(
                "bad-args",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forHashCode().execute(inst, "a"),
                        "Bad arity."
                )
        );
        return Stream.of(a, b, c, d);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testToStringBad() throws Exception {
        var inst = r();
        var a = n(
                "instance",
                () -> ForTests.testNull(
                        "instance",
                        () -> DefaultImplementation.forToString(Runnable.class).execute(null, new Object[0])
                )
        );
        var b = n(
                "args",
                () -> ForTests.testNull(
                        "args",
                        () -> DefaultImplementation.forToString(Runnable.class).execute(inst, (Object[]) null)
                )
        );
        var c = n(
                "bad-instance",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forToString(Runnable.class).execute(() -> {}),
                        "Should be a proxy."
                )
        );
        var d = n(
                "bad-args",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forToString(Runnable.class).execute(inst, "a"),
                        "Bad arity."
                )
        );
        var e = n(
                "toString-args",
                () -> ForTests.testNull("iface", () -> DefaultImplementation.forToString(null))
        );
        return Stream.of(a, b, c, d, e);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testEqualsBad() throws Exception {
        var inst = r();
        var a = n(
                "instance",
                () -> ForTests.testNull("instance", () -> DefaultImplementation.forEquals().execute(null, 42))
        );
        var b = n(
                "args",
                () -> ForTests.testNull("args", () -> DefaultImplementation.forEquals().execute(inst, (Object[]) null))
        );
        var c = n(
                "bad-instance",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute("y", "y"),
                        "Should be a proxy."
                )
        );
        var d = n(
                "bad-args-0",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute(inst),
                        "Bad arity."
                )
        );
        var e = n(
                "bad-instance-args-0",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute("a"),
                        "Bad arity."
                )
        );
        var f = n(
                "bad-instance-args-null",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute(null),
                        "Bad arity."
                )
        );
        var g = n(
                "bad-args-2",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forEquals().execute(inst, 42, 63),
                        "Bad arity."
                )
        );
        return Stream.of(a, b, c, d, e, f, g);
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

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testClone() throws Throwable {
        var a = r();
        Assertions.assertThrows(CloneNotSupportedException.class, () -> DefaultImplementation.forClone().execute(a));
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testFinalize() throws Throwable {
        var a = r();
        Assertions.assertNull(DefaultImplementation.forFinalize().execute(a));
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testCloneBad() throws Exception {
        var inst = r();
        Runnable s = () -> {};
        var a = n(
                "instance",
                () -> ForTests.testNull(
                        "instance",
                        () -> DefaultImplementation.forClone().execute(null, new Object[0])
                )
        );
        var b = n(
                "args",
                () -> ForTests.testNull(
                        "args",
                        () -> DefaultImplementation.forClone().execute(inst, (Object[]) null)
                )
        );
        var c = n(
                "bad-instance",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forClone().execute(s),
                        "Should be a proxy."
                )
        );
        var d = n(
                "bad-args",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forClone().execute(inst, "a"),
                        "Bad arity."
                )
        );
        return Stream.of(a, b, c, d);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testFinalizeBad() throws Exception {
        var inst = r();
        Runnable s = () -> {};
        var a = n(
                "instance",
                () -> ForTests.testNull(
                        "instance",
                        () -> DefaultImplementation.forFinalize().execute(null, new Object[0])
                )
        );
        var b = n(
                "args",
                () -> ForTests.testNull(
                        "args",
                        () -> DefaultImplementation.forFinalize().execute(inst, (Object[]) null)
                )
        );
        var c = n(
                "bad-instance",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forFinalize().execute(s),
                        "Should be a proxy."
                )
        );
        var d = n(
                "bad-args",
                () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DefaultImplementation.forFinalize().execute(inst, "a"),
                        "Bad arity."
                )
        );
        return Stream.of(a, b, c, d);
    }
}
