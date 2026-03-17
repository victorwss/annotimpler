package ninja.javahacker.test.annotimpler.magicfactory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import ninja.javahacker.annotimpler.magicfactory.Converter;

public class ConverterTest {

    @Test
    public void testFromNullDefaultReturnsEmpty() throws Exception {
        Converter<Object> c = new Converter<>() {};
        assertTrue(c.from((Object) null).isEmpty());
        assertTrue(c.fromNull().isEmpty());
    }

    @Test
    public void testPrimitiveDefaultThrows() {
        Converter<Object> c = new Converter<>() {};
        var ex = assertThrows(Converter.ConvertionException.class, () -> c.from(1));
        assertEquals(int.class, ex.getRoot());
        assertEquals("Unsupported int", ex.getMessage());
    }

    @Test
    public void testConvertionExceptionConstructors() {
        var e = new Converter.ConvertionException("msg", String.class);
        assertEquals(String.class, e.getRoot());
        assertEquals("msg", e.getMessage());

        var cause = new RuntimeException("boom");
        var e2 = new Converter.ConvertionException("msg2", cause, Integer.class);
        assertEquals(Integer.class, e2.getRoot());
        assertEquals("msg2", e2.getMessage());
        assertSame(cause, e2.getCause());
    }
}