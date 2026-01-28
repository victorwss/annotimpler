package ninja.javahacker.test.annotimpler.sql.conn;

import lombok.NonNull;
import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class ConnectionPropertyTests {

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    private static Object get(Object conn, String name) throws Exception {
        return conn.getClass().getMethod(name).invoke(conn);
    }

    @SuppressWarnings("unchecked")
    private static Object set(Object conn, String name, Class<?> type, Object value) throws Exception {
        return conn.getClass().getMethod(name, type).invoke(conn, value);
    }

    private static String host(Object conn) throws Exception {
        return (String) get(conn, "host");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withHost(E conn, String value) throws Exception {
        return (E) set(conn, "withHost", String.class, value);
    }

    private static int port(Object conn) throws Exception {
        return (Integer) get(conn, "port");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withPort(E conn, int value) throws Exception {
        return (E) set(conn, "withPort", int.class, value);
    }

    private static String user(Object conn) throws Exception {
        return (String) get(conn, "user");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withUser(E conn, String value) throws Exception {
        return (E) set(conn, "withUser", String.class, value);
    }

    @SuppressWarnings("unchecked")
    private static Optional<String> optUser(Object conn) throws Exception {
        return (Optional<String>) get(conn, "optUser");
    }

    private static String pass(Object conn) throws Exception {
        return (String) get(conn, "password");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withPass(E conn, String value) throws Exception {
        return (E) set(conn, "withPassword", String.class, value);
    }

    @SuppressWarnings("unchecked")
    private static Optional<String> optPass(Object conn) throws Exception {
        return (Optional<String>) get(conn, "optPassword");
    }

    private static Connector.Auth auth(Object conn) throws Exception {
        return (Connector.Auth) get(conn, "auth");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withAuth(E conn, Connector.Auth value) throws Exception {
        return (E) set(conn, "withAuth", Connector.Auth.class, value);
    }

    @SuppressWarnings("unchecked")
    private static <E> E withAuth(E conn, String user, String pass) throws Exception {
        return (E) conn.getClass().getMethod("withAuth", String.class, String.class).invoke(conn, user, pass);
    }

    @SuppressWarnings("unchecked")
    private static Optional<Connector.Auth> optAuth(Object conn) throws Exception {
        return (Optional<Connector.Auth>) get(conn, "optAuth");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withOptAuth(E conn, Optional<Connector.Auth> value) throws Exception {
        return (E) set(conn, "withOptAuth", Optional.class, value);
    }

    private static String url(Object conn) throws Exception {
        return (String) get(conn, "url");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withUrl(E conn, String value) throws Exception {
        return (E) set(conn, "withUrl", String.class, value);
    }

    private static UrlConnector asUrl(Object conn) throws Exception {
        return (UrlConnector) get(conn, "asUrl");
    }

    private static String database(Object conn) throws Exception {
        return (String) get(conn, "database");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withDatabase(E conn, String value) throws Exception {
        return (E) set(conn, "withDatabase", String.class, value);
    }

    private static String filename(Object conn) throws Exception {
        return (String) get(conn, "filename");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withFilename(E conn, String value) throws Exception {
        return (E) set(conn, "withFilename", String.class, value);
    }

    private static String directory(Object conn) throws Exception {
        return (String) get(conn, "directory");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withDirectory(E conn, String value) throws Exception {
        return (E) set(conn, "withDirectory", String.class, value);
    }

    private static String encoding(Object conn) throws Exception {
        return (String) get(conn, "encoding");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withEncoding(E conn, String value) throws Exception {
        return (E) set(conn, "withEncoding", String.class, value);
    }

    private static boolean ssl(Object conn) throws Exception {
        return (Boolean) get(conn, "ssl");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withSsl(E conn, boolean value) throws Exception {
        return (E) set(conn, "withSsl", boolean.class, value);
    }

    private static boolean rac(Object conn) throws Exception {
        return (Boolean) get(conn, "rac");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withRac(E conn, boolean value) throws Exception {
        return (E) set(conn, "withRac", boolean.class, value);
    }

    private static boolean create(Object conn) throws Exception {
        return (Boolean) get(conn, "create");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withCreate(E conn, boolean value) throws Exception {
        return (E) set(conn, "withCreate", boolean.class, value);
    }

    private static void addTestsConnector(
            @NonNull String id,
            @NonNull List<NamedTest> tests,
            @NonNull Map<String, String> props,
            @NonNull Connector conn)
    {
        var host = props.get("host");
        var port = props.get("port");
        var user = props.get("user");
        var pass = props.get("password");
        var database = props.get("database");
        var filename = props.get("filename");
        var directory = props.get("directory");
        var url = props.get("url");
        var encoding = props.get("encoding");
        var rac = props.get("rac");
        var ssl = props.get("ssl");
        var create = props.get("create");
        var hashCode = props.get("hashCode");
        var toString = props.get("toString");

        var keys = props.keySet();
        var others = new ArrayList<>(keys);
        var all = List.of(
                "host", "port", "user", "password", "database", "filename", "directory",
                "url", "encoding", "rac", "ssl", "create", "subsubprotocol", "hashCode", "toString"
        );
        others.removeAll(all);
        if (!others.isEmpty()) throw new AssertionError(others);

        for (var name : all) {
            if (List.of("hashCode", "toString").contains(name)) continue;
            try {
                conn.getClass().getMethod(name);
                if (!keys.contains(name)) throw new AssertionError(name);
            } catch (NoSuchMethodException e) {
                if (keys.contains(name)) {
                    if (List.of("user", "password").contains(name) && conn instanceof UrlConnector) continue;
                    throw new AssertionError(e);
                }
            }
        }

        if (url != null) {
            tests.add(n(id + "url1", () -> Assertions.assertEquals(url, url(conn))));
            tests.add(n(id + "url2", () -> Assertions.assertEquals(url, asUrl(conn).url())));
            if (conn instanceof UrlConnector) {
                tests.add(n(id + "withUrl", () -> Assertions.assertEquals(url + "UUU", url(withUrl(conn, url + "UUU")))));
                tests.add(n("withUrl-null", () -> ForTests.testNullReflective("url", () -> withUrl(conn, null))));
            }
        }
        if (host != null) {
            tests.add(n(id + "host", () -> Assertions.assertEquals(host, host(conn))));
            tests.add(n(id + "withHost", () -> Assertions.assertEquals(host + "GGG", host(withHost(conn, host + "GGG")))));
            tests.add(n("withHost-null", () -> ForTests.testNullReflective("host", () -> withHost(conn, null))));
        }
        if (port != null) {
            int portv = Integer.parseInt(port);
            tests.add(n(id + "port", () -> Assertions.assertEquals(portv, port(conn))));
            tests.add(n(id + "withPort", () -> Assertions.assertEquals(portv + 5, port(withPort(conn, portv + 5)))));
        }
        if (user != null) {
            var oauth = new Connector.Auth(user, pass);
            var uy = user + "YYY";
            var pz = pass + "ZZZ";
            var nauth1 = new Connector.Auth(uy, pz);
            var nauth2 = new Connector.Auth(uy, pz);
            var nauth3 = Optional.of(nauth1);
            var iurl = conn instanceof UrlConnector;
            if (!iurl) tests.add(n(id + "user", () -> Assertions.assertEquals(user, user(conn))));
            if (!iurl) tests.add(n(id + "password", () -> Assertions.assertEquals(pass, pass(conn))));
            if (!iurl) tests.add(n(id + "auth", () -> Assertions.assertEquals(oauth, auth(conn))));
            tests.add(n(id + "optUser", () -> Assertions.assertEquals(user, optUser(conn).get())));
            tests.add(n(id + "optPass", () -> Assertions.assertEquals(pass, optPass(conn).get())));
            tests.add(n(id + "urlOptUser", () -> Assertions.assertEquals(user, asUrl(conn).optUser().get())));
            tests.add(n(id + "urlOptUser", () -> Assertions.assertEquals(pass, asUrl(conn).optPassword().get())));
            tests.add(n(id + "optAuth", () -> Assertions.assertEquals(oauth, optAuth(conn).get())));
            tests.add(n(id + "urlOptAuth", () -> Assertions.assertEquals(oauth, asUrl(conn).optAuth().get())));
            if (!iurl) tests.add(n(id + "withUser", () -> Assertions.assertEquals(uy, user(withUser(conn, uy)))));
            if (!iurl) tests.add(n(id + "withPass", () -> Assertions.assertEquals(pz, pass(withPass(conn, pz)))));
            if (!iurl) tests.add(n(id + "withAuth1", () -> Assertions.assertEquals(nauth1, auth(withAuth(conn, nauth2)))));
            if (!iurl) tests.add(n(id + "withAuth2", () -> Assertions.assertEquals(nauth1, auth(withAuth(conn, uy, pz)))));
            tests.add(n(id + "withAuth3", () -> Assertions.assertEquals(nauth1, withAuth(conn, nauth2).optAuth().get())));
            tests.add(n(id + "withAuth4", () -> Assertions.assertEquals(nauth1, withAuth(conn, uy, pz).optAuth().get())));
            tests.add(n(id + "withAuth5", () -> Assertions.assertEquals(nauth1, asUrl(conn).withAuth(nauth2).optAuth().get())));
            tests.add(n(id + "withAuth6", () -> Assertions.assertEquals(nauth1, asUrl(conn).withAuth(uy, pz).optAuth().get())));
            tests.add(n(id + "withAuth7", () -> Assertions.assertEquals(nauth1, withAuth(conn, nauth2).asUrl().optAuth().get())));
            tests.add(n(id + "withAuth8", () -> Assertions.assertEquals(nauth1, withAuth(conn, uy, pz).asUrl().optAuth().get())));
            tests.add(n(id + "withAuth9", () -> Assertions.assertEquals(nauth2, asUrl(conn).withOptAuth(nauth3).optAuth().get())));
            tests.add(n(id + "withAuth10", () -> Assertions.assertTrue(asUrl(conn).withOptAuth(Optional.empty()).optAuth().isEmpty())));
            tests.add(n(id + "withNoAuth", () -> Assertions.assertTrue(asUrl(conn).withNoAuth().optAuth().isEmpty())));
            if (!iurl) tests.add(n(id + "withUser-null", () -> ForTests.testNullReflective("user", () -> withUser(conn, null))));
            if (!iurl) tests.add(n(id + "withPass-null", () -> ForTests.testNullReflective("password", () -> withPass(conn, null))));
            tests.add(n(id + "withAuth-null", () -> ForTests.testNullReflective("auth", () -> withAuth(conn, null))));
            tests.add(n(id + "withAuthUser-null", () -> ForTests.testNullReflective("user", () -> withAuth(conn, null, pz))));
            tests.add(n(id + "withAuthPass-null", () -> ForTests.testNullReflective("password", () -> withAuth(conn, uy, null))));
            if (iurl) {
                tests.add(n(id + "withOptAuth1", () -> Assertions.assertEquals(nauth1, withOptAuth(conn, nauth3).optAuth().get())));
                tests.add(n(id + "withOptAuth2", () -> Assertions.assertEquals(Optional.empty(), withOptAuth(conn, Optional.empty()).optAuth())));
                tests.add(n(id + "withOptAuth-null", () -> ForTests.testNullReflective("optAuth", () -> withOptAuth(conn, null))));
            }
        } else {
            tests.add(n(id + "optAuth", () -> Assertions.assertTrue(optAuth(conn).isEmpty())));
        }
        if (database != null) {
            tests.add(n(id + "database", () -> Assertions.assertEquals(database, database(conn))));
            tests.add(n(id + "withDatabase" , () -> Assertions.assertEquals(database + "DDD", database(withDatabase(conn, database + "DDD")))));
            tests.add(n(id + "withDatabase-null" , () -> ForTests.testNullReflective("database", () -> withDatabase(conn, null))));
        }
        if (filename != null) {
            tests.add(n(id + "filename", () -> Assertions.assertEquals(filename, filename(conn))));
            tests.add(n(id + "withFilename", () -> Assertions.assertEquals(filename + "DDD", filename(withFilename(conn, filename + "DDD")))));
            tests.add(n(id + "withFilename-null", () -> ForTests.testNullReflective("filename", () -> withFilename(conn, null))));
        }
        if (directory != null) {
            tests.add(n(id + "dir", () -> Assertions.assertEquals(directory, directory(conn))));
            tests.add(n(id + "withDir", () -> Assertions.assertEquals(directory + "DDD", directory(withDirectory(conn, directory + "DDD")))));
            tests.add(n(id + "withDirectory-null", () -> ForTests.testNullReflective("directory", () -> withDirectory(conn, null))));
        }
        if (encoding != null) {
            tests.add(n(id + "withEncoding", () -> Assertions.assertEquals(encoding, encoding(conn))));
            tests.add(n(id + "withEncoding", () -> Assertions.assertEquals(encoding + "EEE", encoding(withEncoding(conn, encoding + "EEE")))));
            tests.add(n(id + "withEncoding-null", () -> ForTests.testNullReflective("encoding", () -> withEncoding(conn, null))));
        }
        if (ssl != null) {
            if (!List.of("true", "false").contains(ssl)) throw new AssertionError();
            var sslv = "true".equals(ssl);
            tests.add(n(id + "ssl", () -> Assertions.assertEquals(sslv, ssl(conn))));
            tests.add(n(id + "sslTrue", () -> Assertions.assertEquals(true, ssl(withSsl(conn, true)))));
            tests.add(n(id + "sslFalse", () -> Assertions.assertEquals(false, ssl(withSsl(conn, false)))));
        }
        if (rac != null) {
            if (!List.of("true", "false").contains(rac)) throw new AssertionError();
            var racv = "true".equals(rac);
            tests.add(n(id + "rac", () -> Assertions.assertEquals(racv, rac(conn))));
            tests.add(n(id + "racTrue", () -> Assertions.assertEquals(true, rac(withRac(conn, true)))));
            tests.add(n(id + "racFalse", () -> Assertions.assertEquals(false, rac(withRac(conn, false)))));
        }
        if (create != null) {
            if (!List.of("true", "false").contains(create)) throw new AssertionError();
            var createv = "true".equals(create);
            tests.add(n(id + "create", () -> Assertions.assertEquals(createv, create(conn))));
            tests.add(n(id + "createTrue", () -> Assertions.assertEquals(true, create(withCreate(conn, true)))));
            tests.add(n(id + "createFalse", () -> Assertions.assertEquals(false, create(withCreate(conn, false)))));
        }
        if (hashCode != null) {
            var hash = Integer.parseInt(hashCode);
            tests.add(n(id + "hashCode", () -> Assertions.assertEquals(hash, conn.hashCode())));
        }
        if (toString != null) {
            tests.add(n(id + "toString", () -> Assertions.assertEquals(toString, conn.toString())));
        }
    }

    private static record TestSet(Map<String, String> props, List<Supplier<Connector>> conns) {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    private static TestSet of(Map<String, String> props, Supplier<Connector>... conns) {
        return new TestSet(props, List.of(conns));
    }

    private static Stream<Arguments> addTestsConnectors(String db, TestSet... sets) {
        var tests = new ArrayList<NamedTest>(50);

        var sa = sets[0];
        var a = sa.conns().get(0);
        var i = 1;
        for (var t : sets) {
            addTestsConnector(db + " item " + i + ": ", tests, t.props(), t.conns().get(0).get());
            i++;
        }

        tests.add(n(db + " not equals to null", () -> Assertions.assertFalse(a.get().equals(null))));
        tests.add(n(db + " not equals to unrelated", () -> Assertions.assertFalse(a.get().equals("XXX"))));

        i = 1;
        for (var x : sa.conns()) {
            var j = i;
            tests.add(n("item 1-" + j + " equals",() -> Assertions.assertEquals(a.get(), x.get())));
            tests.add(n("item 1-" + j + " hashCode equals",() -> Assertions.assertEquals(a.get().hashCode(), x.get().hashCode())));
            tests.add(n("item 1-" + j + " toString equals",() -> Assertions.assertEquals(a.get().toString(), x.get().toString())));
            i++;
        }

        for (var bs : sets) {
            if (bs == sa) continue;
            for (var b : bs.conns()) {
                var j = i;
                tests.add(n("item 1-" + j + " not equals", () -> Assertions.assertNotEquals(a.get(), b.get())));
                tests.add(n("item 1-" + j + " toString not equals", () -> Assertions.assertNotEquals(a.get().toString(), b.get().toString())));
                i++;
            }
        }

        return tests.stream().map(NamedTest::args);
    }

    private static Stream<Arguments> testMariaDbProps() {
        var url1 = "jdbc:mariadb://localhost:3306/test";
        Supplier<Connector> obj1 = () -> new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var map1 = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "secret", "database", "test", "url", url1);

        var url2 = "jdbc:mariadb://10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new MariaDbConnector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2);

        Supplier<Connector> std = () -> MariaDbConnector.std();
        var urls = "jdbc:mariadb://localhost:3306/";
        var maps = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "admin", "database", "", "url", urls);
        return addTestsConnectors("MariaDB", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testMySqlProps() {
        var url1 = "jdbc:mysql://localhost:3306/test";
        Supplier<Connector> obj1 = () -> new MySqlConnector("localhost", 3306, "admin", "secret", "test");
        var map1 = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "secret", "database", "test", "url", url1);

        var url2 = "jdbc:mysql://10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new MySqlConnector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2);

        Supplier<Connector> std = () -> MySqlConnector.std();
        var urls = "jdbc:mysql://localhost:3306/";
        var maps = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "admin", "database", "", "url", urls);

        return addTestsConnectors("MySQL", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testSqlServerProps() {
        var url1 = "jdbc:hyperion:sqlserver://localhost:1433;DatabaseName=test";
        Supplier<Connector> obj1 = () -> new SqlServerConnector("localhost", 1433, "admin", "secret", "test");
        var map1 = Map.of("host", "localhost", "port", "1433", "user", "admin", "password", "secret", "database", "test", "url", url1);

        var url2 = "jdbc:hyperion:sqlserver://10.0.0.1:5555;DatabaseName=sample";
        Supplier<Connector> obj2 = () -> new SqlServerConnector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2);

        Supplier<Connector> std = () -> SqlServerConnector.std();
        var urls = "jdbc:hyperion:sqlserver://localhost:1433;DatabaseName=";
        var maps = Map.of("host", "localhost", "port", "1433", "user", "admin", "password", "admin", "database", "", "url", urls);

        return addTestsConnectors("SQL Server", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testOracleProps() {
        var url1 = "jdbc:oracle:thin:@localhost:1521:test";
        Supplier<Connector> obj1 = () -> new OracleConnector("localhost", 1521, "admin", "secret", "test", false);
        var map1 = Map.of(
                "host", "localhost", "port", "1521", "user", "admin", "password", "secret", "database", "test", "url", url1, "rac", "false"
        );

        var url2 = "jdbc:oracle:thin:@//10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new OracleConnector("10.0.0.1", 5555, "master", "pa$$", "sample", true);
        var map2 = Map.of(
                "host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2, "rac", "true"
        );

        Supplier<Connector> std = () -> OracleConnector.std();
        var urls = "jdbc:oracle:thin:@localhost:1521:";
        var maps = Map.of(
                "host", "localhost", "port", "1521", "user", "admin", "password", "admin", "database", "", "url", urls, "rac", "false"
        );

        return addTestsConnectors("Oracle", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testPostgreSqlProps() {
        var url1 = "jdbc:postgresql://localhost:5432/test";
        Supplier<Connector> obj1 = () -> new PostgreSqlConnector("localhost", 5432, "admin", "secret", "test", false);
        var map1 = Map.of(
                "host", "localhost", "port", "5432", "user", "admin", "password", "secret", "database", "test", "url", url1, "ssl", "false"
        );

        var url2 = "jdbc:postgresql://10.0.0.1:5555/sample?ssl=true";
        Supplier<Connector> obj2 = () -> new PostgreSqlConnector("10.0.0.1", 5555, "master", "pa$$", "sample", true);
        var map2 = Map.of(
                "host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2, "ssl", "true"
        );

        Supplier<Connector> std = () -> PostgreSqlConnector.std();
        var urls = "jdbc:postgresql://localhost:5432/?ssl=true";
        var maps = Map.of(
                "host", "localhost", "port", "5432", "user", "admin", "password", "admin", "database", "", "url", urls, "ssl", "true"
        );

        return addTestsConnectors("PostgreSQL", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testDb2Props() {
        var url1 = "jdbc:db2://localhost:50000/test";
        Supplier<Connector> obj1 = () -> new Db2Connector("localhost", 50000, "admin", "secret", "test");
        var map1 = Map.of(
                "host", "localhost", "port", "50000", "user", "admin", "password", "secret", "database", "test", "url", url1
        );

        var url2 = "jdbc:db2://10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new Db2Connector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of(
                "host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2
        );

        Supplier<Connector> std = () -> Db2Connector.std();
        var urls = "jdbc:db2://localhost:50000/";
        var maps = Map.of(
                "host", "localhost", "port", "50000", "user", "admin", "password", "admin", "database", "", "url", urls
        );

        return addTestsConnectors("DB2", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testFirebirdProps() {
        var url1 = "jdbc:firebird://localhost:3050/test";
        Supplier<Connector> obj1 = () -> new FirebirdConnector("localhost", 3050, "admin", "secret", "test", "");
        var map1 = Map.of(
                "host", "localhost", "port", "3050", "user", "admin", "password", "secret", "filename", "test", "url", url1,
                "encoding", ""
        );

        var url2 = "jdbc:firebird://10.0.0.1:5555/sample?encoding=UTF8";
        Supplier<Connector> obj2 = () -> new FirebirdConnector("10.0.0.1", 5555, "master", "pa$$", "sample", "UTF8");
        var map2 = Map.of(
                "host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "filename", "sample", "url", url2,
                "encoding", "UTF8"
        );

        Supplier<Connector> std = () -> FirebirdConnector.std();
        var urls = "jdbc:firebird://localhost:3050/?encoding=UTF8";
        var maps = Map.of(
                "host", "localhost", "port", "3050", "user", "SYSDBA", "password", "masterkey", "filename", "", "url", urls,
                "encoding", "UTF8"
        );

        return addTestsConnectors("Firebird", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testSqliteProps() {
        var url1 = "jdbc:sqlite:test.db";
        Supplier<Connector> obj1 = () -> new SqliteConnector("test.db");
        var map1 = Map.of("filename", "test.db", "url", url1);

        var url2 = "jdbc:sqlite:sample.db";
        Supplier<Connector> obj2 = () -> new SqliteConnector("sample.db");
        var map2 = Map.of("filename", "sample.db", "url", url2);

        Supplier<Connector> std = () -> SqliteConnector.std();
        var urls = "jdbc:sqlite:";
        var maps = Map.of("filename", "", "url", urls);

        return addTestsConnectors("Sqlite", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testAccessProps() {
        var url1 = "jdbc:ucanaccess:test.mdb";
        Supplier<Connector> obj1 = () -> new AccessConnector("test.mdb");
        var map1 = Map.of("filename", "test.mdb", "url", url1);

        var url2 = "jdbc:ucanaccess:sample.mdb";
        Supplier<Connector> obj2 = () -> new AccessConnector("sample.mdb");
        var map2 = Map.of("filename", "sample.mdb", "url", url2);

        Supplier<Connector> std = () -> AccessConnector.std();
        var urls = "jdbc:ucanaccess:";
        var maps = Map.of("filename", "", "url", urls);

        return addTestsConnectors("Access", of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    private static Stream<Arguments> testHsqldbProps() {
        var url1 = "jdbc:hsqldb:file://test.db";
        Supplier<Connector> obj1 = () -> new HsqldbConnector("admin", "secret", "test.db", false);
        var map1 = Map.of("user", "admin", "password", "secret", "filename", "test.db", "url", url1);

        var url2 = "jdbc:hsqldb:file://sample.db";
        Supplier<Connector> obj2 = () -> new HsqldbConnector("master", "pa$$", "sample.db", false);
        var map2 = Map.of("user", "master", "password", "pa$$", "filename", "sample.db", "url", url2);

        var url3 = "jdbc:hsqldb:mem:sample";
        Supplier<Connector> obj3 = () -> new HsqldbConnector("master", "pa$$", "sample", true);
        var map3 = Map.of("user", "master", "password", "pa$$", "filename", "sample", "url", url3);

        var url4 = "jdbc:hsqldb:mem:";
        Supplier<Connector> obj4 = () -> new HsqldbConnector("master", "pa$$", "", true);
        var map4 = Map.of("user", "master", "password", "pa$$", "filename", "", "url", url4);

        Supplier<Connector> std = () -> HsqldbConnector.std();
        var urls = "jdbc:hsqldb:file://";
        var maps = Map.of("user", "SA", "password", "password", "filename", "", "url", urls);

        return addTestsConnectors("HSQLDB", of(map1, obj1, obj1), of(map2, obj2), of(map3, obj3), of(map4, obj4), of(maps, std));
    }

    private static Stream<Arguments> testH2Props() {
        var url1 = "jdbc:h2:~/test.db";
        Supplier<Connector> obj1 = () -> new H2Connector("admin", "secret", "test.db", false);
        var map1 = Map.of("user", "admin", "password", "secret", "filename", "test.db", "url", url1);

        var url2 = "jdbc:h2:~/sample.db";
        Supplier<Connector> obj2 = () -> new H2Connector("master", "pa$$", "sample.db", false);
        var map2 = Map.of("user", "master", "password", "pa$$", "filename", "sample.db", "url", url2);

        var url3 = "jdbc:h2:mem:sample";
        Supplier<Connector> obj3 = () -> new H2Connector("master", "pa$$", "sample", true);
        var map3 = Map.of("user", "master", "password", "pa$$", "filename", "sample", "url", url3);

        var url4 = "jdbc:h2:mem:";
        Supplier<Connector> obj4 = () -> new H2Connector("master", "pa$$", "", true);
        var map4 = Map.of("user", "master", "password", "pa$$", "filename", "", "url", url4);

        Supplier<Connector> std = () -> H2Connector.std();
        var urls = "jdbc:h2:~/";
        var maps = Map.of("user", "sa", "password", "password", "filename", "", "url", urls);

        return addTestsConnectors("H2", of(map1, obj1, obj1), of(map2, obj2), of(map3, obj3), of(map4, obj4), of(maps, std));
    }

    private static Stream<Arguments> testBareUrl() {
        var url1 = "jdbc:test:blabla";
        Supplier<Connector> obj1a = () -> new UrlConnector(url1);
        Supplier<Connector> obj1b = () -> new UrlConnector(url1, Optional.empty());
        var map1 = Map.of("url", url1);

        var url2 = "jdbc:test:daadaa";
        Supplier<Connector> obj2a = () -> new UrlConnector(url2, "foo", "bar");
        Supplier<Connector> obj2b = () -> new UrlConnector(url2, Optional.of(new Connector.Auth("foo", "bar")));
        var map2 = Map.of("url", url2, "user", "foo", "password", "bar");

        Supplier<Connector> std = () -> UrlConnector.std();
        var maps = Map.of("url", "");

        return addTestsConnectors("URL", of(map1, obj1a, obj1b), of(map2, obj2a, obj2b), of(maps, std));
    }

    @MethodSource({
        "testMariaDbProps", "testMySqlProps",
        "testSqlServerProps", "testOracleProps", "testPostgreSqlProps", "testDb2Props", "testFirebirdProps",
        "testSqliteProps", "testAccessProps", "testHsqldbProps", "testH2Props", "testBareUrl"
    })
    @ParameterizedTest(name = "{0}")
    public void propertyTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
