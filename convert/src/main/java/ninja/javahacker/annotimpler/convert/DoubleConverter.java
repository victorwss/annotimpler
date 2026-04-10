package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public enum DoubleConverter implements Converter<Double> {
    PRIMITIVE, WRAPPER;

    @NonNull
    @Override
    public Class<Double> getType() {
        return this == PRIMITIVE ? double.class : Double.class;
    }

    @NonNull
    @Override
    public Optional<Double> fromNull() {
        return this == PRIMITIVE ? Optional.of(0.0) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Double> from(boolean in) {
        return Optional.of(in ? 1.0 : 0.0);
    }

    @NonNull
    @Override
    public Optional<Double> from(byte in) {
        return Optional.of((double) in);
    }

    @NonNull
    @Override
    public Optional<Double> from(short in) {
        return Optional.of((double) in);
    }

    @NonNull
    @Override
    public Optional<Double> from(int in) {
        return Optional.of((double) in);
    }

    @NonNull
    @Override
    public Optional<Double> from(long in) throws ConvertionException {
        double a = in;
        if (in != (long) a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    private Optional<Double> from(@NonNull Class<?> what, @NonNull BigDecimal in) throws ConvertionException {
        checkNotNull(what);
        checkNotNull(in);
        var a = in.doubleValue();
        if (in.compareTo(FloatAndDouble.makeBig(a)) != 0) throw new ConvertionException(what, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Double> from(float in) throws ConvertionException {
        if (in == Float.POSITIVE_INFINITY) return Optional.of(Double.POSITIVE_INFINITY);
        if (in == Float.NEGATIVE_INFINITY) return Optional.of(Double.NEGATIVE_INFINITY);
        if (Float.isNaN(in)) return Optional.of(Double.NaN);
        if (Float.floatToRawIntBits(in) == Integer.MIN_VALUE) return Optional.of(-0.0);
        return from(float.class, FloatAndDouble.makeBig(in));
    }

    @NonNull
    @Override
    public Optional<Double> from(double in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Double> from(@NonNull BigDecimal in) throws ConvertionException {
        return from(BigDecimal.class, in);
    }

    @NonNull
    @Override
    public Optional<Double> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0.0) : Optional.empty();
        try {
            var a = Double.valueOf(in);
            var b = StringConverter.INSTANCE.from(a).get();
            //if (!List.of("NaN", "Infinity", "-Infinity").contains(b) && !b.contains(".")) b += ".0";
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
