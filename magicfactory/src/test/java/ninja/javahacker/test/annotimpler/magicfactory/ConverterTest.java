package ninja.javahacker.test.annotimpler.magicfactory;

import module org.junit.jupiter.api;

import ninja.javahacker.annotimpler.magicfactory.Converter;

public class ConverterTest {

    public ConverterTest() {
    }

    @Test
    public void testFromNullDefaultReturnsEmpty() throws Exception {
        Converter<Object> c = new Converter<>() {};
        Assertions.assertAll(
                () -> Assertions.assertTrue(c.from((Object) null).isEmpty()),
                () -> Assertions.assertTrue(c.fromNull().isEmpty())
        );
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testPrimitiveDefaultThrows() {
        Converter<Object> c = new Converter<>() {};
        var ex = Assertions.assertThrows(Converter.ConvertionException.class, () -> c.from(1));
        Assertions.assertAll(
                () -> Assertions.assertEquals(int.class, ex.getRoot()),
                () -> Assertions.assertEquals("Unsupported int", ex.getMessage())
        );
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testConvertionExceptionConstructors() {
        var e = new Converter.ConvertionException("msg", String.class);
        Assertions.assertEquals(String.class, e.getRoot());
        Assertions.assertEquals("msg", e.getMessage());

        var cause = new RuntimeException("boom");
        var e2 = new Converter.ConvertionException("msg2", cause, Integer.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(Integer.class, e2.getRoot()),
                () -> Assertions.assertEquals("msg2", e2.getMessage()),
                () -> Assertions.assertSame(cause, e2.getCause())
        );
    }
}