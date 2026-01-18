package ninja.javahacker.test.annotimpler.sql.stmt;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class BadParameterStatementTest {

    @FunctionalInterface
    private interface ConnectionContext {
        public void doIt(Connection con) throws Exception;

        public default void onConnection() throws Exception {
            try (var con = H2Connector.std().withMemory(true).get()) {
                this.doIt(con);
            }
        }

        public default Executable wrap() {
            return this::onConnection;
        }
    }

    @FunctionalInterface
    private interface StatementContext {
        public void doIt(NamedParameterStatement ps) throws Exception;

        public default void onConnection(List<String> sqls, Map<String, List<Integer>> idx, String lastSql) throws Exception {
            ConnectionContext ctx = con -> {
                for (var sql : sqls) {
                    try (var ps = con.prepareStatement(sql)) {
                        ps.executeUpdate();
                    }
                }
                try (var ps = NamedParameterStatement.prepareNamedStatement(con, lastSql, idx)) {
                    this.doIt(ps);
                    ps.executeUpdate();
                }
            };
            ctx.onConnection();
        }

        public default Executable wrap(List<String> sqls, Map<String, List<Integer>> idx, String lastSql) {
            return () -> this.onConnection(sqls, idx, lastSql);
        }
    }

    private Executable ex(StatementContext ctx) {
        var idx = Map.of("bar", List.of(1));
        var prepare = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4), color VARCHAR(4), onceuponatime TIMESTAMP);",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (1, 'whoa', NULL  , '2024-03-04 13:14:15.456');",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (2, 'lol' , 'blue', '2026-01-02 10:11:12.123');"
        );
        var last = "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (3, 'luz' , 'red', '2025-01-02 10:11:12.123');";
        return ctx.wrap(prepare, idx, last);
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
        return ps.getConnection().createArrayOf(String.class.getName(), new Object[] {"yellow", "green"});
    }

    private static SQLXML s(PreparedStatement ps) throws SQLException {
        var x = ps.getConnection().createSQLXML();
        x.setString("<foo>bar</foo>");
        return x;
    }

    private static Ref ref() {
        return new Ref() {
            @Override
            public String getBaseTypeName() throws SQLException {
                throw new AssertionError();
            }

            @Override
            public Object getObject(Map<String, Class<?>> map) throws SQLException {
                throw new AssertionError();
            }

            @Override
            public Object getObject() throws SQLException {
                throw new AssertionError();
            }

            @Override
            public void setObject(Object value) throws SQLException {
                throw new AssertionError();
            }
        };
    }

    private static RowId rowid() {
        return () -> {
            throw new AssertionError();
        };
    }

    private static void testBad(String paramName, Executable runIt, String testName) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt, testName);
        Assertions.assertEquals("Parameter not found: " + paramName, ex.getMessage(), testName);
    }

    private static void testBad(int paramIdx, Executable runIt, String testName) {
        var ex = Assertions.assertThrows(SQLException.class, runIt, testName);
        Assertions.assertEquals("Invalid value \"" + paramIdx + "\" for parameter \"parameterIndex\" [90008-240]", ex.getMessage(), testName);
    }

    private static void testUnsupported(String what, Executable runIt, String testName) {
        var ex = Assertions.assertThrows(SQLException.class, runIt, testName);
        Assertions.assertEquals("Feature not supported: \"" + what + "\" [50100-240]", ex.getMessage(), testName);
    }

    @Test
    @SuppressWarnings("null")
    public void testSimpleNulls() throws Exception {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        Assertions.assertAll(
                () -> ForTests.testNull("name", ex(ps -> ps.setBoolean(null, false)), "setBoolean-false"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBoolean(null, true)), "setBoolean-true"),
                () -> ForTests.testNull("name", ex(ps -> ps.setByte(null, (byte) 123)), "setByte"),
                () -> ForTests.testNull("name", ex(ps -> ps.setShort(null, (short) 123)), "setShort"),
                () -> ForTests.testNull("name", ex(ps -> ps.setInt(null, 123)), "setInt"),
                () -> ForTests.testNull("name", ex(ps -> ps.setInt(null, OptionalInt.of(123))), "setOptInt"),
                () -> ForTests.testNull("name", ex(ps -> ps.setLong(null, 123L)), "setLong"),
                () -> ForTests.testNull("name", ex(ps -> ps.setLong(null, OptionalLong.of(123))), "setOptLong"),
                () -> ForTests.testNull("name", ex(ps -> ps.setFloat(null, 123F)), "setFloat"),
                () -> ForTests.testNull("name", ex(ps -> ps.setDouble(null, 123D)), "setDouble"),
                () -> ForTests.testNull("name", ex(ps -> ps.setDouble(null, OptionalDouble.of(123D))), "setOptDouble"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBigDecimal(null, BigDecimal.TWO)), "setBigDecimal"),
                () -> ForTests.testNull("name", ex(ps -> ps.setString(null, "foo")), "setString"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNString(null, "foo")), "setNString"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBytes(null, "foo".getBytes())), "setBlob-Blob"),
                () -> ForTests.testNull("name", ex(ps -> ps.setURL(null, new URI("http://0.0.0.0/").toURL())), "setURL"),
                () -> ForTests.testNull("name", ex(ps -> ps.setRef(null, ref())), "setRef"),
                () -> ForTests.testNull("name", ex(ps -> ps.setRowId(null, rowid())), "setRowId"),
                () -> ForTests.testNull("name", ex(ps -> ps.setLocalDate(null, LocalDate.of(2026, 1, 1))), "setLocalDate"),
                () -> ForTests.testNull("name", ex(ps -> ps.setLocalTime(null, LocalTime.of(10, 0, 0))), "setLocalTime"),
                () -> ForTests.testNull("name", ex(ps -> ps.setLocalDateTime(null, d)), "setLocalDateTime"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNull(null, Types.VARCHAR)), "setNull"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNull(null, Types.VARCHAR, "foo")), "setNull-2")
        );
    }

    @Test
    public void testSimpleEmpties() throws Exception {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        Assertions.assertAll(
                () -> testBad("", ex(ps -> ps.setBoolean("", false)), "setBoolean-false"),
                () -> testBad("", ex(ps -> ps.setBoolean("", true)), "setBoolean-true"),
                () -> testBad("", ex(ps -> ps.setByte("", (byte) 123)), "setByte"),
                () -> testBad("", ex(ps -> ps.setShort("", (short) 123)), "setShort"),
                () -> testBad("", ex(ps -> ps.setInt("", 123)), "setInt"),
                () -> testBad("", ex(ps -> ps.setInt("", OptionalInt.of(123))), "setOptInt"),
                () -> testBad("", ex(ps -> ps.setLong("", 123L)), "setLong"),
                () -> testBad("", ex(ps -> ps.setLong("", OptionalLong.of(123))), "setOptLong"),
                () -> testBad("", ex(ps -> ps.setFloat("", 123F)), "setFloat"),
                () -> testBad("", ex(ps -> ps.setDouble("", 123D)), "setDouble"),
                () -> testBad("", ex(ps -> ps.setDouble("", OptionalDouble.of(123D))), "setOptDouble"),
                () -> testBad("", ex(ps -> ps.setBigDecimal("", BigDecimal.TWO)), "setBigDecimal"),
                () -> testBad("", ex(ps -> ps.setString("", "foo")), "setString"),
                () -> testBad("", ex(ps -> ps.setNString("", "foo")), "setNString"),
                () -> testBad("", ex(ps -> ps.setBytes("", "foo".getBytes())), "setBlob-Blob"),
                () -> testBad("", ex(ps -> ps.setURL("", new URI("http://0.0.0.0/").toURL())), "setURL"),
                () -> testBad("", ex(ps -> ps.setRef("", ref())), "setRef"),
                () -> testBad("", ex(ps -> ps.setRowId("", rowid())), "setRowId"),
                () -> testBad("", ex(ps -> ps.setLocalDate("", LocalDate.of(2026, 1, 1))), "setLocalDate"),
                () -> testBad("", ex(ps -> ps.setLocalTime("", LocalTime.of(10, 0, 0))), "setLocalTime"),
                () -> testBad("", ex(ps -> ps.setLocalDateTime("", d)), "setLocalDateTime"),
                () -> testBad("", ex(ps -> ps.setNull("", Types.VARCHAR)), "setNull"),
                () -> testBad("", ex(ps -> ps.setNull("", Types.VARCHAR, "foo")), "setNull-2")
        );
    }

    @Test
    public void testSimpleBads() throws Exception {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        Assertions.assertAll(
                () -> testBad("foo", ex(ps -> ps.setBoolean("foo", false)), "setBoolean-false"),
                () -> testBad("foo", ex(ps -> ps.setBoolean("foo", true)), "setBoolean-true"),
                () -> testBad("foo", ex(ps -> ps.setByte("foo", (byte) 123)), "setByte"),
                () -> testBad("foo", ex(ps -> ps.setShort("foo", (short) 123)), "setShort"),
                () -> testBad("foo", ex(ps -> ps.setInt("foo", 123)), "setInt"),
                () -> testBad("foo", ex(ps -> ps.setInt("foo", OptionalInt.of(123))), "setOptInt"),
                () -> testBad("foo", ex(ps -> ps.setLong("foo", 123L)), "setLong"),
                () -> testBad("foo", ex(ps -> ps.setLong("foo", OptionalLong.of(123))), "setOptLong"),
                () -> testBad("foo", ex(ps -> ps.setFloat("foo", 123F)), "setFloat"),
                () -> testBad("foo", ex(ps -> ps.setDouble("foo", 123D)), "setDouble"),
                () -> testBad("foo", ex(ps -> ps.setDouble("foo", OptionalDouble.of(123D))), "setOptDouble"),
                () -> testBad("foo", ex(ps -> ps.setBigDecimal("foo", BigDecimal.TWO)), "setBigDecimal"),
                () -> testBad("foo", ex(ps -> ps.setString("foo", "foo")), "setString"),
                () -> testBad("foo", ex(ps -> ps.setNString("foo", "foo")), "setNString"),
                () -> testBad("foo", ex(ps -> ps.setBytes("foo", "foo".getBytes())), "setBlob-Blob"),
                () -> testBad("foo", ex(ps -> ps.setURL("foo", new URI("http://0.0.0.0/").toURL())), "setURL"),
                () -> testBad("foo", ex(ps -> ps.setRef("foo", ref())), "setRef"),
                () -> testBad("foo", ex(ps -> ps.setRowId("foo", rowid())), "setRowId"),
                () -> testBad("foo", ex(ps -> ps.setLocalDate("foo", LocalDate.of(2026, 1, 1))), "setLocalDate"),
                () -> testBad("foo", ex(ps -> ps.setLocalTime("foo", LocalTime.of(10, 0, 0))), "setLocalTime"),
                () -> testBad("foo", ex(ps -> ps.setLocalDateTime("foo", d)), "setLocalDateTime"),
                () -> testBad("foo", ex(ps -> ps.setNull("foo", Types.VARCHAR)), "setNull"),
                () -> testBad("foo", ex(ps -> ps.setNull("foo", Types.VARCHAR, "foo")), "setNull-2")
        );
    }

    @Test
    public void testSimpleBadIndexes() throws Exception {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        var r = Stream.of(0, -1, 2).flatMap(idx -> {
            Stream<Executable> s = Stream.of(
                    () -> testBad(idx, ex(ps -> ps.setBoolean(idx, false)), "setBoolean-false"),
                    () -> testBad(idx, ex(ps -> ps.setBoolean(idx, true)), "setBoolean-true"),
                    () -> testBad(idx, ex(ps -> ps.setByte(idx, (byte) 123)), "setByte"),
                    () -> testBad(idx, ex(ps -> ps.setShort(idx, (short) 123)), "setShort"),
                    () -> testBad(idx, ex(ps -> ps.setInt(idx, 123)), "setInt"),
                    () -> testBad(idx, ex(ps -> ps.setInt(idx, OptionalInt.of(123))), "setOptInt"),
                    () -> testBad(idx, ex(ps -> ps.setLong(idx, 123L)), "setLong"),
                    () -> testBad(idx, ex(ps -> ps.setLong(idx, OptionalLong.of(123))), "setOptLong"),
                    () -> testBad(idx, ex(ps -> ps.setFloat(idx, 123F)), "setFloat"),
                    () -> testBad(idx, ex(ps -> ps.setDouble(idx, 123D)), "setDouble"),
                    () -> testBad(idx, ex(ps -> ps.setDouble(idx, OptionalDouble.of(123D))), "setOptDouble"),
                    () -> testBad(idx, ex(ps -> ps.setBigDecimal(idx, BigDecimal.TWO)), "setBigDecimal"),
                    () -> testBad(idx, ex(ps -> ps.setString(idx, "foo")), "setString"),
                    () -> testBad(idx, ex(ps -> ps.setNString(idx, "foo")), "setNString"),
                    () -> testBad(idx, ex(ps -> ps.setBytes(idx, "foo".getBytes())), "setBlob-Blob"),
                    () -> testBad(idx, ex(ps -> ps.setLocalDate(idx, LocalDate.of(2026, 1, 1))), "setLocalDate"),
                    () -> testBad(idx, ex(ps -> ps.setLocalTime(idx, LocalTime.of(10, 0, 0))), "setLocalTime"),
                    () -> testBad(idx, ex(ps -> ps.setLocalDateTime(idx, d)), "setLocalDateTime"),
                    () -> testBad(idx, ex(ps -> ps.setNull(idx, Types.VARCHAR)), "setNull"),
                    () -> testBad(idx, ex(ps -> ps.setNull(idx, Types.VARCHAR, "foo")), "setNull-2")
            );
            return s;
        });
        Assertions.assertAll(r.toList());
    }

    @Test
    @SuppressWarnings({"null", "deprecation"})
    public void testInputStreamReaderBlobClobNulls() throws Exception {
        Assertions.assertAll(
                () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream(null, i())), "setAsciiStream"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream(null, i())), "setBinaryStream"),
                () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream(null, r())), "setCharacterStream"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r())), "setNCharacterStream"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBlob(null, b(ps))), "setBlob-Blob"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBlob(null, i())), "setBlob-InputStream"),
                () -> ForTests.testNull("name", ex(ps -> ps.setBlob(null, i(), 0L)), "setBlob-InputStream-int"),
                () -> ForTests.testNull("name", ex(ps -> ps.setClob(null, c(ps))), "setClob-Clob"),
                () -> ForTests.testNull("name", ex(ps -> ps.setClob(null, r())), "setClob-Reader"),
                () -> ForTests.testNull("name", ex(ps -> ps.setClob(null, r(), 0L)), "setClob-Reader-int"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNClob(null, n(ps))), "setNClob-NClob"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNClob(null, r())), "setNClob-Reader"),
                () -> ForTests.testNull("name", ex(ps -> ps.setNClob(null, r(), 0L)), "setNClob-Reader-int"),
                () -> ForTests.testNull("name", ex(ps -> ps.setUnicodeStream(null, i(), 0)), "setUnicodeStream")
        );
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testInputStreamReaderBlobClobEmpties() throws Exception {
        Assertions.assertAll(
                () -> testBad("", ex(ps -> ps.setAsciiStream("", i())), "setAsciiStream"),
                () -> testBad("", ex(ps -> ps.setBinaryStream("", i())), "setBinaryStream"),
                () -> testBad("", ex(ps -> ps.setCharacterStream("", r())), "setCharacterStream"),
                () -> testBad("", ex(ps -> ps.setNCharacterStream("", r())), "setNCharacterStream"),
                () -> testBad("", ex(ps -> ps.setBlob("", b(ps))), "setBlob-Blob"),
                () -> testBad("", ex(ps -> ps.setBlob("", i())), "setBlob-InputStream"),
                () -> testBad("", ex(ps -> ps.setBlob("", i(), 0L)), "setBlob-InputStream-int"),
                () -> testBad("", ex(ps -> ps.setClob("", c(ps))), "setClob-Clob"),
                () -> testBad("", ex(ps -> ps.setClob("", r())), "setClob-Reader"),
                () -> testBad("", ex(ps -> ps.setClob("", r(), 0L)), "setClob-Reader-int"),
                () -> testBad("", ex(ps -> ps.setNClob("", n(ps))), "setNClob-NClob"),
                () -> testBad("", ex(ps -> ps.setNClob("", r())), "setNClob-Reader"),
                () -> testBad("", ex(ps -> ps.setNClob("", r(), 0L)), "setNClob-Reader-int"),
                () -> testBad("", ex(ps -> ps.setUnicodeStream("", i(), 0)), "setUnicodeStream")
        );
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testInputStreamReaderBlobClobBads() throws Exception {
        Assertions.assertAll(
                () -> testBad("foo", ex(ps -> ps.setAsciiStream("foo", i())), "setAsciiStream"),
                () -> testBad("foo", ex(ps -> ps.setBinaryStream("foo", i())), "setBinaryStream"),
                () -> testBad("foo", ex(ps -> ps.setCharacterStream("foo", r())), "setCharacterStream"),
                () -> testBad("foo", ex(ps -> ps.setNCharacterStream("foo", r())), "setNCharacterStream"),
                () -> testBad("foo", ex(ps -> ps.setBlob("foo", b(ps))), "setBlob-Blob"),
                () -> testBad("foo", ex(ps -> ps.setBlob("foo", i())), "setBlob-InputStream"),
                () -> testBad("foo", ex(ps -> ps.setBlob("foo", i(), 0L)), "setBlob-InputStream-int"),
                () -> testBad("foo", ex(ps -> ps.setClob("foo", c(ps))), "setClob-Clob"),
                () -> testBad("foo", ex(ps -> ps.setClob("foo", r())), "setClob-Reader"),
                () -> testBad("foo", ex(ps -> ps.setClob("foo", r(), 0L)), "setClob-Reader-int"),
                () -> testBad("foo", ex(ps -> ps.setNClob("foo", n(ps))), "setNClob-NClob"),
                () -> testBad("foo", ex(ps -> ps.setNClob("foo", r())), "setNClob-Reader"),
                () -> testBad("foo", ex(ps -> ps.setNClob("foo", r(), 0L)), "setNClob-Reader-int"),
                () -> testBad("foo", ex(ps -> ps.setUnicodeStream("foo", i(), 0)), "setUnicodeStream")
        );
    }

    @Test
    public void testInputStreamReaderBlobClobBadIndexes() throws Exception {
        var r = Stream.of(0, -1, 2).flatMap(idx -> {
            Stream<Executable> s = Stream.of(
                    () -> testBad(idx, ex(ps -> ps.setAsciiStream(idx, i())), "setAsciiStream"),
                    () -> testBad(idx, ex(ps -> ps.setBinaryStream(idx, i())), "setBinaryStream"),
                    () -> testBad(idx, ex(ps -> ps.setCharacterStream(idx, r())), "setCharacterStream"),
                    () -> testBad(idx, ex(ps -> ps.setNCharacterStream(idx, r())), "setNCharacterStream"),
                    () -> testBad(idx, ex(ps -> ps.setBlob(idx, b(ps))), "setBlob-Blob"),
                    () -> testBad(idx, ex(ps -> ps.setBlob(idx, i())), "setBlob-InputStream"),
                    () -> testBad(idx, ex(ps -> ps.setBlob(idx, i(), 0L)), "setBlob-InputStream-int"),
                    () -> testBad(idx, ex(ps -> ps.setClob(idx, c(ps))), "setClob-Clob"),
                    () -> testBad(idx, ex(ps -> ps.setClob(idx, r())), "setClob-Reader"),
                    () -> testBad(idx, ex(ps -> ps.setClob(idx, r(), 0L)), "setClob-Reader-int"),
                    () -> testBad(idx, ex(ps -> ps.setNClob(idx, n(ps))), "setNClob-NClob"),
                    () -> testBad(idx, ex(ps -> ps.setNClob(idx, r())), "setNClob-Reader"),
                    () -> testBad(idx, ex(ps -> ps.setNClob(idx, r(), 0L)), "setNClob-Reader-int")
            );
            return s;
        });
        Assertions.assertAll(r.toList());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testUnsupported() throws Exception {
        var r = Stream.of(1, 0, -1, 2).flatMap(idx -> {
            Stream<Executable> s = Stream.of(
                    () -> testUnsupported("unicodeStream", ex(ps -> ps.setUnicodeStream(idx, null, 0)), "setUnicodeStream-null"),
                    () -> testUnsupported("unicodeStream", ex(ps -> ps.setUnicodeStream(idx, i(), 0)), "setUnicodeStream-instance"),
                    () -> testUnsupported("url", ex(ps -> ps.setURL(idx, null)), "setURL-null"),
                    () -> testUnsupported("url", ex(ps -> ps.setURL(idx, new URI("http://0.0.0.0/").toURL())), "setURL-instance"),
                    () -> testUnsupported("ref", ex(ps -> ps.setRef(idx, null)), "setRef-null"),
                    () -> testUnsupported("ref", ex(ps -> ps.setRef(idx, ref())), "setRef-intsance"),
                    () -> testUnsupported("rowId", ex(ps -> ps.setRowId(idx, null)), "setRowId-null"),
                    () -> testUnsupported("rowId", ex(ps -> ps.setRowId(idx, rowid())), "setRowId-intsance")
            );
            return s;
        });
        Assertions.assertAll(r.toList());
    }

    @Test
    @SuppressWarnings("null")
    public void testArrayNullsAndBads() throws Exception {
        Assertions.assertAll(
                () -> ForTests.testNull("name", ex(ps -> ps.setArray(null, a(ps))), "setArray"),
                () -> testBad("", ex(ps -> ps.setArray("", a(ps))), "setArray"),
                () -> testBad("foo", ex(ps -> ps.setArray("foo", a(ps))), "setArray"),
                () -> testBad(0, ex(ps -> ps.setArray(0, a(ps))), "setArray"),
                () -> testBad(-1, ex(ps -> ps.setArray(-1, a(ps))), "setArray"),
                () -> testBad(2, ex(ps -> ps.setArray(2, a(ps))), "setArray")
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testSQLXMLNullsAndBads() throws Exception {
        Assertions.assertAll(
                () -> ForTests.testNull("name", ex(ps -> ps.setSQLXML(null, s(ps))), "setSQLXML"),
                () -> testBad("", ex(ps -> ps.setSQLXML("", s(ps))), "setSQLXML"),
                () -> testBad("foo", ex(ps -> ps.setSQLXML("foo", s(ps))), "setSQLXML"),
                () -> testBad(0, ex(ps -> ps.setSQLXML(0, s(ps))), "setSQLXML"),
                () -> testBad(-1, ex(ps -> ps.setSQLXML(-1, s(ps))), "setSQLXML"),
                () -> testBad(2, ex(ps -> ps.setSQLXML(2, s(ps))), "setSQLXML")
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testObjectsNullAndBads() throws Exception {
        Assertions.assertAll(
                () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0])), "Simple-name"),
                () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], Types.VARCHAR)), "Type-name"),
                () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], H2Type.VARCHAR)), "SQLType-name"),
                () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], Types.VARCHAR, 0)), "Type-Scale-name"),
                () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], H2Type.VARCHAR, 0)), "SQLType-Scale-name"),
                () -> testBad("", ex(ps -> ps.setObject("", new byte[0])), "Simple-empty"),
                () -> testBad("", ex(ps -> ps.setObject("", new byte[0], Types.VARCHAR)), "Type-empty"),
                () -> testBad("", ex(ps -> ps.setObject("", new byte[0], H2Type.VARCHAR)), "SQLType-empty"),
                () -> testBad("", ex(ps -> ps.setObject("", new byte[0], Types.VARCHAR, 0)), "Type-Scale-empty"),
                () -> testBad("", ex(ps -> ps.setObject("", new byte[0], H2Type.VARCHAR, 0)), "SQLType-Scale-empty"),
                () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0])), "Simple-junk"),
                () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], Types.VARCHAR)), "Type-junk"),
                () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], H2Type.VARCHAR)), "SQLType-junk"),
                () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], Types.VARCHAR, 0)), "Type-Scale-junk"),
                () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], H2Type.VARCHAR, 0)), "SQLType-Scale-junk"),
                () -> testBad(0, ex(ps -> ps.setObject(0, new byte[0])), "Simple-zero"),
                () -> testBad(0, ex(ps -> ps.setObject(0, new byte[0], Types.VARCHAR)), "Type-zero"),
                () -> testBad(0, ex(ps -> ps.setObject(0, new byte[0], H2Type.VARCHAR)), "SQLType-zero"),
                () -> testBad(0, ex(ps -> ps.setObject(0, new byte[0], Types.VARCHAR, 0)), "Type-Scale-zero"),
                () -> testBad(0, ex(ps -> ps.setObject(0, new byte[0], H2Type.VARCHAR, 0)), "SQLType-Scale-zero"),
                () -> testBad(-1, ex(ps -> ps.setObject(-1, new byte[0])), "Simple-negative"),
                () -> testBad(-1, ex(ps -> ps.setObject(-1, new byte[0], Types.VARCHAR)), "Type-negative"),
                () -> testBad(-1, ex(ps -> ps.setObject(-1, new byte[0], H2Type.VARCHAR)), "SQLType-negative"),
                () -> testBad(-1, ex(ps -> ps.setObject(-1, new byte[0], Types.VARCHAR, 0)), "Type-Scale-negative"),
                () -> testBad(-1, ex(ps -> ps.setObject(-1, new byte[0], H2Type.VARCHAR, 0)), "SQLType-Scale-negative"),
                () -> testBad(2, ex(ps -> ps.setObject(2, new byte[0])), "Simple-large"),
                () -> testBad(2, ex(ps -> ps.setObject(2, new byte[0], Types.VARCHAR)), "Type-large"),
                () -> testBad(2, ex(ps -> ps.setObject(2, new byte[0], H2Type.VARCHAR)), "SQLType-large"),
                () -> testBad(2, ex(ps -> ps.setObject(2, new byte[0], Types.VARCHAR, 0)), "Type-Scale-large"),
                () -> testBad(2, ex(ps -> ps.setObject(2, new byte[0], H2Type.VARCHAR, 0)), "SQLType-Scale-large")
        );
    }
}
