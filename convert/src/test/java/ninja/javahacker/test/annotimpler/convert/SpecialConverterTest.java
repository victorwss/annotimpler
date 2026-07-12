package ninja.javahacker.test.annotimpler.convert;

import lombok.NonNull;
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
                                var cvt = ConverterFactory.std().get(k2);
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
        if (nodes.isEmpty()) throw new AssertionError();
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadRef() throws Exception {
        var all = new ArrayList<>(TestTypes.CVT_CLASSES_WITH_ARRAYS);
        all.remove(Ref.class);
        all.remove(TestTypes.R4Ref.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = ref();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var nd = DynamicTest.dynamicTest(
                        "[testBadRef] Converter for " + TypeName.of(k2) + " from Ref.",
                        () -> {
                            var cvt = ConverterFactory.std().get(k2);
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
        if (nodes.isEmpty()) throw new AssertionError();
        return nodes;
    }

    @TestFactory
    public List<DynamicNode> testBadStruct() throws Exception {
        var all = new ArrayList<>(TestTypes.CVT_CLASSES_WITH_ARRAYS);
        all.remove(Struct.class);
        all.remove(TestTypes.R4Struct.class);
        List<DynamicNode> nodes = new ArrayList<>(500);
        var r = struct();
        for (var k1 : all) {
            for (var k2 : TestTypes.others(k1)) {
                var nd = DynamicTest.dynamicTest(
                        "[testBadStruct] Converter for " + TypeName.of(k2) + " from Struct.",
                        () -> {
                            var cvt = ConverterFactory.std().get(k2);
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
        if (nodes.isEmpty()) throw new AssertionError();
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
                            var cvt = ConverterFactory.std().get(k2);
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
        if (nodes.isEmpty()) throw new AssertionError();
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
                            var cvt = ConverterFactory.std().get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        if (nodes.isEmpty()) throw new AssertionError();
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
                        var cvt = ConverterFactory.std().get(k2);
                        Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        );
                    }
            );
            nodes.add(nd);
        }
        if (nodes.isEmpty()) throw new AssertionError();
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
                        var cvt = ConverterFactory.std().get(k2);
                        Assertions.assertAll(
                                () -> TestTypes.compare(o2, cvt.from(r).get()),
                                () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                        );
                    }
            );
            nodes.add(nd);
        }
        if (nodes.isEmpty()) throw new AssertionError();
        return nodes;
    }

    public static record Foo(int x) {}
    public static record Foo2(Foo f1) {}

    private static void noopz(List<Foo> a, Collection<Foo> b, Set<Foo> c, Optional<Foo> d) {
        throw new AssertionError();
    }

    @TestFactory
    public DynamicNode testConversionFromSpecials() throws Exception {
        var a = new Foo(42);
        var b = new Foo2(a);
        var c = new Foo[] {a};
        var d = List.of(a);
        var e = Set.of(a);
        var f = Optional.of(a);

        var ps = Stream.of(SpecialConverterTest.class.getDeclaredMethods())
                .filter(n -> n.getName().equals("noopz"))
                .map(Method::getParameters)
                .flatMap(Stream::of)
                .map(Parameter::getParameterizedType)
                .toList();

        var tLst = ps.get(0);
        var tCol = ps.get(1);
        var tSet = ps.get(2);
        var tOpt = ps.get(3);

        var a1 = array();
        var a2 = ref();
        var a3 = rowid("xxx");
        var a4 = struct();

        var testCvt = new Converter<Foo>() {
            @Override
            public Optional<Foo> from(java.sql.Array input) {
                if (input == a1) return Optional.of(a);
                throw new AssertionError();
            }

            @Override
            public Optional<Foo> from(Ref input) {
                if (input == a2) return Optional.of(a);
                throw new AssertionError();
            }

            @Override
            public Optional<Foo> from(RowId input) {
                if (input == a3) return Optional.of(a);
                throw new AssertionError();
            }

            @Override
            public Optional<Foo> from(Struct input) {
                if (input == a4) return Optional.of(a);
                throw new AssertionError();
            }
        };

        @FunctionalInterface
        interface MethodSpec {
            public Object work(Converter<?> cvt) throws Exception;
        }

        record Input(Object what, MethodSpec spec, String name) {}

        record Output(Object what, Type type) {}

        List<Input> ins = List.of(
                new Input(a1, cvt -> cvt.from(a1).get(), "Array"),
                new Input(a2, cvt -> cvt.from(a2).get(), "Ref"),
                new Input(a3, cvt -> cvt.from(a3).get(), "RowId"),
                new Input(a4, cvt -> cvt.from(a4).get(), "Struct")
        );

        var outs = List.of(
                new Output(a, Foo.class),
                new Output(b, Foo2.class),
                new Output(c, Foo[].class),
                new Output(d, tLst),
                new Output(d, tCol),
                new Output(e, tSet),
                new Output(f, tOpt)
        );

        var result = ins.stream().map(input -> {

            ConverterFactory cvtf = ConverterFactory.std().extend(Foo.class, testCvt);

            var s = outs.stream().map(output -> DynamicTest.dynamicTest(
                    "[testConversionFromSpecials] Converter for Custom from " + input.name + " to " + TypeName.of(output.type) + ".",
                    () -> {
                        var cvt = cvtf.get(output.type);
                        Assertions.assertAll(
                                () -> TestTypes.compare(output.what, input.spec.work(cvt)),
                                () -> TestTypes.compare(output.what, cvt.fromObj(input.what).get())
                        );
                    }
            ));
            return DynamicContainer.dynamicContainer("[testConversionFromSpecials] Converter for Custom from " + input.name + ".", s);
        });
        return DynamicContainer.dynamicContainer("[testConversionFromSpecials] Converter for Custom.", result);
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
                            var cvt = ConverterFactory.std().get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        if (nodes.isEmpty()) throw new AssertionError();
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
                            var cvt = ConverterFactory.std().get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        if (nodes.isEmpty()) throw new AssertionError();
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
                            var cvt = ConverterFactory.std().get(k2);
                            Assertions.assertAll(
                                    () -> TestTypes.compare(o2, cvt.from(r).get()),
                                    () -> TestTypes.compare(o2, cvt.fromObj(r).get())
                            );
                        }
                );
                nodes.add(nd);
            }
        }
        if (nodes.isEmpty()) throw new AssertionError();
        return nodes;
    }
}