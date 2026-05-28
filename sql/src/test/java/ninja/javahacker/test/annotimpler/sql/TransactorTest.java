package ninja.javahacker.test.annotimpler.sql;

import org.junit.jupiter.api.function.ThrowingSupplier;
import lombok.experimental.Delegate;

import ninja.javahacker.test.ControlledMock;

import ninja.javahacker.test.ForTests;
import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class TransactorTest {

    private static final Supplier<String> BAD_GEN = () -> {
        throw new AssertionError();
    };

    private static final ConnectionFactory BAD_FACTORY = () -> {
        throw new AssertionError();
    };

    public TransactorTest() {
    }

    public static interface Foo1 {
        public String xxx(int a) throws Exception;
        public int yyy(String x, String y) throws Exception;
    }

    @FunctionalInterface
    public static interface Foo2 {
        public String xxx(int a) throws Exception;
    }

    @FunctionalInterface
    public static interface Foo3 {
        public int yyy(String x, String y) throws Exception;
    }

    public final class Foo4 implements Foo1 {
        @Delegate(types = Foo2.class)
        private final Foo2 f2;

        @Delegate(types = Foo3.class)
        private final Foo3 f3;

        public Foo4(Foo2 f2, Foo3 f3) {
            this.f2 = f2;
            this.f3 = f3;
        }
    }

    private static class TransactionControl {
        private final boolean shouldCommit;
        private final Connection con;
        private int state;

        public TransactionControl(boolean shouldCommit) {
            this.shouldCommit = shouldCommit;
            this.state = 0;
            var md = ControlledMock.mock(Connection.class);
            md.setHandler((i, m, a) -> {
                var n = m.getName();
                if (n.equals("commit")) {
                    commit();
                    return null;
                }
                if (n.equals("rollback")) {
                    rollback();
                    return null;
                }
                if (n.equals("close")) {
                    close();
                    return null;
                }
                throw new AssertionError(m);
            });
            this.con = md.getMock();
        }

        public Connection connect() {
            if (state != 0) throw new AssertionError();
            state = 1;
            return con;
        }

        public void commit() {
            if (!shouldCommit) throw new AssertionError();
            if (state != 1) throw new AssertionError();
            state = 2;
        }

        public void rollback() {
            if (shouldCommit) throw new AssertionError();
            if (state != 1) throw new AssertionError();
            state = 2;
        }

        public void close() {
            if (state != 2) throw new AssertionError();
            state = 3;
        }

        public boolean finishing() {
            return state == 2;
        }

        public boolean finished() {
            return state == 3;
        }

        public void assertConnected() {
            Assertions.assertTrue(state >= 1);
        }

        public void assertSameConnection(Connection other) {
            Assertions.assertSame(con, other);
        }
    }

    @Test
    public void testTransactOperationWithCommit() throws Exception {
        var tc = new TransactionControl(true);
        var idx = new int[1];

        Supplier<String> gen = () -> {
            idx[0]++;
            return "" + idx[0];
        };

        var t = new Transactor(tc::connect, gen);

        Foo2 f2 = a -> {
            Assertions.assertEquals(42, a);
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx = t.currentTransaction();
            tc.assertSameConnection(tx.connection());
            Assertions.assertEquals("1", tx.id());
            return "foo";
        };

        Foo3 f3 = (a, b) -> {
            throw new AssertionError();
        };

        var foo = new Foo4(f2, f3);
        Foo1 transOper = t.transact(foo);
        var out = transOper.xxx(42);
        Assertions.assertEquals("foo", out);
        Assertions.assertTrue(tc.finished());
    }

    @Test
    public void testTransactOperationWithRollback() {
        var tc = new TransactionControl(false);
        var idx = new int[1];

        Supplier<String> gen = () -> {
            idx[0]++;
            return "" + idx[0];
        };

        var t = new Transactor(tc::connect, gen);

        Foo2 f2 = a -> {
            Assertions.assertEquals(42, a);
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx = t.currentTransaction();
            tc.assertSameConnection(tx.connection());
            Assertions.assertEquals("1", tx.id());
            throw new IllegalArgumentException("blabla");
        };

        Foo3 f3 = (a, b) -> {
            throw new AssertionError();
        };

        var foo = new Foo4(f2, f3);
        Foo1 transOper = t.transact(foo);
        var out = Assertions.assertThrows(IllegalArgumentException.class, () -> transOper.xxx(42));
        Assertions.assertEquals("blabla", out.getMessage());
        Assertions.assertTrue(tc.finished());
    }

    @Test
    @Timeout(15)
    public void testTransactionIsolation() throws Exception {
        class SomeException extends RuntimeException {
            private static final long serialVersionUID = 1L;
        }

        class OtherException extends RuntimeException {
            private static final long serialVersionUID = 1L;
        }

        var threads = new Thread[5];
        var tcs = List.of(
                new TransactionControl(true),
                new TransactionControl(false),
                new TransactionControl(true),
                new TransactionControl(false),
                new TransactionControl(true)
        );

        Supplier<Integer> sf = () -> {
            for (var i = 0; i < 5; i++) {
                if (Thread.currentThread() == threads[i]) {
                    return i;
                }
            }
            throw new AssertionError();
        };

        ConnectionFactory f = () -> tcs.get(sf.get()).connect();

        var idx = new int[1];
        Supplier<String> gen = () -> {
            idx[0]++;
            return "" + idx[0];
        };

        var cb1 = new CyclicBarrier(6);
        var cb2 = new CyclicBarrier(6);
        var t = new Transactor(f, gen);

        Foo2 f2 = a -> {
            var n = sf.get();
            var tc = tcs.get(n);
            Assertions.assertEquals((n + 1) * 42, a);
            System.out.println("a... " + n);
            cb1.await(2, TimeUnit.SECONDS);
            System.out.println("a-ok " + n);
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx = t.currentTransaction();
            var tx2 = t.currentTransaction();
            Assertions.assertSame(tx, tx2);
            System.out.println("ooo " + n);
            var tid = tx.id();
            System.out.println("ooo2 " + n);
            var tidx = (n + 1) + "";
            System.out.println("ooo3 " + n);
            Assertions.assertEquals(tidx, tid);
            System.out.println("uuu " + n + " - " + tid);
            tc.assertSameConnection(tx.connection());
            System.out.println("b... " + n);
            cb2.await(2, TimeUnit.SECONDS);
            System.out.println("b-ok " + n);
            if (n == 1) throw new SomeException();
            if (n == 0) return "foo";
            return "bar";
        };

        Foo3 f3 = (a, b) -> {
            var n = sf.get();
            var tc = tcs.get(n);
            Assertions.assertEquals(n * 42 + "", a);
            Assertions.assertEquals(n * 31 + "", b);
            System.out.println("a... " + n);
            cb1.await(2, TimeUnit.SECONDS);
            System.out.println("b-ok " + n);
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx = t.currentTransaction();
            var tx2 = t.currentTransaction();
            Assertions.assertSame(tx, tx2);
            System.out.println("ooo " + n);
            var tid = tx.id();
            System.out.println("ooo2 " + n);
            var tidx = (n + 1) + "";
            System.out.println("ooo3 " + n);
            Assertions.assertEquals(tidx, tid);
            System.out.println("uuu " + n + " - " + tid);
            tc.assertSameConnection(tx.connection());
            System.out.println("b... " + n);
            cb2.await(2, TimeUnit.SECONDS);
            System.out.println("b-ok " + n);
            if (n == 3) throw new OtherException();
            return n * 7;
        };

        var foo = new Foo4(f2, f3);
        Foo1 transOper = t.transact(foo);
        var out = new Object[5];

        BiFunction<Integer, ThrowingSupplier<?>, Runnable> cvt = (p, run) -> {
            return () -> {
                try {
                    out[p] = run.get();
                } catch (Throwable e) {
                    out[p] = e;
                }
            };
        };

        threads[0] = new Thread(cvt.apply(0, () -> transOper.xxx(42)));
        threads[1] = new Thread(cvt.apply(1, () -> transOper.xxx(84)));
        threads[2] = new Thread(cvt.apply(2, () -> transOper.yyy("84", "62")));
        threads[3] = new Thread(cvt.apply(3, () -> transOper.yyy("126", "93")));
        threads[4] = new Thread(cvt.apply(4, () -> transOper.xxx(210)));

        for (var i = 0; i < 5; i++) {
            out[i] = new AssertionError();
            threads[i].start();
        }

        try {
            System.out.println("a... x");
            cb1.await(2, TimeUnit.SECONDS);
            System.out.println("a-ok x");
            System.out.println("b... x");
            cb2.await(6, TimeUnit.SECONDS);
            System.out.println("b-ok x");

            for (var i = 0; i < 5; i++) {
                System.out.println("Join " + i);
                threads[i].join();
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            for (var i = 0; i < 5; i++) {
                threads[i].interrupt();
            }
            throw e;
        }

        System.out.println("Grand finale");
        Assertions.assertEquals("foo", out[0]);
        Assertions.assertTrue(out[1] instanceof SomeException);
        Assertions.assertEquals(14, (int) out[2]);
        Assertions.assertTrue(out[3] instanceof OtherException);
        Assertions.assertEquals("bar", out[4]);
    }

    @Test
    public void testNoTransactionDataOutsideTransaction() {
        var t = new Transactor(BAD_FACTORY, BAD_GEN);

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalStateException.class, () -> t.connection(), "No active transaction."),
                () -> Assertions.assertThrows(IllegalStateException.class, () -> t.currentTransaction(), "No active transaction.")
        );
    }

    public static interface Ugly {
        public Object clone() throws CloneNotSupportedException;

        @Deprecated
        @SuppressWarnings({"removal", "FinalizeNotProtected", "FinalizeDeclaration"})
        public void finalize();
    }

    @TestFactory
    @SuppressWarnings("FinalizeCalledExplicitly")
    public Stream<DynamicTest> testNoTransactionForToStringEqualsHashCodeFinalizeClone() {
        var t = new Transactor(BAD_FACTORY, BAD_GEN);

        var called = new int[1];
        Runnable check = () -> {
            Assertions.assertAll(
                    () -> Assertions.assertThrows(IllegalStateException.class, () -> t.connection(), "No active transaction."),
                    () -> Assertions.assertThrows(IllegalStateException.class, () -> t.currentTransaction(), "No active transaction.")
            );
            called[0]++;
        };

        var nt = new Ugly() {
            @Override
            public String toString() {
                check.run();
                return "ok";
            }

            @Override
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            public boolean equals(Object other) {
                check.run();
                Assertions.assertEquals("foo", other);
                return false;
            }

            @Override
            public int hashCode() {
                check.run();
                return 42;
            }

            //@Override
            @Deprecated
            @SuppressWarnings({"FinalizeDeclaration", "override", "removal", "FinalizeDoesntCallSuperFinalize"})
            public void finalize() {
                check.run();
            }

            @Override
            @SuppressWarnings("CloneDoesntCallSuperClone")
            public Object clone() throws CloneNotSupportedException {
                check.run();
                return "bar";
            }
        };

        var nn = "[testNoTransactionForToStringEqualsHashCodeFinalizeClone] ";
        Ugly nt2 = t.transact(nt);
        return Stream.of(
                DynamicTest.dynamicTest(nn + "toString()", () -> Assertions.assertEquals("ok", nt2.toString())),
                DynamicTest.dynamicTest(nn + "equals(Object)", () -> Assertions.assertFalse(nt2.equals("foo"))),
                DynamicTest.dynamicTest(nn + "hashCode()", () -> Assertions.assertEquals(42, nt2.hashCode())),
                DynamicTest.dynamicTest(nn + "finalize()", () -> Assertions.assertDoesNotThrow(() -> nt2.finalize())),
                DynamicTest.dynamicTest(nn + "clone()", () -> Assertions.assertEquals("bar", nt2.clone()))
        );
    }

    @Test
    public void testTransactionIsReentrant() throws Exception {
        var tc = new TransactionControl(true);
        var idx = new int[1];

        Supplier<String> gen = () -> {
            idx[0]++;
            return "" + idx[0];
        };

        var t = new Transactor(tc::connect, gen);
        var foos = new Foo1[1];

        Foo2 f2 = a -> {
            Assertions.assertEquals(42, a);
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx = t.currentTransaction();
            tc.assertSameConnection(tx.connection());
            Assertions.assertEquals("1", tx.id());
            var z = foos[0].yyy("blue", "red");
            Assertions.assertFalse(tc.finishing());
            Assertions.assertFalse(tc.finished());
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx2 = t.currentTransaction();
            tc.assertSameConnection(tx2.connection());
            return "foo" + z;
        };

        Foo3 f3 = (a, b) -> {
            Assertions.assertEquals("blue", a);
            Assertions.assertEquals("red", b);
            tc.assertConnected();
            tc.assertSameConnection(t.connection());
            var tx = t.currentTransaction();
            tc.assertSameConnection(tx.connection());
            Assertions.assertEquals("1", tx.id());
            return 37;
        };

        var foo = new Foo4(f2, f3);
        foos[0] = t.transact(foo);
        var out = foos[0].xxx(42);
        Assertions.assertEquals("foo37", out);
        Assertions.assertTrue(tc.finished());
    }

    @Test
    public void testDoubleTransactIsIllegal() {
        Runnable x1 = () -> {
            throw new AssertionError();
        };
        var t = new Transactor(BAD_FACTORY, BAD_GEN);
        var x2 = t.transact(x1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> t.transact(x2), "Can't doubly transact an object.");
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                DynamicTest.dynamicTest("[testNulls] ctor(1)" , () -> ForTests.testNull("factory", () -> new Transactor(null, BAD_GEN))),
                DynamicTest.dynamicTest("[testNulls] ctor(2)" , () -> ForTests.testNull("generateIds", () -> new Transactor(BAD_FACTORY, null))),
                DynamicTest.dynamicTest("[testNulls] transact", () -> ForTests.testNull("impl", () -> new Transactor(BAD_FACTORY, BAD_GEN).transact(null)))
        );
    }
}
