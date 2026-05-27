package ninja.javahacker.test.annotimpler.sql.stmt;

import ninja.javahacker.annotimpler.convert.ConverterFactory;
import ninja.javahacker.annotimpler.sql.stmt.SmartResultSet;
import ninja.javahacker.test.ControlledMock;
import ninja.javahacker.test.ForTests;

import module java.base;
import module java.sql;
import module org.junit.jupiter.api;

public class SmartResultSetTest {

    @Test
    public void testConstructorFailure() throws Exception {
        var rs = ControlledMock.mock(ResultSet.class);
        var sqle1 = new SQLException();
        rs.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) throw sqle1;
            throw new AssertionError(m);
        });
        var sqle2 = Assertions.assertThrows(SQLException.class, () -> new SmartResultSet(rs.getMock()));
        Assertions.assertSame(sqle1, sqle2);
    }

    private static record TypeData(String label, int type) {
    }

    private ControlledMock<ResultSetMetaData> makeMetaData(List<TypeData> types) {
        var md = ControlledMock.mock(ResultSetMetaData.class);
        md.setHandler((i, m, a) -> {
            var n = m.getName();
            if (n.equals("getColumnCount")) return types.size();
            if (n.equals("getColumnLabel")) return types.get((int) a[0]).label();
            if (n.equals("getColumnType") && types != null) return types.get((int) a[0]).type();
            throw new AssertionError(m);
        });
        return md;
    }

    private SmartResultSet makeMock(List<TypeData> types) throws Exception {
        var md = makeMetaData(types);
        var rs = ControlledMock.mock(ResultSet.class);
        rs.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            throw new AssertionError(m);
        });
        return new SmartResultSet(rs.getMock());
    }

    private SmartResultSet mock0() throws Exception {
        return makeMock(null);
    }

    private SmartResultSet mock1() throws Exception {
        return makeMock(List.of(
                new TypeData("a", Types.INTEGER),
                new TypeData("b", Types.VARCHAR),
                new TypeData("c", Types.TIMESTAMP)
        ));
    }

    private SmartResultSet mockBad() throws Exception {
        return makeMock(List.of(
                new TypeData("aaai", Types.INTEGER),
                new TypeData(null, Types.VARCHAR),
                new TypeData("", Types.BIGINT),
                new TypeData("aaai", Types.TIMESTAMP),
                new TypeData("aAaI", Types.TIMESTAMP_WITH_TIMEZONE),
                new TypeData("aaaı", Types.BLOB) // Dotless lowercase Tukish ı.
        ));
    }

    /*public Stream<DynamicTest> testMapping() throws Exception {
        var map = Map.of("a", Types.INTEGER, "b", Types.VARCHAR, "c", Types.TIMESTAMP);
        return Stream.of(
                DynamicTest.dynamicTest("[testMapping] int-1", () -> mock1().getMap()
    }*/

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        var ni = (int[]) null;
        var ns = (String[]) null;
        var nf = (Function<String, String>) null;
        var rs = ControlledMock.mock(ResultSet.class).getMock();

        record Foo(int x) {
        }

        return Stream.of(
                DynamicTest.dynamicTest("[testNulls] constructor(ResultSet)"                 , () -> ForTests.testNull("rs"         , () -> new SmartResultSet(null))),
                DynamicTest.dynamicTest("[testNulls] constructor(3)-1"                       , () -> ForTests.testNull("rs"         , () -> new SmartResultSet(null, ConverterFactory.STD, Locale.ROOT))),
                DynamicTest.dynamicTest("[testNulls] constructor(3)-2"                       , () -> ForTests.testNull("factory"    , () -> new SmartResultSet(rs, null, Locale.ROOT))),
                DynamicTest.dynamicTest("[testNulls] constructor(3)-3"                       , () -> ForTests.testNull("localizer"  , () -> new SmartResultSet(rs, ConverterFactory.STD, null))),
                DynamicTest.dynamicTest("[testNulls] getMap(int...)"                         , () -> ForTests.testNull("fields"     , () -> mock0().getMap((int[]) null))),
                DynamicTest.dynamicTest("[testNulls] getMap(String...)"                      , () -> ForTests.testNull("fields"     , () -> mock0().getMap((String[]) null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(int, Class)-2"            , () -> ForTests.testNull("target"     , () -> mock0().getTypedValue(1, null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(String, Class)-1"         , () -> ForTests.testNull("columnLabel", () -> mock0().getTypedValue(null, String.class))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(String, Class)-2"         , () -> ForTests.testNull("target"     , () -> mock0().getTypedValue("x", null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(String)"                  , () -> ForTests.testNull("columnLabel", () -> mock0().getTypedValue(null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValueOpt(int, Class)-2"         , () -> ForTests.testNull("target"     , () -> mock0().getTypedValueOpt(1, null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValueOpt(String, Class)-1"      , () -> ForTests.testNull("columnLabel", () -> mock0().getTypedValueOpt(null, String.class))),
                DynamicTest.dynamicTest("[testNulls] getTypedValueOpt(String, Class)-2"      , () -> ForTests.testNull("target"     , () -> mock0().getTypedValueOpt("x", null))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class)"                       , () -> ForTests.testNull("k"          , () -> mock0().getRecord(null))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, int...)-1"             , () -> ForTests.testNull("k"          , () -> mock0().getRecord(null, 1))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, String...)-1"          , () -> ForTests.testNull("k"          , () -> mock0().getRecord(null, "x"))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function)-1"           , () -> ForTests.testNull("k"          , () -> mock0().getRecord(null, x -> x))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, int...)-2"             , () -> ForTests.testNull("fields"     , () -> mock0().getRecord(Foo.class, ni))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, String...)-2"          , () -> ForTests.testNull("fields"     , () -> mock0().getRecord(Foo.class, ns))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function)-2"           , () -> ForTests.testNull("remapper"   , () -> mock0().getRecord(Foo.class, nf))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, int...)-1"   , () -> ForTests.testNull("k"          , () -> mock0().getRecord(null, x -> x, 1))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, String...)-1", () -> ForTests.testNull("k"          , () -> mock0().getRecord(null, x -> x, "x"))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, int...)-2"   , () -> ForTests.testNull("remapper"   , () -> mock0().getRecord(Foo.class, nf, 1))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, String...)-2", () -> ForTests.testNull("remapper"   , () -> mock0().getRecord(Foo.class, nf, "x"))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, int...)-3"   , () -> ForTests.testNull("fields"     , () -> mock0().getRecord(Foo.class, x -> x, ni))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, String...)-3", () -> ForTests.testNull("fields"     , () -> mock0().getRecord(Foo.class, x -> x, ns)))
        );
    }
}
