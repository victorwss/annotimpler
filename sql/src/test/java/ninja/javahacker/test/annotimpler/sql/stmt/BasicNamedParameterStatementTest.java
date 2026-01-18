package ninja.javahacker.test.annotimpler.sql.stmt;

import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class BasicNamedParameterStatementTest {

    @FunctionalInterface
    private interface ConnectionContext {
        public void doIt(Connection con) throws Exception;

        public default void onConnection() throws Exception {
            try (var con = H2Connector.std().withMemory(true).get()) {
                this.doIt(con);
            }
        }

        public default org.junit.jupiter.api.function.Executable wrap() {
            return this::onConnection;
        }
    }

    @FunctionalInterface
    private interface StatementContext {
        public void doIt(NamedParameterStatement con) throws Exception;
    }

    @FunctionalInterface
    private interface ResultSetContext {
        public void doIt(ResultSet con) throws Exception;
    }

    private static Executable singleLineApply(
            List<String> prepare,
            String sql,
            StatementContext recv,
            ResultSetContext rscv,
            Map<String, List<Integer>> idx)
    {
        ConnectionContext ctx = con -> {
            for (var sqlp : prepare) {
                try (var ps = con.prepareStatement(sqlp)) {
                    ps.executeUpdate();
                }
            }
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql, idx)) {
                recv.doIt(ps);
                try (var rs = ps.executeQuery()) {
                    Assertions.assertTrue(rs.next(), "Assert has 1st line");
                    rscv.doIt(rs);
                    Assertions.assertFalse(rs.next(), "Assert had only one line");
                }
            }
        };
        return ctx.wrap();
    }

    private static Stream<Executable> applyBasicValues(String sql, StatementContext... recvs) {
        var idx = Map.of("bar", List.of(1));
        var prepare = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4), color VARCHAR(4), onceuponatime TIMESTAMP, axml VARCHAR(100));",
                "INSERT INTO foo(pk, blah, color, onceuponatime, axml) VALUES (1, 'whoa', NULL  , '2024-03-04 13:14:15.456', NULL);",
                "INSERT INTO foo(pk, blah, color, onceuponatime, axml) VALUES (2, 'lol' , 'blue', '2026-01-02 16:11:12.123', '<foo>bar</foo>');"
        );
        List<ResultSetContext> runs = List.of(
                rs -> Assertions.assertEquals(2, rs.getInt("pk")),
                rs -> Assertions.assertEquals(2, rs.getInt(1)),
                rs -> Assertions.assertEquals("lol", rs.getString("blah")),
                rs -> Assertions.assertEquals("lol", rs.getString(2))
        );
        return Stream.of(recvs).flatMap(recv -> runs.stream().map(run -> singleLineApply(prepare, sql, recv, run, idx)));
    }

    @Test
    public void testSetBoolean() throws Exception {
        var a = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk = (CASE WHEN ? THEN 2 ELSE 1 END)",
                ps -> ps.setBoolean("bar", true), ps -> ps.setBoolean(1, true)
        );
        var b = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk = (CASE WHEN ? THEN 1 ELSE 2 END)",
                ps -> ps.setBoolean("bar", false), ps -> ps.setBoolean(1, false)
        );
        var tests = Stream.of(a, b).flatMap(x -> x);
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testIntegers() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk = ?",
                ps -> ps.setByte("bar", (byte) 2), ps -> ps.setByte(1, (byte) 2),
                ps -> ps.setShort("bar", (short) 2), ps -> ps.setShort(1, (short) 2),
                ps -> ps.setInt("bar", 2), ps -> ps.setInt(1, 2),
                ps -> ps.setLong("bar", 2), ps -> ps.setLong(1, 2),
                ps -> ps.setInt("bar", OptionalInt.of(2)), ps -> ps.setInt(1, OptionalInt.of(2)),
                ps -> ps.setLong("bar", OptionalLong.of(2)), ps -> ps.setLong(1, OptionalLong.of(2))
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testFloats() throws Exception {
        var oneAndHalf = BigDecimal.valueOf(1.5);
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk > ?",
                ps -> ps.setFloat("bar", 1.5f), ps -> ps.setFloat(1, 1.5f),
                ps -> ps.setDouble("bar", 1.5), ps -> ps.setDouble(1, 1.5),
                ps -> ps.setBigDecimal("bar", oneAndHalf), ps -> ps.setBigDecimal(1, oneAndHalf)
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testString() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ?",
                ps -> ps.setString("bar", "blue"), ps -> ps.setString(1, "blue"),
                ps -> ps.setNString("bar", "blue"), ps -> ps.setNString(1, "blue")
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testByteArray() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ?",
                ps -> ps.setBytes("bar", "blue".getBytes()), ps -> ps.setBytes(1, "blue".getBytes())
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testObject() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ?",
                ps -> ps.setObject("bar", "blue".getBytes()), ps -> ps.setObject(1, "blue".getBytes()),
                ps -> ps.setObject("bar", "blue".getBytes(), Types.VARCHAR), ps -> ps.setObject(1, "blue".getBytes(), Types.VARCHAR)
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testDates() throws Exception {
        var d1 = LocalDate.of(2025, 10, 10);
        var d2 = LocalDateTime.of(2025, 10, 10, 15, 30, 0);
        var d4 = java.sql.Date.valueOf(d1);
        var d5 = java.sql.Timestamp.valueOf(d2);
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE onceuponatime > ?",
                ps -> ps.setDate("bar", d4), ps -> ps.setDate(1, d4),
                ps -> ps.setDate("bar", d4, new GregorianCalendar()), ps -> ps.setDate(1, d4, new GregorianCalendar()),
                ps -> ps.setTimestamp("bar", d5), ps -> ps.setTimestamp(1, d5),
                ps -> ps.setTimestamp("bar", d5, new GregorianCalendar()), ps -> ps.setTimestamp(1, d5, new GregorianCalendar()),
                ps -> ps.setLocalDate("bar", d1), ps -> ps.setLocalDate(1, d1),
                ps -> ps.setLocalDateTime("bar", d2), ps -> ps.setLocalDateTime(1, d2)
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testTimes() throws Exception {
        var d3 = LocalTime.of(15, 30, 0);
        var d6 = java.sql.Time.valueOf(d3);
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE EXTRACT(HOUR FROM onceuponatime) > EXTRACT(HOUR FROM ?)",
                ps -> ps.setTime("bar", d6), ps -> ps.setTime(1, d6),
                ps -> ps.setTime("bar", d6, new GregorianCalendar()), ps -> ps.setTime(1, d6, new GregorianCalendar()),
                ps -> ps.setLocalTime("bar", d3), ps -> ps.setLocalTime(1, d3)
        );
        Assertions.assertAll(tests.toList());
    }

    private static InputStream i() {
        return new ByteArrayInputStream("blue".getBytes());
    }

    private static Reader r() {
        return new CharArrayReader("blue".toCharArray());
    }

    private static Blob b(PreparedStatement ps) throws SQLException {
        var blob = ps.getConnection().createBlob();
        blob.setBytes(1, "blue".getBytes());
        return blob;
    }

    private static Clob c(PreparedStatement ps) throws SQLException {
        var clob = ps.getConnection().createClob();
        clob.setString(1, "blue");
        return clob;
    }

    private static NClob n(PreparedStatement ps) throws SQLException {
        var nclob = ps.getConnection().createNClob();
        nclob.setString(1, "blue");
        return nclob;
    }

    private static java.sql.Array a(PreparedStatement ps) throws SQLException {
        return ps.getConnection().createArrayOf("VARCHAR(10)", List.of("yellow", "blue", "green").toArray());
    }

    private static SQLXML s(PreparedStatement ps) throws SQLException {
        var x = ps.getConnection().createSQLXML();
        x.setString("<foo>bar</foo>");
        return x;
    }

    @Test
    public void testInputStreamReaderClobBlob() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ?",
                ps -> ps.setAsciiStream("bar", i()), ps -> ps.setAsciiStream(1, i()),
                ps -> ps.setAsciiStream("bar", i(), 4), ps -> ps.setAsciiStream(1, i(), 4),
                ps -> ps.setAsciiStream("bar", i(), 4L), ps -> ps.setAsciiStream(1, i(), 4L),
                ps -> ps.setBinaryStream("bar", i()), ps -> ps.setBinaryStream(1, i()),
                ps -> ps.setBinaryStream("bar", i(), 4), ps -> ps.setBinaryStream(1, i(), 4),
                ps -> ps.setBinaryStream("bar", i(), 4L), ps -> ps.setBinaryStream(1, i(), 4L),
                ps -> ps.setCharacterStream("bar", r()), ps -> ps.setCharacterStream(1, r()),
                ps -> ps.setCharacterStream("bar", r(), 4), ps -> ps.setCharacterStream(1, r(), 4),
                ps -> ps.setCharacterStream("bar", r(), 4L), ps -> ps.setCharacterStream(1, r(), 4L),
                ps -> ps.setNCharacterStream("bar", r()), ps -> ps.setNCharacterStream(1, r()),
                ps -> ps.setNCharacterStream("bar", r(), 4), ps -> ps.setNCharacterStream(1, r(), 4),
                ps -> ps.setNCharacterStream("bar", r(), 4L), ps -> ps.setNCharacterStream(1, r(), 4L),
                ps -> ps.setClob("bar", c(ps)), ps -> ps.setClob(1, c(ps)),
                ps -> ps.setClob("bar", r()), ps -> ps.setClob(1, r()),
                ps -> ps.setClob("bar", r(), 4L), ps -> ps.setClob(1, r(), 4L),
                ps -> ps.setNClob("bar", n(ps)), ps -> ps.setNClob(1, n(ps)),
                ps -> ps.setNClob("bar", r()), ps -> ps.setNClob(1, r()),
                ps -> ps.setNClob("bar", r(), 4L), ps -> ps.setNClob(1, r(), 4L),
                ps -> ps.setBlob("bar", b(ps)), ps -> ps.setBlob(1, b(ps)),
                ps -> ps.setBlob("bar", i()), ps -> ps.setBlob(1, i()),
                ps -> ps.setBlob("bar", i(), 4L), ps -> ps.setBlob(1, i(), 4L)
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testArray() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ANY(?)",
                ps -> ps.setArray("bar", a(ps)), ps -> ps.setArray(1, a(ps))
        );
        Assertions.assertAll(tests.toList());
    }

    @Test
    public void testSQLXML() throws Exception {
        var tests = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE axml = ?",
                ps -> ps.setSQLXML("bar", s(ps)), ps -> ps.setSQLXML(1, s(ps))
        );
        Assertions.assertAll(tests.toList());
    }
}
