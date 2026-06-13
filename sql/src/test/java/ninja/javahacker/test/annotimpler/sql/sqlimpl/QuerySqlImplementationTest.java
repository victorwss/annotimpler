package ninja.javahacker.test.annotimpler.sql.sqlimpl;

import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Proxy;
import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

/// Tests for [QuerySqlImplementation], the runtime handler for DAO methods annotated
/// with [@QuerySql][QuerySql].
///
/// Each scenario creates a fresh in-memory H2 database, optionally pre-seeds it with
/// three rows, wires a [PropertyBag] containing that connection, and invokes the compiled
/// operation through `MethodContext.execute`.
///
/// The tests cover:
/// - Rejection of invalid method signatures at `prepare()` time (wildcard type parameters,
///   `fields` used with a non-record type, mismatched `fields` length).
/// - Scalar return types: `Optional<T>`, bare `T`, `OptionalInt`, `OptionalLong`, `OptionalDouble`
///   — both the row-found and no-row-found cases.
/// - Collection return types: `List<T>` for both populated and empty tables.
/// - Record mapping: columns mapped in declaration order and via explicit `fields` indices.
public class QuerySqlImplementationTest {

    // ── DB schemas & seed ─────────────────────────────────────────────────────

    private static final List<String> T_SCHEMA = List.of(
            "CREATE TABLE t (id INT PRIMARY KEY, label VARCHAR(50), amount INT)"
    );

    private static final List<String> T_SEED = List.of(
            "INSERT INTO t VALUES (1, 'alpha', 10)",
            "INSERT INTO t VALUES (2, 'beta',  20)",
            "INSERT INTO t VALUES (3, 'gamma', 30)"
    );

    // ── Record type ────────────────────────────────────────────────────────────

    /// A simple two-component record used as the mapped result type in record-mapping tests.
    public record Item(String label, Integer amount) {}

    // ── Test DAO interfaces ────────────────────────────────────────────────────

    interface QueryDao {
        @QuerySql
        @Sql("SELECT label FROM t WHERE id = :id")
        Optional<String> findLabel(int id) throws Exception;

        @QuerySql
        @Sql("SELECT label FROM t WHERE id = :id")
        String findLabelBare(int id) throws Exception;

        @QuerySql
        @Sql("SELECT label FROM t ORDER BY id")
        List<String> listLabels() throws Exception;

        @QuerySql
        @Sql("SELECT amount FROM t WHERE id = :id")
        OptionalInt findAmountAsInt(int id) throws Exception;

        @QuerySql
        @Sql("SELECT amount FROM t WHERE id = :id")
        OptionalLong findAmountAsLong(int id) throws Exception;

        @QuerySql
        @Sql("SELECT amount FROM t WHERE id = :id")
        OptionalDouble findAmountAsDouble(int id) throws Exception;

        @QuerySql
        @Sql("SELECT label, amount FROM t WHERE id = :id")
        Optional<Item> findItem(int id) throws Exception;

        @QuerySql
        @Sql("SELECT label, amount FROM t ORDER BY id")
        List<Item> listItems() throws Exception;

        @QuerySql(fields = {2, 1})
        @Sql("SELECT amount, label FROM t WHERE id = :id")
        Optional<Item> findItemReversed(int id) throws Exception;
    }

    interface BadWildcardOptionalDao {
        @QuerySql @Sql("SELECT 1") Optional<?> bad() throws Exception;
    }

    interface BadWildcardListDao {
        @QuerySql @Sql("SELECT 1") List<?> bad() throws Exception;
    }

    interface BadMultiFieldNonRecordDao {
        @QuerySql(fields = {1, 2}) @Sql("SELECT label, amount FROM t") Optional<String> bad() throws Exception;
    }

    interface BadRecordFieldMismatchDao {
        // Item has 2 record components but fields specifies 3 indices
        @QuerySql(fields = {1, 2, 3}) @Sql("SELECT label, amount, id FROM t") Optional<Item> bad() throws Exception;
    }

    // ── Infrastructure ─────────────────────────────────────────────────────────

    private static final ConnectionFactory NEVER_CONNECT = () -> {
        throw new AssertionError("Should not connect");
    };

    private static final PropertyBag NO_DB_BAG = PropertyBag.root()
            .add(ConnectionFactoryKeyProperty.INSTANCE, NEVER_CONNECT)
            .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.STD)
            .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);

    @FunctionalInterface
    private interface ConnectionContext {
        void doIt(Connection con) throws Throwable;

        default Executable wrap() {
            return () -> {
                try (var con = H2Connector.std().withMemory(true).withTimezone("UTC").get()) {
                    doIt(con);
                }
            };
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

    private static PropertyBag bagFor(Connection con) {
        return PropertyBag.root()
                .add(ConnectionFactoryKeyProperty.INSTANCE, () -> con)
                .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.STD)
                .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private static <E> E dummyProxy(Class<E> iface) {
        return iface.cast(Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{ iface },
                (p, m, a) -> {
                    throw new AssertionError("Unexpected proxy call: " + m);
                }
        ));
    }

    // ── Tests: prepare() validation ───────────────────────────────────────────

    /// Verifies that [QuerySqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares `Optional<?>` (wildcard type parameter).
    @Test
    public void testPrepareThrowsOnWildcardOptional() throws Exception {
        var impl = new QuerySqlImplementation(NO_DB_BAG);
        var m = BadWildcardOptionalDao.class.getMethod("bad");
        Assertions.assertThrows(BadImplementationException.class,
                () -> impl.prepare(BadWildcardOptionalDao.class, m, NO_DB_BAG)
        );
    }

    /// Verifies that [QuerySqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares `List<?>` (wildcard type parameter).
    @Test
    public void testPrepareThrowsOnWildcardList() throws Exception {
        var impl = new QuerySqlImplementation(NO_DB_BAG);
        var m = BadWildcardListDao.class.getMethod("bad");
        Assertions.assertThrows(BadImplementationException.class,
                () -> impl.prepare(BadWildcardListDao.class, m, NO_DB_BAG)
        );
    }

    /// Verifies that [QuerySqlImplementation#prepare] throws [BadImplementationException]
    /// when `[@QuerySql.fields][QuerySql]` specifies multiple column indices but the element
    /// type is not a record.
    @Test
    public void testPrepareThrowsOnMultiFieldNonRecord() throws Exception {
        var impl = new QuerySqlImplementation(NO_DB_BAG);
        var m = BadMultiFieldNonRecordDao.class.getMethod("bad");
        Assertions.assertThrows(BadImplementationException.class,
                () -> impl.prepare(BadMultiFieldNonRecordDao.class, m, NO_DB_BAG)
        );
    }

    /// Verifies that [QuerySqlImplementation#prepare] throws [BadImplementationException]
    /// when the number of indices in `[@QuerySql.fields][QuerySql]` does not match the number
    /// of components in the target record type.
    @Test
    public void testPrepareThrowsOnRecordFieldMismatch() throws Exception {
        var impl = new QuerySqlImplementation(NO_DB_BAG);
        var m = BadRecordFieldMismatchDao.class.getMethod("bad");
        Assertions.assertThrows(BadImplementationException.class,
                () -> impl.prepare(BadRecordFieldMismatchDao.class, m, NO_DB_BAG)
        );
    }

    // ── Tests: scalar / Optional return types ─────────────────────────────────

    /// Verifies scalar and `Optional` return types for [@QuerySql][QuerySql] methods that
    /// select a single column value:
    ///
    /// - `Optional<String>`: Present when a row is found, empty when none matches.
    /// - Bare `String`: Non-null value when found, `null` when no row matches.
    /// - `OptionalInt`, `OptionalLong`, `OptionalDouble`: Present with the numeric value when
    ///   found, empty when none matches.
    @TestFactory
    public Stream<DynamicTest> testScalarReturnTypes() {
        var pf = "[testScalarReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "Optional<String> - row found → Optional.of(value)", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findLabel", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(Optional.of("alpha"), ctx.execute(dummyProxy(QueryDao.class), 1));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Optional<String> - no row found → Optional.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findLabel", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(Optional.empty(), ctx.execute(dummyProxy(QueryDao.class), 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "bare String - row found → value", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findLabelBare", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals("beta", ctx.execute(dummyProxy(QueryDao.class), 2));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "bare String - no row found → null", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findLabelBare", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertNull(ctx.execute(dummyProxy(QueryDao.class), 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalInt - row found → OptionalInt.of(value)", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findAmountAsInt", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(OptionalInt.of(10), ctx.execute(dummyProxy(QueryDao.class), 1));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalInt - no row found → OptionalInt.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findAmountAsInt", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(OptionalInt.empty(), ctx.execute(dummyProxy(QueryDao.class), 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalLong - row found → OptionalLong.of(value)", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findAmountAsLong", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(OptionalLong.of(20L), ctx.execute(dummyProxy(QueryDao.class), 2));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalLong - no row found → OptionalLong.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findAmountAsLong", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(OptionalLong.empty(), ctx.execute(dummyProxy(QueryDao.class), 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalDouble - row found → OptionalDouble.of(value)", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findAmountAsDouble", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(OptionalDouble.of(30.0), ctx.execute(dummyProxy(QueryDao.class), 3));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalDouble - no row found → OptionalDouble.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findAmountAsDouble", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(OptionalDouble.empty(), ctx.execute(dummyProxy(QueryDao.class), 999));
                }).wrap())
        );
    }

    // ── Tests: List return types ───────────────────────────────────────────────

    /// Verifies `List` return types for [@QuerySql][QuerySql] methods:
    ///
    /// - `List<String>` returns all rows in query order when the table is populated.
    /// - `List<String>` returns an empty list when the table has no rows.
    @TestFactory
    public Stream<DynamicTest> testListReturnTypes() {
        var pf = "[testListReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "List<String> - all rows in order", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("listLabels");
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(List.of("alpha", "beta", "gamma"),
                            ctx.execute(dummyProxy(QueryDao.class))
                    );
                }).wrap()),

                DynamicTest.dynamicTest(pf + "List<String> - empty table → empty list", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("listLabels");
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(List.of(), ctx.execute(dummyProxy(QueryDao.class)));
                }).wrap())
        );
    }

    // ── Tests: record return types ─────────────────────────────────────────────

    /// Verifies record-mapping return types for [@QuerySql][QuerySql] methods:
    ///
    /// - `Optional<Item>` maps column values to record components in declaration order.
    /// - `List<Item>` collects all rows in query order.
    /// - `Optional<Item>` with explicit `fields` indices maps columns in the specified order,
    ///   allowing result columns to appear in a different order than the record components.
    @TestFactory
    public Stream<DynamicTest> testRecordReturnTypes() {
        var pf = "[testRecordReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "Optional<Item> - row found → Optional.of(record)", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findItem", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(Optional.of(new Item("alpha", 10)),
                            ctx.execute(dummyProxy(QueryDao.class), 1)
                    );
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Optional<Item> - no row → Optional.empty()", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findItem", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(Optional.empty(), ctx.execute(dummyProxy(QueryDao.class), 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "List<Item> - all rows in order", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("listItems");
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    Assertions.assertEquals(
                            List.of(new Item("alpha", 10), new Item("beta", 20), new Item("gamma", 30)),
                            ctx.execute(dummyProxy(QueryDao.class))
                    );
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Optional<Item> with explicit field mapping - reversed columns", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new QuerySqlImplementation(bag);
                    var m = QueryDao.class.getMethod("findItemReversed", int.class);
                    var ctx = impl.prepare(QueryDao.class, m, bag);
                    // SELECT amount, label → col1=amount, col2=label; fields={2,1} → label←col2, amount←col1
                    Assertions.assertEquals(Optional.of(new Item("beta", 20)),
                            ctx.execute(dummyProxy(QueryDao.class), 2)
                    );
                }).wrap())
        );
    }
}
