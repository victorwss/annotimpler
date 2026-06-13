package ninja.javahacker.test.annotimpler.sql.sqlimpl;

import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Proxy;
import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

/// Tests for [GenerateSqlImplementation], the runtime handler for DAO methods annotated
/// with [@GenerateSql][GenerateSql].
///
/// Each scenario creates a fresh in-memory H2 database with an auto-increment column,
/// wires a [PropertyBag] containing that connection, and invokes the compiled operation
/// through `MethodContext.execute`.
///
/// The tests cover:
/// - Rejection of illegal return types (raw `List`, `List<String>`, `String`) at `prepare()` time.
/// - Correct generated-key values for all supported return types
///   (`int`, `Integer`, `OptionalInt`, `long`, `Long`, `OptionalLong`, `List<Integer>`, `List<Long>`).
/// - Monotonically increasing key sequence across sequential inserts.
public class GenerateSqlImplementationTest {

    // ── DB schemas ─────────────────────────────────────────────────────────────

    private static final List<String> G_SCHEMA = List.of(
            "CREATE TABLE g (id INT AUTO_INCREMENT PRIMARY KEY, label VARCHAR(50))"
    );

    // ── Test DAO interfaces ────────────────────────────────────────────────────

    interface GenerateDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        int insertInt(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        Integer insertBoxedInt(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        OptionalInt insertOptionalInt(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        long insertLong(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        Long insertBoxedLong(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        OptionalLong insertOptionalLong(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List<Integer> insertListInt(String label) throws Exception;

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List<Long> insertListLong(String label) throws Exception;
    }

    @SuppressWarnings("rawtypes")
    interface BadRawListDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List bad(String label) throws Exception;
    }

    interface BadStringListDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List<String> bad(String label) throws Exception;
    }

    interface BadStringReturnDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        String bad(String label) throws Exception;
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

    /// Verifies that [GenerateSqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares a raw `List` return type (no type parameter).
    @Test
    public void testPrepareThrowsOnRawList() throws Exception {
        var impl = new GenerateSqlImplementation(NO_DB_BAG);
        var m = BadRawListDao.class.getMethod("bad", String.class);
        Assertions.assertThrows(BadImplementationException.class, () -> impl.prepare(BadRawListDao.class, m, NO_DB_BAG));
    }

    /// Verifies that [GenerateSqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares `List<String>` as the return type, which is not a
    /// supported numeric key type.
    @Test
    public void testPrepareThrowsOnStringList() throws Exception {
        var impl = new GenerateSqlImplementation(NO_DB_BAG);
        var m = BadStringListDao.class.getMethod("bad", String.class);
        Assertions.assertThrows(BadImplementationException.class, () -> impl.prepare(BadStringListDao.class, m, NO_DB_BAG));
    }

    /// Verifies that [GenerateSqlImplementation#prepare] throws [BadImplementationException]
    /// when the annotated method declares a `String` return type, which is not supported for
    /// generated-key retrieval.
    @Test
    public void testPrepareThrowsOnStringReturn() throws Exception {
        var impl = new GenerateSqlImplementation(NO_DB_BAG);
        var m = BadStringReturnDao.class.getMethod("bad", String.class);
        Assertions.assertThrows(BadImplementationException.class, () -> impl.prepare(BadStringReturnDao.class, m, NO_DB_BAG));
    }

    // ── Tests: return type selection ──────────────────────────────────────────

    /// Verifies that each supported return type for [@GenerateSql][GenerateSql] methods
    /// correctly delivers the auto-generated key produced by the database.
    ///
    /// Tested return types: `int`, `Integer`, `OptionalInt`, `long`, `Long`, `OptionalLong`,
    /// `List<Integer>`, `List<Long>`.  One additional dynamic test confirms that sequential
    /// inserts produce monotonically increasing key values.
    @TestFactory
    public Stream<DynamicTest> testReturnTypes() {
        var pf = "[testReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "int - returns first generated key as int", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertInt", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(1, ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Integer - returns first generated key as boxed Integer", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertBoxedInt", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(Integer.valueOf(1), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalInt - returns OptionalInt.of(1)", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertOptionalInt", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(OptionalInt.of(1), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "long - returns first generated key as long", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertLong", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(1L, ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Long - returns first generated key as boxed Long", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertBoxedLong", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(Long.valueOf(1L), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalLong - returns OptionalLong.of(1L)", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertOptionalLong", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(OptionalLong.of(1L), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "List<Integer> - returns [1] for first insert", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertListInt", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(List.of(1), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "List<Long> - returns [1L] for first insert", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertListLong", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(List.of(1L), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "int - sequential inserts produce keys 1 then 2", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var impl = new GenerateSqlImplementation(bag);
                    var m = GenerateDao.class.getMethod("insertInt", String.class);
                    var ctx = impl.prepare(GenerateDao.class, m, bag);
                    var proxy = dummyProxy(GenerateDao.class);
                    Assertions.assertEquals(1, ctx.execute(proxy, "foo"));
                    Assertions.assertEquals(2, ctx.execute(proxy, "bar"));
                }).wrap())
        );
    }
}
