package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum InstantConverter implements Converter<Instant> {
    INSTANCE;

    @FunctionalInterface
    public interface Work {
        public Optional<Instant> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Instant> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Instant.class);
        }
    }

    @NonNull
    @Override
    public Class<Instant> getType() {
        return Instant.class;
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull LocalDate in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull LocalDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull OffsetDateTime in) {
        return OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant);
    }

    @NonNull
    @Override
    public Optional<Instant> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> OffsetDateTimeConverter.INSTANCE.from(in).map(OffsetDateTime::toInstant));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
