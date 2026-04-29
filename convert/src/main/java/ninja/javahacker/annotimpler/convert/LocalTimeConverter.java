package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum LocalTimeConverter implements Converter<LocalTime> {
    INSTANCE;

    @NonNull
    @Override
    public Class<LocalTime> getType() {
        return LocalTime.class;
    }

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
            return Optional.of(MultiFormatters.YMD_DASH.parseLocalTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
