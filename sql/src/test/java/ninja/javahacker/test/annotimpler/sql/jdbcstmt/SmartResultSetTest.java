package ninja.javahacker.test.annotimpler.sql.jdbcstmt;

import ninja.javahacker.test.ControlledMock;
import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.limited.AssertionInputStream;
import ninja.javahacker.test.limited.AssertionReader;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
public class SmartResultSetTest {

    private static final class TestA {}
    private static final class TestB {}
    private static final class TestC {}

    public SmartResultSetTest() {
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

    private SmartResultSet mock0() throws Exception {
        var md = makeMetaData(false, List.of(new TypeData("X", Types.VARCHAR)));
        var rs = ControlledMock.mock(ResultSet.class);
        rs.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            throw new AssertionError(m);
        });
        return new SmartResultSet(rs.getMock());
    }

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

    @TestFactory
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

    private SmartResultSet makeTypedMock(int sqlType, InvocationHandler rsHandler) throws Exception {
        var mdMock = ControlledMock.mock(ResultSetMetaData.class);
        mdMock.setHandler((px, m, a) -> {
            if ("getColumnCount".equals(m.getName())) return 1;
            if ("getColumnLabel".equals(m.getName())) return "COL";
            if ("getColumnType".equals(m.getName())) return sqlType;
            throw new AssertionError(m.getName());
        });
        var rsMock = ControlledMock.mock(ResultSet.class);
        rsMock.setHandler((px, m, a) -> {
            if ("getMetaData".equals(m.getName())) return mdMock.getMock();
            return rsHandler.invoke(px, m, a);
        });
        return new SmartResultSet(rsMock.getMock(), ConverterFactory.STD, Locale.ROOT);
    }

    @TestFactory
    public Stream<DynamicTest> testGetTypedValueByIndex() throws Exception {
        var aBlob            = blob();
        var aClob            = clob();
        var aNClob           = nclob();
        var aSqlXml          = sqlxml();
        var aRowId           = rowid();
        var aRef             = ref();
        var anArray          = array();
        var aStruct          = ControlledMock.mock(Struct.class).getMock();
        var aLocalDate       = LocalDate.of(2023, 5, 15);
        var aLocalDateTime   = LocalDateTime.of(2023, 5, 15, 10, 20, 30);
        var aLocalTime       = LocalTime.of(10, 20, 30);
        var anOffsetDateTime = OffsetDateTime.of(2023, 5, 15, 10, 20, 30, 0, ZoneOffset.UTC);
        var anOffsetTime     = OffsetTime.of(10, 20, 30, 0, ZoneOffset.UTC);
        var aBytes           = new byte[] {1, 2, 3};
        var aBigDecimal      = new BigDecimal("3.14");
        var aString          = "hello";

        var pf = "[testGetTypedValueByIndex] ";
        var tests = new ArrayList<DynamicTest>(50);

        // REF_CURSOR → UnsupportedOperationException, no getter called
        tests.add(DynamicTest.dynamicTest(pf + "REF_CURSOR throws UnsupportedOperationException", () -> {
            var srs = makeTypedMock(Types.REF_CURSOR, (px, m, a) -> { throw new AssertionError(m.getName()); });
            Assertions.assertThrows(UnsupportedOperationException.class, () -> srs.getTypedValue(1));
        }));

        // NULL → null, no getter called
        tests.add(DynamicTest.dynamicTest(pf + "NULL returns null without any getter call", () -> {
            var srs = makeTypedMock(Types.NULL, (px, m, a) -> { throw new AssertionError(m.getName()); });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // DATE → getObject(int, LocalDate.class)
        tests.add(DynamicTest.dynamicTest(pf + "DATE uses getObject with LocalDate", () -> {
            var srs = makeTypedMock(Types.DATE, (px, m, a) -> {
                Assertions.assertEquals("getObject", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                Assertions.assertEquals(LocalDate.class, a[1]);
                return aLocalDate;
            });
            Assertions.assertEquals(aLocalDate, srs.getTypedValue(1));
        }));

        // TIMESTAMP → getObject(int, LocalDateTime.class)
        tests.add(DynamicTest.dynamicTest(pf + "TIMESTAMP uses getObject with LocalDateTime", () -> {
            var srs = makeTypedMock(Types.TIMESTAMP, (px, m, a) -> {
                Assertions.assertEquals("getObject", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                Assertions.assertEquals(LocalDateTime.class, a[1]);
                return aLocalDateTime;
            });
            Assertions.assertEquals(aLocalDateTime, srs.getTypedValue(1));
        }));

        // TIME → getObject(int, LocalTime.class)
        tests.add(DynamicTest.dynamicTest(pf + "TIME uses getObject with LocalTime", () -> {
            var srs = makeTypedMock(Types.TIME, (px, m, a) -> {
                Assertions.assertEquals("getObject", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                Assertions.assertEquals(LocalTime.class, a[1]);
                return aLocalTime;
            });
            Assertions.assertEquals(aLocalTime, srs.getTypedValue(1));
        }));

        // TIMESTAMP_WITH_TIMEZONE → getObject(int, OffsetDateTime.class)
        tests.add(DynamicTest.dynamicTest(pf + "TIMESTAMP_WITH_TIMEZONE uses getObject with OffsetDateTime", () -> {
            var srs = makeTypedMock(Types.TIMESTAMP_WITH_TIMEZONE, (px, m, a) -> {
                Assertions.assertEquals("getObject", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                Assertions.assertEquals(OffsetDateTime.class, a[1]);
                return anOffsetDateTime;
            });
            Assertions.assertEquals(anOffsetDateTime, srs.getTypedValue(1));
        }));

        // TIME_WITH_TIMEZONE → getObject(int, OffsetTime.class)
        tests.add(DynamicTest.dynamicTest(pf + "TIME_WITH_TIMEZONE uses getObject with OffsetTime", () -> {
            var srs = makeTypedMock(Types.TIME_WITH_TIMEZONE, (px, m, a) -> {
                Assertions.assertEquals("getObject", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                Assertions.assertEquals(OffsetTime.class, a[1]);
                return anOffsetTime;
            });
            Assertions.assertEquals(anOffsetTime, srs.getTypedValue(1));
        }));

        // BIGINT → getLong + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "BIGINT non-null uses getLong", () -> {
            var srs = makeTypedMock(Types.BIGINT, (px, m, a) -> {
                if ("getLong".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 42L; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(42L, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "BIGINT null via wasNull", () -> {
            var srs = makeTypedMock(Types.BIGINT, (px, m, a) -> {
                if ("getLong".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 0L; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // INTEGER → getInt + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "INTEGER non-null uses getInt", () -> {
            var srs = makeTypedMock(Types.INTEGER, (px, m, a) -> {
                if ("getInt".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 7; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(7, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "INTEGER null via wasNull", () -> {
            var srs = makeTypedMock(Types.INTEGER, (px, m, a) -> {
                if ("getInt".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 0; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // TINYINT → getByte + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "TINYINT non-null uses getByte", () -> {
            var srs = makeTypedMock(Types.TINYINT, (px, m, a) -> {
                if ("getByte".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return (byte) 3; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals((byte) 3, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "TINYINT null via wasNull", () -> {
            var srs = makeTypedMock(Types.TINYINT, (px, m, a) -> {
                if ("getByte".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return (byte) 0; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // SMALLINT → getShort + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "SMALLINT non-null uses getShort", () -> {
            var srs = makeTypedMock(Types.SMALLINT, (px, m, a) -> {
                if ("getShort".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return (short) 10; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals((short) 10, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "SMALLINT null via wasNull", () -> {
            var srs = makeTypedMock(Types.SMALLINT, (px, m, a) -> {
                if ("getShort".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return (short) 0; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // FLOAT → getFloat + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "FLOAT non-null uses getFloat", () -> {
            var srs = makeTypedMock(Types.FLOAT, (px, m, a) -> {
                if ("getFloat".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 1.5F; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(1.5F, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "FLOAT null via wasNull", () -> {
            var srs = makeTypedMock(Types.FLOAT, (px, m, a) -> {
                if ("getFloat".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 0F; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // REAL → getFloat + wasNull (same getter as FLOAT)
        tests.add(DynamicTest.dynamicTest(pf + "REAL non-null uses getFloat", () -> {
            var srs = makeTypedMock(Types.REAL, (px, m, a) -> {
                if ("getFloat".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 2.5F; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(2.5F, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "REAL null via wasNull", () -> {
            var srs = makeTypedMock(Types.REAL, (px, m, a) -> {
                if ("getFloat".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 0F; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // DOUBLE → getDouble + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "DOUBLE non-null uses getDouble", () -> {
            var srs = makeTypedMock(Types.DOUBLE, (px, m, a) -> {
                if ("getDouble".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 3.14; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(3.14, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "DOUBLE null via wasNull", () -> {
            var srs = makeTypedMock(Types.DOUBLE, (px, m, a) -> {
                if ("getDouble".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return 0.0; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // BOOLEAN → getBoolean + wasNull
        tests.add(DynamicTest.dynamicTest(pf + "BOOLEAN non-null uses getBoolean", () -> {
            var srs = makeTypedMock(Types.BOOLEAN, (px, m, a) -> {
                if ("getBoolean".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return true; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(true, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "BOOLEAN null via wasNull", () -> {
            var srs = makeTypedMock(Types.BOOLEAN, (px, m, a) -> {
                if ("getBoolean".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return false; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // BIT → getBoolean + wasNull (same getter as BOOLEAN)
        tests.add(DynamicTest.dynamicTest(pf + "BIT non-null uses getBoolean", () -> {
            var srs = makeTypedMock(Types.BIT, (px, m, a) -> {
                if ("getBoolean".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return true; }
                if ("wasNull".equals(m.getName())) return false;
                throw new AssertionError(m.getName());
            });
            Assertions.assertEquals(true, srs.getTypedValue(1));
        }));
        tests.add(DynamicTest.dynamicTest(pf + "BIT null via wasNull", () -> {
            var srs = makeTypedMock(Types.BIT, (px, m, a) -> {
                if ("getBoolean".equals(m.getName())) { Assertions.assertEquals(1, (int) a[0]); return false; }
                if ("wasNull".equals(m.getName())) return true;
                throw new AssertionError(m.getName());
            });
            Assertions.assertNull(srs.getTypedValue(1));
        }));

        // DECIMAL → getBigDecimal
        tests.add(DynamicTest.dynamicTest(pf + "DECIMAL uses getBigDecimal", () -> {
            var srs = makeTypedMock(Types.DECIMAL, (px, m, a) -> {
                Assertions.assertEquals("getBigDecimal", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aBigDecimal;
            });
            Assertions.assertEquals(aBigDecimal, srs.getTypedValue(1));
        }));

        // NUMERIC → getBigDecimal
        tests.add(DynamicTest.dynamicTest(pf + "NUMERIC uses getBigDecimal", () -> {
            var srs = makeTypedMock(Types.NUMERIC, (px, m, a) -> {
                Assertions.assertEquals("getBigDecimal", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aBigDecimal;
            });
            Assertions.assertEquals(aBigDecimal, srs.getTypedValue(1));
        }));

        // BINARY → getBytes
        tests.add(DynamicTest.dynamicTest(pf + "BINARY uses getBytes", () -> {
            var srs = makeTypedMock(Types.BINARY, (px, m, a) -> {
                Assertions.assertEquals("getBytes", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aBytes;
            });
            Assertions.assertArrayEquals(aBytes, (byte[]) srs.getTypedValue(1));
        }));

        // VARBINARY → getBytes
        tests.add(DynamicTest.dynamicTest(pf + "VARBINARY uses getBytes", () -> {
            var srs = makeTypedMock(Types.VARBINARY, (px, m, a) -> {
                Assertions.assertEquals("getBytes", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aBytes;
            });
            Assertions.assertArrayEquals(aBytes, (byte[]) srs.getTypedValue(1));
        }));

        // LONGVARBINARY → getBytes
        tests.add(DynamicTest.dynamicTest(pf + "LONGVARBINARY uses getBytes", () -> {
            var srs = makeTypedMock(Types.LONGVARBINARY, (px, m, a) -> {
                Assertions.assertEquals("getBytes", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aBytes;
            });
            Assertions.assertArrayEquals(aBytes, (byte[]) srs.getTypedValue(1));
        }));

        // CLOB → getClob
        tests.add(DynamicTest.dynamicTest(pf + "CLOB uses getClob", () -> {
            var srs = makeTypedMock(Types.CLOB, (px, m, a) -> {
                Assertions.assertEquals("getClob", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aClob;
            });
            Assertions.assertSame(aClob, srs.getTypedValue(1));
        }));

        // NCLOB → getNClob
        tests.add(DynamicTest.dynamicTest(pf + "NCLOB uses getNClob", () -> {
            var srs = makeTypedMock(Types.NCLOB, (px, m, a) -> {
                Assertions.assertEquals("getNClob", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aNClob;
            });
            Assertions.assertSame(aNClob, srs.getTypedValue(1));
        }));

        // BLOB → getBlob
        tests.add(DynamicTest.dynamicTest(pf + "BLOB uses getBlob", () -> {
            var srs = makeTypedMock(Types.BLOB, (px, m, a) -> {
                Assertions.assertEquals("getBlob", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aBlob;
            });
            Assertions.assertSame(aBlob, srs.getTypedValue(1));
        }));

        // ARRAY → getArray
        tests.add(DynamicTest.dynamicTest(pf + "ARRAY uses getArray", () -> {
            var srs = makeTypedMock(Types.ARRAY, (px, m, a) -> {
                Assertions.assertEquals("getArray", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return anArray;
            });
            Assertions.assertSame(anArray, srs.getTypedValue(1));
        }));

        // REF → getRef
        tests.add(DynamicTest.dynamicTest(pf + "REF uses getRef", () -> {
            var srs = makeTypedMock(Types.REF, (px, m, a) -> {
                Assertions.assertEquals("getRef", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aRef;
            });
            Assertions.assertSame(aRef, srs.getTypedValue(1));
        }));

        // NVARCHAR → getNString
        tests.add(DynamicTest.dynamicTest(pf + "NVARCHAR uses getNString", () -> {
            var srs = makeTypedMock(Types.NVARCHAR, (px, m, a) -> {
                Assertions.assertEquals("getNString", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aString;
            });
            Assertions.assertEquals(aString, srs.getTypedValue(1));
        }));

        // NCHAR → getNString
        tests.add(DynamicTest.dynamicTest(pf + "NCHAR uses getNString", () -> {
            var srs = makeTypedMock(Types.NCHAR, (px, m, a) -> {
                Assertions.assertEquals("getNString", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aString;
            });
            Assertions.assertEquals(aString, srs.getTypedValue(1));
        }));

        // LONGNVARCHAR → getNString
        tests.add(DynamicTest.dynamicTest(pf + "LONGNVARCHAR uses getNString", () -> {
            var srs = makeTypedMock(Types.LONGNVARCHAR, (px, m, a) -> {
                Assertions.assertEquals("getNString", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aString;
            });
            Assertions.assertEquals(aString, srs.getTypedValue(1));
        }));

        // SQLXML → getSQLXML
        tests.add(DynamicTest.dynamicTest(pf + "SQLXML uses getSQLXML", () -> {
            var srs = makeTypedMock(Types.SQLXML, (px, m, a) -> {
                Assertions.assertEquals("getSQLXML", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aSqlXml;
            });
            Assertions.assertSame(aSqlXml, srs.getTypedValue(1));
        }));

        // ROWID → getRowId
        tests.add(DynamicTest.dynamicTest(pf + "ROWID uses getRowId", () -> {
            var srs = makeTypedMock(Types.ROWID, (px, m, a) -> {
                Assertions.assertEquals("getRowId", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aRowId;
            });
            Assertions.assertSame(aRowId, srs.getTypedValue(1));
        }));

        // STRUCT → getObject(int) cast to Struct
        tests.add(DynamicTest.dynamicTest(pf + "STRUCT uses getObject and casts to Struct", () -> {
            var srs = makeTypedMock(Types.STRUCT, (px, m, a) -> {
                Assertions.assertEquals("getObject", m.getName());
                Assertions.assertEquals(1, a.length);
                Assertions.assertEquals(1, (int) a[0]);
                return aStruct;
            });
            Assertions.assertSame(aStruct, srs.getTypedValue(1));
        }));

        // Types that all fall through to getString
        for (var entry : List.of(
                Map.entry("VARCHAR",     Types.VARCHAR),
                Map.entry("CHAR",        Types.CHAR),
                Map.entry("LONGVARCHAR", Types.LONGVARCHAR),
                Map.entry("DISTINCT",    Types.DISTINCT),
                Map.entry("DATALINK",    Types.DATALINK),
                Map.entry("JAVA_OBJECT", Types.JAVA_OBJECT),
                Map.entry("OTHER",       Types.OTHER)
        )) {
            var typeName = entry.getKey();
            var sqlType  = entry.getValue();
            tests.add(DynamicTest.dynamicTest(pf + typeName + " uses getString", () -> {
                var srs = makeTypedMock(sqlType, (px, m, a) -> {
                    Assertions.assertEquals("getString", m.getName());
                    Assertions.assertEquals(1, (int) a[0]);
                    return aString;
                });
                Assertions.assertEquals(aString, srs.getTypedValue(1));
            }));
        }

        // Default case: unknown SQL type → getString
        tests.add(DynamicTest.dynamicTest(pf + "unknown type (default case) uses getString", () -> {
            var srs = makeTypedMock(Integer.MAX_VALUE, (px, m, a) -> {
                Assertions.assertEquals("getString", m.getName());
                Assertions.assertEquals(1, (int) a[0]);
                return aString;
            });
            Assertions.assertEquals(aString, srs.getTypedValue(1));
        }));

        return tests.stream();
    }

    private SmartResultSet makeConvMock(int sqlType, ConverterFactory factory, InvocationHandler rsHandler) throws Exception {
        var mdMock = ControlledMock.mock(ResultSetMetaData.class);
        mdMock.setHandler((px, m, a) -> {
            if ("getColumnCount".equals(m.getName())) return 1;
            if ("getColumnLabel".equals(m.getName())) return "COL";
            if ("getColumnType".equals(m.getName())) return sqlType;
            throw new AssertionError(m.getName());
        });
        var rsMock = ControlledMock.mock(ResultSet.class);
        rsMock.setHandler((px, m, a) -> {
            if ("getMetaData".equals(m.getName())) return mdMock.getMock();
            return rsHandler.invoke(px, m, a);
        });
        return new SmartResultSet(rsMock.getMock(), factory, Locale.ROOT);
    }

    // Handles getString(1) returning a known raw value to use as pivot in conversion tests.
    private static final InvocationHandler SIMPLE_VARCHAR_HANDLER = (px, m, a) -> {
        Assertions.assertEquals("getString", m.getName());
        Assertions.assertEquals(1, (int) a[0]);
        return "raw_value";
    };

    @TestFactory
    public Stream<DynamicTest> testGetTypedValueOptByIndex() throws Exception {
        var pf = "[testGetTypedValueOptByIndex] ";

        // Strategy: fix SQL type=VARCHAR (getString returns "raw_value") and use a mock
        // ConverterFactory to verify what is passed to fromObj and what is returned,
        // without re-exercising the full converter matrix from the convert subproject.
        var tests = new ArrayList<DynamicTest>(5);

        // Case 1: converter returns a value → getTypedValueOpt returns Optional.of(value)
        // Also verifies that the raw value from getTypedValue(int) is forwarded intact.
        tests.add(DynamicTest.dynamicTest(pf + "returns Optional.of when converter has value", () -> {
            var capturedRaw = new Object[1];
            ConverterFactory factory = type -> {
                Assertions.assertEquals(String.class, type);
                return new Converter<String>() {
                    @Override public Optional<String> fromObj(Object in) { capturedRaw[0] = in; return Optional.of("CONVERTED"); }
                };
            };
            var srs = makeConvMock(Types.VARCHAR, factory, SIMPLE_VARCHAR_HANDLER);
            Assertions.assertEquals(Optional.of("CONVERTED"), srs.getTypedValueOpt(1, String.class));
            Assertions.assertEquals("raw_value", capturedRaw[0]);
        }));

        // Case 2: converter returns empty → getTypedValueOpt returns Optional.empty()
        tests.add(DynamicTest.dynamicTest(pf + "returns Optional.empty when converter is empty", () -> {
            var capturedRaw = new Object[1];
            ConverterFactory factory = type -> new Converter<String>() {
                @Override public Optional<String> fromObj(Object in) { capturedRaw[0] = in; return Optional.empty(); }
            };
            var srs = makeConvMock(Types.VARCHAR, factory, SIMPLE_VARCHAR_HANDLER);
            Assertions.assertEquals(Optional.empty(), srs.getTypedValueOpt(1, String.class));
            Assertions.assertEquals("raw_value", capturedRaw[0]);
        }));

        // Case 3: ConvertionException from fromObj → wrapped in SQLException
        tests.add(DynamicTest.dynamicTest(pf + "wraps ConvertionException in SQLException", () -> {
            var ce = new ConvertionException("test error", String.class, String.class);
            ConverterFactory factory = type -> new Converter<String>() {
                @Override public Optional<String> fromObj(Object in) throws ConvertionException { throw ce; }
            };
            var srs = makeConvMock(Types.VARCHAR, factory, SIMPLE_VARCHAR_HANDLER);
            var sqle = Assertions.assertThrows(SQLException.class, () -> srs.getTypedValueOpt(1, String.class));
            Assertions.assertSame(ce, sqle.getCause());
        }));

        // Case 4: UnavailableConverterException from factory.get() → wrapped in SQLException
        // Note: getTypedValue(int) is still called first, so the RS handler must handle getString.
        tests.add(DynamicTest.dynamicTest(pf + "wraps UnavailableConverterException in SQLException", () -> {
            var uce = UnavailableConverterException.noConverterFor(String.class);
            ConverterFactory factory = type -> { throw uce; };
            var srs = makeConvMock(Types.VARCHAR, factory, SIMPLE_VARCHAR_HANDLER);
            var sqle = Assertions.assertThrows(SQLException.class, () -> srs.getTypedValueOpt(1, String.class));
            Assertions.assertSame(uce, sqle.getCause());
        }));

        // Case 5: raw value is null (column type NULL) → null is forwarded to fromObj
        tests.add(DynamicTest.dynamicTest(pf + "passes null to fromObj when column type is NULL", () -> {
            var capturedRaw = new Object[]{"NOT_NULL_SENTINEL"};
            ConverterFactory factory = type -> new Converter<String>() {
                @Override public Optional<String> fromObj(Object in) { capturedRaw[0] = in; return Optional.empty(); }
            };
            var srs = makeConvMock(Types.NULL, factory, (px, m, a) -> { throw new AssertionError(m.getName()); });
            srs.getTypedValueOpt(1, String.class);
            Assertions.assertNull(capturedRaw[0]);
        }));

        return tests.stream();
    }

    @TestFactory
    public Stream<DynamicTest> testGetTypedValueByIndexWithClass() throws Exception {
        var pf = "[testGetTypedValueByIndexWithClass] ";
        return Stream.of(
            // value present → returned directly (Optional unwrapped)
            DynamicTest.dynamicTest(pf + "returns value when converter has value", () -> {
                ConverterFactory factory = type -> new Converter<String>() {
                    @Override public Optional<String> fromObj(Object in) { return Optional.of("CONVERTED"); }
                };
                var srs = makeConvMock(Types.VARCHAR, factory, SIMPLE_VARCHAR_HANDLER);
                Assertions.assertEquals("CONVERTED", srs.getTypedValue(1, String.class));
            }),
            // value absent → null (Optional.empty().orElse(null))
            DynamicTest.dynamicTest(pf + "returns null when converter returns empty Optional", () -> {
                ConverterFactory factory = type -> new Converter<String>() {
                    @Override public Optional<String> fromObj(Object in) { return Optional.empty(); }
                };
                var srs = makeConvMock(Types.VARCHAR, factory, SIMPLE_VARCHAR_HANDLER);
                Assertions.assertNull(srs.getTypedValue(1, String.class));
            })
        );
    }

    @TestFactory
    public Stream<DynamicTest> testGetTypedValueByLabel() throws Exception {
        var pf = "[testGetTypedValueByLabel] ";
        ConverterFactory presentFactory = type -> new Converter<String>() {
            @Override public Optional<String> fromObj(Object in) { return Optional.of("CONVERTED"); }
        };
        ConverterFactory emptyFactory = type -> new Converter<String>() {
            @Override public Optional<String> fromObj(Object in) { return Optional.empty(); }
        };
        return Stream.of(
            // getTypedValueOpt(String, Class): column found — resolves label to index then runs pipeline
            DynamicTest.dynamicTest(pf + "getTypedValueOpt returns Optional.of for known label", () -> {
                var srs = makeConvMock(Types.VARCHAR, presentFactory, SIMPLE_VARCHAR_HANDLER);
                Assertions.assertEquals(Optional.of("CONVERTED"), srs.getTypedValueOpt("COL", String.class));
            }),
            // getTypedValueOpt(String, Class): label lookup is case-insensitive
            DynamicTest.dynamicTest(pf + "getTypedValueOpt is case-insensitive for column label", () -> {
                var srs = makeConvMock(Types.VARCHAR, presentFactory, SIMPLE_VARCHAR_HANDLER);
                Assertions.assertEquals(Optional.of("CONVERTED"), srs.getTypedValueOpt("col", String.class));
            }),
            // getTypedValueOpt(String, Class): unknown label → IllegalArgumentException before any RS call
            DynamicTest.dynamicTest(pf + "getTypedValueOpt throws IllegalArgumentException for unknown label", () -> {
                var srs = makeConvMock(Types.VARCHAR, presentFactory, (px, m, a) -> {
                    throw new AssertionError(m.getName());
                });
                Assertions.assertThrows(IllegalArgumentException.class, () -> srs.getTypedValueOpt("UNKNOWN", String.class));
            }),
            // getTypedValue(String, Class): value present → value returned (Optional unwrapped)
            DynamicTest.dynamicTest(pf + "getTypedValue returns value for known label", () -> {
                var srs = makeConvMock(Types.VARCHAR, presentFactory, SIMPLE_VARCHAR_HANDLER);
                Assertions.assertEquals("CONVERTED", srs.getTypedValue("COL", String.class));
            }),
            // getTypedValue(String, Class): converter returns empty → null
            DynamicTest.dynamicTest(pf + "getTypedValue returns null when converter returns empty Optional", () -> {
                var srs = makeConvMock(Types.VARCHAR, emptyFactory, SIMPLE_VARCHAR_HANDLER);
                Assertions.assertNull(srs.getTypedValue("COL", String.class));
            }),
            // getTypedValue(String, Class): unknown label → IllegalArgumentException
            DynamicTest.dynamicTest(pf + "getTypedValue throws IllegalArgumentException for unknown label", () -> {
                var srs = makeConvMock(Types.VARCHAR, presentFactory, (px, m, a) -> {
                    throw new AssertionError(m.getName());
                });
                Assertions.assertThrows(IllegalArgumentException.class, () -> srs.getTypedValue("UNKNOWN", String.class));
            })
        );
    }

    private SmartResultSet makeTwoColMock(ConverterFactory factory) throws Exception {
        var mdMock = ControlledMock.mock(ResultSetMetaData.class);
        mdMock.setHandler((px, m, a) -> {
            if ("getColumnCount".equals(m.getName())) return 2;
            if ("getColumnLabel".equals(m.getName())) return (int) a[0] == 1 ? "ALPHA" : "BETA";
            if ("getColumnType".equals(m.getName())) return Types.VARCHAR;
            throw new AssertionError(m.getName());
        });
        var rsMock = ControlledMock.mock(ResultSet.class);
        rsMock.setHandler((px, m, a) -> {
            if ("getMetaData".equals(m.getName())) return mdMock.getMock();
            if ("getString".equals(m.getName())) return (int) a[0] == 1 ? "hello" : "world";
            throw new AssertionError(m.getName());
        });
        return new SmartResultSet(rsMock.getMock(), factory, Locale.ROOT);
    }

    // Creates a ConverterFactory whose mapToRecord captures the received map and returns 'result'.
    // ConverterFactory.get() is not expected to be called in getRecord flows, so it throws.
    @SuppressWarnings("unchecked")
    private static ConverterFactory captureFactory(Object[] capturedMap, Record result) {
        return new ConverterFactory() {
            @Override
            public Converter<?> get(Type t) throws UnavailableConverterException {
                throw UnavailableConverterException.noConverterFor(t);
            }
            @Override
            public <T extends Record> T mapToRecord(Map<String, ?> map, Class<T> k) {
                capturedMap[0] = map;
                return (T) result;
            }
        };
    }

    @TestFactory
    public Stream<DynamicTest> testGetRecord() throws Exception {
        // Strategy: mock mapToRecord to capture the map argument and return a fixed result,
        // without invoking the real ConverterFactory machinery.
        record TestRecord(String name) {
        }

        var expected = new TestRecord("test");
        var mapAll   = Map.<String, Object>of("ALPHA", "hello", "BETA", "world");
        var mapAlpha = Map.<String, Object>of("ALPHA", "hello");
        var mapBeta  = Map.<String, Object>of("BETA",  "world");
        var pf = "[testGetRecord] ";
        var tests = new ArrayList<DynamicTest>(16);

        // getRecord(Class) — all columns
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class) passes all columns to mapToRecord", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class));
            Assertions.assertEquals(mapAll, cap[0]);
        }));

        // getRecord(Class, int...) — column 1
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, int...) passes selected column by index", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, 1));
            Assertions.assertEquals(mapAlpha, cap[0]);
        }));

        // getRecord(Class, int...) — column 2
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, int...) passes other column by index", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, 2));
            Assertions.assertEquals(mapBeta, cap[0]);
        }));

        // getRecord(Class, String...) — by label "ALPHA"
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, String...) passes selected column by label", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, "ALPHA"));
            Assertions.assertEquals(mapAlpha, cap[0]);
        }));

        // getRecord(Class, String...) — by label "BETA"
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, String...) passes other column by label", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, "BETA"));
            Assertions.assertEquals(mapBeta, cap[0]);
        }));

        // getRecord(Class, Function) — remapper is applied to all column keys
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, Function) applies remapper to map keys", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            var mapAllLower = Map.<String, Object>of("alpha", "hello", "beta", "world");
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, String::toLowerCase));
            Assertions.assertEquals(mapAllLower, cap[0]);
        }));

        // getRecord(Class, Function) — identity remapper leaves keys unchanged
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, Function) identity remapper leaves keys unchanged", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, x -> x));
            Assertions.assertEquals(mapAll, cap[0]);
        }));

        // getRecord(Class, Function, int...) — remapper is applied to selected column keys
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, Function, int...) applies remapper to selected column", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            var mapBetaLower = Map.<String, Object>of("beta", "world");
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, String::toLowerCase, 2));
            Assertions.assertEquals(mapBetaLower, cap[0]);
        }));

        // getRecord(Class, Function, int...) — identity remapper, specific column by index
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, Function, int...) identity remapper passes column by index", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, x -> x, 2));
            Assertions.assertEquals(mapBeta, cap[0]);
        }));

        // getRecord(Class, Function, String...) — remapper is applied to selected column keys
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, Function, String...) applies remapper to selected column", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            var mapAlphaLower = Map.<String, Object>of("alpha", "hello");
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, String::toLowerCase, "ALPHA"));
            Assertions.assertEquals(mapAlphaLower, cap[0]);
        }));

        // getRecord(Class, Function, String...) — identity remapper, specific column by label
        tests.add(DynamicTest.dynamicTest(pf + "getRecord(Class, Function, String...) identity remapper passes column by label", () -> {
            var cap = new Object[1];
            var srs = makeTwoColMock(captureFactory(cap, expected));
            Assertions.assertSame(expected, srs.getRecord(TestRecord.class, x -> x, "ALPHA"));
            Assertions.assertEquals(mapAlpha, cap[0]);
        }));

        // Verify the record class is forwarded correctly to mapToRecord
        tests.add(DynamicTest.dynamicTest(pf + "passes the correct record Class to mapToRecord", () -> {
            var capturedClass = new Object[1];
            @SuppressWarnings("unchecked")
            ConverterFactory factory = new ConverterFactory() {
                @Override
                public Converter<?> get(Type t) throws UnavailableConverterException {
                    throw UnavailableConverterException.noConverterFor(t);
                }

                @Override
                public <T extends Record> T mapToRecord(Map<String, ?> map, Class<T> k) {
                    capturedClass[0] = k;
                    return (T) expected;
                }
            };
            makeTwoColMock(factory).getRecord(TestRecord.class);
            Assertions.assertEquals(TestRecord.class, capturedClass[0]);
        }));

        // --- Exception wrapping: all four types must become SQLException ---

        tests.add(DynamicTest.dynamicTest(pf + "wraps ConvertionException in SQLException", () -> {
            var ex = new ConvertionException("test", String.class, TestRecord.class);
            ConverterFactory factory = new ConverterFactory() {
                @Override
                public Converter<?> get(Type t) throws UnavailableConverterException {
                    throw UnavailableConverterException.noConverterFor(t);
                }

                @Override
                public <T extends Record> T mapToRecord(Map<String, ?> map, Class<T> k) throws ConvertionException {
                    throw ex;
                }
            };
            var sqle = Assertions.assertThrows(SQLException.class, () -> makeTwoColMock(factory).getRecord(TestRecord.class));
            Assertions.assertSame(ex, sqle.getCause());
        }));

        tests.add(DynamicTest.dynamicTest(pf + "wraps MagicFactory.CreationException in SQLException", () -> {
            var ex = new MagicFactory.CreationException("test", TestRecord.class);
            ConverterFactory factory = new ConverterFactory() {
                @Override
                public Converter<?> get(Type t) throws UnavailableConverterException {
                    throw UnavailableConverterException.noConverterFor(t);
                }

                @Override
                public <T extends Record> T mapToRecord(Map<String, ?> map, Class<T> k) throws MagicFactory.CreationException {
                    throw ex;
                }
            };
            var sqle = Assertions.assertThrows(SQLException.class, () -> makeTwoColMock(factory).getRecord(TestRecord.class));
            Assertions.assertSame(ex, sqle.getCause());
        }));

        tests.add(DynamicTest.dynamicTest(pf + "wraps MagicFactory.CreatorSelectionException in SQLException", () -> {
            var ex = new MagicFactory.CreatorSelectionException("test", TestRecord.class);
            ConverterFactory factory = new ConverterFactory() {
                @Override
                public Converter<?> get(Type t) throws UnavailableConverterException {
                    throw UnavailableConverterException.noConverterFor(t);
                }

                @Override
                public <T extends Record> T mapToRecord(Map<String, ?> map, Class<T> k) throws MagicFactory.CreatorSelectionException {
                    throw ex;
                }
            };
            var sqle = Assertions.assertThrows(SQLException.class, () -> makeTwoColMock(factory).getRecord(TestRecord.class));
            Assertions.assertSame(ex, sqle.getCause());
        }));

        tests.add(DynamicTest.dynamicTest(pf + "wraps UnavailableConverterException from mapToRecord in SQLException", () -> {
            var ex = UnavailableConverterException.noConverterFor(TestRecord.class);
            ConverterFactory factory = new ConverterFactory() {
                @Override
                public Converter<?> get(Type t) throws UnavailableConverterException {
                    throw UnavailableConverterException.noConverterFor(t);
                }

                @Override
                public <T extends Record> T mapToRecord(Map<String, ?> map, Class<T> k) throws UnavailableConverterException {
                    throw ex;
                }
            };
            var sqle = Assertions.assertThrows(SQLException.class, () -> makeTwoColMock(factory).getRecord(TestRecord.class));
            Assertions.assertSame(ex, sqle.getCause());
        }));

        return tests.stream();
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
                DynamicTest.dynamicTest("[testNulls] constructor(ResultSet)"                 , () -> ForTests.testNull("rs"         , () -> new SmartResultSet(null))),
                DynamicTest.dynamicTest("[testNulls] constructor(3)-1"                       , () -> ForTests.testNull("rs"         , () -> new SmartResultSet(null, ConverterFactory.STD, Locale.ROOT))),
                DynamicTest.dynamicTest("[testNulls] constructor(3)-2"                       , () -> ForTests.testNull("factory"    , () -> new SmartResultSet(rs, null, Locale.ROOT))),
                DynamicTest.dynamicTest("[testNulls] constructor(3)-3"                       , () -> ForTests.testNull("localizer"  , () -> new SmartResultSet(rs, ConverterFactory.STD, null))),
                DynamicTest.dynamicTest("[testNulls] getMapByColumnNumber(int...)"           , () -> ForTests.testNull("fields"     , () -> mock0().getMapByColumnNumbers((int[]) null))),
                DynamicTest.dynamicTest("[testNulls] getMapByLabels(String...)"              , () -> ForTests.testNull("fields"     , () -> mock0().getMapByLabels((String[]) null))),
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

    @Test
    public void testToString() {
        var md = makeMetaData(false, List.of(new TypeData("X", Types.VARCHAR)));
        var rs1 = ControlledMock.mock(ResultSet.class);
        rs1.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            if (m.getName().equals("toString")) return "123-test-123";
            throw new AssertionError(m);
        });
        var mock1 = rs1.getMock();
        var rs2 = ControlledMock.mock(ResultSet.class);
        rs2.setHandler((i, m, a) -> {
            if (m.getName().equals("getMetaData")) return md.getMock();
            if (m.getName().equals("toString")) return "567-test-567";
            throw new AssertionError(m);
        });
        var mock2 = rs2.getMock();
        Assertions.assertAll(
                () -> Assertions.assertEquals("SmartResultSet[123-test-123]", new SmartResultSet(mock1).toString()),
                () -> Assertions.assertEquals("SmartResultSet[567-test-567]", new SmartResultSet(mock2).toString())
        );
    }
}
