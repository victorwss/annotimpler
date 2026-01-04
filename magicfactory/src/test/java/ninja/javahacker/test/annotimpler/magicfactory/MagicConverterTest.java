package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

import java.lang.reflect.Array;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

public class MagicConverterTest {

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Wrapper(int value) {}

    public static interface Pointless<X> extends Collection<X> {}

    @Test
    public void testSimpleValueMapping() throws ConstructionException {
        Assertions.assertAll(
                () -> Assertions.assertEquals("foo", MagicConverter.forValue("foo", String.class)),
                () -> Assertions.assertEquals(42, MagicConverter.forValue(42, Integer.class)),
                () -> Assertions.assertEquals(42L, MagicConverter.forValue(42L, Long.class)),
                () -> Assertions.assertEquals(55, MagicConverter.forValue("55", Integer.class))
        );
    }

    @Test
    public void testDateValueMapping() throws ConstructionException {
        Assertions.assertAll(
                () -> Assertions.assertEquals(LocalDate.of(2025, 10, 17), MagicConverter.forValue("2025-10-17", LocalDate.class)),
                () -> Assertions.assertEquals(LocalTime.of(21, 14, 30), MagicConverter.forValue("21:14:30", LocalTime.class)),
                () -> Assertions.assertEquals(
                        LocalDateTime.of(2025, 10, 17, 21, 14, 30),
                        MagicConverter.forValue("2025-10-17 21:14:30", LocalDateTime.class)
                )
        );
    }

    @Test
    public void testEnumValueMapping() throws ConstructionException {
        Assertions.assertAll(
                () -> Assertions.assertEquals(Color.PINK, MagicConverter.forValue(Color.PINK, Color.class)),
                () -> Assertions.assertEquals(Color.YELLOW, MagicConverter.forValue(3, Color.class)),
                () -> Assertions.assertEquals(Color.BLUE, MagicConverter.forValue(5L, Color.class)),
                () -> Assertions.assertEquals(Color.BLACK, MagicConverter.forValue(BigDecimal.valueOf(7), Color.class)),
                () -> Assertions.assertEquals(Color.GREEN, MagicConverter.forValue("GREEN", Color.class))
        );
    }

    @Test
    public void testRecordValueMapping() throws ConstructionException {
        Assertions.assertEquals(new Wrapper(25), MagicConverter.forValue(25, Wrapper.class));
    }

    @Test
    public void testConvertsMapping() throws ConstructionException {
        var ts = List.of(
                byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class,
                BigInteger.class,
                BigDecimal.class
        );
        var wrapper = Map.of(
                byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class,
                char.class, Character.class
        );
        var vals = List.of((byte) 52, (short) 52, 52, 52L, 52.0, 52.0f, BigInteger.valueOf(52L), BigDecimal.valueOf(52L));
        List<Executable> execs = new ArrayList<>(100);
        for (var t : ts) {
            var w = t.isPrimitive() ? wrapper.get(t) : t;
            for (var v : vals) {
                execs.add(() -> {
                    var c = MagicConverter.forValue(v, t);
                    Assertions.assertEquals(w, c.getClass());
                    Assertions.assertEquals(52, c.intValue());
                });
            }
            execs.add(() -> {
                var c = MagicConverter.forValue((char) 52, t);
                Assertions.assertEquals(w, c.getClass());
                Assertions.assertEquals(52, c.intValue());
            });
        }
        Assertions.assertAll(execs);
    }

    public static record Recursive(Recursive r) {}

    @Test
    public void testRecursiveRecord() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicConverter.forValue("x", Recursive.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Recursive.class, ex.getRoot()),
                () -> Assertions.assertEquals("Recursive record class.", ex.getMessage())
        );
    }

    public static record Verbose(String bla, String blu) {}

    @Test
    public void testVerboseRecord() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicConverter.forValue("x", Verbose.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Verbose.class, ex.getRoot()),
                () -> Assertions.assertEquals("Non-single value record class where single-valued was expected.", ex.getMessage())
        );
    }

    @Test
    public void testBadEnumConversion() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicConverter.forValue(new Wrapper(5), Color.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Color.class, ex.getRoot()),
                () -> Assertions.assertEquals("Can't read value as enum class.", ex.getMessage())
        );
    }

    @Test
    public void testSingleton() {
        Verbose[] arr = {
            new Verbose("tchu", "tcha"), new Verbose("tche", "tcho"), new Verbose("abcd", "efgh"), new Verbose("ijkl", "mnop")
        };
        Assertions.assertAll(
                () -> Assertions.assertEquals(List.of(arr[0]), MagicConverter.singleton(arr[0], List.class)),
                () -> Assertions.assertEquals(List.of(arr[1]), MagicConverter.singleton(arr[1], Collection.class)),
                () -> Assertions.assertEquals(List.of(arr[2]), MagicConverter.singleton(arr[2], ArrayList.class)),
                () -> Assertions.assertEquals(List.of(arr[3]), MagicConverter.singleton(arr[3], LinkedList.class)),
                () -> Assertions.assertEquals(Set.of(Color.YELLOW), MagicConverter.singleton(Color.YELLOW, Set.class)),
                () -> Assertions.assertEquals(Set.of(Color.PURPLE), MagicConverter.singleton(Color.PURPLE, HashSet.class)),
                () -> Assertions.assertEquals(Set.of(Color.ORANGE), MagicConverter.singleton(Color.ORANGE, LinkedHashSet.class)),
                () -> Assertions.assertEquals(Set.of(Color.PINK), MagicConverter.singleton(Color.PINK, TreeSet.class)),
                () -> Assertions.assertEquals(Set.of(Color.BLACK), MagicConverter.singleton(Color.BLACK, SortedSet.class)),
                () -> Assertions.assertEquals(Set.of(Color.WHITE), MagicConverter.singleton(Color.WHITE, NavigableSet.class))
        );
    }

    private static void testSingletonBad(Class<?> k) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, () -> MagicConverter.singleton("x", k));
        Assertions.assertEquals("Can't use " + k.getName() + " as a singleton collection.", ex.getMessage());
    }

    @Test
    public void testSingletonBad() {
        Assertions.assertAll(
                () -> testSingletonBad(Pointless.class),
                () -> testSingletonBad(String.class),
                () -> testSingletonBad(int.class),
                () -> testSingletonBad(Object[].class)
        );
    }

    @Test
    public void testNulls() {
        Assertions.assertAll(
                () -> ForTests.testNull("target", () -> MagicConverter.zero(null), "MagicConverter.zero-target"),
                () -> ForTests.testNull("element", () -> MagicConverter.singleton(null, List.class), "MagicConverter.singleton-element"),
                () -> ForTests.testNull("target", () -> MagicConverter.singleton("xxx", null), "MagicConverter.singleton-target")
        );
    }

    @Test
    public void testZero() {
        var ex = Map
                .ofEntries(
                    Map.entry(boolean.class, false),
                    Map.entry(byte.class, (byte) 0),
                    Map.entry(char.class, '\0'),
                    Map.entry(short.class, (short) 0),
                    Map.entry(int.class, 0),
                    Map.entry(long.class, 0L),
                    Map.entry(float.class, 0f),
                    Map.entry(double.class, 0.0),
                    Map.entry(Optional.class, Optional.empty()),
                    Map.entry(OptionalInt.class, OptionalInt.empty()),
                    Map.entry(OptionalLong.class, OptionalLong.empty()),
                    Map.entry(OptionalDouble.class, OptionalDouble.empty()),
                    Map.entry(Collection.class, List.of()),
                    Map.entry(List.class, List.of()),
                    Map.entry(ArrayList.class, List.of()),
                    Map.entry(LinkedList.class, List.of()),
                    Map.entry(Set.class, Set.of()),
                    Map.entry(SortedSet.class, Set.of()),
                    Map.entry(NavigableSet.class, Set.of()),
                    Map.entry(HashSet.class, Set.of()),
                    Map.entry(LinkedHashSet.class, Set.of()),
                    Map.entry(TreeSet.class, Set.of())
                )
                .entrySet()
                .stream()
                .map(e -> {
                    Executable x = () -> Assertions.assertEquals(e.getValue(), MagicConverter.zero(e.getKey()), e.getKey().getName());
                    return x;
                })
                .toList();
        Assertions.assertAll(ex);
    }

    @Test
    public void testZeroNulls() {
        var ex = List
                .of(
                        Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
                        BigInteger.class, BigDecimal.class, Color.class, String.class, Pointless.class, MagicConverter.class, Test.class
                )
                .stream()
                .map(e -> {
                    Executable x = () -> Assertions.assertEquals(null, MagicConverter.zero(e.getClass()), e.getClass().getName());
                    return x;
                })
                .toList();
        Assertions.assertAll(ex);
    }

    @Test
    public void testZeroArrays() {
        var ex = List
                .of(
                        boolean[].class, char[].class, short[].class, int[].class, long[].class, float[].class, double[].class,
                        Boolean[].class, Byte[].class, Character[].class, Short[].class,
                        Integer[].class, Long[].class, Float[].class, Double[].class,
                        BigInteger[].class, BigDecimal[].class,
                        Color[].class, String[].class, Pointless[].class, MagicConverter[].class, Test[].class
                )
                .stream()
                .map(e -> {
                    Executable x = () -> {
                        Assertions.assertTrue(e.isArray());
                        var s = MagicConverter.zero(e);
                        Assertions.assertEquals(e, s.getClass());
                        Assertions.assertEquals(0, Array.getLength(s));
                    };
                    return x;
                })
                .toList();
        Assertions.assertAll(ex);
    }

    @Test
    public void testUnstringify() {
        record Parts<E>(String input, Class<E> type, E output) {
        }

        var bigi = "123456789101112131415161718192021222324252627282930";
        var bigd = bigi + ".313233";
        var ex = List
                .of(
                    new Parts<>("false", boolean.class, false),
                    new Parts<>("false", boolean.class, false),
                    new Parts<>("true", Boolean.class, true),
                    new Parts<>("true", Boolean.class, true),
                    new Parts<>("0", byte.class, (byte) 0),
                    new Parts<>("0", Byte.class, (byte) 0),
                    new Parts<>("55", byte.class, (byte) 55),
                    new Parts<>("55", Byte.class, (byte) 55),
                    new Parts<>("-120", byte.class, (byte) -120),
                    new Parts<>("-120", Byte.class, (byte) -120),
                    new Parts<>("0", char.class, '0'),
                    new Parts<>("a", char.class, 'a'),
                    new Parts<>("\n", char.class, '\n'),
                    new Parts<>("123", int.class, 123),
                    new Parts<>("-123", int.class, -123),
                    new Parts<>("123", Integer.class, 123),
                    new Parts<>("-123", Integer.class, -123),
                    new Parts<>("123", long.class, 123L),
                    new Parts<>("-123", long.class, -123L),
                    new Parts<>("123", Long.class, 123L),
                    new Parts<>("-123", Long.class, -123L),
                    new Parts<>("123", float.class, 123.0F),
                    new Parts<>("-123", float.class, -123.0F),
                    new Parts<>("123", Float.class, 123.0F),
                    new Parts<>("-123", Float.class, -123.0F),
                    new Parts<>("123", double.class, 123.0),
                    new Parts<>("-123", double.class, -123.0),
                    new Parts<>("123", Double.class, 123.0),
                    new Parts<>("-123", Double.class, -123.0),
                    new Parts<>(bigi, BigInteger.class, new BigInteger(bigi)),
                    new Parts<>(bigi, BigDecimal.class, new BigDecimal(bigi)),
                    new Parts<>(bigd, BigDecimal.class, new BigDecimal(bigd))
                )
                .stream()
                .map(e -> {
                    Executable x = () -> {
                        Assertions.assertEquals(
                                e.output(),
                                MagicConverter.unstringify(e.input(), e.type()),
                                e.input() + "-" + e.type().getName()
                        );
                    };
                    return x;
                })
                .toList();
        Assertions.assertAll(ex);
    }
}
