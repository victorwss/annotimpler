package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.annotimpler.datetime;

public enum ZonedDateTimeConverter implements Converter<ZonedDateTime> {
    INSTANCE;

    @NonNull
    @Override
    public Class<ZonedDateTime> getType() {
        return ZonedDateTime.class;
    }

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

    @NonNull
    @Override
    public Optional<ZonedDateTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(MultiFormatters.YMD_DASH.parseZonedDateTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
