package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum BigDecimalConverter implements Converter<BigDecimal> {
    INSTANCE;

    @NonNull
    private static final String BAD = "Can't read value as BigDecimal.";

    @NonNull
    @Override
    public Optional<BigDecimal> from(boolean in) {
        return Optional.of(in ? BigDecimal.ONE : BigDecimal.ZERO);
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(byte in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(short in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(int in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(long in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(float in) throws ConvertionException {
        try {
            return Optional.of(BigDecimal.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, BigDecimal.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(double in) throws ConvertionException {
        try {
            return Optional.of(BigDecimal.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, BigDecimal.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(@NonNull BigDecimal in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(new BigDecimal(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, BigDecimal.class);
        }
    }
}
