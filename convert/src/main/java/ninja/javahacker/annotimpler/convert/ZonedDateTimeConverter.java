package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum ZonedDateTimeConverter implements Converter<ZonedDateTime> {
    INSTANCE;

    @Override
    public Optional<ZonedDateTime> from(@NonNull LocalDate in) {
        return Optional.of(in.atStartOfDay(ZoneOffset.UTC));
    }

    @Override
    public Optional<ZonedDateTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.atZone(ZoneOffset.UTC));
    }

    @Override
    public Optional<ZonedDateTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toZonedDateTime());
    }

    @Override
    public Optional<ZonedDateTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(ZonedDateTime.parse(in, OffsetDateTimeConverter.FORMATTER_DTZ));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTimeConverter.INSTANCE.from(in).map(x -> x.atOffset(ZoneOffset.UTC).toZonedDateTime());
            } catch (DateTimeParseException e2) {
                throw new ConvertionException("String inconvertible to ZonedDateTime.", e1, ZonedDateTime.class);
            }
        }
    }
}
