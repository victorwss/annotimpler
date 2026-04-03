package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

import org.junit.jupiter.api.function.Executable;

@Disabled
public class FloatValueConverterTest {

    private static final List<Class<?>>   IN_CLASSES   = List.of(
            BigDecimal.class, float.class, double.class, String.class
    );

    private static final List<Class<?>>   CVT_CLASSES  = List.of(
            float.class, double.class, Float.class, Double.class,
            BigDecimal.class, String.class, OptionalDouble.class
    );

    private static final List<BigDecimal> BIG_DECIMALS = Arrays.asList(bd(3.5), bd(0.078), bd(-156.77));
    private static final List<Float>      FLOATS       = Arrays.asList(   3.5F,    0.078F,    -156.77F);
    private static final List<Double>     DOUBLES      = Arrays.asList(   3.5D,    0.078D,    -156.77D);
    private static final List<String>     NUM_STRINGS  = Arrays.asList(  "3.5",   "0.078",   "-156.77");

    private static final List<OptionalDouble> OPT_DOUBLES = DOUBLES.stream().map(x -> x == null ? null : OptionalDouble.of(x)).toList();

    private static final Map<Class<?>, List<?>> MAPPINGS = Map.ofEntries(
            Map.entry(Float.class, FLOATS), Map.entry(Double.class, DOUBLES),
            Map.entry(BigDecimal.class, BIG_DECIMALS), Map.entry(String.class, NUM_STRINGS),
            Map.entry(OptionalDouble.class, OPT_DOUBLES)
    );

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
                List<DynamicNode> nodes3 = new ArrayList<>(FLOATS.size());
                for (var i = 0; i < FLOATS.size(); i++) {
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
