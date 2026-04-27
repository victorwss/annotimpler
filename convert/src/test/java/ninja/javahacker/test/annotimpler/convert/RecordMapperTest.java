package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class RecordMapperTest {

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Fruit(String name, Color c1, int size, Color c2) {
    }

    @Test
    public void testSimpleRecordMap1() throws Exception {
        var p = Map.of("name", "peach", "c1", Color.ORANGE.name(), "size", 3, "c2", Color.PINK.ordinal());
        var f = ConverterFactory.STD.mapToRecord(p, Fruit.class);
        Assertions.assertEquals(new Fruit("peach", Color.ORANGE, 3, Color.PINK), f);
    }

    private Clob clob(String in) {
        return (Clob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Clob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return new StringReader(in);
            throw new AssertionError(m.getName());
        });
    }

    @Test
    public void testSimpleRecordMap2() throws Exception {
        var p = Map.of("name", "apple".getBytes(), "c1", clob(Color.BLUE.name()), "size", "4", "c2", (double) Color.GREEN.ordinal());
        var f = ConverterFactory.STD.mapToRecord(p, Fruit.class);
        Assertions.assertEquals(new Fruit("apple", Color.BLUE, 4, Color.GREEN), f);
    }
}
