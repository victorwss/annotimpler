package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Timestamp;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

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
    private interface Work {
        public Optional<Timestamp> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Timestamp> rewrap(@NonNull Work w) throws ConvertionException {
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

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDate in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull LocalDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

    @NonNull
    @Override
    public Optional<Timestamp> from(@NonNull OffsetDateTime in) {
        return LocalDateTimeConverter.INSTANCE.from(in).map(Timestamp::valueOf);
    }

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
