package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] for `java.util.Date` values.
///
/// Supported conversions: [LocalDate], [LocalDateTime], [OffsetDateTime]
/// (via [InstantConverter] then `Date::from`),
/// [String] (via [InstantConverter]; empty → empty).
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum DateConverter implements Converter<Date> {

    /// Singleton instance.
    INSTANCE;

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `Date.class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted [Date], wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<Date> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `Date.class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<Date> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), Date.class);
        }
    }

    /// Returns `java.util.Date.class`.
    ///
    /// @return `java.util.Date.class`.
    @NonNull
    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDate in) {
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Date> from(@NonNull LocalDateTime in) {
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Date> from(@NonNull OffsetDateTime in) {
        return InstantConverter.INSTANCE.from(in).map(Date::from);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Date> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> InstantConverter.INSTANCE.from(in).map(Date::from));
    }

    /// Returns `[DateConverter]`.
    ///
    /// @return `[DateConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[DateConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
