package ninja.javahacker.test.annotimpler.magicfactory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Type;
import ninja.javahacker.annotimpler.magicfactory.cvt.ConverterFactory;

public class ConverterFactoryTest {

    @Test
    public void basicStdMappings() throws Exception {
        var ci = ConverterFactory.STD.get(Integer.class).from(42).get();
        assertEquals(42, ci);

        var cs = ConverterFactory.stdGet(String.class).from("x").get();
        assertEquals("x", cs);
    }

    @Test
    public void parameterizedListString() throws Exception {
        Type t = ConverterFactoryTest.class
                .getDeclaredMethod("noopList", java.util.List.class)
                .getParameters()[0].getParameterizedType();
        var r = ConverterFactory.STD.get(t).from("a").get();
        assertEquals(java.util.List.of("a"), r);
    }

    private static void noopList(java.util.List<String> x) {
        throw new AssertionError();
    }

    @Test
    public void testEnum() throws Exception {
        enum E { A, B, C }
        var ev = ConverterFactory.STD.get(E.class).from(2).get();
        assertEquals(E.C, ev);
    }

    @Test
    public void testArray() throws Exception {
        var arr = ConverterFactory.STD.get(String[].class).from("z").get();
        assertEquals(1, java.lang.reflect.Array.getLength(arr));
        assertEquals("z", java.lang.reflect.Array.get(arr, 0));
    }

    @Test
    public void testMultidimensionalArrayUnavailable() throws Exception {
        var ex1 = assertThrows(ConverterFactory.UnavailableConverterException.class,
                () -> ConverterFactory.STD.get(int[][].class));
        assertEquals(int[][].class, ex1.getRoot());
    }

    @Test
    public void testBadCollectionUnavailable() throws Exception {
        var ex2 = assertThrows(ConverterFactory.UnavailableConverterException.class,
                () -> ConverterFactory.STD.get(java.util.Map.class));
        assertEquals(java.util.Map.class, ex2.getRoot());
    }
}