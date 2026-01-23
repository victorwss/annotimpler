package ninja.javahacker.test.annotimpler.sql.stmt;

import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class BasicNamedParameterStatementTest {

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

    private static Stream<Arguments> applyBasicValues(String sql, NamedStatementContext... recvs) {
        var idx = Map.of("bar", List.of(1));
        var prepare = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4), color VARCHAR(4), onceuponatime TIMESTAMP, axml VARCHAR(100));",
                "INSERT INTO foo(pk, blah, color, onceuponatime, axml) VALUES (1, 'whoa', NULL  , '2024-03-04 13:14:15.456', NULL);",
                "INSERT INTO foo(pk, blah, color, onceuponatime, axml) VALUES (2, 'lol' , 'blue', '2026-01-02 16:11:12.123', '<foo>bar</foo>');"
        );
        ResultSetContext run = rs -> Assertions.assertAll(
                () -> Assertions.assertEquals(2, rs.getInt("pk")),
                () -> Assertions.assertEquals(2, rs.getInt(1)),
                () -> Assertions.assertEquals("lol", rs.getString("blah")),
                () -> Assertions.assertEquals("lol", rs.getString(2))
        );
        return Stream.of(recvs).map(recv -> Arguments.of(recv.name, singleLineApply(prepare, sql, recv.ctx, run, idx)));
    }

    private static record NamedStatementContext(String name, StatementContext ctx) {
    }

    private static NamedStatementContext n(String name, StatementContext ctx) {
        return new NamedStatementContext(name, ctx);
    }

    private static InputStream i() {
        return new ByteArrayInputStream("blue".getBytes());
    }

    private static Reader r() {
        return new CharArrayReader("blue".toCharArray());
    }

    @SneakyThrows
    private static Blob b(PreparedStatement ps) {
        var blob = ps.getConnection().createBlob();
        blob.setBytes(1, "blue".getBytes());
        return blob;
    }

    @SneakyThrows
    private static Clob c(PreparedStatement ps) {
        var clob = ps.getConnection().createClob();
        clob.setString(1, "blue");
        return clob;
    }

    @SneakyThrows
    private static NClob n(PreparedStatement ps) {
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

    private static String cn(Object obj) {
        if (obj instanceof NClob) return "NCLOB";
        if (obj instanceof Clob) return "CLOB";
        if (obj instanceof Blob) return "BLOB";
        if (obj instanceof Reader) return "READER";
        if (obj instanceof InputStream) return "STREAM";
        return obj.getClass().getName().toUpperCase(Locale.ROOT);
    }

    private static Stream<Arguments> testSetters() {
        var v1_5 = BigDecimal.valueOf(1.5);
        var bytes = "blue".getBytes();
        var opti = OptionalInt.of(2);
        var optl = OptionalLong.of(2);
        var optd = OptionalDouble.of(1.5);
        var blue = "blue";
        byte b2 = 2;
        short s2 = 2;
        var d1 = LocalDate.of(2025, 10, 10);
        var d2 = LocalDateTime.of(2025, 10, 10, 15, 30, 0);
        var d3 = LocalTime.of(15, 30, 0);
        var d4 = java.sql.Date.valueOf(d1);
        var d5 = java.sql.Timestamp.valueOf(d2);
        var d6 = java.sql.Time.valueOf(d3);
        var d7 = ZonedDateTime.of(d2, ZoneOffset.UTC);
        var d8 = OffsetDateTime.of(d2, ZoneOffset.UTC);
        var d9 = d8.toInstant();
        var gc = new GregorianCalendar();

        var t1 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk = (CASE WHEN ? THEN 2 ELSE 1 END)",
                n("STR-TRUE" , ps -> ps.setBoolean("bar", true )),
                n("INT-TRUE" , ps -> ps.setBoolean(1    , true ))
        );

        var t2 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk = (CASE WHEN ? THEN 1 ELSE 2 END)",
                n("STR-FALSE", ps -> ps.setBoolean("bar", false)),
                n("INT-FALSE", ps -> ps.setBoolean(1    , false))
        );

        var t3 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk = ?",
                n("STR-BYTE"           , ps -> ps.setByte  ("bar", b2                      )),
                n("INT-BYTE"           , ps -> ps.setByte  (1    , b2                      )),
                n("STR-SHORT"          , ps -> ps.setShort ("bar", s2                      )),
                n("INT-SHORT"          , ps -> ps.setShort (1    , s2                      )),
                n("STR-INT"            , ps -> ps.setInt   ("bar", 2                       )),
                n("INT-INT"            , ps -> ps.setInt   (1    , 2                       )),
                n("STR-LONG"           , ps -> ps.setLong  ("bar", 2                       )),
                n("INT-LONG"           , ps -> ps.setLong  (1    , 2                       )),
                n("STR-OPTINT"         , ps -> ps.setInt   ("bar", opti                    )),
                n("INT-OPTINT"         , ps -> ps.setInt   (1    , opti                    )),
                n("STR-OPTLONG"        , ps -> ps.setLong  ("bar", optl                    )),
                n("INT-OPTLONG"        , ps -> ps.setLong  (1    , optl                    )),
                n("STR-OBJ-BYTE"       , ps -> ps.setObject("bar", b2                      )),
                n("INT-OBJ-BYTE"       , ps -> ps.setObject(1    , b2                      )),
                n("STR-OBJ-SHORT"      , ps -> ps.setObject("bar", s2                      )),
                n("INT-OBJ-SHORT"      , ps -> ps.setObject(1    , s2                      )),
                n("STR-OBJ-INT"        , ps -> ps.setObject("bar", 2                       )),
                n("INT-OBJ-INT"        , ps -> ps.setObject(1    , 2                       )),
                n("STR-OBJ-LONG"       , ps -> ps.setObject("bar", 2L                      )),
                n("INT-OBJ-LONG"       , ps -> ps.setObject(1    , 2L                      )),
                n("STR-OBJ-BYTE-NUM"   , ps -> ps.setObject("bar", b2   , Types .NUMERIC   )),
                n("INT-OBJ-BYTE-NUM"   , ps -> ps.setObject(1    , b2   , Types .NUMERIC   )),
                n("STR-OBJ-SHORT-NUM"  , ps -> ps.setObject("bar", s2   , Types .NUMERIC   )),
                n("INT-OBJ-SHORT-NUM"  , ps -> ps.setObject(1    , s2   , Types .NUMERIC   )),
                n("STR-OBJ-INT-NUM"    , ps -> ps.setObject("bar", 2    , Types .NUMERIC   )),
                n("INT-OBJ-INT-NUM"    , ps -> ps.setObject(1    , 2    , Types .NUMERIC   )),
                n("STR-OBJ-LONG-NUM"   , ps -> ps.setObject("bar", 2L   , Types .NUMERIC   )),
                n("INT-OBJ-LONG-NUM"   , ps -> ps.setObject(1    , 2L   , Types .NUMERIC   )),
                n("STR-OBJ-BYTE-H2"    , ps -> ps.setObject("bar", b2   , H2Type.NUMERIC   )),
                n("INT-OBJ-BYTE-H2"    , ps -> ps.setObject(1    , b2   , H2Type.NUMERIC   )),
                n("STR-OBJ-SHORT-H2"   , ps -> ps.setObject("bar", s2   , H2Type.NUMERIC   )),
                n("INT-OBJ-SHORT-H2"   , ps -> ps.setObject(1    , s2   , H2Type.NUMERIC   )),
                n("STR-OBJ-INT-H2"     , ps -> ps.setObject("bar", 2    , H2Type.NUMERIC   )),
                n("INT-OBJ-INT-H2"     , ps -> ps.setObject(1    , 2    , H2Type.NUMERIC   )),
                n("STR-OBJ-LONG-H2"    , ps -> ps.setObject("bar", 2L   , H2Type.NUMERIC   )),
                n("INT-OBJ-LONG-H2"    , ps -> ps.setObject(1    , 2L   , H2Type.NUMERIC   )),
                n("STR-OBJ-BYTE-NUM-Z" , ps -> ps.setObject("bar", b2   , Types .NUMERIC, 0)),
                n("INT-OBJ-BYTE-NUM-Z" , ps -> ps.setObject(1    , b2   , Types .NUMERIC, 0)),
                n("STR-OBJ-SHORT-NUM-Z", ps -> ps.setObject("bar", s2   , Types .NUMERIC, 0)),
                n("INT-OBJ-SHORT-NUM-Z", ps -> ps.setObject(1    , s2   , Types .NUMERIC, 0)),
                n("STR-OBJ-INT-NUM-Z"  , ps -> ps.setObject("bar", 2    , Types .NUMERIC, 0)),
                n("INT-OBJ-INT-NUM-Z"  , ps -> ps.setObject(1    , 2    , Types .NUMERIC, 0)),
                n("STR-OBJ-LONG-NUM-Z" , ps -> ps.setObject("bar", 2L   , Types .NUMERIC, 0)),
                n("INT-OBJ-LONG-NUM-Z" , ps -> ps.setObject(1    , 2L   , Types .NUMERIC, 0)),
                n("STR-OBJ-BYTE-H2-Z"  , ps -> ps.setObject("bar", b2   , H2Type.NUMERIC, 0)),
                n("INT-OBJ-BYTE-H2-Z"  , ps -> ps.setObject(1    , b2   , H2Type.NUMERIC, 0)),
                n("STR-OBJ-SHORT-H2-Z" , ps -> ps.setObject("bar", s2   , H2Type.NUMERIC, 0)),
                n("INT-OBJ-SHORT-H2-Z" , ps -> ps.setObject(1    , s2   , H2Type.NUMERIC, 0)),
                n("STR-OBJ-INT-H2-Z"   , ps -> ps.setObject("bar", 2    , H2Type.NUMERIC, 0)),
                n("INT-OBJ-INT-H2-Z"   , ps -> ps.setObject(1    , 2    , H2Type.NUMERIC, 0)),
                n("STR-OBJ-LONG-H2-Z"  , ps -> ps.setObject("bar", 2L   , H2Type.NUMERIC, 0)),
                n("INT-OBJ-LONG-H2-Z"  , ps -> ps.setObject(1    , 2L   , H2Type.NUMERIC, 0))
        );

        var t4 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE pk > ?",
                n("STR-OBJ-FLOAT-H2-Z"  , ps -> ps.setFloat     ("bar", 1.5f                    )),
                n("INT-OBJ-FLOAT-H2-Z"  , ps -> ps.setFloat     (1    , 1.5f                    )),
                n("STR-OBJ-DOUBLE-H2-Z" , ps -> ps.setDouble    ("bar", 1.5                     )),
                n("INT-OBJ-DOUBLE-H2-Z" , ps -> ps.setDouble    (1    , 1.5                     )),
                n("STR-OBJ-BIGD-H2-Z"   , ps -> ps.setBigDecimal("bar", v1_5                    )),
                n("INT-OBJ-BIGD-H2-Z"   , ps -> ps.setBigDecimal(1    , v1_5                    )),
                n("STR-OPTDOUBLE"       , ps -> ps.setDouble    ("bar", optd                    )),
                n("INT-OPTDOUBLE"       , ps -> ps.setDouble    (1    , optd                    )),
                n("STR-OBJ-FLOAT"       , ps -> ps.setObject    ("bar", 1.5f                    )),
                n("INT-OBJ-FLOAT"       , ps -> ps.setObject    (1    , 1.5f                    )),
                n("STR-OBJ-DOUBLE"      , ps -> ps.setObject    ("bar", 1.5                     )),
                n("INT-OBJ-DOUBLE"      , ps -> ps.setObject    (1    , 1.5                     )),
                n("STR-OBJ-BIGD"        , ps -> ps.setObject    ("bar", v1_5                    )),
                n("INT-OBJ-BIGD"        , ps -> ps.setObject    (1    , v1_5                    )),
                n("STR-OBJ-FLOAT-NUM"   , ps -> ps.setObject    ("bar", 1.5f , Types .NUMERIC   )),
                n("INT-OBJ-FLOAT-NUM"   , ps -> ps.setObject    (1    , 1.5f , Types .NUMERIC   )),
                n("STR-OBJ-DOUBLE-NUM"  , ps -> ps.setObject    ("bar", 1.5  , Types .NUMERIC   )),
                n("INT-OBJ-DOUBLE-NUM"  , ps -> ps.setObject    (1    , 1.5  , Types .NUMERIC   )),
                n("STR-OBJ-BIGD-NUM"    , ps -> ps.setObject    ("bar", v1_5 , Types .NUMERIC   )),
                n("INT-OBJ-BIGD-NUM"    , ps -> ps.setObject    (1    , v1_5 , Types .NUMERIC   )),
                n("STR-OBJ-FLOAT-H2"    , ps -> ps.setObject    ("bar", 1.5f , H2Type.NUMERIC   )),
                n("INT-OBJ-FLOAT-H2"    , ps -> ps.setObject    (1    , 1.5f , H2Type.NUMERIC   )),
                n("STR-OBJ-DOUBLE-H2"   , ps -> ps.setObject    ("bar", 1.5  , H2Type.NUMERIC   )),
                n("INT-OBJ-DOUBLE-H2"   , ps -> ps.setObject    (1    , 1.5  , H2Type.NUMERIC   )),
                n("STR-OBJ-BIGD-H2"     , ps -> ps.setObject    ("bar", v1_5 , H2Type.NUMERIC   )),
                n("INT-OBJ-BIGD-H2"     , ps -> ps.setObject    (1    , v1_5 , H2Type.NUMERIC   )),
                n("STR-OBJ-FLOAT-NUM-Z" , ps -> ps.setObject    ("bar", 1.5f , Types .NUMERIC, 1)),
                n("INT-OBJ-FLOAT-NUM-Z" , ps -> ps.setObject    (1    , 1.5f , Types .NUMERIC, 1)),
                n("STR-OBJ-DOUBLE-NUM-Z", ps -> ps.setObject    ("bar", 1.5  , Types .NUMERIC, 1)),
                n("INT-OBJ-DOUBLE-NUM-Z", ps -> ps.setObject    (1    , 1.5  , Types .NUMERIC, 1)),
                n("STR-OBJ-BIGD-NUM-Z"  , ps -> ps.setObject    ("bar", v1_5 , Types .NUMERIC, 1)),
                n("INT-OBJ-BIGD-NUM-Z"  , ps -> ps.setObject    (1    , v1_5 , Types .NUMERIC, 1)),
                n("STR-OBJ-FLOAT-H2-Z"  , ps -> ps.setObject    ("bar", 1.5f , H2Type.NUMERIC, 1)),
                n("INT-OBJ-FLOAT-H2-Z"  , ps -> ps.setObject    (1    , 1.5f , H2Type.NUMERIC, 1)),
                n("STR-OBJ-DOUBLE-H2-Z" , ps -> ps.setObject    ("bar", 1.5  , H2Type.NUMERIC, 1)),
                n("INT-OBJ-DOUBLE-H2-Z" , ps -> ps.setObject    (1    , 1.5  , H2Type.NUMERIC, 1)),
                n("STR-OBJ-BIGD-H2-Z"   , ps -> ps.setObject    ("bar", v1_5 , H2Type.NUMERIC, 1)),
                n("INT-OBJ-BIGD-H2-Z"   , ps -> ps.setObject    (1    , v1_5 , H2Type.NUMERIC, 1))
        );

        var t5 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ?",
                n("STR-STRING"           , ps -> ps.setString          ("bar", blue                     )),
                n("INT-STRING"           , ps -> ps.setString          (1    , blue                     )),
                n("STR-NSTRING"          , ps -> ps.setNString         ("bar", blue                     )),
                n("INT-NSTRING"          , ps -> ps.setNString         (1    , blue                     )),
                n("STR-BYTES"            , ps -> ps.setBytes           ("bar", bytes                    )),
                n("INT-BYTES"            , ps -> ps.setBytes           (1    , bytes                    )),
                n("STR-OBJ-STR"          , ps -> ps.setObject          ("bar", blue                     )),
                n("INT-OBJ-STR"          , ps -> ps.setObject          (1    , blue                     )),
                n("STR-OBJ-STR-VARCHAR"  , ps -> ps.setObject          ("bar", blue , Types .VARCHAR    )),
                n("INT-OBJ-STR-VARCHAR"  , ps -> ps.setObject          (1    , blue , Types .VARCHAR    )),
                n("STR-OBJ-STR-VARCHAR-Q", ps -> ps.setObject          ("bar", blue , Types .VARCHAR, 4 )),
                n("INT-OBJ-STR-VARCHAR-Q", ps -> ps.setObject          (1    , blue , Types .VARCHAR, 4 )),
                n("STR-OBJ-STR-H2"       , ps -> ps.setObject          ("bar", blue , H2Type.VARCHAR    )),
                n("INT-OBJ-STR-H2"       , ps -> ps.setObject          (1    , blue , H2Type.VARCHAR    )),
                n("STR-OBJ-STR-H2-Q"     , ps -> ps.setObject          ("bar", blue , H2Type.VARCHAR, 4 )),
                n("INT-OBJ-STR-H2-Q"     , ps -> ps.setObject          (1    , blue , H2Type.VARCHAR, 4 )),
                n("STR-OBJ-BYT"          , ps -> ps.setObject          ("bar", bytes                    )),
                n("INT-OBJ-BYT"          , ps -> ps.setObject          (1    , bytes                    )),
                n("STR-OBJ-BYT-VARCHAR"  , ps -> ps.setObject          ("bar", bytes, Types .VARCHAR    )),
                n("INT-OBJ-BYT-VARCHAR"  , ps -> ps.setObject          (1    , bytes, Types .VARCHAR    )),
                n("STR-OBJ-BYT-VARCHAR-Q", ps -> ps.setObject          ("bar", bytes, Types .VARCHAR, 4 )),
                n("INT-OBJ-BYT-VARCHAR-Q", ps -> ps.setObject          (1    , bytes, Types .VARCHAR, 4 )),
                n("STR-OBJ-BYT-H2"       , ps -> ps.setObject          ("bar", bytes, H2Type.VARCHAR    )),
                n("INT-OBJ-BYT-H2"       , ps -> ps.setObject          (1    , bytes, H2Type.VARCHAR    )),
                n("STR-OBJ-BYT-H2-Q"     , ps -> ps.setObject          ("bar", bytes, H2Type.VARCHAR, 4 )),
                n("INT-OBJ-BYT-H2-Q"     , ps -> ps.setObject          (1    , bytes, H2Type.VARCHAR, 4 )),
                n("STR-ASCII"            , ps -> ps.setAsciiStream     ("bar", i()                      )),
                n("INT-ASCII"            , ps -> ps.setAsciiStream     (1    , i()                      )),
                n("STR-ASCII-INT"        , ps -> ps.setAsciiStream     ("bar", i()                  , 4 )),
                n("INT-ASCII-INT"        , ps -> ps.setAsciiStream     (1    , i()                  , 4 )),
                n("STR-ASCII-LONG"       , ps -> ps.setAsciiStream     ("bar", i()                  , 4L)),
                n("INT-ASCII-LONG"       , ps -> ps.setAsciiStream     (1    , i()                  , 4L)),
                n("STR-BINARY"           , ps -> ps.setBinaryStream    ("bar", i()                      )),
                n("INT-BINARY"           , ps -> ps.setBinaryStream    (1    , i()                      )),
                n("STR-BINARY-INT"       , ps -> ps.setBinaryStream    ("bar", i()                  , 4 )),
                n("INT-BINARY-INT"       , ps -> ps.setBinaryStream    (1    , i()                  , 4 )),
                n("STR-BINARY-LONG"      , ps -> ps.setBinaryStream    ("bar", i()                  , 4L)),
                n("INT-BINARY-LONG"      , ps -> ps.setBinaryStream    (1    , i()                  , 4L)),
                n("STR-CHAR"             , ps -> ps.setCharacterStream ("bar", r()                      )),
                n("INT-CHAR"             , ps -> ps.setCharacterStream (1    , r()                      )),
                n("STR-CHAR-INT"         , ps -> ps.setCharacterStream ("bar", r()                  , 4 )),
                n("INT-CHAR-INT"         , ps -> ps.setCharacterStream (1    , r()                  , 4 )),
                n("STR-CHAR-LONG"        , ps -> ps.setCharacterStream ("bar", r()                  , 4L)),
                n("INT-CHAR-LONG"        , ps -> ps.setCharacterStream (1    , r()                  , 4L)),
                n("STR-NCHAR"            , ps -> ps.setNCharacterStream("bar", r()                      )),
                n("INT-NCHAR"            , ps -> ps.setNCharacterStream(1    , r()                      )),
                n("STR-NCHAR-INT"        , ps -> ps.setNCharacterStream("bar", r()                  , 4 )),
                n("INT-NCHAR-INT"        , ps -> ps.setNCharacterStream(1    , r()                  , 4 )),
                n("STR-NCHAR-LONG"       , ps -> ps.setNCharacterStream("bar", r()                  , 4L)),
                n("INT-NCHAR-LONG"       , ps -> ps.setNCharacterStream(1    , r()                  , 4L)),
                n("STR-CLOB"             , ps -> ps.setClob            ("bar", c(ps)                    )),
                n("INT-CLOB"             , ps -> ps.setClob            (1    , c(ps)                    )),
                n("STR-CLOB-INT"         , ps -> ps.setClob            ("bar", r()                      )),
                n("INT-CLOB-INT"         , ps -> ps.setClob            (1    , r()                      )),
                n("STR-CLOB-LONG"        , ps -> ps.setClob            ("bar", r()                  , 4L)),
                n("INT-CLOB-LONG"        , ps -> ps.setClob            (1    , r()                  , 4L)),
                n("STR-NCLOB"            , ps -> ps.setNClob           ("bar", n(ps)                    )),
                n("INT-NCLOB"            , ps -> ps.setNClob           (1    , n(ps)                    )),
                n("STR-NCLOB-INT"        , ps -> ps.setNClob           ("bar", r()                      )),
                n("INT-NCLOB-INT"        , ps -> ps.setNClob           (1    , r()                      )),
                n("STR-NCLOB-LONG"       , ps -> ps.setNClob           ("bar", r()                  , 4L)),
                n("INT-NCHAR-LONG"       , ps -> ps.setNClob           (1    , r()                  , 4L)),
                n("STR-BLOB"             , ps -> ps.setBlob            ("bar", b(ps)                    )),
                n("INT-BLOB"             , ps -> ps.setBlob            (1    , b(ps)                    )),
                n("STR-BLOB-INT"         , ps -> ps.setBlob            ("bar", i()                      )),
                n("INT-BLOB-INT"         , ps -> ps.setBlob            (1    , i()                      )),
                n("STR-BLOB-LONG"        , ps -> ps.setBlob            ("bar", i()                  , 4L)),
                n("INT-BLOB-LONG"        , ps -> ps.setBlob            (1    , i()                  , 4L))
        );

        var t6 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE onceuponatime > ?",
                n("STR-DATE"             , ps -> ps.setDate         ("bar", d4      )),
                n("INT-DATE"             , ps -> ps.setDate         (1    , d4      )),
                n("STR-DATE-CAL-NUL"     , ps -> ps.setDate         ("bar", d4, null)),
                n("INT-DATE-CAL-NUL"     , ps -> ps.setDate         (1    , d4, null)),
                n("STR-DATE-CAL-NEW"     , ps -> ps.setDate         ("bar", d4, gc  )),
                n("INT-DATE-CAL-NEW"     , ps -> ps.setDate         (1    , d4, gc  )),
                n("STR-TIMESTAMP"        , ps -> ps.setTimestamp    ("bar", d5      )),
                n("INT-TIMESTAMP"        , ps -> ps.setTimestamp    (1    , d5      )),
                n("STR-TIMESTAMP-CAL-NUL", ps -> ps.setTimestamp    ("bar", d5, null)),
                n("INT-TIMESTAMP-CAL-NUL", ps -> ps.setTimestamp    (1    , d5, null)),
                n("STR-TIMESTAMP-CAL-NEW", ps -> ps.setTimestamp    ("bar", d5, gc  )),
                n("INT-TIMESTAMP-CAL-NEW", ps -> ps.setTimestamp    (1    , d5, gc  )),
                n("STR-LOCALDATE"        , ps -> ps.setLocalDate    ("bar", d1      )),
                n("INT-LOCALDATE"        , ps -> ps.setLocalDate    (1    , d1      )),
                n("STR-LOCALDATETIME"    , ps -> ps.setLocalDateTime("bar", d2      )),
                n("INT-LOCALDATETIME"    , ps -> ps.setLocalDateTime(1    , d2      ))
        );

        Function<Object, Stream<Arguments>> t7p = obj -> applyBasicValues(
                "SELECT pk, blah FROM foo WHERE onceuponatime > ?",
                n("STR-" + cn(obj)           , ps -> ps.setObject("bar", obj                                    )),
                n("INT-" + cn(obj)           , ps -> ps.setObject(1    , obj                                    )),
                n("STR-" + cn(obj) + "-TT"   , ps -> ps.setObject("bar", obj, Types .TIMESTAMP                  )),
                n("INT-" + cn(obj) + "-TT"   , ps -> ps.setObject(1    , obj, Types .TIMESTAMP                  )),
                n("STR-" + cn(obj) + "-TT"   , ps -> ps.setObject("bar", obj, Types .TIMESTAMP               , 0)),
                n("INT-" + cn(obj) + "-TT"   , ps -> ps.setObject(1    , obj, Types .TIMESTAMP               , 0)),
                n("STR-" + cn(obj) + "-H2"   , ps -> ps.setObject("bar", obj, H2Type.TIMESTAMP                  )),
                n("INT-" + cn(obj) + "-H2"   , ps -> ps.setObject(1    , obj, H2Type.TIMESTAMP                  )),
                n("STR-" + cn(obj) + "-H2"   , ps -> ps.setObject("bar", obj, H2Type.TIMESTAMP               , 0)),
                n("INT-" + cn(obj) + "-H2"   , ps -> ps.setObject(1    , obj, H2Type.TIMESTAMP               , 0)),
                n("STR-" + cn(obj) + "-TT-TZ", ps -> ps.setObject("bar", obj, Types .TIMESTAMP_WITH_TIMEZONE    )),
                n("INT-" + cn(obj) + "-TT-TZ", ps -> ps.setObject(1    , obj, Types .TIMESTAMP_WITH_TIMEZONE    )),
                n("STR-" + cn(obj) + "-TT-TZ", ps -> ps.setObject("bar", obj, Types .TIMESTAMP_WITH_TIMEZONE , 0)),
                n("INT-" + cn(obj) + "-TT-TZ", ps -> ps.setObject(1    , obj, Types .TIMESTAMP_WITH_TIMEZONE , 0)),
                n("STR-" + cn(obj) + "-H2-TZ", ps -> ps.setObject("bar", obj, H2Type.TIMESTAMP_WITH_TIME_ZONE   )),
                n("INT-" + cn(obj) + "-H2-TZ", ps -> ps.setObject(1    , obj, H2Type.TIMESTAMP_WITH_TIME_ZONE   )),
                n("STR-" + cn(obj) + "-H2-TZ", ps -> ps.setObject("bar", obj, H2Type.TIMESTAMP_WITH_TIME_ZONE, 0)),
                n("INT-" + cn(obj) + "-H2-TZ", ps -> ps.setObject(1    , obj, H2Type.TIMESTAMP_WITH_TIME_ZONE, 0))
        );
        var t7 = Stream.of(d1, d2, d4, d5, d7, d8, d9).flatMap(t7p);

        var t8 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE EXTRACT(HOUR FROM onceuponatime) > EXTRACT(HOUR FROM ?)",
                n("STR-TIME"        , ps -> ps.setTime     ("bar", d6      )),
                n("INT-TIME"        , ps -> ps.setTime     (1    , d6      )),
                n("STR-TIME-CAL-NUL", ps -> ps.setTime     ("bar", d6, null)),
                n("INT-TIME-CAL-NUL", ps -> ps.setTime     (1    , d6, null)),
                n("STR-TIME-CAL-NEW", ps -> ps.setTime     ("bar", d6, gc  )),
                n("INT-TIME-CAL-NEW", ps -> ps.setTime     (1    , d6, gc  )),
                n("STR-LOCALTIME"   , ps -> ps.setLocalTime("bar", d3      )),
                n("INT-LOCALTIME"   , ps -> ps.setLocalTime(1    , d3      ))
        );

        Function<Object, Stream<Arguments>> t9p = obj -> applyBasicValues(
                "SELECT pk, blah FROM foo WHERE EXTRACT(HOUR FROM onceuponatime) > EXTRACT(HOUR FROM ?)",
                n("STR-" + cn(obj)           , ps -> ps.setObject("bar", obj                               )),
                n("INT-" + cn(obj)           , ps -> ps.setObject(    1, obj                               )),
                n("STR-" + cn(obj) + "-TT"   , ps -> ps.setObject("bar", obj, Types .TIME                  )),
                n("INT-" + cn(obj) + "-TT"   , ps -> ps.setObject(    1, obj, Types .TIME                  )),
                n("STR-" + cn(obj) + "-TT"   , ps -> ps.setObject("bar", obj, Types .TIME               , 0)),
                n("INT-" + cn(obj) + "-TT"   , ps -> ps.setObject(    1, obj, Types .TIME               , 0)),
                n("STR-" + cn(obj) + "-H2"   , ps -> ps.setObject("bar", obj, H2Type.TIME                  )),
                n("INT-" + cn(obj) + "-H2"   , ps -> ps.setObject(    1, obj, H2Type.TIME                  )),
                n("STR-" + cn(obj) + "-H2"   , ps -> ps.setObject("bar", obj, H2Type.TIME               , 0)),
                n("INT-" + cn(obj) + "-H2"   , ps -> ps.setObject(    1, obj, H2Type.TIME               , 0)),
                n("STR-" + cn(obj) + "-TT-TZ", ps -> ps.setObject("bar", obj, Types .TIME_WITH_TIMEZONE    )),
                n("INT-" + cn(obj) + "-TT-TZ", ps -> ps.setObject(    1, obj, Types .TIME_WITH_TIMEZONE    )),
                n("STR-" + cn(obj) + "-TT-TZ", ps -> ps.setObject("bar", obj, Types .TIME_WITH_TIMEZONE , 0)),
                n("INT-" + cn(obj) + "-TT-TZ", ps -> ps.setObject(    1, obj, Types .TIME_WITH_TIMEZONE , 0)),
                n("STR-" + cn(obj) + "-H2-TZ", ps -> ps.setObject("bar", obj, H2Type.TIME_WITH_TIME_ZONE   )),
                n("INT-" + cn(obj) + "-H2-TZ", ps -> ps.setObject(    1, obj, H2Type.TIME_WITH_TIME_ZONE   )),
                n("STR-" + cn(obj) + "-H2-TZ", ps -> ps.setObject("bar", obj, H2Type.TIME_WITH_TIME_ZONE, 0)),
                n("INT-" + cn(obj) + "-H2-TZ", ps -> ps.setObject(    1, obj, H2Type.TIME_WITH_TIME_ZONE, 0))
        );
        var t9 = Stream.of(d3, d6).flatMap(t9p);

        Function<PreparedStatement, Object> o1 = ps -> i();
        Function<PreparedStatement, Object> o2 = ps -> r();
        Function<PreparedStatement, Object> o3 = ps -> c(ps);
        Function<PreparedStatement, Object> o4 = ps -> b(ps);
        Function<PreparedStatement, Object> o5 = ps -> n(ps);
        Function<Function<PreparedStatement, Object>, Stream<Arguments>> t10p = obj -> applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ?",
                n("STR-" + cn(obj)          , ps -> ps.setObject("bar", obj.apply(ps)                )),
                n("INT-" + cn(obj)          , ps -> ps.setObject(    1, obj.apply(ps)                )),
                n("STR-" + cn(obj) + "-TT"  , ps -> ps.setObject("bar", obj.apply(ps), Types .BLOB   )),
                n("INT-" + cn(obj) + "-TT"  , ps -> ps.setObject(    1, obj.apply(ps), Types .BLOB   )),
                n("STR-" + cn(obj) + "-TT-Q", ps -> ps.setObject("bar", obj.apply(ps), Types .BLOB, 4)),
                n("INT-" + cn(obj) + "-TT-Q", ps -> ps.setObject(    1, obj.apply(ps), Types .BLOB, 4)),
                n("STR-" + cn(obj) + "-H2"  , ps -> ps.setObject("bar", obj.apply(ps), H2Type.BLOB   )),
                n("INT-" + cn(obj) + "-H2"  , ps -> ps.setObject(    1, obj.apply(ps), H2Type.BLOB   )),
                n("STR-" + cn(obj) + "-H2-Q", ps -> ps.setObject("bar", obj.apply(ps), H2Type.BLOB, 4)),
                n("INT-" + cn(obj) + "-H2-Q", ps -> ps.setObject(    1, obj.apply(ps), H2Type.BLOB, 4))
        );
        var t10 = Stream.of(o1, o2, o3, o4, o5).flatMap(t10p);

        var t11 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE color = ANY(?)",
                n("STR-ARRAY"         , ps -> ps.setArray ("bar", a(ps)                                 )),
                n("INT-ARRAY"         , ps -> ps.setArray (1    , a(ps)                                 )),
                n("STR-OBJ-ARRAY"     , ps -> ps.setObject("bar", a(ps)                                 )),
                n("INT-OBJ-ARRAY"     , ps -> ps.setObject(1    , a(ps)                                 )),
                n("STR-OBJ-ARRAY-TT"  , ps -> ps.setObject("bar", a(ps), Types.ARRAY                    )),
                n("INT-OBJ-ARRAY-TT"  , ps -> ps.setObject(1    , a(ps), Types.ARRAY                    )),
                n("STR-OBJ-ARRAY-TT-Q", ps -> ps.setObject("bar", a(ps), Types.ARRAY                 , 3)),
                n("INT-OBJ-ARRAY-TT-Q", ps -> ps.setObject(1    , a(ps), Types.ARRAY                 , 3)),
                n("STR-OBJ-ARRAY-H2"  , ps -> ps.setObject("bar", a(ps), H2Type.array(H2Type.VARCHAR)   )),
                n("INT-OBJ-ARRAY-H2"  , ps -> ps.setObject(1    , a(ps), H2Type.array(H2Type.VARCHAR)   )),
                n("STR-OBJ-ARRAY-H2-Q", ps -> ps.setObject("bar", a(ps), H2Type.array(H2Type.VARCHAR), 3)),
                n("INT-OBJ-ARRAY-H2-Q", ps -> ps.setObject(1    , a(ps), H2Type.array(H2Type.VARCHAR), 3))
        );

        var t12 = applyBasicValues(
                "SELECT pk, blah FROM foo WHERE axml = ?",
                n("STR-SQLXML"         , ps -> ps.setSQLXML("bar", s(ps)                    )),
                n("INT-SQLXML"         , ps -> ps.setSQLXML(1    , s(ps)                    )),
                n("STR-OBJ-SQLXML"     , ps -> ps.setObject("bar", s(ps)                    )),
                n("INT-OBJ-SQLXML"     , ps -> ps.setObject(1    , s(ps)                    )),
                n("STR-OBJ-SQLXML-TT"  , ps -> ps.setObject("bar", s(ps), Types .VARCHAR    )),
                n("INT-OBJ-SQLXML-TT"  , ps -> ps.setObject(1    , s(ps), Types .VARCHAR    )),
                n("STR-OBJ-SQLXML-TT-Q", ps -> ps.setObject("bar", s(ps), Types .VARCHAR, 14)),
                n("INT-OBJ-SQLXML-TT-Q", ps -> ps.setObject(1    , s(ps), Types .VARCHAR, 14)),
                n("STR-OBJ-SQLXML-H2"  , ps -> ps.setObject("bar", s(ps), H2Type.VARCHAR    )),
                n("INT-OBJ-SQLXML-H2"  , ps -> ps.setObject(1    , s(ps), H2Type.VARCHAR    )),
                n("STR-OBJ-SQLXML-H2-Q", ps -> ps.setObject("bar", s(ps), H2Type.VARCHAR, 14)),
                n("INT-OBJ-SQLXML-H2-Q", ps -> ps.setObject(1    , s(ps), H2Type.VARCHAR, 14))
        );
        return Stream.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12).flatMap(x -> x);
    }

    @MethodSource
    @ParameterizedTest(name = "testSetters {0}")
    public void testSetters(String name, Executable ex) throws Throwable {
        ex.execute();
    }
}
