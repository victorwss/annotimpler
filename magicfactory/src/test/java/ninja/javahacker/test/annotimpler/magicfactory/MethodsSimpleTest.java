package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class MethodsSimpleTest {

    private static Arguments n(String name, Executable ctx) {
        return Arguments.of(name, ctx);
    }

    private static String name(Method m) {
        return m.getDeclaringClass().getSimpleName()
                + "."
                + m.getName()
                + "("
                + Stream.of(m.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "))
                + ") : "
                + m.getReturnType().getSimpleName();
    }

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

        private static void internal5() {
            throw new AssertionError();
        }

        protected static void internal6() {
            throw new AssertionError();
        }

        @PackagePrivate
        static void internal7() {
            throw new AssertionError();
        }

        @Override
        public int compare(Integer a, Integer b) {
            throw new AssertionError();
        }

        public int internal8(String a, int... b) {
            throw new AssertionError();
        }
    }

    public static abstract class SampleAbstract {

        public abstract void foo1();

        public final void foo2() {
            throw new AssertionError();
        }
    }

    public static final class SampleFinal extends SampleAbstract {

        @Override
        public void foo1() {
            throw new AssertionError();
        }

        public final void foo3() {
            throw new AssertionError();
        }

        public void foo4() {
            throw new AssertionError();
        }
    }

    public static interface SampleIface {
        public default void foo1() {
            throw new AssertionError();
        }
    }

    private static List<Method> all() throws NoSuchMethodException {
        var a = Stream.of(Sample.class.getMethods());
        var b = Stream.of(Sample.class.getDeclaredMethods());
        var c = Stream.of(SampleAbstract.class.getMethods());
        var d = Stream.of(SampleFinal.class.getMethods());
        var e = Stream.of(SampleIface.class.getMethods());
        var f = Stream.of(
                String.class.getMethod("equals", Object.class),
                Object.class.getMethod("equals", Object.class),
                Integer.class.getMethod("equals", Object.class),
                String.class.getMethod("hashCode"),
                Object.class.getMethod("hashCode"),
                Integer.class.getMethod("hashCode"),
                String.class.getMethod("toString"),
                Object.class.getMethod("toString"),
                Integer.class.getMethod("toString"),
                Object.class.getDeclaredMethod("clone"),
                CharacterIterator.class.getMethod("clone"),
                StringCharacterIterator.class.getMethod("clone"),
                Object.class.getDeclaredMethod("clone"),
                Object.class.getMethod("notify"),
                Object.class.getMethod("notifyAll"),
                Object.class.getMethod("getClass"),
                Object.class.getMethod("wait"),
                Object.class.getMethod("wait", long.class),
                Object.class.getMethod("wait", long.class, int.class)
        );
        return Stream.of(a, b, c, d, e, f).flatMap(x -> x).distinct().filter(m -> !m.getName().contains("jacoco")).toList();
    }

    private static Stream<Arguments> testEquals() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("equals", Object.class),
            Object.class.getMethod("equals", Object.class),
            Integer.class.getMethod("equals", Object.class),
            Sample.class.getMethod("equals", Object.class)
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isEquals);
    }

    @MethodSource
    @ParameterizedTest(name = "testEquals {0}")
    public void testEquals(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testHashCode() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("hashCode"),
            Object.class.getMethod("hashCode"),
            Integer.class.getMethod("hashCode"),
            Sample.class.getMethod("hashCode")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isHashCode);
    }

    @MethodSource
    @ParameterizedTest(name = "testHashCode {0}")
    public void testHashCode(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testToString() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("toString"),
            Object.class.getMethod("toString"),
            Integer.class.getMethod("toString"),
            Sample.class.getMethod("toString")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isToString);
    }

    @MethodSource
    @ParameterizedTest(name = "testToString {0}")
    public void testToString(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testClone() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Sample.class.getMethod("clone"),
            Stream.of(Sample.class.getMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Object.class).findFirst().get(),
            CharacterIterator.class.getMethod("clone"),
            StringCharacterIterator.class.getMethod("clone")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isClone);
    }

    @MethodSource
    @ParameterizedTest(name = "testClone {0}")
    public void testClone(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testFinalize() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getMethod("finalize")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isFinalize);
    }

    @MethodSource
    @ParameterizedTest(name = "testFinalize {0}")
    public void testFinalize(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testIntrinsic() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Object.class.getMethod("notify"),
            Object.class.getMethod("notifyAll"),
            Object.class.getMethod("getClass"),
            Object.class.getMethod("wait"),
            Object.class.getMethod("wait", long.class),
            Object.class.getMethod("wait", long.class, int.class)
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isObjectIntrinsic);
    }

    @MethodSource
    @ParameterizedTest(name = "testIntrinsic {0}")
    public void testIntrinsic(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testSimple() throws NoSuchMethodException {
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
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7"),
            Sample.class.getMethod("internal4"),
            Sample.class.getMethod("hashCode"),
            Sample.class.getMethod("toString"),
            Sample.class.getMethod("equals", Object.class),
            Stream.of(Sample.class.getMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Object.class).findFirst().get(),
            Stream.of(Sample.class.getMethods()).filter(m -> "compare".equals(m.getName()) && m.getParameterTypes()[0] == Object.class).findFirst().get(),
            Object.class.getMethod("equals", Object.class),
            Integer.class.getMethod("equals", Object.class),
            String.class.getMethod("equals", Object.class),
            Object.class.getMethod("toString"),
            Integer.class.getMethod("toString"),
            String.class.getMethod("toString"),
            Object.class.getMethod("hashCode"),
            Integer.class.getMethod("hashCode"),
            String.class.getMethod("hashCode")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isSimple);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimple {0}")
    public void testSimple(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testSynthetic() throws NoSuchMethodException {
        var isTrue = List.of(
            Stream.of(Sample.class.getMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Object.class).findFirst().get(),
            Stream.of(Sample.class.getMethods()).filter(m -> "compare".equals(m.getName()) && m.getParameterTypes()[0] == Object.class).findFirst().get()
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isSynthetic);
    }

    @MethodSource
    @ParameterizedTest(name = "testSynthetic {0}")
    public void testSynthetic(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testStatic() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isStatic);
    }

    @MethodSource
    @ParameterizedTest(name = "testStatic {0}")
    public void testStatic(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testVirtual() throws NoSuchMethodException {
        var isNot = List.of(
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isVirtual);
    }

    @MethodSource
    @ParameterizedTest(name = "testVirtual {0}")
    public void testVirtual(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testPublic() throws NoSuchMethodException {
        var isNot = List.of(
            Object.class.getDeclaredMethod("clone"),
            Sample.class.getDeclaredMethod("internal1"),
            Sample.class.getDeclaredMethod("internal2"),
            Sample.class.getDeclaredMethod("internal3"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isPublic);
    }

    @MethodSource
    @ParameterizedTest(name = "testPublic {0}")
    public void testPublic(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testProtected() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Sample.class.getDeclaredMethod("internal2"),
            Sample.class.getDeclaredMethod("internal6")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isProtected);
    }

    @MethodSource
    @ParameterizedTest(name = "testProtected {0}")
    public void testProtected(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testPrivate() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal1"),
            Sample.class.getDeclaredMethod("internal5")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isPrivate);
    }

    @MethodSource
    @ParameterizedTest(name = "testPrivate {0}")
    public void testPrivate(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testPackageProtected() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal7"),
            Sample.class.getDeclaredMethod("internal3")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isPackageProtected);
    }

    @MethodSource
    @ParameterizedTest(name = "testProtected {0}")
    public void testPackageProtected(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testConcrete() throws NoSuchMethodException {
        var isNot = List.of(
            SampleAbstract.class.getDeclaredMethod("foo1"),
            CharacterIterator.class.getDeclaredMethod("clone")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isConcrete);
    }

    @MethodSource
    @ParameterizedTest(name = "testConcrete {0}")
    public void testConcrete(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testAbstract() throws NoSuchMethodException {
        var isTrue = List.of(
            SampleAbstract.class.getDeclaredMethod("foo1"),
            CharacterIterator.class.getDeclaredMethod("clone")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isAbstract);
    }

    @MethodSource
    @ParameterizedTest(name = "testAbstract {0}")
    public void testAbstract(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testOverridable() throws NoSuchMethodException {
        var isNot = List.of(
            SampleFinal.class.getDeclaredMethod("foo1"),
            SampleAbstract.class.getDeclaredMethod("foo2"),
            SampleFinal.class.getDeclaredMethod("foo3"),
            SampleFinal.class.getDeclaredMethod("foo4"),
            Sample.class.getDeclaredMethod("internal1"),
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7"),
            Object.class.getMethod("notify"),
            Object.class.getMethod("notifyAll"),
            Object.class.getMethod("getClass"),
            Object.class.getMethod("wait"),
            Object.class.getMethod("wait", long.class),
            Object.class.getMethod("wait", long.class, int.class),
            String.class.getMethod("equals", Object.class),
            Integer.class.getMethod("equals", Object.class),
            String.class.getMethod("toString"),
            Integer.class.getMethod("toString"),
            String.class.getMethod("hashCode"),
            Integer.class.getMethod("hashCode"),
            StringCharacterIterator.class.getDeclaredMethod("clone")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isOverridable);
    }

    @MethodSource
    @ParameterizedTest(name = "testOverridable {0}")
    public void testOverridable(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testFinal() throws NoSuchMethodException {
        var isTrue = List.of(
            SampleFinal.class.getDeclaredMethod("foo1"),
            SampleAbstract.class.getDeclaredMethod("foo2"),
            SampleFinal.class.getDeclaredMethod("foo3"),
            SampleFinal.class.getDeclaredMethod("foo4"),
            Sample.class.getDeclaredMethod("internal1"),
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7"),
            Object.class.getMethod("notify"),
            Object.class.getMethod("notifyAll"),
            Object.class.getMethod("getClass"),
            Object.class.getMethod("wait"),
            Object.class.getMethod("wait", long.class),
            Object.class.getMethod("wait", long.class, int.class),
            String.class.getMethod("equals", Object.class),
            Integer.class.getMethod("equals", Object.class),
            String.class.getMethod("toString"),
            Integer.class.getMethod("toString"),
            String.class.getMethod("hashCode"),
            Integer.class.getMethod("hashCode"),
            StringCharacterIterator.class.getDeclaredMethod("clone")
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isFinal);
    }

    @MethodSource
    @ParameterizedTest(name = "testFinal {0}")
    public void testFinal(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testVarargs() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal8", String.class, int[].class)
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isVarArgs);
    }

    @MethodSource
    @ParameterizedTest(name = "testVarargs {0}")
    public void testVarargs(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testDefault() throws NoSuchMethodException {
        var isTrue = List.of(
            SampleIface.class.getMethod("foo1"),
            Comparator.class.getMethod("reversed"),
            Comparator.class.getMethod("thenComparing", Function.class, Comparator.class),
            Comparator.class.getMethod("thenComparing", Comparator.class),
            Comparator.class.getMethod("thenComparing", Function.class),
            Comparator.class.getMethod("thenComparingInt", ToIntFunction.class),
            Comparator.class.getMethod("thenComparingLong", ToLongFunction.class),
            Comparator.class.getMethod("thenComparingDouble", ToDoubleFunction.class)
        );
        return ForTests.makeTests(isTrue, all(), MethodsSimpleTest::name, Methods::isDefault);
    }

    @MethodSource
    @ParameterizedTest(name = "testDefault {0}")
    public void testDefault(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testNulls() throws Exception {
        var cloneCrazy = Sample.class.getMethod("clone", int.class);
        return Stream.of(
            n("isClone", () -> ForTests.testNull("m", () -> Methods.isClone(null))),
            n("isToString", () -> ForTests.testNull("m", () -> Methods.isToString(null))),
            n("isEquals", () -> ForTests.testNull("m", () -> Methods.isEquals(null))),
            n("isHashCode", () -> ForTests.testNull("m", () -> Methods.isHashCode(null))),
            n("isSimple", () -> ForTests.testNull("m", () -> Methods.isSimple(null))),
            n("isObjectIntrinsic", () -> ForTests.testNull("m", () -> Methods.isObjectIntrinsic(null))),
            n("isFinalize", () -> ForTests.testNull("m", () -> Methods.isFinalize(null))),
            n("isPublic", () -> ForTests.testNull("m", () -> Methods.isPublic(null))),
            n("isPrivate", () -> ForTests.testNull("m", () -> Methods.isPrivate(null))),
            n("isProtected", () -> ForTests.testNull("m", () -> Methods.isProtected(null))),
            n("isPackageProtected", () -> ForTests.testNull("m", () -> Methods.isPackageProtected(null))),
            n("isStatic", () -> ForTests.testNull("m", () -> Methods.isStatic(null))),
            n("isVirtual", () -> ForTests.testNull("m", () -> Methods.isVirtual(null))),
            n("isDefault", () -> ForTests.testNull("m", () -> Methods.isDefault(null))),
            n("isFinal", () -> ForTests.testNull("m", () -> Methods.isFinal(null))),
            n("isOverridable", () -> ForTests.testNull("m", () -> Methods.isOverridable(null))),
            n("isAbstract", () -> ForTests.testNull("m", () -> Methods.isAbstract(null))),
            n("isConcrete", () -> ForTests.testNull("m", () -> Methods.isConcrete(null))),
            n("isSynthetic", () -> ForTests.testNull("m", () -> Methods.isSynthetic(null))),
            n("isVarargs", () -> ForTests.testNull("m", () -> Methods.isVarArgs(null))),
            n("getReturnType-Method", () -> ForTests.testNull("what", () -> Methods.getReturnType((Method) null))),
            n("getReturnType-Constructor", () -> ForTests.testNull("what", () -> Methods.getReturnType((Constructor<?>) null))),
            n("getReturnType-Executable", () -> ForTests.testNull("what", () -> Methods.getReturnType((java.lang.reflect.Executable) null))),
            n("getReturnType-Field", () -> ForTests.testNull("field", () -> Methods.getReturnType((Field) null))),
            n("paramMap-Exectuable", () -> ForTests.testNull("what", () -> Methods.paramMap(null, 5, 12))),
            n("paramMap-NPE", () -> ForTests.testNull("args", () -> Methods.paramMap(cloneCrazy, (Object[]) null)))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testNulls {0}")
    public void testNulls(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    @Test
    public void testNoInstance() throws Exception {
        ForTests.testNonInstantiable(Methods.class);
    }
}
