package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class DoubleConverter implements Converter<Double> {

    public DoubleConverter() {
    }

    @Override
    public Double from(boolean in) {
        return in ? 1.0 : 0.0;
    }

    @Override
    public Double from(byte in) {
        return (double) in;
    }

    @Override
    public Double from(short in) {
        return (double) in;
    }

    @Override
    public Double from(int in) {
        return (double) in;
    }

    @Override
    public Double from(long in) {
        return (double) in;
    }

    @Override
    public Double from(float in) {
        return (double) in;
    }

    @Override
    public Double from(double in) {
        return in;
    }

    @Override
    public Double from(@NonNull BigDecimal in) {
        return in.doubleValue();
    }

    @Override
    public Double from(@NonNull String in) {
        try {
            return Double.valueOf(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
