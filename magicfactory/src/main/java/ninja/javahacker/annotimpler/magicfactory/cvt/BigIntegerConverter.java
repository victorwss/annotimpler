package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum BigIntegerConverter implements Converter<BigInteger> {
    INSTANCE;

    @Override
    public BigInteger from(boolean in) {
        return in ? BigInteger.ONE : BigInteger.ZERO;
    }

    @Override
    public BigInteger from(byte in) {
        return BigInteger.valueOf(in);
    }

    @Override
    public BigInteger from(short in) {
        return BigInteger.valueOf(in);
    }

    @Override
    public BigInteger from(int in) {
        return BigInteger.valueOf(in);
    }

    @Override
    public BigInteger from(long in) {
        return BigInteger.valueOf(in);
    }

    @Override
    public BigInteger from(float in) {
        return BigDecimal.valueOf(in).toBigInteger();
    }

    @Override
    public BigInteger from(double in) {
        return BigDecimal.valueOf(in).toBigInteger();
    }

    @Override
    public BigInteger from(@NonNull BigDecimal in) {
        return in.toBigInteger();
    }

    @Override
    public BigInteger from(@NonNull String in) {
        try {
            return new BigInteger(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
