package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum ByteConverter implements Converter<Byte> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as $$$.";

    @NonNull
    @Override
    public Class<Byte> getType() {
        return this == PRIMITIVE ? byte.class : Byte.class;
    }

    private String bad() {
        return BAD.replace("$$$", getType().getSimpleName());
    }

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
        if (in != a) throw new ConvertionException(bad(), short.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(int in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(bad(), int.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(long in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(bad(), long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(float in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(bad(), float.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(double in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(bad(), double.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Byte> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.byteValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(bad(), x, BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Byte> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of((byte) 0) : Optional.empty();
        try {
            return Optional.of(Byte.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(bad(), x, String.class, getType());
        }
    }
}
