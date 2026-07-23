package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] for `java.util.GregorianCalendar` values.
///
/// Supported conversions: [LocalDate], [LocalDateTime], [OffsetDateTime]
/// (via [ZonedDateTimeConverter] then `GregorianCalendar::from`),
/// [String] (via [ZonedDateTimeConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum GregorianCalendarConverter implements Converter<GregorianCalendar> {

    /// Singleton instance.
    INSTANCE;

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `GregorianCalendar.class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted [GregorianCalendar], wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<GregorianCalendar> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `GregorianCalendar.class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<GregorianCalendar> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
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

    /// {@inheritDoc}
    @Override
    public Optional<GregorianCalendar> from(@NonNull LocalDate in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    /// {@inheritDoc}
    @Override
    public Optional<GregorianCalendar> from(@NonNull LocalDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    /// {@inheritDoc}
    @Override
    public Optional<GregorianCalendar> from(@NonNull OffsetDateTime in) {
        return ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from);
    }

    /// {@inheritDoc}
    @Override
    public Optional<GregorianCalendar> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> ZonedDateTimeConverter.INSTANCE.from(in).map(GregorianCalendar::from));
    }

    /// Returns `[GregorianCalendarConverter]`.
    ///
    /// @return `[GregorianCalendarConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[GregorianCalendarConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
