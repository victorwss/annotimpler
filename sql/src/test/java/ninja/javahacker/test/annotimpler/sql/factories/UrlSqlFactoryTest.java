package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class UrlSqlFactoryTest {

    private static final String LOREM_ISO_88591 = "Lorem ipsum dolor sit amet - \u00FF áéíóúñçªº \u00C0 - Lorem ipsum dolor sit amet";

    private static final String LOREM_UTF_8 = "Lorem ipsum dolor sit amet 🤩😁🤩😁🥳!";

    private static SimpleHttpServer SERVER;

    public UrlSqlFactoryTest() {
    }

    // Tests if it works with UTF-8.
    @SqlFromUrl(value = "http://localhost:8080/lorem-utf-8.txt")
    private static void withSql1() {
        throw new AssertionError();
    }

    // Tests if defaults to UTF-8 when there is no provided encoding.
    @SqlFromUrl(value = "http://localhost:8080/lorem-utf-8e.txt")
    private static void withSql2() {
        throw new AssertionError();
    }

    // Tests if it works with ISO-8859-1.
    @SqlFromUrl(value = "http://localhost:8080/lorem-iso-8859-1.txt")
    private static void withSql3() {
        throw new AssertionError();
    }

    // Tests if fallbackEncoding saves it when is not UTF-8 but no encoding is provided.
    @SqlFromUrl(value = "http://localhost:8080/lorem-iso-8859-1e.txt", fallbackEncoding = CharsetSpec.Iso88591Strict.class)
    private static void withSql4() {
        throw new AssertionError();
    }

    // Tests if fallbackEncoding is ignored when encoding is provided.
    @SqlFromUrl(value = "http://localhost:8080/lorem-utf-8.txt", fallbackEncoding = CharsetSpec.Iso88591Strict.class)
    private static void withSql5() {
        throw new AssertionError();
    }

    // Test if wrong provided encoding maight be saved by ignoring the provided encoding.
    @SqlFromUrl(value = "http://localhost:8080/lorem-utf-8x.txt", fallbackEncoding = CharsetSpec.Iso88591Strict.class, getEncodingFromHeaders = false)
    private static void withSql6() {
        throw new AssertionError();
    }

    // Tests for 404.
    @SqlFromUrl(value = "http://localhost:8080/does-not-exist.txt")
    private static void withSqlX1() {
        throw new AssertionError();
    }

    // Test if wrong provided encoding makes it crash.
    @SqlFromUrl(value = "http://localhost:8080/lorem-utf-8x.txt")
    private static void withSqlX2() {
        throw new AssertionError();
    }

    // Test if wrong fallback encoding with no encoding provided makes it crash.
    @SqlFromUrl(value = "http://localhost:8080/lorem-iso-8859-1e.txt", fallbackEncoding = CharsetSpec.Utf8.class)
    private static void withSqlX3() {
        throw new AssertionError();
    }

    // Test if error 500 makes it crash.
    @SqlFromUrl(value = "http://localhost:8080/crash.txt")
    private static void withSqlX4() {
        throw new AssertionError();
    }

    // Test if error 166 (made up) makes it crash.
    @SqlFromUrl(value = "http://localhost:8080/nuts.txt")
    private static void withSqlX5() {
        throw new AssertionError();
    }

    // Test if error 666 (made up) makes it crash.
    @SqlFromUrl(value = "http://localhost:8080/devil.txt")
    private static void withSqlX6() {
        throw new AssertionError();
    }

    // Test if error 1234 (made up) makes it crash.
    @SqlFromUrl(value = "http://localhost:8080/crazy.txt")
    private static void withSqlX7() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/idiot.txt")
    private static void withSqlCrash1() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/silence.txt")
    private static void withSqlCrash2() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/no-answer.txt")
    private static void withSqlCrash3() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/vanish.txt")
    private static void withSqlCrash4() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/non-sense.txt")
    private static void withSqlCrash5() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://nowhere.example.com:8080/whatever.txt")
    private static void withSqlCrash6() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:9999/wrong-port.txt")
    private static void withSqlCrash7() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "file:///C/foo.txt")
    private static void withSqlCrash8() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "wtf://foo/bar/crash")
    private static void withSqlCrash9() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "blamblamblam")
    private static void withSqlCrash10() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "")
    private static void withSqlCrash11() {
        throw new AssertionError();
    }

    public static void outputNuts(Socket client, SimpleHttpServer.HttpRequestHeaders headers, SimpleHttpServer.Input in, SimpleHttpServer.Output out) throws IOException {
        var head = """
                   HTTP/1.1 166 I am Nuts
                   Content-Type: crash/sorry; charset=utf-8
                   Connection: Screw up
                   X-Crazy-Stuff: Stoned
                   $Z
                   LOLOL""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    public static void outputDevil(Socket client, SimpleHttpServer.HttpRequestHeaders headers, SimpleHttpServer.Input in, SimpleHttpServer.Output out) throws IOException {
        var head = """
                   HTTP/1.1 666 Devil
                   Content-Type: hell/devil; charset=utf-666
                   Connection: Death
                   $Z
                   What a hell of an error!""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    public static void outputCrazy(Socket client, SimpleHttpServer.HttpRequestHeaders headers, SimpleHttpServer.Input in, SimpleHttpServer.Output out) throws IOException {
        var head = """
                   HTTP/1.1 1234 Way too large status code
                   X-Crazy-Stuff: Ha ha ha
                   $Z
                   Oops!""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeAll
    public static void before() throws Exception {
        Map<String, SimpleHttpServer.RequestHandler> m = Map.ofEntries(
                Map.entry("/lorem-utf-8.txt", new SimpleHttpServer.Content("text/plain; charset=utf-8", LOREM_UTF_8.getBytes(StandardCharsets.UTF_8))),
                Map.entry("/lorem-utf-8e.txt", new SimpleHttpServer.Content("text/plain", LOREM_UTF_8.getBytes(StandardCharsets.UTF_8))),
                Map.entry("/lorem-utf-8x.txt", new SimpleHttpServer.Content("text/plain; charset=utf-8", LOREM_ISO_88591.getBytes(StandardCharsets.ISO_8859_1))),
                Map.entry("/lorem-iso-8859-1.txt", new SimpleHttpServer.Content("text/plain; charset=iso-8859-1", LOREM_ISO_88591.getBytes(StandardCharsets.ISO_8859_1))),
                Map.entry("/lorem-iso-8859-1e.txt",new SimpleHttpServer.Content("text/plain", LOREM_ISO_88591.getBytes(StandardCharsets.ISO_8859_1))),
                Map.entry("/ping.txt", new SimpleHttpServer.Content("text/plain; charset=utf-8", "PING!".getBytes(StandardCharsets.UTF_8))),
                Map.entry("/crash.txt", SimpleHttpServer::output500),
                Map.entry("/devil.txt", UrlSqlFactoryTest::outputDevil),
                Map.entry("/crazy.txt", UrlSqlFactoryTest::outputCrazy),
                Map.entry("/idiot.txt", (s, h, in, out) -> out.write("blam blam blam".getBytes(StandardCharsets.UTF_8))),
                Map.entry("/silence.txt", SimpleHttpServer.DO_NOTHING),
                Map.entry("/no-answer.txt", SimpleHttpServer.ABORT),
                Map.entry("/vanish.txt", SimpleHttpServer.DROP),
                Map.entry("/non-sense.txt", (s, h, in, out) -> out.write(new byte[] {5, 15, 20, -1, -2, 0, 1})),
                Map.entry("/nuts.txt", UrlSqlFactoryTest::outputNuts)
        );
        SERVER = SimpleHttpServer.start(8080, SimpleHttpServer.split(m));
        System.out.println("UP!");
    }

    @AfterAll
    public static void after() throws Exception {
        SERVER.close();
        System.out.println("Down!");
    }

    @Test
    public void sanityCheck() throws Exception {
        var url = new URI("http://localhost:8080/ping.txt").toURL();
        byte[] all;
        try (var s = url.openStream()) {
            all = s.readAllBytes();
        }
        Assertions.assertEquals("PING!", new String(all, StandardCharsets.UTF_8));
    }

    private static Method mtd(String name) {
        return Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    @TestFactory
    public Stream<DynamicTest> testUrlSql() throws Exception {
        var a = Stream.of("withSql1", "withSql2", "withSql5")
                .map(m -> DynamicTest.dynamicTest(m, () -> Assertions.assertEquals(LOREM_UTF_8, UrlSqlFactory.INSTANCE.prepare(mtd(m)).get())));
        var b = Stream.of("withSql3", "withSql4", "withSql6")
                .map(m -> DynamicTest.dynamicTest(m, () -> Assertions.assertEquals(LOREM_ISO_88591, UrlSqlFactory.INSTANCE.prepare(mtd(m)).get())));
        return Stream.concat(a, b);
    }

    @Test
    public void testUrlSqlDoesNotExist() throws Exception {
        var m = "withSqlX1";
        var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
        Assertions.assertTrue(ex.getCause() instanceof FileNotFoundException);
        Assertions.assertEquals("http://localhost:8080/does-not-exist.txt", ex.getMessage());
    }

    @Test
    public void testUrlSqlHasError() throws Exception {
        var m = "withSqlX4";
        var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("HTTP Error: 500", ex.getMessage());
    }

    @Test
    public void testUrlSqlHasDiabolicError() throws Exception {
        var m = "withSqlX6";
        var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("HTTP Error: 666", ex.getMessage());
    }

    @TestFactory
    public Stream<DynamicTest> testUrlSqlHasStrangeError() throws Exception {
        return Stream.of("withSqlX5", "withSqlX7").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> testUrlSqlBadEncoding() throws Exception {
        return Stream.of("withSqlX2", "withSqlX3").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertEquals("String can't be coded as UTF-8.", ex.getMessage());
        }));
    }

    @TestFactory
    public Stream<DynamicTest> testUrlSqlMisbehavingServer1() throws Exception {
        return Stream.of("withSqlCrash1", "withSqlCrash5").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertTrue(ex.getMessage().startsWith("parsing HTTP/1.1 status line"));
        }));
    }

    @TestFactory
    public Stream<DynamicTest> testUrlSqlMisbehavingServer2() throws Exception {
        return Stream.of("withSqlCrash2", "withSqlCrash3", "withSqlCrash4").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertEquals("HTTP/1.1 header parser received no bytes", ex.getMessage());
        }));
    }

    @TestFactory
    public Stream<DynamicTest> testUrlSqlNoServer() throws Exception {
        return Stream.of("withSqlCrash6", "withSqlCrash7").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertTrue(ex.getCause().getCause() instanceof SocketException);
            Assertions.assertEquals("Download failed. Server didn't answer.", ex.getMessage());
        }));
    }

    @TestFactory
    public Stream<DynamicTest> testBadUrlSql() throws Exception {
        return Stream.of("withSqlCrash8", "withSqlCrash9").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertTrue(ex.getCause().getCause() instanceof IllegalArgumentException);
            Assertions.assertTrue(ex.getCause().getCause().getMessage().startsWith("invalid URI scheme "));
        }));
    }

    @TestFactory
    public Stream<DynamicTest> testBadUrlMalformed() throws Exception {
        return Stream.of("withSqlCrash10", "withSqlCrash11").map(m -> DynamicTest.dynamicTest(m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertTrue(ex.getCause().getCause() instanceof IllegalArgumentException);
            Assertions.assertEquals("URI with undefined scheme", ex.getCause().getCause().getMessage());
        }));
    }
}
