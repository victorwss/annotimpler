package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum LocalDateConverter implements Converter<LocalDate> {
    INSTANCE;

    @NonNull
    public static final DateTimeFormatter FORMATTER_D = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

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
        try {
            return Optional.of(LocalDate.parse(in, FORMATTER_D));
        } catch (DateTimeParseException e) {
            throw new ConvertionException(e, String.class, LocalDate.class);
        }
    }
}
