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

    public SmartResultSetDelegateTest() {
    }

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

    private static final List<TypeData> SIMPLE = List.of(
            new TypeData("f1", Types.INTEGER),
            new TypeData("f2", Types.VARCHAR),
            new TypeData("f3", Types.TIMESTAMP)
    );

    private static final List<TypeData> BAD = List.of(
            new TypeData("aaai", Types.INTEGER),
            new TypeData(null, Types.BLOB),
            new TypeData("", Types.BIGINT),
            new TypeData("aaai", Types.TIMESTAMP),
            new TypeData("aAaI", Types.VARCHAR),
            new TypeData("aaaı", Types.TIMESTAMP_WITH_TIMEZONE) // Dotless lowercase Tukish ı.
    );

    private static final List<TypeData> VACUOUS = List.of(
            new TypeData(null, Types.VARCHAR),
            new TypeData("", Types.BIGINT),
            new TypeData("", Types.VARCHAR)
    );

    private static final List<TypeData> EMPTY = List.of();

    private Object implFor(Method m, Object... a) {
        if (a == null) a = new Object[0];
        for (var p = 0; p < a.length; p++) {
            var av = a[p];
            var out = STUFF.get(m.getParameterTypes()[p]).get(p + 1);
            if (av instanceof Blob || av instanceof Clob || av instanceof SQLXML || av instanceof RowId || av instanceof Ref || av instanceof java.sql.Array || av instanceof Statement) {
                Assertions.assertSame(av, out, List.of(m, p, av.getClass(), out.getClass()).toString());
            } else {
                Assertions.assertEquals(av, out, List.of(m, p, av, out).toString());
            }
        }

        return STUFF.get(m.getReturnType()).get(0);
    }

    private ControlledMock<ResultSetMetaData> makeMetaData(boolean withTypes, List<TypeData> types) {
        var md = ControlledMock.mock(ResultSetMetaData.class);
        md.setHandler((i, m, a) -> {
            var n = m.getName();
            if (n.equals("getColumnCount")) return types.size();
            if (n.equals("getColumnLabel")) return types.get((int) a[0] - 1).label();
            if (n.equals("getColumnType") && withTypes) return types.get((int) a[0] - 1).type();
            throw new AssertionError(m);
        });
        return md;
    }

    @Test
    public void testGetMetaDataOk() throws Exception {
        var md = makeMetaData(false, SIMPLE);
        var rs = ControlledMock.mock(ResultSet.class);
        rs.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            throw new AssertionError(m);
        });
        SmartResultSet s = new SmartResultSet(rs.getMock(), ConverterFactory.STD, Locale.ROOT);
        Assertions.assertSame(md.getMock(), s.getMetaData());
    }

    private void testDelegateMethod(Method mt) throws Exception {
        var md = makeMetaData(false, SIMPLE);
        var rs = ControlledMock.mock(ResultSet.class);
        var lastCall = new Method[1];
        rs.setHandler((i, m, a) -> {
            lastCall[0] = m;
            if (m.getName().equals("getMetaData")) return md.getMock();

            Assertions.assertEquals(mt.getName(), m.getName());
            Assertions.assertEquals(mt.getReturnType(), m.getReturnType());
            Assertions.assertArrayEquals(mt.getParameterTypes(), m.getParameterTypes());

            return implFor(m, a);
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

    private SmartResultSet makeMock(List<TypeData> types, ConverterFactory factory, Locale loc) throws Exception {
        var md = makeMetaData(true, types);
        var rs = ControlledMock.mock(ResultSet.class);
        rs.setHandler((i, m, a) -> {
            var n = m.getName();
            if (n.equals("getMetaData")) return md.getMock();
            if (n.equals("getInt")) return 5;
            if (n.equals("getString")) return "abc";
            if (n.equals("getObject") && a[1] == LocalDateTime.class) return LocalDateTime.of(2026, 5, 28, 10, 11, 12);
            if (n.equals("wasNull")) return false;
            throw new AssertionError(m + "-" + Arrays.asList(a));
        });
        SmartResultSet s = new SmartResultSet(rs.getMock(), factory, loc);
        Assertions.assertSame(md.getMock(), s.getMetaData());
        return s;
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testMapping() throws Exception {
        var map1 = Map.of("F1", 5, "F2", "abc", "F3", LocalDateTime.of(2026, 5, 28, 10, 11, 12));
        var map1b = Map.of("F1", 5, "F3", LocalDateTime.of(2026, 5, 28, 10, 11, 12));
        var map2 = Map.of("AAAI", 5);
        var map3 = Map.<String, Object>of();
        var map4 = Map.of("AAAI", "abc", "AAAİ", 5);
        var turkish = Locale.forLanguageTag("TR-tr");
        return Stream.of(
                DynamicTest.dynamicTest("[testMapping] empty"     , () -> Assertions.assertEquals(map3 , makeMock(EMPTY  , ConverterFactory.STD, Locale.ROOT).getMap())),
                DynamicTest.dynamicTest("[testMapping] empty N"   , () -> Assertions.assertEquals(map3 , makeMock(EMPTY  , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers())),
                DynamicTest.dynamicTest("[testMapping] empty S"   , () -> Assertions.assertEquals(map3 , makeMock(EMPTY  , ConverterFactory.STD, Locale.ROOT).getMapByLabels())),

                DynamicTest.dynamicTest("[testMapping] simple"    , () -> Assertions.assertEquals(map1 , makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMap())),
                DynamicTest.dynamicTest("[testMapping] simple S"  , () -> Assertions.assertEquals(map1 , makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 2, 3))),
                DynamicTest.dynamicTest("[testMapping] simple S p", () -> Assertions.assertEquals(map1b, makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 3))),
                DynamicTest.dynamicTest("[testMapping] simple S r", () -> Assertions.assertEquals(map1 , makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(3, 1, 2, 1, 2, 2, 3, 1))),
                DynamicTest.dynamicTest("[testMapping] simple N"  , () -> Assertions.assertEquals(map1 , makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByLabels("f1", "F2", "f3"))),
                DynamicTest.dynamicTest("[testMapping] simple N p", () -> Assertions.assertEquals(map1b, makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByLabels("f1", "F3"))),
                DynamicTest.dynamicTest("[testMapping] simple N r", () -> Assertions.assertEquals(map1b, makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByLabels("f3", "F3", "f1", "F1", "F1"))),
                DynamicTest.dynamicTest("[testMapping] no labels" , () -> Assertions.assertEquals(map3 , makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByLabels())),
                DynamicTest.dynamicTest("[testMapping] no nums"   , () -> Assertions.assertEquals(map3 , makeMock(SIMPLE , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers())),

                DynamicTest.dynamicTest("[testMapping] bad keys"  , () -> Assertions.assertEquals(map2 , makeMock(BAD    , ConverterFactory.STD, Locale.ROOT).getMap())),
                DynamicTest.dynamicTest("[testMapping] bad keys N", () -> Assertions.assertEquals(map2 , makeMock(BAD    , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 2, 3, 4, 5, 6))),
                DynamicTest.dynamicTest("[testMapping] bad keys S", () -> Assertions.assertEquals(map2 , makeMock(BAD    , ConverterFactory.STD, Locale.ROOT).getMapByLabels("aaAi"))),
                DynamicTest.dynamicTest("[testMapping] bad keys X", () -> Assertions.assertEquals(map3 , makeMock(BAD    , ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(2))),
                DynamicTest.dynamicTest("[testMapping] bad keys Y", () -> Assertions.assertEquals(map3 , makeMock(BAD    , ConverterFactory.STD, Locale.ROOT).getMapByLabels())),

                DynamicTest.dynamicTest("[testMapping] vacuous"   , () -> Assertions.assertEquals(map3 , makeMock(VACUOUS, ConverterFactory.STD, Locale.ROOT).getMap())),
                DynamicTest.dynamicTest("[testMapping] vacuous N" , () -> Assertions.assertEquals(map3 , makeMock(VACUOUS, ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 2, 3))),

                DynamicTest.dynamicTest("[testMapping] turkish"   , () -> Assertions.assertEquals(map4 , makeMock(BAD    , ConverterFactory.STD, turkish    ).getMap())),
                DynamicTest.dynamicTest("[testMapping] turkish S" , () -> Assertions.assertEquals(map4 , makeMock(BAD    , ConverterFactory.STD, turkish    ).getMapByLabels("AAAi", "aaaI")))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testDoNotExists() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("[testDoNotExists] column number 4",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 2, 3, 4), "There is no column 2.")
                ),
                DynamicTest.dynamicTest("[testDoNotExists] column number 0",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 0, 2), "There is no column 0.")
                ),
                DynamicTest.dynamicTest("[testDoNotExists] column number -1",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, -1, 2), "There is no column -1.")
                ),
                DynamicTest.dynamicTest("[testDoNotExists] column number 5 and 4",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByColumnNumbers(1, 2, 5, 3, 4), "There is no column 5.")
                ),
                DynamicTest.dynamicTest("[testDoNotExists] column number 5 and 4",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByLabels("bla"), "There is no column \"bla\".")
                ),
                DynamicTest.dynamicTest("[testDoNotExists] column number 5 and 4",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByLabels("f1", "f2", ""), "There is no column \"\".")
                ),
                DynamicTest.dynamicTest("[testDoNotExists] column number 5 and 4",
                        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> makeMock(SIMPLE, ConverterFactory.STD, Locale.ROOT).getMapByLabels("f1", null, "f1"), "Null-named columns are not allowed.")
                )
        );
    }
}
