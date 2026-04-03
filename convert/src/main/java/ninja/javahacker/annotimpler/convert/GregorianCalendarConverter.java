package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum GregorianCalendarConverter implements Converter<GregorianCalendar> {
    INSTANCE;

    @NonNull
    @Override
    public Class<GregorianCalendar> getType() {
        return GregorianCalendar.class;
    }

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
