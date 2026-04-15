package ninja.javahacker.annotimpler.convert;

import java.sql.Time;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum SqlTimeConverter implements Converter<Time> {
    INSTANCE;

    @FunctionalInterface
    private interface Work {
        public Optional<Time> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Time> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Time.class);
        }
    }

    @NonNull
    @Override
    public Class<Time> getType() {
        return Time.class;
    }

    @NonNull
    @Override
    public Optional<Time> from(@NonNull LocalTime in) {
        return Optional.of(Time.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<Time> from(@NonNull LocalDateTime in) {
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }

    @NonNull
    @Override
    public Optional<Time> from(@NonNull OffsetTime in) {
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }

    @NonNull
    @Override
    public Optional<Time> from(@NonNull OffsetDateTime in) {
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }

    @NonNull
    @Override
    public Optional<Time> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
