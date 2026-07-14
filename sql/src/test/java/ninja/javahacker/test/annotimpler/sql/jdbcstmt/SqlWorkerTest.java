package ninja.javahacker.test.annotimpler.sql.jdbcstmt;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Proxy;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings("unused")
public class SqlWorkerTest {

    // ── DB schemas ───────────────────────────────────────────────────────────

    private static final List<String> T_SCHEMA = List.of(
            "CREATE TABLE t (id INT PRIMARY KEY, label VARCHAR(50), amount INT)"
    );

    private static final List<String> T_SEED = List.of(
            "INSERT INTO t VALUES (1, 'alpha', 10)",
            "INSERT INTO t VALUES (2, 'beta',  20)",
            "INSERT INTO t VALUES (3, 'gamma', 30)"
    );

    private static final List<String> G_SCHEMA = List.of(
            "CREATE TABLE g (id INT AUTO_INCREMENT PRIMARY KEY, label VARCHAR(50))"
    );

    private static final List<String> N_SCHEMA = List.of(
            "CREATE TABLE n (id INT PRIMARY KEY, label VARCHAR(50))"
    );

    // ── Record types ─────────────────────────────────────────────────────────

    public record Item(String label, Integer amount) {}

    // ── Infrastructure helpers ────────────────────────────────────────────────

    @FunctionalInterface
    private interface ConnectionContext {
        void doIt(Connection con) throws Exception;

        default void onConnection() throws Exception {
            try (var con = H2Connector.std().withMemory(true).withTimezone("UTC").get()) {
                this.doIt(con);
            }
        }

        default Executable wrap() {
            return this::onConnection;
        }
    }

    @SafeVarargs
    private static void setup(Connection con, List<String>... groups) throws SQLException {
        for (var group : groups) {
            for (var sql : group) {
                try (var ps = con.prepareStatement(sql)) {
                    ps.executeUpdate();
                }
            }
        }
    }

    private static SqlWorker worker(Connection con, ParameterReceiver.Acceptor2 ppq, String sql) {
        return new SqlWorker(con, ppq, ParsedQuery.parse(sql), ConverterFactory.std(), Locale.ROOT);
    }

    private static SqlWorker worker(Connection con, String sql) {
        return worker(con, pr -> {}, sql);
    }

    private static ResultSet poisonClose(ResultSet in) {
        return (ResultSet) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { ResultSet.class }, (p, m, a) -> {
            try {
                if (m.getName().equals("close")) {
                    throw new SQLException("CRASH ResultSet close");
                }
                return m.invoke(in, a);
            } catch (InvocationTargetException x) {
                throw x.getCause();
            }
        });
    }

    private static PreparedStatement poisonClose(PreparedStatement in) {
        return (PreparedStatement) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { PreparedStatement.class }, (p, m, a) -> {
            try {
                if (m.getName().equals("close")) {
                    throw new SQLException("CRASH PreparedStatement close");
                }
                return m.invoke(in, a);
            } catch (InvocationTargetException x) {
                throw x.getCause();
            }
        });
    }

    private static PreparedStatement poisonGetGeneratedKeys(PreparedStatement in) {
        return (PreparedStatement) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { PreparedStatement.class }, (p, m, a) -> {
            try {
                if (m.getName().equals("getGeneratedKeys")) {
                    var ps = m.invoke(in, a);
                    return poisonClose((ResultSet) ps);
                }
                return m.invoke(in, a);
            } catch (InvocationTargetException x) {
                throw x.getCause();
            }
        });
    }

    private static Connection poisonGetGeneratedKeys(Connection in) {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            try {
                if (m.getName().equals("prepareStatement")) {
                    var ps = m.invoke(in, a);
                    return poisonGetGeneratedKeys((PreparedStatement) ps);
                }
                return m.invoke(in, a);
            } catch (InvocationTargetException x) {
                throw x.getCause();
            }
        });
    }

    private static Connection poisonPrepare(Connection in) {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            try {
                if (m.getName().equals("prepareStatement")) {
                    var ps = m.invoke(in, a);
                    return poisonClose((PreparedStatement) ps);
                }
                return m.invoke(in, a);
            } catch (InvocationTargetException x) {
                throw x.getCause();
            }
        });
    }

    private static SqlWorker workerCrashOnClose(Connection con, ParameterReceiver.Acceptor2 ppq, String sql) {
        var con2 = poisonPrepare(con);
        return worker(con2, ppq, sql);
    }

    private static SqlWorker workerCrashOnClose(Connection con, String sql) {
        var con2 = poisonPrepare(con);
        return worker(con2, pr -> {}, sql);
    }

    private static SqlWorker workerCrashOnCloseKeys(Connection con, String sql) {
        var con2 = poisonGetGeneratedKeys(con);
        return worker(con2, pr -> {}, sql);
    }

    // ── Tests: execute() ─────────────────────────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testExecute() {
        var pf = "[testExecute] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "INSERT returns 1", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var w = worker(
                            con,
                            pr -> {
                                pr.receive("id", 99);
                                pr.receive("label", "delta");
                                pr.receive("amount", 40);
                            },
                            "INSERT INTO t VALUES (:id, :label, :amount)"
                    );
                    Assertions.assertEquals(1L, w.execute());
                }).wrap()),

                DynamicTest.dynamicTest(pf + "INSERT then SELECT confirms row exists", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    worker(
                            con,
                            pr -> {
                                pr.receive("id", 7);
                                pr.receive("label", "eta");
                                pr.receive("amount", 70);
                            },
                            "INSERT INTO t VALUES (:id, :label, :amount)"
                    ).execute();
                    var check = worker(con, pr -> pr.receive("id", 7), "SELECT label FROM t WHERE id = :id");
                    Assertions.assertEquals(Optional.of("eta"), check.read(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "UPDATE all rows returns count", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("amount", 99), "UPDATE t SET amount = :amount");
                    Assertions.assertEquals(3L, w.execute());
                }).wrap()),

                DynamicTest.dynamicTest(pf + "UPDATE with WHERE returns 1", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(
                            con,
                            pr -> {
                                pr.receive("amount", 99);
                                pr.receive("id", 2);
                            },
                            "UPDATE t SET amount = :amount WHERE id = :id"
                    );
                    Assertions.assertEquals(1L, w.execute());
                }).wrap()),

                DynamicTest.dynamicTest(pf + "DELETE matching row returns 1", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("id", 1), "DELETE FROM t WHERE id = :id");
                    Assertions.assertEquals(1L, w.execute());
                }).wrap()),

                DynamicTest.dynamicTest(pf + "DELETE no matching rows returns 0", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("id", 999), "DELETE FROM t WHERE id = :id");
                    Assertions.assertEquals(0L, w.execute());
                }).wrap())
        );
    }

    // ── Tests: read() simple types ───────────────────────────────────────────

    @TestFactory
    @SuppressWarnings({"ThrowableResultIgnored", "AssertEqualsBetweenInconvertibleTypes"})
    public Stream<DynamicTest> testReadSimple() {
        var pf = "[testReadSimple] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "read String col 1 → value", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("id", 1), "SELECT label FROM t WHERE id = :id");
                    Assertions.assertEquals(Optional.of("alpha"), w.read(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read Integer col 1 → value", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("id", 2), "SELECT amount FROM t WHERE id = :id");
                    Assertions.assertEquals(Optional.of(20), w.read(Integer.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read when no rows → Optional.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    Assertions.assertEquals(Optional.empty(), worker(con, "SELECT label FROM t").read(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read String with explicit field index 2", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("id", 3), "SELECT amount, label FROM t WHERE id = :id");
                    Assertions.assertEquals(Optional.of("gamma"), w.read(String.class, 2));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read(non-record, multiple fields) → UnsupportedOperationException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT label, amount FROM t WHERE id = 1");
                    Assertions.assertThrows(UnsupportedOperationException.class, () -> w.read(String.class, 1, 2));
                }).wrap())
        );
    }

    // ── Tests: read() record types ───────────────────────────────────────────

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testReadRecord() {
        var pf = "[testReadRecord] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "read record from two columns → correct value", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("id", 1), "SELECT label, amount FROM t WHERE id = :id");
                    Assertions.assertEquals(Optional.of(new Item("alpha", 10)), w.read(Item.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read record when no rows → Optional.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var w = worker(con, "SELECT label, amount FROM t");
                    Assertions.assertEquals(Optional.empty(), w.read(Item.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read record with reversed explicit field mapping", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    // SELECT amount AS col1, label AS col2 → Item(label←col2, amount←col1)
                    var w = worker(con, pr -> pr.receive("id", 2), "SELECT amount, label FROM t WHERE id = :id");
                    Assertions.assertEquals(Optional.of(new Item("beta", 20)), w.read(Item.class, 2, 1));
                }).wrap())
        );
    }

    // ── Tests: list() simple types ───────────────────────────────────────────

    @TestFactory
    @SuppressWarnings({"ThrowableResultIgnored", "AssertEqualsBetweenInconvertibleTypes"})
    public Stream<DynamicTest> testListSimple() {
        var pf = "[testListSimple] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "list all String values ordered", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT label FROM t ORDER BY id");
                    Assertions.assertEquals(List.of("alpha", "beta", "gamma"), w.list(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list Integer values ordered", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT amount FROM t ORDER BY id");
                    Assertions.assertEquals(List.of(10, 20, 30), w.list(Integer.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list filtered by parameter", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, pr -> pr.receive("min", 15), "SELECT label FROM t WHERE amount >= :min ORDER BY id");
                    Assertions.assertEquals(List.of("beta", "gamma"), w.list(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list when empty → empty list", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    Assertions.assertEquals(List.of(), worker(con, "SELECT label FROM t").list(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list String with explicit field index 2", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT amount, label FROM t ORDER BY id");
                    Assertions.assertEquals(List.of("alpha", "beta", "gamma"), w.list(String.class, 2));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list(non-record, multiple fields) → UnsupportedOperationException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT label, amount FROM t");
                    Assertions.assertThrows(UnsupportedOperationException.class, () -> w.list(String.class, 1, 2));
                }).wrap())
        );
    }

    // ── Tests: list() record types ───────────────────────────────────────────

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testListRecord() {
        var pf = "[testListRecord] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "list all records ordered", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT label, amount FROM t ORDER BY id");
                    Assertions.assertEquals(
                            List.of(new Item("alpha", 10), new Item("beta", 20), new Item("gamma", 30)),
                            w.list(Item.class)
                    );
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list records when empty → empty list", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    Assertions.assertEquals(List.of(), worker(con, "SELECT label, amount FROM t").list(Item.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list records with reversed explicit field mapping", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var w = worker(con, "SELECT amount, label FROM t ORDER BY id");
                    Assertions.assertEquals(
                            List.of(new Item("alpha", 10), new Item("beta", 20), new Item("gamma", 30)),
                            w.list(Item.class, 2, 1)
                    );
                }).wrap())
        );
    }

    // ── Tests: generate(), generateLong() and friends ─────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testGenerate() {
        var pf = "[testGenerate] ";
        var sql = "INSERT INTO g (label) VALUES (:label)";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "generate() first row → key = 1", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "foo"), sql).generate();
                    Assertions.assertEquals(1, key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLong() first row → key = 1", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "foo"), sql).generateLong();
                    Assertions.assertEquals(1L, key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOrNull() first row → key = 1", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    Integer key = worker(con, pr -> pr.receive("label", "foo"), sql).generateOrNull();
                    Assertions.assertEquals(Integer.valueOf(1), key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLongOrNull() first row → key = 1", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    Long key = worker(con, pr -> pr.receive("label", "foo"), sql).generateLongOrNull();
                    Assertions.assertEquals(Long.valueOf(1L), key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptional() first row → key = 1", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "foo"), sql).generateOptional();
                    Assertions.assertEquals(OptionalInt.of(1), key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptionalLong() first row → key = 1L", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "foo"), sql).generateOptionalLong();
                    Assertions.assertEquals(OptionalLong.of(1L), key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptional() second row → key = 2", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    worker(con, pr -> pr.receive("label", "foo"), sql).generateOptional();
                    var key2 = worker(con, pr -> pr.receive("label", "bar"), sql).generateOptional();
                    Assertions.assertEquals(OptionalInt.of(2), key2);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptionalLong() second row → key = 2L", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    worker(con, pr -> pr.receive("label", "foo"), sql).generateOptionalLong();
                    var key2 = worker(con, pr -> pr.receive("label", "bar"), sql).generateOptionalLong();
                    Assertions.assertEquals(OptionalLong.of(2L), key2);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptional() then verify row in DB", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "hello"), sql).generateOptional();
                    Assertions.assertTrue(key.isPresent());
                    var check = worker(con, pr -> pr.receive("id", key.getAsInt()), "SELECT label FROM g WHERE id = :id");
                    Assertions.assertEquals(Optional.of("hello"), check.read(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptionalLong() then verify row in DB", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "hello"), sql).generateOptionalLong();
                    Assertions.assertTrue(key.isPresent());
                    var check = worker(con, pr -> pr.receive("id", key.getAsLong()), "SELECT label FROM g WHERE id = :id");
                    Assertions.assertEquals(Optional.of("hello"), check.read(String.class));
                }).wrap())
        );
    }

    // ── Tests: generateList() and generateLongList() ─────────────────────────

    @TestFactory
    public Stream<DynamicTest> testGenerateList() {
        var pf = "[testGenerateList] ";
        var sql = "INSERT INTO g (label) VALUES (:label)";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "generateList() first row → [1]", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    Assertions.assertEquals(List.of(1), worker(con, pr -> pr.receive("label", "foo"), sql).generateList());
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateList() sequential inserts produce sequential keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var k1 = worker(con, pr -> pr.receive("label", "one"), sql).generateList();
                    var k2 = worker(con, pr -> pr.receive("label", "two"), sql).generateList();
                    Assertions.assertEquals(List.of(1), k1);
                    Assertions.assertEquals(List.of(2), k2);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLongList() first row → [1L]", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    Assertions.assertEquals(List.of(1L), worker(con, pr -> pr.receive("label", "foo"), sql).generateLongList());
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLongList() sequential inserts produce sequential keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var k1 = worker(con, pr -> pr.receive("label", "one"), sql).generateLongList();
                    var k2 = worker(con, pr -> pr.receive("label", "two"), sql).generateLongList();
                    Assertions.assertEquals(List.of(1L), k1);
                    Assertions.assertEquals(List.of(2L), k2);
                }).wrap())
        );
    }

    // ── Tests: generate() edge cases (multiple rows) ──────────────────────────

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testGenerateEdgeCases() {
        var pf = "[testGenerateEdgeCases] ";
        return Stream.of(
                // ──── generateOptional() with multiple rows affected ────
                DynamicTest.dynamicTest(pf + "generateOptional() multi-row → SQLException", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    // Insert multiple rows with a single statement (H2 supports it)
                    var w = worker(con, "INSERT INTO g (label) VALUES ('a'), ('b')");
                    Assertions.assertThrows(SQLException.class, w::generateOptional);
                }).wrap()),

                // ──── generateOptionalLong() with multiple rows affected ────
                DynamicTest.dynamicTest(pf + "generateOptionalLong() multi-row → SQLException", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    // Insert multiple rows with a single statement (H2 supports it)
                    var w = worker(con, "INSERT INTO g (label) VALUES ('a'), ('b')");
                    Assertions.assertThrows(SQLException.class, w::generateOptionalLong);
                }).wrap()),

                // ──── generate() with multiple rows affected ────
                DynamicTest.dynamicTest(pf + "generate() multi-row → SQLException", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    // Insert multiple rows with a single statement
                    var w = worker(con, "INSERT INTO g (label) VALUES ('x'), ('y')");
                    Assertions.assertThrows(SQLException.class, w::generate);
                }).wrap()),

                // ──── generateLong() with multiple rows affected ────
                DynamicTest.dynamicTest(pf + "generateLong() multi-row → SQLException", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    // Insert multiple rows with a single statement
                    var w = worker(con, "INSERT INTO g (label) VALUES ('m'), ('n')");
                    Assertions.assertThrows(SQLException.class, w::generateLong);
                }).wrap())
        );
    }

    // ── Tests: generateList() and generateLongList() edge cases ──────────────

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testGenerateListEdgeCases() {
        var pf = "[testGenerateListEdgeCases] ";
        var sql = "INSERT INTO g (label) VALUES (:label)";
        return Stream.of(
                // ──── generateList() with no rows ────
                DynamicTest.dynamicTest(pf + "generateList() no rows → empty list", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var keys = worker(con, "DELETE FROM g").generateList();
                    Assertions.assertEquals(List.of(), keys);
                }).wrap()),

                // ──── generateList() with multiple rows ────
                DynamicTest.dynamicTest(pf + "generateList() multi-row → all keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var keys = worker(con, "INSERT INTO g (label) VALUES ('a'), ('b'), ('c')").generateList();
                    Assertions.assertEquals(List.of(1, 2, 3), keys);
                }).wrap()),

                // ──── generateLongList() with no rows ────
                DynamicTest.dynamicTest(pf + "generateLongList() no rows → empty list", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var keys = worker(con, "DELETE FROM g").generateLongList();
                    Assertions.assertEquals(List.of(), keys);
                }).wrap()),

                // ──── generateLongList() with multiple rows ────
                DynamicTest.dynamicTest(pf + "generateLongList() multi-row → all keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var keys = worker(con, "INSERT INTO g (label) VALUES ('x'), ('y'), ('z')").generateLongList();
                    Assertions.assertEquals(List.of(1L, 2L, 3L), keys);
                }).wrap()),

                // ──── Sequential generateList() calls ────
                DynamicTest.dynamicTest(pf + "generateList() multi-row sequential inserts", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var k1 = worker(con, "INSERT INTO g (label) VALUES ('a'), ('b')").generateList();
                    var k2 = worker(con, pr -> pr.receive("label", "c"), sql).generateList();
                    Assertions.assertEquals(List.of(1, 2), k1);
                    Assertions.assertEquals(List.of(3), k2);
                }).wrap()),

                // ──── Sequential generateLongList() calls ────
                DynamicTest.dynamicTest(pf + "generateLongList() multi-row sequential inserts", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var k1 = worker(con, "INSERT INTO g (label) VALUES ('a'), ('b')").generateLongList();
                    var k2 = worker(con, pr -> pr.receive("label", "c"), sql).generateLongList();
                    Assertions.assertEquals(List.of(1L, 2L), k1);
                    Assertions.assertEquals(List.of(3L), k2);
                }).wrap())
        );
    }

    // ── Tests: generate*() returning empty/null when no keys generated ───────

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testGenerateNoKeysCovered() {
        var pf = "[testGenerateNoKeysCovered] ";
        return Stream.of(
                // ──── generateOptional() with no keys generated ────
                DynamicTest.dynamicTest(pf + "generateOptional() no keys → OptionalInt.empty()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    // INSERT that affects no rows → rs.next() returns false
                    var key = worker(con, "INSERT INTO g (label) SELECT 'test' WHERE 1 = 0").generateOptional();
                    Assertions.assertEquals(OptionalInt.empty(), key);
                }).wrap()),

                // ──── generateOrNull() with no keys generated ────
                DynamicTest.dynamicTest(pf + "generateOrNull() no keys → null", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, "INSERT INTO g (label) SELECT 'test' WHERE 1 = 0").generateOrNull();
                    Assertions.assertNull(key);
                }).wrap()),

                // ──── generate() with no keys generated ────
                DynamicTest.dynamicTest(pf + "generate() no keys → SQLException", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var w = worker(con, "INSERT INTO g (label) SELECT 'test' WHERE 1 = 0");
                    Assertions.assertThrows(SQLException.class, w::generate);
                }).wrap()),

                // ──── generateOptionalLong() with no keys generated ────
                DynamicTest.dynamicTest(pf + "generateOptionalLong() no keys → OptionalLong.empty()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, "INSERT INTO g (label) SELECT 'test' WHERE 1 = 0").generateOptionalLong();
                    Assertions.assertEquals(OptionalLong.empty(), key);
                }).wrap()),

                // ──── generateLongOrNull() with no keys generated ────
                DynamicTest.dynamicTest(pf + "generateLongOrNull() no keys → null", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, "INSERT INTO g (label) SELECT 'test' WHERE 1 = 0").generateLongOrNull();
                    Assertions.assertNull(key);
                }).wrap()),

                // ──── generateLong() with no keys generated ────
                DynamicTest.dynamicTest(pf + "generateLong() no keys → SQLException", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var w = worker(con, "INSERT INTO g (label) SELECT 'test' WHERE 1 = 0");
                    Assertions.assertThrows(SQLException.class, w::generateLong);
                }).wrap())
        );
    }

    // ── Tests: @NonNull violations ────────────────────────────────────────────

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        var pf = "[testNulls] ";
        var pq = ParsedQuery.parse("SELECT 1");
        ParameterReceiver.Acceptor2 noop = pr -> {};
        return Stream.of(
                DynamicTest.dynamicTest(pf + "con null → @NonNull", () ->
                        ForTests.testNull("con", () -> new SqlWorker(null, noop, pq, ConverterFactory.std(), Locale.ROOT))
                ),

                DynamicTest.dynamicTest(pf + "ppq null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("ppq", () -> new SqlWorker(con, null, pq, ConverterFactory.std(), Locale.ROOT))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "pq null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("pq", () -> new SqlWorker(con, noop, null, ConverterFactory.std(), Locale.ROOT))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "factory null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("factory", () -> new SqlWorker(con, noop, pq, null, Locale.ROOT))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "localizer null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("localizer", () -> new SqlWorker(con, noop, pq, ConverterFactory.std(), null))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "read(null class) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("k", () -> worker(con, "SELECT label FROM t").read(null));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read(null class, fields) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("k", () -> worker(con, "SELECT label, amount FROM t").read(null, 1, 2));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "read(class, null) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("fields", () -> worker(con, "SELECT label, amount FROM t").read(Item.class, null));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list(null class) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("k", () -> worker(con, "SELECT label FROM t").list(null));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list(null class, fields) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("k", () -> worker(con, "SELECT label, amount FROM t").list(null, 1, 2));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list(class, null) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("fields", () -> worker(con, "SELECT label, amount FROM t").list(Item.class, null));
                }).wrap())
        );
    }

    // ── Tests: SQLException from database errors ────────────────────────────

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testSqlExceptions() {
        var pf = "[testSqlExceptions] ";
        return Stream.of(
                // ──── read() with invalid SQL (nonexistent column) ────
                DynamicTest.dynamicTest(pf + "read() invalid column → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var w = worker(con, "SELECT nonexistent_column FROM t");
                    Assertions.assertThrows(SQLException.class, () -> w.read(String.class));
                }).wrap()),

                // ──── read() with invalid SQL (nonexistent table) ────
                DynamicTest.dynamicTest(pf + "read() nonexistent table → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "SELECT * FROM nonexistent_table");
                    Assertions.assertThrows(SQLException.class, () -> w.read(String.class));
                }).wrap()),

                // ──── read() with SQL syntax error ────
                DynamicTest.dynamicTest(pf + "read() syntax error → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "SELECT * FORM t");  // FORM instead of FROM
                    Assertions.assertThrows(SQLException.class, () -> w.read(String.class));
                }).wrap()),

                // ──── read() record with invalid column ────
                DynamicTest.dynamicTest(pf + "read() record invalid columns → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var w = worker(con, "SELECT nonexistent1, nonexistent2 FROM t");
                    Assertions.assertThrows(SQLException.class, () -> w.read(Item.class));
                }).wrap()),

                // ──── list() with invalid SQL (nonexistent column) ────
                DynamicTest.dynamicTest(pf + "list() invalid column → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var w = worker(con, "SELECT nonexistent_column FROM t");
                    Assertions.assertThrows(SQLException.class, () -> w.list(String.class));
                }).wrap()),

                // ──── list() with invalid SQL (nonexistent table) ────
                DynamicTest.dynamicTest(pf + "list() nonexistent table → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "SELECT * FROM nonexistent_table");
                    Assertions.assertThrows(SQLException.class, () -> w.list(String.class));
                }).wrap()),

                // ──── list() with SQL syntax error ────
                DynamicTest.dynamicTest(pf + "list() syntax error → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "SELECT * FORM t");  // FORM instead of FROM
                    Assertions.assertThrows(SQLException.class, () -> w.list(String.class));
                }).wrap()),

                // ──── list() record with invalid column ────
                DynamicTest.dynamicTest(pf + "list() record invalid columns → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var w = worker(con, "SELECT col1, col2 FROM t");  // Invalid columns
                    Assertions.assertThrows(SQLException.class, () -> w.list(Item.class));
                }).wrap()),

                // ──── execute() with invalid SQL (nonexistent table) ────
                DynamicTest.dynamicTest(pf + "execute() nonexistent table → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "UPDATE nonexistent_table SET id = 1");
                    Assertions.assertThrows(SQLException.class, w::execute);
                }).wrap()),

                // ──── execute() with SQL syntax error ────
                DynamicTest.dynamicTest(pf + "execute() syntax error → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "UPDAT t SET id = 1");  // UPDAT instead of UPDATE
                    Assertions.assertThrows(SQLException.class, w::execute);
                }).wrap()),

                // ──── generateOptional() with invalid SQL ────
                DynamicTest.dynamicTest(pf + "generateOptional() syntax error → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "INSRT INTO t (id, label) VALUES (1, 'test')");  // INSRT instead of INSERT
                    Assertions.assertThrows(SQLException.class, w::generateOptional);
                }).wrap()),

                // ──── generateList() with invalid SQL ────
                DynamicTest.dynamicTest(pf + "generateList() nonexistent table → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "INSERT INTO nonexistent_table (id) VALUES (1)");
                    Assertions.assertThrows(SQLException.class, w::generateList);
                }).wrap()),

                // ──── generateOptionalLong() with invalid SQL ────
                DynamicTest.dynamicTest(pf + "generateOptionalLong() syntax error → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "INSERT INOT g (label) VALUES ('test')");  // INOT instead of INTO
                    Assertions.assertThrows(SQLException.class, w::generateOptionalLong);
                }).wrap()),

                // ──── generateLongList() with invalid SQL ────
                DynamicTest.dynamicTest(pf + "generateLongList() nonexistent table → SQLException", ((ConnectionContext) con -> {
                    var w = worker(con, "INSERT INTO nonexistent_table (id) VALUES (1)");
                    Assertions.assertThrows(SQLException.class, w::generateLongList);
                }).wrap())
        );
    }

    // ── Tests: SQLException from database errors ────────────────────────────

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testSqlExceptionsOnClose() {
        var pf = "[testSqlExceptionsOnClose] ";
        var sqlGen = "INSERT INTO g (label) VALUES (:label)";
        var sqlRec = "SELECT label, amount FROM t WHERE id = :id";
        var sqlSim = "SELECT label FROM t WHERE id = :id";

        return Stream.of(
                DynamicTest.dynamicTest(pf + "generate() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generate, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generate() crash on close() for keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnCloseKeys(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generate, "CRASH ResultSet close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLong() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateLong, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLong() crash on close() for keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnCloseKeys(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateLong, "CRASH ResultSet close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOrNull() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateOrNull, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOrNull() crash on close() for keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnCloseKeys(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateOrNull, "CRASH ResultSet close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLongOrNull() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, () -> work.generateLongOrNull(), "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLongOrNull() crash on close() for keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnCloseKeys(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, () -> work.generateLongOrNull(), "CRASH ResultSet close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptional() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateOptional, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptional() crash on close() for keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnCloseKeys(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateOptional, "CRASH ResultSet close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptionalLong() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateOptionalLong, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateOptionalLong() crash on close() for keys", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnCloseKeys(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateOptionalLong, "CRASH ResultSet close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateList() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateList, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLongList() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::generateLongList, "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "readSimple() crash on close()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var work = workerCrashOnClose(con, pr -> pr.receive("id", 1), sqlSim);
                    Assertions.assertThrows(SQLException.class, () -> work.read(String.class), "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "readRecord() crash on close()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var work = workerCrashOnClose(con, pr -> pr.receive("id", 1), sqlRec);
                    Assertions.assertThrows(SQLException.class, () -> work.read(Item.class), "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "listSimple() crash on close()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var work = workerCrashOnClose(con, pr -> pr.receive("id", 1), sqlSim);
                    Assertions.assertThrows(SQLException.class, () -> work.list(String.class), "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "listRecord() crash on close()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var work = workerCrashOnClose(con, pr -> pr.receive("id", 1), sqlRec);
                    Assertions.assertThrows(SQLException.class, () -> work.list(Item.class), "CRASH PreparedStatement close");
                }).wrap()),

                DynamicTest.dynamicTest(pf + "execute() crash on close()", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var work = workerCrashOnClose(con, sqlGen);
                    Assertions.assertThrows(SQLException.class, work::execute, "CRASH PreparedStatement close");
                }).wrap())
        );
    }
}
