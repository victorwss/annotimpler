package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum IntegerConverter implements Converter<Integer> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as int.";

    @NonNull
    @Override
    public Optional<Integer> fromNull() {
        return this == PRIMITIVE ? Optional.of(0) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Integer> from(boolean in) {
        return Optional.of(in ? 1 : 0);
    }

    @NonNull
    @Override
    public Optional<Integer> from(byte in) {
        return Optional.of((int) in);
    }

    @NonNull
    @Override
    public Optional<Integer> from(short in) {
        return Optional.of((int) in);
    }

    @NonNull
    @Override
    public Optional<Integer> from(int in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Integer> from(long in) throws ConvertionException {
        var a = (int) in;
        if (in != a) throw new ConvertionException(BAD, int.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Integer> from(float in) throws ConvertionException {
        var a = (int) in;
        if (in != a) throw new ConvertionException(BAD, int.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Integer> from(double in) throws ConvertionException {
        var a = (int) in;
        if (in != a) throw new ConvertionException(BAD, int.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Integer> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.intValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(BAD, x, byte.class);
        }
    }

    @NonNull
    @Override
    public Optional<Integer> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0) : Optional.empty();
        try {
            return Optional.of(Integer.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, int.class);
        }
    }
}
