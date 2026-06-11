package ninja.javahacker.annotimpler.core;

import module java.base;

/// A typed key for use with [PropertyBag].
///
/// Implementations of this interface serve as keys in a [PropertyBag], associating each
/// key with a specific value type `V`. The [valueType] method returns the [Class] token
/// for `V`, enabling [PropertyBag] to validate and cast values in a type-safe way.
///
/// Implementations should be singletons or enum constants to ensure consistent key identity
/// (since `PropertyBag` uses them as map keys).
///
/// @param <V> The type of value associated with this key.
///
/// @see PropertyBag
public interface KeyProperty<V> extends Serializable {
    /// Returns the [Class] token for the value type `V` associated with this key.
    ///
    /// Used by [PropertyBag] to validate values on [PropertyBag#add] and to cast
    /// them on [PropertyBag#get].
    ///
    /// @return The class of `V`; must not be `null`.
    public Class<V> valueType();
}
