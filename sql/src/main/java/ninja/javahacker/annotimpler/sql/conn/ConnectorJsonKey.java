package ninja.javahacker.annotimpler.sql.conn;

import module java.base;

/// Marks a [Connector] implementation with the JSON type discriminator key used by
/// [JsonConnector] during serialisation and deserialisation.
///
/// Every class that may be registered via [JsonConnector#register(Class[])] must carry
/// this annotation with a unique, non-empty string value.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConnectorJsonKey {

    /// Returns the JSON type key that identifies this connector class.
    ///
    /// @return The non-empty string used as the value of the `type` field in the
    ///         serialised JSON representation.
    public String value();
}
