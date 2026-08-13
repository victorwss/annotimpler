package ninja.javahacker.test.annotimpler.sql.meta;

import ninja.javahacker.test.ForTests;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class ThreadTracerSqlSupplierTest {

    @Test
    @SuppressWarnings("null")
    public void testNulls() {
        var downstream = (ThreadTracerSqlSupplier.Downstream) () -> () -> "sql";
        var tracer = new ThreadTracerSqlSupplier(downstream);

        ForTests.testNull("downstream", () -> new ThreadTracerSqlSupplier(null));
        ForTests.testNull("t", () -> tracer.has(null));
    }

    @Test
    public void testCachedResultUsesFastPath() throws SQLException {
        var calls = new AtomicInteger();
        var tracer = new ThreadTracerSqlSupplier(() -> {
            calls.incrementAndGet();
            return () -> "sql";
        });

        Assertions.assertEquals("sql", tracer.get());
        Assertions.assertEquals("sql", tracer.get());
        Assertions.assertEquals(1, calls.get());
        Assertions.assertFalse(tracer.has(Thread.currentThread()));
    }
}
