package ninja.javahacker.annotimpler.convert;

import java.util.Date;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum DateConverter implements Converter<Date> {
    INSTANCE;

    @FunctionalInterface
    private interface Work {
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
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDateTime in) {
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull OffsetDateTime in) {
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> InstantConverter.INSTANCE.from(in).map(Date::from));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
