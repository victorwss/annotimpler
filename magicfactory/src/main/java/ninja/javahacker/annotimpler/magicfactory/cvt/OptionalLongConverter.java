package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum OptionalLongConverter implements Converter<OptionalLong> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<OptionalLong> fromNull() {
        return Optional.of(OptionalLong.empty());
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(boolean in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(byte in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(short in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(int in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(long in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(float in) throws ConvertionException {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(double in) throws ConvertionException {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull BigDecimal in) throws ConvertionException {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull String in) throws ConvertionException {
        return Optional.of(LongConverter.WRAPPER.from(in).map(OptionalLong::of).orElse(OptionalLong.empty()));
    }
}
