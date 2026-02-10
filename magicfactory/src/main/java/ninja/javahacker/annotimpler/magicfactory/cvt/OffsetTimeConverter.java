package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum OffsetTimeConverter implements Converter<OffsetTime> {
    INSTANCE;

    public static final DateTimeFormatter FORMATTER_TZ = DateTimeFormatter
            .ofPattern("HH:mm:ss zzzzz")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public OffsetTime from(@NonNull LocalTime in) {
        return in.atOffset(ZoneOffset.UTC);
    }

    @Override
    public OffsetTime from(@NonNull LocalDateTime in) {
        return in.atOffset(ZoneOffset.UTC).toOffsetTime();
    }

    @Override
    public OffsetTime from(@NonNull OffsetTime in) {
        return in;
    }

    @Override
    public OffsetTime from(@NonNull OffsetDateTime in) {
        return in.toOffsetTime();
    }

    @Override
    public OffsetTime from(@NonNull String in) {
        return OffsetTime.parse(in, FORMATTER_TZ);
    }
}
