package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class SpecialConverterTest {

    public static interface Pointless<X> extends List<X> {}

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
        var all = new ArrayList<>(TestTypes.CVT_CLASSES);
        all.remove(RowId.class);
        all.remove(String.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k1 : all) {
                for (var k2 : TestTypes.others(k1)) {
                    if (k2 == byte[].class) continue;
                    var cvt = ConverterFactory.STD.get(k2);
                    var nd = DynamicTest.dynamicTest(
                            "Converter for " + TestTypes.name(k2) + " from RowId - " + in + ".",
                            () -> {
                                var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                                Assertions.assertAll(
                                        () -> Assertions.assertEquals("Unsupported RowId.", ce.getMessage()),
                                        () -> Assertions.assertEquals(RowId.class, ce.getIn()),
                                        () -> Assertions.assertEquals(k2, ce.getOut())
                                );
                            }
                    );
                    nodes.add(nd);
                }
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadRef() throws Exception {
        var all = new ArrayList<>(TestTypes.CVT_CLASSES);
        all.remove(Ref.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = ref();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var nd = DynamicTest.dynamicTest(
                        "Converter for " + TestTypes.name(k2) + " from Ref.",
                        () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Ref.", ce.getMessage()),
                                    () -> Assertions.assertEquals(Ref.class, ce.getIn()),
                                    () -> Assertions.assertEquals(k2, ce.getOut())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadStruct() throws Exception {
        var all = new ArrayList<>(TestTypes.CVT_CLASSES);
        all.remove(Struct.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = struct();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var nd = DynamicTest.dynamicTest(
                        "Converter for " + TestTypes.name(k2) + " from Struct.",
                        () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Struct.", ce.getMessage()),
                                    () -> Assertions.assertEquals(Struct.class, ce.getIn()),
                                    () -> Assertions.assertEquals(k2, ce.getOut())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadArray() throws Exception {
        var all = new ArrayList<>(TestTypes.CVT_CLASSES);
        all.remove(java.sql.Array.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = array();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var nd = DynamicTest.dynamicTest(
                        "Converter for " + TestTypes.name(k2) + " from Array.",
                        () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.from(r));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Array.", ce.getMessage()),
                                    () -> Assertions.assertEquals(java.sql.Array.class, ce.getIn()),
                                    () -> Assertions.assertEquals(k2, ce.getOut())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromRowId() throws Exception {
        List<DynamicNode> nodes = new ArrayList<>(3 * 5);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k2 : TestTypes.others(RowId.class)) {
                var cvt = ConverterFactory.STD.get(k2);
                var o2 = TestTypes.wrap(r, k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "Converter for RowId from RowId - " + in + " - " + TestTypes.name(k2) + ".",
                        () -> Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        )
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRefFromRef() throws Exception {
        var r = ref();
        List<DynamicNode> nodes = new ArrayList<>(5);
        for (var k2 : TestTypes.others(Ref.class)) {
            var cvt = ConverterFactory.STD.get(k2);
            var o2 = TestTypes.wrap(r, k2);
            DynamicNode nd = DynamicTest.dynamicTest(
                    "Converter for Ref from Ref - " + TestTypes.name(k2) + ".",
                    () -> Assertions.assertAll(
                            () -> TestTypes.compare(o2, cvt.from(r).get()),
                            () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                    )
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testStructFromStruct() throws Exception {
        var r = struct();
        List<DynamicNode> nodes = new ArrayList<>(5);
        for (var k2 : TestTypes.others(Struct.class)) {
            var cvt = ConverterFactory.STD.get(k2);
            var o2 = TestTypes.wrap(r, k2);
            DynamicNode nd = DynamicTest.dynamicTest(
                    "Converter for Struct from Struct - " + TestTypes.name(k2) + ".",
                    () -> Assertions.assertAll(
                            () -> TestTypes.compare(o2, cvt.from(r).get()),
                            () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                    )
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testArrayFromArray() throws Exception {
        var r = array();
        List<DynamicNode> nodes = new ArrayList<>(5);
        for (var k2 : TestTypes.others(java.sql.Array.class)) {
            var cvt = ConverterFactory.STD.get(k2);
            var o2 = TestTypes.wrap(r, k2);
            DynamicNode nd = DynamicTest.dynamicTest(
                    "Converter for Array from Array - " + TestTypes.name(k2) + ".",
                    () -> Assertions.assertAll(
                            () -> TestTypes.compare(o2, cvt.from(r).get()),
                            () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                    )
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromString() throws Exception {
        List<DynamicNode> nodes = new ArrayList<>(3 * 5);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            var b = new BigInteger(r.getBytes());
            for (var k2 : TestTypes.others(String.class)) {
                var cvt = ConverterFactory.STD.get(k2);
                var o2 = TestTypes.wrap(b.toString(), k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "Converter for RowId from String - " + in + " - " + TestTypes.name(k2) + ".",
                        () -> Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        )
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromByteArray() throws Exception {
        List<DynamicNode> nodes = new ArrayList<>(3 * 5);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k2 : TestTypes.others(byte[].class)) {
                var cvt = ConverterFactory.STD.get(k2);
                var o2 = TestTypes.wrap(r.getBytes(), k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "Converter for RowId from byte[] - " + in + " - " + TestTypes.name(k2) + ".",
                        () -> Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        )
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }
}