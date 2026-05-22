package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class UrlSqlFactoryLoadTest {

    @SqlFromUrl(value = "http://localhost:8080/on_first_time_that_works.txt", policy = ReadPolicy.ON_FIRST_TIME_THAT_WORKS)
    private static void withSqlOnFirstTimeThatWorks() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/on_startup.txt", policy = ReadPolicy.ON_STARTUP)
    private static void withSqlOnStartup() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/every_time.txt", policy = ReadPolicy.EVERY_TIME)
    private static void withSqlOnEveryTime() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/on_first_time_dont_retry.txt", policy = ReadPolicy.ON_FIRST_TIME_DONT_RETRY)
    private static void withSqlOnFirstTimeDontRetry() {
        throw new AssertionError();
    }

    private static SimpleHttpServer makeServer() throws IOException {
        return SimpleHttpServer.start(8080, (s, h, i, o) -> {
            if (works[0] == -1) {
                throw new AssertionError();
            }
            if (works[0] > 0) {
                new SimpleHttpServer.Content("text/plain; charset=utf-8", ("xyz" + works[0]).getBytes(StandardCharsets.UTF_8)).handle(s, h, i, o);
            } else {
                SimpleHttpServer.output500(s, h, i, o);
            }
        });
    }

    private static Method mtd(String name) {
        return Stream.of(UrlSqlFactoryLoadTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    private static void test500(SqlSupplier s) throws Exception {
        var ex = Assertions.assertThrows(SQLException.class, () -> s.get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("HTTP Error: 500", ex.getMessage());
    }

    private static void testOk(SqlSupplier s, int workValue) throws Exception {
        Assertions.assertEquals("xyz" + workValue, s.get());
    }

    private static final int[] works = new int[1];
    private static SimpleHttpServer server;

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

    @Test
    public void testOnStartupSuccess() throws Exception {
        var m = mtd("withSqlOnStartup");
        works[0] = 13;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works[0] = -1;
        testOk(f, 13);
        testOk(f, 13);
    }

    @Test
    public void testOnStartupError() throws Exception {
        var m = mtd("withSqlOnStartup");
        works[0] = 0;
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> UrlSqlFactory.INSTANCE.prepare(m));
        Assertions.assertEquals("Can't read from source.", ex.getMessage());
    }

    @Test
    public void testEveryTime() throws Exception {
        var m = mtd("withSqlOnEveryTime");
        works[0] = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works[0] = 0;
        test500(f);
        works[0] = 5;
        testOk(f, 5);
        works[0] = 0;
        test500(f);
        works[0] = 13;
        testOk(f, 13);
        testOk(f, 13);
        works[0] = 7;
        testOk(f, 7);
    }

    @Test
    public void testOnFirstTimeThatWorks() throws Exception {
        var m = mtd("withSqlOnFirstTimeThatWorks");
        works[0] = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works[0] = 0;
        test500(f);
        test500(f);
        works[0] = 13;
        testOk(f, 13);
        works[0] = -1;
        testOk(f, 13);
    }

    @Test
    public void testOnFirstTimeDontRetrySuccess() throws Exception {
        var m = mtd("withSqlOnFirstTimeDontRetry");
        works[0] = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works[0] = 13;
        testOk(f, 13);
        works[0] = -1;
        testOk(f, 13);
    }

    @Test
    public void testOnFirstTimeDontRetryError() throws Exception {
        var m = mtd("withSqlOnFirstTimeDontRetry");
        works[0] = -1;
        var f = UrlSqlFactory.INSTANCE.prepare(m);
        works[0] = 0;
        test500(f);
        works[0] = 13;
        test500(f);
        works[0] = -1;
        test500(f);
    }
}
