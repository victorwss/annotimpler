package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

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

    @FunctionalInterface
    public interface Work {
        public Optional<OptionalLong> work() throws ConvertionException;
    }

    @NonNull
    private Optional<OptionalLong> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
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

    @NonNull
    @Override
    public Optional<OptionalLong> from(boolean in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(byte in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(short in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(int in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(long in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(float in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(double in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> Optional.of(LongConverter.WRAPPER.from(in).map(OptionalLong::of).orElse(OptionalLong.empty())));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
