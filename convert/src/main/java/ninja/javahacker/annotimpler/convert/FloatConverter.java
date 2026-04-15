package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public enum FloatConverter implements Converter<Float> {
    PRIMITIVE, WRAPPER;

    @NonNull
    @Override
    public Class<Float> getType() {
        return this == PRIMITIVE ? float.class : Float.class;
    }

    @NonNull
    @Override
    public Optional<Float> fromNull() {
        return this == PRIMITIVE ? Optional.of(0F) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Float> from(boolean in) {
        return Optional.of(in ? 1F : 0F);
    }

    @NonNull
    @Override
    public Optional<Float> from(byte in) {
        return Optional.of((float) in);
    }

    @NonNull
    @Override
    public Optional<Float> from(short in) {
        return Optional.of((float) in);
    }

    @NonNull
    @Override
    public Optional<Float> from(int in) throws ConvertionException {
        float a = in;
        if (in != (int) a) throw new ConvertionException(int.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(long in) throws ConvertionException {
        float a = in;
        if (in != (long) a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(float in) {
        return Optional.of(in);
    }

    @NonNull
    private Optional<Float> from(@NonNull Class<?> what, @NonNull BigDecimal in) throws ConvertionException {
        checkNotNull(what);
        checkNotNull(in);
        var a = in.floatValue();
        if (in.compareTo(FloatAndDouble.makeBig(a)) != 0) throw new ConvertionException(what, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(double in) throws ConvertionException {
        if (in == Double.POSITIVE_INFINITY) return Optional.of(Float.POSITIVE_INFINITY);
        if (in == Double.NEGATIVE_INFINITY) return Optional.of(Float.NEGATIVE_INFINITY);
        if (Double.isNaN(in)) return Optional.of(Float.NaN);
        if (Double.doubleToRawLongBits(in) == Long.MIN_VALUE) return Optional.of(-0.0F);
        return from(double.class, FloatAndDouble.makeBig(in));
    }

    @NonNull
    @Override
    public Optional<Float> from(@NonNull BigDecimal in) throws ConvertionException {
        return from(BigDecimal.class, in);
    }

    @NonNull
    @Override
    public Optional<Float> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0.0f) : Optional.empty();
        try {
            var a = Float.valueOf(in);
            var b = StringConverter.INSTANCE.from(a).get();
            if (!in.equals(b)) throw new ConvertionException(String.class, getType());
            return Optional.of(a);
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, getType());
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
