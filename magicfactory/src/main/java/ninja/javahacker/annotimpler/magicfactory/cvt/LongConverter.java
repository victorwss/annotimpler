package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class LongConverter implements Converter<Long> {

    public LongConverter() {
    }

    @Override
    public Long from(boolean in) {
        return in ? 1L : 0L;
    }

    @Override
    public Long from(byte in) {
        return (long) in;
    }

    @Override
    public Long from(short in) {
        return (long) in;
    }

    @Override
    public Long from(int in) {
        return (long) in;
    }

    @Override
    public Long from(long in) {
        return in;
    }

    @Override
    public Long from(float in) {
        return (long) in;
    }

    @Override
    public Long from(double in) {
        return (long) in;
    }

    @Override
    public Long from(@NonNull BigDecimal in) {
        return in.longValue();
    }

    @Override
    public Long from(@NonNull String in) {
        try {
            return Long.valueOf(in);
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException();
        }
    }
}
