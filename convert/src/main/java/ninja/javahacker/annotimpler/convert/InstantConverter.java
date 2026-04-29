package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum InstantConverter implements Converter<Instant> {
    INSTANCE;

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
