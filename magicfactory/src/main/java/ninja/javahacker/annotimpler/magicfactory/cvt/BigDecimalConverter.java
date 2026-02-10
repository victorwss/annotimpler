package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum BigDecimalConverter implements Converter<BigDecimal> {
    INSTANCE;

    @Override
    public BigDecimal from(boolean in) {
        return in ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal from(byte in) {
        return BigDecimal.valueOf(in);
    }

    @Override
    public BigDecimal from(short in) {
        return BigDecimal.valueOf(in);
    }

    @Override
    public BigDecimal from(int in) {
        return BigDecimal.valueOf(in);
    }

    @Override
    public BigDecimal from(long in) {
        return BigDecimal.valueOf(in);
    }

    @Override
    public BigDecimal from(float in) {
        return BigDecimal.valueOf(in);
    }

    @Override
    public BigDecimal from(double in) {
        return BigDecimal.valueOf(in);
    }

    @Override
    public BigDecimal from(@NonNull BigDecimal in) {
        return in;
    }

    @Override
    public BigDecimal from(@NonNull String in) {
        try {
            return new BigDecimal(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
