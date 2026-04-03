package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum BigDecimalConverter implements Converter<BigDecimal> {
    INSTANCE;

    @NonNull
    private static final String BAD = "Can't read value as BigDecimal.";

    @NonNull
    @Override
    public Class<BigDecimal> getType() {
        return BigDecimal.class;
    }

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
            return Optional.of(FloatAndDouble.makeBig(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, float.class, BigDecimal.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(double in) throws ConvertionException {
        try {
            return Optional.of(FloatAndDouble.makeBig(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, double.class, BigDecimal.class);
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
            throw new ConvertionException(BAD, x, String.class, BigDecimal.class);
        }
    }
}
