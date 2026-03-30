package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum LocalTimeConverter implements Converter<LocalTime> {
    INSTANCE;

    @NonNull
    public static final DateTimeFormatter FORMATTER_T = DateTimeFormatter
            .ofPattern("HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]]")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull LocalTime in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.toLocalTime());
    }

    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull OffsetTime in) {
        return Optional.of(in.toLocalTime());
    }

    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toLocalTime());
    }

    @NonNull
    @Override
    public Optional<LocalTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(LocalTime.parse(in, FORMATTER_T));
        } catch (DateTimeParseException e) {
            throw new ConvertionException("String inconvertible to LocalTime.", e, LocalTime.class);
        }
    }
}
