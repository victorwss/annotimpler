package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum GregorianCalendarConverter implements Converter<GregorianCalendar> {
    INSTANCE;

    @Override
    public GregorianCalendar from(@NonNull LocalDate in) {
        return GregorianCalendar.from(ZonedDateTimeConverter.INSTANCE.from(in));
    }

    @Override
    public GregorianCalendar from(@NonNull LocalDateTime in) {
        return GregorianCalendar.from(ZonedDateTimeConverter.INSTANCE.from(in));
    }

    @Override
    public GregorianCalendar from(@NonNull OffsetDateTime in) {
        return GregorianCalendar.from(ZonedDateTimeConverter.INSTANCE.from(in));
    }

    @Override
    public GregorianCalendar from(@NonNull String in) {
        return GregorianCalendar.from(ZonedDateTimeConverter.INSTANCE.from(in));
    }
}
