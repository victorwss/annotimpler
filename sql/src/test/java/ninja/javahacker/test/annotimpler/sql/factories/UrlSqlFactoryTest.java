package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class UrlSqlFactoryTest {

    private static final String LOREM_ISO_88591 = "Lorem ipsum dolor sit amet - \u00FF áéíóúñçªº \u00C0 - Lorem ipsum dolor sit amet";

    private static final String LOREM_UTF_8 = "Lorem ipsum dolor sit amet 🤩😁🤩😁🥳!";

    private static SimpleHttpServer SERVER;

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
    @SqlFromUrl(value = "http://localhost:8080/lorem-iso-8859-1e.txt", fallbackEncoding = CharsetSpec.Iso88591.class)
    private static void withSql4() {
        throw new AssertionError();
    }

    // Tests if fallbackEncoding is ignored when encoding is provided.
    @SqlFromUrl(value = "http://localhost:8080/lorem-utf-8.txt", fallbackEncoding = CharsetSpec.Iso88591.class)
    private static void withSql5() {
        throw new AssertionError();
    }

    // Tests for 404.
    @SqlFromUrl("http://localhost:8080/does-not-exist.txt")
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

    @BeforeAll
    public static void before() throws Exception {
        Map<String, Supplier<SimpleHttpServer.Content>> m = Map.of(
                "/lorem-utf-8.txt", () -> new SimpleHttpServer.Content("text/plain; charset=utf-8", LOREM_UTF_8.getBytes(StandardCharsets.UTF_8)),
                "/lorem-utf-8e.txt", () -> new SimpleHttpServer.Content("text/plain", LOREM_UTF_8.getBytes(StandardCharsets.UTF_8)),
                "/lorem-utf-8x.txt", () -> new SimpleHttpServer.Content("text/plain; charset=utf-8", LOREM_ISO_88591.getBytes(StandardCharsets.ISO_8859_1)),
                "/lorem-iso-8859-1.txt", () -> new SimpleHttpServer.Content("text/plain; charset=iso-8859-1", LOREM_ISO_88591.getBytes(StandardCharsets.ISO_8859_1)),
                "/lorem-iso-8859-1e.txt", () -> new SimpleHttpServer.Content("text/plain", LOREM_ISO_88591.getBytes(StandardCharsets.ISO_8859_1)),
                "/ping.txt", () -> new SimpleHttpServer.Content("text/plain; charset=utf-8", "PING!".getBytes(StandardCharsets.UTF_8))
        );
        SERVER = SimpleHttpServer.start(8080, SimpleHttpServer.staticFiles(m));
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

    @Test
    public void testUrlSql() throws Exception {
        var m1 = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql1")).findAny().get();
        var m2 = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql2")).findAny().get();
        var m3 = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql3")).findAny().get();
        var m4 = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql4")).findAny().get();
        var m5 = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql5")).findAny().get();
        var t1 = UrlSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals(LOREM_UTF_8, t1);
        var t2 = UrlSqlFactory.INSTANCE.prepare(m2).get();
        Assertions.assertEquals(LOREM_UTF_8, t2);
        var t3 = UrlSqlFactory.INSTANCE.prepare(m3).get();
        Assertions.assertEquals(LOREM_ISO_88591, t3);
        var t4 = UrlSqlFactory.INSTANCE.prepare(m4).get();
        Assertions.assertEquals(LOREM_ISO_88591, t4);
        var t5 = UrlSqlFactory.INSTANCE.prepare(m5).get();
        Assertions.assertEquals(LOREM_UTF_8, t5);
    }

    @Test
    public void testUrlSqlDoesNotExist() throws Exception {
        var mx = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX1")).findAny().get();
        var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("HTTP Error: 404", ex.getMessage());
    }

    @Test
    public void testUrlSqlBadEncoding1() throws Exception {
        var mx = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX2")).findAny().get();
        var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("String can't be coded as UTF-8.", ex.getMessage());
    }

    @Test
    public void testUrlSqlBadEncoding2() throws Exception {
        var mx = Stream.of(UrlSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX3")).findAny().get();
        var ex = Assertions.assertThrows(SQLException.class, () -> UrlSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("String can't be coded as UTF-8.", ex.getMessage());
    }

    /*/@Test
    public void testBadSupplierClass() throws Exception {
        var mx = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX3")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> UrlSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass());
    }

    @Test
    public void testBadSupplierCtor() throws Exception {
        var mx = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX4")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> UrlSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass());
    }/*/
}
