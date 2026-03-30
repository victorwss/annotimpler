package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum OffsetDateTimeConverter implements Converter<OffsetDateTime> {
    INSTANCE;

    @NonNull
    public static final DateTimeFormatter FORMATTER_DTZ = DateTimeFormatter
            .ofPattern("uuuu-MM-dd[ HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][ x]]")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull LocalDate in) {
        return Optional.of(in.atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.atOffset(ZoneOffset.UTC));
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<OffsetDateTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(OffsetDateTime.parse(in, FORMATTER_DTZ));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTimeConverter.INSTANCE.from(in).map(x -> x.atOffset(ZoneOffset.UTC));
            } catch (DateTimeParseException e2) {
                throw new ConvertionException("String inconvertible to OffsetDateTime.", e1, OffsetDateTime.class);
            }
        }
    }
}
