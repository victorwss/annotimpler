package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.datetime;

/// A [Converter] for [OffsetDateTime] values.
///
/// Supported conversions: [LocalDate] (start of day at UTC), [LocalDateTime] (at UTC),
/// [OffsetDateTime] (identity),
/// [String] (parsed via `MultiFormatters.YMD_DASH`; empty → empty).
public enum OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `OffsetDateTime.class`.
    ///
    /// @return `OffsetDateTime.class`.
    @NonNull
    @Override
    public Class<OffsetDateTime> getType() {
        return OffsetDateTime.class;
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull LocalDate in) {
        return Optional.of(in.atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.atOffset(ZoneOffset.UTC));
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseOffsetDateTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
