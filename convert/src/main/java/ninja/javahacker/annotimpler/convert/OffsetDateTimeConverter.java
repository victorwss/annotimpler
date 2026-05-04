package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module ninja.javahacker.annotimpler.datetime;

public enum OffsetDateTimeConverter implements Converter<OffsetDateTime> {
    INSTANCE;

    @NonNull
    @Override
    public Class<OffsetDateTime> getType() {
        return OffsetDateTime.class;
    }

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
            return Optional.of(MultiFormatters.YMD_DASH.parseOffsetDateTime(in));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, getType());
        }
    }
}
