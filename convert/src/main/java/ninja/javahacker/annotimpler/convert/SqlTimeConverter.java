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

    @FunctionalInterface
    private interface Work {
        public Optional<Time> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Time> rewrap(@NonNull Work w) throws ConvertionException {
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

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
