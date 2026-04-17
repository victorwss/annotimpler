package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class NullConverterTest {

    public static interface MethodSpec {
        public Optional<?> receive(Converter<?> cvt) throws Exception;
    }

    public static record NamedSpec(String name, MethodSpec spec, Class<?> base, Class<? extends Exception> err) {
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private <E> DynamicNode testIn(Class<E> base, MethodSpec m) throws Exception {
        List<DynamicNode> nodes1 = new ArrayList<>(TestTypes.CVT_CLASSES.size());
        for (var k1 : TestTypes.CVT_CLASSES) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + TypeName.of(k2) + " from " + base.getSimpleName() + " as null.",
                        () -> ForTests.testNull("in", () -> m.receive(cvt))
                );
                nodes1.add(nd1);
            }
        }
        return DynamicContainer.dynamicContainer("Test convertions from " + base.getSimpleName() + ".", nodes1);
    }

    @TestFactory
    @SuppressWarnings("null")
    public List<DynamicNode> testFromBadNull() throws Exception {
        return List.of(
                testIn(BigDecimal    .class, cvt -> cvt.from((BigDecimal    ) null)),
                testIn(LocalDateTime .class, cvt -> cvt.from((LocalDateTime ) null)),
                testIn(LocalDate     .class, cvt -> cvt.from((LocalDate     ) null)),
                testIn(LocalTime     .class, cvt -> cvt.from((LocalTime     ) null)),
                testIn(OffsetDateTime.class, cvt -> cvt.from((OffsetDateTime) null)),
                testIn(OffsetTime    .class, cvt -> cvt.from((OffsetTime    ) null)),
                testIn(Blob          .class, cvt -> cvt.from((Blob          ) null)),
                testIn(Clob          .class, cvt -> cvt.from((Clob          ) null)),
                testIn(NClob         .class, cvt -> cvt.from((NClob         ) null)),
                testIn(Ref           .class, cvt -> cvt.from((Ref           ) null)),
                testIn(RowId         .class, cvt -> cvt.from((RowId         ) null)),
                testIn(Struct        .class, cvt -> cvt.from((Struct        ) null)),
                testIn(SQLXML        .class, cvt -> cvt.from((SQLXML        ) null)),
                testIn(java.sql.Array.class, cvt -> cvt.from((java.sql.Array) null)),
                testIn(String        .class, cvt -> cvt.from((String        ) null)),
                testIn(byte[]        .class, cvt -> cvt.from((byte[]        ) null))
        );
    }

    private static Object zero(Type k, boolean special) {
        Class<?> c;
        if (k instanceof Class<?> cx) {
            c = cx;
        } else if (k instanceof ParameterizedType p) {
            c = (Class<?>) p.getRawType();
            var pp = p.getActualTypeArguments()[0];
            var a = List.of(List.class, Collection.class, Set.class, Optional.class).contains(c);
            var b = List.of(OptionalInt.class, OptionalLong.class, OptionalDouble.class).contains(pp);
            if (special && a && b) {
                var z = Map.ofEntries(
                        Map.entry(OptionalInt.class, OptionalInt.empty()),
                        Map.entry(OptionalLong.class, OptionalLong.empty()),
                        Map.entry(OptionalDouble.class, OptionalDouble.empty())
                ).get(pp);
                return Map.ofEntries(
                        Map.entry(Optional.class, Optional.of(z)),
                        Map.entry(List.class, List.of(z)),
                        Map.entry(Set.class, Set.of(z)),
                        Map.entry(Collection.class, List.of(z))
                ).get(c);
            }
        } else {
            throw new AssertionError();
        }

        if (c.isArray()) {
            return java.lang.reflect.Array.newInstance(c.getComponentType(), 0);
        }

        return Map.ofEntries(
                Map.entry(boolean.class, false),
                Map.entry(int.class, 0),
                Map.entry(long.class, 0L),
                Map.entry(short.class, (short) 0),
                Map.entry(byte.class, (byte) 0),
                Map.entry(char.class, '\0'),
                Map.entry(float.class, 0F),
                Map.entry(double.class, 0D),
                Map.entry(OptionalInt.class, OptionalInt.empty()),
                Map.entry(OptionalLong.class, OptionalLong.empty()),
                Map.entry(OptionalDouble.class, OptionalDouble.empty()),
                Map.entry(Optional.class, Optional.empty()),
                Map.entry(List.class, List.of()),
                Map.entry(Set.class, Set.of()),
                Map.entry(Collection.class, List.of())
        ).get(c);
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "null"})
    public List<DynamicNode> testFromOkNull() throws Exception {
        List<DynamicNode> nodes1 = new ArrayList<>(TestTypes.CVT_CLASSES.size());
        for (var k1 : TestTypes.CVT_CLASSES) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var z = Optional.ofNullable(zero(k2, false));
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + TypeName.of(k2) + " from Object null.",
                        () -> TestTypes.compare(z, cvt.fromObj(null))
                );
                nodes1.add(nd1);
                var nd2 = DynamicTest.dynamicTest(
                        "Converter for " + TypeName.of(k2) + " fromNull.",
                        () -> TestTypes.compare(z, cvt.fromNull())
                );
                nodes1.add(nd2);
            }
        }
        return nodes1;
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public List<DynamicNode> testFromBadJunkString() throws Exception {
        MethodSpec m = cvt -> cvt.from("xxx");
        List<DynamicNode> nodes1 = new ArrayList<>(TestTypes.CVT_CLASSES.size());
        for (var k1 : TestTypes.CVT_CLASSES) {
            if (List.of(String.class, Ref.class, Struct.class, RowId.class, java.sql.Array.class).contains(k1)) continue;
            for (var k2 : TestTypes.others(k1)) {
                if (k2 == byte[].class || k2 == char[].class) continue;
                var cvt = ConverterFactory.STD.get(k2);
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + TypeName.of(k2) + " with bad String.",
                        () -> {
                            var ex = Assertions.assertThrows(ConvertionException.class, () -> m.receive(cvt));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k2) + ".", ex.getMessage()),
                                    () -> Assertions.assertEquals(String.class, ex.getIn()),
                                    () -> Assertions.assertEquals(k2, ex.getOut())
                            );
                        }
                );
                nodes1.add(nd1);
            }
        }
        return nodes1;
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public List<DynamicNode> testFromBadJunkObject() throws Exception {
        MethodSpec m = cvt -> cvt.fromObj(Thread.currentThread());
        List<DynamicNode> nodes1 = new ArrayList<>(TestTypes.CVT_CLASSES.size());
        for (var k1 : TestTypes.CVT_CLASSES) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + TypeName.of(k2) + " with bad Object.",
                        () -> {
                            var ex = Assertions.assertThrows(ConvertionException.class, () -> m.receive(cvt));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Unsupported Type: java.lang.Thread.", ex.getMessage()),
                                    () -> Assertions.assertEquals(Thread.class, ex.getIn()),
                                    () -> Assertions.assertEquals(k2, ex.getOut())
                            );
                        }
                );
                nodes1.add(nd1);
            }
        }
        return nodes1;
    }

    @TestFactory
    public List<DynamicNode> testFromEmpty() throws Exception {
        MethodSpec m = cvt -> cvt.from("");
        List<DynamicNode> nodes1 = new ArrayList<>(TestTypes.CVT_CLASSES.size());
        for (var k1 : TestTypes.CVT_CLASSES) {
            if (List.of(String.class, Ref.class, Struct.class, RowId.class, java.sql.Array.class).contains(k1)) continue;
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                var nd1 = DynamicTest.dynamicTest(
                        "Converter for " + TypeName.of(k2) + " with empty String.",
                        () -> TestTypes.compare(Optional.ofNullable(zero(k2, true)), m.receive(cvt))
                );
                nodes1.add(nd1);
            }
        }
        return nodes1;
    }

    private Blob blobSqlex() {
        return (Blob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Blob.class }, (i, m, a) -> {
            if (m.getName().equals("getBinaryStream")) throw new SQLException("test");
            throw new AssertionError(m.getName());
        });
    }

    private NClob nclobSqlex() {
        return (NClob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { NClob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) throw new SQLException("test");
            throw new AssertionError(m.getName());
        });
    }

    private Clob clobSqlex() {
        return (Clob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Clob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) throw new SQLException("test");
            throw new AssertionError(m.getName());
        });
    }

    private SQLXML sqlxmlSqlex() {
        return (SQLXML) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { SQLXML.class }, (i, m, a) -> {
            if (m.getName().equals("getString")) throw new SQLException("test");
            throw new AssertionError(m.getName());
        });
    }

    private Blob blobIoex() {
        var is = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("test");
            }
        };
        return (Blob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Blob.class }, (i, m, a) -> {
            if (m.getName().equals("getBinaryStream")) return is;
            throw new AssertionError(m.getName());
        });
    }

    private NClob nclobIoex() {
        var is = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test");
            }

            @Override
            public void close() {
                throw new AssertionError();
            }
        };
        return (NClob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { NClob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return is;
            throw new AssertionError(m.getName());
        });
    }

    private Clob clobIoex() {
        var is = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test");
            }

            @Override
            public void close() {
                throw new AssertionError();
            }
        };
        return (Clob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Clob.class }, (i, m, a) -> {
            if (m.getName().equals("getCharacterStream")) return is;
            throw new AssertionError(m.getName());
        });
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public List<DynamicNode> testFromBadLob() throws Exception {
        List<NamedSpec> m = List.of(
                new NamedSpec("Blob throws SQLException"  , cvt -> cvt.from(blobSqlex  ()), Blob.class  , SQLException.class),
                new NamedSpec("Blob throws IOException"   , cvt -> cvt.from(blobIoex   ()), Blob.class  , IOException .class),
                new NamedSpec("Clob throws SQLException"  , cvt -> cvt.from(clobSqlex  ()), Clob.class  , SQLException.class),
                new NamedSpec("Clob throws IOException"   , cvt -> cvt.from(clobIoex   ()), Clob.class  , IOException .class),
                new NamedSpec("NClob throws SQLException" , cvt -> cvt.from(nclobSqlex ()), NClob.class , SQLException.class),
                new NamedSpec("NClob throws IOException"  , cvt -> cvt.from(nclobIoex  ()), NClob.class , IOException .class),
                new NamedSpec("SQLXML throws SQLException", cvt -> cvt.from(sqlxmlSqlex()), SQLXML.class, SQLException.class)
        );
        var blobs = List.of(String.class, byte[].class, char[].class);
        List<DynamicNode> nodes1 = new ArrayList<>(blobs.size());
        for (var k1 : blobs) {
            for (var k2 : TestTypes.others(k1)) {
                var cvt = ConverterFactory.STD.get(k2);
                List<DynamicNode> nodes2 = new ArrayList<>(m.size());
                for (var k3 : m) {
                    var nd2 = DynamicTest.dynamicTest(
                            "Converter for " + TypeName.of(k2) + " with " + k3.name() + ".",
                            () -> {
                                var ce = Assertions.assertThrows(ConvertionException.class, () -> k3.spec().receive(cvt));
                                var n = 0;
                                for (Throwable k = ce; k != null; k = k.getCause()) {
                                    n++;
                                }
                                if (n == 2) {
                                    Assertions.assertAll(
                                            () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k2) + ".", ce.getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ce.getIn()),
                                            () -> Assertions.assertEquals(k2, ce.getOut()),

                                            () -> Assertions.assertEquals(k3.err(), ce.getCause().getClass()),
                                            () -> Assertions.assertEquals("test", ce.getCause().getMessage())
                                    );
                                } else if (n == 3 && k1 != k2) {
                                    Assertions.assertAll(
                                            () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k2) + ".", ce.getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ce.getIn()),
                                            () -> Assertions.assertEquals(k2, ce.getOut()),

                                            () -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()),
                                            () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k1) + ".", ce.getCause().getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ((ConvertionException) ce.getCause()).getIn()),
                                            () -> Assertions.assertEquals(k1, ((ConvertionException) ce.getCause()).getOut()),

                                            () -> Assertions.assertEquals(k3.err(), ce.getCause().getCause().getClass()),
                                            () -> Assertions.assertEquals("test", ce.getCause().getCause().getMessage())
                                    );
                                } else if (n == 3) {
                                    Assertions.assertAll(
                                            () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k2) + ".", ce.getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ce.getIn()),
                                            () -> Assertions.assertEquals(k2, ce.getOut()),

                                            () -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()),
                                            () -> Assertions.assertEquals("Can't read value as String.", ce.getCause().getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ((ConvertionException) ce.getCause()).getIn()),
                                            () -> Assertions.assertEquals(String.class, ((ConvertionException) ce.getCause()).getOut()),

                                            () -> Assertions.assertEquals(k3.err(), ce.getCause().getCause().getClass()),
                                            () -> Assertions.assertEquals("test", ce.getCause().getCause().getMessage())
                                    );
                                } else if (n == 4) {
                                    Assertions.assertAll(
                                            () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k2) + ".", ce.getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ce.getIn()),
                                            () -> Assertions.assertEquals(k2, ce.getOut()),

                                            () -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getClass()),
                                            () -> Assertions.assertEquals("Can't read value as " + TypeName.of(k1) + ".", ce.getCause().getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ((ConvertionException) ce.getCause()).getIn()),
                                            () -> Assertions.assertEquals(k1, ((ConvertionException) ce.getCause()).getOut()),

                                            () -> Assertions.assertEquals(ConvertionException.class, ce.getCause().getCause().getClass()),
                                            () -> Assertions.assertEquals("Can't read value as String.", ce.getCause().getCause().getMessage()),
                                            () -> Assertions.assertEquals(k3.base(), ((ConvertionException) ce.getCause().getCause()).getIn()),
                                            () -> Assertions.assertEquals(String.class, ((ConvertionException) ce.getCause().getCause()).getOut()),

                                            () -> Assertions.assertEquals(k3.err(), ce.getCause().getCause().getCause().getClass()),
                                            () -> Assertions.assertEquals("test", ce.getCause().getCause().getCause().getMessage())
                                    );
                                } else {
                                    throw new AssertionError();
                                }
                            }
                    );
                    nodes2.add(nd2);
                }
                nodes1.add(DynamicContainer.dynamicContainer("Test convertions for " + TypeName.of(k2) + ".", nodes2));
            }
        }
        return nodes1;
    }
}
