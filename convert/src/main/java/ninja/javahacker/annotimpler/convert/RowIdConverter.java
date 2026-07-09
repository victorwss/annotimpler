package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for [RowId] values. Only accepts [RowId] inputs (identity conversion).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum RowIdConverter implements Converter<RowId> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `RowId.class`.
    ///
    /// @return `RowId.class`.
    @NonNull
    @Override
    public Class<RowId> getType() {
        return RowId.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<RowId> from(@NonNull RowId in) {
        return Optional.of(in);
    }
}
