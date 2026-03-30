package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum OptionalDoubleConverter implements Converter<OptionalDouble> {
    INSTANCE;

    @NonNull
    @Override
    public Optional<OptionalDouble> fromNull() {
        return Optional.of(OptionalDouble.empty());
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(boolean in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(byte in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(short in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(int in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(long in) throws ConvertionException {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(float in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(double in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(@NonNull BigDecimal in) throws ConvertionException {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    @NonNull
    @Override
    public Optional<OptionalDouble> from(@NonNull String in) throws ConvertionException {
        return Optional.of(DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of).orElse(OptionalDouble.empty()));
    }
}
