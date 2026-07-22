package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.datetime;

/// A [Converter] for [LocalDate] values.
///
/// Supported conversions: [LocalDate] (identity), [LocalDateTime] (extract date),
/// [OffsetDateTime] (extract date), [String] (parsed via `MultiFormatters.YMD_DASH`; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum LocalDateConverter implements Converter<LocalDate> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `LocalDate.class`.
    ///
    /// @return `LocalDate.class`.
    @NonNull
    @Override
    public Class<LocalDate> getType() {
        return LocalDate.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull LocalDate in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull LocalDateTime in) {
        return Optional.of(in.toLocalDate());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toLocalDate());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseLocalDate(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }

    /// Returns `[LocalDateConverter]`.
    ///
    /// @return `[LocalDateConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[LocalDateConverter]";
    }
}
