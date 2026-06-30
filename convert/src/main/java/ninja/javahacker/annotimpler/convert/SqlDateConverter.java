package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Date;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for `java.sql.Date` values.
///
/// Supported conversions: [LocalDate] (via `Date.valueOf`),
/// [LocalDateTime]/[OffsetDateTime] (extract local date, then `Date.valueOf`),
/// [String] (via [LocalDateConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum SqlDateConverter implements Converter<Date> {

    /// Singleton instance.
    INSTANCE;

    @FunctionalInterface
    private interface Work {
        public Optional<Date> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Date> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Date.class);
        }
    }

    /// Returns `java.sql.Date.class`.
    ///
    /// @return `java.sql.Date.class`.
    @NonNull
    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDate in) {
        return Optional.of(Date.valueOf(in));
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDateTime in) {
        return LocalDateConverter.INSTANCE.from(in).map(Date::valueOf);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull OffsetDateTime in) {
        return LocalDateConverter.INSTANCE.from(in).map(Date::valueOf);
    }

    @NonNull
    @Override
    public Optional<Date> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> LocalDateConverter.INSTANCE.from(in).map(Date::valueOf));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
