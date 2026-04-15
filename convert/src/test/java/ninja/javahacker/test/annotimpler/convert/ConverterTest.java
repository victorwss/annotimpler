package ninja.javahacker.test.annotimpler.convert;

import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.convert;

public class ConverterTest {

    public ConverterTest() {
    }

    @Test
    public void testFromNullDefaultReturnsEmpty() throws Exception {
        Converter<Object> c = new Converter<>() {
            @Override
            public Class<Object> getType() {
                throw new AssertionError();
            }
        };
        Assertions.assertAll(
                () -> Assertions.assertTrue(c.fromObj(null).isEmpty()),
                () -> Assertions.assertTrue(c.fromNull().isEmpty())
        );
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testPrimitiveDefaultThrows() {
        Converter<Thread> c = new Converter<>() {
            @Override
            public Class<Thread> getType() {
                return Thread.class;
            }
        };
        var ex = Assertions.assertThrows(ConvertionException.class, () -> c.from(1));
        Assertions.assertAll(
                () -> Assertions.assertEquals(int.class, ex.getIn()),
                () -> Assertions.assertEquals(Thread.class, ex.getOut()),
                () -> Assertions.assertEquals("Unsupported int.", ex.getMessage())
        );
    }
}