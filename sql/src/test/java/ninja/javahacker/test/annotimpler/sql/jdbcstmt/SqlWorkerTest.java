package ninja.javahacker.test.annotimpler.sql.jdbcstmt;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

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
        return new SqlWorker(con, ppq, ParsedQuery.parse(sql), ConverterFactory.STD, Locale.ROOT);
    }

    private static SqlWorker worker(Connection con, String sql) {
        return worker(con, pr -> {}, sql);
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

    // ── Tests: generate() and generateLong() ─────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testGenerate() {
        var pf = "[testGenerate] ";
        var sql = "INSERT INTO g (label) VALUES (:label)";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "generate() first row → key = 1", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "foo"), sql).generate();
                    Assertions.assertEquals(OptionalInt.of(1), key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generate() second row → key = 2", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    worker(con, pr -> pr.receive("label", "foo"), sql).generate();
                    var key2 = worker(con, pr -> pr.receive("label", "bar"), sql).generate();
                    Assertions.assertEquals(OptionalInt.of(2), key2);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generate() then verify row in DB", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "hello"), sql).generate();
                    Assertions.assertTrue(key.isPresent());
                    var check = worker(con, pr -> pr.receive("id", key.getAsInt()), "SELECT label FROM g WHERE id = :id");
                    Assertions.assertEquals(Optional.of("hello"), check.read(String.class));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLong() first row → key = 1L", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var key = worker(con, pr -> pr.receive("label", "foo"), sql).generateLong();
                    Assertions.assertEquals(OptionalLong.of(1L), key);
                }).wrap()),

                DynamicTest.dynamicTest(pf + "generateLong() second row → key = 2L", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    worker(con, pr -> pr.receive("label", "foo"), sql).generateLong();
                    var key2 = worker(con, pr -> pr.receive("label", "bar"), sql).generateLong();
                    Assertions.assertEquals(OptionalLong.of(2L), key2);
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

    // ── Tests: @NonNull violations ────────────────────────────────────────────

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        var pf = "[testNulls] ";
        var pq  = ParsedQuery.parse("SELECT 1");
        ParameterReceiver.Acceptor2 noop = pr -> {};
        return Stream.of(
                DynamicTest.dynamicTest(pf + "con null → @NonNull", () ->
                        ForTests.testNull("con", () -> new SqlWorker(null, noop, pq, ConverterFactory.STD, Locale.ROOT))
                ),

                DynamicTest.dynamicTest(pf + "ppq null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("ppq", () -> new SqlWorker(con, null, pq, ConverterFactory.STD, Locale.ROOT))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "pq null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("pq", () -> new SqlWorker(con, noop, null, ConverterFactory.STD, Locale.ROOT))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "factory null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("factory", () -> new SqlWorker(con, noop, pq, null, Locale.ROOT))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "localizer null → @NonNull", ((ConnectionContext) con ->
                        ForTests.testNull("localizer", () -> new SqlWorker(con, noop, pq, ConverterFactory.STD, null))
                ).wrap()),

                DynamicTest.dynamicTest(pf + "read(null class) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("k", () -> worker(con, "SELECT label FROM t").read(null));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "list(null class) → @NonNull", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    ForTests.testNull("k", () -> worker(con, "SELECT label FROM t").list(null));
                }).wrap())
        );
    }
}
