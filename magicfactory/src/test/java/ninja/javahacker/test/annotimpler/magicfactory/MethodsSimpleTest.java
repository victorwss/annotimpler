package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import java.lang.reflect.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class MethodsSimpleTest {

    public static class Sample implements Cloneable, Comparator<Integer> {
        @Override
        public int hashCode() {
            throw new AssertionError();
        }

        public int hashCode(int hash) {
            throw new AssertionError();
        }

        @Override
        public boolean equals(Object other) {
            throw new AssertionError();
        }

        public boolean equals(Sample other) {
            throw new AssertionError();
        }

        public boolean equals() {
            throw new AssertionError();
        }

        public boolean equals(Object other, Object another) {
            throw new AssertionError();
        }

        @Override
        public String toString() {
            throw new AssertionError();
        }

        public String toString(int x) {
            throw new AssertionError();
        }

        @Override
        public Sample clone() {
            throw new AssertionError();
        }

        public Sample clone(int x) {
            throw new AssertionError();
        }

        @Deprecated
        @SuppressWarnings("removal")
        public void finalize() {
            throw new AssertionError();
        }

        public void finalize(int a) {
            throw new AssertionError();
        }

        public void wait(String x) {
            throw new AssertionError();
        }

        public void notify(String x) {
            throw new AssertionError();
        }

        public void notifyAll(String x) {
            throw new AssertionError();
        }

        public void getClass(String x) {
            throw new AssertionError();
        }

        private void internal1() {
            throw new AssertionError();
        }

        protected void internal2() {
            throw new AssertionError();
        }

        @PackagePrivate
        void internal3() {
            throw new AssertionError();
        }

        public static void internal4() {
            throw new AssertionError();
        }

        @Override
        public int compare(Integer a, Integer b) {
            throw new AssertionError();
        }
    }

    @Test
    public void testEquals() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("equals", Object.class),
            Object.class.getMethod("equals", Object.class),
            Sample.class.getMethod("equals", Object.class)
        );
        var isFalse = List.of(
            Sample.class.getMethod("equals", Sample.class),
            Sample.class.getMethod("equals"),
            Sample.class.getMethod("equals", Object.class, Object.class),
            String.class.getMethod("hashCode"),
            Integer.class.getMethod("toString")
        );
        ForTests.testAll(isTrue, isFalse, Methods::isEquals);
    }

    @Test
    public void testHashCode() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("hashCode"),
            Object.class.getMethod("hashCode"),
            Sample.class.getMethod("hashCode")
        );
        var isFalse = List.of(
            Sample.class.getMethod("hashCode", int.class),
            String.class.getMethod("equals", Object.class),
            Integer.class.getMethod("toString")
        );
        ForTests.testAll(isTrue, isFalse, Methods::isHashCode);
    }

    @Test
    public void testToString() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("toString"),
            Object.class.getMethod("toString"),
            Sample.class.getMethod("toString")
        );
        var isFalse = List.of(
            Sample.class.getMethod("toString", int.class),
            String.class.getMethod("equals", Object.class),
            Integer.class.getMethod("hashCode")
        );
        ForTests.testAll(isTrue, isFalse, Methods::isToString);
    }

    @Test
    public void testClone() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Sample.class.getMethod("clone"),
            CharacterIterator.class.getMethod("clone"),
            StringCharacterIterator.class.getMethod("clone")
        );
        var isFalse = List.of(
            Sample.class.getMethod("clone", int.class),
            String.class.getMethod("equals", Object.class),
            Integer.class.getMethod("hashCode")
        );
        ForTests.testAll(isTrue, isFalse, Methods::isClone);
    }

    @Test
    public void testFinalize() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getMethod("finalize")
        );
        var isFalse = List.of(
            Sample.class.getMethod("finalize", int.class),
            String.class.getMethod("equals", Object.class),
            Integer.class.getMethod("hashCode")
        );
        ForTests.testAll(isTrue, isFalse, Methods::isFinalize);
    }

    @Test
    public void testIntrinsic() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Object.class.getMethod("notify"),
            Object.class.getMethod("notifyAll"),
            Object.class.getMethod("getClass"),
            Object.class.getMethod("wait"),
            Object.class.getMethod("wait", long.class),
            Object.class.getMethod("wait", long.class, int.class)
        );
        var isFalse = List.of(
            Sample.class.getMethod("clone"),
            Object.class.getMethod("hashCode"),
            Sample.class.getMethod("wait", String.class),
            Sample.class.getMethod("notify", String.class),
            Sample.class.getMethod("notifyAll", String.class),
            Sample.class.getMethod("getClass", String.class),
            Sample.class.getDeclaredMethod("internal1")
        );
        ForTests.testAll(isTrue, isFalse, Methods::isObjectIntrinsic);
    }

    @Test
    public void testSimple() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Object.class.getMethod("notify"),
            Object.class.getMethod("notifyAll"),
            Object.class.getMethod("getClass"),
            Object.class.getMethod("wait"),
            Object.class.getMethod("wait", long.class),
            Object.class.getMethod("wait", long.class, int.class),
            Sample.class.getMethod("compare", Object.class, Object.class),
            Sample.class.getDeclaredMethod("internal1"),
            Sample.class.getDeclaredMethod("internal2"),
            Sample.class.getDeclaredMethod("internal3"),
            Sample.class.getMethod("internal4"),
            Sample.class.getMethod("hashCode"),
            Sample.class.getMethod("toString"),
            Sample.class.getMethod("equals", Object.class)
        );
        var isFalse = List.of(
            Sample.class.getMethod("clone"),
            String.class.getMethod("length"),
            Sample.class.getMethod("wait", String.class),
            Sample.class.getMethod("compare", Integer.class, Integer.class),
            Sample.class.getMethod("notifyAll", String.class),
            Sample.class.getMethod("getClass", String.class)
        );
        ForTests.testAll(isTrue, isFalse, Methods::isSimple);
    }

    @Test
    @SuppressWarnings("null")
    public void testNulls() throws Exception {
        var cloneCrazy = Sample.class.getMethod("clone", int.class);
        Assertions.assertAll(
            () -> ForTests.testNull("m", () -> Methods.isClone(null), "isClone"),
            () -> ForTests.testNull("m", () -> Methods.isToString(null), "isToString"),
            () -> ForTests.testNull("m", () -> Methods.isEquals(null), "isEquals"),
            () -> ForTests.testNull("m", () -> Methods.isHashCode(null), "isHashCode"),
            () -> ForTests.testNull("m", () -> Methods.isSimple(null), "isSimple"),
            () -> ForTests.testNull("m", () -> Methods.isObjectIntrinsic(null), "isObjectIntrinsic"),
            () -> ForTests.testNull("m", () -> Methods.isFinalize(null), "isFinalize"),
            () -> ForTests.testNull("what", () -> Methods.getReturnType((Method) null), "getReturnType-Method"),
            () -> ForTests.testNull("what", () -> Methods.getReturnType((Constructor<?>) null), "getReturnType-Constructor"),
            () -> ForTests.testNull("what", () -> Methods.getReturnType((Executable) null), "getReturnType-Executable"),
            () -> ForTests.testNull("field", () -> Methods.getReturnType((Field) null), "getReturnType-Field"),
            () -> ForTests.testNull("what", () -> Methods.paramMap(null, 5, 12), "paramMap-Exectuable"),
            () -> ForTests.testNull("args", () -> Methods.paramMap(cloneCrazy, (Object[]) null), "paramMap-NPE")
        );
    }

    @Test
    public void testNoInstance() throws Exception {
        ForTests.testNonInstantiable(Methods.class);
    }
}
