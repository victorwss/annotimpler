package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class HeavyConverterTest {

    private static final Type COLLECTION_STRING;
    private static final Type LIST_STRING;
    private static final Type SET_STRING;
    private static final Type OPTIONAL_STRING;
    private static final Type POINTLESS;
    private static final Type MAP_STRING_STRING;

    static {
        try {
            var mtd = HeavyConverterTest.class.getDeclaredMethod("noop", Collection.class, List.class, Set.class, Optional.class, Pointless.class, Map.class);
            COLLECTION_STRING = mtd.getParameters()[0].getParameterizedType();
            LIST_STRING = mtd.getParameters()[1].getParameterizedType();
            SET_STRING = mtd.getParameters()[2].getParameterizedType();
            OPTIONAL_STRING = mtd.getParameters()[3].getParameterizedType();
            POINTLESS = mtd.getParameters()[4].getParameterizedType();
            MAP_STRING_STRING = mtd.getParameters()[5].getParameterizedType();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static interface Pointless<X> extends List<X> {}

    private static void noop(Collection<String> a, List<String> b, Set<String> c, Optional<String> d, Pointless<String> e, Map<String, String> g) {
        throw new AssertionError();
    }

    private static String name(Type t) {
        if (t instanceof Class<?> k) return k.getSimpleName();
        if (t instanceof ParameterizedType p) return ((Class<?>) p.getRawType()).getSimpleName() + "<String>";
        throw new AssertionError();
    }

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
            return new Elements<>(k2, data.stream().map(f).toList());
        }
    }

    public static <E> Elements<E> e(Class<E> k, List<E> data) {
        return new Elements<>(k, data);
    }

    private static final List<Type> CVT_CLASSES = List.of(
            boolean.class, byte.class, short.class, int    .class, long.class, float.class, double.class,
            Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigDecimal.class, BigInteger.class, String.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
            Calendar.class, GregorianCalendar.class, java.util.Date.class, java.sql.Date.class, Time.class, java.sql.Timestamp.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, OffsetTime.class, Instant.class,
            Ref.class, RowId.class, Struct.class, java.sql.Array.class,
            byte[].class, LocalDate[].class, COLLECTION_STRING, LIST_STRING, SET_STRING, OPTIONAL_STRING
    );

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

    private Boolean o(Long x) {
        return x == null ? null : x == 0L ? Boolean.FALSE : x == 1L ? Boolean.TRUE : null;
    }

    private BigInteger bi(String x) {
        return x == null || List.of("NaN", "Infinity", "-Infinity").contains(x) || x.contains(".") ? null : new BigInteger(x);
    }

    private BigDecimal bd(String x) {
        return x == null || List.of("NaN", "Infinity", "-Infinity").contains(x) ? null : new BigDecimal(x);
    }

    private OffsetDateTime odt(String x) {
        return x == null ? null : OffsetDateTime.parse(x, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][ xxxxx]").withResolverStyle(ResolverStyle.STRICT));
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private <E> DynamicNode testIn(Class<E> base, List<? extends Elements<?>> lists, MethodSpec<E> m) throws Exception {
        return testIn2(base, lists, m);
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private <E> DynamicNode testIn2(Type base, List<? extends Elements<?>> lists, MethodSpec<E> m) throws Exception {
        var baseWrap = base instanceof Class<?> b2 ? WrapperClass.wrap(b2) : base;
        Map<Type, List<?>> mappings = new HashMap<>(CVT_CLASSES.size());
        for (var a1 : lists) {
            var k = a1.k();
            mappings.put(k, a1.data());
        }
        @SuppressWarnings("unchecked")
        var start = (List<E>) mappings.get(baseWrap);
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            if (k1 == null) throw new AssertionError();
            var v1 = mappings.get(k1 instanceof Class<?> k2 ? WrapperClass.wrap(k2) : k1);
            var cvt = ConverterFactory.STD.get(k1);
            List<DynamicNode> nodes2 = new ArrayList<>(start.size());
            for (var i = 0; i < start.size(); i++) {
                var in = start.get(i);
                if (in == null) continue;
                var out = v1 == null || i >= v1.size() ? null : v1.get(i);
                Giver2 ok = exec0 -> {
                        var g = exec0.give().get();
                        if (g instanceof byte[] && out instanceof byte[]) {
                            Assertions.assertArrayEquals((byte[]) out, (byte[]) g);
                        } else {
                            Assertions.assertEquals(out, g);
                        }
                };
                Giver2 err1 = exec0 -> {
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> exec0.give());
                        Assertions.assertAll(
                                () -> Assertions.assertEquals("Can't read value as " + name(k1) + ".", ce.getMessage()),
                                () -> Assertions.assertEquals(base, ce.getIn()),
                                () -> Assertions.assertEquals(k1, ce.getOut())
                        );
                };
                Giver2 err2 = exec0 -> {
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> exec0.give());
                        Assertions.assertAll(
                                () -> Assertions.assertEquals("Unsupported " + name(base) + ".", ce.getMessage()),
                                () -> Assertions.assertEquals(base, ce.getIn()),
                                () -> Assertions.assertEquals(k1, ce.getOut())
                        );
                };
                var exec = v1 == null && (base != String.class || List.of(Struct.class, RowId.class, Ref.class, java.sql.Array.class, COLLECTION_STRING).contains(k1))
                        ? err2 : out == null
                        ? err1 : ok;
                var inStr = in instanceof Blob ? "<Blob>"
                        : in instanceof NClob ? "<NClob>"
                        : in instanceof Clob ? "<Clob>"
                        : in instanceof SQLXML ? "<SQLXML>"
                        : in instanceof RowId ? "<RowId>"
                        : in instanceof byte[] x ? "(byte[]) " + new String(x)
                        : in instanceof Object[] x ? "<Array>"
                        : "" + in;
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + name(k1) + " from " + name(base) + " - " + inStr + ".",
                        () -> exec.receive(() -> m.receive(cvt, in))
                );
                var nd2 = DynamicTest.dynamicTest(
                        "Converter for " + name(k1) + " fromObj " + name(base) + " - " + inStr + ".",
                        () -> exec.receive(() -> cvt.fromObj(in))
                );
                nodes2.add(nd1);
                nodes2.add(nd2);
            }
            nodes1.add(DynamicContainer.dynamicContainer("Test convertions for " + name(k1) + " from " + name(base) + ".", nodes2));
        }
        return DynamicContainer.dynamicContainer("Test convertions from " + name(base) + ".", nodes1);
    }

    @TestFactory
    public List<DynamicNode> testNumericTypes() throws Exception {
        var str1 = e(String.class, List.of(
                "0", "1", "42", "55", "127", "-30", "-128", "32000", "64000",
                "489876544", "12345678910", "9876543210987654", "98765432109876543210",
                "16777217", "9007199254740993",
                "3.5", "0.078", "-177.77", "98765432109876543210.98765432",
                "NaN", "Infinity", "-Infinity"
        ));
        var floats = e(Float.class, Arrays.asList(
                0F, 1F, 42F, 55F, 127F, -30F, -128F, 32000F, 64000F,
                489876544F, null, null, null,
                null, null,
                3.5F, 0.078F, -177.77F, null,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
        ));
        var doubles = e(Double.class, Arrays.asList(
                0D, 1D, 42D, 55D, 127D, -30D, -128D, 32000D, 64000D,
                489876544D, 12345678910D, 9876543210987654D, null,
                16777217.0D, null,
                3.5D, 0.078D, -177.77D, null,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        ));
        var strB = e(String.class, List.of("false", "true"));

        var bigds      = str1 .map(BigDecimal.class, this::bd);
        var bigis      = str1 .map(BigInteger.class, this::bi);
        var longs      = bigis.map(Long      .class, this::lo);
        var bools      = longs.map(Boolean   .class, this::o );
        var bytes      = longs.map(Byte      .class, this::b );
        var chars      = longs.map(Character .class, this::c );
        var shorts     = longs.map(Short     .class, this::s );
        var ints       = longs.map(Integer   .class, this::i );
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));

        var all  = List.of(bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, str1);
        var allB = List.of(bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, strB);
        var allC = List.of(       bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, str1);
        var allD = List.of(bools,                                                                                                  strB);

        var byteNode   = testIn(byte      .class, all , (cvt, in) -> cvt.from(in));
        var intNode    = testIn(int       .class, all , (cvt, in) -> cvt.from(in));
        var longNode   = testIn(long      .class, all , (cvt, in) -> cvt.from(in));
        var shortNode  = testIn(short     .class, all , (cvt, in) -> cvt.from(in));
        var floatNode  = testIn(float     .class, all , (cvt, in) -> cvt.from(in));
        var doubleNode = testIn(double    .class, all , (cvt, in) -> cvt.from(in));
        var bigdNode   = testIn(BigDecimal.class, all , (cvt, in) -> cvt.from(in));
        var boolNode   = testIn(boolean   .class, allB, (cvt, in) -> cvt.from(in));
        var str1Node   = testIn(String    .class, allC, (cvt, in) -> cvt.from(in));
        var str2Node   = testIn(String    .class, allD, (cvt, in) -> cvt.from(in));

        return List.of(byteNode, intNode, longNode, shortNode, floatNode, doubleNode, bigdNode, boolNode, str1Node, str2Node);
    }

    @TestFactory
    public List<DynamicNode> testMinusZero() throws Exception {
        var str1 = e(String.class, List.of("-0"));
        var floats = e(Float.class, Arrays.asList(-0.0F));
        var doubles = e(Double.class, Arrays.asList(-0.0));

        var bigds      = str1 .map(BigDecimal.class, this::bd);
        var bigis      = str1 .map(BigInteger.class, this::bi);
        var longs      = bigis.map(Long      .class, this::lo);
        var bools      = longs.map(Boolean   .class, this::o );
        var bytes      = longs.map(Byte      .class, this::b );
        var chars      = longs.map(Character .class, this::c );
        var shorts     = longs.map(Short     .class, this::s );
        var ints       = longs.map(Integer   .class, this::i );
        var optDoubles = doubles.map(OptionalDouble.class, x -> x == null ? null : OptionalDouble.of(x));
        var optInts    = ints   .map(OptionalInt   .class, x -> x == null ? null : OptionalInt   .of(x));
        var optLongs   = longs  .map(OptionalLong  .class, x -> x == null ? null : OptionalLong  .of(x));

        var all1 = List.of(bools, bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, str1);
        var all2 = List.of(       bytes, shorts, chars, ints, longs, floats, doubles, bigis, bigds, optInts, optLongs, optDoubles, str1);

        var floatNode  = testIn(float .class, all1, (cvt, in) -> cvt.from(in));
        var doubleNode = testIn(double.class, all1, (cvt, in) -> cvt.from(in));
        var strNode    = testIn(String.class, all2, (cvt, in) -> cvt.from(in));

        return List.of(floatNode, doubleNode, strNode);
    }

    @TestFactory
    public List<DynamicNode> testTemporalTypes() throws Exception {
        var str1 = e(String.class, List.of(
                "2026-01-02 03:04:05 +06:13",
                "2025-10-31 13:14:15.123456 +11:55:44",
                "2022-04-12 08:07:06.123456789 -03:04:05",
                "2023-07-14 11:12:13.123 -00:12",
                "2021-10-04 13:14:15.12 -12:20",
                "2022-09-14 21:10:12.1 -04:10"
        ));
        var str2 = str1.map(String.class, x -> x.split(" ")[0] + " " + x.split(" ")[1]);
        var str3 = str1.map(String.class, x -> x.split(" ")[0]);
        var str4 = str1.map(String.class, x -> x.split(" ")[1] + " " + x.split(" ")[2]);
        var str5 = str1.map(String.class, x -> x.split(" ")[1]);

        var odts1 = str1 .map(OffsetDateTime    .class, this::odt);
        var lds1  = odts1.map(LocalDate         .class, OffsetDateTime::toLocalDate);
        var ldts1 = odts1.map(LocalDateTime     .class, OffsetDateTime::toLocalDateTime);
        var lts1  = odts1.map(LocalTime         .class, OffsetDateTime::toLocalTime);
        var ots1  = odts1.map(OffsetTime        .class, OffsetDateTime::toOffsetTime);
        var ins1  = odts1.map(Instant           .class, OffsetDateTime::toInstant);
        var zdts1 = odts1.map(ZonedDateTime     .class, OffsetDateTime::toZonedDateTime);
        var gcs1  = zdts1.map(GregorianCalendar .class, GregorianCalendar::from);
        var cs1   = gcs1 .map(Calendar          .class, gc -> gc);
        var uds1  = ins1 .map(java.util.Date    .class, java.util.Date::from);
        var tss1  = ldts1.map(java.sql.Timestamp.class, java.sql.Timestamp::valueOf);
        var sts1  = lts1 .map(java.sql.Time     .class, java.sql.Time::valueOf);
        var sds1  = lds1 .map(java.sql.Date     .class, java.sql.Date::valueOf);

        var odts2 = ldts1.map(OffsetDateTime    .class, x -> x.atOffset(ZoneOffset.UTC));
        var ins2  = odts2.map(Instant           .class, OffsetDateTime::toInstant);
        var zdts2 = odts2.map(ZonedDateTime     .class, OffsetDateTime::toZonedDateTime);
        var gcs2  = zdts2.map(GregorianCalendar .class, GregorianCalendar::from);
        var cs2   = gcs2 .map(Calendar          .class, gc -> gc);
        var uds2  = ins2 .map(java.util.Date    .class, java.util.Date::from);

        var ldts3 = lds1 .map(LocalDateTime     .class, x -> x.atTime(LocalTime.MIN));
        var odts3 = ldts3.map(OffsetDateTime    .class, x -> x.atOffset(ZoneOffset.UTC));
        var ins3  = odts3.map(Instant           .class, OffsetDateTime::toInstant);
        var zdts3 = odts3.map(ZonedDateTime     .class, OffsetDateTime::toZonedDateTime);
        var gcs3  = zdts3.map(GregorianCalendar .class, GregorianCalendar::from);
        var cs3   = gcs3 .map(Calendar          .class, gc -> gc);
        var uds3  = ins3 .map(java.util.Date    .class, java.util.Date::from);
        var tss3  = ldts3.map(java.sql.Timestamp.class, java.sql.Timestamp::valueOf);

        var ots5  = lts1 .map(OffsetTime        .class, x -> x.atOffset(ZoneOffset.UTC));

        var all1 = List.of(odts1, lds1, ldts1, lts1, ots1, ins1, zdts1, gcs1, cs1, uds1, tss1, sts1, sds1, str1);
        var all2 = List.of(odts2, lds1, ldts1, lts1, ots5, ins2, zdts2, gcs2, cs2, uds2, tss1, sts1, sds1, str2);
        var all3 = List.of(odts3, lds1, ldts3,             ins3, zdts3, gcs3, cs3, uds3, tss3,       sds1, str3);
        var all4 = List.of(                    lts1, ots1,                                     sts1,       str4);
        var all5 = List.of(                    lts1, ots5,                                     sts1,       str5);

        var odtNode  = testIn(OffsetDateTime.class, all1, (cvt, in) -> cvt.from(in));
        var str1Node = testIn(String        .class, all1, (cvt, in) -> cvt.from(in));
        var ldtNode  = testIn(LocalDateTime .class, all2, (cvt, in) -> cvt.from(in));
        var str2Node = testIn(String        .class, all2, (cvt, in) -> cvt.from(in));
        var ldNode   = testIn(LocalDate     .class, all3, (cvt, in) -> cvt.from(in));
        var str3Node = testIn(String        .class, all3, (cvt, in) -> cvt.from(in));
        var otNode   = testIn(OffsetTime    .class, all4, (cvt, in) -> cvt.from(in));
        var str4Node = testIn(String        .class, all4, (cvt, in) -> cvt.from(in));
        var ltNode   = testIn(LocalTime     .class, all5, (cvt, in) -> cvt.from(in));
        var str5Node = testIn(String        .class, all5, (cvt, in) -> cvt.from(in));

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
        var str1   = e(String.class, List.of("bla bla bla", "blu blu blu", "lorem ipsum dolor sit amet"));
        var bytes  = str1.map(byte[].class, String::getBytes);
        var blobs  = str1.map(Blob  .class, this::blob);
        var clobs  = str1.map(Clob  .class, this::clob);
        var nclobs = str1.map(NClob .class, this::nclob);
        var xmls   = str1.map(SQLXML.class, this::sqlxml);

        var all = List.of(str1, bytes, blobs, clobs, nclobs, xmls);

        var strNode   = testIn(String.class, all, (cvt, in) -> cvt.from(in));
        var bytsNode  = testIn(byte[].class, all, (cvt, in) -> cvt.from(in));
        var blobNode  = testIn(Blob  .class, all, (cvt, in) -> cvt.from(in));
        var clobNode  = testIn(Clob  .class, all, (cvt, in) -> cvt.from(in));
        var nclobNode = testIn(NClob .class, all, (cvt, in) -> cvt.from(in));
        var xmlNode   = testIn(SQLXML.class, all, (cvt, in) -> cvt.from(in));

        return List.of(strNode, bytsNode, blobNode, clobNode, nclobNode, xmlNode);
    }
}
