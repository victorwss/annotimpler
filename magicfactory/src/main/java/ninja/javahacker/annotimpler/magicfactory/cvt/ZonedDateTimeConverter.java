package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum ZonedDateTimeConverter implements Converter<ZonedDateTime> {
    INSTANCE;

    @Override
    public ZonedDateTime from(@NonNull LocalDate in) {
        return in.atStartOfDay().atZone(ZoneOffset.UTC);
    }

    @Override
    public ZonedDateTime from(@NonNull LocalDateTime in) {
        return in.atZone(ZoneOffset.UTC);
    }

    @Override
    public ZonedDateTime from(@NonNull OffsetDateTime in) {
        return in.toZonedDateTime();
    }

    @Override
    public ZonedDateTime from(@NonNull String in) {
        return ZonedDateTime.parse(in, OffsetDateTimeConverter.FORMATTER_DTZ);
    }
}
