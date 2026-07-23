package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalLong;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] for [OptionalLong] values.
///
/// Delegates to [LongConverter#WRAPPER] internally.
/// Returns `Optional.of(OptionalLong.empty())` for `null` input.
///
/// Supported conversions: `boolean`, `byte`, `short`, `int`, `long`,
/// `float`/`double`/[BigDecimal] (with precision check),
/// [String] (empty → `OptionalLong.empty()`).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum OptionalLongConverter implements Converter<OptionalLong> {

    /// Singleton instance.
    INSTANCE;

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `OptionalLong.class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted [OptionalLong], wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<OptionalLong> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `OptionalLong.class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<OptionalLong> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), OptionalLong.class);
        }
    }

    /// Returns `OptionalLong.class`.
    ///
    /// @return `OptionalLong.class`.
    @NonNull
    @Override
    public Class<OptionalLong> getType() {
        return OptionalLong.class;
    }

    /// Returns `Optional.of(OptionalLong.empty())`.
    @NonNull
    @Override
    public Optional<OptionalLong> fromNull() {
        return Optional.of(OptionalLong.empty());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(boolean in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(byte in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(short in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(int in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(long in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(float in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(double in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> Optional.of(LongConverter.WRAPPER.from(in).map(OptionalLong::of).orElseGet(OptionalLong::empty)));
    }

    /// Returns `[OptionalLongConverter]`.
    ///
    /// @return `[OptionalLongConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[OptionalLongConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
