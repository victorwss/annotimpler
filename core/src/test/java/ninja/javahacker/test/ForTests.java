package ninja.javahacker.test;

import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.api;

public final class ForTests {

    private ForTests() {
        throw new UnsupportedOperationException();
    }

    private static <E> Executable testTrue(E m, Predicate<? super E> p) {
        return () -> Assertions.assertTrue(p.test(m), "For " + m);
    }

    private static <E> Executable testFalse(E m, Predicate<? super E> p) {
        return () -> Assertions.assertFalse(p.test(m), "For " + m);
    }

    public static <E> void testAll(List<E> isTrue, List<E> isFalse, Predicate<? super E> p) {
        var s = Stream.concat(
                isTrue.stream().map(m -> testTrue(m, p)),
                isFalse.stream().map(m -> testFalse(m, p))
        );
        Assertions.assertAll(s.toList());
    }

    public static <A, B> Executable checkEquals(A a, B b, Function<A, B> func) {
        return () -> Assertions.assertEquals(b, func.apply(a));
    }

    public static void testNull(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt);
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getMessage());
    }

    public static void testNull(String paramName, Executable runIt, String testName) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt, testName);
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getMessage(), testName);
    }

    public static void testNullReflective(String paramName, Executable runIt, String testName) {
        var ex = Assertions.assertThrows(InvocationTargetException.class, runIt, testName);
        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass(), testName);
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getCause().getMessage(), testName);
    }

    public static void testNonInstantiable(Class<?> klass) {
        var ite = Assertions.assertThrows(InvocationTargetException.class, () -> {
            var ctor = klass.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        });
        Assertions.assertEquals(UnsupportedOperationException.class, ite.getCause().getClass());
    }
}
