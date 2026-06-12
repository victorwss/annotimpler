package ninja.javahacker.annotimpler.sql.meta;

import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

/// The [ninja.javahacker.annotimpler.core.KeyProperty] singleton for [ConnectionFactory].
///
/// Use this enum constant as a key when storing or retrieving the [ConnectionFactory]
/// instance in a [ninja.javahacker.annotimpler.core.PropertyBag].
///
/// @see ninja.javahacker.annotimpler.core.PropertyBag
/// @see ninja.javahacker.annotimpler.core.KeyProperty
public enum ConnectionFactoryKeyProperty implements KeyProperty<ConnectionFactory> {

    /// The single instance of this key.
    INSTANCE;

    /// Returns `ConnectionFactory.class`, the value type associated with this key.
    ///
    /// @return `ConnectionFactory.class`; never `null`.
    @Override
    public Class<ConnectionFactory> valueType() {
        return ConnectionFactory.class;
    }
}
