package ninja.javahacker.annotimpler.convert;

import java.sql.Timestamp;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum SqlTimestampConverter implements Converter<Timestamp> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDate in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull OffsetDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull String in) throws ConvertionException {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }
}
