package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum CalendarConverter implements Converter<Calendar> {
    INSTANCE;

    @Override
    public Optional<Calendar> from(@NonNull LocalDate in) {
        return GregorianCalendarConverter.INSTANCE.from(in).map(x -> x);
    }

    @Override
    public Optional<Calendar> from(@NonNull LocalDateTime in) {
        return GregorianCalendarConverter.INSTANCE.from(in).map(x -> x);
    }

    @Override
    public Optional<Calendar> from(@NonNull OffsetDateTime in) {
        return GregorianCalendarConverter.INSTANCE.from(in).map(x -> x);
    }

    @Override
    public Optional<Calendar> from(@NonNull String in) throws ConvertionException {
        return GregorianCalendarConverter.INSTANCE.from(in).map(x -> x);
    }
}
