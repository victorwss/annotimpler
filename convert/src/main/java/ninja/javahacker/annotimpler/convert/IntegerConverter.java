package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// A [Converter] for `int` and [Integer] values.
///
/// [#PRIMITIVE] targets `int.class` and returns `Optional.of(0)` for `null` input.
/// [#WRAPPER] targets `Integer.class` and returns empty for `null` input.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`, `long` (range check),
/// `float` (via [BigDecimal] exact), `double` (range check), [BigDecimal] (exact),
/// [String] (parsed; empty → 0 for [#PRIMITIVE] or empty for [#WRAPPER]).
public enum IntegerConverter implements Converter<Integer> {

    /// Targets `int.class`.
    PRIMITIVE,

    /// Targets `Integer.class`.
    WRAPPER;

    /// Returns `int.class` for [#PRIMITIVE] or `Integer.class` for [#WRAPPER].
    ///
    /// @return `int.class` for [#PRIMITIVE] or `Integer.class` for [#WRAPPER].
    @NonNull
    @Override
    public Class<Integer> getType() {
        return this == PRIMITIVE ? int.class : Integer.class;
    }

    /// Returns `Optional.of(0)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    @NonNull
    @Override
    public Optional<Integer> fromNull() {
        return this == PRIMITIVE ? Optional.of(0) : Optional.empty();
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(boolean in) {
        return Optional.of(in ? 1 : 0);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(byte in) {
        return Optional.of((int) in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(short in) {
        return Optional.of((int) in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(int in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(long in) throws ConvertionException {
        var a = (int) in;
        if (in != a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    private Optional<Integer> from(@NonNull Class<?> what, @NonNull BigDecimal in) throws ConvertionException {
        checkNotNull(what); // Check recognized by lombok.
        checkNotNull(in); // Check recognized by lombok.
        try {
            return Optional.of(in.intValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, what, getType());
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(float in) throws ConvertionException {
        return from(float.class, FloatAndDouble.makeBig(in, getType()));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public Optional<Integer> from(double in) throws ConvertionException {
        var a = (int) in;
        if (in != a) throw new ConvertionException(double.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(@NonNull BigDecimal in) throws ConvertionException {
        return from(BigDecimal.class, in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Integer> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(0) : Optional.empty();
        try {
            return Optional.of(Integer.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, getType());
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
