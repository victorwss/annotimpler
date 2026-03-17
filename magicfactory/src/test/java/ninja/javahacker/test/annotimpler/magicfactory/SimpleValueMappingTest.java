package ninja.javahacker.test.annotimpler.magicfactory;

import module org.junit.jupiter.api;
import module org.junit.jupiter.params;
import module ninja.javahacker.annotimpler.magicfactory;
import module java.base;

import org.junit.jupiter.api.function.Executable;

public class SimpleValueMappingTest {

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Wrapper(int value) {}

    private static Stream<Arguments> testSimpleValueMapping() {
        return Stream.of(
                ConverterTestUtils.args("basic int", (Executable) () -> Assertions.assertEquals(42, ConverterFactory.STD.get(Integer.class).from(42).get())),
                ConverterTestUtils.args("basic long", (Executable) () -> Assertions.assertEquals(42L, ConverterFactory.STD.get(Long.class).from(42L).get())),
                ConverterTestUtils.args("enum from int", (Executable) () -> Assertions.assertEquals(Color.YELLOW, ConverterFactory.STD.get(Color.class).from(3).get())),
                ConverterTestUtils.args("enum from long", (Executable) () -> Assertions.assertEquals(Color.BLUE, ConverterFactory.STD.get(Color.class).from(5L).get())),
                ConverterTestUtils.args("enum from BigDecimal", (Executable) () -> Assertions.assertEquals(Color.BLACK, ConverterFactory.STD.get(Color.class).from(BigDecimal.valueOf(7)).get())),
                ConverterTestUtils.args("simple record", (Executable) () -> Assertions.assertEquals(new Wrapper(25), ConverterFactory.STD.get(Wrapper.class).from(25).get())),
                ConverterTestUtils.args("simple record converts", (Executable) () -> Assertions.assertEquals(new Wrapper(25), ConverterFactory.STD.get(Wrapper.class).from(25.0).get()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "testSimpleValueMapping {0}")
    public void testSimpleValueMapping(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
