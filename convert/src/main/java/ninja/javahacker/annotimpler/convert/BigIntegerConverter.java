package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

/// A [Converter] for [BigInteger] values.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`, `long`,
/// `float`/`double` (throws for infinity/NaN/fractional values),
/// [BigDecimal] (exact), [String] (parsed; empty → empty).
public enum BigIntegerConverter implements Converter<BigInteger> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `BigInteger.class`.
    ///
    /// @return `BigInteger.class`.
    @NonNull
    @Override
    public Class<BigInteger> getType() {
        return BigInteger.class;
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(boolean in) {
        return Optional.of(in ? BigInteger.ONE : BigInteger.ZERO);
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(byte in) {
        return Optional.of(BigInteger.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(short in) {
        return Optional.of(BigInteger.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(int in) {
        return Optional.of(BigInteger.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(long in) {
        return Optional.of(BigInteger.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(float in) throws ConvertionException {
        if (in == Float.POSITIVE_INFINITY || in == Float.NEGATIVE_INFINITY || Float.isNaN(in)) {
            throw new ConvertionException(float.class, BigInteger.class);
        }
        try {
            return Optional.of(BigDecimal.valueOf(in).toBigIntegerExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, float.class, BigInteger.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(double in) throws ConvertionException {
        if (in == Double.POSITIVE_INFINITY || in == Double.NEGATIVE_INFINITY || Double.isNaN(in)) {
            throw new ConvertionException(double.class, BigInteger.class);
        }
        try {
            return Optional.of(BigDecimal.valueOf(in).toBigIntegerExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, double.class, BigInteger.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.toBigIntegerExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, BigDecimal.class, BigInteger.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(new BigInteger(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, BigInteger.class);
        }
    }
}
