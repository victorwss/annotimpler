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

    public static record BadAnimal1(String name) {
        @Creator
        public BadAnimal1 foo() {
            throw new AssertionError();
        }

        @Creator
        public BadAnimal1 bar() {
            throw new AssertionError();
        }
    }

    public static record BadAnimal2(String name) {
        public BadAnimal2 {
            throw new RuntimeException("ff");
        }
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

    @Test
    public void testBadRecordMapMissingFields() throws Exception {
        var p = Map.of("name", "apple", "c1", Color.BLUE, "size", "4");
        var ex = Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.STD.mapToRecord(p, Fruit.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals("Map keys mismatch.", ex.getMessage()),
                () -> Assertions.assertEquals(Map.class, ex.getIn()),
                () -> Assertions.assertEquals(Fruit.class, ex.getOut())
        );
    }

    @Test
    public void testBadRecordMapExtraFields() throws Exception {
        var p = Map.of("name", "apple", "c1", Color.BLUE, "size", "4", "c2", Color.PINK.ordinal(), "foo", "bar");
        var ex = Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.STD.mapToRecord(p, Fruit.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals("Map keys mismatch.", ex.getMessage()),
                () -> Assertions.assertEquals(Map.class, ex.getIn()),
                () -> Assertions.assertEquals(Fruit.class, ex.getOut())
        );
    }

    @Test
    public void testBadRecordUnavailableFields() throws Exception {
        class Foo {}
        var p = Map.of("name", "apple", "c1", Color.BLUE, "size", "4", "c2", new Foo());
        var ex = Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.STD.mapToRecord(p, Fruit.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals("Map keys mismatch.", ex.getMessage()),
                () -> Assertions.assertEquals(Map.class, ex.getIn()),
                () -> Assertions.assertEquals(Fruit.class, ex.getOut())
        );
    }

    @Test
    public void testBadRecordUnconvertibleFields() throws Exception {
        var p = Map.of("name", "apple", "c1", Color.BLUE, "size", "aaa", "c2", Color.PINK.ordinal());
        Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.STD.mapToRecord(p, Fruit.class));
    }

    @Test
    public void testBadRecordCreatorSelector() throws Exception {
        var p = Map.of("name", "dog");
        Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> ConverterFactory.STD.mapToRecord(p, BadAnimal1.class));
    }

    @Test
    public void testBadRecordCreation() throws Exception {
        var p = Map.of("name", "dog");
        Assertions.assertThrows(MagicFactory.CreationException.class, () -> ConverterFactory.STD.mapToRecord(p, BadAnimal2.class));
    }

    @Test
    public void testBadRecordClass() throws Exception {
        var p = Map.of("name", "dog");
        var ex = Assertions.assertThrows(IllegalArgumentException.class, () -> ConverterFactory.STD.mapToRecord(p, (Class) String.class));
        Assertions.assertEquals("Not a record class.", ex.getMessage());
    }
}
