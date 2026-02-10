package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum ByteConverter implements Converter<Byte> {
    INSTANCE;

    @Override
    public Byte from(boolean in) {
        return in ? (byte) 1 : (byte) 0;
    }

    @Override
    public Byte from(byte in) {
        return in;
    }

    @Override
    public Byte from(short in) {
        return (byte) in;
    }

    @Override
    public Byte from(int in) {
        return (byte) in;
    }

    @Override
    public Byte from(long in) {
        return (byte) in;
    }

    @Override
    public Byte from(float in) {
        return (byte) in;
    }

    @Override
    public Byte from(double in) {
        return (byte) in;
    }

    @Override
    public Byte from(@NonNull BigDecimal in) {
        return in.byteValue();
    }

    @Override
    public Byte from(@NonNull String in) {
        try {
            return Byte.valueOf(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
