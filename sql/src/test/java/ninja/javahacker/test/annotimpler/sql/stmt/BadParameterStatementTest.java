package ninja.javahacker.test.annotimpler.sql.stmt;

import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

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

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    private static Executable ex(StatementContext ctx) {
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

    private static void testBad(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt);
        Assertions.assertEquals("Parameter not found: " + paramName, ex.getMessage());
    }

    private static void testBad(int paramIdx, Executable runIt) {
        var ex = Assertions.assertThrows(SQLException.class, runIt);
        Assertions.assertEquals("Invalid value \"" + paramIdx + "\" for parameter \"parameterIndex\" [90008-240]", ex.getMessage());
    }

    private static void testUnsupported(String what, Executable runIt) {
        var ex = Assertions.assertThrows(SQLException.class, runIt);
        Assertions.assertEquals("Feature not supported: \"" + what + "\" [50100-240]", ex.getMessage());
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testSetNameNulls() throws Exception {
        var ld = LocalDate.of(2026, 1, 1);
        var lt = LocalTime.of(10, 0, 0);
        var ldt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        var d = java.sql.Date.valueOf(ld);
        var t = java.sql.Time.valueOf(lt);
        var ts = java.sql.Timestamp.valueOf(ldt);
        var c = GregorianCalendar.from(ldt.atZone(ZoneOffset.UTC));
        var str = String.class.getName();
        var url = new URI("http://0.0.0.0/").toURL();
        var ret = Stream.<NamedTest>of(
                n("setBoolean-false"    , () -> ForTests.testNull("name", ex(ps -> ps.setBoolean      (null, false                  )))),
                n("setBoolean-true"     , () -> ForTests.testNull("name", ex(ps -> ps.setBoolean      (null, true                   )))),
                n("setByte"             , () -> ForTests.testNull("name", ex(ps -> ps.setByte         (null, (byte ) 123            )))),
                n("setShort"            , () -> ForTests.testNull("name", ex(ps -> ps.setShort        (null, (short) 123            )))),
                n("setInt"              , () -> ForTests.testNull("name", ex(ps -> ps.setInt          (null, 123                    )))),
                n("setOptInt"           , () -> ForTests.testNull("name", ex(ps -> ps.setInt          (null, OptionalInt.of(123)    )))),
                n("setLong"             , () -> ForTests.testNull("name", ex(ps -> ps.setLong         (null, 123L                   )))),
                n("setOptLong"          , () -> ForTests.testNull("name", ex(ps -> ps.setLong         (null, OptionalLong.of(123)   )))),
                n("setFloat"            , () -> ForTests.testNull("name", ex(ps -> ps.setFloat        (null, 123F                   )))),
                n("setDouble"           , () -> ForTests.testNull("name", ex(ps -> ps.setDouble       (null, 123D                   )))),
                n("setOptDouble"        , () -> ForTests.testNull("name", ex(ps -> ps.setDouble       (null, OptionalDouble.of(123D))))),
                n("setBigDecimal"       , () -> ForTests.testNull("name", ex(ps -> ps.setBigDecimal   (null, BigDecimal.TWO         )))),
                n("setString"           , () -> ForTests.testNull("name", ex(ps -> ps.setString       (null, "foo"                  )))),
                n("setNString"          , () -> ForTests.testNull("name", ex(ps -> ps.setNString      (null, "foo"                  )))),
                n("setBlob-Blob"        , () -> ForTests.testNull("name", ex(ps -> ps.setBytes        (null, "foo".getBytes()       )))),
                n("setURL"              , () -> ForTests.testNull("name", ex(ps -> ps.setURL          (null, url                    )))),
                n("setRef"              , () -> ForTests.testNull("name", ex(ps -> ps.setRef          (null, ref()                  )))),
                n("setRowId"            , () -> ForTests.testNull("name", ex(ps -> ps.setRowId        (null, rowid()                )))),
                n("setLocalDate"        , () -> ForTests.testNull("name", ex(ps -> ps.setLocalDate    (null, ld                     )))),
                n("setLocalTime"        , () -> ForTests.testNull("name", ex(ps -> ps.setLocalTime    (null, lt                     )))),
                n("setLocalDateTime"    , () -> ForTests.testNull("name", ex(ps -> ps.setLocalDateTime(null, ldt                    )))),
                n("setDate"             , () -> ForTests.testNull("name", ex(ps -> ps.setDate         (null, d                      )))),
                n("setTime"             , () -> ForTests.testNull("name", ex(ps -> ps.setTime         (null, t                      )))),
                n("setTimestamp"        , () -> ForTests.testNull("name", ex(ps -> ps.setTimestamp    (null, ts                     )))),
                n("setDateCalendar"     , () -> ForTests.testNull("name", ex(ps -> ps.setDate         (null, d , c                  )))),
                n("setTimeCalendar"     , () -> ForTests.testNull("name", ex(ps -> ps.setTime         (null, t , c                  )))),
                n("setTimestampCalendar", () -> ForTests.testNull("name", ex(ps -> ps.setTimestamp    (null, ts, c                  )))),
                n("setNull"             , () -> ForTests.testNull("name", ex(ps -> ps.setNull         (null, Types.VARCHAR          )))),
                n("setNull-2"           , () -> ForTests.testNull("name", ex(ps -> ps.setNull         (null, Types.VARCHAR, str     ))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSetNameNulls {0}")
    public void testSetNameNulls(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testSetParamNulls() {
        var ret = Stream.<NamedTest>of(
                n("setOptInt-int"      , () -> ForTests.testNull("x", ex(ps -> ps.setInt   (1  , null)))),
                n("setOptInt-String"   , () -> ForTests.testNull("x", ex(ps -> ps.setInt   ("a", null)))),
                n("setOptLong-int"     , () -> ForTests.testNull("x", ex(ps -> ps.setLong  (1  , null)))),
                n("setOptLong-String"  , () -> ForTests.testNull("x", ex(ps -> ps.setLong  ("a", null)))),
                n("setOptDouble-int"   , () -> ForTests.testNull("x", ex(ps -> ps.setDouble(1  , null)))),
                n("setOptDouble-String", () -> ForTests.testNull("x", ex(ps -> ps.setDouble("a", null))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSetParamNulls {0}")
    public void testSetParamNulls(String name, Executable s) throws Throwable {
        s.execute();
    }

    private static Stream<Arguments> testSimpleEmpties() {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        var ret = Stream.<NamedTest>of(
                n("setBoolean-false",() -> testBad("", ex(ps -> ps.setBoolean      ("", false                             )))),
                n("setBoolean-true" ,() -> testBad("", ex(ps -> ps.setBoolean      ("", true                              )))),
                n("setByte"         ,() -> testBad("", ex(ps -> ps.setByte         ("", (byte ) 123                       )))),
                n("setShort"        ,() -> testBad("", ex(ps -> ps.setShort        ("", (short) 123                       )))),
                n("setInt"          ,() -> testBad("", ex(ps -> ps.setInt          ("", 123                               )))),
                n("setOptInt"       ,() -> testBad("", ex(ps -> ps.setInt          ("", OptionalInt.of(123)               )))),
                n("setLong"         ,() -> testBad("", ex(ps -> ps.setLong         ("", 123L                              )))),
                n("setOptLong"      ,() -> testBad("", ex(ps -> ps.setLong         ("", OptionalLong.of(123)              )))),
                n("setFloat"        ,() -> testBad("", ex(ps -> ps.setFloat        ("", 123F                              )))),
                n("setDouble"       ,() -> testBad("", ex(ps -> ps.setDouble       ("", 123D                              )))),
                n("setOptDouble"    ,() -> testBad("", ex(ps -> ps.setDouble       ("", OptionalDouble.of(123D)           )))),
                n("setBigDecimal"   ,() -> testBad("", ex(ps -> ps.setBigDecimal   ("", BigDecimal.TWO                    )))),
                n("setString"       ,() -> testBad("", ex(ps -> ps.setString       ("", "foo"                             )))),
                n("setNString"      ,() -> testBad("", ex(ps -> ps.setNString      ("", "foo"                             )))),
                n("setBlob-Blob"    ,() -> testBad("", ex(ps -> ps.setBytes        ("", "foo".getBytes()                  )))),
                n("setURL"          ,() -> testBad("", ex(ps -> ps.setURL          ("", new URI("http://0.0.0.0/").toURL())))),
                n("setRef"          ,() -> testBad("", ex(ps -> ps.setRef          ("", ref()                             )))),
                n("setRowId"        ,() -> testBad("", ex(ps -> ps.setRowId        ("", rowid()                           )))),
                n("setLocalDate"    ,() -> testBad("", ex(ps -> ps.setLocalDate    ("", LocalDate.of(2026, 1, 1)          )))),
                n("setLocalTime"    ,() -> testBad("", ex(ps -> ps.setLocalTime    ("", LocalTime.of(10, 0, 0)            )))),
                n("setLocalDateTime",() -> testBad("", ex(ps -> ps.setLocalDateTime("", d                                 )))),
                n("setNull"         ,() -> testBad("", ex(ps -> ps.setNull         ("", Types.VARCHAR                     )))),
                n("setNull-2"       ,() -> testBad("", ex(ps -> ps.setNull         ("", Types.VARCHAR, "foo"              ))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimpleEmpties {0}")
    public void testSimpleEmpties(String name, Executable s) throws Throwable {
        s.execute();
    }

    private static Stream<Arguments> testSimpleBads() {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        var ret = Stream.<NamedTest>of(
                n("setBoolean-false", () -> testBad("foo", ex(ps -> ps.setBoolean      ("foo", false                             )))),
                n("setBoolean-true" , () -> testBad("foo", ex(ps -> ps.setBoolean      ("foo", true                              )))),
                n("setByte"         , () -> testBad("foo", ex(ps -> ps.setByte         ("foo", (byte ) 123                       )))),
                n("setShort"        , () -> testBad("foo", ex(ps -> ps.setShort        ("foo", (short) 123                       )))),
                n("setInt"          , () -> testBad("foo", ex(ps -> ps.setInt          ("foo", 123                               )))),
                n("setOptInt"       , () -> testBad("foo", ex(ps -> ps.setInt          ("foo", OptionalInt.of(123)               )))),
                n("setLong"         , () -> testBad("foo", ex(ps -> ps.setLong         ("foo", 123L                              )))),
                n("setOptLong"      , () -> testBad("foo", ex(ps -> ps.setLong         ("foo", OptionalLong.of(123)              )))),
                n("setFloat"        , () -> testBad("foo", ex(ps -> ps.setFloat        ("foo", 123F                              )))),
                n("setDouble"       , () -> testBad("foo", ex(ps -> ps.setDouble       ("foo", 123D                              )))),
                n("setOptDouble"    , () -> testBad("foo", ex(ps -> ps.setDouble       ("foo", OptionalDouble.of(123D)           )))),
                n("setBigDecimal"   , () -> testBad("foo", ex(ps -> ps.setBigDecimal   ("foo", BigDecimal.TWO                    )))),
                n("setString"       , () -> testBad("foo", ex(ps -> ps.setString       ("foo", "foo"                             )))),
                n("setNString"      , () -> testBad("foo", ex(ps -> ps.setNString      ("foo", "foo"                             )))),
                n("setBlob-Blob"    , () -> testBad("foo", ex(ps -> ps.setBytes        ("foo", "foo".getBytes()                  )))),
                n("setURL"          , () -> testBad("foo", ex(ps -> ps.setURL          ("foo", new URI("http://0.0.0.0/").toURL())))),
                n("setRef"          , () -> testBad("foo", ex(ps -> ps.setRef          ("foo", ref()                             )))),
                n("setRowId"        , () -> testBad("foo", ex(ps -> ps.setRowId        ("foo", rowid()                           )))),
                n("setLocalDate"    , () -> testBad("foo", ex(ps -> ps.setLocalDate    ("foo", LocalDate.of(2026, 1, 1)          )))),
                n("setLocalTime"    , () -> testBad("foo", ex(ps -> ps.setLocalTime    ("foo", LocalTime.of(10, 0, 0)            )))),
                n("setLocalDateTime", () -> testBad("foo", ex(ps -> ps.setLocalDateTime("foo", d                                 )))),
                n("setNull"         , () -> testBad("foo", ex(ps -> ps.setNull         ("foo", Types.VARCHAR                     )))),
                n("setNull-2"       , () -> testBad("foo", ex(ps -> ps.setNull         ("foo", Types.VARCHAR, "foo"              ))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimpleBads {0}")
    public void testSimpleBads(String name, Executable s) throws Throwable {
        s.execute();
    }

    private static Stream<Arguments> testSimpleBadIndexes() {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        var ret = Stream.of(0, -1, 2).flatMap(idx -> {
            Stream<NamedTest> s = Stream.of(
                    n("setBoolean-false", () -> testBad(idx, ex(ps -> ps.setBoolean      (idx, false                   )))),
                    n("setBoolean-true" , () -> testBad(idx, ex(ps -> ps.setBoolean      (idx, true                    )))),
                    n("setByte"         , () -> testBad(idx, ex(ps -> ps.setByte         (idx, (byte ) 123             )))),
                    n("setShort"        , () -> testBad(idx, ex(ps -> ps.setShort        (idx, (short) 123             )))),
                    n("setInt"          , () -> testBad(idx, ex(ps -> ps.setInt          (idx, 123                     )))),
                    n("setOptInt"       , () -> testBad(idx, ex(ps -> ps.setInt          (idx, OptionalInt.of(123)     )))),
                    n("setLong"         , () -> testBad(idx, ex(ps -> ps.setLong         (idx, 123L                    )))),
                    n("setOptLong"      , () -> testBad(idx, ex(ps -> ps.setLong         (idx, OptionalLong.of(123)    )))),
                    n("setFloat"        , () -> testBad(idx, ex(ps -> ps.setFloat        (idx, 123F                    )))),
                    n("setDouble"       , () -> testBad(idx, ex(ps -> ps.setDouble       (idx, 123D                    )))),
                    n("setOptDouble"    , () -> testBad(idx, ex(ps -> ps.setDouble       (idx, OptionalDouble.of(123D) )))),
                    n("setBigDecimal"   , () -> testBad(idx, ex(ps -> ps.setBigDecimal   (idx, BigDecimal.TWO          )))),
                    n("setString"       , () -> testBad(idx, ex(ps -> ps.setString       (idx, "foo"                   )))),
                    n("setNString"      , () -> testBad(idx, ex(ps -> ps.setNString      (idx, "foo"                   )))),
                    n("setBlob-Blob"    , () -> testBad(idx, ex(ps -> ps.setBytes        (idx, "foo".getBytes()        )))),
                    n("setLocalDate"    , () -> testBad(idx, ex(ps -> ps.setLocalDate    (idx, LocalDate.of(2026, 1, 1))))),
                    n("setLocalTime"    , () -> testBad(idx, ex(ps -> ps.setLocalTime    (idx, LocalTime.of(10, 0, 0)  )))),
                    n("setLocalDateTime", () -> testBad(idx, ex(ps -> ps.setLocalDateTime(idx, d                       )))),
                    n("setNull"         , () -> testBad(idx, ex(ps -> ps.setNull         (idx, Types.VARCHAR           )))),
                    n("setNull-2"       , () -> testBad(idx, ex(ps -> ps.setNull         (idx, Types.VARCHAR, "foo"    ))))
            );
            return s;
        });
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSimpleBadIndexes {0}")
    public void testSimpleBadIndexes(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings({"null", "deprecation"})
    private static Stream<Arguments> testInputStreamReaderBlobClobNulls() {
        var ret = Stream.<NamedTest>of(
                n("setAsciiStream"          , () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream     (null, i()    )))),
                n("setAsciiStream-int"      , () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream     (null, i(), 0 )))),
                n("setAsciiStream-long"     , () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream     (null, i(), 0L)))),
                n("setBinaryStream"         , () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream    (null, i()    )))),
                n("setBinaryStream-int"     , () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream    (null, i(), 0 )))),
                n("setBinaryStream-long"    , () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream    (null, i(), 0L)))),
                n("setCharacterStream"      , () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream (null, r()    )))),
                n("setCharacterStream-int"  , () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream (null, r(), 0 )))),
                n("setCharacterStream-long" , () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream (null, r(), 0L)))),
                n("setNCharacterStream"     , () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r()    )))),
                n("setNCharacterStream-int" , () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r(), 0 )))),
                n("setNCharacterStream-long", () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r(), 0L)))),
                n("setBlob-Blob"            , () -> ForTests.testNull("name", ex(ps -> ps.setBlob            (null, b(ps)  )))),
                n("setBlob-InputStream"     , () -> ForTests.testNull("name", ex(ps -> ps.setBlob            (null, i()    )))),
                n("setBlob-InputStream-int" , () -> ForTests.testNull("name", ex(ps -> ps.setBlob            (null, i(), 0L)))),
                n("setClob-Clob"            , () -> ForTests.testNull("name", ex(ps -> ps.setClob            (null, c(ps)  )))),
                n("setClob-Reader"          , () -> ForTests.testNull("name", ex(ps -> ps.setClob            (null, r()    )))),
                n("setClob-Reader-int"      , () -> ForTests.testNull("name", ex(ps -> ps.setClob            (null, r(), 0L)))),
                n("setNClob-NClob"          , () -> ForTests.testNull("name", ex(ps -> ps.setNClob           (null, n(ps)  )))),
                n("setNClob-Reader"         , () -> ForTests.testNull("name", ex(ps -> ps.setNClob           (null, r()    )))),
                n("setNClob-Reader-int"     , () -> ForTests.testNull("name", ex(ps -> ps.setNClob           (null, r(), 0L)))),
                n("setUnicodeStream"        , () -> ForTests.testNull("name", ex(ps -> ps.setUnicodeStream   (null, i(), 0 ))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testInputStreamReaderBlobClobNulls {0}")
    public void testInputStreamReaderBlobClobNulls(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> testInputStreamReaderBlobClobEmpties() {
        var ret = Stream.<NamedTest>of(
                n("setAsciiStream"         , () -> testBad("", ex(ps -> ps.setAsciiStream     ("", i()    )))),
                n("setBinaryStream"        , () -> testBad("", ex(ps -> ps.setBinaryStream    ("", i()    )))),
                n("setCharacterStream"     , () -> testBad("", ex(ps -> ps.setCharacterStream ("", r()    )))),
                n("setNCharacterStream"    , () -> testBad("", ex(ps -> ps.setNCharacterStream("", r()    )))),
                n("setBlob-Blob"           , () -> testBad("", ex(ps -> ps.setBlob            ("", b(ps)  )))),
                n("setBlob-InputStream"    , () -> testBad("", ex(ps -> ps.setBlob            ("", i()    )))),
                n("setBlob-InputStream-int", () -> testBad("", ex(ps -> ps.setBlob            ("", i(), 0L)))),
                n("setClob-Clob"           , () -> testBad("", ex(ps -> ps.setClob            ("", c(ps)  )))),
                n("setClob-Reader"         , () -> testBad("", ex(ps -> ps.setClob            ("", r()    )))),
                n("setClob-Reader-int"     , () -> testBad("", ex(ps -> ps.setClob            ("", r(), 0L)))),
                n("setNClob-NClob"         , () -> testBad("", ex(ps -> ps.setNClob           ("", n(ps)  )))),
                n("setNClob-Reader"        , () -> testBad("", ex(ps -> ps.setNClob           ("", r()    )))),
                n("setNClob-Reader-int"    , () -> testBad("", ex(ps -> ps.setNClob           ("", r(), 0L)))),
                n("setUnicodeStream"       , () -> testBad("", ex(ps -> ps.setUnicodeStream   ("", i(), 0 ))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testInputStreamReaderBlobClobEmpties {0}")
    public void testInputStreamReaderBlobClobEmpties(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> testInputStreamReaderBlobClobBads() {
        var ret = Stream.<NamedTest>of(
                n("setAsciiStream"         , () -> testBad("foo", ex(ps -> ps.setAsciiStream     ("foo", i()    )))),
                n("setBinaryStream"        , () -> testBad("foo", ex(ps -> ps.setBinaryStream    ("foo", i()    )))),
                n("setCharacterStream"     , () -> testBad("foo", ex(ps -> ps.setCharacterStream ("foo", r()    )))),
                n("setNCharacterStream"    , () -> testBad("foo", ex(ps -> ps.setNCharacterStream("foo", r()    )))),
                n("setBlob-Blob"           , () -> testBad("foo", ex(ps -> ps.setBlob            ("foo", b(ps)  )))),
                n("setBlob-InputStream"    , () -> testBad("foo", ex(ps -> ps.setBlob            ("foo", i()    )))),
                n("setBlob-InputStream-int", () -> testBad("foo", ex(ps -> ps.setBlob            ("foo", i(), 0L)))),
                n("setClob-Clob"           , () -> testBad("foo", ex(ps -> ps.setClob            ("foo", c(ps)  )))),
                n("setClob-Reader"         , () -> testBad("foo", ex(ps -> ps.setClob            ("foo", r()    )))),
                n("setClob-Reader-int"     , () -> testBad("foo", ex(ps -> ps.setClob            ("foo", r(), 0L)))),
                n("setNClob-NClob"         , () -> testBad("foo", ex(ps -> ps.setNClob           ("foo", n(ps)  )))),
                n("setNClob-Reader"        , () -> testBad("foo", ex(ps -> ps.setNClob           ("foo", r()    )))),
                n("setNClob-Reader-int"    , () -> testBad("foo", ex(ps -> ps.setNClob           ("foo", r(), 0L)))),
                n("setUnicodeStream"       , () -> testBad("foo", ex(ps -> ps.setUnicodeStream   ("foo", i(), 0 ))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testInputStreamReaderBlobClobBads {0}")
    public void testInputStreamReaderBlobClobBads(String name, Executable s) throws Throwable {
        s.execute();
    }

    private static Stream<Arguments> testInputStreamReaderBlobClobBadIndexes() {
        var ret = Stream.of(0, -1, 2).flatMap(idx -> {
            Stream<NamedTest> s = Stream.of(
                    n("setAsciiStream"         , () -> testBad(idx, ex(ps -> ps.setAsciiStream     (idx, i()    )))),
                    n("setBinaryStream"        , () -> testBad(idx, ex(ps -> ps.setBinaryStream    (idx, i()    )))),
                    n("setCharacterStream"     , () -> testBad(idx, ex(ps -> ps.setCharacterStream (idx, r()    )))),
                    n("setNCharacterStream"    , () -> testBad(idx, ex(ps -> ps.setNCharacterStream(idx, r()    )))),
                    n("setBlob-Blob"           , () -> testBad(idx, ex(ps -> ps.setBlob            (idx, b(ps)  )))),
                    n("setBlob-InputStream"    , () -> testBad(idx, ex(ps -> ps.setBlob            (idx, i()    )))),
                    n("setBlob-InputStream-int", () -> testBad(idx, ex(ps -> ps.setBlob            (idx, i(), 0L)))),
                    n("setClob-Clob"           , () -> testBad(idx, ex(ps -> ps.setClob            (idx, c(ps)  )))),
                    n("setClob-Reader"         , () -> testBad(idx, ex(ps -> ps.setClob            (idx, r()    )))),
                    n("setClob-Reader-int"     , () -> testBad(idx, ex(ps -> ps.setClob            (idx, r(), 0L)))),
                    n("setNClob-NClob"         , () -> testBad(idx, ex(ps -> ps.setNClob           (idx, n(ps)  )))),
                    n("setNClob-Reader"        , () -> testBad(idx, ex(ps -> ps.setNClob           (idx, r()    )))),
                    n("setNClob-Reader-int"    , () -> testBad(idx, ex(ps -> ps.setNClob           (idx, r(), 0L))))
            );
            return s;
        });
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testInputStreamReaderBlobClobBadIndexes {0}")
    public void testInputStreamReaderBlobClobBadIndexes(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> testUnsupportedMethods() throws Exception {
        var url = new URI("http://0.0.0.0/").toURL();
        var ret = Stream.of(1, 0, -1, 2).flatMap(idx -> {
            Stream<NamedTest> s = Stream.of(
                    n("setUnicodeStream-null"    , () -> testUnsupported("unicodeStream", ex(ps -> ps.setUnicodeStream(idx, null, 0)))),
                    n("setUnicodeStream-instance", () -> testUnsupported("unicodeStream", ex(ps -> ps.setUnicodeStream(idx, i() , 0)))),
                    n("setURL-null"              , () -> testUnsupported("url"          , ex(ps -> ps.setURL          (idx, null   )))),
                    n("setURL-instance"          , () -> testUnsupported("url"          , ex(ps -> ps.setURL          (idx, url    )))),
                    n("setRef-null"              , () -> testUnsupported("ref"          , ex(ps -> ps.setRef          (idx, null   )))),
                    n("setRef-intsance"          , () -> testUnsupported("ref"          , ex(ps -> ps.setRef          (idx, ref()  )))),
                    n("setRowId-null"            , () -> testUnsupported("rowId"        , ex(ps -> ps.setRowId        (idx, null   )))),
                    n("setRowId-intsance"        , () -> testUnsupported("rowId"        , ex(ps -> ps.setRowId        (idx, rowid()))))
            );
            return s;
        });
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testUnsupported {0}")
    public void testUnsupportedMethods(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testArrayNullsAndBads() {
        var ret = Stream.<NamedTest>of(
                n("empty"   , () -> testBad(""   , ex(ps -> ps.setArray(""   , a(ps))))),
                n("junk"    , () -> testBad("foo", ex(ps -> ps.setArray("foo", a(ps))))),
                n("zero"    , () -> testBad(0    , ex(ps -> ps.setArray(0    , a(ps))))),
                n("negative", () -> testBad(-1   , ex(ps -> ps.setArray(-1   , a(ps))))),
                n("large"   , () -> testBad(2    , ex(ps -> ps.setArray(2    , a(ps)))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testArrayNullsAndBads {0}")
    public void testArrayNullsAndBads(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testSQLXMLNullsAndBads() {
        var ret = Stream.<NamedTest>of(
                n("empty"   , () -> testBad(""   , ex(ps -> ps.setSQLXML(""   , s(ps))))),
                n("junk"    , () -> testBad("foo", ex(ps -> ps.setSQLXML("foo", s(ps))))),
                n("zero"    , () -> testBad(0    , ex(ps -> ps.setSQLXML(0    , s(ps))))),
                n("negative", () -> testBad(-1   , ex(ps -> ps.setSQLXML(-1   , s(ps))))),
                n("large"   , () -> testBad(2    , ex(ps -> ps.setSQLXML(2    , s(ps)))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSQLXMLNullsAndBads {0}")
    public void testSQLXMLNullsAndBads(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testObjectsNulls() {
        var ret = Stream.<NamedTest>of(
                n("setArray"          , () -> ForTests.testNull("name", ex(ps -> ps.setArray (null, a(ps)                         )))),
                n("setSQLXML"         , () -> ForTests.testNull("name", ex(ps -> ps.setSQLXML(null, s(ps)                         )))),
                n("Simple-name"       , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0]                   )))),
                n("Type-name"         , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], Types .VARCHAR   )))),
                n("SQLType-name"      , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-name"   , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], Types .VARCHAR, 0)))),
                n("SQLType-Scale-name", () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], H2Type.VARCHAR, 0))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testObjectsNulls {0}")
    public void testObjectsNulls(String name, Executable s) throws Throwable {
        s.execute();
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testObjectsBads() {
        var ret = Stream.<NamedTest>of(
                n("Simple-empty"          , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0]                   )))),
                n("Type-empty"            , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], Types .VARCHAR   )))),
                n("SQLType-empty"         , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-empty"      , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], Types .VARCHAR, 0)))),
                n("SQLType-Scale-empty"   , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], H2Type.VARCHAR, 0)))),
                n("Simple-junk"           , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0]                   )))),
                n("Type-junk"             , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], Types .VARCHAR   )))),
                n("SQLType-junk"          , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-junk"       , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], Types .VARCHAR, 0)))),
                n("SQLType-Scale-junk"    , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], H2Type.VARCHAR, 0)))),
                n("Simple-zero"           , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0]                   )))),
                n("Type-zero"             , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], Types .VARCHAR   )))),
                n("SQLType-zero"          , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-zero"       , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], Types .VARCHAR, 0)))),
                n("SQLType-Scale-zero"    , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], H2Type.VARCHAR, 0)))),
                n("Simple-negative"       , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0]                   )))),
                n("Type-negative"         , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], Types .VARCHAR   )))),
                n("SQLType-negative"      , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-negative"   , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], Types .VARCHAR, 0)))),
                n("SQLType-Scale-negative", () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], H2Type.VARCHAR, 0)))),
                n("Simple-large"          , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0]                   )))),
                n("Type-large"            , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], Types .VARCHAR   )))),
                n("SQLType-large"         , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-large"      , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], Types .VARCHAR, 0)))),
                n("SQLType-Scale-large"   , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], H2Type.VARCHAR, 0))))
        );
        return ret.map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testObjectsBads {0}")
    public void testObjectsBads(String name, Executable s) throws Throwable {
        s.execute();
    }
}
