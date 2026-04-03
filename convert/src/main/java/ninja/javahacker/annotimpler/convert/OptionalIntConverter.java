package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum OptionalIntConverter implements Converter<OptionalInt> {
    INSTANCE;

    @NonNull
    @Override
    public Class<OptionalInt> getType() {
        return OptionalInt.class;
    }

    @NonNull
    @Override
    public Optional<OptionalInt> fromNull() {
        return Optional.of(OptionalInt.empty());
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(boolean in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(byte in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(short in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(int in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(long in) throws ConvertionException {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(float in) throws ConvertionException {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(double in) throws ConvertionException {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(@NonNull BigDecimal in) throws ConvertionException {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(@NonNull String in) throws ConvertionException {
        return Optional.of(IntegerConverter.WRAPPER.from(in).map(OptionalInt::of).orElse(OptionalInt.empty()));
    }
}
