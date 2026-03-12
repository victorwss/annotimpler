package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum LongConverter implements Converter<Long> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as long.";

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
    @Override
    public Optional<Long> from(float in) throws ConvertionException {
        var a = (long) in;
        if (in != a) throw new ConvertionException(BAD, long.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Long> from(double in) throws ConvertionException {
        var a = (long) in;
        if (in != a) throw new ConvertionException(BAD, long.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Long> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.longValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(BAD, x, byte.class);
        }
    }

    @NonNull
    @Override
    public Optional<Long> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0L) : Optional.empty();
        try {
            return Optional.of(Long.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, long.class);
        }
    }
}
