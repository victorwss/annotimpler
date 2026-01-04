package ninja.javahacker.test.annotimpler.magicfactory;

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
        var r = (RunMe) Methods.invoke(RunMe.class.getConstructor(String.class), "xoom");
        Assertions.assertEquals("xoom", r.value);
    }

    @Test
    public void testInvokeStatic() throws Exception {
        Assertions.assertEquals("foofoo", Methods.invoke(RunMe.class.getMethod("valueY", String.class), "foo"));
    }

    @Test
    public void testInvokeInstance() throws Exception {
        var r = new RunMe("xoom");
        var s1 = (String) Methods.invoke(RunMe.class.getMethod("valueX", String.class), r, "whoa");
        Assertions.assertEquals("xoomwhoa", s1);
        var s2 = (String) Methods.invoke(RunMe.class.getMethod("value2"), r);
        Assertions.assertEquals("xoomxoom", s2);
    }

    public static abstract class AbstractMess {
        public AbstractMess() {
        }
    }

    @Test
    public void testInvokeBadException() throws Exception {
        var r = new RunMe("ok");
        Assertions.assertAll(
            () -> Assertions.assertThrows(InstantiationException.class, () -> Methods.invoke(AbstractMess.class.getConstructor())),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke(RunMe.class.getDeclaredConstructor(double.class))),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke(RunMe.class.getDeclaredMethod("foo3"))),
            () -> Assertions.assertThrows(IllegalAccessException.class, () -> Methods.invoke(RunMe.class.getDeclaredMethod("foo4"), r)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getConstructor(String.class), 123)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getConstructor(String.class))),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getMethod("foo2", int.class), "x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getMethod("foo2", int.class))),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getMethod("foo", int.class), r, "x")),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getMethod("foo", int.class), r)),
            () -> Assertions.assertThrows(IllegalArgumentException.class, () -> Methods.invoke(RunMe.class.getMethod("foo", int.class), "c", 5))
        );
    }

    @Test
    public void testInvokeInvocationTargetException() throws Exception {
        var r = new RunMe("ok");
        Assertions.assertAll(
            () -> {
                var e1 = Assertions.assertThrows(
                        InvocationTargetException.class,
                        () -> Methods.invoke(RunMe.class.getConstructor(int.class), 5)
                );
                Assertions.assertEquals(LameException.class, e1.getCause().getClass());
            },
            () -> {
                var e2 = Assertions.assertThrows(
                        InvocationTargetException.class,
                        () -> Methods.invoke(RunMe.class.getMethod("foo2", int.class), 5)
                );
                Assertions.assertEquals(LameException.class, e2.getCause().getClass());
            },
            () -> {
                var e3 = Assertions.assertThrows(
                        InvocationTargetException.class,
                        () -> Methods.invoke(RunMe.class.getMethod("foo", int.class), r, 8)
                );
                Assertions.assertEquals(LameException.class, e3.getCause().getClass());
            }
        );
    }
}
