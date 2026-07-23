package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] for `char[]` values.
///
/// Delegates to [StringConverter] and converts the resulting string to a char array.
/// Supported conversions: [String], `byte[]`, [Blob], [Clob], [NClob], [SQLXML], [RowId].
/// Returns an empty char array for `null` input.
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum CharArrayConverter implements Converter<char[]> {

    /// Singleton instance.
    INSTANCE;

    /// A single conversion operation whose [ConvertionException] is rewrapped by [#rewrap(InternalWork)]
    /// so that it references `char[].class` as the failure's target type.
    @FunctionalInterface
    private interface InternalWork {

        /// Performs the conversion.
        ///
        /// @return The converted `char[]`, wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        public Optional<char[]> work() throws ConvertionException;
    }

    /// Runs `w` and rewrites any thrown [ConvertionException] so it references `char[].class`.
    ///
    /// @param w The conversion operation to run.
    /// @return The result of `w`.
    /// @throws ConvertionException If `w` throws it.
    /// @throws IllegalArgumentException If `w` is `null`.
    @NonNull
    private Optional<char[]> rewrap(@NonNull InternalWork w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), char[].class);
        }
    }

    /// Returns `char[].class`.
    ///
    /// @return `char[].class`.
    @NonNull
    @Override
    public Class<char[]> getType() {
        return char[].class;
    }

    /// Returns `Optional.of(new char[0])` (an empty char array).
    @NonNull
    @Override
    public Optional<char[]> fromNull() {
        return Optional.of(new char[0]);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull byte[] in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull Blob in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull Clob in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull NClob in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull SQLXML in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<char[]> from(@NonNull RowId in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    /// Returns `[CharArrayConverter]`.
    ///
    /// @return `[CharArrayConverter]`.
    @NonNull
    @Override
    public String toString() {
        return "[CharArrayConverter]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
