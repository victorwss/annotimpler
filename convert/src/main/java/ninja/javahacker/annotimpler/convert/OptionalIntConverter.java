package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalInt;
import lombok.Generated;
import lombok.NonNull;

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

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `OptionalInt.class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted [OptionalInt], wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<OptionalInt> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `OptionalInt.class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<OptionalInt> rewrap(@NonNull InternalWork w) throws ConvertionException {
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

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(boolean in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(byte in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(short in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(int in) {
        return IntegerConverter.WRAPPER.from(in).map(OptionalInt::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(long in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(float in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(double in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> IntegerConverter.WRAPPER.from(in).map(OptionalInt::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalInt> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> Optional.of(IntegerConverter.WRAPPER.from(in).map(OptionalInt::of).orElseGet(OptionalInt::empty)));
    }

    /// Returns `[OptionalIntConverter]`.
    ///
    /// @return `[OptionalIntConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[OptionalIntConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
