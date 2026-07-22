package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module java.sql;

/// A [Converter] for [Struct] values. Only accepts [Struct] inputs (identity conversion).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum StructConverter implements Converter<Struct> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `Struct.class`.
    ///
    /// @return `Struct.class`.
    @NonNull
    @Override
    public Class<Struct> getType() {
        return Struct.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Struct> from(@NonNull Struct in) {
        return Optional.of(in);
    }

    /// Returns `[StructConverter]`.
    ///
    /// @return `[StructConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[StructConverter]";
    }
}
