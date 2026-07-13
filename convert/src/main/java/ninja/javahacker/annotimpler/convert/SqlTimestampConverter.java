package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] for `java.sql.Timestamp` values.
///
/// Supported conversions: [LocalDate]/[LocalDateTime]/[OffsetDateTime]
/// (via [LocalDateTimeConverter] then `Timestamp.valueOf`),
/// [String] (via [LocalDateTimeConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum SqlTimestampConverter implements Converter<Timestamp> {

    /// Singleton instance.
    INSTANCE;

    @FunctionalInterface
    private interface InternalWork {
        public Optional<Timestamp> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Timestamp> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Timestamp.class);
        }
    }

    /// Returns `java.sql.Timestamp.class`.
    ///
    /// @return `java.sql.Timestamp.class`.
    @NonNull
    @Override
    public Class<Timestamp> getType() {
        return Timestamp.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDate in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull OffsetDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
