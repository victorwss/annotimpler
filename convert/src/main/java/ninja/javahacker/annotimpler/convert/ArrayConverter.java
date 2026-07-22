package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.Struct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Objects;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;

/// A [Converter] that wraps an element [Converter] and produces arrays of type `E[]`.
///
/// For `null` input, returns an empty `E[0]`. For an empty string input, returns an empty `E[0]`.
/// For any other non-null input, converts it using the element converter and returns a single-element `E[1]`.
///
/// @param <E> The element type of the array.
public final class ArrayConverter<E> implements Converter<E[]> {

    @NonNull
    private final Class<E> baseClass;

    @NonNull
    private final Class<E[]> arrayClass;

    @NonNull
    private final Converter<E> cvt;

    /// Constructs an [ArrayConverter] that wraps a converter for the given element class.
    ///
    /// Looks up the element converter from `factory` and derives the array class.
    ///
    /// @param factory The factory used to obtain the element converter.
    /// @param baseClass The element class of the array.
    /// @throws UnavailableConverterException If no converter is available for `baseClass`.
    /// @throws IllegalArgumentException If `factory` or `baseClass` is `null`.
    @SuppressWarnings("unchecked")
    public ArrayConverter(@NonNull ConverterFactory factory, @NonNull Class<E> baseClass) throws UnavailableConverterException {
        this.baseClass = baseClass;
        this.cvt = factory.getOf(baseClass);
        this.arrayClass = (Class<E[]>) java.lang.reflect.Array.newInstance(baseClass, 0).getClass();
    }

    /// Returns the array class `E[]` that this converter produces.
    ///
    /// @return The array class `E[]` that this converter produces.
    @NonNull
    @Override
    public Class<E[]> getType() {
        return arrayClass;
    }

    @FunctionalInterface
    private interface InternalWork<E> {

        @NonNull
        public Optional<E> work() throws ConvertionException;

        @NonNull
        public default Optional<E> rework(@NonNull Class<E[]> arrayClass) throws ConvertionException {
            checkNotNull(arrayClass); // Check recognized by lombok.
            try {
                return work();
            } catch (ConvertionException e) {
                if (e.getMessage().contains("Unsupported ")) {
                    throw new ConvertionException(e.getMessage(), e, e.getIn(), arrayClass);
                }
                throw new ConvertionException(e, e.getIn(), arrayClass);
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private Optional<E[]> wrap(InternalWork<E> e) throws ConvertionException {
        var e2 = (Optional<Object>) e.rework(arrayClass);
        var array = e2.map(c -> {
            var ret = java.lang.reflect.Array.newInstance(baseClass, 1);
            java.lang.reflect.Array.set(ret, 0, c);
            return ret;
        }).orElseGet(() -> java.lang.reflect.Array.newInstance(baseClass, 0));
        return (Optional<E[]>) (Optional<?>) Optional.of(array);
    }

    /// Returns `Optional.of(new E[0])` (an empty array).
    ///
    /// @return `Optional.of(new E[0])` (an empty array).
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Optional<E[]> fromNull() {
        var array = java.lang.reflect.Array.newInstance(baseClass, 0);
        return (Optional<E[]>) (Optional<?>) Optional.of(array);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Optional<E[]> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) {
            var array = java.lang.reflect.Array.newInstance(baseClass, 0);
            return (Optional<E[]>) (Optional<?>) Optional.of(array);
        }
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return Objects.hash(getClass(), baseClass, cvt);
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(@Nullable Object other) {
        return other instanceof ArrayConverter<?> ot
                && Objects.equals(this.baseClass, ot.baseClass)
                && Objects.equals(this.cvt, ot.cvt);
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return "ArrayConverter[baseType=" + baseClass.getName() + ", cvt=" + cvt.toString() + "]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
