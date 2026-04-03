package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Array;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

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

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    public static enum Color {
        RED, ORANGE, GREEN, YELLOW, PURPLE, BLUE, WHITE, BLACK, BROWN, PINK, CYAN;
    }

    public static record Wrapper(int value) {}

    public static interface Pointless<X> extends List<X> {}

    public static record Recursive(Recursive r) {}

    @Test
    public void testRecursiveRecord() {
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(Recursive.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Recursive.class, ex.getRoot()),
                () -> Assertions.assertEquals("Recursive record class: " + Recursive.class.getName(), ex.getMessage())
        );
    }

    public static record Verbose(String bla, String blu) {}

    @Test
    public void testVerboseRecord() {
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(Verbose.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Verbose.class, ex.getRoot()),
                () -> Assertions.assertEquals("Non-single value record class where single-valued was expected: " + Verbose.class.getName(), ex.getMessage())
        );
    }

    @Test
    public void testBadEnum1() {
        var in = new Wrapper(5);
        var ex = Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.STD.get(Color.class).fromObj(in));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Color.class, ex.getOut()),
                () -> Assertions.assertEquals(Wrapper.class, ex.getIn()),
                () -> Assertions.assertEquals("Unsupported Type: " + Wrapper.class.getName(), ex.getMessage())
        );
    }

    @Test
    public void testBadEnum2() {
        var ex = Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.STD.get(Color.class).from("bla"));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Color.class, ex.getOut()),
                () -> Assertions.assertEquals(String.class, ex.getIn()),
                () -> Assertions.assertEquals("Can't read value as an enum object.", ex.getMessage())
        );
    }

    @TestFactory
    public Stream<DynamicTest> testSingleton() {
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
        );
    }

    private static void testNoConverter(Type k) {
        var msg = (k instanceof Class<?> kk && kk.isArray())
                ? "No converter for multidimensional arrays."
                : "No converter for " + k.getTypeName();
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(k));
        Assertions.assertAll(
                () -> Assertions.assertEquals(msg, ex.getMessage()),
                () -> Assertions.assertEquals(k, ex.getRoot())
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testBads() {
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

            public static DynamicTest args(Type t) {
                return n(t.getTypeName(), () -> testNoConverter(t));
            }
        }

        var mt = Stream.of(Foo.class.getMethods()).filter(m -> "a".equals(m.getName())).findFirst().get();
        var p = Stream.of(mt.getParameters()).map(java.lang.reflect.Parameter::getParameterizedType);
        var q = Stream.of(List.class, Collection.class, Set.class, Optional.class, Map.class, Pointless.class, POINTLESS, MAP_STRING_STRING);
        return Stream.concat(p, q).map(Foo::args);
    }

    @TestFactory
    public Stream<DynamicTest> testNullOverride() {
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
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testDefaultNulls() {
        return Stream.of(
                Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
                BigInteger.class, BigDecimal.class, Color.class, String.class, Wrapper.class,
                LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetTime.class, OffsetDateTime.class, ZonedDateTime.class,
                java.util.Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class,
                Calendar.class, GregorianCalendar.class, Instant.class,
                Ref.class, java.sql.RowId.class, Struct.class, java.sql.Array.class
        ).flatMap(e -> Stream.of(
                n(
                        e.getSimpleName() + " fromNull",
                        () -> Assertions.assertTrue(ConverterFactory.STD.get(e).fromNull().isEmpty())
                ),
                n(
                        e.getSimpleName() + "from(null)",
                        () -> Assertions.assertTrue(ConverterFactory.STD.get(e).fromObj(null).isEmpty())
                )
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testZeroLengthArrays() {
        return Stream.of(
                boolean[].class, byte[].class, char[].class, short[].class, int[].class, long[].class, float[].class, double[].class,
                Boolean[].class, Byte[].class, Character[].class, Short[].class,
                Integer[].class, Long[].class, Float[].class, Double[].class,
                BigInteger[].class, BigDecimal[].class,
                Color[].class, String[].class, Wrapper[].class,
                LocalDate[].class, LocalTime[].class, LocalDateTime[].class, OffsetTime[].class, OffsetDateTime[].class,
                ZonedDateTime[].class, Instant[].class,
                GregorianCalendar[].class, Calendar[].class,
                java.util.Date[].class, java.sql.Date[].class, java.sql.Time[].class, java.sql.Timestamp[].class,
                Struct[].class, Ref[].class, RowId[].class
        ).map(e -> n(
                e.getCanonicalName(),
                () -> {
                    var s = ConverterFactory.STD.get(e).fromNull().get();
                    Assertions.assertEquals(e, s.getClass());
                    Assertions.assertEquals(0, Array.getLength(s));
                }
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testSingletonArrays() {
        return Stream.of(
                new boolean[] {true}, new byte[] {(byte) 5}, /*new char[] {'x'},*/ new short[] {(short) 42}, new int[] {42},
                new long[] {42L}, new float[] {42F}, new double[] {42.0},
                new Boolean[] {true}, new Byte[] {(byte) 5}, /*new Character[] {'x'},*/ new Short[] {(short) 42}, new Integer[] {42},
                new Long[] {42L}, new Float[] {42F}, new Double[] {42.0},
                /*new BigInteger[] {BigInteger.valueOf(42)},*/ new BigDecimal[] {BigDecimal.valueOf(42) },
                /*new Color[] {Color.PINK }, new String[] {"x"}, new Wrapper[] {new Wrapper(42)},*/
                new LocalDate[] {LocalDate.of(2025, 5, 5)}, new LocalTime[] {LocalTime.of(12, 15, 25)},
                new LocalDateTime[] {LocalDateTime.of(2025, 4, 5, 12, 15, 25)},
                new OffsetTime[] {LocalTime.of(12, 15, 25).atOffset(ZoneOffset.UTC)},
                new OffsetDateTime[] {LocalDateTime.of(2025, 4, 5, 12, 15, 25).atOffset(ZoneOffset.UTC)}/*,
                new ZonedDateTime[] {LocalDateTime.of(2025, 4, 5, 12, 15, 25).atOffset(ZoneOffset.UTC).toZonedDateTime()},
                new Instant[] {LocalDateTime.of(2025, 4, 5, 12, 15, 25).atOffset(ZoneOffset.UTC).toInstant()},
                new GregorianCalendar[] {new GregorianCalendar(2025, 4, 5)},
                new Calendar[] {new GregorianCalendar(2025, 4, 5)},
                new java.util.Date[] {java.util.Date.from(LocalDateTime.of(2025, 4, 5, 12, 15, 25).atOffset(ZoneOffset.UTC).toInstant())},
                new java.sql.Date[] {java.sql.Date.valueOf(LocalDate.of(2025, 4, 5))},
                new java.sql.Time[] {java.sql.Time.valueOf(LocalTime.of(12, 15, 25))},
                new java.sql.Timestamp[] {java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 4, 5, 12, 15, 25))}*/
        ).map(e -> n(
                e.getClass().getComponentType().getCanonicalName() + "[]",
                () -> {
                    var s = ConverterFactory.STD.get(e.getClass()).fromObj(Array.get(e, 0)).get();
                    Assertions.assertEquals(s.getClass(), e.getClass());
                    Assertions.assertEquals(1, Array.getLength(s));
                    Assertions.assertEquals(Array.get(e, 0), Array.get(s, 0));
                }
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testFromString() {
        var timestamp = "2025-10-10 04:03:02";
        var time = "04:03:02";
        var date = "2025-10-10";
        var ldt = LocalDateTime.of(2025, 10, 10, 4, 3, 2);
        var ld = ldt.toLocalDate();
        var lt = ldt.toLocalTime();
        var odt = ldt.atOffset(ZoneOffset.UTC);
        var ot = lt.atOffset(ZoneOffset.UTC);
        var zdt = odt.toZonedDateTime();
        var ins = odt.toInstant();
        var d = java.util.Date.from(ins);
        var gc = GregorianCalendar.from(zdt);

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
                new Parts<>("", int[].class, new int[0]),
                new Parts<>("", Color.class, null),
                new Parts<>("", Wrapper.class, new Wrapper(0)),

                new Parts<>("", LocalDate.class, null),
                new Parts<>("", LocalTime.class, null),
                new Parts<>("", LocalDateTime.class, null),
                new Parts<>("", ZonedDateTime.class, null),
                new Parts<>("", OffsetDateTime.class, null),
                new Parts<>("", OffsetTime.class, null),
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
                new Parts<>("52", int[].class, new int[] {52}),
                new Parts<>("GREEN", Color.class, Color.GREEN),
                new Parts<>("5", Wrapper.class, new Wrapper(5)),

                new Parts<>(timestamp, Instant.class, ins),
                new Parts<>(timestamp, LocalDateTime.class, ldt),
                new Parts<>(date     , LocalDate.class, ld),
                new Parts<>(time     , LocalTime.class, lt),
                new Parts<>(time     , OffsetTime.class, ot),
                new Parts<>(timestamp, OffsetDateTime.class, odt),
                new Parts<>(timestamp, ZonedDateTime.class, zdt),
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
                } else if (out instanceof int[] b) {
                    Assertions.assertArrayEquals(b, (int[]) opt.get());
                } else {
                    Assertions.assertEquals(out, opt.get());
                }
            });
            return Stream.of(y);
        });
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        @FunctionalInterface
        interface Foo {
            public Object convert(Converter<?> in) throws ConvertionException;

            public default Consumer<Converter<?>> asFunc() {
                return in -> ForTests.testNull("in", () -> this.convert(in));
            }
        }

        record NamedCall2(String name, Consumer<Converter<?>> action) {
            public DynamicTest apply(Type c) {
                var cn = !(c instanceof Class<?> k) ? c.getTypeName()
                        : k.isArray() ? k.getComponentType().getSimpleName() + "[]"
                        : java.util.Date.class.isAssignableFrom(k) ? k.getName()
                        : k.getSimpleName();
                return n("for " + cn + " from " + name, () -> action.accept(ConverterFactory.STD.get(c)));
            }
        }

        record NamedCall(String name, Foo action) {
            public NamedCall2 asFunc() {
                return new NamedCall2(name, action.asFunc());
            }
        }

        var cls = List.of(
                boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class, char.class,
                Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class,
                OptionalInt.class, OptionalLong.class, OptionalDouble.class,
                byte[].class, int[].class, Double[].class, String.class,
                LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetTime.class, OffsetDateTime.class, ZonedDateTime.class,
                Instant.class, java.util.Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class,
                Calendar.class, GregorianCalendar.class, Ref.class, RowId.class, Struct.class,
                Color.class, Wrapper.class,
                COLLECTION_DATE, LIST_STRING, SET_COLOR, OPTIONAL_COLOR
        );
        var mtds = Stream.of(
                new NamedCall("BigDecimal", c -> c.from((BigDecimal) null)),
                new NamedCall("LocalDate", c -> c.from((LocalDate) null)),
                new NamedCall("LocalTime", c -> c.from((LocalTime) null)),
                new NamedCall("LocalDateTime", c -> c.from((LocalDateTime) null)),
                new NamedCall("OffsetTime", c -> c.from((OffsetTime) null)),
                new NamedCall("OffsetDateTime", c -> c.from((OffsetDateTime) null)),
                new NamedCall("String", c -> c.from((String) null)),
                new NamedCall("byte[]", c -> c.from((byte[]) null)),
                new NamedCall("Blob", c -> c.from((Blob) null)),
                new NamedCall("Clob", c -> c.from((Clob) null)),
                new NamedCall("NClob", c -> c.from((NClob) null)),
                new NamedCall("RowId", c -> c.from((RowId) null)),
                new NamedCall("Struct", c -> c.from((Struct) null)),
                new NamedCall("Array", c -> c.from((java.sql.Array) null)),
                new NamedCall("Ref", c -> c.from((Ref) null))
        ).map(NamedCall::asFunc).toList();
        return cls.stream().flatMap(k -> mtds.stream().map(m -> m.apply(k)));
    }
}
