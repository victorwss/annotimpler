package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for [Ref] values. Only accepts [Ref] inputs (identity conversion).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum RefConverter implements Converter<Ref> {

    /// Singleton instance.
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
