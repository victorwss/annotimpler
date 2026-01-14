package ninja.javahacker.test.annotimpler.sql.conn;

import lombok.NonNull;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class ConnectionPropertyTests {

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

    private static LocalDerbyConnector.DerbyType subsubprotocol(Object conn) throws Exception {
        return (LocalDerbyConnector.DerbyType) get(conn, "subsubprotocol");
    }

    @SuppressWarnings("unchecked")
    private static <E> E withSubsubprotocol(E conn, LocalDerbyConnector.DerbyType value) throws Exception {
        return (E) set(conn, "withSubsubprotocol", LocalDerbyConnector.DerbyType.class, value);
    }

    private static void addTestsConnector(
            @NonNull String ident,
            @NonNull List<Executable> tests,
            @NonNull Map<String, String> props,
            @NonNull Connector conn)
            throws Exception
    {
        var id = ident + " ";
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
        var subsubprotocol = props.get("subsubprotocol");
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

        for (var n : all) {
            if (List.of("hashCode", "toString").contains(n)) continue;
            try {
                conn.getClass().getMethod(n);
                if (!keys.contains(n)) throw new AssertionError(n);
            } catch (NoSuchMethodException e) {
                if (keys.contains(n)) {
                    if (List.of("user", "password").contains(n) && conn instanceof UrlConnector) continue;
                    throw new AssertionError(e);
                }
            }
        }

        if (url != null) {
            tests.add(() -> Assertions.assertEquals(url, url(conn), id + "url1"));
            tests.add(() -> Assertions.assertEquals(url, asUrl(conn).url(), id + "url2"));
            if (conn instanceof UrlConnector) {
                tests.add(() -> Assertions.assertEquals(url + "UUU", url(withUrl(conn, url + "UUU")), id + "withUrl"));
                tests.add(() -> ForTests.testNullReflective("url", () -> withUrl(conn, null), "withUrl-null"));
            }
        }
        if (host != null) {
            tests.add(() -> Assertions.assertEquals(host, host(conn), id + "host"));
            tests.add(() -> Assertions.assertEquals(host + "GGG", host(withHost(conn, host + "GGG")), id + "withHost"));
            tests.add(() -> ForTests.testNullReflective("host", () -> withHost(conn, null), "withHost-null"));
        }
        if (port != null) {
            int portv = Integer.parseInt(port);
            tests.add(() -> Assertions.assertEquals(portv, port(conn), id + "port"));
            tests.add(() -> Assertions.assertEquals(portv + 5, port(withPort(conn, portv + 5)), id + "withPort"));
        }
        if (user != null) {
            var oauth = new Connector.Auth(user, pass);
            var uy = user + "YYY";
            var pz = pass + "ZZZ";
            var nauth1 = new Connector.Auth(uy, pz);
            var nauth2 = new Connector.Auth(uy, pz);
            var nauth3 = Optional.of(nauth1);
            var iurl = conn instanceof UrlConnector;
            if (!iurl) tests.add(() -> Assertions.assertEquals(user, user(conn), id + "user"));
            if (!iurl) tests.add(() -> Assertions.assertEquals(pass, pass(conn), id + "password"));
            if (!iurl) tests.add(() -> Assertions.assertEquals(oauth, auth(conn), id + "auth"));
            tests.add(() -> Assertions.assertEquals(user, optUser(conn).get(), id + "optUser"));
            tests.add(() -> Assertions.assertEquals(pass, optPass(conn).get(), id + "optPass"));
            tests.add(() -> Assertions.assertEquals(user, asUrl(conn).optUser().get(), id + "urlOptUser"));
            tests.add(() -> Assertions.assertEquals(pass, asUrl(conn).optPassword().get(), id + "urlOptUser"));
            tests.add(() -> Assertions.assertEquals(oauth, optAuth(conn).get(), id + "optAuth"));
            tests.add(() -> Assertions.assertEquals(oauth, asUrl(conn).optAuth().get(), id + "urlOptAuth"));
            if (!iurl) tests.add(() -> Assertions.assertEquals(uy, user(withUser(conn, uy)), id + "withUser"));
            if (!iurl) tests.add(() -> Assertions.assertEquals(pz, pass(withPass(conn, pz)), id + "withPass"));
            if (!iurl) tests.add(() -> Assertions.assertEquals(nauth1, auth(withAuth(conn, nauth2)), id + "withAuth1"));
            if (!iurl) tests.add(() -> Assertions.assertEquals(nauth1, auth(withAuth(conn, uy, pz)), id + "withAuth2"));
            tests.add(() -> Assertions.assertEquals(nauth1, withAuth(conn, nauth2).optAuth().get(), id + "withAuth3"));
            tests.add(() -> Assertions.assertEquals(nauth1, withAuth(conn, uy, pz).optAuth().get(), id + "withAuth4"));
            tests.add(() -> Assertions.assertEquals(nauth1, asUrl(conn).withAuth(nauth2).optAuth().get(), id + "withAuth5"));
            tests.add(() -> Assertions.assertEquals(nauth1, asUrl(conn).withAuth(uy, pz).optAuth().get(), id + "withAuth6"));
            tests.add(() -> Assertions.assertEquals(nauth1, withAuth(conn, nauth2).asUrl().optAuth().get(), id + "withAuth7"));
            tests.add(() -> Assertions.assertEquals(nauth1, withAuth(conn, uy, pz).asUrl().optAuth().get(), id + "withAuth8"));
            tests.add(() -> Assertions.assertEquals(nauth2, asUrl(conn).withOptAuth(nauth3).optAuth().get(), id + "withAuth9"));
            tests.add(() -> Assertions.assertTrue(asUrl(conn).withOptAuth(Optional.empty()).optAuth().isEmpty(), id + "withAuth10"));
            tests.add(() -> Assertions.assertTrue(asUrl(conn).withNoAuth().optAuth().isEmpty(), id + "withNoAuth"));
            if (!iurl) tests.add(() -> ForTests.testNullReflective("user", () -> withUser(conn, null), id + "withUser-null"));
            if (!iurl) tests.add(() -> ForTests.testNullReflective("password", () -> withPass(conn, null), id + "withPass-null"));
            tests.add(() -> ForTests.testNullReflective("auth", () -> withAuth(conn, null), id + "withAuth-null"));
            tests.add(() -> ForTests.testNullReflective("user", () -> withAuth(conn, null, pz), id + "withAuthUser-null"));
            tests.add(() -> ForTests.testNullReflective("password", () -> withAuth(conn, uy, null), id + "withAuthPass-null"));
            if (iurl) {
                tests.add(() -> Assertions.assertEquals(nauth1, withOptAuth(conn, nauth3).optAuth().get(), id + "withOptAuth1"));
                tests.add(() -> Assertions.assertEquals(Optional.empty(), withOptAuth(conn, Optional.empty()).optAuth(), id + "withOptAuth2"));
                tests.add(() -> ForTests.testNullReflective("optAuth", () -> withOptAuth(conn, null), id + "withOptAuth-null"));
            }
        } else {
            tests.add(() -> Assertions.assertTrue(optAuth(conn).isEmpty(), id + "optAuth"));
        }
        if (database != null) {
            tests.add(() -> Assertions.assertEquals(database, database(conn), id + "database"));
            tests.add(() -> Assertions.assertEquals(database + "DDD", database(withDatabase(conn, database + "DDD")), id + "withDatabase"));
            tests.add(() -> ForTests.testNullReflective("database", () -> withDatabase(conn, null), "withDatabase-null"));
        }
        if (filename != null) {
            tests.add(() -> Assertions.assertEquals(filename, filename(conn), id + "filename"));
            tests.add(() -> Assertions.assertEquals(filename + "DDD", filename(withFilename(conn, filename + "DDD")), id + "withFilename"));
            tests.add(() -> ForTests.testNullReflective("filename", () -> withFilename(conn, null), "withFilename-null"));
        }
        if (directory != null) {
            tests.add(() -> Assertions.assertEquals(directory, directory(conn), id + "dir"));
            tests.add(() -> Assertions.assertEquals(directory + "DDD", directory(withDirectory(conn, directory + "DDD")), id + "withDir"));
            tests.add(() -> ForTests.testNullReflective("directory", () -> withDirectory(conn, null), "withDirectory-null"));
        }
        if (encoding != null) {
            tests.add(() -> Assertions.assertEquals(encoding, encoding(conn), id + "withEncoding"));
            tests.add(() -> Assertions.assertEquals(encoding + "EEE", encoding(withEncoding(conn, encoding + "EEE")), id + "withEncoding"));
            tests.add(() -> ForTests.testNullReflective("encoding", () -> withEncoding(conn, null), "withEncoding-null"));
        }
        if (ssl != null) {
            if (!List.of("true", "false").contains(ssl)) throw new AssertionError();
            var sslv = "true".equals(ssl);
            tests.add(() -> Assertions.assertEquals(sslv, ssl(conn), id + "ssl"));
            tests.add(() -> Assertions.assertEquals(true, ssl(withSsl(conn, true)), id + "sslTrue"));
            tests.add(() -> Assertions.assertEquals(false, ssl(withSsl(conn, false)), id + "sslFalse"));
        }
        if (rac != null) {
            if (!List.of("true", "false").contains(rac)) throw new AssertionError();
            var racv = "true".equals(rac);
            tests.add(() -> Assertions.assertEquals(racv, rac(conn), id + "rac"));
            tests.add(() -> Assertions.assertEquals(true, rac(withRac(conn, true)), id + "racTrue"));
            tests.add(() -> Assertions.assertEquals(false, rac(withRac(conn, false)), id + "racFalse"));
        }
        if (create != null) {
            if (!List.of("true", "false").contains(create)) throw new AssertionError();
            var createv = "true".equals(create);
            tests.add(() -> Assertions.assertEquals(createv, create(conn), id + "create"));
            tests.add(() -> Assertions.assertEquals(true, create(withCreate(conn, true)), id + "createTrue"));
            tests.add(() -> Assertions.assertEquals(false, create(withCreate(conn, false)), id + "createFalse"));
        }
        if (subsubprotocol != null) {
            var subs = LocalDerbyConnector.DerbyType.valueOf(subsubprotocol);
            var other = LocalDerbyConnector.DerbyType.values()[(subs.ordinal() + 2) % 5];
            tests.add(() -> Assertions.assertEquals(subs, subsubprotocol(conn), id + "subsubprotocol"));
            tests.add(() -> Assertions.assertEquals(other, subsubprotocol(withSubsubprotocol(conn, other)), id + "withSubsubprotocol"));
            tests.add(() -> ForTests.testNullReflective("subsubprotocol", () -> withSubsubprotocol(conn, null), "withSubsubprotocol-null"));
        }
        if (hashCode != null) {
            var hash = Integer.parseInt(hashCode);
            tests.add(() -> Assertions.assertEquals(hash, conn.hashCode(), id + "hashCode"));
        }
        if (toString != null) {
            tests.add(() -> Assertions.assertEquals(toString, conn.toString(), id + "toString"));
        }
    }

    private static record TestSet(Map<String, String> props, List<Supplier<Connector>> conns) {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    private static TestSet of(Map<String, String> props, Supplier<Connector>... conns) {
        return new TestSet(props, List.of(conns));
    }

    private static void addTestsConnectors(@NonNull TestSet... sets) throws Exception {
        var tests = new ArrayList<Executable>(50);

        var sa = sets[0];
        var a = sa.conns().get(0);
        var i = 1;
        for (var t : sets) {
            addTestsConnector("item " + i, tests, t.props(), t.conns().get(0).get());
            i++;
        }

        tests.add(() -> Assertions.assertFalse(a.get().equals(null), "not equals to null"));
        tests.add(() -> Assertions.assertFalse(a.get().equals("XXX"), "not equals to unrelated"));

        i = 1;
        for (var x : sa.conns()) {
            var j = i;
            tests.add(() -> Assertions.assertEquals(a.get(), x.get(), "item 1-" + j + " equals"));
            tests.add(() -> Assertions.assertEquals(a.get().hashCode(), x.get().hashCode(), "item 1-" + j + " hashCode equals"));
            tests.add(() -> Assertions.assertEquals(a.get().toString(), x.get().toString(), "item 1-" + j + " toString equals"));
            i++;
        }

        for (var bs : sets) {
            if (bs == sa) continue;
            for (var b : bs.conns()) {
                var j = i;
                tests.add(() -> Assertions.assertNotEquals(a.get(), b.get(), "item 1-" + j + " not equals"));
                tests.add(() -> Assertions.assertNotEquals(a.get().toString(), b.get().toString(), "item 1-" + j + " toString not equals"));
                i++;
            }
        }

        Assertions.assertAll(tests);
    }

    @Test
    public void testMariaDbProps() throws Exception {
        var url1 = "jdbc:mariadb://localhost:3306/test";
        Supplier<Connector> obj1 = () -> new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var map1 = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "secret", "database", "test", "url", url1);

        var url2 = "jdbc:mariadb://10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new MariaDbConnector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2);

        Supplier<Connector> std = () -> MariaDbConnector.std();
        var urls = "jdbc:mariadb://localhost:3306/";
        var maps = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "admin", "database", "", "url", urls);
        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testMySqlProps() throws Exception {
        var url1 = "jdbc:mysql://localhost:3306/test";
        Supplier<Connector> obj1 = () -> new MySqlConnector("localhost", 3306, "admin", "secret", "test");
        var map1 = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "secret", "database", "test", "url", url1);

        var url2 = "jdbc:mysql://10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new MySqlConnector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2);

        Supplier<Connector> std = () -> MySqlConnector.std();
        var urls = "jdbc:mysql://localhost:3306/";
        var maps = Map.of("host", "localhost", "port", "3306", "user", "admin", "password", "admin", "database", "", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testSqlServerProps() throws Exception {
        var url1 = "jdbc:hyperion:sqlserver://localhost:1433;DatabaseName=test";
        Supplier<Connector> obj1 = () -> new SqlServerConnector("localhost", 1433, "admin", "secret", "test");
        var map1 = Map.of("host", "localhost", "port", "1433", "user", "admin", "password", "secret", "database", "test", "url", url1);

        var url2 = "jdbc:hyperion:sqlserver://10.0.0.1:5555;DatabaseName=sample";
        Supplier<Connector> obj2 = () -> new SqlServerConnector("10.0.0.1", 5555, "master", "pa$$", "sample");
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "user", "master", "password", "pa$$", "database", "sample", "url", url2);

        Supplier<Connector> std = () -> SqlServerConnector.std();
        var urls = "jdbc:hyperion:sqlserver://localhost:1433;DatabaseName=";
        var maps = Map.of("host", "localhost", "port", "1433", "user", "admin", "password", "admin", "database", "", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testOracleProps() throws Exception {
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

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testPostgreSqlProps() throws Exception {
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

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testFirebirdProps() throws Exception {
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

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testSqliteProps() throws Exception {
        var url1 = "jdbc:sqlite:test.db";
        Supplier<Connector> obj1 = () -> new SqliteConnector("test.db");
        var map1 = Map.of("filename", "test.db", "url", url1);

        var url2 = "jdbc:sqlite:sample.db";
        Supplier<Connector> obj2 = () -> new SqliteConnector("sample.db");
        var map2 = Map.of("filename", "sample.db", "url", url2);

        Supplier<Connector> std = () -> SqliteConnector.std();
        var urls = "jdbc:sqlite:";
        var maps = Map.of("filename", "", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testAccessProps() throws Exception {
        var url1 = "jdbc:ucanaccess:test.mdb";
        Supplier<Connector> obj1 = () -> new AccessConnector("test.mdb");
        var map1 = Map.of("filename", "test.mdb", "url", url1);

        var url2 = "jdbc:ucanaccess:sample.mdb";
        Supplier<Connector> obj2 = () -> new AccessConnector("sample.mdb");
        var map2 = Map.of("filename", "sample.mdb", "url", url2);

        Supplier<Connector> std = () -> AccessConnector.std();
        var urls = "jdbc:ucanaccess:";
        var maps = Map.of("filename", "", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testHsqldbProps() throws Exception {
        var url1 = "jdbc:hsqldb:file://test.db";
        Supplier<Connector> obj1 = () -> new HsqldbConnector("admin", "secret", "test.db");
        var map1 = Map.of("user", "admin", "password", "secret", "filename", "test.db", "url", url1);

        var url2 = "jdbc:hsqldb:file://sample.db";
        Supplier<Connector> obj2 = () -> new HsqldbConnector("master", "pa$$", "sample.db");
        var map2 = Map.of("user", "master", "password", "pa$$", "filename", "sample.db", "url", url2);

        Supplier<Connector> std = () -> HsqldbConnector.std();
        var urls = "jdbc:hsqldb:file://";
        var maps = Map.of("user", "SA", "password", "password", "filename", "", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testH2Props() throws Exception {
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

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(map3, obj3), of(map4, obj4), of(maps, std));
    }

    @Test
    public void testLocalDerbyProps() throws Exception {
        var url1 = "jdbc:derby:directory:C:\\test?create=true";
        Supplier<Connector> obj1 = () -> new LocalDerbyConnector("C:\\test", LocalDerbyConnector.DerbyType.DIRECTORY, true);
        var map1 = Map.of("directory", "C:\\test", "subsubprotocol", "DIRECTORY", "create", "true", "url", url1);

        var url2 = "jdbc:derby:jar:sample";
        Supplier<Connector> obj2 = () -> new LocalDerbyConnector("sample", LocalDerbyConnector.DerbyType.JAR, false);
        var map2 = Map.of("directory", "sample", "subsubprotocol", "JAR", "create", "false", "url", url2);

        var url3 = "jdbc:derby:memory:sketch?create=true";
        Supplier<Connector> obj3 = () -> new LocalDerbyConnector("sketch", LocalDerbyConnector.DerbyType.MEMORY, true);
        var map3 = Map.of("directory", "sketch", "subsubprotocol", "MEMORY", "create", "true", "url", url3);

        var url4 = "jdbc:derby:classpath:/foo";
        Supplier<Connector> obj4 = () -> new LocalDerbyConnector("/foo", LocalDerbyConnector.DerbyType.CLASSPATH, false);
        var map4 = Map.of("directory", "/foo", "subsubprotocol", "CLASSPATH", "create", "false", "url", url4);

        Supplier<Connector> std = () -> LocalDerbyConnector.std();
        var urls = "jdbc:derby:";
        var maps = Map.of("directory", "", "subsubprotocol", "DEFAULT", "create", "false", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(map3, obj3), of(map4, obj4), of(maps, std));
    }

    @Test
    public void testRemoteDerbyProps() throws Exception {
        var url1 = "jdbc:derby://localhost:1527/test?create=true";
        Supplier<Connector> obj1 = () -> new RemoteDerbyConnector("localhost", 1527, "test", true);
        var map1 = Map.of("host", "localhost", "port", "1527", "directory", "test", "create", "true", "url", url1);

        var url2 = "jdbc:derby://10.0.0.1:5555/sample";
        Supplier<Connector> obj2 = () -> new RemoteDerbyConnector("10.0.0.1", 5555, "sample", false);
        var map2 = Map.of("host", "10.0.0.1", "port", "5555", "directory", "sample", "create", "false", "url", url2);

        Supplier<Connector> std = () -> RemoteDerbyConnector.std();
        var urls = "jdbc:derby://localhost:1527/";
        var maps = Map.of("host", "localhost", "port", "1527", "directory", "", "create", "false", "url", urls);

        addTestsConnectors(of(map1, obj1, obj1), of(map2, obj2), of(maps, std));
    }

    @Test
    public void testBareUrl() throws Exception {
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

        addTestsConnectors(of(map1, obj1a, obj1b), of(map2, obj2a, obj2b), of(maps, std));
    }
}
