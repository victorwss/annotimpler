package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum ShortConverter implements Converter<Short> {
    INSTANCE;

    @Override
    public Short from(boolean in) {
        return in ? (short) 1 : (short) 0;
    }

    @Override
    public Short from(byte in) {
        return (short) in;
    }

    @Override
    public Short from(short in) {
        return in;
    }

    @Override
    public Short from(int in) {
        return (short) in;
    }

    @Override
    public Short from(long in) {
        return (short) in;
    }

    @Override
    public Short from(float in) {
        return (short) in;
    }

    @Override
    public Short from(double in) {
        return (short) in;
    }

    @Override
    public Short from(@NonNull BigDecimal in) {
        return in.shortValue();
    }

    @Override
    public Short from(@NonNull String in) {
        try {
            return Short.valueOf(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
