package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class MethodsDefaultImplTest {

    public static interface A1 {
        public default int foo(int x) {
            return 1;
        }
    }

    public static interface B1 {
        public int foo(int x);
    }

    public static interface B2 extends B1 {
        @Override
        public default int foo(int x) {
            return 2;
        }

        public default int foo(String x) {
            throw new AssertionError();
        }

        public default int goo() {
            throw new AssertionError();
        }
    }

    public static interface C1 {
        public int foo(int x);
    }

    public static interface C2 {
        public int foo(int x);
    }

    public static interface C3 extends C1, C2 {
        @Override
        public default int foo(int x) {
            return 3;
        }
    }

    public static interface D2 extends C1 {
        @Override
        public default int foo(int x) {
            return 4;
        }
    }

    public static interface D3A extends D2 {
        @Override
        public int foo(int x);
    }

    public static interface D3B extends D3A {
    }

    public static interface E3 extends C1 {
        @Override
        public int foo(int x);
    }

    public static interface E4A extends B2, E3 {
        @Override
        public int foo(int x);
    }

    public static interface E4B extends B2, E3 {
        @Override
        public default int foo(int x) {
            return 5;
        }
    }

    public static interface E4C extends E4B {
    }

    public static interface F1 extends B1 {
    }

    public static interface G1 extends B1 {
        @Override
        public int foo(int x);
    }

    public static interface H1 extends B1 {
        @Override
        public int foo(int x);
    }

    public static interface H2 extends G1, H1 {
    }

    public static interface I1 extends B1, C1 {
    }

    public static interface J1 {
        public default int foo(int x) {
            return 6;
        }
    }

    public static interface J2 extends A1, J1 {
        @Override
        public int foo(int x);
    }

    public static class K1A implements A1 {
    }

    public static class K1B implements J1 {
    }

    public static class K1C implements E4C {
    }

    public static interface L1 {
        public static int foo(int x) {
            throw new AssertionError();
        }
    }

    public static interface L2 {
        private int foo(int x) {
            throw new AssertionError();
        }
    }

    public static interface L3 extends E4C, L1, L2 {
    }

    public static interface M1 extends A1, B2, C2, L2 {
        @Override
        public default int foo(int x) {
            return 6;
        }
    }

    /*@Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testFindDefaultImplementation() throws Exception {
        interface X1 {
            public int foo(int x);
        }
        var m = X1.class.getMethod("foo", int.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(A1.class, Methods.findDefaultImplementation(A1.class, m).get().getDeclaringClass(), "A1"),
                () -> Assertions.assertEquals(B2.class, Methods.findDefaultImplementation(B2.class, m).get().getDeclaringClass(), "B1"),
                () -> Assertions.assertEquals(C3.class, Methods.findDefaultImplementation(C3.class, m).get().getDeclaringClass(), "C3"),
                () -> Assertions.assertEquals(D2.class, Methods.findDefaultImplementation(D3A.class, m).get().getDeclaringClass(), "D3A"),
                () -> Assertions.assertEquals(D2.class, Methods.findDefaultImplementation(D3B.class, m).get().getDeclaringClass(), "D3B"),
                () -> Assertions.assertEquals(B2.class, Methods.findDefaultImplementation(E4A.class, m).get().getDeclaringClass(), "E4A"),
                () -> Assertions.assertEquals(E4B.class, Methods.findDefaultImplementation(E4B.class, m).get().getDeclaringClass(), "E4B"),
                () -> Assertions.assertEquals(E4B.class, Methods.findDefaultImplementation(E4C.class, m).get().getDeclaringClass(), "E4C"),
                () -> Assertions.assertEquals(E4B.class, Methods.findDefaultImplementation(L3.class, m).get().getDeclaringClass(), "L3"),
                () -> Assertions.assertEquals(M1.class, Methods.findDefaultImplementation(M1.class, m).get().getDeclaringClass(), "M1"),
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(B1.class, m).isEmpty(), "B1"), // Only 1, is abstract.
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(F1.class, m).isEmpty(), "F1"), // Only 1, is abstract.
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(G1.class, m).isEmpty(), "G1"), // Abstract overrides abstract.
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(H2.class, m).isEmpty(), "H2"), // Two abstracts overriding same other abstract.
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(I1.class, m).isEmpty(), "I1"), // Two different abstracts.
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(J2.class, m).isEmpty(), "J2"), // Two different defaults - real diamond.
                () -> Assertions.assertTrue(Methods.findDefaultImplementation(X1.class, m).isEmpty(), "X1")
        );
    }*/

    /*@Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testNormalize() throws Exception {
        interface X1 {
            public int foo(int x);
        }
        var m = X1.class.getMethod("foo", int.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(A1.class, Methods.normalize(A1.class, m).getDeclaringClass(), "A1"),
                () -> Assertions.assertEquals(B2.class, Methods.normalize(B2.class, m).getDeclaringClass(), "B1"),
                () -> Assertions.assertEquals(C3.class, Methods.normalize(C3.class, m).getDeclaringClass(), "C3"),
                () -> Assertions.assertEquals(D3A.class, Methods.normalize(D3A.class, m).getDeclaringClass(), "D3A"),
                () -> Assertions.assertEquals(D3A.class, Methods.normalize(D3B.class, m).getDeclaringClass(), "D3B"),
                () -> Assertions.assertEquals(E4A.class, Methods.normalize(E4A.class, m).getDeclaringClass(), "E4A"),
                () -> Assertions.assertEquals(E4B.class, Methods.normalize(E4B.class, m).getDeclaringClass(), "E4B"),
                () -> Assertions.assertEquals(E4B.class, Methods.normalize(E4C.class, m).getDeclaringClass(), "E4C"),
                () -> Assertions.assertEquals(E4B.class, Methods.normalize(L3.class, m).getDeclaringClass(), "L3"),
                () -> Assertions.assertEquals(M1.class, Methods.normalize(M1.class, m).getDeclaringClass(), "M1"),

                () -> Assertions.assertEquals(B1.class, Methods.normalize(B1.class, m).getDeclaringClass(), "B1"), // Only 1, is abstract.
                () -> Assertions.assertEquals(B1.class, Methods.normalize(F1.class, m).getDeclaringClass(), "F1"), // Only 1, is abstract.
                () -> Assertions.assertEquals(G1.class, Methods.normalize(G1.class, m).getDeclaringClass(), "G1"), // Abstract overrides abstract.
                () -> Assertions.assertEquals(G1.class, Methods.normalize(H2.class, m).getDeclaringClass(), "H2"), // Two abstracts overriding same other abstract.
                () -> Assertions.assertEquals(B1.class, Methods.normalize(I1.class, m).getDeclaringClass(), "I1"), // Two different abstracts.
                () -> Assertions.assertEquals(J2.class, Methods.normalize(J2.class, m).getDeclaringClass(), "J2"), // Two different defaults - real diamond.
                () -> Assertions.assertEquals(X1.class, Methods.normalize(X1.class, m).getDeclaringClass(), "X1")
        );
    }*/

    /*@Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testAllInterfaces() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(Set.of(A1.class), Methods.getAllInterfaces(A1.class), "A1"),
                () -> Assertions.assertEquals(Set.of(B1.class), Methods.getAllInterfaces(B1.class), "B1"),
                () -> Assertions.assertEquals(Set.of(B1.class, B2.class), Methods.getAllInterfaces(B2.class), "B2"),
                () -> Assertions.assertEquals(Set.of(C1.class), Methods.getAllInterfaces(C1.class), "C1"),
                () -> Assertions.assertEquals(Set.of(C2.class), Methods.getAllInterfaces(C2.class), "C2"),
                () -> Assertions.assertEquals(Set.of(C1.class, C2.class, C3.class), Methods.getAllInterfaces(C3.class), "C3"),
                () -> Assertions.assertEquals(Set.of(C1.class, D2.class), Methods.getAllInterfaces(D2.class), "D2"),
                () -> Assertions.assertEquals(Set.of(C1.class, D2.class, D3A.class), Methods.getAllInterfaces(D3A.class), "D3A"),
                () -> Assertions.assertEquals(Set.of(C1.class, D2.class, D3A.class, D3B.class), Methods.getAllInterfaces(D3B.class), "D3B"),
                () -> Assertions.assertEquals(Set.of(C1.class, E3.class), Methods.getAllInterfaces(E3.class), "E3"),
                () -> Assertions.assertEquals(Set.of(B1.class, B2.class, C1.class, E3.class, E4A.class), Methods.getAllInterfaces(E4A.class), "E4A"),
                () -> Assertions.assertEquals(Set.of(B1.class, B2.class, C1.class, E3.class, E4B.class), Methods.getAllInterfaces(E4B.class), "E4B"),
                () -> Assertions.assertEquals(Set.of(B1.class, B2.class, C1.class, E3.class, E4B.class, E4C.class), Methods.getAllInterfaces(E4C.class), "E4C"),
                () -> Assertions.assertEquals(Set.of(B1.class, F1.class), Methods.getAllInterfaces(F1.class), "F1"),
                () -> Assertions.assertEquals(Set.of(B1.class, G1.class), Methods.getAllInterfaces(G1.class), "G1"),
                () -> Assertions.assertEquals(Set.of(B1.class, H1.class), Methods.getAllInterfaces(H1.class), "H1"),
                () -> Assertions.assertEquals(Set.of(B1.class, G1.class, H1.class, H2.class), Methods.getAllInterfaces(H2.class), "H2"),
                () -> Assertions.assertEquals(Set.of(B1.class, C1.class, I1.class), Methods.getAllInterfaces(I1.class), "I1"),
                () -> Assertions.assertEquals(Set.of(J1.class), Methods.getAllInterfaces(J1.class), "J1"),
                () -> Assertions.assertEquals(Set.of(A1.class, J1.class, J2.class), Methods.getAllInterfaces(J2.class), "J2"),
                () -> Assertions.assertEquals(Set.of(A1.class), Methods.getAllInterfaces(K1A.class), "K1A"),
                () -> Assertions.assertEquals(Set.of(J1.class), Methods.getAllInterfaces(K1B.class), "K1B"),
                () -> Assertions.assertEquals(Set.of(B1.class, B2.class, C1.class, E3.class, E4B.class, E4C.class), Methods.getAllInterfaces(K1C.class), "K1C"),
                () -> Assertions.assertEquals(Set.of(L1.class), Methods.getAllInterfaces(L1.class), "L1"),
                () -> Assertions.assertEquals(Set.of(L2.class), Methods.getAllInterfaces(L2.class), "L2"),
                () -> Assertions.assertEquals(Set.of(B1.class, B2.class, C1.class, E3.class, E4B.class, E4C.class, L1.class, L2.class, L3.class), Methods.getAllInterfaces(L3.class), "L3"),
                () -> Assertions.assertEquals(Set.of(), Methods.getAllInterfaces(int.class), "int"),
                () -> Assertions.assertEquals(Set.of(Serializable.class, Cloneable.class), Methods.getAllInterfaces(int[].class), "int[]"),
                () -> Assertions.assertEquals(Set.of(Runnable.class), Methods.getAllInterfaces(Thread.class), "Thread")
        );
    }*/

    @SuppressWarnings("ThrowableResultIgnored")
    private static void illegalArg(String msg, Executable runIt) {
        Assertions.assertThrows(IllegalArgumentException.class, runIt, msg);
    }

    /*@Test
    @SuppressWarnings("null")
    public void testNullsBads() throws Exception {
        interface X1 {
            public int foo(int x);
        }
        var m = X1.class.getMethod("foo", int.class);
        var n = String.class.getMethod("length");
        Assertions.assertAll(
                () -> ForTests.testNull("k", () -> Methods.getAllInterfaces(null), "getAllInterfaces"),

                () -> ForTests.testNull("iface", () -> Methods.findDefaultImplementation(null, m), "findDefaultImplementation-iface"),
                () -> ForTests.testNull("m", () -> Methods.findDefaultImplementation(A1.class, null), "findDefaultImplementation-m"),
                () -> illegalArg("Not an interface.", () -> Methods.findDefaultImplementation(String.class, n)),
                () -> illegalArg("Method is not on the interface.", () -> Methods.findDefaultImplementation(A1.class, n)),
                () -> illegalArg("Method is not on the interface.", () -> Methods.findDefaultImplementation(L1.class, m)),
                () -> illegalArg("Method is not on the interface.", () -> Methods.findDefaultImplementation(L2.class, m)),

                () -> ForTests.testNull("iface", () -> Methods.normalize(null, m), "findDefaultImplementation-iface"),
                () -> ForTests.testNull("m", () -> Methods.normalize(A1.class, null), "findDefaultImplementation-m"),
                () -> illegalArg("Not an interface.", () -> Methods.normalize(String.class, n)),
                () -> illegalArg("Method is not on the interface.", () -> Methods.normalize(A1.class, n)),
                () -> illegalArg("Method is not on the interface.", () -> Methods.normalize(L1.class, m)),
                () -> illegalArg("Method is not on the interface.", () -> Methods.normalize(L2.class, m))
        );
    }*/
}
