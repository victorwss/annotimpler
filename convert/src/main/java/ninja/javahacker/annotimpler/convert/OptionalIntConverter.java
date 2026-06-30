package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for [OptionalInt] values.
///
/// Delegates to [IntegerConverter#WRAPPER] internally.
/// Returns `Optional.of(OptionalInt.empty())` for `null` input.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`,
/// `long`/`float`/`double`/[BigDecimal] (with range check),
/// [String] (empty → `OptionalInt.empty()`).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum OptionalIntConverter implements Converter<OptionalInt> {

    /// Singleton instance.
    INSTANCE;

    @FunctionalInterface
    private interface Work {
        public Optional<OptionalInt> work() throws ConvertionException;
    }

    @NonNull
    private Optional<OptionalInt> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), OptionalInt.class);
        }
    }

    /// Returns `OptionalInt.class`.
    ///
    /// @return `OptionalInt.class`.
    @NonNull
    @Override
    public Class<OptionalInt> getType() {
        return OptionalInt.class;
    }

    /// Returns `Optional.of(OptionalInt.empty())`.
    @NonNull
    @Override
    public Optional<OptionalInt> fromNull() {
        return Optional.of(OptionalInt.empty());
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(boolean in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(byte in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(short in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(int in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(long in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(float in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(double in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    @NonNull
    @Override
    public Optional<OptionalInt> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> Optional.of(IntegerConverter.WRAPPER.from(in).map(OptionalInt::of).orElseGet(OptionalInt::empty)));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
