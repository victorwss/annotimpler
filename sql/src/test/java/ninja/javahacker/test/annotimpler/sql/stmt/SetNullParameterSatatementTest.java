package ninja.javahacker.test.annotimpler.sql.stmt;

import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class SetNullParameterSatatementTest {

    @FunctionalInterface
    public static interface StatementContext {
        public void doIt(NamedParameterStatement con) throws Exception;
    }

    public static record NamedStatement(String name, StatementContext ctx) {
    }

    private static NamedStatement n(String name, StatementContext ctx) {
        return new NamedStatement(name, ctx);
    }

    @FunctionalInterface
    public static interface ResultContext {
        public void doIt(ResultSet rs) throws Exception;
    }

    public static NamedTest makeStuff(
            Map<String, List<Integer>> idx,
            List<String> prepare,
            String insert,
            String select,
            NamedStatement test1,
            ResultContext test2)
    {
        return new NamedTest(test1.name, () -> {
            try (var con = H2Connector.std().withMemory(true).get()) {
                for (var sqlp : prepare) {
                    try (var ps = con.prepareStatement(sqlp)) {
                        ps.executeUpdate();
                    }
                }
                try (var ps = NamedParameterStatement.prepareNamedStatement(con, insert, idx)) {
                    test1.ctx.doIt(ps);
                    ps.executeUpdate();
                }
                try (var ps = con.prepareStatement(select)) {
                    try (var rs = ps.executeQuery()) {
                        Assertions.assertTrue(rs.next(), "Assert has 1st line");
                        test2.doIt(rs);
                        Assertions.assertFalse(rs.next(), "Assert had only one line");
                    }
                }
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

    private static Stream<Arguments> testSetNullSimple() {
        var str = String.class.getName();
        var time = java.sql.Timestamp.class.getName();
        var gc = new GregorianCalendar();
        var a = Stream.<NamedStatement>of(
                n("STR-NULL-VARCHAR"       , ps -> ps.setNull         ("bar", Types.VARCHAR                          )),
                n("INT-NULL-VARCHAR"       , ps -> ps.setNull         (1    , Types.VARCHAR                          )),
                n("STR-NULL-VARCHAR-NAME"  , ps -> ps.setNull         ("bar", Types.VARCHAR, str                     )),
                n("INT-NULL-VARCHAR-NAME"  , ps -> ps.setNull         (1    , Types.VARCHAR, str                     )),
                n("STR-OPTINT-EMPTY"       , ps -> ps.setInt          ("bar", OptionalInt   .empty()                 )),
                n("INT-OPTINT-EMPTY"       , ps -> ps.setInt          (1    , OptionalInt   .empty()                 )),
                n("STR-OPTLONG-EMPTY"      , ps -> ps.setLong         ("bar", OptionalLong  .empty()                 )),
                n("INT-OPTLONG-EMPTY"      , ps -> ps.setLong         (1    , OptionalLong  .empty()                 )),
                n("STR-OPTDOUBLE-EMPTY"    , ps -> ps.setDouble       ("bar", OptionalDouble.empty()                 )),
                n("INT-OPTDOUBLE-EMPTY"    , ps -> ps.setDouble       (1    , OptionalDouble.empty()                 )),
                n("STR-STRING"             , ps -> ps.setString       ("bar",               null                     )),
                n("INT-STRING"             , ps -> ps.setString       (1    ,               null                     )),
                n("STR-OBJECT"             , ps -> ps.setObject       ("bar",               null                     )),
                n("INT-OBJECT"             , ps -> ps.setObject       (1    ,               null                     )),
                n("STR-OBJ-VARCHAR"        , ps -> ps.setObject       ("bar",               null, Types .VARCHAR     )),
                n("INT-OBJ-VARCHAR"        , ps -> ps.setObject       (1    ,               null, Types .VARCHAR     )),
                n("STR-OBJ-VARCHAR-H2"     , ps -> ps.setObject       ("bar",               null, H2Type.VARCHAR     )),
                n("INT-OBJ-VARCHAR-H2"     , ps -> ps.setObject       (1    ,               null, H2Type.VARCHAR     )),
                n("STR-OBJ-VARCHAR-Z"      , ps -> ps.setObject       ("bar",               null, Types .VARCHAR, 0  )),
                n("INT-OBJ-VARCHAR-Z"      , ps -> ps.setObject       (1    ,               null, Types .VARCHAR, 0  )),
                n("STR-OBJ-VARCHAR-H2-Z"   , ps -> ps.setObject       ("bar",               null, H2Type.VARCHAR, 0  )),
                n("INT-OBJ-VARCHAR-H2-Z"   , ps -> ps.setObject       (1    ,               null, H2Type.VARCHAR, 0  )),
                n("STR-BLOB"               , ps -> ps.setBlob         ("bar", (Blob)        null                     )),
                n("INT-BLOB"               , ps -> ps.setBlob         (1    , (Blob)        null                     )),
                n("STR-BLOB-STREAM"        , ps -> ps.setBlob         ("bar", (InputStream) null                     )),
                n("INT-BLOB-STREAM"        , ps -> ps.setBlob         (1    , (InputStream) null                     )),
                n("STR-BLOB-Z"             , ps -> ps.setBlob         ("bar",               null, 0L                 )),
                n("INT-BLOB-Z"             , ps -> ps.setBlob         (1    ,               null, 0L                 )),
                n("STR-CLOB"               , ps -> ps.setClob         ("bar", (Clob)        null                     )),
                n("INT-CLOB"               , ps -> ps.setClob         (1    , (Clob)        null                     )),
                n("STR-CLOB-READER"        , ps -> ps.setClob         ("bar", (Reader)      null                     )),
                n("INT-CLOB-READER"        , ps -> ps.setClob         (1    , (Reader)      null                     )),
                n("STR-CLOB-Z"             , ps -> ps.setClob         ("bar",               null, 0L                 )),
                n("INT-CLOB-Z"             , ps -> ps.setClob         (1    ,               null, 0L                 )),
                n("STR-NCLOB"              , ps -> ps.setNClob        ("bar", (NClob)       null                     )),
                n("INT-NCLOB"              , ps -> ps.setNClob        (1    , (NClob)       null                     )),
                n("STR-NCLOB-READER"       , ps -> ps.setNClob        ("bar", (Reader)      null                     )),
                n("INT-NCLOB-READER"       , ps -> ps.setNClob        (1    , (Reader)      null                     )),
                n("STR-NCLOB-Z"            , ps -> ps.setNClob        ("bar",               null, 0L                 )),
                n("INT-NCLOB-Z"            , ps -> ps.setNClob        (1    ,               null, 0L                 )),
                n("STR-OBJ-BLOB"           , ps -> ps.setObject       ("bar",               null, Types .BLOB        )),
                n("INT-OBJ-BLOB"           , ps -> ps.setObject       (1    ,               null, Types .BLOB        )),
                n("STR-OBJ-CLOB"           , ps -> ps.setObject       ("bar",               null, Types .CLOB        )),
                n("INT-OBJ-CLOB"           , ps -> ps.setObject       (1    ,               null, Types .CLOB        )),
                n("STR-OBJ-BLOB-H2"        , ps -> ps.setObject       ("bar",               null, H2Type.BLOB        )),
                n("INT-OBJ-BLOB-H2"        , ps -> ps.setObject       (1    ,               null, H2Type.BLOB        )),
                n("STR-OBJ-CLOB-H2"        , ps -> ps.setObject       ("bar",               null, H2Type.CLOB        )),
                n("INT-OBJ-CLOB-H2"        , ps -> ps.setObject       (1    ,               null, H2Type.CLOB        )),
                n("STR-OBJ-BLOB-Z"         , ps -> ps.setObject       ("bar",               null, Types .BLOB, 0     )),
                n("INT-OBJ-BLOB-Z"         , ps -> ps.setObject       (1    ,               null, Types .BLOB, 0     )),
                n("STR-OBJ-CLOB-Z"         , ps -> ps.setObject       ("bar",               null, Types .CLOB, 0     )),
                n("INT-OBJ-CLOB-Z"         , ps -> ps.setObject       (1    ,               null, Types .CLOB, 0     )),
                n("STR-OBJ-BLOB-H2-Z"      , ps -> ps.setObject       ("bar",               null, H2Type.BLOB, 0     )),
                n("INT-OBJ-BLOB-H2-Z"      , ps -> ps.setObject       (1    ,               null, H2Type.BLOB, 0     )),
                n("STR-OBJ-CLOB-H2-Z"      , ps -> ps.setObject       ("bar",               null, H2Type.CLOB, 0     )),
                n("INT-OBJ-CLOB-H2-Z"      , ps -> ps.setObject       (1    ,               null, H2Type.CLOB, 0     ))
        );
        var b = Stream.<NamedStatement>of(
                n("STR-NULL-TIMESTAMP"     , ps -> ps.setNull         ("bar", Types.TIMESTAMP                        )),
                n("INT-NULL-TIMESTAMP"     , ps -> ps.setNull         (1    , Types.TIMESTAMP                        )),
                n("STR-NULL-TIMESTAMP-NAME", ps -> ps.setNull         ("bar", Types.TIMESTAMP   , time               )),
                n("STR-NULL-TIMESTAMP-NAME", ps -> ps.setNull         (1    , Types.TIMESTAMP   , time               )),
                n("STR-DATE"               , ps -> ps.setDate         ("bar",               null                     )),
                n("INT-DATE"               , ps -> ps.setDate         (1    ,               null                     )),
                n("STR-TIME"               , ps -> ps.setTime         ("bar",               null                     )),
                n("INT-TIME"               , ps -> ps.setTime         (1    ,               null                     )),
                n("STR-TIMESTAMP"          , ps -> ps.setTimestamp    ("bar",               null                     )),
                n("INT-TIMESTAMP"          , ps -> ps.setTimestamp    (1    ,               null                     )),
                n("STR-DATE-CAL-NULL"      , ps -> ps.setDate         ("bar",               null, null               )),
                n("INT-DATE-CAL-NULL"      , ps -> ps.setDate         (1    ,               null, null               )),
                n("STR-TIME-CAL-NULL"      , ps -> ps.setTime         ("bar",               null, null               )),
                n("INT-TIME-CAL-NULL"      , ps -> ps.setTime         (1    ,               null, null               )),
                n("STR-TIMESTAMP-CAL-NULL" , ps -> ps.setTimestamp    ("bar",               null, null               )),
                n("INT-TIMESTAMP-CAL-NULL" , ps -> ps.setTimestamp    (1    ,               null, null               )),
                n("STR-DATE-CAL-NEW"       , ps -> ps.setDate         ("bar",               null, gc                 )),
                n("INT-DATE-CAL-NEW"       , ps -> ps.setDate         (1    ,               null, gc                 )),
                n("STR-TIME-CAL-NEW"       , ps -> ps.setTime         ("bar",               null, gc                 )),
                n("INT-TIME-CAL-NEW"       , ps -> ps.setTime         (1    ,               null, gc                 )),
                n("STR-TIMESTAMP-CAL-NEW"  , ps -> ps.setTimestamp    ("bar",               null, gc                 )),
                n("INT-TIMESTAMP-CAL-NEW"  , ps -> ps.setTimestamp    (1    ,               null, gc                 )),
                n("STR-LOCALDATE"          , ps -> ps.setLocalDate    ("bar",               null                     )),
                n("INT-LOCALDATE"          , ps -> ps.setLocalDate    (1    ,               null                     )),
                n("STR-LOCALTIME"          , ps -> ps.setLocalTime    ("bar",               null                     )),
                n("INT-LOCALTIME"          , ps -> ps.setLocalTime    (1    ,               null                     )),
                n("STR-LOCALDATETIME"      , ps -> ps.setLocalDateTime("bar",               null                     )),
                n("INT-LOCALDATETIME"      , ps -> ps.setLocalDateTime(1    ,               null                     )),
                n("STR-OBJ-TIMESTAMP"      , ps -> ps.setObject       ("bar",               null, Types .TIMESTAMP   )),
                n("INT-OBJ-TIMESTAMP"      , ps -> ps.setObject       (1    ,               null, Types .TIMESTAMP   )),
                n("STR-OBJ-TIMESTAMP-H2"   , ps -> ps.setObject       ("bar",               null, H2Type.TIMESTAMP   )),
                n("INT-OBJ-TIMESTAMP-H2"   , ps -> ps.setObject       (1    ,               null, H2Type.TIMESTAMP   )),
                n("STR-OBJ-TIMESTAMP-Z"    , ps -> ps.setObject       ("bar",               null, Types .TIMESTAMP, 0)),
                n("INT-OBJ-TIMESTAMP-Z"    , ps -> ps.setObject       (1    ,               null, Types .TIMESTAMP, 0)),
                n("STR-OBJ-TIMESTAMP-H2-Z" , ps -> ps.setObject       ("bar",               null, H2Type.TIMESTAMP, 0)),
                n("INT-OBJ-TIMESTAMP-H2-Z" , ps -> ps.setObject       (1    ,               null, H2Type.TIMESTAMP, 0))
        );
        var idx = Map.of("bar", List.of(1));
        var prepare1 = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4));",
                "INSERT INTO foo(pk, blah) VALUES (1, 'whoa');"
        );
        var prepare2 = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah TIMESTAMP);",
                "INSERT INTO foo(pk, blah) VALUES (1, '2026-01-02 03:04:05.678');"
        );
        var insert = "INSERT INTO foo(pk, blah) VALUES (2, ?);";
        var select = "SELECT pk, blah FROM foo WHERE pk = 2";
        ResultContext rsc1 = rs -> {
                Assertions.assertEquals(2, rs.getInt("pk"));
                Assertions.assertEquals(2, rs.getInt(1));
                Assertions.assertEquals(null, rs.getString("blah"));
                Assertions.assertEquals(null, rs.getString(2));
        };
        ResultContext rsc2 = rs -> {
                Assertions.assertEquals(2, rs.getInt("pk"));
                Assertions.assertEquals(2, rs.getInt(1));
                Assertions.assertEquals(null, rs.getTimestamp("blah"));
                Assertions.assertEquals(null, rs.getTimestamp(2));
        };
        var a2 = a.map(s -> makeStuff(idx, prepare1, insert, select, s, rsc1));
        var b2 = b.map(s -> makeStuff(idx, prepare2, insert, select, s, rsc2));
        return Stream.concat(a2, b2).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testSetNullSimple {0}")
    public void testSetNullSimple(String name, Executable s) throws Throwable {
        s.execute();
    }
}
