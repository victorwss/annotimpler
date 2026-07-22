package ninja.javahacker.test.annotimpler.sql.sqlimpl;

import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;

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

    public static interface ExecuteDao {
        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        void insertVoid(int id, String label, int amount);

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        void insertVoidBoxed(int id, String label, int amount);

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        long insertLong(int id, String label, int amount);

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        Long insertBoxedLong(int id, String label, int amount);

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        int insertInt(int id, String label, int amount);

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("INSERT INTO t VALUES (:id, :label, :amount)")
        Integer insertBoxedInt(int id, String label, int amount);

        @ExecuteSql
        @Sql("DELETE FROM t WHERE id = :id")
        void deleteStrict(int id);

        @ExecuteSql(acceptsZero = true)
        @Sql("DELETE FROM t WHERE id = :id")
        void deleteRelaxed(int id);

        @ExecuteSql(acceptsZero = true)
        @Sql("UPDATE t SET amount = :amount")
        void updateAllStrict(int amount);

        @ExecuteSql(acceptsZero = true, acceptsMulti = true)
        @Sql("UPDATE t SET amount = :amount")
        void updateAllRelaxed(int amount);
    }

    public static interface BadBasicDao {
        @ExecuteSql
        @Sql("UPDATE t SET amount = 1")
        @Override
        public boolean equals(Object other);

        @ExecuteSql
        @Sql("UPDATE t SET amount = 1")
        @Override
        public int hashCode();

        @ExecuteSql
        @Sql("UPDATE t SET amount = 1")
        @Override
        public String toString();

        @ExecuteSql
        @Sql("UPDATE t SET amount = 1")
        public Object clone();

        @ExecuteSql
        @Sql("UPDATE t SET amount = 1")
        public void finalize();

        @ExecuteSql
        @Sql("UPDATE t SET amount = 1")
        public static void staticMethod() {
            throw new AssertionError();
        }
    }

    public static interface BadStringReturnDao {
        @ExecuteSql
        @Sql("SELECT 1")
        String bad();
    }

    public static interface BadBooleanReturnDao {
        @ExecuteSql
        @Sql("SELECT 1")
        boolean bad();
    }

    // ── Infrastructure ─────────────────────────────────────────────────────────

    private static final ConnectionFactory NEVER_CONNECT = () -> {
        throw new AssertionError("Should not connect");
    };

    private static final PropertyBag NO_DB_BAG = PropertyBag.root()
            .add(ConnectionFactoryKeyProperty.INSTANCE, NEVER_CONNECT)
            .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.std())
            .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);

    private static final PropertyBag INCOMPLETE_BAG_1 = PropertyBag.root()
            .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.std())
            .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);

    private static final PropertyBag INCOMPLETE_BAG_2 = PropertyBag.root()
            .add(ConnectionFactoryKeyProperty.INSTANCE, NEVER_CONNECT)
            .add(LocalizerKeyProperty.INSTANCE, Locale.ROOT);

    private static final PropertyBag INCOMPLETE_BAG_3 = PropertyBag.root()
            .add(ConnectionFactoryKeyProperty.INSTANCE, NEVER_CONNECT)
            .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.std());

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
                .add(ConverterFactoryKeyProperty.INSTANCE, ConverterFactory.std())
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

    /// Verifies the exceptions that the [ExecuteSqlImplementation#prepare] method might throw.
    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "null", "ThrowableResultIgnored"})
    public Stream<DynamicTest> testPrepareExceptions() throws Exception {
        var pf = "[testPrepareExceptions] ";
        var m = ExecuteDao.class.getMethod("deleteStrict", int.class);
        var badm1 = Runnable.class.getMethod("run");
        var badm2 = BadStringReturnDao.class.getMethod("bad");
        var badm3 = BadBooleanReturnDao.class.getMethod("bad");
        var bado1 = BadBasicDao.class.getMethod("equals", Object.class);
        var bado2 = BadBasicDao.class.getMethod("hashCode");
        var bado3 = BadBasicDao.class.getMethod("toString");
        var bado4 = BadBasicDao.class.getMethod("clone");
        var bado5 = BadBasicDao.class.getMethod("finalize");
        var bado6 = BadBasicDao.class.getMethod("staticMethod");
        var impl = (ExecuteDao) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {ExecuteDao.class}, (i, t, a) -> {
            throw new AssertionError();
        });

        return Stream.of(
                DynamicTest.dynamicTest(pf + "null-k", () -> ForTests.testNull("k", () -> ExecuteSqlImplementation.INSTANCE.prepare(null, m, NO_DB_BAG))),
                DynamicTest.dynamicTest(pf + "null-m", () -> ForTests.testNull("m", () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, null, NO_DB_BAG))),
                DynamicTest.dynamicTest(pf + "null-p", () -> ForTests.testNull("props", () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, null))),

                DynamicTest.dynamicTest(pf + "null-i",
                        () -> ForTests.testNull("instance", () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, NO_DB_BAG).execute(null, new Object[0]))
                ),

                DynamicTest.dynamicTest(pf + "null-a",
                        () -> ForTests.testNull("a", () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, NO_DB_BAG).execute(impl, (Object[]) null))
                ),

                DynamicTest.dynamicTest(pf + "bad-meth", () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, badm1, NO_DB_BAG)
                )),

                DynamicTest.dynamicTest(pf + "no-anno", () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(Runnable.class, badm1, NO_DB_BAG)
                )),

                DynamicTest.dynamicTest(pf + "prop-missing-1", () -> Assertions.assertEquals(ConnectionFactoryKeyProperty.INSTANCE, Assertions.assertThrows(
                        PropertyBag.PropertyNotFoundException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, INCOMPLETE_BAG_1)
                ).getProperty())),

                DynamicTest.dynamicTest(pf + "prop-missing-2", () -> Assertions.assertEquals(ConverterFactoryKeyProperty.INSTANCE, Assertions.assertThrows(
                        PropertyBag.PropertyNotFoundException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, INCOMPLETE_BAG_2)
                ).getProperty())),

                DynamicTest.dynamicTest(pf + "prop-missing-3", () -> Assertions.assertEquals(LocalizerKeyProperty.INSTANCE, Assertions.assertThrows(
                        PropertyBag.PropertyNotFoundException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, INCOMPLETE_BAG_3)
                ).getProperty())),

                DynamicTest.dynamicTest(pf + "bad-equals", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado1, NO_DB_BAG),
                        "Unsupported annotation @Execute on " + bado1
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-hashCode", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado2, NO_DB_BAG),
                        "Unsupported annotation @Execute on " + bado2
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-toString", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado3, NO_DB_BAG),
                        "Unsupported annotation @Execute on " + bado3
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-clone", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado4, NO_DB_BAG),
                        "Unsupported annotation @Execute on " + bado4
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-finalize", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado5, NO_DB_BAG),
                        "Unsupported annotation @Execute on " + bado5
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-static", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado6, NO_DB_BAG),
                        "Unsupported annotation @Execute on " + bado6
                ).getRoot())),

                // Annotated method declares a String return type, which is not allowed for DML operations.
                DynamicTest.dynamicTest(pf + "bad-return-string", () -> Assertions.assertEquals(BadStringReturnDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadStringReturnDao.class, badm2, NO_DB_BAG),
                        "Unsupported return @Execute type on " + badm2
                ).getRoot())),

                // Annotated method declares a boolean return type, which is not allowed for DML operations.
                DynamicTest.dynamicTest(pf + "bad-return-boolean", () -> Assertions.assertEquals(BadBooleanReturnDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> ExecuteSqlImplementation.INSTANCE.prepare(BadBooleanReturnDao.class, badm3, NO_DB_BAG),
                        "Unsupported return @Execute type on " + badm3
                ).getRoot()))
        );
    }

    // ── Tests: return type selection ──────────────────────────────────────────

    /// Verifies that each supported return type for [@ExecuteSql][ExecuteSql] methods produces
    /// the correct value after a single-row INSERT.
    ///
    /// Tested return types: `void`, `Void`, `long`, `Long`, `int`, `Integer`.
    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testReturnTypes() {
        var pf = "[testReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "void - row is inserted", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("insertVoid", int.class, String.class, int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10);
                    Assertions.assertEquals(1, countRows(con));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Void - row is inserted", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("insertVoidBoxed", int.class, String.class, int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10);
                    Assertions.assertEquals(1, countRows(con));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "long - returns 1 for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("insertLong", int.class, String.class, int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(1L, ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Long - returns boxed 1L for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("insertBoxedLong", int.class, String.class, int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(1L, ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "int - returns 1 for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("insertInt", int.class, String.class, int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(1, ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Integer - returns boxed 1 for single insert", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("insertBoxedInt", int.class, String.class, int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    Assertions.assertEquals(1, ctx.execute(dummyProxy(ExecuteDao.class), 1, "alpha", 10));
                }).wrap())
        );
    }

    // ── Tests: acceptsZero / acceptsMulti ─────────────────────────────────────

    /// Verifies the row-count enforcement flags `acceptsZero` and `acceptsMulti` on
    /// [@ExecuteSql][ExecuteSql]:
    ///
    /// - `acceptsZero = false` with 0 affected rows → [SQLException].
    /// - `acceptsZero = true` with 0 affected rows → no exception.
    /// - `acceptsMulti = false` with multiple affected rows → [SQLException].
    /// - `acceptsMulti = true` with multiple affected rows → no exception.
    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testAcceptsFlags() {
        var pf = "[testAcceptsFlags] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "acceptsZero=false, 0 rows → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("deleteStrict", int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertThrows(SQLException.class, () -> ctx.execute(proxy, 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "acceptsZero=true, 0 rows → ok", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("deleteRelaxed", int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertDoesNotThrow(() -> ctx.execute(proxy, 999));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "acceptsMulti=false, 3 rows → SQLException", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("updateAllStrict", int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertThrows(SQLException.class, () -> ctx.execute(proxy, 99));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "acceptsMulti=true, 3 rows → ok", ((ConnectionContext) con -> {
                    setup(con, T_SCHEMA, T_SEED);
                    var bag = bagFor(con);
                    var m = ExecuteDao.class.getMethod("updateAllRelaxed", int.class);
                    var ctx = ExecuteSqlImplementation.INSTANCE.prepare(ExecuteDao.class, m, bag);
                    var proxy = dummyProxy(ExecuteDao.class);
                    Assertions.assertDoesNotThrow(() -> ctx.execute(proxy, 99));
                }).wrap())
        );
    }
}
