package ninja.javahacker.annotimpler.sql.conn;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module tools.jackson.core;
import module tools.jackson.databind;
import module java.base;

/// JSON-serializable wrapper for any [Connector].
///
/// The serialized form is a JSON object containing all fields of the wrapped connector plus a
/// `"type"` discriminator field whose value is the [ConnectorJsonKey] of the concrete
/// connector class. For example:
///
/// ```json
/// {"type":"mysql","host":"localhost","port":3306,"user":"admin","password":"admin","database":""}
/// ```
///
/// The set of recognized connector types is maintained in a global registry initialized with
/// all concrete connectors shipped in this package. Additional types may be added at runtime
/// via [#register(Class[])].
@JsonDeserialize(using = JsonConnector.Deserializer.class)
@JsonSerialize(using = JsonConnector.Serializer.class)
public final class JsonConnector implements Connector {

    /// The Jackson's JSON mapper used to map JSON-formatted [Connecrtor]s.
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .build();

    /// The globally mutable set of registered classes, mapped by names.
    private static final Map<String, Class<? extends Connector>> REGISTERED_CLASSES;

    /// The standard immutable set of registered classes, mapped by names.
    private static final Map<String, Class<? extends Connector>> STD_REGISTERED_CLASSES;

    /// The lock for messing with [#REGISTERED_CLASSES].
    private static final ReentrantLock LOCK = new ReentrantLock();

    /// The wrapped [Connector].
    @Delegate(types = Connector.class)
    private final Connector delegate;

    static {
        REGISTERED_CLASSES = new HashMap<>(20);
        register(
                AccessConnector.class,
                Db2Connector.class,
                FirebirdConnector.class,
                H2Connector.class,
                HsqldbConnector.class,
                MariaDbConnector.class,
                MySqlConnector.class,
                OracleConnector.class,
                PostgreSqlConnector.class,
                SqlServerConnector.class,
                SqliteConnector.class,
                SqliteMemoryConnector.class,
                UrlConnector.class
        );
        STD_REGISTERED_CLASSES = Map.copyOf(REGISTERED_CLASSES);
    }

    /// Creates a new `JsonConnector` wrapping the given delegate.
    ///
    /// @param delegate The connector to wrap.
    /// @throws IllegalArgumentException If `delegate` is `null`.
    public JsonConnector(@NonNull Connector delegate) {
        this.delegate = delegate;
    }

    /// Returns the wrapped connector.
    ///
    /// @return The wrapped delegate connector; never `null`.
    public Connector getDelegate() {
        return delegate;
    }

    /// Registers one or more connector classes in the global type registry.
    ///
    /// Each class must carry a [ConnectorJsonKey] annotation and must not be
    /// `JsonConnector` itself. Registrations are thread-safe and take effect immediately.
    ///
    /// @param classes The connector classes to register.
    /// @throws IllegalArgumentException If any element of `classes` is `null`,
    ///         is `JsonConnector.class`, or lacks a [ConnectorJsonKey] annotation.
    @SafeVarargs
    public static void register(@NonNull Class<? extends Connector>... classes) {
        for (var k : classes) {
            if (k == null || k == JsonConnector.class) throw new IllegalArgumentException();
            var key = k.getAnnotation(ConnectorJsonKey.class);
            if (key == null) throw new UnsupportedOperationException();
        }

        try {
            LOCK.lock();
            for (var k : classes) {
                var key = k.getAnnotation(ConnectorJsonKey.class);
                var kv = key.value();
                REGISTERED_CLASSES.put(kv, k);
            }
        } finally {
            LOCK.unlock();
        }
    }

    /// Resets the global type registry to the standard set of connectors shipped with this package.
    ///
    /// Any classes previously added via [#register(Class[])] are removed.
    public static void resetRegister() {
        try {
            LOCK.lock();
            REGISTERED_CLASSES.clear();
            REGISTERED_CLASSES.putAll(STD_REGISTERED_CLASSES);
        } finally {
            LOCK.unlock();
        }
    }

    /// Looks up a connector class by its JSON type key.
    ///
    /// @param key The JSON type discriminator value to look up.
    /// @return An [Optional] containing the registered class for `key`,
    ///         or an empty optional if no class is registered under that key.
    /// @throws IllegalArgumentException If `key` is `null`.
    public static Optional<Class<? extends Connector>> find(@NonNull String key) {
        try {
            LOCK.lock();
            return Optional.ofNullable(REGISTERED_CLASSES.get(key));
        } finally {
            LOCK.unlock();
        }
    }

    /// JSON Deserializer for [JsonConnector].
    @PackagePrivate
    static class Deserializer extends ValueDeserializer<JsonConnector> {

        /// Sole constructor.
        public Deserializer() {
        }

        /// {@inheritDoc}
        @Override
        public JsonConnector deserialize(@NonNull JsonParser jp, @NonNull DeserializationContext ctxt) {
            checkNotNull(jp); // Check recognized by lombok.
            checkNotNull(ctxt); // Check recognized by lombok.
            var tree = jp.readValueAsTree();
            if (!(tree instanceof ObjectNode)) throw new UnknownConnectorException("JSON does not contain a connector.");
            var copy = ((ObjectNode) tree).deepCopy();
            var node = copy.get("type");
            if (node == null) throw new UnknownConnectorException("JSON has no type field.");
            var type = node.asString();
            copy.remove("type");
            var targetClass = find(type).orElseThrow(() -> new UnknownConnectorException("Unknown connector: \"" + type + "\"."));
            var delegate = MAPPER.readValue(copy.toString(), targetClass);
            return new JsonConnector(delegate);
        }
    }

    /// JSON Serializer for [JsonConnector].
    @PackagePrivate
    static class Serializer extends ValueSerializer<JsonConnector> {

        /// Sole constructor.
        public Serializer() {
        }

        /// {@inheritDoc}
        @Override
        public void serialize(@NonNull JsonConnector t, @NonNull JsonGenerator jg, @NonNull SerializationContext sp) {
            checkNotNull(t); // Check recognized by lombok.
            checkNotNull(jg); // Check recognized by lombok.
            checkNotNull(sp); // Check recognized by lombok.
            var d = t.delegate;
            var c = d.getClass();
            var inner = MAPPER.writeValueAsString(d);
            var annot = c.getAnnotation(ConnectorJsonKey.class);
            if (annot == null) throw new UnknownConnectorException("Untyped connector: \"" + c.getName() + "\".");
            var key = annot.value();
            inner = inner.substring(0, 1) + "\"type\":\"" + key + "\"," + inner.substring(1);
            jg.writeRaw(inner);
        }
    }

    /// Thrown when a JSON object refers to a connector type that is not registered in the
    /// global type registry.
    public static class UnknownConnectorException extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a new `UnknownConnectorException` with the given detail message.
        ///
        /// @param message The detail message.
        /// @throws IllegalArgumentException If `message` is `null`.
        public UnknownConnectorException(@NonNull String message) {
            List.of(message); // Force lombok to put the null-checks before the constructor call.
            super(message);
        }

        /// Disabled. Should not be used. Does nothing.
        ///
        /// This method exists with the sole purpose of fixing SpotBugs' CT_CONSTRUCTOR_THROW
        /// by disabling the ability to override the `finalize()` method that should not even exist to start with.
        ///
        /// @deprecated Finalization was deprecated. This method is intentionally unused, unusable and disabled.
        @Deprecated
        @SuppressWarnings({
            "override", "removal", "FinalizeDoesntCallSuperFinalize", "FinalizeDeclaration", "PMD.EmptyFinalizer", "checkstyle:NoFinalizer"
        })
        protected final void finalize() {
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public String toString() {
        return "JSON Connector: [" + delegate.toString() + "]";
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    public boolean equals(@Nullable Object other) {
        return other instanceof JsonConnector jo && Objects.equals(this.delegate, jo.getDelegate());
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /// Deserializes a `JsonConnector` from a JSON string.
    ///
    /// The JSON object must contain a `"type"` field whose value matches the
    /// [ConnectorJsonKey] of a registered connector class.
    ///
    /// @param json The JSON string to parse.
    /// @return The deserialized `JsonConnector`; never `null`.
    /// @throws IOException If the JSON is malformed, the `"type"` field is missing or
    ///         unrecognized, or any other I/O error occurs.
    /// @throws IllegalArgumentException If `json` is `null`.
    @NonNull
    public static JsonConnector read(@NonNull String json) throws IOException {
        var r = MAPPER.readValue(json, JsonConnector.class);
        if (r == null) throw new IOException("No connector found.");
        return r;
    }

    /// Serializes this connector to a JSON string.
    ///
    /// The output includes a `"type"` discriminator field alongside all fields of the
    /// wrapped connector.
    ///
    /// @return The JSON representation of this connector; never `null`.
    /// @throws IOException If the delegate connector class has no registered
    ///         [ConnectorJsonKey], or if any other serialization error occurs.
    @NonNull
    public String toJson() throws IOException {
        return MAPPER.writeValueAsString(this);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
