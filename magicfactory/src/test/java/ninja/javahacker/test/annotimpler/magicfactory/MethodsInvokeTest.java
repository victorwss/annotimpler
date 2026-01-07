package ninja.javahacker.test.annotimpler.magicfactory;

import java.lang.reflect.Executable;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class MethodsInvokeTest {

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
    }

    @Test
    public void testInvokeConstructor() throws Exception {
        var ctor = RunMe.class.getConstructor(String.class);
        var r = Methods.invoke(ctor, "xoom");
        Assertions.assertEquals("xoom", r.value);
    }

    @Test
    public void testInvokeConstructorExec() throws Exception {
        Executable ctor = RunMe.class.getConstructor(String.class);
        var r = (RunMe) Methods.invoke(ctor, "xoom");
        Assertions.assertEquals("xoom", r.value);
    }

    @Test
    public void testInvokeStatic() throws Exception {
        var m = RunMe.class.getMethod("valueY", String.class);
        Assertions.assertEquals("foofoo", Methods.invoke(m, "foo"));
    }

    @Test
    public void testInvokeStaticExec() throws Exception {
        Executable m = RunMe.class.getMethod("valueY", String.class);
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
        Executable m1 = RunMe.class.getMethod("valueX", String.class);
        Executable m2 = RunMe.class.getMethod("value2");
        var s1 = (String) Methods.invoke(m1, r, "whoa");
        Assertions.assertEquals("xoomwhoa", s1);
        var s2 = (String) Methods.invoke(m2, r);
        Assertions.assertEquals("xoomxoom", s2);
    }

    public static abstract class AbstractMess {
        public AbstractMess() {
        }
    }

    @Test
    public void testInvokeBadException() throws Exception {
        var r = new RunMe("ok");
        var e1 = AbstractMess.class.getConstructor();
        var e2 = RunMe.class.getDeclaredConstructor(double.class);
        var e3 = RunMe.class.getDeclaredMethod("foo3");
        var e4 = RunMe.class.getDeclaredMethod("foo4");
        var e5 = RunMe.class.getConstructor(String.class);
        var e6 = RunMe.class.getMethod("foo2", int.class);
        var e7 = RunMe.class.getMethod("foo", int.class);
        Assertions.assertAll(
            () -> Assertions.assertThrows(InstantiationException.class, () -> Methods.invoke(e1)),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke(e2)),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke(e3)),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke(e4, r)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e5, 123)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e5)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e6, "x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e6)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e7, r, "x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e7, r)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(e7, "c", 5)),
            () -> Assertions.assertThrows(InstantiationException.class, () -> Methods.invoke((Executable) e1)),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke((Executable) e2)),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke((Executable) e3)),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke((Executable) e4, r)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e5, 123)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e5)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e6, "x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e6)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e7, r, "x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e7, r)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke((Executable) e7, "c", 5))
        );
    }

    @Test
    public void testInvokeInvocationTargetException() throws Exception {
        var r = new RunMe("ok");
        var x1 = RunMe.class.getConstructor(int.class);
        var x2 = RunMe.class.getMethod("foo2", int.class);
        var x3 = RunMe.class.getMethod("foo", int.class);
        Assertions.assertAll(
            () -> {
                var e1 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke(x1, 5));
                Assertions.assertEquals(LameException.class, e1.getCause().getClass());
            },
            () -> {
                var e1 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke((Executable) x1, 5));
                Assertions.assertEquals(LameException.class, e1.getCause().getClass());
            },
            () -> {
                var e2 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke(x2, 5));
                Assertions.assertEquals(LameException.class, e2.getCause().getClass());
            },
            () -> {
                var e2 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke((Executable) x2, 5));
                Assertions.assertEquals(LameException.class, e2.getCause().getClass());
            },
            () -> {
                var e3 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke(x3, r, 8));
                Assertions.assertEquals(LameException.class, e3.getCause().getClass());
            },
            () -> {
                var e3 = Assertions.assertThrows(InvocationTargetException.class, () -> Methods.invoke((Executable) x3, r, 8));
                Assertions.assertEquals(LameException.class, e3.getCause().getClass());
            }
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

    @Test
    @SuppressWarnings("null")
    public void testNulls() throws Exception {
        Method dont1 = MethodsInvokeTest.class.getDeclaredMethod("dontRun", String.class);
        Constructor<?> dont2 = DontRun.class.getDeclaredConstructor(String.class);
        Assertions.assertAll(
            () -> ForTests.testNull("what", () -> Methods.invoke((Method) null), "invoke-method-1-what"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Method) null, "x"), "invoke-method-2-what"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Method) null, (Object[]) null), "invoke-method-3-what"),
            () -> ForTests.testNull("args", () -> Methods.invoke(dont1, (Object[]) null), "invoke-method-args"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Constructor<?>) null), "invoke-ctor-1-what"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Constructor<?>) null, "x"), "invoke-ctor-2-what"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Constructor<?>) null, (Object[]) null), "invoke-ctor-3-what"),
            () -> ForTests.testNull("args", () -> Methods.invoke(dont2, (Object[]) null), "invoke-ctor-args"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Executable) null), "invoke-exec-1-what"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Executable) null, "x"), "invoke-exec-2-what"),
            () -> ForTests.testNull("what", () -> Methods.invoke((Executable) null, (Object[]) null), "invoke-exec-3-what"),
            () -> ForTests.testNull("args", () -> Methods.invoke((Executable) dont1, (Object[]) null), "invoke-exec-m-args"),
            () -> ForTests.testNull("args", () -> Methods.invoke((Executable) dont2, (Object[]) null), "invoke-exec-c-args")
        );
    }
}
