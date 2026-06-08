package ninja.javahacker.test.annotimpler.sql.jdbcstmt;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings({"unused", "unchecked"})
public class SqlNamedParameterTest {

    private static final Method TEST_METHOD_1 = find("testMethod1");
    private static final Method TEST_METHOD_2 = find("testMethod2");
    /*private static final Method TEST_METHOD_3 = find("testMethod3");
    private static final Method TEST_METHOD_4 = find("testMethod4");
    private static final Method BAD_METHOD_1 = find("badMethod1");
    private static final Method BAD_METHOD_2 = find("badMethod2");
    private static final Method BAD_METHOD_3 = find("badMethod3");
    private static final Method BAD_METHOD_4 = find("badMethod4");*/

    private static final BigDecimal FIVE = new BigDecimal(5);
    //private static final List<? extends SqlNamedParameter<?>> S1 = SqlNamedParameter.forMethod(TEST_METHOD_1);
    private static final ParameterSet P1;
    //private static final SqlNamedParameter<String> X0 = (SqlNamedParameter<String>) S1.get(0);
    //private static final SqlNamedParameter<Integer> Y0 = (SqlNamedParameter<Integer>) S1.get(1);
    //private static final SqlNamedParameter<Double> Z0 = (SqlNamedParameter<Double>) S1.get(2);
    //private static final List<? extends SqlNamedParameter<?>> S2 = SqlNamedParameter.forMethod(TEST_METHOD_2);
    private static final ParameterSet P2;
    private static final Type OPT_BD = TEST_METHOD_2.getParameters()[0].getParameterizedType();
    //private static final SqlNamedParameter<Optional<BigDecimal>> A0 = (SqlNamedParameter<Optional<BigDecimal>>) S2.get(0);
    //private static final SqlNamedParameter<OptionalInt> B0 = (SqlNamedParameter<OptionalInt>) S2.get(1);
    //private static final SqlNamedParameterWithValue<String> X1 = X0.withValue("a");
    //private static final SqlNamedParameterWithValue<String> X2 = X0.withValue("");
    //private static final SqlNamedParameterWithValue<String> X3 = X0.withValue("whoa");
    //private static final SqlNamedParameterWithValue<String> X4 = X0.withValue(null);
    //private static final SqlNamedParameterWithValue<Integer> Y1 = Y0.withValue(42);
    //private static final SqlNamedParameterWithValue<Integer> Y2 = Y0.withValue(73);
    //private static final SqlNamedParameterWithValue<Integer> Y3 = Y0.withValue(null);
    //private static final SqlNamedParameterWithValue<Double> Z1 = Z0.withValue(55.0);
    //private static final SqlNamedParameterWithValue<Double> Z2 = Z0.withValue(-787.75);
    //private static final SqlNamedParameterWithValue<Double> Z3 = Z0.withValue(Double.NaN);
    //private static final SqlNamedParameterWithValue<Optional<BigDecimal>> A1 = A0.withValue(Optional.of(FIVE));
    //private static final SqlNamedParameterWithValue<Optional<BigDecimal>> A2 = A0.withValue(Optional.empty());
    //private static final SqlNamedParameterWithValue<Optional<BigDecimal>> A3 = A0.withValue(null);
    //private static final SqlNamedParameterWithValue<OptionalInt> B1 = B0.withValue(OptionalInt.of(6));
    //private static final SqlNamedParameterWithValue<OptionalInt> B2 = B0.withValue(OptionalInt.empty());
    //private static final SqlNamedParameterWithValue<OptionalInt> B3 = B0.withValue(null);

    static {
        try {
            P1 = new ParameterSet(TEST_METHOD_1);
            P2 = new ParameterSet(TEST_METHOD_2);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public SqlNamedParameterTest() {
    }

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    private static Method find(String name) {
        return Stream.of(SqlNamedParameterTest.class.getDeclaredMethods()).filter(m -> m.getName().equals(name)).findFirst().orElseThrow();
    }

    private void testMethod1(String x, Integer y, double z) {
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

    /*private static record BarRecord(FooRecord foo4, Optional<FooRecord> foo5, BigDecimal foo6) {
    }*/

    /*private void testMethod3(
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
    }*/

    /*private void badMethod1(Thread x) {
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
    }*/

    /*private static Stream<SqlNamedParameter<?>> params() {
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
    }*/

    private static String name(ParameterSet obj) {
        if (obj == P1) return "P1";
        if (obj == P2) return "P2";
        throw new AssertionError();
    }

    /*private static String name(ParameterSetWithValues obj) {
        if (obj.getSet() == P1) return "P1V";
        if (obj.getSet() == P2) return "P2V";
        throw new AssertionError();
    }*/

    /*private static String paramName(SqlNamedParameter<?> obj) {
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

    private static String toString(SqlNamedParameter<?> obj) {
        return "SqlNamedParameter[" + index(obj) + ", " + type(obj) + ", " + paramName(obj) + ", " + shouldBeFlat(obj) + "]";
    }

    private static String toString(SqlNamedParameterWithValue<?> obj) {
        return "SqlNamedParameterWithValue[" + index(obj) + ", " + type(obj) + ", " + paramName(obj) + ", " + shouldBeFlat(obj) + ", " + value(obj) + "]";
    }*/

    private static String toString(ParameterSet obj) {
        if (obj == P1) return "ParameterSet - void SqlNamedParameterTest.testMethod1(String, Integer, double)";
        if (obj == P2) return "ParameterSet - void SqlNamedParameterTest.testMethod2(Optional<BigDecimal>, OptionalInt)";
        throw new AssertionError();
    }

    /*private static String toString(ParameterSetWithValues obj) {
        var a = """
                ParameterSetWithValues - void SqlNamedParameterTest.testMethod1(String, Integer, double)
                - [SqlNamedParameterWithValue[0, class java.lang.String, x, false, x],
                SqlNamedParameterWithValue[1, class java.lang.Integer, y, true, 45],
                SqlNamedParameterWithValue[2, double, z, false, 3.3]]
                """;
        var b = """
                ParameterSetWithValues - void SqlNamedParameterTest.testMethod2(Optional<BigDecimal>, OptionalInt)
                - [SqlNamedParameterWithValue[0, java.util.Optional<java.math.BigDecimal>, a, false, Optional[5]],
                SqlNamedParameterWithValue[1, class java.util.OptionalInt, b, false, OptionalInt[7]]]
                """;
        a = a.replace("\n", " ").replaceAll(" +", " ").trim();
        b = b.replace("\n", " ").replaceAll(" +", " ").trim();
        if (obj.getSet() == P1) return a;
        if (obj.getSet() == P2) return b;
        throw new AssertionError();
    }*/

    /*private static boolean shouldAcceptNull(SqlNamedParameter<?> obj) {
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
    }*/

    /*@TestFactory
    public Stream<DynamicTest> testFlat() {
        return params().map(x -> n(
                "[testFlat] " + name(x) + " flat = " + shouldBeFlat(x),
                () -> Assertions.assertEquals(shouldBeFlat(x), x.isFlat())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testFlatValue() {
        return values().map(x -> n(
                "[testFlatValue] " + name(x) + " flat = " + shouldBeFlat(x),
                () -> Assertions.assertEquals(shouldBeFlat(x), x.isFlat())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testType() {
        return params().map(x -> n(
                "[testType] " + name(x) + " type = " + type(x),
                () -> Assertions.assertEquals(type(x), x.getType())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testValueType() {
        return values().map(x -> n(
                "[testValueType] " + name(x) + " type = " + type(x),
                () -> Assertions.assertEquals(type(x), x.getType())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testName() {
        return params().map(x -> n(
                "[testName] " + name(x) + " name = " + paramName(x),
                () -> Assertions.assertEquals(paramName(x), x.getName())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testNameValue() {
        return values().map(x -> n(
                "[testNameValue] " + name(x) + " name = " + paramName(x),
                () -> Assertions.assertEquals(paramName(x), x.getName())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testIndex() {
        return params().map(x -> n(
                "[testIndex] " + name(x) + " index = " + index(x),
                () -> Assertions.assertEquals(index(x), x.getIndex())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testIndexValue() {
        return values().map(x -> n(
                "[testIndexValue] " + name(x) + " index = " + index(x),
                () -> Assertions.assertEquals(index(x), x.getIndex())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testToString() {
        return params().map(x -> n(
                "[testToString] " + name(x),
                () -> Assertions.assertEquals(toString(x), x.toString())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testToStringValue() {
        return values().map(x -> n(
                "[testToStringValue] " + name(x),
                () -> Assertions.assertEquals(toString(x), x.toString())
        ));
    }*/

    @TestFactory
    public Stream<DynamicTest> testToStringSet() {
        return Stream.of(P1, P2).map(x -> n(
                "[testToStringSet] " + name(x),
                () -> Assertions.assertEquals(toString(x), x.toString())
        ));
    }

    /*@TestFactory
    public Stream<DynamicTest> testToStringSetValues() throws Exception {
        var v1 = P1.withValues("x", 45, 3.3);
        var v2 = P2.withValues(Optional.of(FIVE), OptionalInt.of(7));
        return Stream.of(v1, v2).map(x -> n(
                "[testToStringSetValues] " + name(x),
                () -> Assertions.assertEquals(toString(x), x.toString())
        ));
    }*/

    /*@TestFactory
    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    public Stream<DynamicTest> testEquals() {
        var s1Copy = SqlNamedParameter.forMethod(TEST_METHOD_1);
        return Stream.of(
                n("[testEquals] a", () -> Assertions.assertTrue(S1.equals(S1))),
                n("[testEquals] b", () -> Assertions.assertTrue(S1.equals(s1Copy))),
                n("[testEquals] c", () -> Assertions.assertTrue(S2.equals(S2))),
                n("[testEquals] d", () -> Assertions.assertFalse(S1.equals(S2))),
                n("[testEquals] e", () -> Assertions.assertFalse(S1.equals(null))),
                n("[testEquals] f", () -> Assertions.assertFalse(S1.equals("xxx")))
        );
    }

    @TestFactory
    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    public Stream<DynamicTest> testEqualsValue() {
        var x1copy = X0.withValue("a");
        return Stream.of(
                n("[testEqualsValue] a", () -> Assertions.assertTrue(X1.equals(X1))),
                n("[testEqualsValue] b", () -> Assertions.assertTrue(X1.equals(x1copy))),
                n("[testEqualsValue] c", () -> Assertions.assertTrue(X2.equals(X2))),
                n("[testEqualsValue] d", () -> Assertions.assertFalse(X1.equals(X2))),
                n("[testEqualsValue] e", () -> Assertions.assertFalse(X1.equals(null))),
                n("[testEqualsValue] f", () -> Assertions.assertFalse(X1.equals("xxx")))
        );
    }*/

    @TestFactory
    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    public Stream<DynamicTest> testEqualsSet() throws Exception {
        var p1Copy = new ParameterSet(TEST_METHOD_1);
        return Stream.of(
                n("[testEqualsSet] a", () -> Assertions.assertTrue(P1.equals(P1))),
                n("[testEqualsSet] b", () -> Assertions.assertTrue(P1.equals(p1Copy))),
                n("[testEqualsSet] c", () -> Assertions.assertTrue(P2.equals(P2))),
                n("[testEqualsSet] d", () -> Assertions.assertFalse(P1.equals(P2))),
                n("[testEqualsSet] e", () -> Assertions.assertFalse(P1.equals(null))),
                n("[testEqualsSet] f", () -> Assertions.assertFalse(P1.equals("xxx")))
        );
    }

    /*@TestFactory
    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    public Stream<DynamicTest> testEqualsSetValues() throws Exception {
        var v1 = P1.withValues("x", 45, 3.3);
        var v1Copy = P1.withValues("x", 45, 3.3);
        var v2 = P2.withValues(Optional.of(FIVE), OptionalInt.of(7));
        return Stream.of(
                n("[testEqualsSetValues] a", () -> Assertions.assertTrue(v1.equals(v1))),
                n("[testEqualsSetValues] b", () -> Assertions.assertTrue(v1.equals(v1Copy))),
                n("[testEqualsSetValues] c", () -> Assertions.assertTrue(v2.equals(v2))),
                n("[testEqualsSetValues] d", () -> Assertions.assertFalse(v1.equals(v2))),
                n("[testEqualsSetValues] e", () -> Assertions.assertFalse(v1.equals(null))),
                n("[testEqualsSetValues] f", () -> Assertions.assertFalse(v1.equals("xxx")))
        );
    }*/

    /*@TestFactory
    public Stream<DynamicTest> testHashCode() {
        var s1Copy = SqlNamedParameter.forMethod(TEST_METHOD_1);
        return Stream.of(
                n("[testHashCode] a", () -> Assertions.assertEquals(S1.hashCode(), S1.hashCode())),
                n("[testHashCode] b", () -> Assertions.assertEquals(S1.hashCode(), s1Copy.hashCode())),
                n("[testHashCode] c", () -> Assertions.assertEquals(S2.hashCode(), S2.hashCode())),
                n("[testHashCode] d", () -> Assertions.assertNotEquals(S1.hashCode(), S2.hashCode()))
        );
    }

    @TestFactory
    public Stream<DynamicTest> testHashCodeValue() {
        var x1copy = X0.withValue("a");
        return Stream.of(
                n("[testHashCodeValue] a", () -> Assertions.assertEquals(X1.hashCode(), X1.hashCode())),
                n("[testHashCodeValue] b", () -> Assertions.assertEquals(X1.hashCode(), x1copy.hashCode())),
                n("[testHashCodeValue] c", () -> Assertions.assertEquals(X2.hashCode(), X2.hashCode())),
                n("[testHashCodeValue] d", () -> Assertions.assertNotEquals(X1.hashCode(), X2.hashCode()))
        );
    }*/

    @TestFactory
    public Stream<DynamicTest> testHashCodeSet()throws Exception  {
        var p1Copy = new ParameterSet(TEST_METHOD_1);
        return Stream.of(
                n("[testHashCodeSet] a", () -> Assertions.assertEquals(P1.hashCode(), P1.hashCode())),
                n("[testHashCodeSet] b", () -> Assertions.assertEquals(P1.hashCode(), p1Copy.hashCode())),
                n("[testHashCodeSet] c", () -> Assertions.assertEquals(P2.hashCode(), P2.hashCode())),
                n("[testHashCodeSet] d", () -> Assertions.assertNotEquals(P1.hashCode(), P2.hashCode()))
        );
    }

    /*@TestFactory
    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    public Stream<DynamicTest> testHashCodeValues()throws Exception  {
        var v1 = P1.withValues("x", 45, 3.3);
        var v1Copy = P1.withValues("x", 45, 3.3);
        var v2 = P2.withValues(Optional.of(FIVE), OptionalInt.of(7));
        return Stream.of(
                n("[testHashCodeValues] a", () -> Assertions.assertEquals(v1.hashCode(), v1.hashCode())),
                n("[testHashCodeValues] b", () -> Assertions.assertEquals(v1.hashCode(), v1Copy.hashCode())),
                n("[testHashCodeValues] c", () -> Assertions.assertEquals(v2.hashCode(), v2.hashCode())),
                n("[testHashCodeValues] d", () -> Assertions.assertNotEquals(v1.hashCode(), v2.hashCode()))
        );
    }*/

    /*@TestFactory
    public Stream<DynamicTest> testAcceptNull() {
        return params().map(x -> n(
                "[testAcceptNull] " + name(x) + " accept null = " + shouldAcceptNull(x),
                () -> Assertions.assertEquals(shouldAcceptNull(x), x.accept(null))
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testAccepts() {
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
                "[testAccepts] " + name(x) + " accepts " + vn.apply(s) + " = " + shouldAccept(x, s),
                () -> Assertions.assertEquals(shouldAccept(x, s), x.accept(s))
        )));
    }

    @TestFactory
    public Stream<DynamicTest> testValue() {
        return values().map(x -> n(
                "[testValue] " + name(x) + " value = " + value(x),
                () -> Assertions.assertEquals(value(x), x.getValue())
        ));
    }

    @TestFactory
    @SuppressWarnings({"unchecked", "null"})
    public Stream<DynamicTest> testBadSqlNamedParameter() throws Exception {
        var ex = UnsupportedOperationException.class;
        var optThread = "" + BAD_METHOD_2.getParameters()[0].getParameterizedType();
        var listBool = "" + BAD_METHOD_3.getParameters()[1].getParameterizedType();
        return Stream.of(
                n("[testBadSqlNamedParameter] Thread", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_1), "" + Thread.class)),
                n("[testBadSqlNamedParameter] Optional<Thread>", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_2), optThread)),
                n("[testBadSqlNamedParameter] List<Boolean>", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_3), listBool)),
                n("[testBadSqlNamedParameter] String[]", () -> Assertions.assertThrows(ex, () -> SqlNamedParameter.forMethod(BAD_METHOD_4), "" + String[].class)),
                n("[testBadSqlNamedParameter] null", () -> ForTests.testNull("m", () -> SqlNamedParameter.forMethod(null)))
        );
    }

    @Test
    public void testBadValue() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SqlNamedParameter.forMethod(TEST_METHOD_1).get(2).withValue(null));
    }*/

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testBadsAndNulls() throws Exception {
        return Stream.of(
                n("[testBadsAndNulls] ParameterSet(null)"                 , () -> ForTests.testNull("method", () -> new ParameterSet(null))),
                //n("[testBadsAndNulls] ParameterSet.testParameters(null)"  , () -> ForTests.testNull("keys"  , () -> new ParameterSet(TEST_METHOD_1).testParameters(null))),
                n("[testBadsAndNulls] Acceptor2.withValues(null)"         , () -> ForTests.testNull("args"  , () -> new ParameterSet(TEST_METHOD_1).withValues((Object[]) null))),
                n("[testBadsAndNulls] Acceptor2.accept(null)"             , () -> ForTests.testNull("ps"    , () -> new ParameterSet(TEST_METHOD_1).withValues("x", 5, 5.5).accept(null))),
                n("[testBadsAndNulls] ParameterSet.withValues(BAD)"       ,
                        () -> Assertions.assertThrows(ParameterReceiver.IllegalValueException.class, () -> new ParameterSet(TEST_METHOD_1).withValues("x", 5))
                )
        );
    }
}
