package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;
import module ninja.javahacker.annotimpler.magicfactory;

import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Named;

public class ConversionMappingTest {

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    private static Stream<Arguments> testConvertsMapping() {
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
                    Assertions.assertEquals(52, ((Number) c).intValue());
                };
                var name = "forValue((" + v.getClass().getSimpleName() + ") " + v + ", " + t.getSimpleName() + ")";
                execs.add(n(name, x));
            }
        }
        return execs.stream().map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "{0}")
    public void testConvertsMapping(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
