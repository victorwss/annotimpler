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
        var all = new ArrayList<>(TestTypes.CVT_CLASSES_WITH_ARRAYS);
        all.remove(RowId.class);
        all.remove(String.class);
        all.remove(TestTypes.R4String.class);
        all.remove(TestTypes.R4byteArray.class);
        all.remove(TestTypes.R4Record.class);
        all.remove(TestTypes.R4RecordDeep.class);
        all.remove(TestTypes.R4RecordDeeper.class);
        all.remove(TestTypes.R4StringArray.class);
        all.remove(TestTypes.R4StringList.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k1 : all) {
                for (var k2 : TestTypes.others(k1)) {
                    if (k2 == byte[].class || k2 == char[].class) continue;
                    var nd = DynamicTest.dynamicTest(
                            "[testBadRowId] Converter for " + TypeName.of(k2) + " from RowId - " + in + ".",
                            () -> {
                                var cvt = ConverterFactory.STD.get(k2);
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
        var all = new ArrayList<>(TestTypes.CVT_CLASSES_WITH_ARRAYS);
        all.remove(Ref.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = ref();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var nd = DynamicTest.dynamicTest(
                        "[testBadRef] Converter for " + TypeName.of(k2) + " from Ref.",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
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
        var all = new ArrayList<>(TestTypes.CVT_CLASSES_WITH_ARRAYS);
        all.remove(Struct.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = struct();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var nd = DynamicTest.dynamicTest(
                        "[testBadStruct] Converter for " + TypeName.of(k2) + " from Struct.",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
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
        var all = new ArrayList<>(TestTypes.CVT_CLASSES_WITH_ARRAYS);
        all.remove(java.sql.Array.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = array();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var nd = DynamicTest.dynamicTest(
                        "[testBadArray] Converter for " + TypeName.of(k2) + " from Array.",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
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
        var all = new ArrayList<>(TestTypes.others(RowId.class));
        List<DynamicNode> nodes = new ArrayList<>(3 * all.size());
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k2 : all) {
                var o2 = TestTypes.wrap(r, k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "[testRowIdFromRowId] Converter for RowId from RowId - " + in + " - " + TypeName.of(k2) + ".",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRefFromRef() throws Exception {
        var all = new ArrayList<>(TestTypes.others(Ref.class));
        var r = ref();
        List<DynamicNode> nodes = new ArrayList<>(all.size());
        for (var k2 : all) {
            var o2 = TestTypes.wrap(r, k2);
            DynamicNode nd = DynamicTest.dynamicTest(
                    "[testRefFromRef] Converter for Ref from Ref - " + TypeName.of(k2) + ".",
                    () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        );
                    }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testStructFromStruct() throws Exception {
        var all = new ArrayList<>(TestTypes.others(Struct.class));
        var r = struct();
        List<DynamicNode> nodes = new ArrayList<>(all.size());
        for (var k2 : all) {
            var o2 = TestTypes.wrap(r, k2);
            DynamicNode nd = DynamicTest.dynamicTest(
                    "[testStructFromStruct] Converter for Struct from Struct - " + TypeName.of(k2) + ".",
                    () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        );
                    }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testArrayFromArray() throws Exception {
        var all = new ArrayList<>(TestTypes.others(java.sql.Array.class));
        var r = array();
        List<DynamicNode> nodes = new ArrayList<>(all.size());
        for (var k2 : all) {
            var o2 = TestTypes.wrap(r, k2);
            DynamicNode nd = DynamicTest.dynamicTest(
                    "[testArrayFromArray] Converter for Array from Array - " + TypeName.of(k2) + ".",
                    () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        );
                    }
            );
            nodes.add(nd);
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromStringlike() throws Exception {
        var all = new ArrayList<>(TestTypes.others(String.class));
        all.add(TestTypes.R4String.class);
        all.add(TestTypes.R4Record.class);
        all.add(TestTypes.R4RecordDeep.class);
        all.add(TestTypes.R4StringArray.class);
        all.add(TestTypes.R4StringList.class);
        List<DynamicNode> nodes = new ArrayList<>(3 * all.size());
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            var b = new BigInteger(r.getBytes());
            for (var k2 : all) {
                var o2 = TestTypes.wrap(b.toString(), k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "[testRowIdFromStringlike] Converter for RowId from String - " + in + " - " + TypeName.of(k2) + ".",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromCharArray() throws Exception {
        var all = new ArrayList<>(TestTypes.others(char[].class));
        all.add(TestTypes.R4RecordDeeper.class);
        List<DynamicNode> nodes = new ArrayList<>(3 * all.size());
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            var b = new BigInteger(r.getBytes());
            for (var k2 : all) {
                var o2 = TestTypes.wrap((b.toString() + (k2 == TestTypes.R4RecordDeeper.class ? "xxx" : "")).toCharArray(), k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "[testRowIdFromCharArray] Converter for RowId from String - " + in + " - " + TypeName.of(k2) + ".",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testRowIdFromByteArray() throws Exception {
        var all1 = new ArrayList<>(TestTypes.others(byte[].class));
        var all2 = new ArrayList<>(TestTypes.others(TestTypes.R4byteArray.class));
        var all = Stream.of(all1, all2).flatMap(List::stream).toList();
        List<DynamicNode> nodes = new ArrayList<>(3 * all.size());
        for (var in : List.of("ABCD", "abcde", "1234")) {
            var r = rowid(in);
            for (var k2 : all) {
                var o2 = TestTypes.wrap(r.getBytes(), k2);
                DynamicNode nd = DynamicTest.dynamicTest(
                        "[testRowIdFromByteArray] Converter for RowId from byte[] - " + in + " - " + TypeName.of(k2) + ".",
                        () -> {
                            var cvt = ConverterFactory.STD.get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        return nodes;
    }
}