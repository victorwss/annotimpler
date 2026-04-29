package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum LocalDateTimeConverter implements Converter<LocalDateTime> {
    INSTANCE;

    @NonNull
    @Override
    public Class<LocalDateTime> getType() {
        return LocalDateTime.class;
    }

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
            return Optional.of(MultiFormatters.YMD_DASH.parseLocalDateTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
