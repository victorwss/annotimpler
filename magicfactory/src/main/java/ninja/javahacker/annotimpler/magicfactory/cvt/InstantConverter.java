package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum InstantConverter implements Converter<Instant> {
    INSTANCE;

    @Override
    public Instant from(@NonNull LocalDate in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).toInstant();
    }

    @Override
    public Instant from(@NonNull LocalDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).toInstant();
    }

    @Override
    public Instant from(@NonNull OffsetDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).toInstant();
    }

    @Override
    public Instant from(@NonNull String in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).toInstant();
    }
}
