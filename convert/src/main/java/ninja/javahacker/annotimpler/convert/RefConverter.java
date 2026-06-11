package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for [Ref] values. Only accepts [Ref] inputs (identity conversion).
public enum RefConverter implements Converter<Ref> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `Ref.class`.
    ///
    /// @return `Ref.class`.
    @NonNull
    @Override
    public Class<Ref> getType() {
        return Ref.class;
    }

    @NonNull
    @Override
    public Optional<Ref> from(@NonNull Ref in) {
        return Optional.of(in);
    }
}
