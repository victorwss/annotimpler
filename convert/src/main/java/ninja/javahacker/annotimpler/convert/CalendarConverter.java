package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum CalendarConverter implements Converter<Calendar> {
    INSTANCE;

    @NonNull
    @Override
    public Class<Calendar> getType() {
        return Calendar.class;
    }

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
