package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

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
            return new Elements<>(k2, data.stream().map(f).toList());
        }
    }

    public static <E> Elements<E> e(Class<E> k, List<E> data) {
        return new Elements<>(k, data);
    }

    private static final List<Class<?>> CVT_CLASSES = List.of(
            boolean.class, byte.class, short.class, int    .class, long.class, float.class, double.class,
            Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigDecimal.class, BigInteger.class, String.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
            Calendar.class, GregorianCalendar.class, java.util.Date.class, java.sql.Date.class, Time.class, java.sql.Timestamp.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, OffsetTime.class, Instant.class,
            Ref.class, RowId.class, Struct.class, java.sql.Array.class
    );

    private Long lo(BigInteger x) {
        if (x == null) return null;
        var max = BigInteger.valueOf(Long.MAX_VALUE);
        var min = BigInteger.valueOf(Long.MIN_VALUE);
        return x.compareTo(min) < 0 || x.compareTo(max) > 0 ? null : x.longValue();
    }

    private Integer i(Long x) {
        return x == null ? null : x < Integer.MIN_VALUE || x > Integer.MAX_VALUE ? null : x.intValue();
    }

    private Character c(Long x) {
        return x == null ? null : x < Character.MIN_VALUE || x > Character.MAX_VALUE ? null : (char) x.intValue();
    }

    private Short s(Long x) {
        return x == null ? null : x < Short.MIN_VALUE || x > Short.MAX_VALUE ? null : x.shortValue();
    }

    private Byte b(Long x) {
        return x == null ? null : x < Byte.MIN_VALUE || x > Byte.MAX_VALUE ? null : x.byteValue();
    }

    private Boolean o(Long x) {
        return x == null ? null : x == 0L ? Boolean.FALSE : x == 1L ? Boolean.TRUE : null;
    }

    private BigInteger bi(String x) {
        return x == null || x.contains(".") ? null : new BigInteger(x);
    }

    private BigDecimal bd(String x) {
        return x == null ? null : new BigDecimal(x);
    }

    private static Class<?> unoptionalize(Class<?> x) {
        return x == OptionalLong.class ? Long.class : x == OptionalInt.class ? Integer.class : x == OptionalDouble.class ? Double.class : x;
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private <E> DynamicNode testIn(Class<E> base, List<Elements<?>> lists, MethodSpec<E> m) throws Exception {
        var baseWrap = WrapperClass.wrap(base);
        Map<Class<?>, List<?>> mappings = new HashMap<>(CVT_CLASSES.size());
        for (var a1 : lists) {
            var k = a1.k();
            mappings.put(k, a1.data());
        }
        @SuppressWarnings("unchecked")
        var start = (List<E>) mappings.get(baseWrap);
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            var v1 = mappings.get(WrapperClass.wrap(k1));
            var cvt = ConverterFactory.STD.get(k1);
            List<DynamicNode> nodes2 = new ArrayList<>(start.size());
            for (var i = 0; i < start.size(); i++) {
                var in = start.get(i);
                if (in == null) continue;
                var out = v1 == null || i >= v1.size() ? null : v1.get(i);
                var k1s = unoptionalize(k1);
                Giver2 ok = exec0 -> {
                        var g = exec0.give().get();
                        Assertions.assertEquals(out, g);
                };
                Giver2 err1 = exec0 -> {
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> exec0.give());
                        Assertions.assertAll(
                                () -> Assertions.assertEquals("Can't read value as " + k1s.getSimpleName() + ".", ce.getMessage()),
                                () -> Assertions.assertEquals(base, ce.getIn()),
                                () -> Assertions.assertEquals(k1s, ce.getOut())
                        );
                };
                Giver2 err2 = exec0 -> {
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> exec0.give());
                        Assertions.assertAll(
                                () -> Assertions.assertEquals("Unsupported " + base.getSimpleName(), ce.getMessage()),
                                () -> Assertions.assertEquals(base, ce.getIn()),
                                () -> Assertions.assertEquals(k1s, ce.getOut())
                        );
                };
                var exec = v1 == null ? err2 : out == null ? err1 : ok;
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + k1.getSimpleName() + " from " + base.getSimpleName() + " - " + in + ".",
                        () -> exec.receive(() -> m.receive(cvt, in))
                );
                var nd2 = DynamicTest.dynamicTest(
                        "Converter for " + k1.getSimpleName() + " fromObj " + base.getSimpleName() + " - " + in + ".",
                        () -> exec.receive(() -> cvt.fromObj(in))
                );
                nodes2.add(nd1);
                nodes2.add(nd2);
            }
            nodes1.add(DynamicContainer.dynamicContainer("Test convertions for " + k1.getSimpleName() + " from " + base.getSimpleName() + ".", nodes2));
        }
        return DynamicContainer.dynamicContainer("Test convertions from " + base.getSimpleName() + ".", nodes1);
    }

    @TestFactory
    public List<DynamicNode> testNumericTypes() throws Exception {
        var str1 = e(String.class, List.of(
                "0", "1", "42", "55", "127", "-30", "-128", "32000", "64000",
                "489876544", "12345678910", "9876543210987654", "98765432109876543210",
                "3.5", "0.078", "-177.77", "98765432109876543210.98765432"
        ));
        var floats = e(Float.class, Arrays.asList(
                0F, 1F, 42F, 55F, 127F, -30F, -128F, 32000F, 64000F,
                489876544F, null, null, null,
                3.5F, 0.078F, -177.77F, null
        ));
        var doubles = e(Double.class, Arrays.asList(
                0D, 1D, 42D, 55D, 127D, -30D, -128D, 32000D, 64000D,
                489876544D, 12345678910D, 9876543210987654D, null,
                3.5D, 0.078D, -177.77D, null
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

        var byteNode   = testIn(byte      .class, all , (cvt, in) -> cvt.from(in));
        var intNode    = testIn(int       .class, all , (cvt, in) -> cvt.from(in));
        var longNode   = testIn(long      .class, all , (cvt, in) -> cvt.from(in));
        var shortNode  = testIn(short     .class, all , (cvt, in) -> cvt.from(in));
        var floatNode  = testIn(float     .class, all , (cvt, in) -> cvt.from(in));
        var doubleNode = testIn(double    .class, all , (cvt, in) -> cvt.from(in));
        var bigdNode   = testIn(BigDecimal.class, all , (cvt, in) -> cvt.from(in));
        var boolNode   = testIn(boolean   .class, allB, (cvt, in) -> cvt.from(in));

        return List.of(byteNode, intNode, longNode, shortNode, floatNode, doubleNode, bigdNode, boolNode);
    }
}
