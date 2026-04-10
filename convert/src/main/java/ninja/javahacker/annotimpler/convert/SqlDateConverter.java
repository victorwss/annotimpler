package ninja.javahacker.annotimpler.convert;

import java.sql.Date;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum SqlDateConverter implements Converter<Date> {
    INSTANCE;

    @FunctionalInterface
    public interface Work {
        public Optional<Date> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Date> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Date.class);
        }
    }

    @NonNull
    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDate in) {
        return Optional.of(Date.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDateTime in) {
        return LocalDateConverter.INSTANCE.from(in).map(Date::valueOf);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull OffsetDateTime in) {
        return LocalDateConverter.INSTANCE.from(in).map(Date::valueOf);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> LocalDateConverter.INSTANCE.from(in).map(Date::valueOf));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
