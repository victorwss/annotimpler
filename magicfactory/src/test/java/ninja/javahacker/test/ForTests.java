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

    public static <E> Stream<DynamicTest> makeTests(String prefix, List<E> isTrue, List<E> all, Function<E, String> strz, Predicate<? super E> p) {
        var isFalse = new ArrayList<>(all);
        isFalse.removeAll(isTrue);
        return Stream.concat(
                isTrue.stream().map(m -> DynamicTest.dynamicTest(prefix + strz.apply(m), testTrue(m, p))),
                isFalse.stream().map(m -> DynamicTest.dynamicTest(prefix + strz.apply(m), testFalse(m, p)))
        );
    }

    public static <A, B> Executable checkEquals(A a, B b, Function<A, B> func) {
        return () -> Assertions.assertEquals(b, func.apply(a));
    }

    public static void testNull(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt);
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getMessage());
    }

    public static void testNullReflective(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(InvocationTargetException.class, runIt);
        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getCause().getMessage());
    }

    public static void testNonInstantiable(Class<?> klass) throws Exception {
        var ctor = klass.getDeclaredConstructor();
        Assertions.assertAll(
                () -> {
                    var ite = Assertions.assertThrows(InvocationTargetException.class, () -> {
                        ctor.setAccessible(true);
                        ctor.newInstance();
                    });
                    Assertions.assertEquals(UnsupportedOperationException.class, ite.getCause().getClass());
                },
                () -> Assertions.assertEquals(List.of(ctor), List.of(klass.getDeclaredConstructors())),
                () -> Assertions.assertTrue(Modifier.isPrivate(ctor.getModifiers())),
                () -> Assertions.assertTrue(Modifier.isFinal(klass.getModifiers()))
        );
    }
}
