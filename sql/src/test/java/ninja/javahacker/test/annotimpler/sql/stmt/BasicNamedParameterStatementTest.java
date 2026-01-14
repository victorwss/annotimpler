package ninja.javahacker.test.annotimpler.sql.stmt;

import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class BasicNamedParameterStatementTest {

    @FunctionalInterface
    private interface ConnectionContext {
        public void doIt(Connection con) throws Exception;
    }

    @FunctionalInterface
    private interface StatementContext {
        public void doIt(NamedParameterStatement con) throws Exception;
    }

    @FunctionalInterface
    private interface ResultSetContext {
        public void doIt(ResultSet con) throws Exception;
    }

    @FunctionalInterface
    private interface Inserter {
        public void doIt(ConnectionContext con) throws Exception;

        @SneakyThrows
        public default Executable setBasic(String sql, StatementContext recv, ResultSetContext rscv) {
            var idx = Map.of("bar", List.of(1));
            return () -> {
                this.doIt(con -> {
                    try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql, idx)) {
                        recv.doIt(ps);
                        try (var rs = ps.executeQuery()) {
                            Assertions.assertTrue(rs.next());
                            rscv.doIt(rs);
                            Assertions.assertFalse(rs.next());
                        }
                    }
                });
            };
        }

        @SneakyThrows
        public default Stream<Executable> setBasic(String sql, StatementContext recv) {
            Stream<ResultSetContext> runs = Stream.of(
                    rs -> Assertions.assertEquals(2, rs.getInt("pk")),
                    rs -> Assertions.assertEquals(2, rs.getInt(1)),
                    rs -> Assertions.assertEquals("lol", rs.getString("blah")),
                    rs -> Assertions.assertEquals("lol", rs.getString(2))
            );
            return runs.map(run -> setBasic(sql, recv, run));
        }

        public default Stream<Executable> setBasics(String sql, StatementContext... recvs) throws Exception {
            return Stream.of(recvs).flatMap(recv -> setBasic(sql, recv));
        }
    }

    private static void withConnectionSimple(ConnectionContext recv) throws Exception {
        var sqls = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4), color VARCHAR(4), onceuponatime TIMESTAMP);",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (1, 'whoa', NULL, '2024-03-04 13:14:15.456');",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (2, 'lol', 'blue', '2026-01-02 10:11:12.123');"
        );
        try (var con = H2Connector.std().withMemory(true).get()) {
            for (var sql : sqls) {
                try (var ps = con.prepareStatement(sql)) {
                    ps.executeUpdate();
                }
            }
            recv.doIt(con);
        }
    }

    @Test
    public void testSetBoolean() throws Exception {
        Inserter ins = BasicNamedParameterStatementTest::withConnectionSimple;
        var a = ins.setBasics(
                "SELECT pk, blah FROM foo WHERE pk = (CASE WHEN ? THEN 2 ELSE 1 END)",
                ps -> ps.setBoolean("bar", true), ps -> ps.setBoolean(1, true)
        );
        var b = ins.setBasics(
                "SELECT pk, blah FROM foo WHERE pk = (CASE WHEN ? THEN 1 ELSE 2 END)",
                ps -> ps.setBoolean("bar", false), ps -> ps.setBoolean(1, false)
        );
        var tests = Stream.of(a, b).flatMap(x -> x);
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testIntegers() throws Exception {
        Inserter ins = BasicNamedParameterStatementTest::withConnectionSimple;
        var tests = ins.setBasics(
                "SELECT pk, blah FROM foo WHERE pk = ?",
                ps -> ps.setByte("bar", (byte) 2), ps -> ps.setByte(1, (byte) 2),
                ps -> ps.setShort("bar", (short) 2), ps -> ps.setShort(1, (short) 2),
                ps -> ps.setInt("bar", 2), ps -> ps.setInt(1, 2),
                ps -> ps.setLong("bar", 2), ps -> ps.setLong(1, 2)
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testFloats() throws Exception {
        Inserter ins = BasicNamedParameterStatementTest::withConnectionSimple;
        var oneAndHalf = BigDecimal.valueOf(1.5);
        var tests = ins.setBasics(
                "SELECT pk, blah FROM foo WHERE pk > ?",
                ps -> ps.setFloat("bar", 1.5f), ps -> ps.setFloat(1, 1.5f),
                ps -> ps.setDouble("bar", 1.5), ps -> ps.setDouble(1, 1.5),
                ps -> ps.setBigDecimal("bar", oneAndHalf), ps -> ps.setBigDecimal(1, oneAndHalf)
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testString() throws Exception {
        Inserter ins = BasicNamedParameterStatementTest::withConnectionSimple;
        var tests = ins.setBasics(
                "SELECT pk, blah FROM foo WHERE color = ?",
                ps -> ps.setString("bar", "blue"), ps -> ps.setString(1, "blue")
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testDates() throws Exception {
        Inserter ins = BasicNamedParameterStatementTest::withConnectionSimple;
        var d1 = java.sql.Date.valueOf(LocalDate.of(2025, 10, 10));
        var d2 = java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 10, 10, 0, 0, 0));
        var tests = ins.setBasics(
                "SELECT pk, blah FROM foo WHERE onceuponatime > ?",
                ps -> ps.setDate("bar", d1), ps -> ps.setDate(1, d1),
                ps -> ps.setTimestamp("bar", d2), ps -> ps.setTimestamp(1, d2)
        );
        Assertions.assertAll(tests.toList());
    }
}
