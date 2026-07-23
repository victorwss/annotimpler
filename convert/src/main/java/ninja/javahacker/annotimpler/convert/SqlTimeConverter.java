package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] for `java.sql.Time` values.
///
/// Supported conversions: [LocalTime] (via `Time.valueOf`),
/// [LocalDateTime]/[OffsetTime]/[OffsetDateTime] (extract local time, then `Time.valueOf`),
/// [String] (via [LocalTimeConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum SqlTimeConverter implements Converter<Time> {

    /// Singleton instance.
    INSTANCE;

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `java.sql.Time.class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted [Time], wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<Time> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `java.sql.Time.class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<Time> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Time.class);
        }
    }

    /// Returns `java.sql.Time.class`.
    ///
    /// @return `java.sql.Time.class`.
    @NonNull
    @Override
    public Class<Time> getType() {
        return Time.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Time> from(@NonNull LocalTime in) {
        return Optional.of(Time.valueOf(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Time> from(@NonNull LocalDateTime in) {
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Time> from(@NonNull OffsetTime in) {
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Time> from(@NonNull OffsetDateTime in) {
        return LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Time> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> LocalTimeConverter.INSTANCE.from(in).map(Time::valueOf));
    }

    /// Returns `[SqlTimeConverter]`.
    ///
    /// @return `[SqlTimeConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[SqlTimeConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
