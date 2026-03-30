package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum DoubleConverter implements Converter<Double> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as double.";

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
        if (in != a) throw new ConvertionException(BAD, double.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Double> from(float in) {
        return Optional.of((double) in);
    }

    @NonNull
    @Override
    public Optional<Double> from(double in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Double> from(@NonNull BigDecimal in) throws ConvertionException {
        var a = in.doubleValue();
        if (in.compareTo(BigDecimal.valueOf(a)) != 0) throw new ConvertionException(BAD, double.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Double> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0.0) : Optional.empty();
        try {
            return Optional.of(Double.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, double.class);
        }
    }
}
