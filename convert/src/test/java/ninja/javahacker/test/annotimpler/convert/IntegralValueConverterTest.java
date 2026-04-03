package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

import org.junit.jupiter.api.function.Executable;

@Disabled
public class IntegralValueConverterTest {

    private static final List<Class<?>>   IN_CLASSES   = List.of(
            byte.class, short.class, int.class, long.class, BigDecimal.class, float.class, double.class, String.class
    );

    private static final List<Class<?>>   CVT_CLASSES  = List.of(
            byte.class, short.class, int    .class, long.class, float.class, double.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigDecimal.class, BigInteger.class, String.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class
    );

    private static final List<Byte>       BYTES        = Arrays.asList( b(0),  b(1),  b(42),  b(55),  b(127),  b(-30),      null,      null,        null,         null,        null,                     null,                      null);
    private static final List<Short>      SHORTS       = Arrays.asList( s(0),  s(1),  s(42),  s(55),  s(127),  s(-30),  s(32000),      null,        null,         null,        null,                     null,                      null);
    private static final List<Character>  CHARS        = Arrays.asList( c(0),  c(1),  c(42),  c(55),  c(127),    null,  c(32000),  c(65000),        null,         null,        null,                     null,                      null);
    private static final List<Integer>    INTS         = Arrays.asList(   0 ,    1 ,    42 ,    55 ,    127 ,    -30 ,    32000 ,    65000 ,    9876543 ,    -9999999 ,        null,                     null,                      null);
    private static final List<Long>       LONGS        = Arrays.asList(   0L,    1L,    42L,    55L,    127L,    -30L,    32000L,    65000L,    9876543L,    -9999999L,        null,                     null,                      null);
    private static final List<BigInteger> BIG_INTEGERS = Arrays.asList(bi(0), bi(1), bi(42), bi(55), bi(127), bi(-30), bi(32000), bi(65000), bi(9876543), bi(-9999999),        null,                     null,                      null);
    private static final List<BigDecimal> BIG_DECIMALS = Arrays.asList(bd(0), bd(1), bd(42), bd(55), bd(127), bd(-30), bd(32000), bd(65000), bd(9876543), bd(-9999999),        null,                     null,                      null);
    private static final List<Float>      FLOATS       = Arrays.asList(   0F,    1F,    42F,    55F,    127F,    -30F,    32000F,    65000F,    9876543F,    -9999999F, Float .NaN, Float .POSITIVE_INFINITY , Float .NEGATIVE_INFINITY );
    private static final List<Double>     DOUBLES      = Arrays.asList(   0D,    1D,    42D,    55D,    127D,    -30D,    32000D,    65000D,    9876543D,    -9999999D, Double.NaN, Double.POSITIVE_INFINITY , Double.NEGATIVE_INFINITY );
    private static final List<String>     NUM_STRINGS  = Arrays.asList(  "0",   "1",   "42",   "55",   "127",   "-30",   "32000",   "65000",   "9876543",   "-9999999",       "NaN",               "Infinity",               "-Infinity");

    private static final List<OptionalDouble> OPT_DOUBLES = DOUBLES.stream().map(x -> x == null ? null : OptionalDouble.of(x)).toList();
    private static final List<OptionalInt>    OPT_INTS    = INTS   .stream().map(x -> x == null ? null : OptionalInt   .of(x)).toList();
    private static final List<OptionalLong>   OPT_LONGS   = LONGS  .stream().map(x -> x == null ? null : OptionalLong  .of(x)).toList();

    private static final Map<Class<?>, List<?>> MAPPINGS = Map.ofEntries(
            Map.entry(Byte.class, BYTES), Map.entry(Short.class, SHORTS), Map.entry(Character.class, CHARS),
            Map.entry(Integer.class, INTS), Map.entry(Long.class, LONGS),
            Map.entry(Float.class, FLOATS), Map.entry(Double.class, DOUBLES),
            Map.entry(BigInteger.class, BIG_INTEGERS), Map.entry(BigDecimal.class, BIG_DECIMALS), Map.entry(String.class, NUM_STRINGS),
            Map.entry(OptionalDouble.class, OPT_DOUBLES), Map.entry(OptionalInt.class, OPT_INTS), Map.entry(OptionalLong.class, OPT_LONGS)
    );

    private static char c(int x) {
        return (char) x;
    }

    private static short s(int x) {
        return (short) x;
    }

    private static byte b(int x) {
        return (byte) x;
    }

    private static BigInteger bi(int x) {
        return BigInteger.valueOf(x);
    }

    private static BigDecimal bd(double x) {
        return new BigDecimal(x);
    }

    private static record Val(Class<?> c, Object v) {}

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public List<DynamicNode> testSimpleValueMapping() throws Exception {
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            var k1w = WrapperClass.wrap(k1);
            var v1 = MAPPINGS.get(k1w);
            var cvt = ConverterFactory.STD.get(k1);
            List<DynamicNode> nodes2 = new ArrayList<>(IN_CLASSES.size());
            for (var k2 : IN_CLASSES) {
                var v2 = MAPPINGS.get(WrapperClass.wrap(k2));
                List<DynamicNode> nodes3 = new ArrayList<>(BYTES.size());
                for (var i = 0; i < BYTES.size(); i++) {
                    var in = v2.get(i);
                    if (in == null) continue;
                    var out = v1.get(i);
                    var k1s = k1 == OptionalLong.class ? Long.class : k1 == OptionalInt.class ? Integer.class : k1 == OptionalDouble.class ? Double.class : k1;
                    Executable exec1 = () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.fromObj(in));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Can't read value as " + k1s.getSimpleName() + ".", ce.getMessage()),
                                    () -> Assertions.assertEquals(k2, ce.getIn()),
                                    () -> Assertions.assertEquals(k1s, ce.getOut())
                            );
                    };
                    Executable exec2 = () -> {
                            var g = cvt.fromObj(in).get();
                            Assertions.assertEquals(new Val(k1w, out), new Val(g.getClass(), g));
                    };
                    var nd = DynamicTest.dynamicTest(
                            "Converter for " + k1.getSimpleName() + " from " + k2.getSimpleName() + " - " + in,
                            out == null ? exec1 : exec2
                    );
                    nodes3.add(nd);
                }
                nodes2.add(DynamicContainer.dynamicContainer("Test convertions for " + k1.getSimpleName() + " from " + k2.getSimpleName(), nodes3));
            }
            nodes1.add(DynamicContainer.dynamicContainer("Test convertions for " + k1.getSimpleName() + ".", nodes2));
        }
        return nodes1;
    }
}
