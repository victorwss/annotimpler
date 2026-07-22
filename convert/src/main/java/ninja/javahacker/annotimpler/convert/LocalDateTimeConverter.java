package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.datetime;

/// A [Converter] for [LocalDateTime] values.
///
/// Supported conversions: [LocalDate] (start of day), [LocalDateTime] (identity),
/// [OffsetDateTime] (extract local date-time),
/// [String] (parsed via `MultiFormatters.YMD_DASH`; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum LocalDateTimeConverter implements Converter<LocalDateTime> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `LocalDateTime.class`.
    ///
    /// @return `LocalDateTime.class`.
    @NonNull
    @Override
    public Class<LocalDateTime> getType() {
        return LocalDateTime.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull LocalDate in) {
        return Optional.of(in.atStartOfDay());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toLocalDateTime());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseLocalDateTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }

    /// Returns `[LocalDateTimeConverter]`.
    ///
    /// @return `[LocalDateTimeConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[LocalDateTimeConverter]";
    }
}
