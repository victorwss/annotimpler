package ninja.javahacker.test.annotimpler.convert;

import module ninja.javahacker.annotimpler.convert;
import module java.base;
import module org.junit.jupiter.api;

import org.junit.jupiter.api.function.Executable;

public class SimpleValueMappingTest {

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Wrapper(int value) {}

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testSimpleValueMapping() {
        return Stream.of(
                n("basic int", () -> Assertions.assertEquals(42, ConverterFactory.STD.get(Integer.class).from(42).get())),
                n("basic long", () -> Assertions.assertEquals(42L, ConverterFactory.STD.get(Long.class).from(42L).get())),
                n("enum from int", () -> Assertions.assertEquals(Color.YELLOW, ConverterFactory.STD.get(Color.class).from(3).get())),
                n("enum from long", () -> Assertions.assertEquals(Color.BLUE, ConverterFactory.STD.get(Color.class).from(5L).get())),
                n("enum from BigDecimal", () -> Assertions.assertEquals(Color.BLACK, ConverterFactory.STD.get(Color.class).from(BigDecimal.valueOf(7)).get())),
                n("simple record", () -> Assertions.assertEquals(new Wrapper(25), ConverterFactory.STD.get(Wrapper.class).from(25).get())),
                n("simple record converts", () -> Assertions.assertEquals(new Wrapper(25), ConverterFactory.STD.get(Wrapper.class).from(25.0).get()))
        );
    }
}
