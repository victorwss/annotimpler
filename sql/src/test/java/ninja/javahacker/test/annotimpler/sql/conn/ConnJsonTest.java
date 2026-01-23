package ninja.javahacker.test.annotimpler.sql.conn;

import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module com.fasterxml.jackson.core;
import module com.fasterxml.jackson.databind;
import module com.fasterxml.jackson.datatype.jdk8;
import module com.fasterxml.jackson.module.paramnames;
import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "ThrowableResultIgnored"})
public class ConnJsonTest {

    private static final String JSON = """
            {
                "type": "mariadb",
                "host": "localhost",
                "port": 3306,
                "user": "admin",
                "password": "secret",
                "database": "test"
            }""";

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    public static ObjectMapper mapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule());
    }

    @Test
    public void testReadJson1() throws IOException {
        var con = mapper().readValue(JSON, JsonConnector.class);
        var delegate = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        Assertions.assertAll(
                () -> Assertions.assertEquals(new JsonConnector(delegate), con),
                () -> Assertions.assertEquals(delegate, con.delegate())
        );
    }

    @Test
    public void testReadJson2() throws IOException {
        var con = JsonConnector.read(JSON);
        var delegate = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        Assertions.assertAll(
                () -> Assertions.assertEquals(new JsonConnector(delegate), con),
                () -> Assertions.assertEquals(delegate, con.delegate())
        );
    }

    @Test
    public void testWriteJson() throws IOException {
        var inJson = JSON.replace(" ", "").replace("\r", "").replace("\n", "");
        var delegate = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var jsc = new JsonConnector(delegate);
        var outJson = mapper().writeValueAsString(jsc);
        Assertions.assertAll(
                () -> Assertions.assertEquals(inJson, outJson),
                () -> Assertions.assertEquals(inJson, jsc.toJson())
        );
    }

    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    private static Stream<Arguments> testEqualsHashCodeToString() {
        var delegate1 = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var delegate2 = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var jsc1 = new JsonConnector(delegate1);
        var jsc2 = new JsonConnector(delegate2);
        var jsc3 = new JsonConnector(delegate1);
        var delegate3 = new MySqlConnector("10.0.0.1", 4444, "dba", "$$$$", "sample");
        var jsc4 = new JsonConnector(delegate3);
        return Stream.of(
                n("equals to self"             , () -> Assertions.assertTrue(jsc1.equals(jsc1))),
                n("equals to shallow copy"     , () -> Assertions.assertTrue(jsc1.equals(jsc3))),
                n("equals reflexive shallow"   , () -> Assertions.assertTrue(jsc3.equals(jsc1))),
                n("equals to deep copy"        , () -> Assertions.assertTrue(jsc1.equals(jsc2))),
                n("equals reflexive deep"      , () -> Assertions.assertTrue(jsc2.equals(jsc1))),
                n("delegate is same"           , () -> Assertions.assertEquals(jsc1.delegate(), jsc2.delegate())),
                n("delegates are equals"       , () -> Assertions.assertEquals(jsc1.delegate(), jsc3.delegate())),
                n("same delegate hashCode"     , () -> Assertions.assertEquals(delegate1.hashCode(), jsc1.hashCode())),
                n("distinct hashCode"          , () -> Assertions.assertNotEquals(jsc1.hashCode(), jsc4.hashCode())),
                n("equals has same hashCode"   , () -> Assertions.assertEquals(jsc1.hashCode(), jsc2.hashCode())),
                n("not equals"                 , () -> Assertions.assertFalse(jsc1.equals(jsc4))),
                n("not equals reflexive"       , () -> Assertions.assertFalse(jsc4.equals(jsc1))),
                n("not equals delegates"       , () -> Assertions.assertNotEquals(jsc1.delegate(), jsc4.delegate())),
                n("not equals to unrelated"    , () -> Assertions.assertFalse(jsc1.equals("x"))),
                n("not equals to null"         , () -> Assertions.assertFalse(jsc1.equals(null))),
                n("toString 1"                 , () -> Assertions.assertEquals("JSON Connector: [" + delegate1.toString() + "]", jsc1.toString())),
                n("toString 2"                 , () -> Assertions.assertEquals("JSON Connector: [" + delegate1.toString() + "]", jsc2.toString())),
                n("toString 3"                 , () -> Assertions.assertEquals("JSON Connector: [" + delegate3.toString() + "]", jsc4.toString())),
                n("same url as delegate 1"     , () -> Assertions.assertEquals(delegate1.url(), jsc1.url())),
                n("same user as delegate 1"    , () -> Assertions.assertEquals(delegate1.optUser(), jsc1.optUser())),
                n("same password as delegate 1", () -> Assertions.assertEquals(delegate1.optPassword(), jsc1.optPassword())),
                n("same auth as delegate 1"    , () -> Assertions.assertEquals(delegate1.optAuth(), jsc1.optAuth())),
                n("same urlconn as delegate 1" , () -> Assertions.assertEquals(delegate1.asUrl(), jsc1.asUrl())),
                n("same url as delegate 2"     , () -> Assertions.assertEquals(delegate3.url(), jsc4.url())),
                n("same user as delegate 2"    , () -> Assertions.assertEquals(delegate3.optUser(), jsc4.optUser())),
                n("same password as delegate 2", () -> Assertions.assertEquals(delegate3.optPassword(), jsc4.optPassword())),
                n("same auth as delegate 2"    , () -> Assertions.assertEquals(delegate3.optAuth(), jsc4.optAuth())),
                n("same urlconn as delegate 2" , () -> Assertions.assertEquals(delegate3.asUrl(), jsc4.asUrl()))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testEqualsHashCodeToString {0}")
    public void testEqualsHashCodeToString(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static class BadConnector implements Connector {
        @Override
        public String url() {
            throw new AssertionError();
        }

        @Override
        public Optional<Auth> optAuth() {
            throw new AssertionError();
        }
    }

    @Test
    public void testReadBadJson1() throws JsonProcessingException {
        var bad = JSON.replace("mariadb", "bad");
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var ex = Assertions.assertThrows(JsonMappingException.class, () -> mapper().readValue(bad, JsonConnector.class));
        Assertions.assertAll(
                //() -> Assertions.assertEquals(JsonConnector.UnknownConnectorException.class, ex.getCause().getClass()),
                //() -> Assertions.assertEquals("Unknown connector \"bad\".", ex.getCause().getMessage()),
                () -> Assertions.assertTrue(ex.getMessage().contains("IOException")),
                () -> Assertions.assertTrue(ex.getMessage().contains(name1)),
                () -> Assertions.assertTrue(ex.getMessage().contains("Unknown connector: \"bad\"."))
        );
    }

    @Test
    public void testReadBadJson2() throws JsonProcessingException {
        Assertions.assertThrows(JsonProcessingException.class, () -> JsonConnector.read(""));
    }

    @Test
    public void testReadBadJson3() throws JsonProcessingException {
        Assertions.assertThrows(JsonProcessingException.class, () -> JsonConnector.read("{["));
    }

    @Test
    public void testReadBadJson4() throws JsonProcessingException {
        var bad = "{\"blah\":46,\"wha\":[]}";
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var ex = Assertions.assertThrows(JsonMappingException.class, () -> JsonConnector.read(bad));
        Assertions.assertAll(
                //() -> Assertions.assertEquals(JsonConnector.UnknownConnectorException.class, ex.getCause().getClass()),
                //() -> Assertions.assertEquals("Unknown connector \"bad\".", ex.getCause().getMessage()),
                () -> Assertions.assertTrue(ex.getMessage().contains("IOException")),
                () -> Assertions.assertTrue(ex.getMessage().contains(name1)),
                () -> Assertions.assertTrue(ex.getMessage().contains("JSON has no type field."))
        );
    }

    @Test
    public void testReadBadJson5() throws JsonProcessingException {
        var bad2 = JSON.substring(0, JSON.length() - 1) + ",\"wtf\":\"oops\"}";
        Assertions.assertThrows(JsonProcessingException.class, () -> JsonConnector.read(bad2));
    }

    @Test
    public void testReadBadJson6() throws JsonProcessingException {
        var bad = "[1,2,3]";
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var ex = Assertions.assertThrows(JsonMappingException.class, () -> JsonConnector.read(bad));
        Assertions.assertAll(
                //() -> Assertions.assertEquals(JsonConnector.UnknownConnectorException.class, ex.getCause().getClass()),
                //() -> Assertions.assertEquals("Unknown connector \"bad\".", ex.getCause().getMessage()),
                () -> Assertions.assertTrue(ex.getMessage().contains("IOException")),
                () -> Assertions.assertTrue(ex.getMessage().contains(name1)),
                () -> Assertions.assertTrue(ex.getMessage().contains("JSON does not contain a connector."))
        );
    }

    @Test
    public void testReadBadJson7() throws JsonProcessingException {
        Assertions.assertThrows(IOException.class, () -> JsonConnector.read("null"), "No connector found.");
    }

    @Test
    @SuppressWarnings("null")
    public void testReadBadJsonNull() throws JsonProcessingException {
        ForTests.testNull("json", () -> JsonConnector.read(null), "json");
    }

    @Test
    public void testWriteBadJson2() throws JsonProcessingException {
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var name2 = BadConnector.class.getName();
        var jsc = new JsonConnector(new BadConnector());
        var ex = Assertions.assertThrows(JsonMappingException.class, () -> mapper().writeValueAsString(jsc));
        Assertions.assertAll(
                //() -> Assertions.assertEquals(JsonConnector.UnknownConnectorException.class, ex.getCause().getClass()),
                //() -> Assertions.assertEquals("Untyped connector \"bad\".", ex.getCause().getMessage()),
                () -> Assertions.assertTrue(ex.getMessage().contains("IOException")),
                () -> Assertions.assertTrue(ex.getMessage().contains(name1)),
                () -> Assertions.assertTrue(ex.getMessage().contains("Untyped connector: \"" + name2 + "\"."))
        );
    }

    @Test
    public void testWriteBadJson3() throws JsonProcessingException {
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var name2 = BadConnector.class.getName();
        var jsc = new JsonConnector(new BadConnector());
        var ex = Assertions.assertThrows(JsonMappingException.class, () -> jsc.toJson());
        Assertions.assertAll(
                //() -> Assertions.assertEquals(JsonConnector.UnknownConnectorException.class, ex.getCause().getClass()),
                //() -> Assertions.assertEquals("Untyped connector \"bad\".", ex.getCause().getMessage()),
                () -> Assertions.assertTrue(ex.getMessage().contains("IOException")),
                () -> Assertions.assertTrue(ex.getMessage().contains(name1)),
                () -> Assertions.assertTrue(ex.getMessage().contains("Untyped connector: \"" + name2 + "\"."))
        );
    }

    @ConnectorJsonKey("foo")
    public static class FooConnector implements Connector {
        @Override
        public String url() {
            throw new AssertionError();
        }

        @Override
        public Optional<Connector.Auth> optAuth() {
            throw new AssertionError();
        }
    }

    @ConnectorJsonKey("bar")
    public static class BarConnector implements Connector {
        @Override
        public String url() {
            throw new AssertionError();
        }

        @Override
        public Optional<Connector.Auth> optAuth() {
            throw new AssertionError();
        }
    }

    @ConnectorJsonKey("goo")
    public static class GooConnector implements Connector {
        @Override
        public String url() {
            throw new AssertionError();
        }

        @Override
        public Optional<Connector.Auth> optAuth() {
            throw new AssertionError();
        }
    }

    @ConnectorJsonKey("firebird")
    public static class FirebirdImposterConnector implements Connector {
        @Override
        public String url() {
            throw new AssertionError();
        }

        @Override
        public Optional<Connector.Auth> optAuth() {
            throw new AssertionError();
        }
    }

    @ConnectorJsonKey("mysql")
    public static class MysqlImposterConnector implements Connector {
        @Override
        public String url() {
            throw new AssertionError();
        }

        @Override
        public Optional<Connector.Auth> optAuth() {
            throw new AssertionError();
        }
    }

    private static void testStd() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(Optional.of(AccessConnector.class), JsonConnector.find("access")),
                () -> Assertions.assertEquals(Optional.of(Db2Connector.class), JsonConnector.find("db2")),
                () -> Assertions.assertEquals(Optional.of(FirebirdConnector.class), JsonConnector.find("firebird")),
                () -> Assertions.assertEquals(Optional.of(H2Connector.class), JsonConnector.find("h2")),
                () -> Assertions.assertEquals(Optional.of(HsqldbConnector.class), JsonConnector.find("hsqldb")),
                () -> Assertions.assertEquals(Optional.of(MariaDbConnector.class), JsonConnector.find("mariadb")),
                () -> Assertions.assertEquals(Optional.of(MySqlConnector.class), JsonConnector.find("mysql")),
                () -> Assertions.assertEquals(Optional.of(OracleConnector.class), JsonConnector.find("oracle")),
                () -> Assertions.assertEquals(Optional.of(PostgreSqlConnector.class), JsonConnector.find("postgresql")),
                () -> Assertions.assertEquals(Optional.of(SqlServerConnector.class), JsonConnector.find("sqlserver")),
                () -> Assertions.assertEquals(Optional.of(SqliteConnector.class), JsonConnector.find("sqlite")),
                () -> Assertions.assertEquals(Optional.of(UrlConnector.class), JsonConnector.find("url"))
        );
    }

    @Test
    public void testRegister() throws JsonProcessingException {
        JsonConnector.register(FooConnector.class, BarConnector.class, FirebirdImposterConnector.class, MysqlImposterConnector.class);
        Assertions.assertAll(
                () -> {
                    var con = JsonConnector.read("{\"type\":\"foo\"}");
                    Assertions.assertEquals(FooConnector.class, con.delegate().getClass());
                },
                () -> {
                    var con = mapper().readValue("{\"type\":\"bar\"}", JsonConnector.class);
                    Assertions.assertEquals(BarConnector.class, con.delegate().getClass());
                },
                () -> Assertions.assertEquals(Optional.of(FooConnector.class), JsonConnector.find("foo")),
                () -> Assertions.assertEquals(Optional.of(BarConnector.class), JsonConnector.find("bar")),
                () -> Assertions.assertEquals(Optional.of(FirebirdImposterConnector.class), JsonConnector.find("firebird")),
                () -> Assertions.assertEquals(Optional.of(MysqlImposterConnector.class), JsonConnector.find("mysql"))
        );
    }

    @Test
    public void testReset() throws JsonProcessingException {
        JsonConnector.register(FooConnector.class, BarConnector.class, FirebirdImposterConnector.class, MysqlImposterConnector.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(Optional.of(FooConnector.class), JsonConnector.find("foo")),
                () -> Assertions.assertEquals(Optional.of(BarConnector.class), JsonConnector.find("bar")),
                () -> Assertions.assertEquals(Optional.of(FirebirdImposterConnector.class), JsonConnector.find("firebird")),
                () -> Assertions.assertEquals(Optional.of(MysqlImposterConnector.class), JsonConnector.find("mysql"))
        );
        JsonConnector.resetRegister();
        Assertions.assertAll(
                () -> Assertions.assertEquals(Optional.empty(), JsonConnector.find("foo")),
                () -> Assertions.assertEquals(Optional.empty(), JsonConnector.find("bar"))
        );
        testStd();
    }

    @Test
    public void testRegisterBad1() throws JsonProcessingException {
        JsonConnector.resetRegister();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> JsonConnector.register(GooConnector.class, BadConnector.class));
        Assertions.assertAll(
                () -> Assertions.assertThrows(
                        JsonMappingException.class,
                        () -> mapper().readValue("{\"type\":\"goo\"}", JsonConnector.class)
                ),
                () -> Assertions.assertThrows(
                        JsonMappingException.class,
                        () -> mapper().readValue("{\"type\":\"bad\"}", JsonConnector.class)
                ),
                () -> Assertions.assertEquals(Optional.empty(), JsonConnector.find("goo")),
                () -> Assertions.assertEquals(Optional.empty(), JsonConnector.find("bad"))
        );
    }

    @Test
    public void testRegisterBad2() throws JsonProcessingException {
        JsonConnector.resetRegister();
        Assertions.assertThrows(IllegalArgumentException.class, () -> JsonConnector.register(GooConnector.class, JsonConnector.class));
        Assertions.assertEquals(Optional.empty(), JsonConnector.find("goo"));
    }

    @Test
    public void testRegisterBad3() throws JsonProcessingException {
        JsonConnector.resetRegister();
        Assertions.assertThrows(IllegalArgumentException.class, () -> JsonConnector.register(GooConnector.class, null));
        Assertions.assertEquals(Optional.empty(), JsonConnector.find("goo"));
    }

    @SuppressWarnings("null")
    private static Stream<Arguments> testNulls() throws JsonProcessingException {
        return Stream.of(
                n("register", () -> ForTests.testNull("classes", () -> JsonConnector.register((Class<? extends Connector>[]) null))),
                n("find",     () -> ForTests.testNull("key", () -> JsonConnector.find(null), "key")),
                n("UCE",      () -> ForTests.testNull("message", () -> new JsonConnector.UnknownConnectorException(null), "message")),
                n("ctor",     () -> ForTests.testNull("delegate", () -> new JsonConnector(null), "delegate"))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "testNulls {0}")
    public void testNulls(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
