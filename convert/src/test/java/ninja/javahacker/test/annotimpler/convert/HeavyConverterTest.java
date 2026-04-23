package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

@SuppressWarnings("unused")
public class HeavyConverterTest {

    public static interface Giver {
        public Optional<?> give() throws Exception;
    }

    public static interface Giver2 {
        public void receive(Giver giver1) throws Exception;
    }

    public static interface MethodSpec<E> {
        public Optional<?> receive(Converter<?> cvt, E in) throws Exception;
    }

    public record Elements<E>(Class<E> k, List<E> data) {
        public <X> Elements<X> map(Class<X> k2, Function<E, X> f) {
            return new Elements<>(k2, data.stream().map(x -> x == null ? null : f.apply(x)).toList());
        }
    }

    public static <E> Elements<E> e(Class<E> k, List<E> data) {
        return new Elements<>(k, data);
    }

    private Long lo(BigInteger x) {
        if (x == null) return null;
        var max = BigInteger.valueOf(Long.MAX_VALUE);
        var min = BigInteger.valueOf(Long.MIN_VALUE);
        return x.compareTo(min) < 0 || x.compareTo(max) > 0 ? null : x.longValue();
    }

    private Integer i(Long x) {
        return x == null || x < Integer.MIN_VALUE || x > Integer.MAX_VALUE ? null : x.intValue();
    }

    private Character c(Long x) {
        return x == null || x < Character.MIN_VALUE || x > Character.MAX_VALUE ? null : (char) x.intValue();
    }

    private Short s(Long x) {
        return x == null || x < Short.MIN_VALUE || x > Short.MAX_VALUE ? null : x.shortValue();
    }

    private Byte b(Long x) {
        return x == null || x < Byte.MIN_VALUE || x > Byte.MAX_VALUE ? null : x.byteValue();
    }

    private BigInteger bi(String x) {
        return x == null || List.of("q", "xxx", "RED", "NaN", "Infinity", "-Infinity").contains(x) || x.contains(".") ? null : new BigInteger(x);
    }

    private BigDecimal bd(String x) {
        return x == null || List.of("q", "xxx", "RED", "NaN", "Infinity", "-Infinity").contains(x) ? null : new BigDecimal(x);
    }

    private OffsetDateTime odt(String x) {
        return x == null || x.contains("xxx") ? null : OffsetDateTime.parse(x, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][ xxxxx]").withResolverStyle(ResolverStyle.STRICT));
    }

    private static <E> MethodSpec<E> spec(Class<E> k) {
        if (k == boolean       .class) return (cvt, in) -> cvt.from((boolean       ) in);
        if (k == byte          .class) return (cvt, in) -> cvt.from((byte          ) in);
        if (k == short         .class) return (cvt, in) -> cvt.from((short         ) in);
        if (k == int           .class) return (cvt, in) -> cvt.from((int           ) in);
        if (k == long          .class) return (cvt, in) -> cvt.from((long          ) in);
        if (k == float         .class) return (cvt, in) -> cvt.from((float         ) in);
        if (k == double        .class) return (cvt, in) -> cvt.from((double        ) in);
        if (k == BigDecimal    .class) return (cvt, in) -> cvt.from((BigDecimal    ) in);
        if (k == String        .class) return (cvt, in) -> cvt.from((String        ) in);
        if (k == byte[]        .class) return (cvt, in) -> cvt.from((byte[]        ) in);
        if (k == LocalDate     .class) return (cvt, in) -> cvt.from((LocalDate     ) in);
        if (k == LocalDateTime .class) return (cvt, in) -> cvt.from((LocalDateTime ) in);
        if (k == LocalTime     .class) return (cvt, in) -> cvt.from((LocalTime     ) in);
        if (k == OffsetDateTime.class) return (cvt, in) -> cvt.from((OffsetDateTime) in);
        if (k == OffsetTime    .class) return (cvt, in) -> cvt.from((OffsetTime    ) in);
        if (k == Blob          .class) return (cvt, in) -> cvt.from((Blob          ) in);
        if (k == Clob          .class) return (cvt, in) -> cvt.from((Clob          ) in);
        if (k == NClob         .class) return (cvt, in) -> cvt.from((NClob         ) in);
        if (k == Ref           .class) return (cvt, in) -> cvt.from((Ref           ) in);
        if (k == RowId         .class) return (cvt, in) -> cvt.from((RowId         ) in);
        if (k == Struct        .class) return (cvt, in) -> cvt.from((Struct        ) in);
        if (k == SQLXML        .class) return (cvt, in) -> cvt.from((SQLXML        ) in);
        if (k == java.sql.Array.class) return (cvt, in) -> cvt.from((java.sql.Array) in);
        throw new AssertionError();
    }

    @SuppressWarnings({"element-type-mismatch", "AssertEqualsBetweenInconvertibleTypes"})
    private static void checkException(ConvertionException ce, boolean unsupported, Type base, Type k1, Type k3) {
        var e1a = "Can't read value as " + TypeName.of(k3) + ".";
        var e1b = "Can't read value as " + TypeName.of(k1) + ".";
        var e2 = "Unsupported " + name(base) + ".";
        var n = 0;
        for (Throwable k = ce; k != null; k = k.getCause()) {
            n++;
        }
        var isNumeric = List.of(byte.class, short.class, int.class, long.class, float.class, double.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigDecimal.class, BigInteger.class).contains(k3);
        var isTemporal = List.of(LocalDate.class, LocalDateTime.class, LocalTime.class, OffsetDateTime.class, OffsetTime.class, Instant.class).contains(k3);
        var temporalBack = Map.of(
                java.util.Date.class, ZonedDateTime.class,
                Calendar.class, ZonedDateTime.class,
                GregorianCalendar.class, ZonedDateTime.class,
                java.sql.Date.class, LocalDate.class,
                java.sql.Time.class, LocalTime.class,
                java.sql.Timestamp.class, LocalDateTime.class
        );
        var isTemporalLegacy = temporalBack.containsKey(k3);
        List<Executable> parts = new ArrayList<>(20);
        parts.add(() -> Assertions.assertEquals(unsupported ? e2 : e1a, ce.getMessage()));
        parts.add(() -> Assertions.assertEquals(base, ce.getIn()));
        parts.add(() -> Assertions.assertEquals(k3, ce.getOut()));
        if (n == 1) {
        } else if (n == 2 && isNumeric) {
            var ex = base == String.class ? NumberFormatException.class : ArithmeticException.class;
            parts.add(() -> Assertions.assertEquals(ex, ce.getCause().getClass()));
        } else if (n == 2 && isTemporal) {
            //parts.add(() -> Assertions.assertEquals(DateTimeParseException.class, ce.getCause().getClass()));
        } else if (n == 2 && isTemporalLegacy) {
            var k5 = temporalBack.get(k3);
            var r = "Can't read value as " + k5.getSimpleName() + ".";
            parts.add(() -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()));
            parts.add(() -> Assertions.assertEquals(unsupported ? e2 : r, ce.getCause().getMessage()));
            parts.add(() -> Assertions.assertEquals(base, ((ConvertionException) ce.getCause()).getIn()));
            parts.add(() -> Assertions.assertEquals(k5, ((ConvertionException) ce.getCause()).getOut()));
        } else if (n == 2) {
            /*parts.add(() -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()));
            parts.add(() -> Assertions.assertEquals(unsupported ? e2 : e1b, ce.getCause().getMessage()));
            parts.add(() -> Assertions.assertEquals(base, ((ConvertionException) ce.getCause()).getIn()));
            parts.add(() -> Assertions.assertEquals(k1, ((ConvertionException) ce.getCause()).getOut()));*/
        } else if (n == 3 && isNumeric) {
            /*parts.add(() -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()));
            parts.add(() -> Assertions.assertEquals(unsupported ? e2 : e1b, ce.getCause().getMessage()));
            parts.add(() -> Assertions.assertEquals(base, ((ConvertionException) ce.getCause()).getIn()));
            parts.add(() -> Assertions.assertEquals(k1, ((ConvertionException) ce.getCause()).getOut()));
            parts.add(() -> Assertions.assertEquals(NumberFormatException.class, ce.getCause().getCause().getClass()));*/
        } else if (n == 3 && isTemporal) {
            /*parts.add(() -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()));
            parts.add(() -> Assertions.assertEquals(unsupported ? e2 : e1b, ce.getCause().getMessage()));
            parts.add(() -> Assertions.assertEquals(base, ((ConvertionException) ce.getCause()).getIn()));
            parts.add(() -> Assertions.assertEquals(k1, ((ConvertionException) ce.getCause()).getOut()));
            parts.add(() -> Assertions.assertEquals(DateTimeParseException.class, ce.getCause().getCause().getClass()));*/
        } else if (n == 3 && isTemporalLegacy) {
            /*parts.add(() -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()));
            parts.add(() -> Assertions.assertEquals(unsupported ? e2 : e1b, ce.getCause().getMessage()));
            parts.add(() -> Assertions.assertEquals(base, ((ConvertionException) ce.getCause()).getIn()));
            parts.add(() -> Assertions.assertEquals(k1, ((ConvertionException) ce.getCause()).getOut()));

            var k5 = temporalBack.get(k3);
            var r = "Can't read value as " + k5.getSimpleName() + ".";
            parts.add(() -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getCause().getClass()));
            parts.add(() -> Assertions.assertEquals(unsupported ? e2 : r, ce.getCause().getCause().getMessage()));
            parts.add(() -> Assertions.assertEquals(base, ((ConvertionException) ce.getCause().getCause()).getIn()));
            parts.add(() -> Assertions.assertEquals(k5, ((ConvertionException) ce.getCause().getCause()).getOut()));*/
        } else if (n == 3) {
        } else if (n == 4) {
        } else {
            throw new AssertionError();
        }
        Assertions.assertAll(parts);
    }

    private static String name(Type t) {
        return TypeName.of(t, Set.of(java.sql.Date.class, java.util.Date.class));
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private <E> DynamicNode testIn(
            String prefix,
            List<? extends Class<?>> typesForConverters,
            Elements<E> inputs,
            List<? extends Elements<?>> lists)
            throws Exception
    {
        var base = inputs.k();
        var m = spec(base);
        var nb = name(base);
        Map<Class<?>, List<?>> mappings = new HashMap<>(lists.size());
        for (var a1 : lists) {
            mappings.put(a1.k(), a1.data());
        }

        List<DynamicNode> nodes1 = new ArrayList<>(typesForConverters.size());

        for (var k1 : typesForConverters) {
            if (k1 == null) throw new AssertionError();

            var v1 = mappings.get(WrapperClass.unwrap(k1));
            List<DynamicNode> nodes2 = new ArrayList<>(12 * inputs.data().size());

            for (var i = 0; i < inputs.data().size(); i++) {
                var in = inputs.data().get(i);
                if (in == null) continue;
                var out = v1 == null || i >= v1.size() ? null : v1.get(i);

                var inStr = in instanceof Blob ? ""
                        : in instanceof NClob ? ""
                        : in instanceof Clob ? ""
                        : in instanceof SQLXML ? ""
                        : in instanceof RowId ? ""
                        : in instanceof byte[] x ? " - (byte[]) " + new String(x)
                        : in instanceof char[] x ? " - (char[]) " + new String(x)
                        : in instanceof Object[] ? ""
                        : " - " + in;

                var k3all = TestTypes.others(k1);
                for (var k3 : k3all) {
                    if (k3 == byte[].class && k1 != k3) continue;
                    if (k3 == char[].class && k1 != k3) continue;
                    var o2 = TestTypes.wrap(out, k3);
                    var unsupported = v1 == null && (base != String.class || TestTypes.SPECIALS.contains(k1));

                    Giver2 err = exec0 -> {
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> exec0.give());
                        checkException(ce, unsupported, base, k1, k3);
                    };

                    Giver2 ok = exec0 -> TestTypes.compare(o2, exec0.give().get());
                    var exec = unsupported || out == null ? err : ok;
                    var res = unsupported ? " - should be unsupported." : out == null ? " - should not read." : " - should be ok.";

                    var nd1 = DynamicTest.dynamicTest(prefix + " Converter for " + name(k3) + " from "    + nb + inStr + res, () -> {
                        var cvt = ConverterFactory.STD.get(k3);
                        exec.receive(() -> m.receive(cvt, in));
                    });
                    var nd2 = DynamicTest.dynamicTest(prefix + " Converter for " + name(k3) + " fromObj " + nb + inStr + res, () -> {
                        var cvt = ConverterFactory.STD.get(k3);
                        exec.receive(() -> cvt.fromObj(in));
                    });
                    nodes2.add(nd1);
                    nodes2.add(nd2);
                }
                if (k3all.isEmpty()) {
                    var nf = DynamicTest.dynamicTest(prefix + " No types found for " + name(k1) + " from " + nb + ".", () -> { throw new AssertionError(); });
                    nodes2.add(nf);
                }
            }
            if (nodes2.isEmpty()) {
                var nf = DynamicTest.dynamicTest(prefix + " No tests for " + name(k1) + " from " + nb + ".", () -> { throw new AssertionError(); });
                nodes2.add(nf);
            }
            nodes1.add(DynamicContainer.dynamicContainer(prefix + " Test convertions for " + name(k1) + " from " + nb + ".", nodes2));
        }
        return DynamicContainer.dynamicContainer(prefix + " Test convertions from " + nb + ".", nodes1);
    }

    @TestFactory
    public List<DynamicNode> testNumericTypes() throws Exception {
        var prefix1 = "[testNumericTypes - byte]";
        var prefix2 = "[testNumericTypes - short]";
        var prefix3 = "[testNumericTypes - int]";
        var prefix4 = "[testNumericTypes - long]";
        var prefix5 = "[testNumericTypes - float]";
        var prefix6 = "[testNumericTypes - double]";
        var prefix7 = "[testNumericTypes - BigDecimal]";
        var prefix8 = "[testNumericTypes - String]";
        var strs = e(String.class, List.of(
                "q", "xxx", "RED",
                "0", "1", "9", "42", "55", "127", "-30", "-128", "32000", "64000",
                "489876544", "12345678910", "9876543210987654", "98765432109876543210",
                "16777217", "9007199254740993",
                "3.5", "0.078", "-177.77", "98765432109876543210.98765432",
                "NaN", "Infinity", "-Infinity"
        ));
        var floats = e(float.class, Arrays.asList(
                null, null, null,
                0F, 1F, 9F, 42F, 55F, 127F, -30F, -128F, 32000F, 64000F,
                489876544F, null, null, null,
                null, null,
                3.5F, 0.078F, -177.77F, null,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
        ));
        var doubles = e(double.class, Arrays.asList(
                null, null, null,
                0D, 1D, 9D, 42D, 55D, 127D, -30D, -128D, 32000D, 64000D,
                489876544D, 12345678910D, 9876543210987654D, null,
                16777217.0D, null,
                3.5D, 0.078D, -177.77D, null,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        ));
        var chs   = e(char           .class, Arrays.asList('q', null, null, '0', '1', '9'));
        var ens   = e(TestTypes.Color.class, Arrays.asList(null, null, TestTypes.Color.RED, TestTypes.Color.RED, TestTypes.Color.GREEN));
        var bools = e(boolean        .class, Arrays.asList(null, null, null, false, true));

        var bigds      = strs .map(BigDecimal.class, this::bd);
        var bigis      = strs .map(BigInteger.class, this::bi);
        var charsArs   = strs .map(char[]    .class, String::toCharArray);
        var bytesArs   = strs .map(byte[]    .class, String::getBytes);
        var longs      = bigis.map(long      .class, this::lo);
        var bytes      = longs.map(byte      .class, this::b );
        var chars      = longs.map(char      .class, this::c );
        var shorts     = longs.map(short     .class, this::s );
        var ints       = longs.map(int       .class, this::i );
        var bytesA     = bytes.map(byte[]    .class, x -> new byte[] {x});
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));
        var r4bools    = bools   .map(TestTypes.R4boolean     .class, TestTypes.R4boolean  ::new);
        var r4ints     = ints    .map(TestTypes.R4int         .class, TestTypes.R4int      ::new);
        var r4longs    = longs   .map(TestTypes.R4long        .class, TestTypes.R4long     ::new);
        var r4doubles  = doubles .map(TestTypes.R4double      .class, TestTypes.R4double   ::new);
        var r4color    = ens     .map(TestTypes.R4Color       .class, TestTypes.R4Color    ::new);
        var r4bas1     = bytesArs.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4bas2     = bytesA  .map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA    = strs    .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB    = strs    .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC    = r4strsB .map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD    = strs    .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE    = r4strsC .map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF    = r4strsE .map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var all  = List.of(
                bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens, strs,
                r4bools, r4ints, r4longs, r4doubles, r4color, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF
        );
        var allB = List.of(
                bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens, strs,
                r4bools, r4ints, r4longs, r4doubles, r4color, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                bytesA, r4bas2
        );
        var allC = List.of(
                bools, bytes, shorts, chs  , ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens, strs,
                r4bools, r4ints, r4longs, r4doubles, r4color, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                charsArs, bytesArs, r4bas1
        );

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var byteNode   = testIn(prefix1, cvts, bytes  , allB);
        var shortNode  = testIn(prefix2, cvts, shorts , all );
        var intNode    = testIn(prefix3, cvts, ints   , all );
        var longNode   = testIn(prefix4, cvts, longs  , all );
        var floatNode  = testIn(prefix5, cvts, floats , all );
        var doubleNode = testIn(prefix6, cvts, doubles, all );
        var bigdNode   = testIn(prefix7, cvts, bigds  , all );
        var strNode    = testIn(prefix8, cvts, strs   , allC);

        return List.of(byteNode, shortNode, intNode, longNode, floatNode, doubleNode, bigdNode, strNode);
    }

    @TestFactory
    public List<DynamicNode> testMinusZero() throws Exception {
        var prefix1 = "[testMinusZero - float]";
        var prefix2 = "[testMinusZero - double]";
        var prefix3 = "[testMinusZero - String]";
        var strs    = e(String .class, List.of("-0"));
        var floats  = e(float  .class, List.of(-0.0F));
        var doubles = e(double .class, List.of(-0.0));
        var bools   = e(boolean.class, List.of(false));
        var ens     = e(TestTypes.Color.class, List.of(TestTypes.Color.RED));

        var bigds      = strs .map(BigDecimal.class, this::bd);
        var bigis      = strs .map(BigInteger.class, this::bi);
        var charsArs   = strs .map(char[]    .class, String::toCharArray);
        var bytesArs   = strs .map(byte[]    .class, String::getBytes);
        var longs      = bigis.map(long      .class, this::lo);
        var bytes      = longs.map(byte      .class, this::b);
        var chars      = longs.map(char      .class, this::c);
        var shorts     = longs.map(short     .class, this::s);
        var ints       = longs.map(int       .class, this::i);
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));
        var r4bools    = bools   .map(TestTypes.R4boolean     .class, TestTypes.R4boolean  ::new);
        var r4ints     = ints    .map(TestTypes.R4int         .class, TestTypes.R4int      ::new);
        var r4longs    = longs   .map(TestTypes.R4long        .class, TestTypes.R4long     ::new);
        var r4doubles  = doubles .map(TestTypes.R4double      .class, TestTypes.R4double   ::new);
        var r4color    = ens     .map(TestTypes.R4Color       .class, TestTypes.R4Color    ::new);
        var r4bas      = bytesArs.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA    = strs    .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB    = strs    .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC    = r4strsB .map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD    = strs    .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE    = r4strsC .map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF    = r4strsE .map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var all1 = List.of(
                bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                r4bools, r4ints, r4longs, r4doubles, r4color
        );
        var all2 = List.of(
                bools, bytes, shorts,        ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, ens,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                r4bools, r4ints, r4longs, r4doubles, r4color,
                charsArs, bytesArs, r4bas
        );

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var floatNode  = testIn(prefix1, cvts, floats , all1);
        var doubleNode = testIn(prefix2, cvts, doubles, all1);
        var strNode    = testIn(prefix3, cvts, strs   , all2);

        return List.of(floatNode, doubleNode, strNode);
    }

    @TestFactory
    public List<DynamicNode> testBooleanTypes() throws Exception {
        var prefix1 = "[testBooleanTypes - from boolean]";
        var prefix2 = "[testBooleanTypes - from String]";
        var longs     = e(long   .class, List.of(0L, 1L));
        var floats    = e(float  .class, List.of(0F, 1F));
        var doubles   = e(double .class, List.of(0D, 1D));
        var bools1    = e(boolean.class, List.of(false, true));
        var bools2    = e(boolean.class, List.of(false, true, false, true));
        var strs      = e(String .class, List.of("false", "true", "FALSE", "TRUE"));
        var bytesArs1 = e(byte[] .class, List.of(new byte[] {0}, new byte[] {1}));
        var bigds      = longs.map(BigDecimal.class, BigDecimal::valueOf);
        var bigis      = longs.map(BigInteger.class, BigInteger::valueOf);
        var charsArs   = strs .map(char[]    .class, String::toCharArray);
        var bytesArsS  = strs .map(byte[]    .class, String::getBytes);
        var bytes      = longs.map(byte      .class, this::b);
        var chars      = longs.map(char      .class, this::c);
        var shorts     = longs.map(short     .class, this::s);
        var ints       = longs.map(int       .class, this::i);
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));
        var r4bools    = bools2   .map(TestTypes.R4boolean     .class, TestTypes.R4boolean  ::new);
        var r4ints     = ints     .map(TestTypes.R4int         .class, TestTypes.R4int      ::new);
        var r4longs    = longs    .map(TestTypes.R4long        .class, TestTypes.R4long     ::new);
        var r4doubles  = doubles  .map(TestTypes.R4double      .class, TestTypes.R4double   ::new);
        var r4bas1     = bytesArs1.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4basS     = bytesArsS.map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA    = strs     .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB    = strs     .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC    = r4strsB  .map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD    = strs     .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE    = r4strsC  .map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF    = r4strsE  .map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var allB = List.of(
                bools1, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                bytesArs1, r4bas1, r4bools, r4ints, r4longs, r4doubles
        );
        var allD = List.of(
                bools2,                charsArs,
                strs, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF,
                bytesArsS, r4basS, r4bools
        );

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var boolNode   = testIn(prefix1, cvts, bools1, allB);
        var str2Node   = testIn(prefix2, cvts, strs  , allD);

        return List.of(boolNode, str2Node);
    }

    @TestFactory
    public List<DynamicNode> testTemporalTypes() throws Exception {
        var prefix1a = "[testTemporalTypes - From Date + Time + Timezone]";
        var prefix1b = "[testTemporalTypes - From String with Date + Time + Timezone]";
        var prefix2a = "[testTemporalTypes - From Date + Time, no Timezone]";
        var prefix2b = "[testTemporalTypes - From String with Date + Time, no Timezone]";
        var prefix3a = "[testTemporalTypes - From Only Date]";
        var prefix3b = "[testTemporalTypes - From String with Only Date]";
        var prefix4a = "[testTemporalTypes - From Time + Timezone, no Date]";
        var prefix4b = "[testTemporalTypes - From String with Time + Timezone, no Date]";
        var prefix5a = "[testTemporalTypes - From Only Time]";
        var prefix5b = "[testTemporalTypes - From String with Only Time]";

        var str1 = e(String.class, List.of(
                "2026-01-02 03:04:05 +06:13",
                "2025-10-31 13:14:15.123456 +11:55:44",
                "2022-04-12 08:07:06.123456789 -03:04:05",
                "2023-07-14 11:12:13.123 -00:12",
                "2021-10-04 13:14:15.12 -12:20",
                "2022-09-14 21:10:12.1 -04:10",
                "xxx xxx xxx"
        ));
        var str2 = str1.map(String.class, x -> x.split(" ")[0] + " " + x.split(" ")[1]);
        var str3 = str1.map(String.class, x -> x.split(" ")[0]);
        var str4 = str1.map(String.class, x -> x.split(" ")[1] + " " + x.split(" ")[2]);
        var str5 = str1.map(String.class, x -> x.split(" ")[1]);

        var r4str1A = str1.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str2A = str2.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str3A = str3.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str4A = str4.map(TestTypes.R4String.class, TestTypes.R4String::new);
        var r4str5A = str5.map(TestTypes.R4String.class, TestTypes.R4String::new);

        var r4str1B = str1.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str2B = str2.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str3B = str3.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str4B = str4.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4str5B = str5.map(TestTypes.R4StringList.class, x -> new TestTypes.R4StringList(List.of(x)));

        var r4str1C = r4str1B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str2C = r4str2B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str3C = r4str3B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str4C = r4str4B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);
        var r4str5C = r4str5B.map(TestTypes.R4Record.class, TestTypes.R4Record::new);

        var r4str1D = str1.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str2D = str2.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str3D = str3.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str4D = str4.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4str5D = str5.map(TestTypes.R4StringArray.class, x -> new TestTypes.R4StringArray(new String[] {x}));

        var r4str1E = r4str1C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str2E = r4str2C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str3E = r4str3C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str4E = r4str4C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4str5E = r4str5C.map(TestTypes.R4RecordDeep.class, x -> new TestTypes.R4RecordDeep(List.of(x)));

        var r4str1F = r4str1E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str2F = r4str2E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str3F = r4str3E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str4F = r4str4E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));
        var r4str5F = r4str5E.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var odts1 = str1 .map(OffsetDateTime      .class, this::odt);
        var lds1  = odts1.map(LocalDate           .class, OffsetDateTime::toLocalDate);
        var ldts1 = odts1.map(LocalDateTime       .class, OffsetDateTime::toLocalDateTime);
        var lts1  = odts1.map(LocalTime           .class, OffsetDateTime::toLocalTime);
        var ots1  = odts1.map(OffsetTime          .class, OffsetDateTime::toOffsetTime);
        var ins1  = odts1.map(Instant             .class, OffsetDateTime::toInstant);
        var zdts1 = odts1.map(ZonedDateTime       .class, OffsetDateTime::toZonedDateTime);
        var gcs1  = zdts1.map(GregorianCalendar   .class, GregorianCalendar::from);
        var cs1   = gcs1 .map(Calendar            .class, gc -> gc);
        var uds1  = ins1 .map(java.util.Date      .class, java.util.Date::from);
        var tss1  = ldts1.map(java.sql.Timestamp  .class, java.sql.Timestamp::valueOf);
        var sts1  = lts1 .map(java.sql.Time       .class, java.sql.Time::valueOf);
        var sds1  = lds1 .map(java.sql.Date       .class, java.sql.Date::valueOf);
        var r4dt1 = ldts1.map(TestTypes.R4DateTime.class, TestTypes.R4DateTime::new);

        var odts2 = ldts1.map(OffsetDateTime      .class, x -> x.atOffset(ZoneOffset.UTC));
        var ins2  = odts2.map(Instant             .class, OffsetDateTime::toInstant);
        var zdts2 = odts2.map(ZonedDateTime       .class, OffsetDateTime::toZonedDateTime);
        var gcs2  = zdts2.map(GregorianCalendar   .class, GregorianCalendar::from);
        var cs2   = gcs2 .map(Calendar            .class, gc -> gc);
        var uds2  = ins2 .map(java.util.Date      .class, java.util.Date::from);

        var ldts3 = lds1 .map(LocalDateTime       .class, x -> x.atTime(LocalTime.MIN));
        var odts3 = ldts3.map(OffsetDateTime      .class, x -> x.atOffset(ZoneOffset.UTC));
        var ins3  = odts3.map(Instant             .class, OffsetDateTime::toInstant);
        var zdts3 = odts3.map(ZonedDateTime       .class, OffsetDateTime::toZonedDateTime);
        var gcs3  = zdts3.map(GregorianCalendar   .class, GregorianCalendar::from);
        var cs3   = gcs3 .map(Calendar            .class, gc -> gc);
        var uds3  = ins3 .map(java.util.Date      .class, java.util.Date::from);
        var tss3  = ldts3.map(java.sql.Timestamp  .class, java.sql.Timestamp::valueOf);
        var r4dt3 = ldts3.map(TestTypes.R4DateTime.class, TestTypes.R4DateTime::new);

        var ots5  = lts1 .map(OffsetTime          .class, x -> x.atOffset(ZoneOffset.UTC));

        var all1 = List.of(odts1, lds1, ldts1, lts1, ots1, ins1, zdts1, gcs1, cs1, uds1, tss1, sts1, sds1, str1, r4str1A, r4str1B, r4str1C, r4str1D, r4str1E, r4str1F, r4dt1);
        var all2 = List.of(odts2, lds1, ldts1, lts1, ots5, ins2, zdts2, gcs2, cs2, uds2, tss1, sts1, sds1, str2, r4str2A, r4str2B, r4str2C, r4str2D, r4str2E, r4str2F, r4dt1);
        var all3 = List.of(odts3, lds1, ldts3,             ins3, zdts3, gcs3, cs3, uds3, tss3,       sds1, str3, r4str3A, r4str3B, r4str3C, r4str3D, r4str3E, r4str3F, r4dt3);
        var all4 = List.of(                    lts1, ots1,                                     sts1,       str4, r4str4A, r4str4B, r4str4C, r4str4D, r4str4E, r4str4F);
        var all5 = List.of(                    lts1, ots5,                                     sts1,       str5, r4str5A, r4str5B, r4str5C, r4str5D, r4str5E, r4str5F);

        var cvts  = TestTypes.CVT_CLASSES;
        var cvtsx = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var odtNode  = testIn(prefix1a, cvtsx, odts1, all1);
        var str1Node = testIn(prefix1b, cvts , str1 , all1);
        var ldtNode  = testIn(prefix2a, cvtsx, ldts1, all2);
        var str2Node = testIn(prefix2b, cvts , str2 , all2);
        var ldNode   = testIn(prefix3a, cvtsx, lds1 , all3);
        var str3Node = testIn(prefix3b, cvts , str3 , all3);
        var otNode   = testIn(prefix4a, cvtsx, ots1 , all4);
        var str4Node = testIn(prefix4b, cvts , str4 , all4);
        var ltNode   = testIn(prefix5a, cvtsx, lts1 , all5);
        var str5Node = testIn(prefix5b, cvts , str5 , all5);

        return List.of(odtNode, ldNode, ldtNode, ltNode, otNode, str1Node, str2Node, str3Node, str4Node, str5Node);
    }

    private Blob blob(String in) {
        return (Blob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Blob.class }, (i, m, a) -> {
            if (m.getName().equals("getBinaryStream")) return new ByteArrayInputStream(in.getBytes());
            throw new AssertionError(m.getName());
        });
    }

    private NClob nclob(String in) {
        return (NClob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { NClob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return new StringReader(in);
            throw new AssertionError(m.getName());
        });
    }

    private Clob clob(String in) {
        return (Clob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Clob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return new StringReader(in);
            throw new AssertionError(m.getName());
        });
    }

    private SQLXML sqlxml(String in) {
        return (SQLXML) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { SQLXML.class }, (i, m, a) -> {
            if (m.getName().equals("getString")) return in;
            throw new AssertionError(m.getName());
        });
    }

    @TestFactory
    public List<DynamicNode> testLobTypes() throws Exception {
        var prefix1 = "[testLobTypes - String]";
        var prefix2 = "[testLobTypes - byte[]]";
        var prefix3 = "[testLobTypes - Blob]";
        var prefix4 = "[testLobTypes - Clob]";
        var prefix5 = "[testLobTypes - NClob]";
        var prefix6 = "[testLobTypes - SQLXML]";
        var strs    = e(String.class, List.of("bla bla bla", "lorem ipsum dolor sit amet"));
        var bytes   = strs.map(byte[].class, String::getBytes);
        var chars   = strs.map(char[].class, String::toCharArray);
        var blobs   = strs.map(Blob  .class, this::blob);
        var clobs   = strs.map(Clob  .class, this::clob);
        var nclobs  = strs.map(NClob .class, this::nclob);
        var xmls    = strs.map(SQLXML.class, this::sqlxml);
        var r4bas   = bytes  .map(TestTypes.R4byteArray   .class, TestTypes.R4byteArray::new);
        var r4strsA = strs   .map(TestTypes.R4String      .class, TestTypes.R4String   ::new);
        var r4strsB = strs   .map(TestTypes.R4StringList  .class, x -> new TestTypes.R4StringList(List.of(x)));
        var r4strsC = r4strsB.map(TestTypes.R4Record      .class, TestTypes.R4Record   ::new);
        var r4strsD = strs   .map(TestTypes.R4StringArray .class, x -> new TestTypes.R4StringArray(new String[] {x}));
        var r4strsE = r4strsC.map(TestTypes.R4RecordDeep  .class, x -> new TestTypes.R4RecordDeep(List.of(x)));
        var r4strsF = r4strsE.map(TestTypes.R4RecordDeeper.class, x -> TestTypes.R4RecordDeeper.foo(List.of(x)));

        var all = List.of(strs, bytes, blobs, clobs, nclobs, xmls, chars, r4bas, r4strsA, r4strsB, r4strsC, r4strsD, r4strsE, r4strsF);

        var cvts = TestTypes.CVT_CLASSES_WITH_ARRAYS;

        var strNode   = testIn(prefix1, cvts, strs  , all);
        var bytsNode  = testIn(prefix2, cvts, bytes , all);
        var blobNode  = testIn(prefix3, cvts, blobs , all);
        var clobNode  = testIn(prefix4, cvts, clobs , all);
        var nclobNode = testIn(prefix5, cvts, nclobs, all);
        var xmlNode   = testIn(prefix6, cvts, xmls  , all);

        return List.of(strNode, bytsNode, blobNode, clobNode, nclobNode, xmlNode);
    }
}
