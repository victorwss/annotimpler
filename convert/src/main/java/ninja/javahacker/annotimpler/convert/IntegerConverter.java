package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public enum IntegerConverter implements Converter<Integer> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as $$$.";

    @NonNull
    @Override
    public Class<Integer> getType() {
        return this == PRIMITIVE ? int.class : Integer.class;
    }

    private String bad() {
        return BAD.replace("$$$", getType().getSimpleName());
    }

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
        if (in != a) throw new ConvertionException(bad(), long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    private Optional<Integer> from(@NonNull Class<?> what, @NonNull BigDecimal in) throws ConvertionException {
        checkNotNull(what);
        checkNotNull(in);
        try {
            return Optional.of(in.intValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(bad(), x, what, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Integer> from(float in) throws ConvertionException {
        return from(float.class, FloatAndDouble.makeBig(in));
    }

    @NonNull
    @Override
    public Optional<Integer> from(double in) throws ConvertionException {
        var a = (int) in;
        if (in != a) throw new ConvertionException(bad(), double.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Integer> from(@NonNull BigDecimal in) throws ConvertionException {
        return from(BigDecimal.class, in);
    }

    @NonNull
    @Override
    public Optional<Integer> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0) : Optional.empty();
        try {
            return Optional.of(Integer.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(bad(), x, String.class, getType());
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
