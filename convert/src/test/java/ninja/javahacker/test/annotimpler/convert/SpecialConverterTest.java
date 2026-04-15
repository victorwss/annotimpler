package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class SpecialConverterTest {

    private static final Type COLLECTION_STRING;
    private static final Type LIST_STRING;
    private static final Type SET_STRING;
    private static final Type OPTIONAL_STRING;
    private static final Type POINTLESS;
    private static final Type MAP_STRING_STRING;

    static {
        try {
            var mtd = SpecialConverterTest.class.getDeclaredMethod("noop", Collection.class, List.class, Set.class, Optional.class, Pointless.class, Map.class);
            COLLECTION_STRING = mtd.getParameters()[0].getParameterizedType();
            LIST_STRING = mtd.getParameters()[1].getParameterizedType();
            SET_STRING = mtd.getParameters()[2].getParameterizedType();
            OPTIONAL_STRING = mtd.getParameters()[3].getParameterizedType();
            POINTLESS = mtd.getParameters()[4].getParameterizedType();
            MAP_STRING_STRING = mtd.getParameters()[5].getParameterizedType();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static interface Pointless<X> extends List<X> {}

    private static void noop(Collection<String> a, List<String> b, Set<String> c, Optional<String> d, Pointless<String> e, Map<String, String> g) {
        throw new AssertionError();
    }

    private static String name(Type t) {
        if (t instanceof Class<?> k) return k.getSimpleName();
        if (t instanceof ParameterizedType p) return ((Class<?>) p.getRawType()).getSimpleName() + "<String>";
        throw new AssertionError();
    }

    private static final List<Type> CVT_CLASSES = List.of(
            boolean.class, byte.class, short.class, int    .class, long.class, float.class, double.class,
            Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigDecimal.class, BigInteger.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
            Calendar.class, GregorianCalendar.class, java.util.Date.class, java.sql.Date.class, Time.class, java.sql.Timestamp.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, OffsetTime.class, Instant.class,
            RowId.class, Ref.class, Struct.class, java.sql.Array.class, byte[].class, String.class,
            LocalDate[].class, COLLECTION_STRING, LIST_STRING, SET_STRING, OPTIONAL_STRING
    );

    private RowId rowid(String in) {
        return (RowId) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { RowId.class }, (i, m, a) -> {
            if (m.getName().equals("getBytes")) return in.getBytes();
            throw new AssertionError(m.getName());
        });
    }

    private Ref ref() {
        return (Ref) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Ref.class }, (i, m, a) -> {
            throw new AssertionError(m.getName());
        });
    }

    private Struct struct() {
        return (Struct) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Struct.class }, (i, m, a) -> {
            throw new AssertionError(m.getName());
        });
    }

    private java.sql.Array array() {
        return (java.sql.Array) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { java.sql.Array.class }, (i, m, a) -> {
            throw new AssertionError(m.getName());
        });
    }

    @TestFactory
    public List<DynamicNode> testBadRowId() throws Exception {
        var all = new ArrayList<>(CVT_CLASSES);
        all.remove(RowId.class);
        all.remove(String.class);
        all.remove(byte[].class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k1 : all) {
                var cvt = ConverterFactory.STD.get(k1);
                var nd = DynamicTest.dynamicTest(
                        "Converter for " + name(k1) + " from RowId - " + in + ".",
                        () -> {
                                var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                                Assertions.assertAll(
                                        () -> Assertions.assertEquals("Unsupported RowId.", ce.getMessage()),
                                        () -> Assertions.assertEquals(RowId.class, ce.getIn()),
                                        () -> Assertions.assertEquals(k1, ce.getOut())
                                );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadRef() throws Exception {
        var all = new ArrayList<>(CVT_CLASSES);
        all.remove(Ref.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = ref();
        for (var k1 : all) {
            var cvt = ConverterFactory.STD.get(k1);
            var nd = DynamicTest.dynamicTest(
                    "Converter for " + name(k1) + " from Ref.",
                    () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Ref.", ce.getMessage()),
                                    () -> Assertions.assertEquals(Ref.class, ce.getIn()),
                                    () -> Assertions.assertEquals(k1, ce.getOut())
                            );
                    }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadStruct() throws Exception {
        var all = new ArrayList<>(CVT_CLASSES);
        all.remove(Struct.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = struct();
        for (var k1 : all) {
            var cvt = ConverterFactory.STD.get(k1);
            var nd = DynamicTest.dynamicTest(
                    "Converter for " + name(k1) + " from Struct.",
                    () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Struct.", ce.getMessage()),
                                    () -> Assertions.assertEquals(Struct.class, ce.getIn()),
                                    () -> Assertions.assertEquals(k1, ce.getOut())
                            );
                    }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadArray() throws Exception {
        var all = new ArrayList<>(CVT_CLASSES);
        all.remove(java.sql.Array.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = array();
        for (var k1 : all) {
            var cvt = ConverterFactory.STD.get(k1);
            var nd = DynamicTest.dynamicTest(
                    "Converter for " + name(k1) + " from Array.",
                    () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Array.", ce.getMessage()),
                                    () -> Assertions.assertEquals(java.sql.Array.class, ce.getIn()),
                                    () -> Assertions.assertEquals(k1, ce.getOut())
                            );
                    }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromRowId() throws Exception {
        List<DynamicNode> nodes = new ArrayList<>(3);
        var cvt = ConverterFactory.STD.get(RowId.class);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            DynamicNode nd = DynamicTest.dynamicTest(
                "Converter for RowId from RowId - " + in + ".",
                () -> {
                        Assertions.assertSame(r, cvt.from(r).get());
                        Assertions.assertSame(r, cvt.fromObj(r).get());
                }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @Test
    public void testRefFromRef() throws Exception {
        var cvt = ConverterFactory.STD.get(Ref.class);
        var r = ref();
        Assertions.assertAll(
                () -> Assertions.assertSame(r, cvt.from(r).get()),
                () -> Assertions.assertSame(r, cvt.fromObj(r).get())
        );
    }

    @Test
    public void testStructFromStruct() throws Exception {
        var cvt = ConverterFactory.STD.get(Struct.class);
        var r = struct();
        Assertions.assertAll(
                () -> Assertions.assertSame(r, cvt.from(r).get()),
                () -> Assertions.assertSame(r, cvt.fromObj(r).get())
        );
    }

    @Test
    public void testArrayFromArray() throws Exception {
        var cvt = ConverterFactory.STD.get(java.sql.Array.class);
        var r = array();
        Assertions.assertAll(
                () -> Assertions.assertSame(r, cvt.from(r).get()),
                () -> Assertions.assertSame(r, cvt.fromObj(r).get())
        );
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromString() throws Exception {
        List<DynamicNode> nodes = new ArrayList<>(3);
        var cvt = ConverterFactory.STD.get(String.class);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            var b = new BigInteger(r.getBytes());
            DynamicNode nd = DynamicTest.dynamicTest(
                "Converter for RowId from RowId - " + in + ".",
                () -> {
                        Assertions.assertEquals(b.toString(), cvt.from(r).get());
                        Assertions.assertEquals(b.toString(), cvt.fromObj(r).get());
                }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromByteArray() throws Exception {
        List<DynamicNode> nodes = new ArrayList<>(3);
        var cvt = ConverterFactory.STD.get(byte[].class);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            DynamicNode nd = DynamicTest.dynamicTest(
                "Converter for RowId from byte[] - " + in + ".",
                () -> {
                        Assertions.assertArrayEquals(r.getBytes(), cvt.from(r).get());
                        Assertions.assertArrayEquals(r.getBytes(), cvt.fromObj(r).get());
                }
            );
            nodes.add(nd);
        }
        return nodes;
    }
}