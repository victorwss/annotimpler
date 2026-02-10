package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum LocalDateConverter implements Converter<LocalDate> {
    INSTANCE;

    public static final DateTimeFormatter FORMATTER_D = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public LocalDate from(@NonNull LocalDate in) {
        return in;
    }

    @Override
    public LocalDate from(@NonNull LocalDateTime in) {
        return in.toLocalDate();
    }

    @Override
    public LocalDate from(@NonNull OffsetDateTime in) {
        return in.toLocalDate();
    }

    @Override
    public LocalDate from(@NonNull String in) {
        return LocalDate.parse(in, FORMATTER_D);
    }
}
