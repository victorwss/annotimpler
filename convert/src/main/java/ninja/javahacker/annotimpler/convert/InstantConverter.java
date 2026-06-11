package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.datetime;

/// A [Converter] for [Instant] values.
///
/// Supported conversions: [LocalDate], [LocalDateTime], [OffsetDateTime]
/// (via [OffsetDateTimeConverter] then `toInstant`),
/// [String] (parsed via `MultiFormatters.YMD_DASH`; empty → empty).
public enum InstantConverter implements Converter<Instant> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `Instant.class`.
    ///
    /// @return `Instant.class`.
    @NonNull
    @Override
    public Class<Instant> getType() {
        return Instant.class;
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull LocalDate in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull LocalDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull OffsetDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseInstant(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
