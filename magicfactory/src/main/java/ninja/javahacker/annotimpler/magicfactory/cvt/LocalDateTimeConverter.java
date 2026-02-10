package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum LocalDateTimeConverter implements Converter<LocalDateTime> {
    INSTANCE;

    public static final DateTimeFormatter FORMATTER_DT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public LocalDateTime from(@NonNull LocalDate in) {
        return in.atStartOfDay();
    }

    @Override
    public LocalDateTime from(@NonNull LocalDateTime in) {
        return in;
    }

    @Override
    public LocalDateTime from(@NonNull OffsetDateTime in) {
        return in.toLocalDateTime();
    }

    @Override
    public LocalDateTime from(@NonNull String in) {
        return LocalDateTime.parse(in, FORMATTER_DT);
    }
}
