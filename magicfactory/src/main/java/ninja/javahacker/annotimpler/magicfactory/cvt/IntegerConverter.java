package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum IntegerConverter implements Converter<Integer> {
    INSTANCE;

    @Override
    public Integer from(boolean in) {
        return in ? 1 : 0;
    }

    @Override
    public Integer from(byte in) {
        return (int) in;
    }

    @Override
    public Integer from(short in) {
        return (int) in;
    }

    @Override
    public Integer from(int in) {
        return in;
    }

    @Override
    public Integer from(long in) {
        return (int) in;
    }

    @Override
    public Integer from(float in) {
        return (int) in;
    }

    @Override
    public Integer from(double in) {
        return (int) in;
    }

    @Override
    public Integer from(@NonNull BigDecimal in) {
        return in.intValue();
    }

    @Override
    public Integer from(@NonNull String in) {
        try {
            return Integer.valueOf(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
