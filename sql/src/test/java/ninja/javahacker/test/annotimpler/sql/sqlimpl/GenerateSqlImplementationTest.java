package ninja.javahacker.test.annotimpler.sql.sqlimpl;

import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;

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

    public static interface GenerateDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        int insertInt(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        Integer insertBoxedInt(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        OptionalInt insertOptionalInt(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        long insertLong(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        Long insertBoxedLong(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        OptionalLong insertOptionalLong(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List<Integer> insertListInt(String label);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List<Long> insertListLong(String label);
    }

    public static interface BadBasicDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        @Override
        public boolean equals(Object other);

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        @Override
        public int hashCode();

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        @Override
        public String toString();

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        public Object clone();

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        public void finalize();

        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        public static void staticMethod() {
            throw new AssertionError();
        }
    }

    @SuppressWarnings("rawtypes")
    public static interface BadRawListDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List bad(String label);
    }

    public static interface BadStringListDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        List<String> bad(String label);
    }

    public static interface BadStringReturnDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        String bad(String label);
    }

    public static interface BadNonListDao {
        @GenerateSql
        @Sql("INSERT INTO g (label) VALUES (:label)")
        Optional<Integer> bad(String label);
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

    // ── Tests: prepare() validation ───────────────────────────────────────────

    /// Verifies the exceptions that the [GenerateSqlImplementation#prepare] method might throw.
    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "null"})
    public Stream<DynamicTest> testPrepareExceptions() throws Exception {
        var pf = "[testPrepareExceptions] ";
        var m = GenerateDao.class.getMethod("insertInt", String.class);
        var badm1 = Runnable.class.getMethod("run");
        var badm2 = BadRawListDao.class.getMethod("bad", String.class);
        var badm3 = BadStringListDao.class.getMethod("bad", String.class);
        var badm4 = BadStringReturnDao.class.getMethod("bad", String.class);
        var badm5 = BadNonListDao.class.getMethod("bad", String.class);
        var bado1 = BadBasicDao.class.getMethod("equals", Object.class);
        var bado2 = BadBasicDao.class.getMethod("hashCode");
        var bado3 = BadBasicDao.class.getMethod("toString");
        var bado4 = BadBasicDao.class.getMethod("clone");
        var bado5 = BadBasicDao.class.getMethod("finalize");
        var bado6 = BadBasicDao.class.getMethod("staticMethod");
        var impl = (GenerateDao) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {GenerateDao.class}, (i, t, a) -> {
            throw new AssertionError();
        });

        return Stream.of(
                DynamicTest.dynamicTest(pf + "null-k", () -> ForTests.testNull("k", () -> GenerateSqlImplementation.INSTANCE.prepare(null, m, NO_DB_BAG))),
                DynamicTest.dynamicTest(pf + "null-m", () -> ForTests.testNull("m", () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, null, NO_DB_BAG))),
                DynamicTest.dynamicTest(pf + "null-p", () -> ForTests.testNull("props", () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, null))),

                DynamicTest.dynamicTest(pf + "null-i",
                        () -> ForTests.testNull("instance", () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, NO_DB_BAG).execute(null, new Object[0]))
                ),

                DynamicTest.dynamicTest(pf + "null-a",
                        () -> ForTests.testNull("a", () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, NO_DB_BAG).execute(impl, (Object[]) null))
                ),

                DynamicTest.dynamicTest(pf + "bad-meth", () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, badm1, NO_DB_BAG)
                )),

                DynamicTest.dynamicTest(pf + "no-anno", () -> Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(Runnable.class, badm1, NO_DB_BAG)
                )),

                DynamicTest.dynamicTest(pf + "prop-missing-1", () -> Assertions.assertEquals(ConnectionFactoryKeyProperty.INSTANCE, Assertions.assertThrows(
                        PropertyBag.PropertyNotFoundException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, INCOMPLETE_BAG_1)
                ).getProperty())),

                DynamicTest.dynamicTest(pf + "prop-missing-2", () -> Assertions.assertEquals(ConverterFactoryKeyProperty.INSTANCE, Assertions.assertThrows(
                        PropertyBag.PropertyNotFoundException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, INCOMPLETE_BAG_2)
                ).getProperty())),

                DynamicTest.dynamicTest(pf + "prop-missing-3", () -> Assertions.assertEquals(LocalizerKeyProperty.INSTANCE, Assertions.assertThrows(
                        PropertyBag.PropertyNotFoundException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, INCOMPLETE_BAG_3)
                ).getProperty())),

                DynamicTest.dynamicTest(pf + "bad-equals", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado1, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado1
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-hashCode", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado2, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado2
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-toString", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado3, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado3
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-clone", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado4, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado4
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-finalize", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado5, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado5
                ).getRoot())),

                DynamicTest.dynamicTest(pf + "bad-static", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadBasicDao.class, bado6, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado6
                ).getRoot())),

                // Annotated method declares a raw List return type (no type parameter).
                DynamicTest.dynamicTest(pf + "bad-return-raw", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadRawListDao.class, badm2, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado6
                ).getRoot())),

                // Annotated method declares List<String> as the return type, which is not a supported numeric key type.
                DynamicTest.dynamicTest(pf + "bad-return-bad-list", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadStringListDao.class, badm3, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado6
                ).getRoot())),

                // Annotated method declares a String return type, which is not supported for generated-key retrieval.
                DynamicTest.dynamicTest(pf + "bad-return-type", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadStringReturnDao.class, badm4, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado6
                ).getRoot())),

                // Annotated method declares Optional<Integer> return type, which is not supported parameterized type.
                DynamicTest.dynamicTest(pf + "bad-parameterized-return-type", () -> Assertions.assertEquals(BadBasicDao.class, Assertions.assertThrows(
                        BadImplementationException.class,
                        () -> GenerateSqlImplementation.INSTANCE.prepare(BadNonListDao.class, badm5, NO_DB_BAG),
                        "Unsupported annotation @Generate on " + bado6
                ).getRoot()))
        );
    }

    // ── Tests: return type selection ──────────────────────────────────────────

    /// Verifies that each supported return type for [@GenerateSql][GenerateSql] methods
    /// correctly delivers the auto-generated key produced by the database.
    ///
    /// Tested return types: `int`, `Integer`, `OptionalInt`, `long`, `Long`, `OptionalLong`,
    /// `List<Integer>`, `List<Long>`.  One additional dynamic test confirms that sequential
    /// inserts produce monotonically increasing key values.
    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testReturnTypes() {
        var pf = "[testReturnTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(pf + "int - returns first generated key as int", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertInt", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(1, ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Integer - returns first generated key as boxed Integer", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertBoxedInt", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(1, ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalInt - returns OptionalInt.of(1)", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertOptionalInt", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(OptionalInt.of(1), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "long - returns first generated key as long", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertLong", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(1L, ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "Long - returns first generated key as boxed Long", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertBoxedLong", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(1L, ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "OptionalLong - returns OptionalLong.of(1L)", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertOptionalLong", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(OptionalLong.of(1L), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "List<Integer> - returns [1] for first insert", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertListInt", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(List.of(1), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "List<Long> - returns [1L] for first insert", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertListLong", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    Assertions.assertEquals(List.of(1L), ctx.execute(dummyProxy(GenerateDao.class), "foo"));
                }).wrap()),

                DynamicTest.dynamicTest(pf + "int - sequential inserts produce keys 1 then 2", ((ConnectionContext) con -> {
                    setup(con, G_SCHEMA);
                    var bag = bagFor(con);
                    var m = GenerateDao.class.getMethod("insertInt", String.class);
                    var ctx = GenerateSqlImplementation.INSTANCE.prepare(GenerateDao.class, m, bag);
                    var proxy = dummyProxy(GenerateDao.class);
                    Assertions.assertEquals(1, ctx.execute(proxy, "foo"));
                    Assertions.assertEquals(2, ctx.execute(proxy, "bar"));
                }).wrap())
        );
    }
}
