package ninja.javahacker.test.annotimpler.jdbc.stmt;

import ninja.javahacker.test.ForTests;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.jdbc;
import module org.junit.jupiter.api;

public class ParameterSetJdbcTest {

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

        NamedParameterStatement receiver() {
            return nps;
        }

        Call firstCall() {
            return calls.get(0);
        }
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
                DynamicTest.dynamicTest(pf + "ParameterReceiver.receiveNull(null) → @NonNull violation [2]", () ->
                        ForTests.testNull("name", () -> new MockNps("x").receiver().receiveNull(null))
                ),

                DynamicTest.dynamicTest(pf + "ParameterReceiver.receiveNull(null, Class) → @NonNull violation", () ->
                        ForTests.testNull("name", () -> new MockNps("x").receiver().receiveNull(null, String.class))
                ),

                DynamicTest.dynamicTest(pf + "ParameterReceiver.receiveNull(String, null) → @NonNull violation", () ->
                        ForTests.testNull("type", () -> new MockNps("x").receiver().receiveNull("x", null))
                )
        );
    }
}