package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for `char` and [Character] values.
///
/// [#PRIMITIVE] targets `char.class` and returns `Optional.of('\0')` for `null` input.
/// [#WRAPPER] targets `Character.class` and returns empty for `null` input.
///
/// Supported conversions: `boolean` (`true` → 1, `false` → 0), `byte`/`short`/`int`/`long`/`float`/`double`
/// (range check for valid char), [BigDecimal] (exact integer in char range),
/// [String] (exactly one character; empty → `'\0'` for [#PRIMITIVE] or empty for [#WRAPPER]).
public enum CharacterConverter implements Converter<Character> {

    /// Targets `char.class`.
    PRIMITIVE,

    /// Targets `Character.class`.
    WRAPPER;

    /// Returns `char.class` for [#PRIMITIVE] or `Character.class` for [#WRAPPER].
    ///
    /// @return `char.class` for [#PRIMITIVE] or `Character.class` for [#WRAPPER].
    @NonNull
    @Override
    public Class<Character> getType() {
        return this == PRIMITIVE ? char.class : Character.class;
    }

    /// Returns `Optional.of('\0')` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    @NonNull
    @Override
    public Optional<Character> fromNull() {
        return this == PRIMITIVE ? Optional.of('\0') : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Character> from(boolean in) {
        return Optional.of(in ? (char) 1 : (char) 0);
    }

    @NonNull
    @Override
    public Optional<Character> from(byte in) throws ConvertionException {
        var a = (char) (0x7F & in);
        if (in != (byte) a) throw new ConvertionException(byte.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(short in) throws ConvertionException {
        var a = (char) (0x7FFF & in);
        if (in != (short) a) throw new ConvertionException(short.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(int in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(int.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(long in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(long.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public Optional<Character> from(float in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(float.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    public Optional<Character> from(double in) throws ConvertionException {
        var a = (char) in;
        if (in != a) throw new ConvertionException(double.class, getType());
        return Optional.of(a);
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            var i = in.intValueExact();
            var a = (char) i;
            if (i != a) throw new ConvertionException(BigDecimal.class, getType());
            return Optional.of(a);
        } catch (ArithmeticException x) {
            throw new ConvertionException(x, BigDecimal.class, getType());
        }
    }

    @NonNull
    @Override
    public Optional<Character> from(@NonNull String in) throws ConvertionException {
        if (in.length() == 0) return this == PRIMITIVE ? Optional.of('\0') : Optional.empty();
        Character c = in.charAt(0);
        if (!in.equals(c.toString())) throw new ConvertionException(String.class, getType());
        return Optional.of(c);
    }
}
