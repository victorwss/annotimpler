package ninja.javahacker.test.annotimpler.sql;

import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;
import static ninja.javahacker.annotimpler.sql.meta.SqlNamedParameter.*;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

@SuppressWarnings({"unused", "unchecked"})
public class SqlNamedParameterTest {

    private static final Method TEST_METHOD_1 = find("testMethod1");
    private static final Method TEST_METHOD_2 = find("testMethod2");
    private static final Method TEST_METHOD_3 = find("testMethod3");
    private static final Method TEST_METHOD_4 = find("testMethod4");
    private static final Method BAD_METHOD_1 = find("badMethod1");
    private static final Method BAD_METHOD_2 = find("badMethod2");
    private static final Method BAD_METHOD_3 = find("badMethod3");
    private static final Method BAD_METHOD_4 = find("badMethod4");

    private static final BigDecimal FIVE = new BigDecimal(5);
    private static final List<? extends SqlNamedParameter<?>> S1 = SqlNamedParameter.forMethod(TEST_METHOD_1);
    private static final SqlNamedParameter<String> X0 = (SqlNamedParameter<String>) S1.get(0);
    private static final SqlNamedParameter<Integer> Y0 = (SqlNamedParameter<Integer>) S1.get(1);
    private static final SqlNamedParameter<Double> Z0 = (SqlNamedParameter<Double>) S1.get(2);
    private static final List<? extends SqlNamedParameter<?>> S2 = SqlNamedParameter.forMethod(TEST_METHOD_2);
    private static final Type OPT_BD = TEST_METHOD_2.getParameters()[0].getParameterizedType();
    private static final SqlNamedParameter<Optional<BigDecimal>> A0 = (SqlNamedParameter<Optional<BigDecimal>>) S2.get(0);
    private static final SqlNamedParameter<OptionalInt> B0 = (SqlNamedParameter<OptionalInt>) S2.get(1);
    private static final SqlNamedParameterWithValue<String> X1 = X0.withValue("a");
    private static final SqlNamedParameterWithValue<String> X2 = X0.withValue("");
    private static final SqlNamedParameterWithValue<String> X3 = X0.withValue("whoa");
    private static final SqlNamedParameterWithValue<String> X4 = X0.withValue(null);
    private static final SqlNamedParameterWithValue<Integer> Y1 = Y0.withValue(42);
    private static final SqlNamedParameterWithValue<Integer> Y2 = Y0.withValue(73);
    private static final SqlNamedParameterWithValue<Integer> Y3 = Y0.withValue(null);
    private static final SqlNamedParameterWithValue<Double> Z1 = Z0.withValue(55.0);
    private static final SqlNamedParameterWithValue<Double> Z2 = Z0.withValue(-787.75);
    private static final SqlNamedParameterWithValue<Double> Z3 = Z0.withValue(Double.NaN);
    private static final SqlNamedParameterWithValue<Optional<BigDecimal>> A1 = A0.withValue(Optional.of(FIVE));
    private static final SqlNamedParameterWithValue<Optional<BigDecimal>> A2 = A0.withValue(Optional.empty());
    private static final SqlNamedParameterWithValue<Optional<BigDecimal>> A3 = A0.withValue(null);
    private static final SqlNamedParameterWithValue<OptionalInt> B1 = B0.withValue(OptionalInt.of(6));
    private static final SqlNamedParameterWithValue<OptionalInt> B2 = B0.withValue(OptionalInt.empty());
    private static final SqlNamedParameterWithValue<OptionalInt> B3 = B0.withValue(null);

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

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

    private static Stream<SqlNamedParameter<?>> params() {
        return Stream.of(X0, Y0, Z0, A0, B0);
    }

    private static Stream<SqlNamedParameterWithValue<?>> values() {
        return Stream.of(X1, X2, X3, X4, Y1, Y2, Y3, Z1, Z2, Z3, A1, A2, A3, B1, B2, B3);
    }

    private static String name(SqlNamedParameter<?> obj) {
        if (obj == X0) return "testMethod1(0) - String";
        if (obj == Y0) return "testMethod1(1) - @Flat Integer";
        if (obj == Z0) return "testMethod1(2) - double";
        if (obj == A0) return "Optional<BigDecimal> parameter";
        if (obj == B0) return "OptionalInt parameter";
        throw new AssertionError();
    }

    private static String name(SqlNamedParameterWithValue<?> obj) {
        if (obj == X1) return "a";
        if (obj == X2) return "empty";
        if (obj == X3) return "whoa";
        if (obj == X4) return "null String";
        if (obj == Y1) return "42";
        if (obj == Y2) return "73";
        if (obj == Y3) return "null integer";
        if (obj == Z1) return "55.0";
        if (obj == Z2) return "-787.75";
        if (obj == Z3) return "NaN";
        if (obj == A1) return "Optional.of(BigDecimal 5)";
        if (obj == A2) return "Optional.empty()";
        if (obj == A3) return "Optional null";
        if (obj == B1) return "OptionalInt.of(6)";
        if (obj == B2) return "OptionalInt.empty()";
        if (obj == B3) return "OptionalInt null";
        throw new AssertionError();
    }

    private static String paramName(SqlNamedParameter<?> obj) {
        if (obj == X0) return "x";
        if (obj == Y0) return "y";
        if (obj == Z0) return "z";
        if (obj == A0) return "a";
        if (obj == B0) return "b";
        throw new AssertionError();
    }

    @SuppressWarnings("element-type-mismatch")
    private static String paramName(SqlNamedParameterWithValue<?> obj) {
        if (List.of(X1, X2, X3, X4).contains(obj)) return "x";
        if (List.of(Y1, Y2, Y3).contains(obj)) return "y";
        if (List.of(Z1, Z2, Z3).contains(obj)) return "z";
        if (List.of(A1, A2, A3).contains(obj)) return "a";
        if (List.of(B1, B2, B3).contains(obj)) return "b";
        throw new AssertionError();
    }

    private static boolean shouldBeFlat(SqlNamedParameter<?> obj) {
        return name(obj).contains("@Flat");
    }

    @SuppressWarnings("element-type-mismatch")
    private static boolean shouldBeFlat(SqlNamedParameterWithValue<?> obj) {
        if (!List.of(X1, X2, X3, X4, Y1, Y2, Y3, Z1, Z2, Z3, A1, A2, A3, B1, B2, B3).contains(obj)) throw new AssertionError();
        return List.of(Y1, Y2, Y3).contains(obj);
    }

    private static Type type(SqlNamedParameter<?> obj) {
        if (obj == X0) return String.class;
        if (obj == Y0) return Integer.class;
        if (obj == Z0) return double.class;
        if (obj == A0) return OPT_BD;
        if (obj == B0) return OptionalInt.class;
        throw new AssertionError();
    }

    @SuppressWarnings("element-type-mismatch")
    private static Type type(SqlNamedParameterWithValue<?> obj) {
        if (List.of(X1, X2, X3, X4).contains(obj)) return String.class;
        if (List.of(Y1, Y2, Y3).contains(obj)) return Integer.class;
        if (List.of(Z1, Z2, Z3).contains(obj)) return double.class;
        if (List.of(A1, A2, A3).contains(obj)) return OPT_BD;
        if (List.of(B1, B2, B3).contains(obj)) return OptionalInt.class;
        throw new AssertionError();
    }

    private static int index(SqlNamedParameter<?> obj) {
        if (obj == X0) return 0;
        if (obj == Y0) return 1;
        if (obj == Z0) return 2;
        if (obj == A0) return 0;
        if (obj == B0) return 1;
        throw new AssertionError();
    }

    @SuppressWarnings("element-type-mismatch")
    private static int index(SqlNamedParameterWithValue<?> obj) {
        if (List.of(X1, X2, X3, X4).contains(obj)) return 0;
        if (List.of(Y1, Y2, Y3).contains(obj)) return 1;
        if (List.of(Z1, Z2, Z3).contains(obj)) return 2;
        if (List.of(A1, A2, A3).contains(obj)) return 0;
        if (List.of(B1, B2, B3).contains(obj)) return 1;
        throw new AssertionError();
    }

    private static boolean shouldAcceptNull(SqlNamedParameter<?> obj) {
        if (!List.of(X0, Y0, Z0, A0, B0).contains(obj)) throw new AssertionError();
        return obj != Z0;
    }

    @SuppressWarnings("element-type-mismatch")
    private static boolean shouldAccept(SqlNamedParameter<?> obj, Object target) {
        if (obj == X0) return List.of("", "abc").contains(target);
        if (obj == Y0) return List.of(42).contains(target);
        if (obj == Z0) return List.of(55.0).contains(target);
        if (obj == A0) return List.of(Optional.of(FIVE), Optional.empty()).contains(target);
        if (obj == B0) return List.of(OptionalInt.of(6), OptionalInt.empty()).contains(target);
        throw new AssertionError();
    }

    private static Object value(SqlNamedParameterWithValue<?> obj) {
        if (obj == X1) return "a";
        if (obj == X2) return "";
        if (obj == X3) return "whoa";
        if (obj == X4) return null;
        if (obj == Y1) return 42;
        if (obj == Y2) return 73;
        if (obj == Y3) return null;
        if (obj == Z1) return 55.0;
        if (obj == Z2) return -787.75;
        if (obj == Z3) return Double.NaN;
        if (obj == A1) return Optional.of(FIVE);
        if (obj == A2) return Optional.empty();
        if (obj == A3) return null;
        if (obj == B1) return OptionalInt.of(6);
        if (obj == B2) return OptionalInt.empty();
        if (obj == B3) return null;
        throw new AssertionError();
    }

    private static Stream<Arguments> testFlat() {
        return params().map(x -> n(
                name(x) + " flat = " + shouldBeFlat(x),
                () -> Assertions.assertEquals(shouldBeFlat(x), x.isFlat())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testFlat {0}")
    public void testFlat(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testFlatValue() {
        return values().map(x -> n(
                name(x) + " flat = " + shouldBeFlat(x),
                () -> Assertions.assertEquals(shouldBeFlat(x), x.isFlat())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testFlatValue {0}")
    public void testFlatValue(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testType() {
        return params().map(x -> n(
                name(x) + " type = " + type(x),
                () -> Assertions.assertEquals(type(x), x.getType())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testType {0}")
    public void testType(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testValueType() {
        return values().map(x -> n(
                name(x) + " type = " + type(x),
                () -> Assertions.assertEquals(type(x), x.getType())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testType {0}")
    public void testValueType(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testName() {
        return params().map(x -> n(
                name(x) + " name = " + paramName(x),
                () -> Assertions.assertEquals(paramName(x), x.getName())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testName {0}")
    public void testName(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testNameValue() {
        return values().map(x -> n(
                name(x) + " name = " + paramName(x),
                () -> Assertions.assertEquals(paramName(x), x.getName())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testNameValue {0}")
    public void testNameValue(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testIndex() {
        return params().map(x -> n(
                name(x) + " index = " + index(x),
                () -> Assertions.assertEquals(index(x), x.getIndex())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testIndex {0}")
    public void testIndex(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testIndexValue() {
        return values().map(x -> n(
                name(x) + " index = " + index(x),
                () -> Assertions.assertEquals(index(x), x.getIndex())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testIndexValue {0}")
    public void testIndexValue(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testAcceptNull() {
        return params().map(x -> n(
                name(x) + " accept null = " + shouldAcceptNull(x),
                () -> Assertions.assertEquals(shouldAcceptNull(x), x.accept(null))
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testAcceptNull {0}")
    public void testAcceptNull(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testAccepts() {
        class Foo {
            @Override
            public String toString() {
                return "a foo";
            }
        }
        var stuff = List.of(
                42, 55.0, FIVE, Optional.of(FIVE), Optional.empty(), OptionalInt.of(6), OptionalInt.empty(), "", "abc",
                Thread.currentThread(), Foo.class, new Foo(), Optional.of(new Foo())
        );
        Function<Object, String> vn = obj -> {
            if (obj instanceof Thread) return "a Thread";
            if (obj instanceof Class<?> c) return c.getSimpleName();
            return obj.toString();
        };
        return params().flatMap(x -> stuff.stream().map(s -> n(
                name(x) + " accepts " + vn.apply(s) + " = " + shouldAccept(x, s),
                () -> Assertions.assertEquals(shouldAccept(x, s), x.accept(s))
        ))).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testAccepts {0}")
    public void testAccepts(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> testValue() {
        return values().map(x -> n(
                name(x) + " value = " + value(x),
                () -> Assertions.assertEquals(value(x), x.getValue())
        )).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testValue {0}")
    public void testValue(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    /*@Test
    @SuppressWarnings("unchecked")
    public void testSimpleSqlNamedParameterWithValue() throws Exception {
        //var n = ParameterSet.parameters(ParsedQuery.parse(":x and :y"), m).associar("a", 42);
    }*/

    @SuppressWarnings({"unchecked", "null"})
    private static Stream<Arguments> testBadSqlNamedParameter() throws Exception {
        var ex = UnsupportedOperationException.class;
        var optThread = "" + BAD_METHOD_2.getParameters()[0].getParameterizedType();
        var listBool = "" + BAD_METHOD_3.getParameters()[1].getParameterizedType();
        return Stream.of(
                n("Thread", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_1), "" + Thread.class)),
                n("Optional<Thread>", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_2), optThread)),
                n("List<Boolean>", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_3), listBool)),
                n("String[]", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_4), "" + String[].class)),
                n("null", () -> ForTests.testNull("m", () -> SqlNamedParameter.forMethod(null), "null"))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testBadSqlNamedParameter {0}")
    public void testBadSqlNamedParameter(String name, Executable exec) throws Throwable {
        exec.execute();
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
