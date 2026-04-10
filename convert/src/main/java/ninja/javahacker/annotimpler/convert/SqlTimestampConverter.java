package ninja.javahacker.annotimpler.convert;

import java.sql.Timestamp;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum SqlTimestampConverter implements Converter<Timestamp> {
    INSTANCE;

    @FunctionalInterface
    public interface Work {
        public Optional<Timestamp> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Timestamp> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Timestamp.class);
        }
    }

    @NonNull
    @Override
    public Class<Timestamp> getType() {
        return Timestamp.class;
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDate in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull OffsetDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
