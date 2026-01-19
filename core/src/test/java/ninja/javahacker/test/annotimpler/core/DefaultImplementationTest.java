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

    @Test
    @SuppressWarnings("null")
    public void testHashCodeBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("hashCode"), Bar.class.getMethod("hashCode"), DefaultImplementation.HASH_CODE);
        var a = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "instance",
                () -> DefaultImplementation.of(m).get().execute(null, new Object[0]),
                "hashCode-instance-" + m.getDeclaringClass().getSimpleName()
        ));
        var b = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "args",
                () -> DefaultImplementation.of(m).get().execute(new Object(), (Object[]) null),
                "hashCode-args" + m.getDeclaringClass().getSimpleName()
        ));
        var c = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.of(m).get().execute(new Object(), (Integer) null)
        ));
        var all = Stream.of(a, b, c).flatMap(x -> x).toList();
        Assertions.assertAll(all);
    }

    @Test
    @SuppressWarnings("null")
    public void testToStringBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("toString"), Bar.class.getMethod("toString"), DefaultImplementation.TO_STRING);
        var a = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "instance",
                () -> DefaultImplementation.of(m).get().execute(null, new Object[0]),
                "toString-instance-" + m.getDeclaringClass().getSimpleName()
        ));
        var b = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "args",
                () -> DefaultImplementation.of(m).get().execute(new Object(), (Object[]) null),
                "toString-args" + m.getDeclaringClass().getSimpleName()
        ));
        var c = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.of(m).get().execute(new Object(), (Integer) null)
        ));
        var all = Stream.of(a, b, c).flatMap(x -> x).toList();
        Assertions.assertAll(all);
    }

    @Test
    @SuppressWarnings("null")
    public void testEqualsBad() throws Exception {
        var mtds = List.of(Foo.class.getMethod("equals", Object.class), Bar.class.getMethod("equals", Object.class), DefaultImplementation.EQUALS);
        var a = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "instance",
                () -> DefaultImplementation.of(m).get().execute(null, 42),
                "equals-instance-" + m.getDeclaringClass().getSimpleName()
        ));
        var b = mtds.stream().map(m -> (Executable) () -> ForTests.testNull(
                "args",
                () -> DefaultImplementation.of(m).get().execute(new Object(), (Object[]) null),
                "equals-args" + m.getDeclaringClass().getSimpleName()
        ));
        var c = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.of(m).get().execute(new Object(), new Object[0])
        ));
        var d = mtds.stream().map(m -> (Executable) () -> Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DefaultImplementation.of(m).get().execute(new Object(), 42, 63)
        ));
        var all = Stream.of(a, b, c, d).flatMap(x -> x).toList();
        Assertions.assertAll(all);
    }

    private static interface Bottom {
        public int foo();

        public int bar();
    }

    private static interface Low extends Bottom {
        @Override
        public default int foo() {
            return 42;
        }

        @Override
        public default int bar() {
            return 50;
        }
    }

    private static interface Mid extends Low {
        @Override
        public int foo();

        @Override
        public int bar();
    }

    private static interface High extends Mid {
        @Override
        public default int foo() {
            return 36;
        }

        @Override
        public default int bar() {
            return 13;
        }
    }

    private static interface Top extends High {
    }

    @Test
    @SuppressWarnings("null")
    public void testDefaultImpl() throws Exception {
        var th = Thread.currentThread().getContextClassLoader();
        InvocationHandler ih = (i, n, a) -> {
            throw new AssertionError();
        };
        var b = Proxy.newProxyInstance(th, new Class<?>[] {Bottom.class}, ih);
        var m = Proxy.newProxyInstance(th, new Class<?>[] {Mid.class}, ih);
        var t = Proxy.newProxyInstance(th, new Class<?>[] {Top.class}, ih);
        var tfoo = Top.class.getMethod("foo");
        var tbar = Top.class.getMethod("bar");
        var bfoo = Bottom.class.getMethod("foo");
        var bbar = Bottom.class.getMethod("bar");
        Assertions.assertAll(
                () -> Assertions.assertEquals(30, DefaultImplementation.of(tfoo).get().execute(t)),
                () -> Assertions.assertEquals(13, DefaultImplementation.of(tbar).get().execute(t)),
                () -> Assertions.assertEquals(36, DefaultImplementation.of(tfoo).get().execute(m)),
                () -> Assertions.assertEquals(13, DefaultImplementation.of(tbar).get().execute(m)),
                () -> Assertions.assertEquals(42, DefaultImplementation.of(tfoo).get().execute(b)),
                () -> Assertions.assertEquals(50, DefaultImplementation.of(tbar).get().execute(b)),
                () -> Assertions.assertEquals(30, DefaultImplementation.of(bfoo).get().execute(t)),
                () -> Assertions.assertEquals(13, DefaultImplementation.of(bbar).get().execute(t)),
                () -> Assertions.assertEquals(36, DefaultImplementation.of(bfoo).get().execute(m)),
                () -> Assertions.assertEquals(13, DefaultImplementation.of(bbar).get().execute(m)),
                () -> Assertions.assertEquals(42, DefaultImplementation.of(bfoo).get().execute(b)),
                () -> Assertions.assertEquals(50, DefaultImplementation.of(bbar).get().execute(b))
        );
    }
}
