package ninja.javahacker.test.annotimpler.magicfactory;

import java.lang.reflect.Array;
import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class MagicConverterTest {

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Wrapper(int value) {}

    public static interface Pointless<X> extends Collection<X> {}

    private static Stream<Arguments> testSimpleValueMapping() throws ConstructionException {
        return Stream.of(
                n("basic int", () -> Assertions.assertEquals(42, MagicConverter.forValue(42, Integer.class))),
                n("basic long", () -> Assertions.assertEquals(42L, MagicConverter.forValue(42L, Long.class))),
                n("enum from itself", () -> Assertions.assertEquals(Color.PINK, MagicConverter.forValue(Color.PINK, Color.class))),
                n("enum from int", () -> Assertions.assertEquals(Color.YELLOW, MagicConverter.forValue(3, Color.class))),
                n("enum from long", () -> Assertions.assertEquals(Color.BLUE, MagicConverter.forValue(5L, Color.class))),
                n("enum from BigDecimal", () -> Assertions.assertEquals(
                        Color.BLACK,
                        MagicConverter.forValue(BigDecimal.valueOf(7), Color.class))
                ),
                n("simple record", () -> Assertions.assertEquals(new Wrapper(25), MagicConverter.forValue(25, Wrapper.class)))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimpleValueMapping {0}")
    public void testSimpleValueMapping(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testConvertsMapping() throws ConstructionException {
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
        var vals = List.of((byte) 52, (short) 52, 52, 52L, 52.0, 52.0f, BigInteger.valueOf(52L), BigDecimal.valueOf(52L), (char) 52);
        List<NamedTest> execs = new ArrayList<>(100);
        for (var t : ts) {
            var w = t.isPrimitive() ? wrapper.get(t) : t;
            for (var v : vals) {
                Executable x = () -> {
                    var c = MagicConverter.forValue(v, t);
                    Assertions.assertEquals(w, c.getClass());
                    Assertions.assertEquals(52, c.intValue());
                };
                execs.add(n("forValue((" + v.getClass().getSimpleName() + ") " + v + ", " + t.getSimpleName() + " )", x));
            }
        }
        return execs.stream().map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testConvertsMapping {0}")
    public void testConvertsMapping(String name, Executable exec) throws Throwable {
        exec.execute();
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

    private static void testBadEnum(Object in, Class<? extends Enum<?>> out) {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicConverter.forValue(in, out));
        Assertions.assertAll(
                () -> Assertions.assertEquals(out, ex.getRoot()),
                () -> Assertions.assertEquals("Can't read value as enum class.", ex.getMessage())
        );
    }

    private static Stream<Arguments> testBadEnumConversion() throws ConstructionException {
        return Stream.of(
                n("record to enum", () -> testBadEnum(new Wrapper(5), Color.class)),
                n("bad name to enum", () -> testBadEnum("bla", Color.class))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testBadEnumConversion {0}")
    public void testBadEnumConversion(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testSingleton() {
        Verbose[] arr = {
            new Verbose("tchu", "tcha"), new Verbose("tche", "tcho"), new Verbose("abcd", "efgh"), new Verbose("ijkl", "mnop")
        };
        return Stream.of(
                n("List", () -> Assertions.assertEquals(List.of(arr[0]), MagicConverter.singleton(arr[0], List.class))),
                n("Collection", () -> Assertions.assertEquals(List.of(arr[1]), MagicConverter.singleton(arr[1], Collection.class))),
                n("ArrayList", () -> Assertions.assertEquals(List.of(arr[2]), MagicConverter.singleton(arr[2], ArrayList.class))),
                n("LinkedList", () -> Assertions.assertEquals(List.of(arr[3]), MagicConverter.singleton(arr[3], LinkedList.class))),
                n("Set", () -> Assertions.assertEquals(Set.of(Color.YELLOW), MagicConverter.singleton(Color.YELLOW, Set.class))),
                n("HashSet", () -> Assertions.assertEquals(Set.of(Color.PURPLE), MagicConverter.singleton(Color.PURPLE, HashSet.class))),
                n("LinkedHashSet", () -> Assertions.assertEquals(Set.of(Color.ORANGE), MagicConverter.singleton(Color.ORANGE, LinkedHashSet.class))),
                n("TreeSet", () -> Assertions.assertEquals(Set.of(Color.PINK), MagicConverter.singleton(Color.PINK, TreeSet.class))),
                n("SortedSet", () -> Assertions.assertEquals(Set.of(Color.BLACK), MagicConverter.singleton(Color.BLACK, SortedSet.class))),
                n("NavigableSet", () -> Assertions.assertEquals(Set.of(Color.WHITE), MagicConverter.singleton(Color.WHITE, NavigableSet.class)))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSingleton {0}")
    public void testSingleton(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static void testSingletonBad(Class<?> k) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, () -> MagicConverter.singleton("x", k));
        Assertions.assertEquals("Can't use " + k.getName() + " as a singleton collection.", ex.getMessage());
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testBads() {
        return Stream.of(
                n("unknown collectiom singleton", () -> testSingletonBad(Pointless.class)),
                n("not collection singleton", () -> testSingletonBad(String.class)),
                n("primitive singleton", () -> testSingletonBad(int.class)),
                n("array singleton", () -> testSingletonBad(Object[].class)),
                n("MagicConverter.zero-target", () -> ForTests.testNull("target", () -> MagicConverter.zero(null))),
                n("MagicConverter.singleton-element", () -> ForTests.testNull("element", () -> MagicConverter.singleton(null, List.class))),
                n("MagicConverter.singleton-target", () -> ForTests.testNull("target", () -> MagicConverter.singleton("xxx", null)))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testBads {0}")
    public void testBads(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testZero() {
        return Map.ofEntries(
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
        .map(e -> n(
                "zero(" + e.getKey().getSimpleName() + ")",
                () -> Assertions.assertEquals(e.getValue(), MagicConverter.zero(e.getKey()), e.getKey().getName())
        ))
        .map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testZero {0}")
    public void testZero(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testZeroNulls() {
        return Stream.of(
                Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Class.class,
                BigInteger.class, BigDecimal.class, Color.class, String.class, Pointless.class, MagicConverter.class, Test.class
        ).map(e -> n(
                e.getSimpleName(),
                () -> Assertions.assertEquals(null, MagicConverter.zero(e))
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testZeroNulls {0}")
    public void testZeroNulls(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testZeroArrays() {
        return Stream.of(
                boolean[].class, byte[].class, char[].class, short[].class, int[].class, long[].class, float[].class, double[].class,
                Boolean[].class, Byte[].class, Character[].class, Short[].class,
                Integer[].class, Long[].class, Float[].class, Double[].class,
                BigInteger[].class, BigDecimal[].class,
                Color[].class, String[].class, Pointless[].class, MagicConverter[].class, Test[].class
        ).map(e -> n(
                "",
                () -> {
                    Assertions.assertTrue(e.isArray());
                    var s = MagicConverter.zero(e);
                    Assertions.assertEquals(e, s.getClass());
                    Assertions.assertEquals(0, Array.getLength(s));
                }
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testZeroArrays {0}")
    public void testZeroArrays(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testUnstringify() {
        var timestamp = "2025-10-10 04:03:02";
        var time = "04:03:02";
        var date = "2025-10-10";
        var ldt = LocalDateTime.of(2025, 10, 10, 4, 3, 2);
        var ld = ldt.toLocalDate();
        var lt = ldt.toLocalTime();
        var off = ldt.atOffset(ZoneOffset.UTC);
        var zon = off.toZonedDateTime();
        var ins = off.toInstant();
        var d = java.util.Date.from(ins);
        var gc = GregorianCalendar.from(zon);

        record Parts<E>(String input, Class<E> type, E output) {
            public String name() {
                var x = type.getSimpleName();
                return "Date".equals(x) ? type.getName() : x;
            }
        }

        var bigi = "123456789101112131415161718192021222324252627282930";
        var bigd = bigi + ".313233";
        var parts = List.of(
                new Parts<>("", boolean.class, false),
                new Parts<>("", byte.class, (byte) 0),
                new Parts<>("", char.class, (char) 0),
                new Parts<>("", short.class, (short) 0),
                new Parts<>("", int.class, 0),
                new Parts<>("", long.class, 0L),
                new Parts<>("", float.class, 0.0F),
                new Parts<>("", double.class, 0.0),

                new Parts<>("", Boolean.class, null),
                new Parts<>("", Byte.class, null),
                new Parts<>("", Character.class, null),
                new Parts<>("", Short.class, null),
                new Parts<>("", Integer.class, null),
                new Parts<>("", Long.class, null),
                new Parts<>("", Float.class, null),
                new Parts<>("", Double.class, null),
                new Parts<>("", BigInteger.class, null),
                new Parts<>("", BigDecimal.class, null),
                new Parts<>("", OptionalDouble.class, OptionalDouble.empty()),
                new Parts<>("", OptionalLong.class, OptionalLong.empty()),
                new Parts<>("", OptionalInt.class, OptionalInt.empty()),
                new Parts<>("", Color.class, null),
                new Parts<>("", LocalDate.class, null),

                new Parts<>("false", boolean.class, false),
                new Parts<>("false", Boolean.class, false),
                new Parts<>("true", boolean.class, true),
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
                new Parts<>("55", short.class, (short) 55),
                new Parts<>("55", Short.class, (short) 55),
                new Parts<>("-120", short.class, (short) -120),
                new Parts<>("-120", Short.class, (short) -120),
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
                new Parts<>(bigd, BigDecimal.class, new BigDecimal(bigd)),
                new Parts<>("123", OptionalDouble.class, OptionalDouble.of(123.0)),
                new Parts<>("-123", OptionalDouble.class, OptionalDouble.of(-123.0)),
                new Parts<>("123", OptionalLong.class, OptionalLong.of(123)),
                new Parts<>("-123", OptionalLong.class, OptionalLong.of(-123)),
                new Parts<>("123", OptionalInt.class, OptionalInt.of(123)),
                new Parts<>("-123", OptionalInt.class, OptionalInt.of(-123)),

                new Parts<>("", String.class, ""),
                new Parts<>("foo", String.class, "foo"),
                new Parts<>("GREEN", Color.class, Color.GREEN),
                new Parts<>("5", Wrapper.class, new Wrapper(5)),

                new Parts<>(timestamp, Instant.class, ins),
                new Parts<>(timestamp, LocalDateTime.class, ldt),
                new Parts<>(date     , LocalDate.class, ld),
                new Parts<>(time     , LocalTime.class, lt),
                new Parts<>(timestamp, OffsetDateTime.class, off),
                new Parts<>(timestamp, ZonedDateTime.class, zon),
                new Parts<>(timestamp, java.util.Date.class, d),
                new Parts<>(timestamp, Calendar.class, gc),
                new Parts<>(timestamp, GregorianCalendar.class, gc),
                new Parts<>(date     , java.sql.Date.class, java.sql.Date.valueOf(ld)),
                new Parts<>(time     , java.sql.Time.class, java.sql.Time.valueOf(lt)),
                new Parts<>(timestamp, java.sql.Timestamp.class, java.sql.Timestamp.valueOf(ldt))
        );
        return parts.stream().flatMap(e -> {
            var out = e.output();
            var name = e.input() + " as " + e.name();
            var x = n("unstringify " + name, () -> Assertions.assertEquals(out, MagicConverter.unstringify(e.input(), e.type())));
            var y = n("forValue " + name, () -> Assertions.assertEquals(out, MagicConverter.forValue(e.input(), e.type())));
            return Stream.of(x, y);
        }).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testUnstringify {0}")
    public void testUnstringify(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
