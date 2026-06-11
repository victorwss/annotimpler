package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

/// A [Converter] for `short` and [Short] values.
///
/// [#PRIMITIVE] targets `short.class` and returns `Optional.of((short) 0)` for `null` input.
/// [#WRAPPER] targets `Short.class` and returns empty for `null` input.
///
/// Supported conversions: `boolean`, `byte`, `short`,
/// `int`/`long`/`float`/`double` (with range check), [BigDecimal] (exact),
/// [String] (parsed; empty → 0 for [#PRIMITIVE] or empty for [#WRAPPER]).
public enum ShortConverter implements Converter<Short> {

    /// Targets `short.class`.
    PRIMITIVE,

    /// Targets `Short.class`.
    WRAPPER;

    /// Returns `short.class` for [#PRIMITIVE] or `Short.class` for [#WRAPPER].
    @NonNull
    @Override
    public Class<Short> getType() {
        return this == PRIMITIVE ? short.class : Short.class;
    }

    /// Returns `Optional.of((short) 0)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    ///
    /// @return `Optional.of((short) 0)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    @NonNull
    @Override
    public Optional<Short> fromNull() {
        return this == PRIMITIVE ? Optional.of((short) 0) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Short> from(boolean in) {
        return Optional.of(in ? (short) 1 : (short) 0);
    }

    @NonNull
    @Override
    public Optional<Short> from(byte in) {
        return Optional.of((short) in);
    }

    @NonNull
    @Override
    public Optional<Short> from(short in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Short> from(int in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(int.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(long in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(float in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(float.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(double in) throws ConvertionException {
        var a = (short) in;
        if (in != a) throw new ConvertionException(double.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Short> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(in.shortValueExact());
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Short> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of((short) 0) : Optional.empty();
        try {
            return Optional.of(Short.valueOf(in));
        } catch (NumberFormatException x) {
            throw new ConvertionException(x, String.class, getType());
        }
    }
}
