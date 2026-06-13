package ninja.javahacker.test.annotimpler.sql.sqlimpl;

import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Proxy;
import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

/// Tests for [ExecuteSqlImplementation], the runtime handler for DAO methods annotated
/// with [@ExecuteSql][ExecuteSql].
///
/// Each scenario creates a fresh in-memory H2 database, installs the required schema (and
/// optionally seed data), wires a [PropertyBag] containing that connection, and invokes the
/// compiled operation through `MethodContext.execute`.
///
/// The tests cover:
/// - Rejection of illegal return types (e.g. `String`, `boolean`) at `prepare()` time.
/// - Correct row-count values for all supported return types
///   (`void`, `long`, `Long`, `int`, `Integer`).
/// - Enforcement of the `acceptsZero` and `acceptsMulti` flags.
public class ExecuteSqlImplementationTest {

    // ── DB schemas ─────────────────────────────────────────────────────────────

    private static final List<String> T_SCHEMA = List.of(
            "CREATE TABLE t (id INT PRIMARY KEY, label VARCHAR(50), amount INT)"
    );

    private static final List<String> T_SEED = List.of(
            "INSERT INTO t VALUES (1, 'alpha', 10)",
            "INSERT INTO t VALUES (2, 'beta',  20)",
            "INSERT INTO t VALUES (3, 'gamma', 30)"
    );

    // ── Test DAO interfaces ────────────────────────────────────────────────────

    interface ExecuteDao {
        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        void insertVoid(int id, String label, int amount) throws Exception;

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        long insertLong(int id, String label, int amount) throws Exception;

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        Long insertBoxedLong(int id, String label, int amount) throws Exception;

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        int insertInt(int id, String label, int amount) throws Exception;

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        Integer insertBoxedInt(int id, String label, int amount) throws Exception;

        @ExecuteSql
        @Sql("DELETE FROM t WHERE id = :id")
        void deleteStrict(int id) throws Exception;

        @ExecuteSql(acceptsZero = true)
        @Sql("DELETE FROM t WHERE id = :id")
        void deleteRelaxed(int id) throws Exception;

        @ExecuteSql(acceptsZero = true)
        @Sql("UPDATE t SET amount = :amount")
        void updateAllStrict(int amount) throws Exception;

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("UPDATE t SET amount = :amount")
        void updateAllRelaxed(int amount) throws Exception;
    }

    interface BadStringReturnDao {
        @ExecuteSql @Sql("SELECT 1") String bad() throws Exception;
    }

    interface BadBooleanReturnDao {
        @ExecuteSql @Sql("SELECT 1") boolean bad() throws Exception;
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

    private static int countRows(Connection con) throws SQLException {
        try (
                var ps = con.prepareStatement("SELECT COUNT(*) FROM t");
                var rs = ps.executeQuery()
        ) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ── Tests: prepare() validation ───────────────────────────────────────────

    /// Verifies that [ExecuteSqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares a `String` return type, which is not allowed for
    /// DML operations.
    @Test
    public void testPrepareThrowsOnStringReturnType() throws Exception {
        var impl = new ExecuteSqlImplementation(NO_DB_BAG);
        var m = BadStringReturnDao.class.getMethod("bad");
        Assertions.assertThrows(BadImplementationException.class,
                () -> impl.prepare(BadStringReturnDao.class, m, NO_DB_BAG)
        );
    }

    /// Verifies that [ExecuteSqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares a `boolean` return type, which is not allowed for
    /// DML operations.
    @Test
    public void testPrepareThrowsOnBooleanReturnType() throws Exception {
        var impl = new ExecuteSqlImplementation(NO_DB_BAG);
        var m = BadBooleanReturnDao.class.getMethod("bad");
        Assertions.assertThrows(BadImplementationException.class,
                () -> impl.prepare(BadBooleanReturnDao.class, m, NO_DB_BAG)
        );
    }

    // ── Tests: return type selection ──────────────────────────────────────────

    /// Verifies that each supported return type for [@ExecuteSql][ExecuteSql] methods produces
    /// the correct value after a single-row INSERT.
    ///
    /// Tested return types: `void`, `long`, `Long`, `int`, `Integer`.
    @TestFactory
    public Stream<DynamicTest> testReturnTypes() {
        var pf = "[testReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "void - row is inserted", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("insertVoid", int.class, String.class, int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10);
                    Assertions.assertEquals(1, countRows(con));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "long - returns 1 for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("insertLong", int.class, String.class, int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(1L, ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Long - returns boxed 1L for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("insertBoxedLong", int.class, String.class, int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(Long.valueOf(1L), ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "int - returns 1 for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("insertInt", int.class, String.class, int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(1, ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Integer - returns boxed 1 for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("insertBoxedInt", int.class, String.class, int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(Integer.valueOf(1), ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap())
        );
    }

    // ── Tests: acceptsZero / acceptsMulti ─────────────────────────────────────

    /// Verifies the row-count enforcement flags `acceptsZero` and `acceptsMulti` on
    /// [@ExecuteSql][ExecuteSql]:
    ///
    /// - `acceptsZero = false` with 0 affected rows → [java.sql.SQLException].
    /// - `acceptsZero = true` with 0 affected rows → no exception.
    /// - `acceptsMulti = false` with multiple affected rows → [java.sql.SQLException].
    /// - `acceptsMulti = true` with multiple affected rows → no exception.
    @TestFactory
    public Stream<DynamicTest> testAcceptsFlags() {
        var pf = "[testAcceptsFlags] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "acceptsZero=false, 0 rows → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("deleteStrict", int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertThrows(SQLException.class, () -> ctx.execute(proxy, 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "acceptsZero=true, 0 rows → ok", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("deleteRelaxed", int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertDoesNotThrow(() -> ctx.execute(proxy, 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "acceptsMulti=false, 3 rows → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("updateAllStrict", int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertThrows(SQLException.class, () -> ctx.execute(proxy, 99));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "acceptsMulti=true, 3 rows → ok", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var impl = new ExecuteSqlImplementation(bag);
                    var m = ExecuteDao.class.getMethod("updateAllRelaxed", int.class);
                    var ctx = impl.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertDoesNotThrow(() -> ctx.execute(proxy, 99));
                }).wrap())
        );
    }
}
