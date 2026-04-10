package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class NullConverterTest {

    public static interface MethodSpec {
        public Optional<?> receive(Converter<?> cvt) throws Exception;
    }

    public static record NamedSpec(String name, MethodSpec spec, Class<?> base, Class<? extends Exception> err) {
    }

    private static final List<Class<?>> CVT_CLASSES = List.of(
            boolean.class, byte.class, short.class, int    .class, long.class, float.class, double.class,
            Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigDecimal.class, BigInteger.class, String.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
            Calendar.class, GregorianCalendar.class, java.util.Date.class, java.sql.Date.class, Time.class, java.sql.Timestamp.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, OffsetTime.class, Instant.class,
            Ref.class, RowId.class, Struct.class, java.sql.Array.class
    );

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private <E> DynamicNode testIn(Class<E> base, MethodSpec m) throws Exception {
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            var cvt = ConverterFactory.STD.get(k1);
            var nd1 = DynamicTest.dynamicTest(
                    "Converter for " + k1.getSimpleName() + " from " + base.getSimpleName() + " as null.",
                    () -> ForTests.testNull("in", () -> m.receive(cvt))
            );
            nodes1.add(nd1);
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

    private static Object zero(Class<?> k) {
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
                Map.entry(OptionalDouble.class, OptionalDouble.empty())
        ).get(k);
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "null"})
    public List<DynamicNode> testFromOkNull() throws Exception {
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            var cvt = ConverterFactory.STD.get(k1);
            var z = Optional.ofNullable(zero(k1));
            var nd1 = DynamicTest.dynamicTest(
                    "Converter for " + k1.getSimpleName() + " from Object null.",
                    () -> Assertions.assertEquals(z, cvt.fromObj(null))
            );
            nodes1.add(nd1);
            var nd2 = DynamicTest.dynamicTest(
                    "Converter for " + k1.getSimpleName() + " fromNull.",
                    () -> Assertions.assertEquals(z, cvt.fromNull())
            );
            nodes1.add(nd2);
        }
        return nodes1;
    }

    @TestFactory
    @SuppressWarnings("null")
    public List<DynamicNode> testFromBadJunk() throws Exception {
        MethodSpec m = cvt -> cvt.from("xxx");
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            if (List.of(String.class, Ref.class, Struct.class, RowId.class, java.sql.Array.class).contains(k1)) continue;
            var cvt = ConverterFactory.STD.get(k1);
            var nd1 = DynamicTest.dynamicTest(
                    "Converter for " + k1.getSimpleName() + " with bad String.",
                    () -> {
                        var ex = Assertions.assertThrows(ConvertionException.class, () -> m.receive(cvt));
                        Assertions.assertAll(
                                () -> Assertions.assertEquals("Can't read value as " + k1.getSimpleName() + ".", ex.getMessage()),
                                () -> Assertions.assertEquals(String.class, ex.getIn()),
                                () -> Assertions.assertEquals(k1, ex.getOut())
                        );
                    }
            );
            nodes1.add(nd1);
        }
        return nodes1;
    }

    @TestFactory
    @SuppressWarnings("null")
    public List<DynamicNode> testFromEmpty() throws Exception {
        MethodSpec m = cvt -> cvt.from("");
        List<DynamicNode> nodes1 = new ArrayList<>(CVT_CLASSES.size());
        for (var k1 : CVT_CLASSES) {
            if (List.of(String.class, Ref.class, Struct.class, RowId.class, java.sql.Array.class).contains(k1)) continue;
            var cvt = ConverterFactory.STD.get(k1);
            var nd1 = DynamicTest.dynamicTest(
                    "Converter for " + k1.getSimpleName() + " with empty String.",
                    () -> Assertions.assertEquals(Optional.ofNullable(zero(k1)), m.receive(cvt))
            );
            nodes1.add(nd1);
        }
        return nodes1;
    }

    private Blob blobSqlex() {
        return (Blob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Blob.class }, (i, m, a) -> {
            if (!m.getName().equals("getBinaryStream")) throw new AssertionError(m.getName());
            throw new SQLException("test");
        });
    }

    private NClob nclobSqlex() {
        return (NClob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { NClob.class }, (i, m, a) -> {
            if (!m.getName().equals("getCharacterStream")) throw new AssertionError(m.getName());
            throw new SQLException("test");
        });
    }

    private Clob clobSqlex() {
        return (Clob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Clob.class }, (i, m, a) -> {
            if (!m.getName().equals("getCharacterStream")) throw new AssertionError(m.getName());
            throw new SQLException("test");
        });
    }

    private SQLXML sqlxmlSqlex() {
        return (SQLXML) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { SQLXML.class }, (i, m, a) -> {
            if (!m.getName().equals("getString")) throw new AssertionError(m.getName());
            throw new SQLException("test");
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
            if (!m.getName().equals("getBinaryStream")) throw new AssertionError(m.getName());
            return is;
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
            if (!m.getName().equals("getCharacterStream")) throw new AssertionError(m.getName());
            return is;
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
            if (!m.getName().equals("getCharacterStream")) throw new AssertionError(m.getName());
            return is;
        });
    }

    @TestFactory
    @SuppressWarnings("null")
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
        var blobs = List.of(String.class, byte[].class);
        List<DynamicNode> nodes1 = new ArrayList<>(blobs.size());
        for (var k1 : blobs) {
            var cvt = ConverterFactory.STD.get(k1);
            List<DynamicNode> nodes2 = new ArrayList<>(m.size());
            for (var k2 : m) {
                var nd2 = DynamicTest.dynamicTest(
                        "Converter for " + k1.getSimpleName() + " with " + k2.name() + ".",
                        () -> {
                            var ce = Assertions.assertThrows(ConvertionException.class, () -> k2.spec().receive(cvt));
                            Assertions.assertAll(
                                    () -> Assertions.assertEquals("Can't read value as " + k1.getSimpleName() + ".", ce.getMessage()),
                                    () -> Assertions.assertEquals(k2.base(), ce.getIn()),
                                    () -> Assertions.assertEquals(k1, ce.getOut()),
                                    () -> Assertions.assertEquals(k2.err(), ce.getCause().getClass()),
                                    () -> Assertions.assertEquals("test", ce.getCause().getMessage())
                            );
                        }
                );
                nodes2.add(nd2);
            }
            nodes1.add(DynamicContainer.dynamicContainer("Test convertions for " + k1.getSimpleName() + ".", nodes2));
        }
        return nodes1;
    }
}
