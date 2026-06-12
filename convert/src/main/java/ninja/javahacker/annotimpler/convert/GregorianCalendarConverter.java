package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for `java.util.GregorianCalendar` values.
///
/// Supported conversions: [LocalDate], [LocalDateTime], [OffsetDateTime]
/// (via [ZonedDateTimeConverter] then `GregorianCalendar::from`),
/// [String] (via [ZonedDateTimeConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum GregorianCalendarConverter implements Converter<GregorianCalendar> {

    /// Singleton instance.
    INSTANCE;

    @FunctionalInterface
    public interface Work {
        public Optional<GregorianCalendar> work() throws ConvertionException;
    }

    @NonNull
    private Optional<GregorianCalendar> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), GregorianCalendar.class);
        }
    }

    /// Returns `GregorianCalendar.class`.
    ///
    /// @return `GregorianCalendar.class`.
    @NonNull
    @Override
    public Class<GregorianCalendar> getType() {
        return GregorianCalendar.class;
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull LocalDate in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull LocalDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull OffsetDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    @Override
    public Optional<GregorianCalendar> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
