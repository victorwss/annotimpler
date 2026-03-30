package ninja.javahacker.test.annotimpler.magicfactory;

import org.junit.jupiter.api.function.Executable;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unused"})
public class MethodsInvokeTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    public static class LameException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public LameException() {
        }
    }

    public static class RunMe {
        public final String value;

        public RunMe(String value) {
            this.value = value;
        }

        public RunMe(int x) {
            throw new LameException();
        }

        private RunMe(double x) {
            throw new AssertionError();
        }

        public String value2() {
            return value + value;
        }

        public String valueX(String other) {
            return value + other;
        }

        public static String valueY(String other) {
            return other + other;
        }

        public void foo(int x) {
            throw new LameException();
        }

        public static void foo2(int x) {
            throw new LameException();
        }

        private static void foo3() {
            throw new AssertionError();
        }

        private void foo4() {
            throw new AssertionError();
        }

        public static int foo5() {
            return 42;
        }

        public int foo6() {
            return 42;
        }
    }

    @Test
    public void testInvokeConstructor() throws Exception {
        var ctor = RunMe.class.getConstructor(String.class);
        var r = Methods.invoke(ctor, "xoom");
        Assertions.assertEquals("xoom", r.value);
    }

    @Test
    @SuppressWarnings("null")
    public void testInvokeConstructorExec() throws Exception {
        java.lang.reflect.Executable ctor = RunMe.class.getConstructor(String.class);
        var r = (RunMe) Methods.invoke(ctor, "xoom");
        Assertions.assertEquals("xoom", r.value);
    }

    @Test
    public void testInvokeStatic() throws Exception {
        var m = RunMe.class.getMethod("valueY", String.class);
        Assertions.assertEquals("foofoo", Methods.invoke(m, "foo"));
    }

    @Test
    public void testInvokeStatic2() throws Exception {
        var m = RunMe.class.getMethod("foo5");
        Assertions.assertEquals(42, Methods.invoke(m));
    }

    @Test
    public void testInvokeStaticExec() throws Exception {
        java.lang.reflect.Executable m = RunMe.class.getMethod("valueY", String.class);
        Assertions.assertEquals("foofoo", Methods.invoke(m, "foo"));
    }

    @Test
    public void testInvokeInstance() throws Exception {
        var r = new RunMe("xoom");
        var m1 = RunMe.class.getMethod("valueX", String.class);
        var m2 = RunMe.class.getMethod("value2");
        var s1 = (String) Methods.invoke(m1, r, "whoa");
        Assertions.assertEquals("xoomwhoa", s1);
        var s2 = (String) Methods.invoke(m2, r);
        Assertions.assertEquals("xoomxoom", s2);
    }

    @Test
    public void testInvokeInstanceExec() throws Exception {
        var r = new RunMe("xoom");
        java.lang.reflect.Executable m1 = RunMe.class.getMethod("valueX", String.class);
        java.lang.reflect.Executable m2 = RunMe.class.getMethod("value2");
        var s1 = (String) Methods.invoke(m1, r, "whoa");
        Assertions.assertEquals("xoomwhoa", s1);
        var s2 = (String) Methods.invoke(m2, r);
        Assertions.assertEquals("xoomxoom", s2);
    }

    public static abstract class AbstractMess {
        public AbstractMess() {
        }
    }

    @TestFactory
    public Stream<DynamicTest> testInvokeBadException() throws Exception {
        var r = new RunMe("ok");
        var e1 = AbstractMess.class.getConstructor();
        var e2 = RunMe.class.getDeclaredConstructor(double.class);
        var e3 = RunMe.class.getDeclaredMethod("foo3");
        var e4 = RunMe.class.getDeclaredMethod("foo4");
        var e5 = RunMe.class.getConstructor(String.class);
        var e6 = RunMe.class.getMethod("foo2", int.class);
        var e7 = RunMe.class.getMethod("foo", int.class);
        return Stream.of(
            n("a", () -> Assertions.assertThrows(InstantiationException.class  , () -> Methods.invoke(e1))),
            n("b", () -> Assertions.assertThrows(IllegalAccessException.class  , () -> Methods.invoke(e2))),
            n("c", () -> Assertions.assertThrows(IllegalAccessException.class  , () -> Methods.invoke(e3))),
            n("d", () -> Assertions.assertThrows(IllegalAccessException.class  , () -> Methods.invoke(e4, r))),
            n("e", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e5, 123))),
            n("f", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e5))),
            n("g", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e6, "x"))),
            n("h", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e6))),
            n("i", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e7, r, "x"))),
            n("j", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e7, r))),
            n("k", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e7, "c", 5))),
            n("l", () -> Assertions.assertThrows(InstantiationException.class  , () -> Methods.invoke((java.lang.reflect.Executable) e1))),
            n("m", () -> Assertions.assertThrows(IllegalAccessException.class  , () -> Methods.invoke((java.lang.reflect.Executable) e2))),
            n("n", () -> Assertions.assertThrows(IllegalAccessException.class  , () -> Methods.invoke((java.lang.reflect.Executable) e3))),
            n("o", () -> Assertions.assertThrows(IllegalAccessException.class  , () -> Methods.invoke((java.lang.reflect.Executable) e4, r))),
            n("p", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e5, 123))),
            n("q", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e5))),
            n("r", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e6, "x"))),
            n("s", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e6))),
            n("t", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e7, r, "x"))),
            n("u", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e7, r))),
            n("v", () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((java.lang.reflect.Executable) e7, "c", 5)))
        );
    }

    @TestFactory
    public Stream<DynamicTest> testInvokeNullPointerException() throws Exception {
        var e8 = RunMe.class.getMethod("foo6");
        return Stream.of(
            n("m0", () -> Assertions.assertThrows(NullPointerException.class, () -> Methods.invoke(e8))),
            n("e0", () -> Assertions.assertThrows(NullPointerException.class, () -> Methods.invoke((java.lang.reflect.Executable) e8))),
            n("m1", () -> Assertions.assertThrows(NullPointerException.class, () -> Methods.invoke(e8, new Object[0]))),
            n("e1", () -> Assertions.assertThrows(NullPointerException.class, () -> Methods.invoke((java.lang.reflect.Executable) e8, new Object[0]))),
            n("mn", () -> Assertions.assertThrows(NullPointerException.class, () -> Methods.invoke(e8, (Object) null))),
            n("en", () -> Assertions.assertThrows(NullPointerException.class, () -> Methods.invoke((java.lang.reflect.Executable) e8, (Object) null)))
        );
    }

    @TestFactory
    public Stream<DynamicTest> testInvokeInvocationTargetException() throws Exception {
        var r = new RunMe("ok");
        var x1 = RunMe.class.getConstructor(int.class);
        var x2 = RunMe.class.getMethod("foo2", int.class);
        var x3 = RunMe.class.getMethod("foo", int.class);
        return Stream.of(
            n("ctor", () -> {
                var e1 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke(x1, 5));
                Assertions.assertEquals(LameException.class, e1.getCause().getClass());
            }),
            n("exec-ctor", () -> {
                var e1 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke((java.lang.reflect.Executable) x1, 5));
                Assertions.assertEquals(LameException.class, e1.getCause().getClass());
            }),
            n("meth", () -> {
                var e2 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke(x2, 5));
                Assertions.assertEquals(LameException.class, e2.getCause().getClass());
            }),
            n("exec-meth", () -> {
                var e2 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke((java.lang.reflect.Executable) x2, 5));
                Assertions.assertEquals(LameException.class, e2.getCause().getClass());
            }),
            n("meth2", () -> {
                var e3 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke(x3, r, 8));
                Assertions.assertEquals(LameException.class, e3.getCause().getClass());
            }),
            n("exec-meth2", () -> {
                var e3 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke((java.lang.reflect.Executable) x3, r, 8));
                Assertions.assertEquals(LameException.class, e3.getCause().getClass());
            })
        );
    }

    private static void dontRun(String x) {
        throw new AssertionError();
    }

    private static class DontRun {
        public DontRun(String x) {
            throw new AssertionError();
        }
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        Method dont1 = MethodsInvokeTest.class.getDeclaredMethod("dontRun", String.class);
        Constructor<?> dont2 = DontRun.class.getDeclaredConstructor(String.class);
        return Stream.of(
            n("invoke-method-1-what", () -> ForTests.testNull("what", () -> Methods.invoke((Method) null))),
            n("invoke-method-2-what", () -> ForTests.testNull("what", () -> Methods.invoke((Method) null, "x"))),
            n("invoke-method-3-what", () -> ForTests.testNull("what", () -> Methods.invoke((Method) null, (Object[]) null))),
            n("invoke-method-args"  , () -> ForTests.testNull("args", () -> Methods.invoke(dont1, (Object[]) null))),
            n("invoke-ctor-1-what"  , () -> ForTests.testNull("what", () -> Methods.invoke((Constructor<?>) null))),
            n("invoke-ctor-2-what"  , () -> ForTests.testNull("what", () -> Methods.invoke((Constructor<?>) null, "x"))),
            n("invoke-ctor-3-what"  , () -> ForTests.testNull("what", () -> Methods.invoke((Constructor<?>) null, (Object[]) null))),
            n("invoke-ctor-args"    , () -> ForTests.testNull("args", () -> Methods.invoke(dont2, (Object[]) null))),
            n("invoke-exec-1-what"  , () -> ForTests.testNull("what", () -> Methods.invoke((java.lang.reflect.Executable) null))),
            n("invoke-exec-2-what"  , () -> ForTests.testNull("what", () -> Methods.invoke((java.lang.reflect.Executable) null, "x"))),
            n("invoke-exec-3-what"  , () -> ForTests.testNull("what", () -> Methods.invoke((java.lang.reflect.Executable) null, (Object[]) null))),
            n("invoke-exec-m-args"  , () -> ForTests.testNull("args", () -> Methods.invoke((java.lang.reflect.Executable) dont1, (Object[]) null))),
            n("invoke-exec-c-args"  , () -> ForTests.testNull("args", () -> Methods.invoke((java.lang.reflect.Executable) dont2, (Object[]) null)))
        );
    }
}
