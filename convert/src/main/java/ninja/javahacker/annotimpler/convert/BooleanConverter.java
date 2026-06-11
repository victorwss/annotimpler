package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

/// A [Converter] for `boolean` and [Boolean] values.
///
/// [#PRIMITIVE] targets `boolean.class` and returns `false` for `null` input.
/// [#WRAPPER] targets `Boolean.class` and returns empty for `null` input.
///
/// Supported conversions: `boolean`, `byte`/`short`/`int`/`long`/`float`/`double` (only 0 and 1),
/// [BigDecimal] (only 0 and 1), and [String] ("true"/"TRUE"/"1" → true, "false"/"FALSE"/"0"/"-0" → false,
/// empty string → false for [#PRIMITIVE] or empty for [#WRAPPER]).
public enum BooleanConverter implements Converter<Boolean> {

    /// Targets `boolean.class`.
    PRIMITIVE,

    /// Targets `Boolean.class`.
    WRAPPER;

    private static final List<String> FALSE = List.of("false", "FALSE", "0", "-0");
    private static final List<String> TRUE  = List.of("true" , "TRUE" , "1");

    /// Returns `boolean.class` for [#PRIMITIVE] or `Boolean.class` for [#WRAPPER].
    ///
    /// @return `boolean.class` for [#PRIMITIVE] or `Boolean.class` for [#WRAPPER].
    @NonNull
    @Override
    public Class<Boolean> getType() {
        return this == PRIMITIVE ? boolean.class : Boolean.class;
    }

    /// Returns `Optional.of(false)` for [#PRIMITIVE] or [Optional#empty()] for [#WRAPPER].
    @NonNull
    @Override
    public Optional<Boolean> fromNull() {
        return this == PRIMITIVE ? Optional.of(false) : Optional.empty();
    }

    @NonNull
    @Override
    public Optional<Boolean> from(boolean in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(byte in) throws ConvertionException {
        if (in != (byte) 0 && in != (byte) 1) throw new ConvertionException(byte.class, getType());
        return Optional.of(in != (byte) 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(short in) throws ConvertionException {
        if (in != (short) 0 && in != (short) 1) throw new ConvertionException(short.class, getType());
        return Optional.of(in != (short) 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(int in) throws ConvertionException {
        if (in != 0 && in != 1) throw new ConvertionException(int.class, getType());
        return Optional.of(in != 0);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(long in) throws ConvertionException {
        if (in != 0L && in != 1L) throw new ConvertionException(long.class, getType());
        return Optional.of(in != 0L);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(float in) throws ConvertionException {
        if (in != 0F && in != 1F) throw new ConvertionException(float.class, getType());
        return Optional.of(in != 0F);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(double in) throws ConvertionException {
        if (in != 0D && in != 1D) throw new ConvertionException(double.class, getType());
        return Optional.of(in != 0D);
    }

    @NonNull
    @Override
    public Optional<Boolean> from(@NonNull BigDecimal in) throws ConvertionException {
        if (BigDecimal.ZERO.compareTo(in) == 0) return Optional.of(false);
        if (BigDecimal.ONE.compareTo(in) == 0) return Optional.of(true);
        throw new ConvertionException(BigDecimal.class, getType());
    }

    @NonNull
    @Override
    public Optional<Boolean> from(@NonNull String in) throws ConvertionException {
        var n = in.toUpperCase(Locale.ROOT);
        if (TRUE.contains(n)) return Optional.of(true);
        if (FALSE.contains(n)) return Optional.of(false);
        if (in.isEmpty()) return this == PRIMITIVE ? Optional.of(false) : Optional.empty();
        throw new ConvertionException(String.class, getType());
    }
}
