package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum BigIntegerConverter implements Converter<BigInteger> {
    INSTANCE;

    @NonNull
    private static final String BAD = "Can't read value as BigInteger.";

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
        try {
            return Optional.of(BigDecimal.valueOf(in).toBigIntegerExact());
        } catch (NumberFormatException | ArithmeticException x) {
            throw new ConvertionException(BAD, x, BigDecimal.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(double in) throws ConvertionException {
        try {
            return Optional.of(BigDecimal.valueOf(in).toBigIntegerExact());
        } catch (NumberFormatException | ArithmeticException x) {
            throw new ConvertionException(BAD, x, BigDecimal.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.toBigIntegerExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(BAD, x, BigInteger.class);
        }
    }

    @NonNull
    @Override
    public Optional<BigInteger> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(new BigInteger(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, BigInteger.class);
        }
    }
}
