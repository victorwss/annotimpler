package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class RecordMapperTest {

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Fruit(String name, Color c, int size) {
    }

    @Test
    public void testSimpleRecordMap() throws ConstructionException {
        var p = Map.of("name", "peach", "c", Color.ORANGE, "size", 3);
        var f = RecordMapper.forMap(p, Fruit.class);
        Assertions.assertEquals(new Fruit("peach", Color.ORANGE, 3), f);
    }
}
