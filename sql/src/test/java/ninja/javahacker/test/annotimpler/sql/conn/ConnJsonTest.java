package ninja.javahacker.test.annotimpler.sql.conn;

import ninja.javahacker.test.ForTests;

import module com.fasterxml.jackson.core;
import module com.fasterxml.jackson.databind;
import module com.fasterxml.jackson.datatype.jdk8;
import module com.fasterxml.jackson.module.paramnames;
import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

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

    @Test
    public void testEqualsHashCodeToString() {
        var delegate1 = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var delegate2 = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var jsc1 = new JsonConnector(delegate1);
        var jsc2 = new JsonConnector(delegate2);
        var jsc3 = new JsonConnector(delegate1);
        var delegate3 = new MySqlConnector("10.0.0.1", 4444, "dba", "$$$$", "sample");
        var jsc4 = new JsonConnector(delegate3);
        Assertions.assertAll(
                () -> Assertions.assertEquals(jsc1, jsc2),
                () -> Assertions.assertEquals(jsc1.delegate(), jsc2.delegate()),
                () -> Assertions.assertEquals(jsc1, jsc3),
                () -> Assertions.assertEquals(jsc1.delegate(), jsc3.delegate()),
                () -> Assertions.assertEquals(delegate1.hashCode(), jsc1.hashCode()),
                () -> Assertions.assertNotEquals(jsc1, jsc4),
                () -> Assertions.assertNotEquals(jsc1.delegate(), jsc4.delegate()),
                () -> Assertions.assertEquals(jsc1.hashCode(), jsc2.hashCode()),
                () -> Assertions.assertEquals("JSON Connector: [" + delegate1.toString() + "]", jsc1.toString()),
                () -> Assertions.assertEquals("JSON Connector: [" + delegate1.toString() + "]", jsc2.toString()),
                () -> Assertions.assertEquals("JSON Connector: [" + delegate3.toString() + "]", jsc4.toString()),
                () -> Assertions.assertEquals(delegate1.url(), jsc1.url()),
                () -> Assertions.assertEquals(delegate1.optUser(), jsc1.optUser()),
                () -> Assertions.assertEquals(delegate1.optPassword(), jsc1.optPassword()),
                () -> Assertions.assertEquals(delegate1.optAuth(), jsc1.optAuth()),
                () -> Assertions.assertEquals(delegate1.asUrl(), jsc1.asUrl()),
                () -> Assertions.assertEquals(delegate3.url(), jsc4.url()),
                () -> Assertions.assertEquals(delegate3.optUser(), jsc4.optUser()),
                () -> Assertions.assertEquals(delegate3.optPassword(), jsc4.optPassword()),
                () -> Assertions.assertEquals(delegate3.optAuth(), jsc4.optAuth()),
                () -> Assertions.assertEquals(delegate3.asUrl(), jsc4.asUrl())
        );
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

    @Test
    public void testRegisterBad4() throws JsonProcessingException {
        ForTests.testNull("classes", () -> JsonConnector.register((Class<? extends Connector>[]) null), "classes");
    }
}
