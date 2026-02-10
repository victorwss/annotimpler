package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum FloatConverter implements Converter<Float> {
    INSTANCE;

    @Override
    public Float from(boolean in) {
        return in ? 1F : 0F;
    }

    @Override
    public Float from(byte in) {
        return (float) in;
    }

    @Override
    public Float from(short in) {
        return (float) in;
    }

    @Override
    public Float from(int in) {
        return (float) in;
    }

    @Override
    public Float from(long in) {
        return (float) in;
    }

    @Override
    public Float from(float in) {
        return in;
    }

    @Override
    public Float from(double in) {
        return (float) in;
    }

    @Override
    public Float from(@NonNull BigDecimal in) {
        return in.floatValue();
    }

    @Override
    public Float from(@NonNull String in) {
        try {
            return Float.valueOf(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
