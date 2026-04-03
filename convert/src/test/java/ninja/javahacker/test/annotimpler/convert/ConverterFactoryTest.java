package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

import java.lang.reflect.Type;

public class ConverterFactoryTest {

    @Test
    public void basicStdMappings() throws Exception {
        var ci = ConverterFactory.STD.get(Integer.class).from(42).get();
        Assertions.assertEquals(42, ci);

        var cs = ConverterFactory.stdGet(String.class).from("x").get();
        Assertions.assertEquals("x", cs);
    }

    @Test
    public void parameterizedListString() throws Exception {
        Type t = ConverterFactoryTest.class
                .getDeclaredMethod("noopList", List.class)
                .getParameters()[0].getParameterizedType();
        var r = ConverterFactory.STD.get(t).from("a").get();
        Assertions.assertEquals(List.of("a"), r);
    }

    private static void noopList(List<String> x) {
        throw new AssertionError();
    }

    @Test
    public void testEnum() throws Exception {
        enum E { A, B, C }
        var ev = ConverterFactory.STD.get(E.class).from(2).get();
        Assertions.assertEquals(E.C, ev);
    }

    @Test
    public void testArray() throws Exception {
        var arr = ConverterFactory.STD.get(String[].class).from("z").get();
        Assertions.assertEquals(1, java.lang.reflect.Array.getLength(arr));
        Assertions.assertEquals("z", java.lang.reflect.Array.get(arr, 0));
    }

    @Test
    public void testMultidimensionalArrayUnavailable() throws Exception {
        var ex1 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(int[][].class));
        Assertions.assertEquals(int[][].class, ex1.getRoot());
    }

    @Test
    public void testBadCollectionUnavailable() throws Exception {
        var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(java.util.Map.class));
        Assertions.assertEquals(Map.class, ex2.getRoot());
    }
}