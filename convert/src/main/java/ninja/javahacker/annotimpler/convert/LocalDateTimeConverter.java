package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum LocalDateTimeConverter implements Converter<LocalDateTime> {
    INSTANCE;

    @NonNull
    public static final DateTimeFormatter FORMATTER_DT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd[ HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]]]")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull LocalDate in) {
        return Optional.of(in.atStartOfDay());
    }

    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toLocalDateTime());
    }

    @NonNull
    @Override
    public Optional<LocalDateTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(LocalDateTime.parse(in, FORMATTER_DT));
        } catch (DateTimeParseException e) {
            throw new ConvertionException("String inconvertible to LocalDateTime.", e, LocalDateTime.class);
        }
    }
}
