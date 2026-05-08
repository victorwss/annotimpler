package ninja.javahacker.test.annotimpler.sql.stmt;

import ninja.javahacker.annotimpler.convert.ConverterFactory;
import ninja.javahacker.annotimpler.sql.stmt.SmartResultSet;
import ninja.javahacker.test.ControlledMock;
import ninja.javahacker.test.ForTests;

import module java.base;
import module java.sql;
import module org.junit.jupiter.api;

public class SmartResultTest {

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

    private SmartResultSet getMock() throws Exception {
        var md = ControlledMock.mock(ResultSetMetaData.class);
        md.setHandler((i, m, a) -> {
            if (m.getName().equals("getColumnCount")) return 3;
            if (m.getName().equals("getColumnLabel")) return new String[] {"a", null, ""}[(int) a[0] - 1];
            if (m.getName().equals("getColumnName")) {
                var p = (int) a[0];
                if (p == 2) return "b";
                if (p == 3) return "c";
                throw new AssertionError(p);
            }
            throw new AssertionError(m);
        });
        var rs = ControlledMock.mock(ResultSet.class);
        rs.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            throw new AssertionError(m);
        });
        return new SmartResultSet(rs.getMock());
    }

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
                DynamicTest.dynamicTest("[testNulls] constructor(ResultSet)"                    , () -> ForTests.testNull("rs"         , () -> new SmartResultSet(null))),
                DynamicTest.dynamicTest("[testNulls] constructor(ResultSet, ConverterFactory)-1", () -> ForTests.testNull("rs"         , () -> new SmartResultSet(null, ConverterFactory.STD))),
                DynamicTest.dynamicTest("[testNulls] constructor(ResultSet, ConverterFactory)-2", () -> ForTests.testNull("factory"    , () -> new SmartResultSet(rs, null))),
                DynamicTest.dynamicTest("[testNulls] getMap(int...)"                            , () -> ForTests.testNull("fields"     , () -> getMock().getMap((int[]) null))),
                DynamicTest.dynamicTest("[testNulls] getMap(String...)"                         , () -> ForTests.testNull("fields"     , () -> getMock().getMap((String[]) null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(int, Class)-2"               , () -> ForTests.testNull("target"     , () -> getMock().getTypedValue(1, null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(String, Class)-1"            , () -> ForTests.testNull("columnLabel", () -> getMock().getTypedValue(null, String.class))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(String, Class)-2"            , () -> ForTests.testNull("target"     , () -> getMock().getTypedValue("x", null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValue(String)"                     , () -> ForTests.testNull("columnLabel", () -> getMock().getTypedValue(null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValueOpt(int, Class)-2"            , () -> ForTests.testNull("target"     , () -> getMock().getTypedValueOpt(1, null))),
                DynamicTest.dynamicTest("[testNulls] getTypedValueOpt(String, Class)-1"         , () -> ForTests.testNull("columnLabel", () -> getMock().getTypedValueOpt(null, String.class))),
                DynamicTest.dynamicTest("[testNulls] getTypedValueOpt(String, Class)-2"         , () -> ForTests.testNull("target"     , () -> getMock().getTypedValueOpt("x", null))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class)"                          , () -> ForTests.testNull("k"          , () -> getMock().getRecord(null))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, int...)-1"                , () -> ForTests.testNull("k"          , () -> getMock().getRecord(null, 1))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, String...)-1"             , () -> ForTests.testNull("k"          , () -> getMock().getRecord(null, "x"))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function)-1"              , () -> ForTests.testNull("k"          , () -> getMock().getRecord(null, x -> x))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, int...)-2"                , () -> ForTests.testNull("fields"     , () -> getMock().getRecord(Foo.class, ni))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, String...)-2"             , () -> ForTests.testNull("fields"     , () -> getMock().getRecord(Foo.class, ns))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function)-2"              , () -> ForTests.testNull("remapper"   , () -> getMock().getRecord(Foo.class, nf))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, int...)-1"      , () -> ForTests.testNull("k"          , () -> getMock().getRecord(null, x -> x, 1))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, String...)-1"   , () -> ForTests.testNull("k"          , () -> getMock().getRecord(null, x -> x, "x"))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, int...)-2"      , () -> ForTests.testNull("remapper"   , () -> getMock().getRecord(Foo.class, nf, 1))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, String...)-2"   , () -> ForTests.testNull("remapper"   , () -> getMock().getRecord(Foo.class, nf, "x"))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, int...)-3"      , () -> ForTests.testNull("fields"     , () -> getMock().getRecord(Foo.class, x -> x, ni))),
                DynamicTest.dynamicTest("[testNulls] getRecord(Class, Function, String...)-3"   , () -> ForTests.testNull("fields"     , () -> getMock().getRecord(Foo.class, x -> x, ns)))
        );
    }
}
