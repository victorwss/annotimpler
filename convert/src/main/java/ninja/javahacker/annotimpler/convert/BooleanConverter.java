package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum BooleanConverter implements Converter<Boolean> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as $$$.";

    @NonNull
    @Override
    public Class<Boolean> getType() {
        return this == PRIMITIVE ? boolean.class : Boolean.class;
    }

    private String bad() {
        return BAD.replace("$$$", getType().getSimpleName());
    }

    @NonNull
    @Override
    public Optional<Boolean> fromNull() {
        return this == PRIMITIVE ? Optional.of(false) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Boolean> from(boolean in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(byte in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(bad(), byte.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(short in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(bad(), short.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(int in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(bad(), int.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(long in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(bad(), long.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(float in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(bad(), float.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(double in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(bad(), double.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            var a = in.byteValueExact();
            if (a != 0 && a != 1) throw new ConvertionException(bad(), BigDecimal.class, getType());
            return Optional.of(a != 0);
        } catch (ArithmeticException x) {
            throw new ConvertionException(bad(), x, BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Boolean> from(@NonNull String in) throws ConvertionException {
        var n = in.toUpperCase(Locale.ROOT);
        if ("TRUE".equals(n)) return Optional.of(true);
        if ("FALSE".equals(n)) return Optional.of(false);
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(false) : Optional.empty();
        throw new ConvertionException(bad(), String.class, getType());
    }
}
