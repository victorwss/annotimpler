package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum SqlTimeConverter implements Converter<Time> {
    INSTANCE;

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
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }
}
