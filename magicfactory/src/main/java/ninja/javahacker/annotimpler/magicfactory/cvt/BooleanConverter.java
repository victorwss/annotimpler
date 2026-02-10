package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum BooleanConverter implements Converter<Boolean> {
    INSTANCE;

    @Override
    public Boolean from(boolean in) {
        return in;
    }

    @Override
    public Boolean from(byte in) {
        return in != 0;
    }

    @Override
    public Boolean from(short in) {
        return in != 0;
    }

    @Override
    public Boolean from(int in) {
        return in != 0;
    }

    @Override
    public Boolean from(long in) {
        return in != 0;
    }

    @Override
    public Boolean from(float in) {
        return in != 0;
    }

    @Override
    public Boolean from(double in) {
        return in != 0;
    }

    @Override
    public Boolean from(@NonNull BigDecimal in) {
        return in.signum() != 0;
    }

    @Override
    public Boolean from(@NonNull String in) {
        var n = in.toUpperCase(Locale.ROOT);
        if ("TRUE".equals(n)) return true;
        if ("FALSE".equals(n)) return false;
        if (n.isEmpty()) return null;
        throw new IllegalArgumentException();
    }
}
