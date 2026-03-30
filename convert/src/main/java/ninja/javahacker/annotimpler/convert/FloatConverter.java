package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum FloatConverter implements Converter<Float> {
    PRIMITIVE, WRAPPER;

    @NonNull
    private static final String BAD = "Can't read value as float.";

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
        if (in != a) throw new ConvertionException(BAD, float.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(long in) throws ConvertionException {
        float a = in;
        if (in != a) throw new ConvertionException(BAD, float.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(float in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Float> from(double in) throws ConvertionException {
        var a = (float) in;
        if (in != a) throw new ConvertionException(BAD, float.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(@NonNull BigDecimal in) throws ConvertionException {
        var a = in.floatValue();
        if (in.compareTo(BigDecimal.valueOf(a)) != 0) throw new ConvertionException(BAD, double.class);
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Float> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0.0f) : Optional.empty();
        try {
            return Optional.of(Float.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(BAD, x, float.class);
        }
    }
}
