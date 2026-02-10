package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum OffsetDateTimeConverter implements Converter<OffsetDateTime> {
    INSTANCE;

    public static final DateTimeFormatter FORMATTER_DTZ = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss zzzzz")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public OffsetDateTime from(@NonNull LocalDate in) {
        return in.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime from(@NonNull LocalDateTime in) {
        return in.atOffset(ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime from(@NonNull OffsetDateTime in) {
        return in;
    }

    @Override
    public OffsetDateTime from(@NonNull String in) {
        return OffsetDateTime.parse(in, FORMATTER_DTZ);
    }
}
