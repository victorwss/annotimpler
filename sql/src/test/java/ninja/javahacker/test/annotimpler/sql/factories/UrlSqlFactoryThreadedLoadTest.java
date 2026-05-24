package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class UrlSqlFactoryThreadedLoadTest {

    private final Object lock = new Object();
    private volatile boolean okA;
    private volatile boolean okB;
    private volatile String r1;
    private volatile String r2;
    private volatile String r3;
    private volatile Throwable x1;
    private volatile Throwable x2;
    private volatile Throwable x3;
    private volatile int works = -1;
    private SimpleHttpServer server;

    @SqlFromUrl(value = "http://localhost:8080/on_first_time_that_works.txt", policy = ReadPolicy.ON_FIRST_TIME_THAT_WORKS)
    private static void withSqlOnFirstTimeThatWorks() {
        throw new AssertionError();
    }

    @SqlFromUrl(value = "http://localhost:8080/on_first_time_dont_retry.txt", policy = ReadPolicy.ON_FIRST_TIME_DONT_RETRY)
    private static void withSqlOnFirstTimeDontRetry() {
        throw new AssertionError();
    }

    private SimpleHttpServer makeServer() throws IOException {
        return SimpleHttpServer.start(8080, (s, h, i, o) -> {
            var w = works;
            if (w == -1) throw new AssertionError();
            synchronized (lock) {
                okA = true;
                lock.notifyAll();
                var c = 0;
                while (!okB && c < 50) {
                    try {
                        lock.wait(50);
                    } catch (InterruptedException x) {
                        throw new AssertionError();
                    }
                }
                if (c >= 50) throw new AssertionError();
            }
            if (w > 0) {
                new SimpleHttpServer.Content("text/plain; charset=utf-8", ("xyz" + w).getBytes(StandardCharsets.UTF_8)).handle(s, h, i, o);
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

    private static void testFails(Throwable ex) throws Exception {
        Assertions.assertTrue(ex instanceof SQLException);
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("HTTP Error: 444", ex.getMessage());
    }

    @BeforeEach
    public void before() throws Exception {
        server = makeServer();
        System.out.println("UP!");
    }

    @AfterEach
    public void after() throws Exception {
        server.close();
        System.out.println("Down!");
    }

    private static Method mtd(String name) {
        return Stream.of(UrlSqlFactoryThreadedLoadTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    private void threadedTestSuccess(String mName, boolean success) throws Exception {
        var m = mtd(mName);
        works = -1;
        var tt = (ThreadTracerSqlSupplier) UrlSqlFactory.INSTANCE.prepare(m);
        var t1 = new Thread(() -> {
            try {
                r1 = tt.get();
            } catch (Throwable e) {
                x1 = e;
            }
        });
        var t2 = new Thread(() -> {
            try {
                r2 = tt.get();
            } catch (Throwable e) {
                x2 = e;
            }
        });
        synchronized (lock) {
            works = success ? 13 : 0;
            t1.start();
            var count = 0;
            while (!okA && count < 50) {
                lock.wait(50);
                count++;
            }
            if (count >= 50) throw new AssertionError();
            works = -1;
            t2.start();
            Thread.sleep(1000);
            Assertions.assertTrue(tt.has(t1));
            Assertions.assertTrue(tt.has(t2));
            okB = true;
            lock.notifyAll();
        }
        t1.join(1000);
        t2.join(1000);
        if (t1.getState() != Thread.State.TERMINATED) throw new AssertionError();
        if (t2.getState() != Thread.State.TERMINATED) throw new AssertionError();
        if (success) {
            Assertions.assertNull(x1);
            Assertions.assertNull(x2);
            Assertions.assertEquals("xyz13", r1);
            Assertions.assertEquals("xyz13", r2);
        } else {
            testFails(x1);
            testFails(x2);
            Assertions.assertNull(r1);
            Assertions.assertNull(r2);
        }
    }

    @Test
    @Timeout(5)
    public void testOnFirstTimeThatWorksSuccessThreaded() throws Exception {
        threadedTestSuccess("withSqlOnFirstTimeThatWorks", true);
    }

    @Test
    @Timeout(5)
    public void testOnFirstTimeDontRetrySuccessThreaded() throws Exception {
        threadedTestSuccess("withSqlOnFirstTimeDontRetry", true);
    }

    @Test
    @Timeout(5)
    public void testOnFirstTimeDontRetryFailThreaded() throws Exception {
        threadedTestSuccess("withSqlOnFirstTimeDontRetry", false);
    }

    @Test
    @Timeout(5)
    public void testOnFirstTimeThatWorksFailThreaded() throws Exception {
        var m = mtd("withSqlOnFirstTimeThatWorks");
        works = -1;
        var tt = (ThreadTracerSqlSupplier) UrlSqlFactory.INSTANCE.prepare(m);
        var t1 = new Thread(() -> {
            try {
                r1 = tt.get();
            } catch (Throwable e) {
                x1 = e;
            }
        });
        var t2 = new Thread(() -> {
            try {
                r2 = tt.get();
            } catch (Throwable e) {
                x2 = e;
            }
        });
        var t3 = new Thread(() -> {
            try {
                r3 = tt.get();
            } catch (Throwable e) {
                x3 = e;
            }
        });
        synchronized (lock) {
            works = 0;
            t1.start();
            var count = 0;
            while (!okA && count < 50) {
                lock.wait(50);
                count++;
            }
            if (count >= 50) throw new AssertionError();
            works = -1;
            t2.start();
            t3.start();
            Thread.sleep(20);
            Assertions.assertTrue(tt.has(t1));
            Assertions.assertTrue(tt.has(t2));
            Assertions.assertTrue(tt.has(t3));
            works = 13;
            okA = false;
            okB = true;
            lock.notifyAll();
            count = 0;
            while (!okA && count < 50) {
                lock.wait(50);
                count++;
            }
            if (count >= 50) throw new AssertionError();
            works = -1;
        }
        t1.join(1000);
        t2.join(1000);
        t3.join(1000);
        if (t1.getState() != Thread.State.TERMINATED) throw new AssertionError();
        if (t2.getState() != Thread.State.TERMINATED) throw new AssertionError();
        if (t3.getState() != Thread.State.TERMINATED) throw new AssertionError();
        testFails(x1);
        Assertions.assertNull(x2);
        Assertions.assertNull(x3);
        Assertions.assertNull(r1);
        Assertions.assertEquals("xyz13", r2);
        Assertions.assertEquals("xyz13", r3);
    }
}
