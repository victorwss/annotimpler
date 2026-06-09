package ninja.javahacker.test.annotimpler.sql.meta;

import ninja.javahacker.annotimpler.sql.Flat;
import ninja.javahacker.annotimpler.sql.jdbcstmt.NamedParameterStatement;
import ninja.javahacker.annotimpler.sql.meta.ParameterReceiver;
import ninja.javahacker.annotimpler.sql.meta.ParameterSet;
import ninja.javahacker.test.ForTests;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings({"unused", "ObjectEqualsNull", "IncompatibleEquals"})
public class ParameterSetTest {

    // ── Auxiliary types ──────────────────────────────────────────────────────

    private enum Hue { RED, YELLOW, BLUE }

    private record Pair(String first, Integer second) {}

    private record Sole(String value) {}

    // ── Test methods — bodies never run, only Method reflection objects are used ──

    private void mStr(String x) { throw new AssertionError(); }
    private void mTwo(String x, Integer y) { throw new AssertionError(); }
    private void mPrim(int n) { throw new AssertionError(); }
    private void mEnum(Hue color) { throw new AssertionError(); }
    private void mPair(Pair rec) { throw new AssertionError(); }
    private void mSole(Sole sr) { throw new AssertionError(); }
    private void mOpt(Optional<String> opt) { throw new AssertionError(); }
    private void mMulti(String a, Hue b, Integer c) { throw new AssertionError(); }

    private static final Method M_STR   = m("mStr",   String.class);
    private static final Method M_TWO   = m("mTwo",   String.class, Integer.class);
    private static final Method M_PRIM  = m("mPrim",  int.class);
    private static final Method M_ENUM  = m("mEnum",  Hue.class);
    private static final Method M_PAIR  = m("mPair",  Pair.class);
    private static final Method M_SOLE  = m("mSole",  Sole.class);
    private static final Method M_OPT   = m("mOpt",   Optional.class);
    private static final Method M_MULTI = m("mMulti", String.class, Hue.class, Integer.class);

    private static Method m(String name, Class<?>... paramTypes) {
        try {
            return ParameterSetTest.class.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ── Recording ParameterReceiver ──────────────────────────────────────────

    record RecCall(boolean isReceive, String name, Class<?> type, Object value) {}

    private static class RecordingReceiver implements ParameterReceiver {
        final List<RecCall> calls = new ArrayList<>();

        @Override
        public void receiveNull(String name, Class<?> type) {
            calls.add(new RecCall(false, name, type, null));
        }

        @Override
        public void receive(String name, Object value) {
            calls.add(new RecCall(true, name, null, value));
        }
    }

    // ── Access to package-private NamedParameterStatementHandler.forJdbc ────

    private static ParameterReceiver forJdbc(NamedParameterStatement nps) throws Exception {
        var cls = Class.forName("ninja.javahacker.annotimpler.sql.jdbcstmt.NamedParameterStatementHandler");
        var meth = cls.getDeclaredMethod("forJdbc", NamedParameterStatement.class);
        meth.setAccessible(true);
        return (ParameterReceiver) meth.invoke(null, nps);
    }

    // ── Mock NPS backed by a recording PreparedStatement proxy ───────────────

    private static class MockNps {
        record Call(String methodName, Object[] args) {}

        final List<Call> calls = new ArrayList<>();
        final NamedParameterStatement nps;

        MockNps(String paramName) {
            var mockPs = (PreparedStatement) java.lang.reflect.Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] {PreparedStatement.class},
                    (proxy, method, args) -> {
                        calls.add(new Call(method.getName(), args == null ? new Object[0] : args));
                        return null;
                    });
            nps = NamedParameterStatement.wrap(mockPs, Map.of(paramName, List.of(1)));
        }

        ParameterReceiver receiver() throws Exception {
            return forJdbc(nps);
        }

        Call firstCall() {
            return calls.get(0);
        }
    }

    // ── Tests: paramNames() ──────────────────────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testParamNames() throws Exception {
        var pf = "[testParamNames] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "single String param → [\"x\"]", () ->
                        Assertions.assertEquals(List.of("x"), new ParameterSet(M_STR).paramNames())),

                DynamicTest.dynamicTest(pf + "two typed params → [\"x\", \"y\"]", () ->
                        Assertions.assertEquals(List.of("x", "y"), new ParameterSet(M_TWO).paramNames())),

                DynamicTest.dynamicTest(pf + "primitive int param → [\"n\"]", () ->
                        Assertions.assertEquals(List.of("n"), new ParameterSet(M_PRIM).paramNames())),

                DynamicTest.dynamicTest(pf + "enum param → [\"color\"]", () ->
                        Assertions.assertEquals(List.of("color"), new ParameterSet(M_ENUM).paramNames())),

                DynamicTest.dynamicTest(pf + "multi-component record → [\"rec::first\", \"rec::second\"]", () ->
                        Assertions.assertEquals(List.of("rec::first", "rec::second"), new ParameterSet(M_PAIR).paramNames())),

                DynamicTest.dynamicTest(pf + "single-component record → [\"sr\"] (not \"sr::value\")", () ->
                        Assertions.assertEquals(List.of("sr"), new ParameterSet(M_SOLE).paramNames())),

                DynamicTest.dynamicTest(pf + "Optional<String> param → [\"opt\"]", () ->
                        Assertions.assertEquals(List.of("opt"), new ParameterSet(M_OPT).paramNames())),

                DynamicTest.dynamicTest(pf + "three mixed params → [\"a\", \"b\", \"c\"]", () ->
                        Assertions.assertEquals(List.of("a", "b", "c"), new ParameterSet(M_MULTI).paramNames()))
        );
    }

    // ── Tests: withValues().accept() dispatch via RecordingReceiver ──────────

    @TestFactory
    public Stream<DynamicTest> testDispatch() throws Exception {
        var pf = "[testDispatch] ";
        var tests = new ArrayList<DynamicTest>();

        // String with a value → receive("x", "hello")
        tests.add(DynamicTest.dynamicTest(pf + "String value → receive", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_STR).withValues("hello").accept(rec);
            Assertions.assertEquals(List.of(new RecCall(true, "x", null, "hello")), rec.calls);
        }));

        // String null → receiveNull("x", String.class)
        tests.add(DynamicTest.dynamicTest(pf + "String null → receiveNull", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_STR).withValues((Object) null).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(false, "x", String.class, null)), rec.calls);
        }));

        // Integer value → receive("y", 42); also verifies second param alongside first
        tests.add(DynamicTest.dynamicTest(pf + "Integer value → receive (two-param)", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_TWO).withValues("hi", 42).accept(rec);
            Assertions.assertEquals(List.of(
                    new RecCall(true, "x", null, "hi"),
                    new RecCall(true, "y", null, 42)
            ), rec.calls);
        }));

        // Integer null → receiveNull("y", Integer.class)
        tests.add(DynamicTest.dynamicTest(pf + "Integer null → receiveNull", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_TWO).withValues("hi", null).accept(rec);
            Assertions.assertEquals(List.of(
                    new RecCall(true, "x", null, "hi"),
                    new RecCall(false, "y", Integer.class, null)
            ), rec.calls);
        }));

        // Primitive int with value → receive boxed Integer
        tests.add(DynamicTest.dynamicTest(pf + "int primitive value → receive (autoboxed)", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_PRIM).withValues(99).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(true, "n", null, 99)), rec.calls);
        }));

        // Primitive int null → IllegalValueException (primitives reject null)
        tests.add(DynamicTest.dynamicTest(pf + "int primitive null → IllegalValueException", () ->
                Assertions.assertThrows(
                        ParameterReceiver.IllegalValueException.class,
                        () -> new ParameterSet(M_PRIM).withValues((Object) null))));

        // Enum value → receive ordinal (YELLOW.ordinal() == 1)
        tests.add(DynamicTest.dynamicTest(pf + "enum value → receive ordinal", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_ENUM).withValues(Hue.YELLOW).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(true, "color", null, Hue.YELLOW.ordinal())), rec.calls);
        }));

        // Enum null → IllegalValueException (enum strategy always rejects null)
        tests.add(DynamicTest.dynamicTest(pf + "enum null → IllegalValueException", () ->
                Assertions.assertThrows(
                        ParameterReceiver.IllegalValueException.class,
                        () -> new ParameterSet(M_ENUM).withValues((Object) null))));

        // Multi-component record non-null → receives each component under name::field
        tests.add(DynamicTest.dynamicTest(pf + "record non-null → receive each component with rec::field name", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_PAIR).withValues(new Pair("hello", 5)).accept(rec);
            Assertions.assertEquals(List.of(
                    new RecCall(true, "rec::first", null, "hello"),
                    new RecCall(true, "rec::second", null, 5)
            ), rec.calls);
        }));

        // Multi-component record null → receiveNull for each component
        tests.add(DynamicTest.dynamicTest(pf + "record null → receiveNull for each component", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_PAIR).withValues((Object) null).accept(rec);
            Assertions.assertEquals(List.of(
                    new RecCall(false, "rec::first", String.class, null),
                    new RecCall(false, "rec::second", Integer.class, null)
            ), rec.calls);
        }));

        // Single-component record uses outer param name (not name::field)
        tests.add(DynamicTest.dynamicTest(pf + "single-component record → param name is outer name only", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_SOLE).withValues(new Sole("world")).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(true, "sr", null, "world")), rec.calls);
        }));

        // Optional<String> present → receive unwrapped value
        tests.add(DynamicTest.dynamicTest(pf + "Optional<String> present → receive inner value", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_OPT).withValues(Optional.of("hello")).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(true, "opt", null, "hello")), rec.calls);
        }));

        // Optional<String> empty → receiveNull (treat as absent value)
        tests.add(DynamicTest.dynamicTest(pf + "Optional<String> empty → receiveNull", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_OPT).withValues(Optional.empty()).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(false, "opt", String.class, null)), rec.calls);
        }));

        // Optional<String> null → receiveNull (null Optional treated as absent)
        tests.add(DynamicTest.dynamicTest(pf + "Optional<String> null → receiveNull", () -> {
            var rec = new RecordingReceiver();
            new ParameterSet(M_OPT).withValues((Object) null).accept(rec);
            Assertions.assertEquals(List.of(new RecCall(false, "opt", String.class, null)), rec.calls);
        }));

        return tests.stream();
    }

    // ── Tests: IllegalValueException cases ───────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testIllegalValues() throws Exception {
        var pf = "[testIllegalValues] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "wrong argument count → IllegalValueException", () ->
                        Assertions.assertThrows(
                                ParameterReceiver.IllegalValueException.class,
                                () -> new ParameterSet(M_TWO).withValues("only-one"))),

                DynamicTest.dynamicTest(pf + "extra argument → IllegalValueException", () ->
                        Assertions.assertThrows(
                                ParameterReceiver.IllegalValueException.class,
                                () -> new ParameterSet(M_STR).withValues("x", "extra"))),

                DynamicTest.dynamicTest(pf + "wrong type for enum param → IllegalValueException", () ->
                        Assertions.assertThrows(
                                ParameterReceiver.IllegalValueException.class,
                                () -> new ParameterSet(M_ENUM).withValues("not-a-hue"))),

                DynamicTest.dynamicTest(pf + "wrong record type → IllegalValueException", () ->
                        Assertions.assertThrows(
                                ParameterReceiver.IllegalValueException.class,
                                () -> new ParameterSet(M_PAIR).withValues(new Sole("oops"))))
        );
    }

    // ── Tests: ParameterSet identity (getMethod, methodName, toString) ────────

    @TestFactory
    public Stream<DynamicTest> testIdentity() throws Exception {
        var ps = new ParameterSet(M_STR);
        var ps2 = new ParameterSet(M_TWO);
        var pf = "[testIdentity] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "getMethod() returns the original Method", () ->
                        Assertions.assertEquals(M_STR, ps.getMethod())),

                DynamicTest.dynamicTest(pf + "methodName() contains the Java method name", () ->
                        Assertions.assertTrue(ps.methodName().contains("mStr"), ps.methodName())),

                DynamicTest.dynamicTest(pf + "toString() = \"ParameterSet - \" + methodName()", () ->
                        Assertions.assertEquals("ParameterSet - " + ps.methodName(), ps.toString())),

                DynamicTest.dynamicTest(pf + "equals same method → true", () -> {
                    var copy = new ParameterSet(M_STR);
                    Assertions.assertEquals(ps, copy);
                }),

                DynamicTest.dynamicTest(pf + "equals different method → false", () ->
                        Assertions.assertNotEquals(ps, ps2)),

                DynamicTest.dynamicTest(pf + "hashCode same method → equal", () -> {
                    var copy = new ParameterSet(M_STR);
                    Assertions.assertEquals(ps.hashCode(), copy.hashCode());
                }),

                DynamicTest.dynamicTest(pf + "equals null → false", () ->
                        Assertions.assertFalse(ps.equals(null))),

                DynamicTest.dynamicTest(pf + "equals unrelated type → false", () ->
                        Assertions.assertFalse(ps.equals("x")))
        );
    }

    // ── Tests: ParameterReceiver.forMethod() returns equivalent strategy ──────

    @TestFactory
    public Stream<DynamicTest> testForMethodEquivalence() throws Exception {
        var pf = "[testForMethodEquivalence] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "forMethod returns NamedAcceptor1 with correct paramNames", () -> {
                    var acceptor = (ParameterReceiver.NamedAcceptor1) ParameterReceiver.forMethod(M_PAIR);
                    Assertions.assertEquals(List.of("rec::first", "rec::second"), acceptor.paramNames());
                }),

                DynamicTest.dynamicTest(pf + "forMethod dispatch identical to ParameterSet dispatch", () -> {
                    var acceptor = ParameterReceiver.forMethod(M_PAIR);
                    var rec = new RecordingReceiver();
                    acceptor.handle(new Object[] {new Pair("x", 1)}).accept(rec);

                    var recPs = new RecordingReceiver();
                    new ParameterSet(M_PAIR).withValues(new Pair("x", 1)).accept(recPs);

                    Assertions.assertEquals(rec.calls, recPs.calls);
                })
        );
    }

    // ── Tests: NamedParameterStatementHandler.forJdbc() JDBC dispatch ─────────

    @TestFactory
    public Stream<DynamicTest> testJdbcReceiver() throws Exception {
        var pf = "[testJdbcReceiver] ";
        var tests = new ArrayList<DynamicTest>();

        // String value → setString(1, "hello")
        tests.add(DynamicTest.dynamicTest(pf + "String value → setString", () -> {
            var mock = new MockNps("x");
            mock.receiver().receive("x", "hello");
            Assertions.assertEquals("setString", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, "hello"}, mock.firstCall().args());
        }));

        // Integer value → setInt(1, 42)
        tests.add(DynamicTest.dynamicTest(pf + "Integer value → setInt", () -> {
            var mock = new MockNps("x");
            mock.receiver().receive("x", 42);
            Assertions.assertEquals("setInt", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, 42}, mock.firstCall().args());
        }));

        // Integer null → setNull(1, Types.INTEGER)
        tests.add(DynamicTest.dynamicTest(pf + "Integer null → setNull(INTEGER)", () -> {
            var mock = new MockNps("x");
            mock.receiver().receiveNull("x", Integer.class);
            Assertions.assertEquals("setNull", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, Types.INTEGER}, mock.firstCall().args());
        }));

        // Long null → setNull(1, Types.BIGINT)
        tests.add(DynamicTest.dynamicTest(pf + "Long null → setNull(BIGINT)", () -> {
            var mock = new MockNps("x");
            mock.receiver().receiveNull("x", Long.class);
            Assertions.assertEquals("setNull", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, Types.BIGINT}, mock.firstCall().args());
        }));

        // Boolean value → setBoolean(1, true)
        tests.add(DynamicTest.dynamicTest(pf + "Boolean value → setBoolean", () -> {
            var mock = new MockNps("x");
            mock.receiver().receive("x", Boolean.TRUE);
            Assertions.assertEquals("setBoolean", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, true}, mock.firstCall().args());
        }));

        // Boolean null → setNull(1, Types.BOOLEAN)
        tests.add(DynamicTest.dynamicTest(pf + "Boolean null → setNull(BOOLEAN)", () -> {
            var mock = new MockNps("x");
            mock.receiver().receiveNull("x", Boolean.class);
            Assertions.assertEquals("setNull", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, Types.BOOLEAN}, mock.firstCall().args());
        }));

        // String null → setString(1, null)  [no SQL type code, uses direct setter with null]
        tests.add(DynamicTest.dynamicTest(pf + "String null → setString(null)", () -> {
            var mock = new MockNps("x");
            mock.receiver().receiveNull("x", String.class);
            Assertions.assertEquals("setString", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, null}, mock.firstCall().args());
        }));

        // LocalDate value → setObject(1, date)  [NamedParameterStatement.setLocalDate delegates to setObject]
        tests.add(DynamicTest.dynamicTest(pf + "LocalDate value → setObject", () -> {
            var date = LocalDate.of(2024, 6, 8);
            var mock = new MockNps("x");
            mock.receiver().receive("x", date);
            Assertions.assertEquals("setObject", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, date}, mock.firstCall().args());
        }));

        // BigDecimal value → setBigDecimal(1, value)
        tests.add(DynamicTest.dynamicTest(pf + "BigDecimal value → setBigDecimal", () -> {
            var bd = new BigDecimal("3.14");
            var mock = new MockNps("x");
            mock.receiver().receive("x", bd);
            Assertions.assertEquals("setBigDecimal", mock.firstCall().methodName());
            Assertions.assertArrayEquals(new Object[] {1, bd}, mock.firstCall().args());
        }));

        return tests.stream();
    }

    // ── Tests: @NonNull violations ────────────────────────────────────────────

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        var pf = "[testNulls] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "new ParameterSet(null) → @NonNull violation", () ->
                        ForTests.testNull("method", () -> new ParameterSet(null))),

                DynamicTest.dynamicTest(pf + "withValues(null array) → @NonNull violation", () ->
                        ForTests.testNull("args", () -> new ParameterSet(M_STR).withValues((Object[]) null))),

                DynamicTest.dynamicTest(pf + "accept(null receiver) → @NonNull violation", () ->
                        ForTests.testNull("ps", () -> new ParameterSet(M_STR).withValues("hi").accept(null))),

                DynamicTest.dynamicTest(pf + "ParameterReceiver.forMethod(null) → @NonNull violation", () ->
                        ForTests.testNull("method", () -> ParameterReceiver.forMethod(null)))
        );
    }
}
