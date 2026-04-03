package ninja.javahacker.annotimpler.convert;

import java.util.Date;
import lombok.NonNull;

import module java.base;

public enum DateConverter implements Converter<Date> {
    INSTANCE;

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
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }
}
