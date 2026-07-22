package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// A [Converter] for `double` and [Double] values.
///
/// [#PRIMITIVE] targets `double.class` and returns `Optional.of(0.0)` for `null` input.
/// [#WRAPPER] targets `Double.class` and returns empty for `null` input.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`, `long` (range check),
/// `float` (special handling for infinity/NaN/negative-zero), `double`,
/// [BigDecimal] (roundtrip check), [String] (roundtrip parse check; empty → 0.0 for [#PRIMITIVE] or empty for [#WRAPPER]).
/// Infinity and NaN from [BigDecimal] throw [ConvertionException].
@SuppressFBWarnings("OI_OPTIONAL_ISSUES_PRIMITIVE_VARIANT_PREFERRED")
public enum DoubleConverter implements Converter<Double> {

    /// Targets `double.class`.
    PRIMITIVE,

    /// Targets `Double.class`.
    WRAPPER;

    /// Returns `double.class` for [#PRIMITIVE] or `Double.class` for [#WRAPPER].
    ///
    /// @return `double.class` for [#PRIMITIVE] or `Double.class` for [#WRAPPER].
    @NonNull
    @Override
    public Class<Double> getType() {
        return this == PRIMITIVE ? double.class : Double.class;
    }

    /// Returns `Optional.of(0.0)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    ///
    /// @return `Optional.of(0.0)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    @NonNull
    @Override
    public Optional<Double> fromNull() {
        return this == PRIMITIVE ? Optional.of(0.0) : Optional.empty();
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(boolean in) {
        return Optional.of(in ? 1.0 : 0.0);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(byte in) {
        return Optional.of((double) in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(short in) {
        return Optional.of((double) in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(int in) {
        return Optional.of((double) in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(long in) throws ConvertionException {
        double a = in;
        if (in != (long) a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    private Optional<Double> from(@NonNull Class<?> what, @NonNull BigDecimal in) throws ConvertionException {
        checkNotNull(what); // Check recognized by lombok.
        checkNotNull(in); // Check recognized by lombok.
        var a = in.doubleValue();
        if (in.compareTo(FloatAndDouble.makeBig(a)) != 0) throw new ConvertionException(what, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(float in) throws ConvertionException {
        if (in == Float.POSITIVE_INFINITY) return Optional.of(Double.POSITIVE_INFINITY);
        if (in == Float.NEGATIVE_INFINITY) return Optional.of(Double.NEGATIVE_INFINITY);
        if (Float.isNaN(in)) return Optional.of(Double.NaN);
        if (Float.floatToRawIntBits(in) == Integer.MIN_VALUE) return Optional.of(-0.0);
        return from(float.class, FloatAndDouble.makeBig(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(double in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(@NonNull BigDecimal in) throws ConvertionException {
        return from(BigDecimal.class, in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Double> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0.0) : Optional.empty();
        try {
            var a = Double.valueOf(in);
            var b = StringConverter.INSTANCE.from(a).get();
            if (!in.equals(b)) throw new ConvertionException(String.class, getType());
            return Optional.of(a);
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, getType());
        }
    }

    /// Returns `[DoubleConverter-PRIMITIVE]` or `[DoubleConverter-WRAPPER]`, depending on which instance this method is called.
    ///
    /// @return `[DoubleConverter-PRIMITIVE]` or `[DoubleConverter-WRAPPER]`.
    @NonNull
    @Override
    public String toString() {
        return "[DoubleConverter-" + name() + "]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
