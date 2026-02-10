package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum LocalTimeConverter implements Converter<LocalTime> {
    INSTANCE;

    public static final DateTimeFormatter FORMATTER_T = DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public LocalTime from(@NonNull LocalTime in) {
        return in;
    }

    @Override
    public LocalTime from(@NonNull LocalDateTime in) {
        return in.toLocalTime();
    }

    @Override
    public LocalTime from(@NonNull OffsetTime in) {
        return in.toLocalTime();
    }

    @Override
    public LocalTime from(@NonNull OffsetDateTime in) {
        return in.toLocalTime();
    }

    @Override
    public LocalTime from(@NonNull String in) {
        return LocalTime.parse(in, FORMATTER_T);
    }
}
