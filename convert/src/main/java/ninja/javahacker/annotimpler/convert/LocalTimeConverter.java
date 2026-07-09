package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.datetime;

/// A [Converter] for [LocalTime] values.
///
/// Supported conversions: [LocalTime] (identity), [LocalDateTime] (extract time),
/// [OffsetTime] (extract local time), [OffsetDateTime] (extract local time),
/// [String] (parsed via `MultiFormatters.YMD_DASH`; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum LocalTimeConverter implements Converter<LocalTime> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `LocalTime.class`.
    ///
    /// @return `LocalTime.class`.
    @NonNull
    @Override
    public Class<LocalTime> getType() {
        return LocalTime.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull LocalTime in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.toLocalTime());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull OffsetTime in) {
        return Optional.of(in.toLocalTime());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toLocalTime());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseLocalTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
