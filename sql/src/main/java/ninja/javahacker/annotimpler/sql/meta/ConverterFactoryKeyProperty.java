package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

/// The [ninja.javahacker.annotimpler.core.KeyProperty] singleton for [ConverterFactory].
///
/// Use this enum constant as a key when storing or retrieving the [ConverterFactory]
/// instance in a [ninja.javahacker.annotimpler.core.PropertyBag].
///
/// @see ninja.javahacker.annotimpler.core.PropertyBag
/// @see ninja.javahacker.annotimpler.core.KeyProperty
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum ConverterFactoryKeyProperty implements KeyProperty<ConverterFactory> {

    /// The single instance of this key.
    INSTANCE;

    /// Returns `ConverterFactory.class`, the value type associated with this key.
    ///
    /// @return `ConverterFactory.class`; never `null`.
    @Override
    public Class<ConverterFactory> valueType() {
        return ConverterFactory.class;
    }
}
