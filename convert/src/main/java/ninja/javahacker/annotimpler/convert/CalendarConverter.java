package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for `java.util.Calendar` values.
///
/// Supported conversions: [LocalDate], [LocalDateTime], [OffsetDateTime]
/// (via [ZonedDateTimeConverter] then `GregorianCalendar::from`),
/// [String] (via [ZonedDateTimeConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum CalendarConverter implements Converter<Calendar> {

    /// Singleton instance.
    INSTANCE;

    @FunctionalInterface
    private interface Work {
        public Optional<Calendar> work() throws ConvertionException;
    }

    @NonNull
    private Optional<Calendar> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Calendar.class);
        }
    }

    /// Returns `java.util.Calendar.class`.
    ///
    /// @return `java.util.Calendar.class`.
    @NonNull
    @Override
    public Class<Calendar> getType() {
        return Calendar.class;
    }

    @Override
    public Optional<Calendar> from(@NonNull LocalDate in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<Calendar> from(@NonNull LocalDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<Calendar> from(@NonNull OffsetDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<Calendar> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
