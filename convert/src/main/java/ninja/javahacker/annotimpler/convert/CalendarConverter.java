package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum CalendarConverter implements Converter<Calendar> {
    INSTANCE;

    @FunctionalInterface
    public interface Work {
        public Optional<Calendar> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Calendar> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Calendar.class);
        }
    }

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
        return rewrap(() -> GregorianCalendarConverter.INSTANCE.from(in).map(x -> x));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
