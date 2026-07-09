package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;

/// A [Converter] for [BigDecimal] values.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`, `long`,
/// `float`/`double` (via `FloatAndDouble.makeBig`; throws for infinity/NaN),
/// [BigDecimal], [String] (parsed; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum BigDecimalConverter implements Converter<BigDecimal> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `BigDecimal.class`.
    ///
    /// @return `BigDecimal.class`.
    @NonNull
    @Override
    public Class<BigDecimal> getType() {
        return BigDecimal.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(boolean in) {
        return Optional.of(in ? BigDecimal.ONE : BigDecimal.ZERO);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(byte in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(short in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(int in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(long in) {
        return Optional.of(BigDecimal.valueOf(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(float in) throws ConvertionException {
        return Optional.of(FloatAndDouble.makeBig(in, getType()));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(double in) throws ConvertionException {
        return Optional.of(FloatAndDouble.makeBig(in, getType()));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<BigDecimal> from(@NonNull BigDecimal in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
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
