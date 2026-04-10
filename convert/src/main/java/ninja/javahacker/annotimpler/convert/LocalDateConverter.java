package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum LocalDateConverter implements Converter<LocalDate> {
    INSTANCE;

    @NonNull
    @Override
    public Class<LocalDate> getType() {
        return LocalDate.class;
    }

    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull LocalDate in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull LocalDateTime in) {
        return Optional.of(in.toLocalDate());
    }

    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull OffsetDateTime in) {
        return Optional.of(in.toLocalDate());
    }

    @NonNull
    @Override
    public Optional<LocalDate> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        return Optional.of(MultiFormatters.parseLocalDate(in));
    }
}
