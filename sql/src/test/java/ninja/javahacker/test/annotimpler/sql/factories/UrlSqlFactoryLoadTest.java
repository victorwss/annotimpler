package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class UrlSqlFactoryLoadTest {

    private static volatile int works = -1;
    private static SimpleHttpServer server;

    public UrlSqlFactoryLoadTest() {
    }

    @SqlFromUrl(value = "http://localhost:8081/on_first_time_that_works.txt", policy = ReadPolicy.ON_FIRST_TIME_THAT_WORKS)
    private static void withSqlOnFirstTimeThatWorks() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8081/on_startup.txt", policy = ReadPolicy.ON_STARTUP)
    private static void withSqlOnStartup() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8081/every_time.txt", policy = ReadPolicy.EVERY_TIME)
    private static void withSqlOnEveryTime() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8081/on_first_time_dont_retry.txt", policy = ReadPolicy.ON_FIRST_TIME_DONT_RETRY)
    private static void withSqlOnFirstTimeDontRetry() {
        throw new AssertionError();
    }

    private static SimpleHttpServer makeServer() throws IOException {
        return SimpleHttpServer.start(8081, (s, h, i, o) -> {
            if (works == -1) {
                throw new AssertionError();
            }
            if (works > 0) {
                new SimpleHttpServer.Content("text/plain; charset=utf-8", ("xyz" + works).getBytes(StandardCharsets.UTF_8)).handle(s, h, i, o);
            } else {
                output444(o);
            }
        });
    }

    private static void output444(SimpleHttpServer.Output out) throws IOException {
        var head = """
                   HTTP/1.1 444 Strange Error
                   $Z
                   Strange Error""";
        var header = head.replace("$Z", "").replace("\n", "\r\n");
        out.write(header.getBytes(StandardCharsets.UTF_8));
    }

    private static void testFails(SqlSupplier s) throws Exception {
        var ex = Assertions.assertThrows(SQLException.class, () -> s.get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("HTTP Error: 444", ex.getMessage());
    }

    private static void testOk(SqlSupplier s, int workValue) throws Exception {
        Assertions.assertEquals("xyz" + workValue, s.get());
    }

    @BeforeAll
    public static void before() throws Exception {
        server = makeServer();
        System.out.println("UP!");
    }

    @AfterAll
    public static void after() throws Exception {
        server.close();
        System.out.println("Down!");
    }

    private static Method mtd(String name) {
        return Stream.of(UrlSqlFactoryLoadTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    @Test
    public void testOnStartupSuccess() throws Exception {
        var m = mtd("withSqlOnStartup");
        works = 13;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works = -1;
        testOk(f, 13);
        testOk(f, 13);
    }

    @Test
    public void testOnStartupError() throws Exception {
        var m = mtd("withSqlOnStartup");
        works = 0;
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> UrlSqlFactory.INSTANCE.prepare(m));
        Assertions.assertEquals("Can't read from source.", ex.getMessage());
    }

    @Test
    public void testEveryTime() throws Exception {
        var m = mtd("withSqlOnEveryTime");
        works = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works = 0;
        testFails(f);
        works = 5;
        testOk(f, 5);
        works = 0;
        testFails(f);
        works = 13;
        testOk(f, 13);
        testOk(f, 13);
        works = 7;
        testOk(f, 7);
    }

    @Test
    public void testOnFirstTimeThatWorks() throws Exception {
        var m = mtd("withSqlOnFirstTimeThatWorks");
        works = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works = 0;
        testFails(f);
        testFails(f);
        works = 13;
        testOk(f, 13);
        works = -1;
        testOk(f, 13);
    }

    @Test
    public void testOnFirstTimeDontRetrySuccess() throws Exception {
        var m = mtd("withSqlOnFirstTimeDontRetry");
        works = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works = 13;
        testOk(f, 13);
        works = -1;
        testOk(f, 13);
    }

    @Test
    public void testOnFirstTimeDontRetryError() throws Exception {
        var m = mtd("withSqlOnFirstTimeDontRetry");
        works = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works = 0;
        testFails(f);
        works = 13;
        testFails(f);
        works = -1;
        testFails(f);
    }
}
