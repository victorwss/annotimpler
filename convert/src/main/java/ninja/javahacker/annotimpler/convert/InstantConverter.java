package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum InstantConverter implements Converter<Instant> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull LocalDate in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull LocalDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull OffsetDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull String in) throws ConvertionException {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }
}
