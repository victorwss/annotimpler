package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;

/// A [Converter] for `byte` and [Byte] values.
///
/// [#PRIMITIVE] targets `byte.class` and returns `Optional.of((byte) 0)` for `null` input.
/// [#WRAPPER] targets `Byte.class` and returns empty for `null` input.
///
/// Supported conversions: `boolean` (`true` → 1, `false` → 0), `byte`,
/// `short`/`int`/`long`/`float`/`double` (with range check), [BigDecimal] (exact),
/// [String] (parsed; empty → 0 for [#PRIMITIVE] or empty for [#WRAPPER]).
public enum ByteConverter implements Converter<Byte> {

    /// Targets `byte.class`.
    PRIMITIVE,

    /// Targets `Byte.class`.
    WRAPPER;

    /// Returns `byte.class` for [#PRIMITIVE] or `Byte.class` for [#WRAPPER].
    ///
    /// @return `byte.class` for [#PRIMITIVE] or `Byte.class` for [#WRAPPER].
    @NonNull
    @Override
    public Class<Byte> getType() {
        return this == PRIMITIVE ? byte.class : Byte.class;
    }

    /// Returns `Optional.of((byte) 0)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    @NonNull
    @Override
    public Optional<Byte> fromNull() {
        return this == PRIMITIVE ? Optional.of((byte) 0) : Optional.empty();
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(boolean in) {
        return Optional.of(in ? (byte) 1 : (byte) 0);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(byte in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(short in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(short.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(int in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(int.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(long in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public Optional<Byte> from(float in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(float.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public Optional<Byte> from(double in) throws ConvertionException {
        var a = (byte) in;
        if (in != a) throw new ConvertionException(double.class, getType());
        return Optional.of(a);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.byteValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, BigDecimal.class, getType());
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Byte> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of((byte) 0) : Optional.empty();
        try {
            return Optional.of(Byte.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, getType());
        }
    }
}
