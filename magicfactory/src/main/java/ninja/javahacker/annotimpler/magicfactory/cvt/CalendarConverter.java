package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum CalendarConverter implements Converter<Calendar> {
    INSTANCE;

    @Override
    public Calendar from(@NonNull LocalDate in) {
        return GregorianCalendarConverter.INSTANCE.from(in);
    }

    @Override
    public Calendar from(@NonNull LocalDateTime in) {
        return GregorianCalendarConverter.INSTANCE.from(in);
    }

    @Override
    public Calendar from(@NonNull OffsetDateTime in) {
        return GregorianCalendarConverter.INSTANCE.from(in);
    }

    @Override
    public Calendar from(@NonNull String in) {
        return GregorianCalendarConverter.INSTANCE.from(in);
    }
}
