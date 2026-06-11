package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for [RowId] values. Only accepts [RowId] inputs (identity conversion).
public enum RowIdConverter implements Converter<RowId> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `RowId.class`.
    ///
    /// @return `RowId.class`.
    @NonNull
    @Override
    public Class<RowId> getType() {
        return RowId.class;
    }

    @NonNull
    @Override
    public Optional<RowId> from(@NonNull RowId in) {
        return Optional.of(in);
    }
}
