package ninja.javahacker.test.annotimpler.sql;

import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings("unused")
public class SqlNamedParameterTest {

    private static final Method TEST_METHOD_1 = find("testMethod1");
    private static final Method TEST_METHOD_2 = find("testMethod2");
    private static final Method TEST_METHOD_3 = find("testMethod3");
    private static final Method TEST_METHOD_4 = find("testMethod4");
    private static final Method BAD_METHOD_1 = find("badMethod1");
    private static final Method BAD_METHOD_2 = find("badMethod2");
    private static final Method BAD_METHOD_3 = find("badMethod3");
    private static final Method BAD_METHOD_4 = find("badMethod4");

    private static Method find(String name) {
        return Stream.of(SqlNamedParameterTest.class.getDeclaredMethods()).filter(m -> m.getName().equals(name)).findFirst().orElseThrow();
    }

    private void testMethod1(String x, @Flat Integer y, double z) {
        throw new AssertionError();
    }

    private void testMethod2(Optional<BigDecimal> a, OptionalInt b) {
        throw new AssertionError();
    }

    private static enum Foo {
        RED, YELLOW, BLUE;
    }

    private static enum Bar {
        CAT, DOG, FISH;
    }

    private static record FooRecord(int foo1, String foo2, Optional<String> foo3) {
    }

    private static record BarRecord(FooRecord foo4, Optional<FooRecord> foo5, BigDecimal foo6) {
    }

    private void testMethod3(
            boolean a, Boolean b, byte c, Byte d, short e, Short f, char g, Character h,
            int i, Integer j, long k, Long l, float m, Float n, double o, Double p,
            BigInteger q, BigDecimal r, OptionalInt s, OptionalLong t, OptionalDouble u,
            String v, LocalDate w, LocalTime x, LocalDateTime y,
            Optional<BigInteger> z
    ) {
        throw new AssertionError();
    }

    private void testMethod4(Foo a, Bar b, FooRecord c, BarRecord d, Optional<Foo> e, Optional<BarRecord> f) {
        throw new AssertionError();
    }

    private void badMethod1(Thread x) {
        throw new AssertionError();
    }

    private void badMethod2(Optional<Thread> x) {
        throw new AssertionError();
    }

    private void badMethod3(int a, List<Boolean> x, String b) {
        throw new AssertionError();
    }

    private void badMethod4(String... x) {
        throw new AssertionError();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleSqlNamedParameterProperties() throws Exception {
        var five = new BigDecimal(5);
        var s1 = SqlNamedParameter.forMethod(TEST_METHOD_1);
        var x0 = (SqlNamedParameter<String>) s1.get(0);
        var y0 = (SqlNamedParameter<Integer>) s1.get(1);
        var z0 = (SqlNamedParameter<Double>) s1.get(2);
        var s2 = SqlNamedParameter.forMethod(TEST_METHOD_2);
        var optBd = TEST_METHOD_2.getParameters()[0].getParameterizedType();
        var a0 = (SqlNamedParameter<Optional<BigDecimal>>) s2.get(0);
        var b0 = (SqlNamedParameter<OptionalInt>) s2.get(1);
        var x1 = x0.withValue("a");
        var x2 = x0.withValue("");
        var x3 = x0.withValue("whoa");
        var x4 = x0.withValue(null);
        var y1 = y0.withValue(42);
        var y2 = y0.withValue(73);
        var y3 = y0.withValue(null);
        var z1 = z0.withValue(55.0);
        var z2 = z0.withValue(-787.75);
        var z3 = z0.withValue(Double.NaN);
        var a1 = a0.withValue(Optional.of(five));
        var a2 = a0.withValue(Optional.empty());
        var a3 = a0.withValue(null);
        var b1 = b0.withValue(OptionalInt.of(6));
        var b2 = b0.withValue(OptionalInt.empty());
        var b3 = b0.withValue(null);

        Assertions.assertAll(
                () -> Assertions.assertEquals(String.class, x0.getType()),
                () -> Assertions.assertFalse(x0.isFlat()),
                () -> Assertions.assertEquals("x", x0.getName()),
                () -> Assertions.assertEquals(0, x0.getIndex()),
                () -> Assertions.assertTrue(x0.accept(null)),
                () -> Assertions.assertTrue(x0.accept("")),
                () -> Assertions.assertTrue(x0.accept("abc")),
                () -> Assertions.assertFalse(x0.accept(this)),
                () -> Assertions.assertFalse(x0.accept(42)),
                () -> Assertions.assertFalse(x0.accept(55.0)),
                () -> Assertions.assertFalse(x0.accept(five)),
                () -> Assertions.assertFalse(x0.accept(Optional.of(five))),
                () -> Assertions.assertFalse(x0.accept(Optional.empty())),
                () -> Assertions.assertFalse(x0.accept(OptionalInt.of(6))),
                () -> Assertions.assertFalse(x0.accept(OptionalInt.empty())),
                () -> Assertions.assertFalse(x0.accept(Thread.currentThread())),

                () -> Assertions.assertEquals(Integer.class, y0.getType()),
                () -> Assertions.assertTrue(y0.isFlat()),
                () -> Assertions.assertEquals("y", y0.getName()),
                () -> Assertions.assertEquals(1, y0.getIndex()),
                () -> Assertions.assertTrue(y0.accept(null)),
                () -> Assertions.assertFalse(y0.accept("")),
                () -> Assertions.assertFalse(y0.accept("abc")),
                () -> Assertions.assertFalse(y0.accept(this)),
                () -> Assertions.assertTrue(y0.accept(42)),
                () -> Assertions.assertFalse(y0.accept(55.0)),
                () -> Assertions.assertFalse(y0.accept(five)),
                () -> Assertions.assertFalse(y0.accept(Optional.of(five))),
                () -> Assertions.assertFalse(y0.accept(Optional.empty())),
                () -> Assertions.assertFalse(y0.accept(OptionalInt.of(6))),
                () -> Assertions.assertFalse(y0.accept(OptionalInt.empty())),
                () -> Assertions.assertFalse(y0.accept(Thread.currentThread())),

                () -> Assertions.assertEquals(double.class, z0.getType()),
                () -> Assertions.assertFalse(z0.isFlat()),
                () -> Assertions.assertEquals("z", z0.getName()),
                () -> Assertions.assertEquals(2, z0.getIndex()),
                () -> Assertions.assertFalse(z0.accept(null)),
                () -> Assertions.assertFalse(z0.accept("")),
                () -> Assertions.assertFalse(z0.accept("abc")),
                () -> Assertions.assertFalse(z0.accept(this)),
                () -> Assertions.assertFalse(z0.accept(42)),
                () -> Assertions.assertTrue(z0.accept(55.0)),
                () -> Assertions.assertFalse(z0.accept(five)),
                () -> Assertions.assertFalse(z0.accept(Optional.of(five))),
                () -> Assertions.assertFalse(z0.accept(Optional.empty())),
                () -> Assertions.assertFalse(z0.accept(OptionalInt.of(6))),
                () -> Assertions.assertFalse(z0.accept(OptionalInt.empty())),
                () -> Assertions.assertFalse(z0.accept(Thread.currentThread())),

                () -> Assertions.assertEquals(optBd, a0.getType()),
                () -> Assertions.assertFalse(a0.isFlat()),
                () -> Assertions.assertEquals("a", a0.getName()),
                () -> Assertions.assertEquals(0, a0.getIndex()),
                () -> Assertions.assertTrue(a0.accept(null)),
                () -> Assertions.assertFalse(a0.accept("")),
                () -> Assertions.assertFalse(a0.accept("abc")),
                () -> Assertions.assertFalse(a0.accept(this)),
                () -> Assertions.assertFalse(a0.accept(42)),
                () -> Assertions.assertFalse(a0.accept(55.0)),
                () -> Assertions.assertFalse(a0.accept(five)),
                () -> Assertions.assertTrue(a0.accept(Optional.of(five))),
                () -> Assertions.assertTrue(a0.accept(Optional.empty())),
                () -> Assertions.assertFalse(a0.accept(OptionalInt.of(6))),
                () -> Assertions.assertFalse(a0.accept(OptionalInt.empty())),
                () -> Assertions.assertFalse(a0.accept(Thread.currentThread())),

                () -> Assertions.assertEquals(OptionalInt.class, b0.getType()),
                () -> Assertions.assertFalse(b0.isFlat()),
                () -> Assertions.assertEquals("b", b0.getName()),
                () -> Assertions.assertEquals(1, b0.getIndex()),
                () -> Assertions.assertTrue(b0.accept(null)),
                () -> Assertions.assertFalse(b0.accept("")),
                () -> Assertions.assertFalse(b0.accept("abc")),
                () -> Assertions.assertFalse(b0.accept(this)),
                () -> Assertions.assertFalse(b0.accept(42)),
                () -> Assertions.assertFalse(b0.accept(55.0)),
                () -> Assertions.assertFalse(b0.accept(five)),
                () -> Assertions.assertFalse(b0.accept(Optional.of(five))),
                () -> Assertions.assertFalse(b0.accept(Optional.empty())),
                () -> Assertions.assertTrue(b0.accept(OptionalInt.of(6))),
                () -> Assertions.assertTrue(b0.accept(OptionalInt.empty())),
                () -> Assertions.assertFalse(b0.accept(Thread.currentThread())),

                () -> Assertions.assertEquals(String.class, x1.getType()),
                () -> Assertions.assertFalse(x1.isFlat()),
                () -> Assertions.assertEquals("x", x1.getName()),
                () -> Assertions.assertEquals(0, x1.getIndex()),
                () -> Assertions.assertEquals("a", x1.getValue()),
                () -> Assertions.assertEquals("", x2.getValue()),
                () -> Assertions.assertEquals("whoa", x3.getValue()),
                () -> Assertions.assertEquals(null, x4.getValue()),

                () -> Assertions.assertEquals(Integer.class, y1.getType()),
                () -> Assertions.assertTrue(y1.isFlat()),
                () -> Assertions.assertEquals("y", y1.getName()),
                () -> Assertions.assertEquals(1, y1.getIndex()),
                () -> Assertions.assertEquals(42, y1.getValue()),
                () -> Assertions.assertEquals(73, y2.getValue()),
                () -> Assertions.assertEquals(null, y3.getValue()),

                () -> Assertions.assertEquals(double.class, z1.getType()),
                () -> Assertions.assertFalse(z1.isFlat()),
                () -> Assertions.assertEquals("z", z1.getName()),
                () -> Assertions.assertEquals(2, z1.getIndex()),
                () -> Assertions.assertEquals(55.0, z1.getValue()),
                () -> Assertions.assertEquals(-787.75, z2.getValue()),
                () -> Assertions.assertTrue(Double.isNaN(z3.getValue())),

                () -> Assertions.assertEquals(optBd, a1.getType()),
                () -> Assertions.assertFalse(a1.isFlat()),
                () -> Assertions.assertEquals("a", a1.getName()),
                () -> Assertions.assertEquals(0, a1.getIndex()),
                () -> Assertions.assertEquals(Optional.of(five), a1.getValue()),
                () -> Assertions.assertEquals(Optional.empty(), a2.getValue()),
                () -> Assertions.assertEquals(null, a3.getValue()),

                () -> Assertions.assertEquals(OptionalInt.class, b1.getType()),
                () -> Assertions.assertFalse(b1.isFlat()),
                () -> Assertions.assertEquals("b", b1.getName()),
                () -> Assertions.assertEquals(1, b1.getIndex()),
                () -> Assertions.assertEquals(OptionalInt.of(6), b1.getValue()),
                () -> Assertions.assertEquals(OptionalInt.empty(), b2.getValue()),
                () -> Assertions.assertEquals(null, b3.getValue())
        );
    }

    /*@Test
    @SuppressWarnings("unchecked")
    public void testSimpleSqlNamedParameterWithValue() throws Exception {
        //var n = ParameterSet.parameters(ParsedQuery.parse(":x and :y"), m).associar("a", 42);
    }*/

    @Test
    @SuppressWarnings("unchecked")
    public void testBadSqlNamedParameter() throws Exception {
        var ex = UnsupportedOperationException.class;
        var optThread = "" + BAD_METHOD_2.getParameters()[0].getParameterizedType();
        var listBool = "" + BAD_METHOD_3.getParameters()[1].getParameterizedType();
        Assertions.assertAll(
                () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_1), "" + Thread.class),
                () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_2), optThread),
                () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_3), listBool),
                () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_4), "" + String[].class),
                () -> ForTests.testNull("m", () -> SqlNamedParameter.forMethod(null), "null")
        );
    }

    /*@Test
    @SuppressWarnings("unchecked")
    public void testHandle() throws Exception {
    }*/

    @Test
    public void testBadValue() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SqlNamedParameter.forMethod(TEST_METHOD_1).get(2).withValue(null));
    }
}
