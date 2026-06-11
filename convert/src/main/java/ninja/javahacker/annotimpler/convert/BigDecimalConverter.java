package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

/// A [Converter] for [BigDecimal] values.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`, `long`,
/// `float`/`double` (via `FloatAndDouble.makeBig`; throws for infinity/NaN),
/// [BigDecimal], [String] (parsed; empty → empty).
public enum BigDecimalConverter implements Converter<BigDecimal> {

    /// Singeton instance.
    INSTANCE;

    /// Returns `BigDecimal.class`.
    ///
    /// @return `BigDecimal.class`.
    @NonNull
    @Override
    public Class<BigDecimal> getType() {
        return BigDecimal.class;
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(boolean in) {
        return Optional.of(in ? BigDecimal.ONE : BigDecimal.ZERO);
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(byte in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(short in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(int in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(long in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(float in) throws ConvertionException {
        return Optional.of(FloatAndDouble.makeBig(in, getType()));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(double in) throws ConvertionException {
        return Optional.of(FloatAndDouble.makeBig(in, getType()));
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(@NonNull BigDecimal in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<BigDecimal> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(new BigDecimal(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, BigDecimal.class);
        }
    }
}
