package ninja.javahacker.test.annotimpler.sql;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import ninja.javahacker.test.ControlledMock;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings("missing-explicit-ctor")
public class TransactorTest {

    public static interface Foo1 {
        public String xxx(int a);
        public int yyy(String x, String y);
    }

    @FunctionalInterface
    public static interface Foo2 {
        public String xxx(int a);
    }

    @FunctionalInterface
    public static interface Foo3 {
        public int yyy(String x, String y);
    }

    @RequiredArgsConstructor
    public final class Foo4 implements Foo1 {
        @Delegate(types = Foo2.class)
        private final Foo2 f2;
        @Delegate(types = Foo3.class)
        private final Foo3 f3;
    }

    private ControlledMock<Connection> connect(Runnable commit, Runnable rollback, Runnable close) {
        var md = ControlledMock.mock(Connection.class);
        md.setHandler((i, m, a) -> {
            var n = m.getName();
            if (n.equals("commit")) {
                commit.run();
                return null;
            }
            if (n.equals("rollback")) {
                rollback.run();
                return null;
            }
            if (n.equals("close")) {
                close.run();
                return null;
            }
            throw new AssertionError(m);
        });
        return md;
    }

    @Test
    public void testTransactOperationWithCommit() {
        var result = new int[1];
        Runnable commit = () -> {
            if (result[0] != 0) throw new AssertionError();
            result[0] = 1;
        };
        Runnable rollback = () -> {
            throw new AssertionError();
        };
        Runnable close = () -> {
            if (result[0] != 1) throw new AssertionError();
            result[0] = 2;
        };
        var cc = new Connection[1];
        ConnectionFactory f = () -> {
            if (cc[0] != null) throw new AssertionError();
            var c = connect(commit, rollback, close).getMock();
            cc[0] = c;
            return c;
        };
        var idx = new int[1];
        Supplier<String> gen = () -> {
            idx[0]++;
            return "" + idx[0];
        };
        var t = new Transactor(f, gen);
        Foo2 f2 = a -> {
            Assertions.assertEquals(42, a);
            Assertions.assertNotNull(cc[0]);
            Assertions.assertSame(cc[0], t.connection());
            var tx = t.currentTransaction();
            Assertions.assertSame(cc[0], tx.connection());
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
        Assertions.assertEquals(2, result[0]);
    }

    @Test
    public void testTransactOperationWithRollback() {
        var result = new int[1];
        Runnable commit = () -> {
            throw new AssertionError();
        };
        Runnable rollback = () -> {
            if (result[0] != 0) throw new AssertionError();
            result[0] = 1;
        };
        Runnable close = () -> {
            if (result[0] != 1) throw new AssertionError();
            result[0] = 2;
        };
        var cc = new Connection[1];
        ConnectionFactory f = () -> {
            if (cc[0] != null) throw new AssertionError();
            var c = connect(commit, rollback, close).getMock();
            cc[0] = c;
            return c;
        };
        var idx = new int[1];
        Supplier<String> gen = () -> {
            idx[0]++;
            return "" + idx[0];
        };
        var t = new Transactor(f, gen);
        Foo2 f2 = a -> {
            Assertions.assertEquals(42, a);
            Assertions.assertNotNull(cc[0]);
            Assertions.assertSame(cc[0], t.connection());
            var tx = t.currentTransaction();
            Assertions.assertSame(cc[0], tx.connection());
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
        Assertions.assertEquals(2, result[0]);
    }
}
