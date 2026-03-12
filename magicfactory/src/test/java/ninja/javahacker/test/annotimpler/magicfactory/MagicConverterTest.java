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

    private static final Type COLLECTION_DATE;
    private static final Type LIST_STRING;
    private static final Type SET_COLOR;
    private static final Type OPTIONAL_COLOR;
    private static final Type POINTLESS;
    private static final Type MAP_STRING_STRING;

    static {
        try {
            var mtd = MagicConverterTest.class.getDeclaredMethod("noop", Collection.class, List.class, Set.class, Optional.class, Pointless.class, Map.class);
            COLLECTION_DATE = mtd.getParameters()[0].getParameterizedType();
            LIST_STRING = mtd.getParameters()[1].getParameterizedType();
            SET_COLOR = mtd.getParameters()[2].getParameterizedType();
            OPTIONAL_COLOR = mtd.getParameters()[3].getParameterizedType();
            POINTLESS = mtd.getParameters()[4].getParameterizedType();
            MAP_STRING_STRING = mtd.getParameters()[5].getParameterizedType();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static void noop(Collection<LocalDate> a, List<String> b, Set<Color> c, Optional<Color> d, Pointless<String> e, Map<String, String> g) {
        throw new AssertionError();
    }

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Wrapper(int value) {}

    public static interface Pointless<X> extends List<X> {}

    private static Stream<Arguments> testSimpleValueMapping() throws Exception {
        return Stream.of(
                n("basic int", () -> Assertions.assertEquals(42, ConverterFactory.STD.get(Integer.class).from(42).get())),
                n("basic long", () -> Assertions.assertEquals(42L, ConverterFactory.STD.get(Long.class).from(42L).get())),
                n("enum from int", () -> Assertions.assertEquals(Color.YELLOW, ConverterFactory.STD.get(Color.class).from(3).get())),
                n("enum from long", () -> Assertions.assertEquals(Color.BLUE, ConverterFactory.STD.get(Color.class).from(5L).get())),
                n("enum from BigDecimal", () -> Assertions.assertEquals(
                        Color.BLACK,
                        ConverterFactory.STD.get(Color.class).from(BigDecimal.valueOf(7)).get())
                ),
                n("simple record", () -> Assertions.assertEquals(
                        new Wrapper(25),
                        ConverterFactory.STD.get(Wrapper.class).from(25).get()
                )),
                n("simple record converts", () -> Assertions.assertEquals(
                        new Wrapper(25),
                        ConverterFactory.STD.get(Wrapper.class).from(25.0).get()
                ))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimpleValueMapping {0}")
    public void testSimpleValueMapping(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testConvertsMapping() throws Exception {
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
        var vals = List.of((byte) 52, (short) 52, 52, 52L, 52.0, 52.0f, BigDecimal.valueOf(52L));
        List<NamedTest> execs = new ArrayList<>(vals.size() * ts.size());
        for (var t : ts) {
            var w = t.isPrimitive() ? wrapper.get(t) : t;
            for (var v : vals) {
                Executable x = () -> {
                    var c = ConverterFactory.STD.get(t).from(v).get();
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
    public void testRecursiveRecord() throws Exception {
        var ex = Assertions.assertThrows(ConverterFactory.UnavailableConverterException.class, () -> ConverterFactory.STD.get(Recursive.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Recursive.class, ex.getRoot()),
                () -> Assertions.assertEquals("Recursive record class: " + Recursive.class.getName(), ex.getMessage()),
                () -> Assertions.assertEquals(Converter.ConvertionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals("Recursive record class: " + Recursive.class.getName(), ex.getCause().getMessage())
        );
    }

    public static record Verbose(String bla, String blu) {}

    @Test
    public void testVerboseRecord() throws Exception {
        var ex = Assertions.assertThrows(ConverterFactory.UnavailableConverterException.class, () -> ConverterFactory.STD.get(Verbose.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Verbose.class, ex.getRoot()),
                () -> Assertions.assertEquals("Non-single value record class where single-valued was expected: " + Verbose.class.getName(), ex.getMessage()),
                () -> Assertions.assertEquals(Converter.ConvertionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals("Non-single value record class where single-valued was expected: " + Verbose.class.getName(), ex.getCause().getMessage())
        );
    }

    @Test
    public void testBadEnum1() {
        var in = new Wrapper(5);
        var ex = Assertions.assertThrows(Converter.ConvertionException.class, () -> ConverterFactory.STD.get(Color.class).from(in));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Wrapper.class, ex.getRoot()),
                () -> Assertions.assertEquals("Unsupported Type: " + Wrapper.class.getName(), ex.getMessage())
        );
    }

    @Test
    public void testBadEnum2() {
        var ex = Assertions.assertThrows(Converter.ConvertionException.class, () -> ConverterFactory.STD.get(Color.class).from("bla"));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Color.class, ex.getRoot()),
                () -> Assertions.assertEquals("Can't read value as an enum object.", ex.getMessage())
        );
    }

    private static Stream<Arguments> testSingleton() {
        var bla = "blabla";
        var blu = LocalDate.of(2000, 1, 1);
        return Stream.of(
                n("List<String> from String", () -> Assertions.assertEquals(
                        List.of(bla),
                        ConverterFactory.STD.get(LIST_STRING).from(bla).get()
                )),
                n("Collection<LocalDate> from LocalDate", () -> Assertions.assertEquals(
                        List.of(blu),
                        ConverterFactory.STD.get(COLLECTION_DATE).from(blu).get()
                )),
                n("Set<Enum> from ordinal", () -> Assertions.assertEquals(
                        Set.of(Color.YELLOW),
                        ConverterFactory.STD.get(SET_COLOR).from(Color.YELLOW.ordinal()).get()
                )),
                n("Set<Enum> from ordinal as String", () -> Assertions.assertEquals(
                        Set.of(Color.BLUE),
                        ConverterFactory.STD.get(SET_COLOR).from(String.valueOf(Color.BLUE.ordinal())).get()
                )),
                n("Optional<Enum> from name", () -> Assertions.assertEquals(
                        Optional.of(Color.PINK),
                        ConverterFactory.STD.get(OPTIONAL_COLOR).from(Color.PINK.name()).get()
                )),
                n("Optional<Enum> from ordinal as long", () -> Assertions.assertEquals(
                        Optional.of(Color.BLACK),
                        ConverterFactory.STD.get(OPTIONAL_COLOR).from((long) Color.BLACK.ordinal()).get()
                )),
                n("Optional<Enum> from ordinal as BigDecimal", () -> Assertions.assertEquals(
                        Optional.of(Color.GREEN),
                        ConverterFactory.STD.get(OPTIONAL_COLOR).from(BigDecimal.valueOf(Color.GREEN.ordinal())).get()
                ))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSingleton {0}")
    public void testSingleton(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static void testNoConverter(Type k) {
        var msg = (k instanceof Class<?> kk && kk.isArray()) ? "No converter for multidimensional arrays." : "No converter for " + k.getTypeName();
        var ex = Assertions.assertThrows(ConverterFactory.UnavailableConverterException.class, () -> ConverterFactory.STD.get(k));
        Assertions.assertAll(
                () -> Assertions.assertEquals(msg, ex.getMessage()),
                () -> Assertions.assertEquals(k, ex.getRoot())
        );
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testBads() throws Exception {
        interface Foo<E> {
            void a(
                    E a,
                    List<E> b,
                    List<? extends Serializable> c,
                    Set<E> d,
                    Set<? extends Serializable> e,
                    Collection<E> f,
                    Collection<? extends Serializable> g,
                    Optional<E> h,
                    Optional<? extends Serializable> i,
                    List<E[]> j,
                    E[] k,
                    int[][] m,
                    boolean[][][] n,
                    String[][] o
            );

            public static Arguments args(Type t) {
                return n(t.getTypeName(), () -> testNoConverter(t)).args();
            }
        }

        var mt = Stream.of(Foo.class.getMethods()).filter(m -> "a".equals(m.getName())).findFirst().get();
        var p = Stream.of(mt.getParameters()).map(java.lang.reflect.Parameter::getParameterizedType);
        var q = Stream.of(List.class, Collection.class, Set.class, Optional.class, Map.class, Pointless.class, POINTLESS, MAP_STRING_STRING);
        return Stream.concat(p, q).map(Foo::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testBads {0}")
    public void testBads(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testNullOverride() {
        return Map.ofEntries(
                Map.entry(boolean.class, false),
                Map.entry(byte.class, (byte) 0),
                Map.entry(char.class, '\0'),
                Map.entry(short.class, (short) 0),
                Map.entry(int.class, 0),
                Map.entry(long.class, 0L),
                Map.entry(float.class, 0f),
                Map.entry(double.class, 0.0),
                Map.entry(OPTIONAL_COLOR, Optional.empty()),
                Map.entry(OptionalInt.class, OptionalInt.empty()),
                Map.entry(OptionalLong.class, OptionalLong.empty()),
                Map.entry(OptionalDouble.class, OptionalDouble.empty()),
                Map.entry(COLLECTION_DATE, List.of()),
                Map.entry(LIST_STRING, List.of()),
                Map.entry(SET_COLOR, Set.of())
        )
        .entrySet()
        .stream()
        .map(e -> n(
                "zero(" + e.getKey().getTypeName() + ")",
                () -> Assertions.assertEquals(e.getValue(), ConverterFactory.STD.get(e.getKey()).fromNull().get(), e.getKey().getTypeName())
        ))
        .map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testNullOverride {0}")
    public void testNullOverride(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testDefaultNulls() {
        return Stream.of(
                Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
                BigInteger.class, BigDecimal.class, Color.class, String.class, Wrapper.class,
                LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetTime.class, OffsetDateTime.class, ZonedDateTime.class,
                java.util.Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class,
                Calendar.class, GregorianCalendar.class, Instant.class,
                java.sql.Ref.class, java.sql.RowId.class, java.sql.Array.class
        ).map(e -> n(
                e.getSimpleName(),
                () -> Assertions.assertTrue(ConverterFactory.STD.get(e).fromNull().isEmpty(), e.getTypeName())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testDefaultNulls {0}")
    public void testDefaultNulls(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testZeroLengthArrays() {
        return Stream.of(
                boolean[].class, byte[].class, char[].class, short[].class, int[].class, long[].class, float[].class, double[].class,
                Boolean[].class, Byte[].class, Character[].class, Short[].class,
                Integer[].class, Long[].class, Float[].class, Double[].class,
                BigInteger[].class, BigDecimal[].class,
                Color[].class, String[].class, Wrapper[].class, LocalDate[].class, GregorianCalendar[].class
        ).map(e -> n(
                e.getCanonicalName(),
                () -> {
                    var s = ConverterFactory.STD.get(e).fromNull().get();
                    Assertions.assertEquals(e, s.getClass());
                    Assertions.assertEquals(0, Array.getLength(s));
                }
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testZeroLengthArrays {0}")
    public void testZeroLengthArrays(String name, Executable exec) throws Throwable {
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
                return List.of("Date", "Time", "Timestamp").contains(x) ? type.getName() : x;
            }
        }

        var bigi = "123456789101112131415161718192021222324252627282930";
        var bigd = bigi + ".313233";
        var parts = List.of(
                new Parts<>("", boolean.class, false),
                new Parts<>("", byte.class, (byte) 0),
                new Parts<>("", char.class, '\0'),
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

                new Parts<>("", String.class, ""),
                new Parts<>("", byte[].class, new byte[0]),
                new Parts<>("", Color.class, null),
                new Parts<>("", Wrapper.class, new Wrapper(0)),

                new Parts<>("", LocalDate.class, null),
                new Parts<>("", LocalTime.class, null),
                new Parts<>("", LocalDateTime.class, null),
                new Parts<>("", ZonedDateTime.class, null),
                new Parts<>("", OffsetDateTime.class, null),
                new Parts<>("", Instant.class, null),
                new Parts<>("", Calendar.class, null),
                new Parts<>("", GregorianCalendar.class, null),
                new Parts<>("", java.util.Date.class, null),
                new Parts<>("", java.sql.Date.class, null),
                new Parts<>("", java.sql.Time.class, null),
                new Parts<>("", java.sql.Timestamp.class, null),

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

                new Parts<>("foo", String.class, "foo"),
                new Parts<>("foo", byte[].class, "foo".getBytes()),
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
            var in = e.input();
            var out = e.output();
            var name = "from " + ("".equals(in) ? "<empty>" : in) + " as " + e.name();
            var y = n(name, () -> {
                var opt = ConverterFactory.STD.get(e.type()).from(in);
                if (out == null) {
                    Assertions.assertTrue(opt.isEmpty());
                } else if (out instanceof byte[] b) {
                    Assertions.assertArrayEquals(b, (byte[]) opt.get());
                } else {
                    Assertions.assertEquals(out, opt.get());
                }
            });
            return Stream.of(y);
        }).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testUnstringify {0}")
    public void testUnstringify(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
