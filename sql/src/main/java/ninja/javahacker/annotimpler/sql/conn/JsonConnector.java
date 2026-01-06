package ninja.javahacker.annotimpler.sql.conn;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module com.fasterxml.jackson.core;
import module com.fasterxml.jackson.databind;
import module java.base;

@JsonDeserialize(using = JsonConnector.Deserializer.class)
@JsonSerialize(using = JsonConnector.Serializer.class)
public final class JsonConnector implements Connector {

    private static final Map<String, Class<? extends Connector>> CLASSES = new HashMap<>(20);

    @Delegate(types = Connector.class)
    private final Connector delegate;

    static {
        register(
                AccessConnector.class,
                FirebirdConnector.class,
                H2Connector.class,
                HsqldbConnector.class,
                LocalDerbyConnector.class,
                RemoteDerbyConnector.class,
                MariaDbConnector.class,
                MySqlConnector.class,
                OracleConnector.class,
                PostgreSqlConnector.class,
                SqlServerConnector.class,
                SqliteConnector.class,
                UrlConnector.class
        );
    }

    public JsonConnector(@NonNull Connector delegate) {
        this.delegate = delegate;
    }

    public Connector delegate() {
        return delegate;
    }

    @SafeVarargs
    public static void register(@NonNull Class<? extends Connector>... klasses) {
        for (var k : klasses) {
            if (k == JsonConnector.class) throw new IllegalArgumentException();
            var key = k.getAnnotation(ConnectorJsonKey.class);
            if (key == null) throw new UnsupportedOperationException();
        }

        for (var k : klasses) {
            var key = k.getAnnotation(ConnectorJsonKey.class);
            var kv = key.value();

            synchronized (CLASSES) {
                CLASSES.put(kv, k);
            }
        }
    }

    public static Optional<Class<? extends Connector>> find(@NonNull String key) {
        synchronized (CLASSES) {
            return Optional.ofNullable(CLASSES.get(key));
        }
    }

    @PackagePrivate
    static class Deserializer extends JsonDeserializer<JsonConnector> {

        public Deserializer() {
        }

        @Override
        public JsonConnector deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            var tree = jp.readValueAsTree();
            var copy = ((ObjectNode) tree).deepCopy();
            var mapper = (ObjectMapper) jp.getCodec();
            var type = copy.get("type").asText();
            copy.remove("type");
            var targetClass = find(type).orElseThrow(() -> new UnknownConnectorException("Unknown connector: \"" + type + "\"."));
            var delegate = mapper.readValue(copy + "", targetClass);
            return new JsonConnector(delegate);
        }
    }

    @PackagePrivate
    static class Serializer extends JsonSerializer<JsonConnector> {

        public Serializer() {
        }

        @Override
        public void serialize(JsonConnector t, JsonGenerator jg, SerializerProvider sp) throws IOException {
            var d = t.delegate;
            var c = d.getClass();
            var mapper = (ObjectMapper) jg.getCodec();
            var inner = mapper.writeValueAsString(d);
            var annot = c.getAnnotation(ConnectorJsonKey.class);
            if (annot == null) throw new UnknownConnectorException("Untyped connector: \"" + c.getName() + "\".");
            var key = annot.value();
            inner = inner.substring(0, 1) + "\"type\":\"" + key + "\"," + inner.substring(1);
            jg.writeRaw(inner);
        }
    }

    public static class UnknownConnectorException extends IOException {

        @Serial
        private static final long serialVersionUID = 1L;

        public UnknownConnectorException(@NonNull String message) {
            List.of(message); // Force lombok put the null-checks before the constructor call.
            super(message);
        }
    }

    @Override
    public String toString() {
        return "JSON Connector: [" + delegate.toString() + "]";
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof JsonConnector jo && Objects.equals(this.delegate, jo.delegate());
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
