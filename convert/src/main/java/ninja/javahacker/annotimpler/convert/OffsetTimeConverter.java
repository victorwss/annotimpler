package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum OffsetTimeConverter implements Converter<OffsetTime> {
    INSTANCE;

    @NonNull
    @Override
    public Class<OffsetTime> getType() {
        return OffsetTime.class;
    }

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
        return Optional.of(MultiFormatters.parseOffsetTime(in));
    }
}
