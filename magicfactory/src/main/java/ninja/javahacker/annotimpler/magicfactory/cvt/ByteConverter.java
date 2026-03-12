package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum ByteConverter implements Converter<Byte> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as byte.";

    @NonNull
    @Override
    public Optional<Byte> fromNull() {
        return this == PRIMITIVE ? Optional.of((byte) 0) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Byte> from(boolean in) {
        return Optional.of(in ? (byte) 1 : (byte) 0);
    }

    @NonNull
    @Override
    public Optional<Byte> from(byte in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Byte> from(short in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(BAD, byte.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(int in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(BAD, byte.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(long in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(BAD, byte.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(float in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(BAD, byte.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(double in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(BAD, byte.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.byteValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(BAD, x, byte.class);
        }
    }

    @NonNull
    @Override
    public Optional<Byte> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of((byte) 0) : Optional.empty();
        try {
            return Optional.of(Byte.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, byte.class);
        }
    }
}
