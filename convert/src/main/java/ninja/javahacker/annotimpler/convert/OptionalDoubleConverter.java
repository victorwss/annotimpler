package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] for [OptionalDouble] values.
///
/// Delegates to [DoubleConverter#WRAPPER] internally.
/// Returns `Optional.of(OptionalDouble.empty())` for `null` input.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`,
/// `long`/`float` (with precision check), `double`,
/// [BigDecimal] (roundtrip), [String] (empty → `OptionalDouble.empty()`).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum OptionalDoubleConverter implements Converter<OptionalDouble> {

    /// Singleton instance.
    INSTANCE;

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `OptionalDouble.class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted [OptionalDouble], wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<OptionalDouble> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `OptionalDouble.class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<OptionalDouble> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), OptionalDouble.class);
        }
    }

    /// Returns `OptionalDouble.class`.
    ///
    /// @return `OptionalDouble.class`.
    @NonNull
    @Override
    public Class<OptionalDouble> getType() {
        return OptionalDouble.class;
    }

    /// Returns `Optional.of(OptionalDouble.empty())`.
    @NonNull
    @Override
    public Optional<OptionalDouble> fromNull() {
        return Optional.of(OptionalDouble.empty());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(boolean in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(byte in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(short in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(int in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(long in) throws ConvertionException {
        return rewrap(() -> DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(float in) throws ConvertionException {
        return rewrap(() -> DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(double in) {
        return DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalDouble> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> Optional.of(DoubleConverter.WRAPPER.from(in).map(OptionalDouble::of).orElseGet(OptionalDouble::empty)));
    }

    /// Returns `[OptionalDoubleConverter]`.
    ///
    /// @return `[OptionalDoubleConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[OptionalDoubleConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
