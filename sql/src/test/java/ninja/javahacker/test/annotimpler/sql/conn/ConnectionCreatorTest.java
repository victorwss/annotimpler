package ninja.javahacker.test.annotimpler.sql.conn;

import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class ConnectionCreatorTest {

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    @FunctionalInterface
    private static interface Destructure<T> {
        public Object[] work(T in);
    }

    private static Method creatorMethod(Class<? extends Connector> k, int arity) {
        return Stream.of(k.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getParameterCount() == arity)
                .filter(m -> "create".equals(m.getName()))
                .findFirst()
                .get();
    }

    private static OptionalInt promote(Integer i) {
        return i == null ? OptionalInt.empty() : OptionalInt.of(i);
    }

    private static record TestFactory<E extends Connector>(
            Object[] replaces,
            Class<E> k,
            Method creator,
            Destructure<E> destructure)
    {

        private Object[] promoteAll(Object[] from) {
            var params = new Object[replaces.length];
            var types = creator.getParameterTypes();
            if (types.length != replaces.length) throw new AssertionError();
            for (int b = 0; b < params.length; b++) {
                var next = from[b];
                params[b] = types[b] == OptionalInt.class ? promote((Integer) next) : Optional.ofNullable(next);
            }
            return params;
        }

        private Object[] replace(int props, Object[] from, boolean wrap) {
            var params = new Object[replaces.length];
            var types = creator.getParameterTypes();
            if (types.length != replaces.length) throw new AssertionError();
            for (int p = 1, b = 0; b < params.length; p *= 2, b++) {
                var replacement = (p & props) != 0;
                var next = replacement ? replaces[b] : from[b];
                if (wrap) next = types[b] == OptionalInt.class ? promote((Integer) next) : Optional.ofNullable(next);
                params[b] = next;
            }
            return params;
        }

        private E create(Object[] params) throws Exception {
            return k.cast(creator.invoke(null, params));
        }

        private E create(int props) throws Exception {
            return create(replace(props, new Object[replaces.length], true));
        }

        public Stream<NamedTest> createAll(E model) {
            var original = destructure.work(model);
            var p = (int) Math.pow(2, replaces.length);
            var list = new ArrayList<NamedTest>(p);
            for (var i = 0; i < p; i++) {
                var j = i;
                var myReplace = replace(i, original, false);
                if (k == UrlConnector.class && p == 8 && (i >= 2 && i <= 5)) {
                    list.add(n(k.getSimpleName() + " user and password separated test " + j + "/" + p, () -> {
                        var ex = Assertions.assertThrows(InvocationTargetException.class, () -> create(j));
                        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
                        Assertions.assertEquals("User and password can't be separated.", ex.getCause().getMessage());
                    }));
                } else {
                    list.add(n(k.getSimpleName() + " destructure test " + j + "/" + p, () -> {
                        var obj = create(j);
                        var des = destructure.work(obj);
                        Assertions.assertArrayEquals(myReplace, des, "test ");
                    }));
                }
            }
            for (var i = 0; i < replaces.length; i++) {
                var copy = promoteAll(original);
                copy[i] = null;
                var pn = creator.getParameters()[i].getName();
                list.add(n(
                        k.getSimpleName() + " creator " + (i + 1) + " param is null",
                        () -> ForTests.testNullReflective(pn, () -> create(copy), "test null creator " + pn)
                ));
            }
            return list.stream();
        }
    }

    private static Stream<NamedTest> createAll(Constructor<? extends Connector> k, Object[] params) {
        var dbName = k.getDeclaringClass().getSimpleName();
        var arity = params.length;
        var list = new ArrayList<NamedTest>(arity);
        var kp = k.getParameters();
        list.add(n(dbName + " ctor does not throw", () -> Assertions.assertDoesNotThrow(() -> k.newInstance(params.clone()))));
        for (var i = 0; i < arity; i++) {
            if (kp[i].getType().isPrimitive()) continue;
            var copy = params.clone();
            var pn = kp[i].getName();
            copy[i] = null;
            var j = i;
            list.add(n(
                    dbName + " ctor " + (j + 1) + " param is null",
                    () -> ForTests.testNullReflective(pn, () -> k.newInstance(copy), "[" + j + "] test " + pn + " null")
            ));
        }
        return list.stream();
    }

    private static <E extends Connector> E model(Class<E> k) {
        try {
            return k.cast(k.getMethod("std").invoke(null));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static <E extends Connector> Stream<NamedTest> testAll(Class<E> k, Object[] replaces, Destructure<E> destructure) {
        return new TestFactory<>(replaces, k, creatorMethod(k, replaces.length), destructure).createAll(model(k));
    }

    private static Stream<NamedTest> testAll(Constructor<? extends Connector> k, Object[] params) {
        return createAll(k, params);
    }

    private static <E extends Connector> Stream<NamedTest> testAll(Class<E> k, Object[] replaces, Destructure<E> destructure, Object[] ctorParams) {
        var tests1 = new TestFactory<>(replaces, k, creatorMethod(k, replaces.length), destructure).createAll(model(k));
        @SuppressWarnings("unchecked")
        var tests2 = createAll((Constructor<E>) k.getConstructors()[0], ctorParams);
        return Stream.concat(tests1, tests2);
    }

    private static Stream<NamedTest> mariaDbCreatorTest() {
        Destructure<MariaDbConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        return testAll(MariaDbConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> mySqlCreatorTest() {
        Destructure<MySqlConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        return testAll(MySqlConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> postgreSqlCreatorTest() {
        Destructure<PostgreSqlConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database(), a.ssl()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test", false};
        return testAll(PostgreSqlConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> oracleCreatorTest() {
        Destructure<OracleConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database(), a.rac()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test", true};
        return testAll(OracleConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> db2CreatorTest() {
        Destructure<Db2Connector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        return testAll(Db2Connector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> sqlServerCreatorTest() {
        Destructure<SqlServerConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        return testAll(SqlServerConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> firebirdCreatorTest() {
        Destructure<FirebirdConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.filename(), a.encoding()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test", "ISO-8859-1"};
        return testAll(FirebirdConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> hsqldbCreatorTest() {
        Destructure<HsqldbConnector> dest = a -> new Object[] {a.user(), a.password(), a.filename(), a.memory()};
        var replaces = new Object[] {"master", "pa$$", "test", true};
        return testAll(HsqldbConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> h2CreatorTest() {
        Destructure<H2Connector> dest = a -> new Object[] {a.user(), a.password(), a.filename(), a.memory()};
        var replaces = new Object[] {"master", "pa$$", "test", true};
        return testAll(H2Connector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> accessCreatorTest() {
        Destructure<AccessConnector> dest = a -> new Object[] {a.filename()};
        var replaces = new Object[] {"test"};
        return testAll(AccessConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> sqliteCreatorTest() {
        Destructure<SqliteConnector> dest = a -> new Object[] {a.filename()};
        var replaces = new Object[] {"test"};
        return testAll(SqliteConnector.class, replaces, dest, replaces);
    }

    private static Stream<NamedTest> sqliteMemoryCreatorTest() {
        Destructure<SqliteMemoryConnector> dest = a -> new Object[0];
        var replaces = new Object[0];
        return testAll(SqliteMemoryConnector.class, replaces, dest);
    }

    private static Stream<NamedTest> urlCreatorTest1() {
        Destructure<UrlConnector> dest = a -> new Object[] {a.url()};
        var replaces = new Object[] {"jdbc:test:test"};
        return testAll(UrlConnector.class, replaces, dest);
    }

    private static Stream<NamedTest> urlCreatorTest2() {
        Destructure<UrlConnector> dest = a -> new Object[] {a.url(), a.optAuth().orElse(null)};
        var replaces = new Object[] {"jdbc:test:test", new Connector.Auth("X", "Y")};
        return testAll(UrlConnector.class, replaces, dest);
    }

    private static Stream<NamedTest> urlCreatorTest3() {
        Destructure<UrlConnector> dest = a -> new Object[] {a.url(), a.optUser().orElse(null), a.optPassword().orElse(null)};
        var replaces = new Object[] {"jdbc:test:test", "X", "Y"};
        return testAll(UrlConnector.class, replaces, dest);
    }

    private static Stream<Arguments> creatorTest() {
        return Stream.of(
                mariaDbCreatorTest(),
                mySqlCreatorTest(),
                postgreSqlCreatorTest(),
                oracleCreatorTest(),
                db2CreatorTest(),
                sqlServerCreatorTest(),
                firebirdCreatorTest(),
                hsqldbCreatorTest(),
                h2CreatorTest(),
                accessCreatorTest(),
                sqliteCreatorTest(),
                sqliteMemoryCreatorTest(),
                urlCreatorTest1(),
                urlCreatorTest2(),
                urlCreatorTest3()
        ).flatMap(x -> x).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "creatorTest {0}")
    public void creatorTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> urlConnectorNullContructorTests() throws Exception {
        var u = UrlConnector.class;
        var auth = new Connector.Auth("X", "Y");
        var url = "jdbc:test:test";
        var a = testAll(u.getConstructor(String.class), new Object[] {url});
        var b = testAll(u.getConstructor(String.class, Connector.Auth.class), new Object[] {url, auth});
        var c = testAll(u.getConstructor(String.class, Optional.class), new Object[] {url, Optional.of(auth)});
        var d = testAll(u.getConstructor(String.class, String.class, String.class), new Object[] {url, "X", "Y"});
        return Stream.of(a, b, c, d).flatMap(x -> x).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "urlConnectorNullContructorTests {0}")
    public void urlConnectorNullContructorTests(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static void connectionTest(ConnectionFactory confac) throws Exception {
        try (var con = confac.get()) {
            try (var ps = con.prepareStatement("CREATE TABLE foo(pk INT PRIMARY KEY, blah TEXT);")) {
                ps.executeUpdate();
            }
            try (var ps = con.prepareStatement("INSERT INTO foo(pk, blah) VALUES (1, 'whoa');")) {
                ps.executeUpdate();
            }
            try (var ps = con.prepareStatement("INSERT INTO foo(pk, blah) VALUES (2, 'lol');")) {
                ps.executeUpdate();
            }
            try (
                    var ps = con.prepareStatement("SELECT pk, blah FROM foo;");
                    var rs = ps.executeQuery())
            {
                rs.next();
                var a = rs.getInt("pk");
                var b = rs.getString("blah");
                rs.next();
                var c = rs.getInt("pk");
                var d = rs.getString("blah");
                Assertions.assertAll(
                        () -> Assertions.assertEquals(a, 1),
                        () -> Assertions.assertEquals(b, "whoa"),
                        () -> Assertions.assertEquals(c, 2),
                        () -> Assertions.assertEquals(d, "lol")
                );
            }
        }
    }

    private static Stream<Arguments> simpleConnectionTest() throws Exception {
        var json1 = "{\"type\":\"sqlite-memory\"}";
        var json2 = "{\"type\":\"h2\",\"memory\":true}";
        return Stream.of(
                n("sqlite std"     , () -> Assertions.assertDoesNotThrow(() -> connectionTest(SqliteMemoryConnector.std()))),
                n("sqlite std url1", () -> Assertions.assertDoesNotThrow(() -> connectionTest(SqliteMemoryConnector.std().asUrl()::get))),
                n("sqlite std url2", () -> Assertions.assertDoesNotThrow(() -> connectionTest(new UrlConnector(SqliteMemoryConnector.std().url())))),
                n("sqlite std url3", () -> Assertions.assertDoesNotThrow(() -> connectionTest(new UrlConnector(SqliteMemoryConnector.std().url())::get))),
                n("sqlite json"    , () -> Assertions.assertDoesNotThrow(() -> connectionTest(JsonConnector.read(json1)))),
                n("h2 std"         , () -> Assertions.assertDoesNotThrow(() -> connectionTest(H2Connector.std().withMemory(true)))),
                n("h2 std url1"    , () -> Assertions.assertDoesNotThrow(() -> connectionTest(H2Connector.std().withMemory(true).asUrl()::get))),
                n("h2 std url2"    , () -> Assertions.assertDoesNotThrow(() -> connectionTest(new UrlConnector(H2Connector.std().withMemory(true).url())))),
                n("h2 std url3"    , () -> Assertions.assertDoesNotThrow(() -> connectionTest(new UrlConnector(H2Connector.std().withMemory(true).url())::get))),
                n("h2 json"        , () -> Assertions.assertDoesNotThrow(() -> connectionTest(JsonConnector.read(json2))))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "simpleConnectionTest {0}")
    public void simpleConnectionTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Stream<Arguments> urlsTest() {
        var maria1 = MariaDbConnector.std();
        var maria2 = maria1.withDatabase("foo").withPort(12345).withHost("10.10.10.10").withUser("lol").withPassword("pass");
        var mysql1 = MySqlConnector.std();
        var mysql2 = mysql1.withDatabase("foo").withPort(12345).withHost("10.10.10.10").withUser("lol").withPassword("pass");
        var fire1 = FirebirdConnector.std();
        var fire2 = fire1.withFilename("foo").withPort(12345).withHost("10.10.10.10").withUser("lol").withPassword("pass")
                .withEncoding("UTF16");
        var fire3 = fire1.withFilename("bar").withPort(44444).withHost("10.11.12.13").withUser("lol").withPassword("pass").withEncoding("");
        var hTwo1 = H2Connector.std();
        var hTwo2 = hTwo1.withFilename("foo").withUser("lol").withPassword("pass");
        var hTwo3 = hTwo2.withMemory(true);
        var hsql1 = HsqldbConnector.std();
        var hsql2 = hsql1.withFilename("foo").withUser("lol").withPassword("pass");
        var hsql3 = hsql2.withMemory(true);
        var sqliteMem = SqliteMemoryConnector.std();
        // TODO: Access, DB2, Oracle, PostgreSQL, SQL Server, SQLite file.
        return Stream.of(
                n("sqlite 1"  , () -> Assertions.assertEquals("jdbc:sqlite::memory:", sqliteMem.url())),
                n("mariadb 1" , () -> Assertions.assertEquals("jdbc:mariadb://localhost:3306/", maria1.url())),
                n("mariadb 2" , () -> Assertions.assertEquals("jdbc:mariadb://10.10.10.10:12345/foo", maria2.url())),
                n("mysql 1"   , () -> Assertions.assertEquals("jdbc:mysql://localhost:3306/", mysql1.url())),
                n("mysql 2"   , () -> Assertions.assertEquals("jdbc:mysql://10.10.10.10:12345/foo", mysql2.url())),
                n("firebird 1", () -> Assertions.assertEquals("jdbc:firebird://localhost:3050/?encoding=UTF8", fire1.url())),
                n("firebird 2", () -> Assertions.assertEquals("jdbc:firebird://10.10.10.10:12345/foo?encoding=UTF16", fire2.url())),
                n("firebird 3", () -> Assertions.assertEquals("jdbc:firebird://10.11.12.13:44444/bar", fire3.url())),
                n("h2 1"      , () -> Assertions.assertEquals("jdbc:h2:~/", hTwo1.url())),
                n("h2 2"      , () -> Assertions.assertEquals("jdbc:h2:~/foo", hTwo2.url())),
                n("h2 3"      , () -> Assertions.assertEquals("jdbc:h2:mem:foo", hTwo3.url())),
                n("hsqldb 1"  , () -> Assertions.assertEquals("jdbc:hsqldb:file://", hsql1.url())),
                n("hsqldb 2"  , () -> Assertions.assertEquals("jdbc:hsqldb:file://foo", hsql2.url())),
                n("hsqldb 3"  , () -> Assertions.assertEquals("jdbc:hsqldb:mem:foo", hsql3.url()))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "urlsTest {0}")
    public void urlsTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
