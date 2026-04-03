package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum ShortConverter implements Converter<Short> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as $$$.";

    @NonNull
    @Override
    public Class<Short> getType() {
        return this == PRIMITIVE ? short.class : Short.class;
    }

    private String bad() {
        return BAD.replace("$$$", getType().getSimpleName());
    }

    @NonNull
    @Override
    public Optional<Short> fromNull() {
        return this == PRIMITIVE ? Optional.of((short) 0) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Short> from(boolean in) {
        return Optional.of(in ? (short) 1 : (short) 0);
    }

    @NonNull
    @Override
    public Optional<Short> from(byte in) {
        return Optional.of((short) in);
    }

    @NonNull
    @Override
    public Optional<Short> from(short in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Short> from(int in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(bad(), int.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(long in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(bad(), long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(float in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(bad(), float.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(double in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(bad(), double.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.shortValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(bad(), x, BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Short> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of((short) 0) : Optional.empty();
        try {
            return Optional.of(Short.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(bad(), x, String.class, getType());
        }
    }
}
