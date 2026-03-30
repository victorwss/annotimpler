package ninja.javahacker.test.annotimpler.sql.stmt;

import java.lang.reflect.Proxy;
import lombok.SneakyThrows;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class BadParameterStatementTest {


    private static final LocalDate LD = LocalDate.of(2026, 1, 1);
    private static final LocalTime LT = LocalTime.of(10, 0, 0);
    private static final LocalDateTime LDT = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
    private static final java.sql.Date D = java.sql.Date.valueOf(LD);
    private static final java.sql.Time T = java.sql.Time.valueOf(LT);
    private static final java.sql.Timestamp TS = java.sql.Timestamp.valueOf(LDT);
    private static final ZonedDateTime ZDT = LDT.atZone(ZoneOffset.UTC);
    private static final OffsetDateTime ODT = LDT.atOffset(ZoneOffset.UTC);
    private static final OffsetTime OT = LT.atOffset(ZoneOffset.UTC);
    private static final Instant INS = ZDT.toInstant();
    private static final GregorianCalendar GC = GregorianCalendar.from(ZDT);
    private static final URL AURL;

    static {
        try {
            AURL = new URI("http://0.0.0.0/").toURL();
        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        }
    }

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
                try (var ps = NamedParameterStatement.wrap(con.prepareStatement(lastSql), idx)) {
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

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    private static Executable ex(StatementContext ctx) {
        var idx = Map.of("bar", List.of(1));
        var prepare = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4), color VARCHAR(4), onceuponatime TIMESTAMP);",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (1, 'whoa', NULL  , '2024-03-04 13:14:15.456');",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (2, 'lol' , 'blue', '2026-01-02 10:11:12.123');"
        );
        var last = "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (3, ?, 'red', '2025-01-02 10:11:12.123');";
        return ctx.wrap(prepare, idx, last);
    }

    private static Executable ex2(StatementContext ctx) {
        var idx = Map.of("bar", List.of(1, 2));
        var prepare = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4), color VARCHAR(4), onceuponatime TIMESTAMP);",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (1, 'whoa', NULL  , '2024-03-04 13:14:15.456');",
                "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (2, 'lol' , 'blue', '2026-01-02 10:11:12.123');"
        );
        var last = "INSERT INTO foo(pk, blah, color, onceuponatime) VALUES (3, ?, ?, '2025-01-02 10:11:12.123');";
        return ctx.wrap(prepare, idx, last);
    }

    private static class LameException extends RuntimeException {
        private static final long serialVersionUID = 42L;
    }

    private static class LamerException extends Exception {
        private static final long serialVersionUID = 42L;
    }

    private static class LamestError extends Error {
        private static final long serialVersionUID = 42L;
    }

    private static InputStream i() {
        return new ByteArrayInputStream("blue".getBytes());
    }

    private static InputStream ix(int t) {
        return new InputStream() {
            @Override
            @SneakyThrows
            public int read() throws IOException {
                if (t == 1) throw new IOException("wuf");
                if (t == 2) throw new LameException();
                if (t == 3) throw new LamerException();
                throw new LamestError();
            }

            @Override
            public void close() throws IOException {
                throw new AssertionError();
            }
        };
    }

    private static Reader r() {
        return new CharArrayReader("blue".toCharArray());
    }

    private static Reader rx(int t) {
        return new Reader() {
            @Override
            @SneakyThrows
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (t == 1) throw new IOException("wuf");
                if (t == 2) throw new LameException();
                if (t == 3) throw new LamerException();
                throw new LamestError();
            }

            @Override
            public void close() throws IOException {
                throw new AssertionError();
            }
        };
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

    private static record MyStruct(String getSQLTypeName, Object[] getAttributes) implements Struct, Serializable {
        @Override
        public Object[] getAttributes(Map<String, Class<?>> map) throws SQLException {
            throw new AssertionError();
        }
    }

    private static Struct struct() {
        var attrs = new Object[] {"a", 5, 4.0};
        return new MyStruct("foo", attrs);
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

    private static void testBadIndex(int idx, Executable runIt) {
        var ex = Assertions.assertThrows(SQLException.class, runIt);
        Assertions.assertEquals("Invalid value \"" + idx + "\" for parameter \"parameterIndex\" [90008-240]", ex.getMessage());
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private static void testThrows(int t, Executable runIt) {
        var ex = Assertions.assertThrows(SQLException.class, runIt);
        switch (t) {
            case 1 -> {
                Assertions.assertEquals(IOException.class, ex.getCause().getClass());
                Assertions.assertEquals("wuf", ex.getCause().getMessage());
            }
            case 2 -> {
                var cause = ex.getCause();
                if (cause instanceof IOException) cause = cause.getCause(); // Leaking abstraction implementation detail.
                Assertions.assertEquals(LameException.class, cause.getClass());
            }
            case 3 -> {
                var cause = ex.getCause();
                if (cause instanceof IOException) cause = cause.getCause(); // Leaking abstraction implementation detail.
                Assertions.assertEquals(LamerException.class, cause.getClass());
            }
            case 4 -> {
                Assertions.assertEquals(LamestError.class, ex.getCause().getClass());
            }
            case 5 -> {
                Assertions.assertEquals(NamedParameterStatement.INPUT_STREAM_MESSAGE, ex.getMessage());
            }
            case 6 -> {
                Assertions.assertEquals(NamedParameterStatement.READER_MESSAGE, ex.getMessage());
            }
            default -> {
                throw new AssertionError();
            }
        }
    }

    @TestFactory
    @SuppressWarnings({"null", "deprecation"})
    public Stream<DynamicTest> testSetNameNulls() throws Exception {
        var str = String.class.getName();

        return Stream.of(
                n("setBoolean-false"    , () -> ForTests.testNull("name", ex(ps -> ps.setBoolean       (null, false                  )))),
                n("setBoolean-true"     , () -> ForTests.testNull("name", ex(ps -> ps.setBoolean       (null, true                   )))),
                n("setByte"             , () -> ForTests.testNull("name", ex(ps -> ps.setByte          (null, (byte ) 123            )))),
                n("setShort"            , () -> ForTests.testNull("name", ex(ps -> ps.setShort         (null, (short) 123            )))),
                n("setInt"              , () -> ForTests.testNull("name", ex(ps -> ps.setInt           (null, 123                    )))),
                n("setOptInt"           , () -> ForTests.testNull("name", ex(ps -> ps.setInt           (null, OptionalInt.of(123)    )))),
                n("setLong"             , () -> ForTests.testNull("name", ex(ps -> ps.setLong          (null, 123L                   )))),
                n("setOptLong"          , () -> ForTests.testNull("name", ex(ps -> ps.setLong          (null, OptionalLong.of(123)   )))),
                n("setFloat"            , () -> ForTests.testNull("name", ex(ps -> ps.setFloat         (null, 123F                   )))),
                n("setDouble"           , () -> ForTests.testNull("name", ex(ps -> ps.setDouble        (null, 123D                   )))),
                n("setOptDouble"        , () -> ForTests.testNull("name", ex(ps -> ps.setDouble        (null, OptionalDouble.of(123D))))),
                n("setBigDecimal"       , () -> ForTests.testNull("name", ex(ps -> ps.setBigDecimal    (null, BigDecimal.TWO         )))),
                n("setString"           , () -> ForTests.testNull("name", ex(ps -> ps.setString        (null, "foo"                  )))),
                n("setNString"          , () -> ForTests.testNull("name", ex(ps -> ps.setNString       (null, "foo"                  )))),
                n("setBlob-Blob"        , () -> ForTests.testNull("name", ex(ps -> ps.setBytes         (null, "foo".getBytes()       )))),
                n("setURL"              , () -> ForTests.testNull("name", ex(ps -> ps.setURL           (null, AURL                   )))),
                n("setRef"              , () -> ForTests.testNull("name", ex(ps -> ps.setRef           (null, ref()                  )))),
                n("setRowId"            , () -> ForTests.testNull("name", ex(ps -> ps.setRowId         (null, rowid()                )))),
                n("setStruct"           , () -> ForTests.testNull("name", ex(ps -> ps.setStruct        (null, struct()               )))),
                n("setLocalDate"        , () -> ForTests.testNull("name", ex(ps -> ps.setLocalDate     (null, LD                     )))),
                n("setLocalTime"        , () -> ForTests.testNull("name", ex(ps -> ps.setLocalTime     (null, LT                     )))),
                n("setLocalDateTime"    , () -> ForTests.testNull("name", ex(ps -> ps.setLocalDateTime (null, LDT                    )))),
                n("setOffsetDateTime"   , () -> ForTests.testNull("name", ex(ps -> ps.setOffsetDateTime(null, ODT                    )))),
                n("setZonedDateTime"    , () -> ForTests.testNull("name", ex(ps -> ps.setZonedDateTime (null, ZDT                    )))),
                n("setOffsetTime"       , () -> ForTests.testNull("name", ex(ps -> ps.setOffsetTime    (null, OT                     )))),
                n("setInstant"          , () -> ForTests.testNull("name", ex(ps -> ps.setInstant       (null, INS                    )))),
                n("setDate"             , () -> ForTests.testNull("name", ex(ps -> ps.setDate          (null, D                      )))),
                n("setTime"             , () -> ForTests.testNull("name", ex(ps -> ps.setTime          (null, T                      )))),
                n("setTimestamp"        , () -> ForTests.testNull("name", ex(ps -> ps.setTimestamp     (null, TS                     )))),
                n("setDateCalendar"     , () -> ForTests.testNull("name", ex(ps -> ps.setDate          (null, D , GC                 )))),
                n("setTimeCalendar"     , () -> ForTests.testNull("name", ex(ps -> ps.setTime          (null, T , GC                 )))),
                n("setTimestampCalendar", () -> ForTests.testNull("name", ex(ps -> ps.setTimestamp     (null, TS, GC                 )))),
                n("setArray"            , () -> ForTests.testNull("name", ex(ps -> ps.setArray         (null, a(ps)                  )))),
                n("setSQLXML"           , () -> ForTests.testNull("name", ex(ps -> ps.setSQLXML        (null, s(ps)                  )))),
                n("setNull"             , () -> ForTests.testNull("name", ex(ps -> ps.setNull          (null, Types.VARCHAR          )))),
                n("setNull-2"           , () -> ForTests.testNull("name", ex(ps -> ps.setNull          (null, Types.VARCHAR, str     ))))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testSetParamNulls() {
        return Stream.of(
                n("setOptInt-int"      , () -> ForTests.testNull("x", ex(ps -> ps.setInt   (1  , null)))),
                n("setOptInt-String"   , () -> ForTests.testNull("x", ex(ps -> ps.setInt   ("a", null)))),
                n("setOptLong-int"     , () -> ForTests.testNull("x", ex(ps -> ps.setLong  (1  , null)))),
                n("setOptLong-String"  , () -> ForTests.testNull("x", ex(ps -> ps.setLong  ("a", null)))),
                n("setOptDouble-int"   , () -> ForTests.testNull("x", ex(ps -> ps.setDouble(1  , null)))),
                n("setOptDouble-String", () -> ForTests.testNull("x", ex(ps -> ps.setDouble("a", null))))
        );
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testSimpleEmpties() {
        var d = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        return Stream.of(
                n("setBoolean-false"    , () -> testBad("", ex(ps -> ps.setBoolean       ("", false                  )))),
                n("setBoolean-true"     , () -> testBad("", ex(ps -> ps.setBoolean       ("", true                   )))),
                n("setByte"             , () -> testBad("", ex(ps -> ps.setByte          ("", (byte ) 123            )))),
                n("setShort"            , () -> testBad("", ex(ps -> ps.setShort         ("", (short) 123            )))),
                n("setInt"              , () -> testBad("", ex(ps -> ps.setInt           ("", 123                    )))),
                n("setOptInt"           , () -> testBad("", ex(ps -> ps.setInt           ("", OptionalInt.of(123)    )))),
                n("setLong"             , () -> testBad("", ex(ps -> ps.setLong          ("", 123L                   )))),
                n("setOptLong"          , () -> testBad("", ex(ps -> ps.setLong          ("", OptionalLong.of(123)   )))),
                n("setFloat"            , () -> testBad("", ex(ps -> ps.setFloat         ("", 123F                   )))),
                n("setDouble"           , () -> testBad("", ex(ps -> ps.setDouble        ("", 123D                   )))),
                n("setOptDouble"        , () -> testBad("", ex(ps -> ps.setDouble        ("", OptionalDouble.of(123D))))),
                n("setBigDecimal"       , () -> testBad("", ex(ps -> ps.setBigDecimal    ("", BigDecimal.TWO         )))),
                n("setString"           , () -> testBad("", ex(ps -> ps.setString        ("", "foo"                  )))),
                n("setNString"          , () -> testBad("", ex(ps -> ps.setNString       ("", "foo"                  )))),
                n("setBlob-Blob"        , () -> testBad("", ex(ps -> ps.setBytes         ("", "foo".getBytes()       )))),
                n("setURL"              , () -> testBad("", ex(ps -> ps.setURL           ("", AURL                   )))),
                n("setRef"              , () -> testBad("", ex(ps -> ps.setRef           ("", ref()                  )))),
                n("setRowId"            , () -> testBad("", ex(ps -> ps.setRowId         ("", rowid()                )))),
                n("setStruct"           , () -> testBad("", ex(ps -> ps.setStruct        ("", struct()               )))),
                n("setLocalDate"        , () -> testBad("", ex(ps -> ps.setLocalDate     ("", LD                     )))),
                n("setLocalTime"        , () -> testBad("", ex(ps -> ps.setLocalTime     ("", LT                     )))),
                n("setLocalDateTime"    , () -> testBad("", ex(ps -> ps.setLocalDateTime ("", LDT                    )))),
                n("setOffsetDateTime"   , () -> testBad("", ex(ps -> ps.setOffsetDateTime("", ODT                    )))),
                n("setZonedDateTime"    , () -> testBad("", ex(ps -> ps.setZonedDateTime ("", ZDT                    )))),
                n("setOffsetTime"       , () -> testBad("", ex(ps -> ps.setOffsetTime    ("", OT                     )))),
                n("setInstant"          , () -> testBad("", ex(ps -> ps.setInstant       ("", INS                    )))),
                n("setDate"             , () -> testBad("", ex(ps -> ps.setDate          ("", D                      )))),
                n("setTime"             , () -> testBad("", ex(ps -> ps.setTime          ("", T                      )))),
                n("setTimestamp"        , () -> testBad("", ex(ps -> ps.setTimestamp     ("", TS                     )))),
                n("setDateCalendar"     , () -> testBad("", ex(ps -> ps.setDate          ("", D , GC                 )))),
                n("setTimeCalendar"     , () -> testBad("", ex(ps -> ps.setTime          ("", T , GC                 )))),
                n("setTimestampCalendar", () -> testBad("", ex(ps -> ps.setTimestamp     ("", TS, GC                 )))),
                n("setArray"            , () -> testBad("", ex(ps -> ps.setArray         ("", a(ps)                  )))),
                n("setSQLXML"           , () -> testBad("", ex(ps -> ps.setSQLXML        ("", s(ps)                  )))),
                n("setNull"             , () -> testBad("", ex(ps -> ps.setNull          ("", Types.VARCHAR          )))),
                n("setNull-2"           , () -> testBad("", ex(ps -> ps.setNull          ("", Types.VARCHAR, "foo"   ))))
        );
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testSimpleBads() {
        return Stream.of(
                n("setBoolean-false"    , () -> testBad("foo", ex(ps -> ps.setBoolean       ("foo", false                  )))),
                n("setBoolean-true"     , () -> testBad("foo", ex(ps -> ps.setBoolean       ("foo", true                   )))),
                n("setByte"             , () -> testBad("foo", ex(ps -> ps.setByte          ("foo", (byte ) 123            )))),
                n("setShort"            , () -> testBad("foo", ex(ps -> ps.setShort         ("foo", (short) 123            )))),
                n("setInt"              , () -> testBad("foo", ex(ps -> ps.setInt           ("foo", 123                    )))),
                n("setOptInt"           , () -> testBad("foo", ex(ps -> ps.setInt           ("foo", OptionalInt.of(123)    )))),
                n("setLong"             , () -> testBad("foo", ex(ps -> ps.setLong          ("foo", 123L                   )))),
                n("setOptLong"          , () -> testBad("foo", ex(ps -> ps.setLong          ("foo", OptionalLong.of(123)   )))),
                n("setFloat"            , () -> testBad("foo", ex(ps -> ps.setFloat         ("foo", 123F                   )))),
                n("setDouble"           , () -> testBad("foo", ex(ps -> ps.setDouble        ("foo", 123D                   )))),
                n("setOptDouble"        , () -> testBad("foo", ex(ps -> ps.setDouble        ("foo", OptionalDouble.of(123D))))),
                n("setBigDecimal"       , () -> testBad("foo", ex(ps -> ps.setBigDecimal    ("foo", BigDecimal.TWO         )))),
                n("setString"           , () -> testBad("foo", ex(ps -> ps.setString        ("foo", "foo"                  )))),
                n("setNString"          , () -> testBad("foo", ex(ps -> ps.setNString       ("foo", "foo"                  )))),
                n("setBlob-Blob"        , () -> testBad("foo", ex(ps -> ps.setBytes         ("foo", "foo".getBytes()       )))),
                n("setURL"              , () -> testBad("foo", ex(ps -> ps.setURL           ("foo", AURL                   )))),
                n("setRef"              , () -> testBad("foo", ex(ps -> ps.setRef           ("foo", ref()                  )))),
                n("setRowId"            , () -> testBad("foo", ex(ps -> ps.setRowId         ("foo", rowid()                )))),
                n("setStruct"           , () -> testBad("foo", ex(ps -> ps.setStruct        ("foo", struct()               )))),
                n("setLocalDate"        , () -> testBad("foo", ex(ps -> ps.setLocalDate     ("foo", LD                     )))),
                n("setLocalTime"        , () -> testBad("foo", ex(ps -> ps.setLocalTime     ("foo", LT                     )))),
                n("setLocalDateTime"    , () -> testBad("foo", ex(ps -> ps.setLocalDateTime ("foo", LDT                    )))),
                n("setOffsetTime"       , () -> testBad("foo", ex(ps -> ps.setOffsetTime    ("foo", OT                     )))),
                n("setOffsetDateTime"   , () -> testBad("foo", ex(ps -> ps.setOffsetDateTime("foo", ODT                    )))),
                n("setZonedDateTime"    , () -> testBad("foo", ex(ps -> ps.setZonedDateTime ("foo", ZDT                    )))),
                n("setInstant-"         , () -> testBad("foo", ex(ps -> ps.setInstant       ("foo", INS                    )))),
                n("setDate"             , () -> testBad("foo", ex(ps -> ps.setDate          ("foo", D                      )))),
                n("setTime"             , () -> testBad("foo", ex(ps -> ps.setTime          ("foo", T                      )))),
                n("setTimestamp"        , () -> testBad("foo", ex(ps -> ps.setTimestamp     ("foo", TS                     )))),
                n("setDateCalendar"     , () -> testBad("foo", ex(ps -> ps.setDate          ("foo", D , GC                 )))),
                n("setTimeCalendar"     , () -> testBad("foo", ex(ps -> ps.setTime          ("foo", T , GC                 )))),
                n("setTimestampCalendar", () -> testBad("foo", ex(ps -> ps.setTimestamp     ("foo", TS, GC                 )))),
                n("setArray"            , () -> testBad("foo", ex(ps -> ps.setArray         ("foo", a(ps)                  )))),
                n("setSQLXML"           , () -> testBad("foo", ex(ps -> ps.setSQLXML        ("foo", s(ps)                  )))),
                n("setNull"             , () -> testBad("foo", ex(ps -> ps.setNull          ("foo", Types.VARCHAR          )))),
                n("setNull-2"           , () -> testBad("foo", ex(ps -> ps.setNull          ("foo", Types.VARCHAR, "foo"   ))))
        );
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testSimpleBadIndexes() {
        return Stream.of(0, -1, 2).flatMap(idx -> Stream.of(
                n("setBoolean-false-"     + idx, () -> testBad(idx, ex(ps -> ps.setBoolean       (idx, false                  )))),
                n("setBoolean-true-"      + idx, () -> testBad(idx, ex(ps -> ps.setBoolean       (idx, true                   )))),
                n("setByte-"              + idx, () -> testBad(idx, ex(ps -> ps.setByte          (idx, (byte ) 123            )))),
                n("setShort-"             + idx, () -> testBad(idx, ex(ps -> ps.setShort         (idx, (short) 123            )))),
                n("setInt-"               + idx, () -> testBad(idx, ex(ps -> ps.setInt           (idx, 123                    )))),
                n("setOptInt-"            + idx, () -> testBad(idx, ex(ps -> ps.setInt           (idx, OptionalInt.of(123)    )))),
                n("setLong-"              + idx, () -> testBad(idx, ex(ps -> ps.setLong          (idx, 123L                   )))),
                n("setOptLong-"           + idx, () -> testBad(idx, ex(ps -> ps.setLong          (idx, OptionalLong.of(123)   )))),
                n("setFloat-"             + idx, () -> testBad(idx, ex(ps -> ps.setFloat         (idx, 123F                   )))),
                n("setDouble-"            + idx, () -> testBad(idx, ex(ps -> ps.setDouble        (idx, 123D                   )))),
                n("setOptDouble-"         + idx, () -> testBad(idx, ex(ps -> ps.setDouble        (idx, OptionalDouble.of(123D))))),
                n("setBigDecimal-"        + idx, () -> testBad(idx, ex(ps -> ps.setBigDecimal    (idx, BigDecimal.TWO         )))),
                n("setString-"            + idx, () -> testBad(idx, ex(ps -> ps.setString        (idx, "foo"                  )))),
                n("setNString-"           + idx, () -> testBad(idx, ex(ps -> ps.setNString       (idx, "foo"                  )))),
                n("setBlob-Blob-"         + idx, () -> testBad(idx, ex(ps -> ps.setBytes         (idx, "foo".getBytes()       )))),
                n("setLocalDate-"         + idx, () -> testBad(idx, ex(ps -> ps.setLocalDate     (idx, LD                     )))),
                n("setLocalTime-"         + idx, () -> testBad(idx, ex(ps -> ps.setLocalTime     (idx, LT                     )))),
                n("setLocalDateTime-"     + idx, () -> testBad(idx, ex(ps -> ps.setLocalDateTime (idx, LDT                    )))),
                n("setOffsetTime-"        + idx, () -> testBad(idx, ex(ps -> ps.setOffsetTime    (idx, OT                     )))),
                n("setOffsetDateTime-"    + idx, () -> testBad(idx, ex(ps -> ps.setOffsetDateTime(idx, ODT                    )))),
                n("setZonedDateTime-"     + idx, () -> testBad(idx, ex(ps -> ps.setZonedDateTime (idx, ZDT                    )))),
                n("setInstant-"           + idx, () -> testBad(idx, ex(ps -> ps.setInstant       (idx, INS                    )))),
                n("setDate-"              + idx, () -> testBad(idx, ex(ps -> ps.setDate          (idx, D                      )))),
                n("setTime-"              + idx, () -> testBad(idx, ex(ps -> ps.setTime          (idx, T                      )))),
                n("setTimestamp-"         + idx, () -> testBad(idx, ex(ps -> ps.setTimestamp     (idx, TS                     )))),
                n("setDateCalendar-"      + idx, () -> testBad(idx, ex(ps -> ps.setDate          (idx, D , GC                 )))),
                n("setTimeCalendar-"      + idx, () -> testBad(idx, ex(ps -> ps.setTime          (idx, T , GC                 )))),
                n("setTimestampCalendar-" + idx, () -> testBad(idx, ex(ps -> ps.setTimestamp     (idx, TS, GC                 )))),
                n("setArray-"             + idx, () -> testBad(idx, ex(ps -> ps.setArray         (idx, a(ps)                  )))),
                n("setSQLXML-"            + idx, () -> testBad(idx, ex(ps -> ps.setSQLXML        (idx, s(ps)                  )))),
                n("setNull-"              + idx, () -> testBad(idx, ex(ps -> ps.setNull          (idx, Types.VARCHAR          )))),
                n("setNull-2-"            + idx, () -> testBad(idx, ex(ps -> ps.setNull          (idx, Types.VARCHAR, "foo"   ))))
        ));
    }

    @TestFactory
    @SuppressWarnings({"null", "deprecation"})
    public Stream<DynamicTest> testInputStreamReaderBlobClobNulls() {
        return Stream.of(
                n("setAsciiStream"          , () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream     (null, i()     )))),
                n("setAsciiStream-int"      , () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream     (null, i(), 10 )))),
                n("setAsciiStream-long"     , () -> ForTests.testNull("name", ex(ps -> ps.setAsciiStream     (null, i(), 10L)))),
                n("setBinaryStream"         , () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream    (null, i()     )))),
                n("setBinaryStream-int"     , () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream    (null, i(), 10 )))),
                n("setBinaryStream-long"    , () -> ForTests.testNull("name", ex(ps -> ps.setBinaryStream    (null, i(), 10L)))),
                n("setCharacterStream"      , () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream (null, r()     )))),
                n("setCharacterStream-int"  , () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream (null, r(), 10 )))),
                n("setCharacterStream-long" , () -> ForTests.testNull("name", ex(ps -> ps.setCharacterStream (null, r(), 10L)))),
                n("setNCharacterStream"     , () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r()     )))),
                n("setNCharacterStream-int" , () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r(), 10 )))),
                n("setNCharacterStream-long", () -> ForTests.testNull("name", ex(ps -> ps.setNCharacterStream(null, r(), 10L)))),
                n("setBlob-Blob"            , () -> ForTests.testNull("name", ex(ps -> ps.setBlob            (null, b(ps)   )))),
                n("setBlob-InputStream"     , () -> ForTests.testNull("name", ex(ps -> ps.setBlob            (null, i()     )))),
                n("setBlob-InputStream-int" , () -> ForTests.testNull("name", ex(ps -> ps.setBlob            (null, i(), 10L)))),
                n("setClob-Clob"            , () -> ForTests.testNull("name", ex(ps -> ps.setClob            (null, c(ps)   )))),
                n("setClob-Reader"          , () -> ForTests.testNull("name", ex(ps -> ps.setClob            (null, r()     )))),
                n("setClob-Reader-int"      , () -> ForTests.testNull("name", ex(ps -> ps.setClob            (null, r(), 10L)))),
                n("setNClob-NClob"          , () -> ForTests.testNull("name", ex(ps -> ps.setNClob           (null, n(ps)   )))),
                n("setNClob-Reader"         , () -> ForTests.testNull("name", ex(ps -> ps.setNClob           (null, r()     )))),
                n("setNClob-Reader-int"     , () -> ForTests.testNull("name", ex(ps -> ps.setNClob           (null, r(), 10L)))),
                n("setUnicodeStream"        , () -> ForTests.testNull("name", ex(ps -> ps.setUnicodeStream   (null, i(), 10 ))))
        );
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testInputStreamReaderBlobClobEmpties() {
        return Stream.of(
                n("setAsciiStream"          , () -> testBad("", ex(ps -> ps.setAsciiStream     ("", i()     )))),
                n("setAsciiStream-int"      , () -> testBad("", ex(ps -> ps.setAsciiStream     ("", i(), 10 )))),
                n("setAsciiStream-long"     , () -> testBad("", ex(ps -> ps.setAsciiStream     ("", i(), 10L)))),
                n("setBinaryStream"         , () -> testBad("", ex(ps -> ps.setBinaryStream    ("", i()     )))),
                n("setBinaryStream-int"     , () -> testBad("", ex(ps -> ps.setBinaryStream    ("", i(), 10 )))),
                n("setBinaryStream-long"    , () -> testBad("", ex(ps -> ps.setBinaryStream    ("", i(), 10L)))),
                n("setCharacterStream"      , () -> testBad("", ex(ps -> ps.setCharacterStream ("", r()     )))),
                n("setCharacterStream-int"  , () -> testBad("", ex(ps -> ps.setCharacterStream ("", r(), 10 )))),
                n("setCharacterStream-long" , () -> testBad("", ex(ps -> ps.setCharacterStream ("", r(), 10L)))),
                n("setNCharacterStream"     , () -> testBad("", ex(ps -> ps.setNCharacterStream("", r()     )))),
                n("setNCharacterStream-int" , () -> testBad("", ex(ps -> ps.setNCharacterStream("", r(), 10 )))),
                n("setNCharacterStream-long", () -> testBad("", ex(ps -> ps.setNCharacterStream("", r(), 10L)))),
                n("setBlob-Blob"            , () -> testBad("", ex(ps -> ps.setBlob            ("", b(ps)   )))),
                n("setBlob-InputStream"     , () -> testBad("", ex(ps -> ps.setBlob            ("", i()     )))),
                n("setBlob-InputStream-long", () -> testBad("", ex(ps -> ps.setBlob            ("", i(), 10L)))),
                n("setClob-Clob"            , () -> testBad("", ex(ps -> ps.setClob            ("", c(ps)   )))),
                n("setClob-Reader"          , () -> testBad("", ex(ps -> ps.setClob            ("", r()     )))),
                n("setClob-Reader-long"     , () -> testBad("", ex(ps -> ps.setClob            ("", r(), 10L)))),
                n("setNClob-NClob"          , () -> testBad("", ex(ps -> ps.setNClob           ("", n(ps)   )))),
                n("setNClob-Reader"         , () -> testBad("", ex(ps -> ps.setNClob           ("", r()     )))),
                n("setNClob-Reader-long"    , () -> testBad("", ex(ps -> ps.setNClob           ("", r(), 10L)))),
                n("setUnicodeStream"        , () -> testBad("", ex(ps -> ps.setUnicodeStream   ("", i(), 10 ))))
        );
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testInputStreamReaderBlobClobBads() {
        return Stream.of(
                n("setAsciiStream"          , () -> testBad("foo", ex(ps -> ps.setAsciiStream     ("foo", i()     )))),
                n("setAsciiStream-int"      , () -> testBad("foo", ex(ps -> ps.setAsciiStream     ("foo", i(), 10 )))),
                n("setAsciiStream-long"     , () -> testBad("foo", ex(ps -> ps.setAsciiStream     ("foo", i(), 10L)))),
                n("setBinaryStream"         , () -> testBad("foo", ex(ps -> ps.setBinaryStream    ("foo", i()     )))),
                n("setBinaryStream-int"     , () -> testBad("foo", ex(ps -> ps.setBinaryStream    ("foo", i(), 10 )))),
                n("setBinaryStream-long"    , () -> testBad("foo", ex(ps -> ps.setBinaryStream    ("foo", i(), 10L)))),
                n("setCharacterStream"      , () -> testBad("foo", ex(ps -> ps.setCharacterStream ("foo", r()     )))),
                n("setCharacterStream-int"  , () -> testBad("foo", ex(ps -> ps.setCharacterStream ("foo", r(), 10 )))),
                n("setCharacterStream-long" , () -> testBad("foo", ex(ps -> ps.setCharacterStream ("foo", r(), 10L)))),
                n("setNCharacterStream"     , () -> testBad("foo", ex(ps -> ps.setNCharacterStream("foo", r()     )))),
                n("setNCharacterStream-int" , () -> testBad("foo", ex(ps -> ps.setNCharacterStream("foo", r(), 10 )))),
                n("setNCharacterStream-long", () -> testBad("foo", ex(ps -> ps.setNCharacterStream("foo", r(), 10L)))),
                n("setBlob-Blob"            , () -> testBad("foo", ex(ps -> ps.setBlob            ("foo", b(ps)   )))),
                n("setBlob-InputStream"     , () -> testBad("foo", ex(ps -> ps.setBlob            ("foo", i()     )))),
                n("setBlob-InputStream-int" , () -> testBad("foo", ex(ps -> ps.setBlob            ("foo", i(), 10L)))),
                n("setClob-Clob"            , () -> testBad("foo", ex(ps -> ps.setClob            ("foo", c(ps)   )))),
                n("setClob-Reader"          , () -> testBad("foo", ex(ps -> ps.setClob            ("foo", r()     )))),
                n("setClob-Reader-int"      , () -> testBad("foo", ex(ps -> ps.setClob            ("foo", r(), 10L)))),
                n("setNClob-NClob"          , () -> testBad("foo", ex(ps -> ps.setNClob           ("foo", n(ps)   )))),
                n("setNClob-Reader"         , () -> testBad("foo", ex(ps -> ps.setNClob           ("foo", r()     )))),
                n("setNClob-Reader-int"     , () -> testBad("foo", ex(ps -> ps.setNClob           ("foo", r(), 10L)))),
                n("setUnicodeStream"        , () -> testBad("foo", ex(ps -> ps.setUnicodeStream   ("foo", i(), 10 ))))
        );
    }

    @TestFactory
    public Stream<DynamicTest> testInputStreamReaderBlobClobBadIndexes() {
        return Stream.of(0, -1, 2).flatMap(idx -> Stream.of(
                n("setAsciiStream"           + idx, () -> testBad(idx, ex(ps -> ps.setAsciiStream     (idx, i()     )))),
                n("setAsciiStream-int"       + idx, () -> testBad(idx, ex(ps -> ps.setAsciiStream     (idx, i(), 10 )))),
                n("setAsciiStream-long"      + idx, () -> testBad(idx, ex(ps -> ps.setAsciiStream     (idx, i(), 10L)))),
                n("setBinaryStream"          + idx, () -> testBad(idx, ex(ps -> ps.setBinaryStream    (idx, i()     )))),
                n("setBinaryStream-int"      + idx, () -> testBad(idx, ex(ps -> ps.setBinaryStream    (idx, i(), 10 )))),
                n("setBinaryStream-long"     + idx, () -> testBad(idx, ex(ps -> ps.setBinaryStream    (idx, i(), 10L)))),
                n("setCharacterStream"       + idx, () -> testBad(idx, ex(ps -> ps.setCharacterStream (idx, r()     )))),
                n("setCharacterStream-int"   + idx, () -> testBad(idx, ex(ps -> ps.setCharacterStream (idx, r(), 10 )))),
                n("setCharacterStream-long"  + idx, () -> testBad(idx, ex(ps -> ps.setCharacterStream (idx, r(), 10L)))),
                n("setNCharacterStream"      + idx, () -> testBad(idx, ex(ps -> ps.setNCharacterStream(idx, r()     )))),
                n("setNCharacterStream-int"  + idx, () -> testBad(idx, ex(ps -> ps.setNCharacterStream(idx, r(), 10 )))),
                n("setNCharacterStream-long" + idx, () -> testBad(idx, ex(ps -> ps.setNCharacterStream(idx, r(), 10L)))),
                n("setBlob-Blob-"            + idx, () -> testBad(idx, ex(ps -> ps.setBlob            (idx, b(ps)   )))),
                n("setBlob-InputStream-"     + idx, () -> testBad(idx, ex(ps -> ps.setBlob            (idx, i()     )))),
                n("setBlob-InputStream-int-" + idx, () -> testBad(idx, ex(ps -> ps.setBlob            (idx, i(), 10L)))),
                n("setClob-Clob-"            + idx, () -> testBad(idx, ex(ps -> ps.setClob            (idx, c(ps)   )))),
                n("setClob-Reader-"          + idx, () -> testBad(idx, ex(ps -> ps.setClob            (idx, r()     )))),
                n("setClob-Reader-int-"      + idx, () -> testBad(idx, ex(ps -> ps.setClob            (idx, r(), 10L)))),
                n("setNClob-NClob-"          + idx, () -> testBad(idx, ex(ps -> ps.setNClob           (idx, n(ps)   )))),
                n("setNClob-Reader-"         + idx, () -> testBad(idx, ex(ps -> ps.setNClob           (idx, r()     )))),
                n("setNClob-Reader-int-"     + idx, () -> testBad(idx, ex(ps -> ps.setNClob           (idx, r(), 10L))))
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testInputStreamReaderThrowsException() {
        return Stream.of(1, 2, 3, 4).flatMap(t -> Stream.of(
                n("setAsciiStream-1-"           + t, () -> testThrows(t, ex (ps -> ps.setAsciiStream     (1    , ix(t)     )))),
                n("setAsciiStream-2-"           + t, () -> testThrows(t, ex (ps -> ps.setAsciiStream     ("bar", ix(t)     )))),
                n("setAsciiStream-int-1-"       + t, () -> testThrows(t, ex (ps -> ps.setAsciiStream     (1    , ix(t), 10 )))),
                n("setAsciiStream-int-2-"       + t, () -> testThrows(t, ex (ps -> ps.setAsciiStream     ("bar", ix(t), 10 )))),
                n("setAsciiStream-long-1-"      + t, () -> testThrows(t, ex (ps -> ps.setAsciiStream     (1    , ix(t), 10L)))),
                n("setAsciiStream-long-2-"      + t, () -> testThrows(t, ex (ps -> ps.setAsciiStream     ("bar", ix(t), 10L)))),
                n("setBinaryStream-1-"          + t, () -> testThrows(t, ex (ps -> ps.setBinaryStream    (1    , ix(t)     )))),
                n("setBinaryStream-2-"          + t, () -> testThrows(t, ex (ps -> ps.setBinaryStream    ("bar", ix(t)     )))),
                n("setBinaryStream-int-1-"      + t, () -> testThrows(t, ex (ps -> ps.setBinaryStream    (1    , ix(t), 10 )))),
                n("setBinaryStream-int-2-"      + t, () -> testThrows(t, ex (ps -> ps.setBinaryStream    ("bar", ix(t), 10 )))),
                n("setBinaryStream-long-1-"     + t, () -> testThrows(t, ex (ps -> ps.setBinaryStream    (1    , ix(t), 10L)))),
                n("setBinaryStream-long-2-"     + t, () -> testThrows(t, ex (ps -> ps.setBinaryStream    ("bar", ix(t), 10L)))),
                n("setCharacterStream-1-"       + t, () -> testThrows(t, ex (ps -> ps.setCharacterStream (1    , rx(t)     )))),
                n("setCharacterStream-2-"       + t, () -> testThrows(t, ex (ps -> ps.setCharacterStream ("bar", rx(t)     )))),
                n("setCharacterStream-int-1-"   + t, () -> testThrows(t, ex (ps -> ps.setCharacterStream (1    , rx(t), 10 )))),
                n("setCharacterStream-int-2-"   + t, () -> testThrows(t, ex (ps -> ps.setCharacterStream ("bar", rx(t), 10 )))),
                n("setCharacterStream-long-1-"  + t, () -> testThrows(t, ex (ps -> ps.setCharacterStream (1    , rx(t), 10L)))),
                n("setCharacterStream-long-2-"  + t, () -> testThrows(t, ex (ps -> ps.setCharacterStream ("bar", rx(t), 10L)))),
                n("setNCharacterStream-1-"      + t, () -> testThrows(t, ex (ps -> ps.setNCharacterStream(1    , rx(t)     )))),
                n("setNCharacterStream-2-"      + t, () -> testThrows(t, ex (ps -> ps.setNCharacterStream("bar", rx(t)     )))),
                n("setNCharacterStream-long-1-" + t, () -> testThrows(t, ex (ps -> ps.setNCharacterStream(1    , rx(t), 10L)))),
                n("setNCharacterStream-long-2-" + t, () -> testThrows(t, ex (ps -> ps.setNCharacterStream("bar", rx(t), 10L)))),
                n("setBlob-InputStream-1-"      + t, () -> testThrows(t, ex (ps -> ps.setBlob            (1    , ix(t)     )))),
                n("setBlob-InputStream-2-"      + t, () -> testThrows(t, ex (ps -> ps.setBlob            ("bar", ix(t)     )))),
                n("setBlob-InputStream-long-1-" + t, () -> testThrows(t, ex (ps -> ps.setBlob            (1    , ix(t), 10L)))),
                n("setBlob-InputStream-long-2-" + t, () -> testThrows(t, ex (ps -> ps.setBlob            ("bar", ix(t), 10L)))),
                n("setClob-Reader-1-"           + t, () -> testThrows(t, ex (ps -> ps.setClob            (1    , rx(t)     )))),
                n("setClob-Reader-2-"           + t, () -> testThrows(t, ex (ps -> ps.setClob            ("bar", rx(t)     )))),
                n("setClob-Reader-long-1-"      + t, () -> testThrows(t, ex (ps -> ps.setClob            (1    , rx(t), 10L)))),
                n("setClob-Reader-long-2-"      + t, () -> testThrows(t, ex (ps -> ps.setClob            ("bar", rx(t), 10L)))),
                n("setNClob-Reader-1-"          + t, () -> testThrows(t, ex (ps -> ps.setNClob           (1    , rx(t)     )))),
                n("setNClob-Reader-2-"          + t, () -> testThrows(t, ex (ps -> ps.setNClob           ("bar", rx(t)     )))),
                n("setNClob-Reader-long-1-"     + t, () -> testThrows(t, ex (ps -> ps.setNClob           (1    , rx(t), 10L)))),
                n("setNClob-Reader-long-2-"     + t, () -> testThrows(t, ex (ps -> ps.setNClob           ("bar", rx(t), 10L))))
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testInputStreamReaderThrowsExceptionIfRepeated() {
        return Stream.of(
                n("setAsciiStream-3-X"          , () -> testThrows(5, ex2(ps -> ps.setAsciiStream     ("bar", i()       )))),
                n("setAsciiStream-int-3-X"      , () -> testThrows(5, ex2(ps -> ps.setAsciiStream     ("bar", i()  , 10 )))),
                n("setAsciiStream-long-3-X"     , () -> testThrows(5, ex2(ps -> ps.setAsciiStream     ("bar", i()  , 10L)))),
                n("setBinaryStream-3-X"         , () -> testThrows(5, ex2(ps -> ps.setBinaryStream    ("bar", i()       )))),
                n("setBinaryStream-int-3-X"     , () -> testThrows(5, ex2(ps -> ps.setBinaryStream    ("bar", i()  , 10 )))),
                n("setBinaryStream-long-3-X"    , () -> testThrows(5, ex2(ps -> ps.setBinaryStream    ("bar", i()  , 10L)))),
                n("setCharacterStream-3-X"      , () -> testThrows(6, ex2(ps -> ps.setCharacterStream ("bar", r()       )))),
                n("setCharacterStream-int-3-X"  , () -> testThrows(6, ex2(ps -> ps.setCharacterStream ("bar", r()  , 10 )))),
                n("setCharacterStream-long-3-X" , () -> testThrows(6, ex2(ps -> ps.setCharacterStream ("bar", r()  , 10L)))),
                n("setNCharacterStream-3-X"     , () -> testThrows(6, ex2(ps -> ps.setNCharacterStream("bar", r()       )))),
                n("setNCharacterStream-long-3-X", () -> testThrows(6, ex2(ps -> ps.setNCharacterStream("bar", r()  , 10L)))),
                n("setBlob-InputStream-3-X"     , () -> testThrows(5, ex2(ps -> ps.setBlob            ("bar", i()       )))),
                n("setBlob-InputStream-long-3-X", () -> testThrows(5, ex2(ps -> ps.setBlob            ("bar", i()  , 10L)))),
                n("setClob-Reader-3-X"          , () -> testThrows(6, ex2(ps -> ps.setClob            ("bar", r()       )))),
                n("setClob-Reader-long-3-X"     , () -> testThrows(6, ex2(ps -> ps.setClob            ("bar", r()  , 10L)))),
                n("setNClob-Reader-3-X"         , () -> testThrows(6, ex2(ps -> ps.setNClob           ("bar", r()       )))),
                n("setNClob-Reader-long-3-X"    , () -> testThrows(6, ex2(ps -> ps.setNClob           ("bar", r()  , 10L))))
        );
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testUnsupportedMethodsIntKeys() throws Exception {
        var url = new URI("http://0.0.0.0/").toURL();
        return Stream.of(1, 0, -1, 2).flatMap(idx -> Stream.of(
                n("setUnicodeStream-null-"     + idx, () -> testUnsupported("unicodeStream", ex (ps -> ps.setUnicodeStream(idx, null, 10)))),
                n("setUnicodeStream-instance-" + idx, () -> testUnsupported("unicodeStream", ex (ps -> ps.setUnicodeStream(idx, i() , 10)))),
                n("setURL-null-"               + idx, () -> testUnsupported("url"          , ex (ps -> ps.setURL          (idx, null    )))),
                n("setURL-instance-"           + idx, () -> testUnsupported("url"          , ex (ps -> ps.setURL          (idx, url     )))),
                n("setRef-null-"               + idx, () -> testUnsupported("ref"          , ex (ps -> ps.setRef          (idx, null    )))),
                n("setRef-instance-"           + idx, () -> testUnsupported("ref"          , ex (ps -> ps.setRef          (idx, ref()   )))),
                n("setRowId-null-"             + idx, () -> testUnsupported("rowId"        , ex (ps -> ps.setRowId        (idx, null    )))),
                n("setRowId-instance-"         + idx, () -> testUnsupported("rowId"        , ex (ps -> ps.setRowId        (idx, rowid() )))),
                n("setStruct-null-"            + idx, () -> testUnsupported("Struct"       , ex (ps -> ps.setStruct       (idx, null    )))),
                n("setStruct-instance-"        + idx, () -> testUnsupported("Struct"       , ex (ps -> ps.setStruct       (idx, struct()))))
        ));
    }

    @TestFactory
    @SuppressWarnings("deprecation")
    public Stream<DynamicTest> testUnsupportedMethodsStringKeys() throws Exception {
        var url = new URI("http://0.0.0.0/").toURL();
        return Stream.of("bar").flatMap(idx -> Stream.of(
                n("setUnicodeStream-null-1-"     + idx, () -> testUnsupported("unicodeStream", ex (ps -> ps.setUnicodeStream(idx, null, 10)))),
                n("setUnicodeStream-null-2-"     + idx, () -> testUnsupported("unicodeStream", ex2(ps -> ps.setUnicodeStream(idx, null, 10)))),
                n("setUnicodeStream-instance-1-" + idx, () -> testUnsupported("unicodeStream", ex (ps -> ps.setUnicodeStream(idx, i() , 10)))),
                n("setUnicodeStream-instance-2-" + idx, () -> testThrows     (5              , ex2(ps -> ps.setUnicodeStream(idx, i() , 10)))),
                n("setURL-null-1-"               + idx, () -> testUnsupported("url"          , ex (ps -> ps.setURL          (idx, null    )))),
                n("setURL-null-2-"               + idx, () -> testUnsupported("url"          , ex2(ps -> ps.setURL          (idx, null    )))),
                n("setURL-instance-1-"           + idx, () -> testUnsupported("url"          , ex (ps -> ps.setURL          (idx, url     )))),
                n("setURL-instance-2-"           + idx, () -> testUnsupported("url"          , ex2(ps -> ps.setURL          (idx, url     )))),
                n("setRef-null-1-"               + idx, () -> testUnsupported("ref"          , ex (ps -> ps.setRef          (idx, null    )))),
                n("setRef-null-2-"               + idx, () -> testUnsupported("ref"          , ex2(ps -> ps.setRef          (idx, null    )))),
                n("setRef-instance-1-"           + idx, () -> testUnsupported("ref"          , ex (ps -> ps.setRef          (idx, ref()   )))),
                n("setRef-instance-2-"           + idx, () -> testUnsupported("ref"          , ex2(ps -> ps.setRef          (idx, ref()   )))),
                n("setRowId-null-1-"             + idx, () -> testUnsupported("rowId"        , ex (ps -> ps.setRowId        (idx, null    )))),
                n("setRowId-null-2-"             + idx, () -> testUnsupported("rowId"        , ex2(ps -> ps.setRowId        (idx, null    )))),
                n("setRowId-instance-1-"         + idx, () -> testUnsupported("rowId"        , ex (ps -> ps.setRowId        (idx, rowid() )))),
                n("setRowId-instance-2-"         + idx, () -> testUnsupported("rowId"        , ex2(ps -> ps.setRowId        (idx, rowid() )))),
                n("setStruct-null-1-"            + idx, () -> testUnsupported("Struct"       , ex (ps -> ps.setStruct       (idx, null    )))),
                n("setStruct-null-2-"            + idx, () -> testUnsupported("Struct"       , ex2(ps -> ps.setStruct       (idx, null    )))),
                n("setStruct-instance-1-"        + idx, () -> testUnsupported("Struct"       , ex (ps -> ps.setStruct       (idx, struct())))),
                n("setStruct-instance-2-"        + idx, () -> testUnsupported("Struct"       , ex2(ps -> ps.setStruct       (idx, struct()))))
        ));
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testObjectsNulls() {
        return Stream.of(
                n("Simple-name"       , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0]                   )))),
                n("Type-name"         , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], Types .VARCHAR   )))),
                n("SQLType-name"      , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-name"   , () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], Types .VARCHAR, 3)))),
                n("SQLType-Scale-name", () -> ForTests.testNull("name", ex(ps -> ps.setObject(null, new byte[0], H2Type.VARCHAR, 3))))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testObjectsBads() {
        return Stream.of(
                n("Simple-empty"          , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0]                   )))),
                n("Type-empty"            , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], Types .VARCHAR   )))),
                n("SQLType-empty"         , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-empty"      , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], Types .VARCHAR, 3)))),
                n("SQLType-Scale-empty"   , () -> testBad(""   , ex(ps -> ps.setObject(""   , new byte[0], H2Type.VARCHAR, 3)))),
                n("Simple-junk"           , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0]                   )))),
                n("Type-junk"             , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], Types .VARCHAR   )))),
                n("SQLType-junk"          , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-junk"       , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], Types .VARCHAR, 3)))),
                n("SQLType-Scale-junk"    , () -> testBad("foo", ex(ps -> ps.setObject("foo", new byte[0], H2Type.VARCHAR, 3)))),
                n("Simple-zero"           , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0]                   )))),
                n("Type-zero"             , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], Types .VARCHAR   )))),
                n("SQLType-zero"          , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-zero"       , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], Types .VARCHAR, 3)))),
                n("SQLType-Scale-zero"    , () -> testBad(0    , ex(ps -> ps.setObject(0    , new byte[0], H2Type.VARCHAR, 3)))),
                n("Simple-negative"       , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0]                   )))),
                n("Type-negative"         , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], Types .VARCHAR   )))),
                n("SQLType-negative"      , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-negative"   , () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], Types .VARCHAR, 3)))),
                n("SQLType-Scale-negative", () -> testBad(-1   , ex(ps -> ps.setObject(-1   , new byte[0], H2Type.VARCHAR, 3)))),
                n("Simple-large"          , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0]                   )))),
                n("Type-large"            , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], Types .VARCHAR   )))),
                n("SQLType-large"         , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], H2Type.VARCHAR   )))),
                n("Type-Scale-large"      , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], Types .VARCHAR, 3)))),
                n("SQLType-Scale-large"   , () -> testBad(2    , ex(ps -> ps.setObject(2    , new byte[0], H2Type.VARCHAR, 3))))
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testWrapNullConnection() throws Exception {
        ForTests.testNull("ps", () -> NamedParameterStatement.wrap(null, Map.of()));
    }

    private static PreparedStatement mockPs() {
        var mock = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {PreparedStatement.class}, (i, m, a) -> {
            throw new AssertionError();
        });
        return (PreparedStatement) mock;
    }

    @Test
    @SuppressWarnings("null")
    public void testWrapNullMap() throws Exception {
        ForTests.testNull("indexes", () -> NamedParameterStatement.wrap(mockPs(), null));
    }
}
