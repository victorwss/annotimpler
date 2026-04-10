package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public enum BooleanConverter implements Converter<Boolean> {
    PRIMITIVE, WRAPPER;

    @NonNull
    @Override
    public Class<Boolean> getType() {
        return this == PRIMITIVE ? boolean.class : Boolean.class;
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
        if (in != 0 && in != 1) throw new ConvertionException(byte.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(short in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(short.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(int in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(int.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(long in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(long.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(float in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(float.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(double in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(double.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            var a = in.byteValueExact();
            if (a != 0 && a != 1) throw new ConvertionException(BigDecimal.class, getType());
            return Optional.of(a != 0);
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Boolean> from(@NonNull String in) throws ConvertionException {
        var n = in.toUpperCase(Locale.ROOT);
        if ("TRUE".equals(n)) return Optional.of(true);
        if ("FALSE".equals(n)) return Optional.of(false);
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(false) : Optional.empty();
        throw new ConvertionException(String.class, getType());
    }
}
