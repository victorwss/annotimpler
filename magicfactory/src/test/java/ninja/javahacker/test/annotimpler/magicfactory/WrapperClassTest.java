package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.magicfactory;

@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
public class WrapperClassTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    @TestFactory
    public Stream<DynamicTest> testWrappings() {
        var primitives = List.of(boolean.class, byte.class, char     .class, short.class, int    .class, long.class, float.class, double.class, void.class);
        var wrappers   = List.of(Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class);
        return Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8).flatMap(i -> {
            var p = primitives.get(i);
            var w = wrappers.get(i);
            var n1 = n("[testWrappings] wrap "   + p.getSimpleName(), () -> Assertions.assertEquals(w, WrapperClass.  wrap(p)));
            var n2 = n("[testWrappings] unwrap " + w.getSimpleName(), () -> Assertions.assertEquals(p, WrapperClass.unwrap(w)));
            return Stream.of(n1, n2);
        });
    }

    @TestFactory
    public Stream<DynamicTest> testNotWrappings() {
        return Stream.of(boolean[].class, int[][].class, Thread.class, String.class, String[].class).flatMap(k -> {
            var n1 = n("[testNotWrappings] wrap"   + k.getSimpleName(), () -> Assertions.assertEquals(k, WrapperClass.  wrap(k)));
            var n2 = n("[testNotWrappings] unwrap" + k.getSimpleName(), () -> Assertions.assertEquals(k, WrapperClass.unwrap(k)));
            return Stream.of(n1, n2);
        });
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                DynamicTest.dynamicTest("[testNulls] wrap", () -> ForTests.testNull("in", () -> WrapperClass.wrap(null))),
                DynamicTest.dynamicTest("[testNulls] unwrap", () -> ForTests.testNull("in", () -> WrapperClass.unwrap(null)))
        );
    }

    @Test
    public void testNoInstance() throws Exception {
        ForTests.testNonInstantiable(WrapperClass.class);
    }
}
