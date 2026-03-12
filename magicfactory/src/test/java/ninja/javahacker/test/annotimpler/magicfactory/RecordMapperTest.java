package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class RecordMapperTest {

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Fruit(String name, Color c1, int size, Color c2) {
    }

    @Test
    public void testSimpleRecordMap() throws Exception {
        var p = Map.of("name", "peach", "c1", Color.ORANGE.name(), "size", 3, "c2", Color.PINK.ordinal());
        var f = new RecordMapper(ConverterFactory.STD).mapToRecord(p, Fruit.class);
        Assertions.assertEquals(new Fruit("peach", Color.ORANGE, 3, Color.PINK), f);
    }
}
