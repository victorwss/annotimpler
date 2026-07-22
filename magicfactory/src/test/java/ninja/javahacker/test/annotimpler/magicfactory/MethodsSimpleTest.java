package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class MethodsSimpleTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
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

        public int compare(Integer a) {
            throw new AssertionError();
        }

        @Override
        public int compare(Integer a, Integer b) {
            throw new AssertionError();
        }

        public int compare(Integer a, Integer b, Integer c) {
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
                Object.class.getDeclaredMethod("finalize"),
                Object.class.getMethod("notify"),
                Object.class.getMethod("notifyAll"),
                Object.class.getMethod("getClass"),
                Object.class.getMethod("wait"),
                Object.class.getMethod("wait", long.class),
                Object.class.getMethod("wait", long.class, int.class)
        );
        return Stream.of(a, b, c, d, e, f).flatMap(x -> x).distinct().filter(m -> !m.getName().contains("jacoco")).toList();
    }

    @TestFactory
    public Stream<DynamicTest> testEquals() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("equals", Object.class),
            Object.class.getMethod("equals", Object.class),
            Integer.class.getMethod("equals", Object.class),
            Sample.class.getMethod("equals", Object.class)
        );
        return ForTests.makeTests("[testEquals] ", isTrue, all(), MethodsSimpleTest::name, Methods::isEquals);
    }

    @TestFactory
    public Stream<DynamicTest> testHashCode() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("hashCode"),
            Object.class.getMethod("hashCode"),
            Integer.class.getMethod("hashCode"),
            Sample.class.getMethod("hashCode")
        );
        return ForTests.makeTests("[testHashCode] ", isTrue, all(), MethodsSimpleTest::name, Methods::isHashCode);
    }

    @TestFactory
    public Stream<DynamicTest> testToString() throws NoSuchMethodException {
        var isTrue = List.of(
            String.class.getMethod("toString"),
            Object.class.getMethod("toString"),
            Integer.class.getMethod("toString"),
            Sample.class.getMethod("toString")
        );
        return ForTests.makeTests("[testToString] ", isTrue, all(), MethodsSimpleTest::name, Methods::isToString);
    }

    @TestFactory
    public Stream<DynamicTest> testClone() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Sample.class.getMethod("clone"),
            Stream.of(Sample.class.getMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Object.class).findFirst().get(),
            CharacterIterator.class.getMethod("clone"),
            StringCharacterIterator.class.getMethod("clone")
        );
        return ForTests.makeTests("[testClone] ", isTrue, all(), MethodsSimpleTest::name, Methods::isClone);
    }

    @TestFactory
    public Stream<DynamicTest> testFinalize() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("finalize"),
            Sample.class.getMethod("finalize")
        );
        return ForTests.makeTests("[testFinalize] ", isTrue, all(), MethodsSimpleTest::name, Methods::isFinalize);
    }

    @TestFactory
    public Stream<DynamicTest> testIntrinsic() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Object.class.getDeclaredMethod("finalize"),
            Object.class.getMethod("notify"),
            Object.class.getMethod("notifyAll"),
            Object.class.getMethod("getClass"),
            Object.class.getMethod("wait"),
            Object.class.getMethod("wait", long.class),
            Object.class.getMethod("wait", long.class, int.class)
        );
        return ForTests.makeTests("[testIntrinsic] ", isTrue, all(), MethodsSimpleTest::name, Methods::isObjectIntrinsic);
    }

    @TestFactory
    public Stream<DynamicTest> testSimple() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("clone"),
            Object.class.getDeclaredMethod("finalize"),
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
            Sample.class.getMethod("hashCode"),
            Sample.class.getMethod("toString"),
            Sample.class.getMethod("equals", Object.class),
            Sample.class.getMethod("finalize"),
            Stream.of(Sample.class.getDeclaredMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Object.class && m.getParameterCount() == 0).findFirst().get(),
            Stream.of(Sample.class.getDeclaredMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Sample.class && m.getParameterCount() == 0).findFirst().get(),
            Stream.of(Sample.class.getDeclaredMethods()).filter(m -> "compare".equals(m.getName()) && m.getParameterTypes()[0] == Object.class && m.getParameterCount() == 2).findFirst().get(),
            CharacterIterator.class.getMethod("clone"),
            StringCharacterIterator.class.getMethod("clone"),
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
        return ForTests.makeTests("[testSimple] ", isTrue, all(), MethodsSimpleTest::name, Methods::isSimple);
    }

    @TestFactory
    public Stream<DynamicTest> testSynthetic() throws NoSuchMethodException {
        var isTrue = List.of(
            Stream.of(Sample.class.getMethods()).filter(m -> "clone".equals(m.getName()) && m.getReturnType() == Object.class).findFirst().get(),
            Stream.of(Sample.class.getMethods()).filter(m -> "compare".equals(m.getName()) && m.getParameterTypes()[0] == Object.class).findFirst().get()
        );
        return ForTests.makeTests("[testSynthetic] ", isTrue, all(), MethodsSimpleTest::name, Methods::isSynthetic);
    }

    @TestFactory
    public Stream<DynamicTest> testStatic() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7")
        );
        return ForTests.makeTests("[testStatic] ", isTrue, all(), MethodsSimpleTest::name, Methods::isStatic);
    }

    @TestFactory
    public Stream<DynamicTest> testVirtual() throws NoSuchMethodException {
        var isNot = List.of(
            Sample.class.getDeclaredMethod("internal4"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests("[testVirtual] ", isTrue, all(), MethodsSimpleTest::name, Methods::isVirtual);
    }

    @TestFactory
    public Stream<DynamicTest> testPublic() throws NoSuchMethodException {
        var isNot = List.of(
            Object.class.getDeclaredMethod("clone"),
            Object.class.getDeclaredMethod("finalize"),
            Sample.class.getDeclaredMethod("internal1"),
            Sample.class.getDeclaredMethod("internal2"),
            Sample.class.getDeclaredMethod("internal3"),
            Sample.class.getDeclaredMethod("internal5"),
            Sample.class.getDeclaredMethod("internal6"),
            Sample.class.getDeclaredMethod("internal7")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests("[testPublic] ", isTrue, all(), MethodsSimpleTest::name, Methods::isPublic);
    }

    @TestFactory
    public Stream<DynamicTest> testProtected() throws NoSuchMethodException {
        var isTrue = List.of(
            Object.class.getDeclaredMethod("finalize"),
            Object.class.getDeclaredMethod("clone"),
            Sample.class.getDeclaredMethod("internal2"),
            Sample.class.getDeclaredMethod("internal6")
        );
        return ForTests.makeTests("[testProtected] ", isTrue, all(), MethodsSimpleTest::name, Methods::isProtected);
    }


    @TestFactory
    public Stream<DynamicTest> testPrivate() throws NoSuchMethodException {
        var isTrue = List.of(
                Sample.class.getDeclaredMethod("internal1"),
                Sample.class.getDeclaredMethod("internal5")
        );
        return ForTests.makeTests("[testPrivate] ", isTrue, all(), MethodsSimpleTest::name, Methods::isPrivate);
    }

    @TestFactory
    public Stream<DynamicTest> testPackageProtected() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal7"),
            Sample.class.getDeclaredMethod("internal3")
        );
        return ForTests.makeTests("[testPackageProtected] ", isTrue, all(), MethodsSimpleTest::name, Methods::isPackageProtected);
    }

    @TestFactory
    public Stream<DynamicTest> testConcrete() throws NoSuchMethodException {
        var isNot = List.of(
                SampleAbstract.class.getDeclaredMethod("foo1"),
                CharacterIterator.class.getDeclaredMethod("clone")
        );
        var isTrue = new ArrayList<>(all());
        isTrue.removeAll(isNot);
        return ForTests.makeTests("[testConcrete] ", isTrue, all(), MethodsSimpleTest::name, Methods::isConcrete);
    }

    @TestFactory
    public Stream<DynamicTest> testAbstract() throws NoSuchMethodException {
        var isTrue = List.of(
            SampleAbstract.class.getDeclaredMethod("foo1"),
            CharacterIterator.class.getDeclaredMethod("clone")
        );
        return ForTests.makeTests("[testAbstract] ", isTrue, all(), MethodsSimpleTest::name, Methods::isAbstract);
    }

    @TestFactory
    public Stream<DynamicTest> testOverridable() throws NoSuchMethodException {
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
        return ForTests.makeTests("[testOverridable] ", isTrue, all(), MethodsSimpleTest::name, Methods::isOverridable);
    }

    @TestFactory
    public Stream<DynamicTest> testFinal() throws NoSuchMethodException {
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
        return ForTests.makeTests("[testFinal] ", isTrue, all(), MethodsSimpleTest::name, Methods::isFinal);
    }

    @TestFactory
    public Stream<DynamicTest> testVarargs() throws NoSuchMethodException {
        var isTrue = List.of(
            Sample.class.getDeclaredMethod("internal8", String.class, int[].class)
        );
        return ForTests.makeTests("[testVarargs] ", isTrue, all(), MethodsSimpleTest::name, Methods::isVarArgs);
    }

    @TestFactory
    public Stream<DynamicTest> testDefault() throws NoSuchMethodException {
        var isTrue = new ArrayList<>(List.of(
            SampleIface.class.getMethod("foo1"),
            Comparator.class.getMethod("reversed"),
            Comparator.class.getMethod("thenComparing", Function.class, Comparator.class),
            Comparator.class.getMethod("thenComparing", Comparator.class),
            Comparator.class.getMethod("thenComparing", Function.class),
            Comparator.class.getMethod("thenComparingInt", ToIntFunction.class),
            Comparator.class.getMethod("thenComparingLong", ToLongFunction.class),
            Comparator.class.getMethod("thenComparingDouble", ToDoubleFunction.class)
        ));
        try {
            // Java 26+.
            isTrue.add(Comparator.class.getMethod("max", Object.class, Object.class));
            isTrue.add(Comparator.class.getMethod("min", Object.class, Object.class));
        } catch (NoSuchMethodException e) {
            // Ignore. Maybe Java 25-...
        }
        return ForTests.makeTests("[testDefault] ", isTrue, all(), MethodsSimpleTest::name, Methods::isDefault);
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        var pf = "[testNulls] ";
        var cloneCrazy = Sample.class.getMethod("clone", int.class);
        return Stream.of(
            n(pf + "isClone", () -> ForTests.testNull("m", () -> Methods.isClone(null))),
            n(pf + "isToString", () -> ForTests.testNull("m", () -> Methods.isToString(null))),
            n(pf + "isEquals", () -> ForTests.testNull("m", () -> Methods.isEquals(null))),
            n(pf + "isHashCode", () -> ForTests.testNull("m", () -> Methods.isHashCode(null))),
            n(pf + "isSimple", () -> ForTests.testNull("m", () -> Methods.isSimple(null))),
            n(pf + "isObjectIntrinsic", () -> ForTests.testNull("m", () -> Methods.isObjectIntrinsic(null))),
            n(pf + "isFinalize", () -> ForTests.testNull("m", () -> Methods.isFinalize(null))),
            n(pf + "isPublic", () -> ForTests.testNull("m", () -> Methods.isPublic(null))),
            n(pf + "isPrivate", () -> ForTests.testNull("m", () -> Methods.isPrivate(null))),
            n(pf + "isProtected", () -> ForTests.testNull("m", () -> Methods.isProtected(null))),
            n(pf + "isPackageProtected", () -> ForTests.testNull("m", () -> Methods.isPackageProtected(null))),
            n(pf + "isStatic", () -> ForTests.testNull("m", () -> Methods.isStatic(null))),
            n(pf + "isVirtual", () -> ForTests.testNull("m", () -> Methods.isVirtual(null))),
            n(pf + "isDefault", () -> ForTests.testNull("m", () -> Methods.isDefault(null))),
            n(pf + "isFinal", () -> ForTests.testNull("m", () -> Methods.isFinal(null))),
            n(pf + "isOverridable", () -> ForTests.testNull("m", () -> Methods.isOverridable(null))),
            n(pf + "isAbstract", () -> ForTests.testNull("m", () -> Methods.isAbstract(null))),
            n(pf + "isConcrete", () -> ForTests.testNull("m", () -> Methods.isConcrete(null))),
            n(pf + "isSynthetic", () -> ForTests.testNull("m", () -> Methods.isSynthetic(null))),
            n(pf + "isVarargs", () -> ForTests.testNull("m", () -> Methods.isVarArgs(null))),
            n(pf + "getReturnType-Method", () -> ForTests.testNull("what", () -> Methods.getReturnType((Method) null))),
            n(pf + "getReturnType-Constructor", () -> ForTests.testNull("what", () -> Methods.getReturnType((Constructor<?>) null))),
            n(pf + "getReturnType-Executable", () -> ForTests.testNull("what", () -> Methods.getReturnType((java.lang.reflect.Executable) null))),
            n(pf + "getReturnType-Field", () -> ForTests.testNull("field", () -> Methods.getReturnType((Field) null))),
            n(pf + "paramMap-Exectuable", () -> ForTests.testNull("what", () -> Methods.paramMap(null, 5, 12))),
            n(pf + "paramMap-NPE", () -> ForTests.testNull("args", () -> Methods.paramMap(cloneCrazy, (Object[]) null)))
        );
    }

    @Test
    public void testNoInstance() throws Exception {
        ForTests.testNonInstantiable(Methods.class);
    }
}
