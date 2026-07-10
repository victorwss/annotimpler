package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

/// Tests that [ReadPolicy#ON_FIRST_TIME_THAT_WORKS] and [ReadPolicy#ON_FIRST_TIME_DONT_RETRY]
/// cache their results **globally** — not per-thread — so that all threads share the same
/// cached [SqlSupplier] once it has been resolved by any one thread.
@SuppressWarnings("unused")
public class ReadPolicyThreadingTest {

    public ReadPolicyThreadingTest() {
    }

    private static void assertThreadsTerminated(Thread[] threads) throws InterruptedException {
        for (var t : threads) {
            t.join(3000);
            Assertions.assertEquals(Thread.State.TERMINATED, t.getState(), "Thread did not terminate in time.");
        }
    }

    private static void assertNoErrors(Throwable[] errors) {
        for (var i = 0; i < errors.length; i++) {
            var idx = i;
            Assertions.assertNull(errors[idx], () -> "Thread " + idx + " threw: " + errors[idx]);
        }
    }

    // ----- ON_FIRST_TIME_THAT_WORKS -----

    /// Verifies that when many threads call [SqlSupplier#get()] simultaneously on a supplier
    /// prepared with [ReadPolicy#ON_FIRST_TIME_THAT_WORKS], the underlying read is performed
    /// exactly once.  All threads must receive the same cached result.
    @Test
    @Timeout(5)
    public void testOnFirstTimeThatWorksReadsGloballyOnce() throws Exception {
        var callCount = new AtomicInteger(0);
        ReadPolicy.StringExtractor<String> impl = input -> {
            callCount.incrementAndGet();
            return "SELECT " + input;
        };
        var supplier = ReadPolicy.ON_FIRST_TIME_THAT_WORKS.prepare(impl, "1");

        int numThreads = 5;
        var barrier = new CyclicBarrier(numThreads);
        var results = new String[numThreads];
        var errors = new Throwable[numThreads];
        var threads = new Thread[numThreads];
        for (var i = 0; i < numThreads; i++) {
            var idx = i;
            threads[idx] = new Thread(() -> {
                try {
                    barrier.await();
                    results[idx] = supplier.get();
                } catch (Throwable e) {
                    errors[idx] = e;
                }
            });
            threads[idx].start();
        }

        assertThreadsTerminated(threads);
        assertNoErrors(errors);
        for (var r : results) {
            Assertions.assertEquals("SELECT 1", r);
        }
        Assertions.assertEquals(1, callCount.get(), "The underlying read should have been performed exactly once, globally.");
    }

    /// Verifies that a failed read with [ReadPolicy#ON_FIRST_TIME_THAT_WORKS] is **not** cached:
    /// after thread 1 fails, thread 2 (a different thread) retries successfully and caches the
    /// result globally.  A subsequent call from a third thread must return the cached result
    /// without triggering another read.
    @Test
    @Timeout(5)
    public void testOnFirstTimeThatWorksFailureDoesNotCache() throws Exception {
        var callCount = new AtomicInteger(0);
        var shouldFail = new AtomicBoolean(true);
        ReadPolicy.StringExtractor<String> impl = input -> {
            callCount.incrementAndGet();
            if (shouldFail.get()) throw new IOException("Simulated failure");
            return "SELECT " + input;
        };
        var supplier = ReadPolicy.ON_FIRST_TIME_THAT_WORKS.prepare(impl, "1");

        // Thread 1: fails — result must NOT be cached.
        var err1 = new AtomicReference<Throwable>();
        var t1 = new Thread(() -> {
            try {
                supplier.get();
            } catch (Throwable e) {
                err1.set(e);
            }
        });
        t1.start();
        t1.join(2000);
        Assertions.assertEquals(Thread.State.TERMINATED, t1.getState());
        Assertions.assertInstanceOf(
                SQLException.class,
                err1.get(),
                "Thread 1 should have received a SQLException for the simulated failure."
        );
        Assertions.assertEquals(1, callCount.get());

        // Thread 2 (different thread): succeeds — result IS cached globally.
        shouldFail.set(false);
        var r2 = new AtomicReference<String>();
        var err2 = new AtomicReference<Throwable>();
        var t2 = new Thread(() -> {
            try {
                r2.set(supplier.get());
            } catch (Throwable e) {
                err2.set(e);
            }
        });
        t2.start();
        t2.join(2000);
        Assertions.assertEquals(Thread.State.TERMINATED, t2.getState());
        Assertions.assertNull(err2.get());
        Assertions.assertEquals("SELECT 1", r2.get());
        Assertions.assertEquals(2, callCount.get(), "The read should have been retried exactly once after the first failure.");

        // Thread 3: must get the globally cached result without any new read.
        var r3 = new AtomicReference<String>();
        var err3 = new AtomicReference<Throwable>();
        var t3 = new Thread(() -> {
            try {
                r3.set(supplier.get());
            } catch (Throwable e) {
                err3.set(e);
            }
        });
        t3.start();
        t3.join(2000);
        Assertions.assertEquals(Thread.State.TERMINATED, t3.getState());
        Assertions.assertNull(err3.get());
        Assertions.assertEquals("SELECT 1", r3.get());
        Assertions.assertEquals(2, callCount.get(), "No additional read should have occurred; the cached result must be served globally.");
    }

    // ----- ON_FIRST_TIME_DONT_RETRY -----

    /// Verifies that when many threads call [SqlSupplier#get()] simultaneously on a supplier
    /// prepared with [ReadPolicy#ON_FIRST_TIME_DONT_RETRY], the underlying read is performed
    /// exactly once.  All threads must receive the same cached result.
    @Test
    @Timeout(5)
    public void testOnFirstTimeDontRetryReadsGloballyOnce() throws Exception {
        var callCount = new AtomicInteger(0);
        ReadPolicy.StringExtractor<String> impl = input -> {
            callCount.incrementAndGet();
            return "SELECT " + input;
        };
        var supplier = ReadPolicy.ON_FIRST_TIME_DONT_RETRY.prepare(impl, "2");

        int numThreads = 5;
        var barrier = new CyclicBarrier(numThreads);
        var results = new String[numThreads];
        var errors = new Throwable[numThreads];
        var threads = new Thread[numThreads];
        for (var i = 0; i < numThreads; i++) {
            var idx = i;
            threads[idx] = new Thread(() -> {
                try {
                    barrier.await();
                    results[idx] = supplier.get();
                } catch (Throwable e) {
                    errors[idx] = e;
                }
            });
            threads[idx].start();
        }

        assertThreadsTerminated(threads);
        assertNoErrors(errors);
        for (var r : results) {
            Assertions.assertEquals("SELECT 2", r);
        }
        Assertions.assertEquals(1, callCount.get(), "The underlying read should have been performed exactly once, globally.");
    }

    /// Verifies that a failed read with [ReadPolicy#ON_FIRST_TIME_DONT_RETRY] **is** cached
    /// globally: after thread 1 fails, even though the source would now succeed, thread 2
    /// (a different thread) receives the same cached failure without any retry attempt.
    @Test
    @Timeout(5)
    public void testOnFirstTimeDontRetryCachesFailureGlobally() throws Exception {
        var callCount = new AtomicInteger(0);
        var shouldFail = new AtomicBoolean(true);
        ReadPolicy.StringExtractor<String> impl = input -> {
            callCount.incrementAndGet();
            if (shouldFail.get()) throw new IOException("Simulated failure");
            return "SELECT " + input;
        };
        var supplier = ReadPolicy.ON_FIRST_TIME_DONT_RETRY.prepare(impl, "2");

        // Thread 1: fails — the failure IS cached immediately.
        var err1 = new AtomicReference<Throwable>();
        var t1 = new Thread(() -> {
            try {
                supplier.get();
            } catch (Throwable e) {
                err1.set(e);
            }
        });
        t1.start();
        t1.join(2000);
        Assertions.assertEquals(Thread.State.TERMINATED, t1.getState());
        Assertions.assertInstanceOf(
                SQLException.class,
                err1.get(),
                "Thread 1 should have received a SQLException for the simulated failure."
        );
        Assertions.assertEquals(1, callCount.get());

        // Make the source "available" — but it must not be consulted again.
        shouldFail.set(false);

        // Thread 2 (different thread): must still receive the cached failure.
        var err2 = new AtomicReference<Throwable>();
        var t2 = new Thread(() -> {
            try {
                supplier.get();
            } catch (Throwable e) {
                err2.set(e);
            }
        });
        t2.start();
        t2.join(2000);
        Assertions.assertEquals(Thread.State.TERMINATED, t2.getState());
        Assertions.assertInstanceOf(
                SQLException.class,
                err2.get(),
                "Thread 2 should have received the globally cached failure, not retried."
        );
        Assertions.assertEquals(
                1,
                callCount.get(),
                "No retry should have been attempted; the failure was cached globally after the first call."
        );
    }
}
