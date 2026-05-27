package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class UrlSqlFactoryInterruptedDownloadTest {

    private static SimpleHttpServer SERVER;

    private static final Deadlock HANG = new Deadlock();

    public UrlSqlFactoryInterruptedDownloadTest() {
    }

    @SqlFromUrl(value = "http://localhost:8082/hang")
    private static void hang() {
        throw new AssertionError();
    }

    private static class Deadlock {
        private final Object lock = new Object();
        private volatile int step = 0;
        private volatile int ticks;

        public SimpleHttpServer.Content deadlock() {
            System.out.println("Server starting to hang.");
            try {
                synchronized (lock) {
                    if (step != 0) throw new AssertionError();
                    step = 1;
                    System.out.println("Server hangs.");
                    lock.notifyAll();
                    waitKill();
                }
            } catch (InterruptedException e) {
                // Do nothing.
            }
            System.out.println("Server was killed.");
            return null;
        }

        public void kill() {
            if (step != 1) throw new AssertionError();
            synchronized (lock) {
                step = 2;
                System.out.println("Server being killed.");
                lock.notifyAll();
            }
        }

        public boolean hanged() {
            synchronized (lock) {
                return step == 1;
            }
        }

        public void waitHang() throws InterruptedException {
            synchronized (lock) {
                while (step < 1) {
                    if (ticks == 50) throw new AssertionError("Crash waiting hang");
                    ticks++;
                    lock.wait(500);
                }
            }
        }

        public void waitKill() throws InterruptedException {
            synchronized (lock) {
                while (step != 2) {
                    if (ticks == 100) throw new AssertionError("Crash waiting kill");
                    ticks++;
                    lock.wait(500);
                }
            }
        }
    }

    @BeforeAll
    public static void before() throws Exception {
        SERVER = SimpleHttpServer.start(8082, (s, h, i, o) -> HANG.deadlock());
        System.out.println("UP!");
    }

    @AfterAll
    public static void after() throws Exception {
        SERVER.close();
        System.out.println("Down!");
    }

    private static Method mtd(String name) {
        return Stream.of(UrlSqlFactoryInterruptedDownloadTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    @Test
    @Timeout(5)
    public void testInterruptedDownload() throws InterruptedException {
        var m = "hang";
        var ex = new Exception[1];
        var out = Thread.currentThread();
        var t = new Thread(() -> {
            try {
                System.out.println("Starting hanging request.");
                UrlSqlFactory.INSTANCE.prepare(mtd(m)).get();
                out.interrupt();
                throw new AssertionError();
            } catch (Exception x) {
                System.out.println("Was interrupted.");
                ex[0] = x;
            }
            HANG.kill();
        });
        System.out.println("Starting proccess.");
        t.start();
        HANG.waitHang();
        System.out.println("Interrupting download.");
        t.interrupt();
        System.out.println("Waiting to finish.");
        HANG.waitKill();
        Assertions.assertTrue(ex[0] instanceof SQLException);
        Assertions.assertTrue(ex[0].getCause() instanceof IOException);
        Assertions.assertTrue(ex[0].getCause().getCause() instanceof InterruptedException);
        Assertions.assertEquals("Download was interrupted.", ex[0].getMessage());
    }
}
