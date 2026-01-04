package ninja.javahacker.test.annotimpler.sql.conn;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import ninja.javahacker.test.ForTests;

import module org.junit.jupiter.api;
import org.junit.jupiter.api.function.Executable;

public class ConnectionCreatorTest {

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

        public List<Executable> createAll(E model) {
            var original = destructure.work(model);
            var p = (int) Math.pow(2, replaces.length);
            var list = new ArrayList<Executable>(p);
            for (var i = 0; i < p; i++) {
                var j = i;
                var myReplace = replace(i, original, false);
                if (k == UrlConnector.class && p == 8 && (i >= 2 && i <= 5)) {
                    list.add(() -> {
                        var ex = Assertions.assertThrows(InvocationTargetException.class, () -> create(j), "test " + j);
                        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
                        Assertions.assertEquals("User and password can't be separated.", ex.getCause().getMessage());
                    });
                } else {
                    list.add(() -> {
                        var obj = create(j);
                        var des = destructure.work(obj);
                        Assertions.assertArrayEquals(myReplace, des, "test " + j);
                    });
                }
            }
            for (var i = 0; i < replaces.length; i++) {
                var copy = promoteAll(original);
                copy[i] = null;
                var pn = creator.getParameters()[i].getName();
                list.add(() -> ForTests.testNullReflective(pn, () -> create(copy), "test null creator " + pn));
            }
            return list;
        }
    }

    private static record NullCtorFactory<E extends Connector>(Constructor<E> k, Object[] params) {
        public List<Executable> createAll() {
            var arity = params.length;
            var list = new ArrayList<Executable>(arity);
            var kp = k.getParameters();
            list.add(() -> Assertions.assertDoesNotThrow(() -> k.newInstance(params.clone()), "[good] test"));
            for (var i = 0; i < arity; i++) {
                if (kp[i].getType().isPrimitive()) continue;
                var copy = params.clone();
                var pn = kp[i].getName();
                copy[i] = null;
                var j = i;
                list.add(() -> ForTests.testNullReflective(pn, () -> k.newInstance(copy), "[" + j + "] test " + pn + " null"));
            }
            return list;
        }
    }

    private static <E extends Connector> E model(Class<E> k) {
        try {
            return k.cast(k.getMethod("std").invoke(null));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static <E extends Connector> void testAll(Class<E> k, Object[] replaces, Destructure<E> destructure) {
        var tests = new TestFactory<>(replaces, k, creatorMethod(k, replaces.length), destructure).createAll(model(k));
        Assertions.assertAll(tests);
    }

    private static <E extends Connector> List<Executable> testAll(Constructor<E> k, Object[] params) {
        return new NullCtorFactory<>(k, params).createAll();
    }

    private static <E extends Connector> void testAll(Class<E> k, Object[] replaces, Destructure<E> destructure, Object[] ctorParams) {
        var tests1 = new TestFactory<>(replaces, k, creatorMethod(k, replaces.length), destructure).createAll(model(k));
        @SuppressWarnings("unchecked")
        var tests2 = new NullCtorFactory<>((Constructor<E>) k.getConstructors()[0], ctorParams).createAll();
        var tests = Stream.of(tests1, tests2).flatMap(List::stream).toList();
        Assertions.assertAll(tests);
    }

    @Test
    public void mariaDbCreatorTest() {
        Destructure<MariaDbConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        testAll(MariaDbConnector.class, replaces, dest, replaces);
    }

    @Test
    public void mariaDbNullCtorTest() {
        Destructure<MariaDbConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        testAll(MariaDbConnector.class, replaces, dest, replaces);
    }

    @Test
    public void mySqlCreatorTest() {
        Destructure<MySqlConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        testAll(MySqlConnector.class, replaces, dest, replaces);
    }

    @Test
    public void postgreSqlCreatorTest() {
        Destructure<PostgreSqlConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database(), a.ssl()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test", false};
        testAll(PostgreSqlConnector.class, replaces, dest, replaces);
    }

    @Test
    public void oracleCreatorTest() {
        Destructure<OracleConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database(), a.rac()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test", true};
        testAll(OracleConnector.class, replaces, dest, replaces);
    }

    @Test
    public void sqlServerCreatorTest() {
        Destructure<SqlServerConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.database()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test"};
        testAll(SqlServerConnector.class, replaces, dest, replaces);
    }

    @Test
    public void firebirdCreatorTest() {
        Destructure<FirebirdConnector> dest = a -> new Object[] {a.host(), a.port(), a.user(), a.password(), a.filename(), a.encoding()};
        var replaces = new Object[] {"10.0.0.1", 5555, "master", "pa$$", "test", "ISO-8859-1"};
        testAll(FirebirdConnector.class, replaces, dest, replaces);
    }

    @Test
    public void localDebyCreatorTest() {
        Destructure<LocalDerbyConnector> dest = a -> new Object[] {a.directory(), a.subsubprotocol(), a.create()};
        var replaces = new Object[] {"test", LocalDerbyConnector.DerbyType.JAR, true};
        testAll(LocalDerbyConnector.class, replaces, dest, replaces);
    }

    @Test
    public void remoteDebyCreatorTest() {
        Destructure<RemoteDerbyConnector> dest = a -> new Object[] {a.host(), a.port(), a.directory(), a.create()};
        var replaces = new Object[] {"10.0.0.1", 5555, "test", true};
        testAll(RemoteDerbyConnector.class, replaces, dest, replaces);
    }

    @Test
    public void hsqldbCreatorTest() {
        Destructure<HsqldbConnector> dest = a -> new Object[] {a.user(), a.password(), a.filename()};
        var replaces = new Object[] {"master", "pa$$", "test"};
        testAll(HsqldbConnector.class, replaces, dest, replaces);
    }

    @Test
    public void h2CreatorTest() {
        Destructure<H2Connector> dest = a -> new Object[] {a.user(), a.password(), a.filename()};
        var replaces = new Object[] {"master", "pa$$", "test"};
        testAll(H2Connector.class, replaces, dest, replaces);
    }

    @Test
    public void accessCreatorTest() {
        Destructure<AccessConnector> dest = a -> new Object[] {a.filename()};
        var replaces = new Object[] {"test"};
        testAll(AccessConnector.class, replaces, dest, replaces);
    }

    @Test
    public void sqliteCreatorTest() {
        Destructure<SqliteConnector> dest = a -> new Object[] {a.filename()};
        var replaces = new Object[] {"test"};
        testAll(SqliteConnector.class, replaces, dest, replaces);
    }

    @Test
    public void urlCreatorTest1() {
        Destructure<UrlConnector> dest = a -> new Object[] {a.url()};
        var replaces = new Object[] {"jdbc:test:test"};
        testAll(UrlConnector.class, replaces, dest);
    }

    @Test
    public void urlCreatorTest2() {
        Destructure<UrlConnector> dest = a -> new Object[] {a.url(), a.optAuth().orElse(null)};
        var replaces = new Object[] {"jdbc:test:test", new Connector.Auth("X", "Y")};
        testAll(UrlConnector.class, replaces, dest);
    }

    @Test
    public void urlCreatorTest3() {
        Destructure<UrlConnector> dest = a -> new Object[] {a.url(), a.optUser().orElse(null), a.optPassword().orElse(null)};
        var replaces = new Object[] {"jdbc:test:test", "X", "Y"};
        testAll(UrlConnector.class, replaces, dest);
    }

    @Test
    public void urlConnectorNullContructorTests() throws Exception {
        var u = UrlConnector.class;
        var auth = new Connector.Auth("X", "Y");
        var url = "jdbc:test:test";
        var a = testAll(u.getConstructor(String.class), new Object[] {url});
        var b = testAll(u.getConstructor(String.class, Connector.Auth.class), new Object[] {url, auth});
        var c = testAll(u.getConstructor(String.class, Optional.class), new Object[] {url, Optional.of(auth)});
        var d = testAll(u.getConstructor(String.class, String.class, String.class), new Object[] {url, "X", "Y"});
        var e = Stream.of(a, b, c, d).flatMap(List::stream).toList();
        Assertions.assertAll(e);
    }
}
