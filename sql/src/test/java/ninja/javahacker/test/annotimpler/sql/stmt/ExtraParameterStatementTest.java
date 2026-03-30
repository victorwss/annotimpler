package ninja.javahacker.test.annotimpler.sql.stmt;

import java.lang.reflect.Proxy;
import lombok.SneakyThrows;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
public class ExtraParameterStatementTest {

    private static class LameException extends RuntimeException {
        private static final long serialVersionUID = 42L;
    }

    private static class LamerException extends Exception {
        private static final long serialVersionUID = 42L;
    }

    private static class LamestError extends Error {
        private static final long serialVersionUID = 42L;
    }

    private static final Method rowIdMethod, urlMethod, refMethod, unicodeMethod, intMethod, executeMethod, closeMethod;

    static {
        try {
            rowIdMethod = PreparedStatement.class.getMethod("setRowId", int.class, RowId.class);
            urlMethod = PreparedStatement.class.getMethod("setURL", int.class, URL.class);
            refMethod = PreparedStatement.class.getMethod("setRef", int.class, Ref.class);
            unicodeMethod = PreparedStatement.class.getMethod("setUnicodeStream", int.class, InputStream.class, int.class);
            intMethod = PreparedStatement.class.getMethod("setInt", int.class, int.class);
            executeMethod = PreparedStatement.class.getMethod("executeUpdate");
            closeMethod = PreparedStatement.class.getMethod("close");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static record MyRowId(String x) implements RowId {

        @Override
        public byte[] getBytes() {
            return x.getBytes();
        }
    }

    private static record MyRef(int x) implements Ref {

        @Override
        public String getBaseTypeName() throws SQLException {
            throw new AssertionError();
        }

        @Override
        public Object getObject(Map<String, Class<?>> map) throws SQLException {
            throw new AssertionError();
        }

        @Override
        public Object getObject() throws SQLException {
            throw new AssertionError();
        }

        @Override
        public void setObject(Object value) throws SQLException {
            throw new AssertionError();
        }
    }

    private static Connection mockCon(PreparedStatement ps) throws NoSuchMethodException {
        Method prepareMethod = Connection.class.getMethod("prepareStatement", String.class);
        var mock = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {Connection.class}, (i, m, a) -> {
            if (!m.equals(prepareMethod)) throw new AssertionError(m);
            var sql = (String) a[0];
            Assertions.assertEquals("MOCK SQL", sql);
            return ps;
        });

        return (Connection) mock;
    }

    private static interface Strategy {
        public Object run(MockPreparedStatementState m, Object[] a) throws Throwable;
    }

    private static class MockPreparedStatementState {
        private boolean ok1;
        private boolean ok2;
        private boolean ok3;
        private boolean okExecute;
        private boolean okClose;
        private boolean expectException;
        private final PreparedStatement ps;

        public MockPreparedStatementState(Map<Method, Strategy> actions) throws NoSuchMethodException {
            var mock = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {PreparedStatement.class}, (i, m, a) -> {
                if (a == null) a = new Object[0];
                var action = actions.get(m);
                if (action != null) return action.run(this, a);
                if (m.equals(intMethod)) {
                    if (a.length != 2) throw new AssertionError();
                    if (okExecute || okClose) throw new AssertionError();
                    var idx = (int) a[0];
                    var num = (int) a[1];
                    if (idx == 2) {
                        if (ok2) throw new AssertionError();
                        Assertions.assertEquals(84, num);
                        ok2 = true;
                    } else {
                        throw new AssertionError();
                    }
                    return null;
                }
                if (m.equals(executeMethod)) {
                    if (a.length != 0) throw new AssertionError();
                    if (!ok1 || !ok2 || !ok3 || okExecute || okClose) throw new AssertionError();
                    okExecute = true;
                    return 1;
                }
                if (m.equals(closeMethod)) {
                    if (a.length != 0) throw new AssertionError();
                    if (!expectException && (!ok1 || !ok2 || !ok3 || !okExecute || okClose)) throw new AssertionError();
                    okClose = true;
                    return null;
                }
                throw new AssertionError();
            });
            this.ps = (PreparedStatement) mock;
        }

        public boolean success() {
            return ok1 && ok2 && ok3 && okExecute && okClose;
        }
    }

    private static InputStream ix(int t) {
        return new InputStream() {
            @Override
            @SneakyThrows
            public int read() throws IOException {
                if (t == 1) throw new IOException("wuf");
                if (t == 2) throw new LameException();
                if (t == 3) throw new LamerException();
                if (t == 4) throw new LamestError();
                throw new AssertionError();
            }
        };
    }

    @ValueSource(strings = {"ok", "null"})
    @ParameterizedTest(name = "testRowId {0}")
    public void testRowId(String tx) throws Exception {
        var nully = tx.equals("null");
        var map = Map.of("lol", List.of(1, 3), "wul", List.of(2));
        var state = new MockPreparedStatementState(Map.of(rowIdMethod, (x, a) -> {
            if (a.length != 2) throw new AssertionError();
            if (x.okExecute || x.okClose) throw new AssertionError();
            var idx = (int) a[0];
            var rowId = (RowId) a[1];
            if (nully) {
                Assertions.assertNull(rowId);
            } else {
                Assertions.assertEquals("XYZ", new String(rowId.getBytes()));
            }
            if (idx == 1) {
                if (x.ok1) throw new AssertionError();
                x.ok1 = true;
            } else if (idx == 3) {
                if (x.ok3) throw new AssertionError();
                x.ok3 = true;
            } else {
                throw new AssertionError();
            }
            return null;
        }));
        var con = mockCon(state.ps);
        try (var ps = NamedParameterStatement.wrap(con.prepareStatement("MOCK SQL"), map)) {
            ps.setRowId("lol", nully ? null : new MyRowId("XYZ"));
            ps.setInt("wul", 84);
            Assertions.assertEquals(1, ps.executeUpdate());
        }
        Assertions.assertTrue(state.success());
    }

    @ValueSource(strings = {"ok", "null"})
    @ParameterizedTest(name = "testUrl {0}")
    public void testUrl(String tx) throws Exception {
        var nully = tx.equals("null");
        var map = Map.of("lol", List.of(1, 3), "wul", List.of(2));
        var state = new MockPreparedStatementState(Map.of(urlMethod, (x, a) -> {
            if (a.length != 2) throw new AssertionError();
            if (x.okExecute || x.okClose) throw new AssertionError();
            var idx = (int) a[0];
            var url = (URL) a[1];
            if (nully) {
                Assertions.assertNull(url);
            } else {
                Assertions.assertEquals("http://test.com", url.toURI().toString());
            }
            if (idx == 1) {
                if (x.ok1) throw new AssertionError();
                x.ok1 = true;
            } else if (idx == 3) {
                if (x.ok3) throw new AssertionError();
                x.ok3 = true;
            } else {
                throw new AssertionError();
            }
            return null;
        }));
        var con = mockCon(state.ps);
        try (var ps = NamedParameterStatement.wrap(con.prepareStatement("MOCK SQL"), map)) {
            ps.setURL("lol", nully ? null : new URI("http://test.com").toURL());
            ps.setInt("wul", 84);
            Assertions.assertEquals(1, ps.executeUpdate());
        }
        Assertions.assertTrue(state.success());
    }

    @ValueSource(strings = {"ok", "null"})
    @ParameterizedTest(name = "testRef {0}")
    public void testRef(String tx) throws Exception {
        var nully = tx.equals("null");
        var map = Map.of("lol", List.of(1, 3), "wul", List.of(2));
        var state = new MockPreparedStatementState(Map.of(refMethod, (x, a) -> {
            if (a.length != 2) throw new AssertionError();
            if (x.okExecute || x.okClose) throw new AssertionError();
            var idx = (int) a[0];
            var ref = (MyRef) a[1];
            if (nully) {
                Assertions.assertNull(ref);
            } else {
                Assertions.assertEquals(53, ref.x);
            }
            if (idx == 1) {
                if (x.ok1) throw new AssertionError();
                x.ok1 = true;
            } else if (idx == 3) {
                if (x.ok3) throw new AssertionError();
                x.ok3 = true;
            } else {
                throw new AssertionError();
            }
            return null;
        }));
        var con = mockCon(state.ps);
        try (var ps = NamedParameterStatement.wrap(con.prepareStatement("MOCK SQL"), map)) {
            ps.setRef("lol", nully ? null : new MyRef(53));
            ps.setInt("wul", 84);
            Assertions.assertEquals(1, ps.executeUpdate());
        }
        Assertions.assertTrue(state.success());
    }

    @ValueSource(strings = {"single-ok", "single-null", "multi-null"})
    @ParameterizedTest(name = "testUnicodeStream {0}")
    @SuppressWarnings("deprecation")
    public void testUnicodeStream(String tx) throws Exception {
        var multi = tx.startsWith("multi");
        var nully = tx.endsWith("null");
        var map = Map.of("lol", multi ? List.of(1, 3) : List.of(1), "wul", List.of(2));
        var state = new MockPreparedStatementState(Map.of(unicodeMethod, (x, a) -> {
            if (a.length != 3) throw new AssertionError();
            if (x.okExecute || x.okClose) throw new AssertionError();
            var idx = (int) a[0];
            var is = (InputStream) a[1];
            var len = (int) a[2];
            Assertions.assertEquals(25, len);
            if (nully) {
                Assertions.assertNull(is);
            } else {
                Assertions.assertEquals("yellow", new String(is.readAllBytes()));
            }
            if (idx == 1) {
                if (x.ok1) throw new AssertionError();
                x.ok1 = true;
            } else if (idx == 3) {
                if (x.ok3) throw new AssertionError();
                x.ok3 = true;
            } else {
                throw new AssertionError();
            }
            return null;
        }));
        if (!multi) state.ok3 = true;
        var con = mockCon(state.ps);
        try (var ps = NamedParameterStatement.wrap(con.prepareStatement("MOCK SQL"), map)) {
            ps.setUnicodeStream("lol", nully ? null : new ByteArrayInputStream("yellow".getBytes()), 25);
            ps.setInt("wul", 84);
            Assertions.assertEquals(1, ps.executeUpdate());
        }
        Assertions.assertTrue(state.success());
    }

    @ValueSource(ints = {1, 2, 3, 4, 5})
    @ParameterizedTest(name = "testUnicodeStreamError {0}")
    @SuppressWarnings({"deprecation", "AssertEqualsBetweenInconvertibleTypes"})
    public void testUnicodeStreamError(int t) throws Throwable {
        var map = Map.of("lol", t == 5 ? List.of(1, 3) : List.of(1), "wul", List.of(2));
        var state = new MockPreparedStatementState(Map.of(unicodeMethod, (x, a) -> {
            if (a.length != 3) throw new AssertionError();
            if (x.ok1 || x.ok2 || x.ok3 || x.okExecute || x.okClose) throw new AssertionError();
            var idx = (int) a[0];
            var is = (InputStream) a[1];
            var len = (int) a[2];
            Assertions.assertEquals(25, len);
            Assertions.assertNotNull(is);
            if (idx != 1) throw new AssertionError();
            is.readAllBytes(); // Should throw exception here.
            throw new AssertionError();
        }));
        state.expectException = true;
        var con = mockCon(state.ps);
        try {
            try (var ps = NamedParameterStatement.wrap(con.prepareStatement("MOCK SQL"), map)) {
                ps.setUnicodeStream("lol", ix(t), 25);
                throw new AssertionError();
            }
        } catch (SQLException ex) {
            var cause = ex.getCause();
            // Leaking abstraction implementation detail.
            while (cause instanceof UndeclaredThrowableException) {
                cause = cause.getCause();
            }
            switch (t) {
                case 1 -> {
                    Assertions.assertEquals(IOException.class, cause.getClass());
                    Assertions.assertEquals("wuf", cause.getMessage());
                }
                case 2 -> {
                    Assertions.assertEquals(LameException.class, cause.getClass());
                }
                case 3 -> {
                    Assertions.assertEquals(LamerException.class, cause.getClass());
                }
                case 4 -> {
                    Assertions.assertEquals(LamestError.class, cause.getClass());
                }
                case 5 -> {
                    Assertions.assertEquals(NamedParameterStatement.INPUT_STREAM_MESSAGE, ex.getMessage());
                }
                default -> {
                    throw new AssertionError();
                }
            }
        }
        if (state.ok1 || state.ok2 || state.ok3 || state.okExecute || !state.okClose) throw new AssertionError();
    }
}
