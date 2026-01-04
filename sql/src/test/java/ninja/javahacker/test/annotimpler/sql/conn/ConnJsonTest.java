package ninja.javahacker.test.annotimpler.sql.conn;

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
        return new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new ParameterNamesModule());
    }

    @Test
    public void testReadJson() throws JsonProcessingException {
        var con = mapper().readValue(JSON, JsonConnector.class);
        var delegate = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        Assertions.assertAll(
                () -> Assertions.assertEquals(new JsonConnector(delegate), con),
                () -> Assertions.assertEquals(delegate, con.delegate())
        );
    }

    @Test
    public void testWriteJson() throws JsonProcessingException {
        var inJson = JSON.replace(" ", "").replace("\r", "").replace("\n", "");
        var delegate = new MariaDbConnector("localhost", 3306, "admin", "secret", "test");
        var jsc = new JsonConnector(delegate);
        var mapper = new ObjectMapper();
        var outJson = mapper.writeValueAsString(jsc);
        Assertions.assertEquals(inJson, outJson);
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
    public void testReadBadJson() throws JsonProcessingException {
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var ex = Assertions.assertThrows(
                JsonMappingException.class,
                () -> mapper().readValue(JSON.replace("mariadb", "bad"), JsonConnector.class)
        );
        Assertions.assertAll(
                //() -> Assertions.assertEquals(JsonConnector.UnknownConnectorException.class, ex.getCause().getClass()),
                //() -> Assertions.assertEquals("Unknown connector \"bad\".", ex.getCause().getMessage()),
                () -> Assertions.assertTrue(ex.getMessage().contains("IOException")),
                () -> Assertions.assertTrue(ex.getMessage().contains(name1)),
                () -> Assertions.assertTrue(ex.getMessage().contains("Unknown connector: \"bad\"."))
        );
    }

    @Test
    public void testWriteBadJson() throws JsonProcessingException {
        var name1 = JsonConnector.UnknownConnectorException.class.getName();
        var name2 = BadConnector.class.getName();
        var jsc = new JsonConnector(new BadConnector());
        var ex = Assertions.assertThrows(
                JsonMappingException.class,
                () -> mapper().writeValueAsString(jsc)
        );
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

    @Test
    public void testRegister() throws JsonProcessingException {
        JsonConnector.register(FooConnector.class, BarConnector.class);
        var mapper = new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new ParameterNamesModule());
        Assertions.assertAll(
                () -> {
                    var con = mapper.readValue("{\"type\":\"foo\"}", JsonConnector.class);
                    Assertions.assertEquals(FooConnector.class, con.delegate().getClass());
                },
                () -> {
                    var con = mapper.readValue("{\"type\":\"bar\"}", JsonConnector.class);
                    Assertions.assertEquals(BarConnector.class, con.delegate().getClass());
                },
                () -> Assertions.assertEquals(Optional.of(FooConnector.class), JsonConnector.find("foo")),
                () -> Assertions.assertEquals(Optional.of(BarConnector.class), JsonConnector.find("bar"))
        );
    }

    @Test
    public void testRegisterBad1() throws JsonProcessingException {
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
        Assertions.assertThrows(IllegalArgumentException.class, () -> JsonConnector.register(GooConnector.class, JsonConnector.class));
    }
}
