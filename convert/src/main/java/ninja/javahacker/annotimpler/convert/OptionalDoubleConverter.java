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

    @FunctionalInterface
    private interface InternalWork {
        public Optional<OptionalDouble> work() throws ConvertionException;
    }

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
