package ninja.javahacker.test.annotimpler.sql.stmt;

import ninja.javahacker.test.ControlledMock;
import ninja.javahacker.test.limited.AssertionInputStream;
import ninja.javahacker.test.limited.AssertionReader;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class SmartResultSetDelegateTest {

    private static final class TestA {}
    private static final class TestB {}
    private static final class TestC {}

    private static Reader r(int c) {
        return new AssertionReader("aaa" + c, 4, false);
    }

    private static InputStream i(int i) {
        return new AssertionInputStream(("aaa" + i).getBytes(), 4, false);
    }

    private static Blob blob() {
        return ControlledMock.mock(Blob.class).getMock();
    }

    private static Clob clob() {
        return ControlledMock.mock(Clob.class).getMock();
    }

    private static NClob nclob() {
        return ControlledMock.mock(NClob.class).getMock();
    }

    private static SQLXML sqlxml() {
        return ControlledMock.mock(SQLXML.class).getMock();
    }

    private static RowId rowid() {
        return ControlledMock.mock(RowId.class).getMock();
    }

    private static Ref ref() {
        return ControlledMock.mock(Ref.class).getMock();
    }

    private static java.sql.Array array() {
        return ControlledMock.mock(java.sql.Array.class).getMock();
    }

    private static Statement stmt() {
        return ControlledMock.mock(Statement.class).getMock();
    }

    private static URL url() {
        try {
            return new URI("http://localhost").toURL();
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }

    private static final Map<Class<?>, List<?>> STUFF = Map.ofEntries(
            Map.entry(int.class, List.of(1, 2, 3, 4, 5)),
            Map.entry(String.class, List.of("a", "b", "c", "d")),
            Map.entry(Class.class, List.of(TestA.class, TestB.class, TestC.class)),
            Map.entry(long.class, List.of(1L, 2L, 3L, 4L)),
            Map.entry(boolean.class, List.of(false, true, false, true)),
            Map.entry(short.class, List.of((short) 0, (short) 1, (short) 2)),
            Map.entry(float.class, List.of(1.0F, 2.0F, 3.0F)),
            Map.entry(double.class, List.of(1.0, 2.0, 3.0)),
            Map.entry(byte.class, List.of((byte) 0, (byte) 1, (byte) 2)),
            Map.entry(BigDecimal.class, List.of(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN)),
            Map.entry(Object.class, List.of(new TestA(), new TestB(), new TestC())),
            Map.entry(Reader.class, List.of(r(1), r(2), r(3))),
            Map.entry(InputStream.class, List.of(i(1), i(2), i(3))),
            Map.entry(Clob.class, List.of(clob(), clob(), clob())),
            Map.entry(Blob.class, List.of(blob(), blob(), blob())),
            Map.entry(NClob.class, List.of(nclob(), nclob(), nclob())),
            Map.entry(byte[].class, List.of(new byte[1], new byte[2], new byte[3])),
            Map.entry(SQLXML.class, List.of(sqlxml(), sqlxml(), sqlxml())),
            Map.entry(RowId.class, List.of(rowid(), rowid(), rowid())),
            Map.entry(Ref.class, List.of(ref(), ref(), ref())),
            Map.entry(java.sql.Array.class, List.of(array(), array(), array())),
            Map.entry(void.class, Arrays.asList((Object) null)),
            Map.entry(java.sql.Date.class, List.of(
                    java.sql.Date.valueOf(LocalDate.of(2020,  1,  2)),
                    java.sql.Date.valueOf(LocalDate.of(2021,  6,  7)),
                    java.sql.Date.valueOf(LocalDate.of(2022, 11, 12))
            )),
            Map.entry(java.sql.Time.class, List.of(
                    java.sql.Time.valueOf(LocalTime.of( 3,  4,  5)),
                    java.sql.Time.valueOf(LocalTime.of( 8,  9, 10)),
                    java.sql.Time.valueOf(LocalTime.of(13, 14, 15))
            )),
            Map.entry(java.sql.Timestamp.class, List.of(
                    java.sql.Timestamp.valueOf(LocalDateTime.of(2020,  1,  2,  3,  4,  5)),
                    java.sql.Timestamp.valueOf(LocalDateTime.of(2021,  6,  7,  8,  9, 10)),
                    java.sql.Timestamp.valueOf(LocalDateTime.of(2022, 11, 12, 13, 14, 15))
            )),
            Map.entry(Calendar.class, List.of(
                    GregorianCalendar.from(LocalDateTime.of(2020,  1,  2,  3,  4,  5).atZone(ZoneOffset.UTC)),
                    GregorianCalendar.from(LocalDateTime.of(2021,  6,  7,  8,  9, 10).atZone(ZoneOffset.UTC)),
                    GregorianCalendar.from(LocalDateTime.of(2022, 11, 12, 13, 14, 15).atZone(ZoneOffset.UTC))
            )),
            Map.entry(SQLType.class, List.of(H2Type.CHAR, H2Type.DATE, H2Type.VARCHAR, H2Type.INTEGER)),
            Map.entry(URL.class, List.of(url())),
            Map.entry(SQLWarning.class, List.of(new SQLWarning("foo"))),
            Map.entry(Statement.class, List.of(stmt())),
            Map.entry(Map.class, Arrays.asList(null, null, Map.of("bla", TestA.class)))
    );

    private static record TypeData(String label, int type) {
    }

    private ControlledMock<ResultSetMetaData> makeMetaData(List<TypeData> types) {
        var md = ControlledMock.mock(ResultSetMetaData.class);
        md.setHandler((i, m, a) -> {
            var n = m.getName();
            if (n.equals("getColumnCount")) return 3;
            if (n.equals("getColumnLabel")) return new String[] {"a", null, ""}[(int) a[0] - 1];
            if (n.equals("getColumnType") && types != null) return types.get((int) a[0]).type();
            throw new AssertionError(m);
        });
        return md;
    }

    @Test
    public void testGetMetaDataOk() throws Exception {
        var md = makeMetaData(null);
        var rs = ControlledMock.mock(ResultSet.class);
        rs.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            throw new AssertionError(m);
        });
        SmartResultSet s = new SmartResultSet(rs.getMock());
        Assertions.assertSame(md.getMock(), s.getMetaData());
    }

    private void testDelegateMethod(Method mt) throws Exception {
        var md = makeMetaData(null);
        var rs = ControlledMock.mock(ResultSet.class);
        var lastCall = new Method[1];
        rs.setHandler((i, m, a) -> {
            lastCall[0] = m;
            if (m.getName().equals("getMetaData")) return md.getMock();

            Assertions.assertEquals(mt.getName(), m.getName());
            Assertions.assertEquals(mt.getReturnType(), m.getReturnType());
            Assertions.assertArrayEquals(mt.getParameterTypes(), m.getParameterTypes());

            if (a == null) a = new Object[0];
            for (var p = 0; p < a.length; p++) {
                var ret = a[p];
                var out = STUFF.get(mt.getParameterTypes()[p]).get(p + 1);
                if (ret instanceof Blob || ret instanceof Clob || ret instanceof SQLXML || ret instanceof RowId || ret instanceof Ref || ret instanceof java.sql.Array || ret instanceof Statement) {
                    Assertions.assertSame(ret, out);
                } else {
                    Assertions.assertEquals(ret, out);
                }
            }

            return STUFF.get(mt.getReturnType()).get(0);
        });
        SmartResultSet s = new SmartResultSet(rs.getMock());
        var ps = new Object[mt.getParameterCount()];
        for (var i = 0; i < ps.length; i++) {
            ps[i] = STUFF.get(mt.getParameterTypes()[i]).get(i + 1);
        }
        var ret = STUFF.get(mt.getReturnType()).get(0);
        var out = mt.invoke(s, ps);
        if (ret instanceof Blob || ret instanceof Clob || ret instanceof SQLXML || ret instanceof RowId || ret instanceof Ref || ret instanceof java.sql.Array || ret instanceof Statement) {
            Assertions.assertSame(ret, out);
        } else {
            Assertions.assertEquals(ret, out);
        }
        Assertions.assertEquals(mt, lastCall[0]);
    }

    @TestFactory
    public Stream<DynamicTest> testDelegateMethods() throws Exception {
        return Stream.of(ResultSet.class.getMethods())
                .filter(mt -> !"getMetaData".equals(mt.getName()))
                .map(mt -> DynamicTest.dynamicTest("[testDelegateMethods] " + MethodWrapper.of(mt).toString(), () -> testDelegateMethod(mt)));
    }
}
