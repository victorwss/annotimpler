package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.datetime;

/// A [Converter] for [OffsetTime] values.
///
/// Supported conversions: [LocalTime] (at UTC offset), [LocalDateTime] (at UTC, extract offset time),
/// [OffsetTime] (identity), [OffsetDateTime] (extract offset time),
/// [String] (parsed via `MultiFormatters.YMD_DASH`; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum OffsetTimeConverter implements Converter<OffsetTime> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `OffsetTime.class`.
    ///
    /// @return `OffsetTime.class`.
    @NonNull
    @Override
    public Class<OffsetTime> getType() {
        return OffsetTime.class;
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull LocalTime in) {
        return Optional.of(in.atOffset(ZoneOffset.UTC));
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.atOffset(ZoneOffset.UTC).toOffsetTime());
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull OffsetTime in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toOffsetTime());
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseOffsetTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
