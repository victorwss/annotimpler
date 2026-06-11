package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for [Struct] values. Only accepts [Struct] inputs (identity conversion).
public enum StructConverter implements Converter<Struct> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `Struct.class`.
    ///
    /// @return `Struct.class`.
    @NonNull
    @Override
    public Class<Struct> getType() {
        return Struct.class;
    }

    @NonNull
    @Override
    public Optional<Struct> from(@NonNull Struct in) {
        return Optional.of(in);
    }
}
