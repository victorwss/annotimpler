package ninja.javahacker.annotimpler.sql.meta;

import module java.base;
import module ninja.javahacker.annotimpler.core;

/// The [ninja.javahacker.annotimpler.core.KeyProperty] singleton for [java.util.Locale].
///
/// Use this enum constant as a key when storing or retrieving the [java.util.Locale]
/// (localizer) instance in a [ninja.javahacker.annotimpler.core.PropertyBag].
///
/// @see ninja.javahacker.annotimpler.core.PropertyBag
/// @see ninja.javahacker.annotimpler.core.KeyProperty
public enum LocalizerKeyProperty implements KeyProperty<Locale> {

    /// The single instance of this key.
    INSTANCE;

    /// Returns `Locale.class`, the value type associated with this key.
    ///
    /// @return `Locale.class`; never `null`.
    @Override
    public Class<Locale> valueType() {
        return Locale.class;
    }
}
