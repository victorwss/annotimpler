package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public enum LongConverter implements Converter<Long> {
    PRIMITIVE, WRAPPER;

    @NonNull
    @Override
    public Class<Long> getType() {
        return this == PRIMITIVE ? long.class : Long.class;
    }

    @NonNull
    @Override
    public Optional<Long> fromNull() {
        return this == PRIMITIVE ? Optional.of(0L) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Long> from(boolean in) {
        return Optional.of(in ? 1L : 0L);
    }

    @NonNull
    @Override
    public Optional<Long> from(byte in) {
        return Optional.of((long) in);
    }

    @NonNull
    @Override
    public Optional<Long> from(short in) {
        return Optional.of((long) in);
    }

    @NonNull
    @Override
    public Optional<Long> from(int in) {
        return Optional.of((long) in);
    }

    @NonNull
    @Override
    public Optional<Long> from(long in) {
        return Optional.of(in);
    }

    @NonNull
    private Optional<Long> from(@NonNull Class<?> what, @NonNull BigDecimal in) throws ConvertionException {
        checkNotNull(what);
        checkNotNull(in);
        try {
            return Optional.of(in.longValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, what, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Long> from(float in) throws ConvertionException {
        return from(float.class, FloatAndDouble.makeBig(in, getType()));
    }

    @NonNull
    @Override
    public Optional<Long> from(double in) throws ConvertionException {
        return from(double.class, FloatAndDouble.makeBig(in, getType()));
    }

    @NonNull
    @Override
    public Optional<Long> from(@NonNull BigDecimal in) throws ConvertionException {
        return from(BigDecimal.class, in);
    }

    @NonNull
    @Override
    public Optional<Long> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0L) : Optional.empty();
        try {
            return Optional.of(Long.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, getType());
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
