package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum OffsetTimeConverter implements Converter<OffsetTime> {
    INSTANCE;

    @NonNull
    public static final DateTimeFormatter FORMATTER_TZ = DateTimeFormatter
            .ofPattern("HH:mm[:ss][ Z]")
            .withResolverStyle(ResolverStyle.STRICT);

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull LocalTime in) {
        return Optional.of(in.atOffset(ZoneOffset.UTC));
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull LocalDateTime in) {
        return Optional.of(in.atOffset(ZoneOffset.UTC).toOffsetTime());
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull OffsetTime in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toOffsetTime());
    }

    @NonNull
    @Override
    public Optional<OffsetTime> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(OffsetTime.parse(in, FORMATTER_TZ));
        } catch (DateTimeParseException e1) {
            try {
                return LocalTimeConverter.INSTANCE.from(in).map(x -> x.atOffset(ZoneOffset.UTC));
            } catch (DateTimeParseException e2) {
                throw new ConvertionException("String inconvertible to OffsetTime.", e1, OffsetTime.class);
            }
        }
    }
}
