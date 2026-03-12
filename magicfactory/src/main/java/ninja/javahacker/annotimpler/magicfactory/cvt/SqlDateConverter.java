package ninja.javahacker.annotimpler.magicfactory.cvt;

import java.sql.Date;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlDateConverter implements Converter<Date> {
    INSTANCE;

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
        return LocalDateConverter.INSTANCE.from(in).map(Date::valueOf);
    }
}
