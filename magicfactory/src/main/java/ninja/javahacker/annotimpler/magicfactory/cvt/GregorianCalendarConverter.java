package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum GregorianCalendarConverter implements Converter<GregorianCalendar> {
    INSTANCE;

    @Override
    public Optional<GregorianCalendar> from(@NonNull LocalDate in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull LocalDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull OffsetDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull String in) throws ConvertionException {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }
}
