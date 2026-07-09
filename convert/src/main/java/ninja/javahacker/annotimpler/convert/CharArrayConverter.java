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

    @FunctionalInterface
    private interface Work {
        public Optional<char[]> work() throws ConvertionException;
    }

    @NonNull
    private Optional<char[]> rewrap(@NonNull Work w) throws ConvertionException {
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

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
