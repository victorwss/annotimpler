package ninja.javahacker.annotimpler.convert;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] that wraps an element [Converter] and produces arrays of type `E[]`.
///
/// For `null` input, returns an empty `E[0]`. For an empty string input, returns an empty `E[0]`.
/// For any other non-null input, converts it using the element converter and returns a single-element `E[1]`.
///
/// @param <E> The element type of the array.
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
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
    private interface Work<E> {

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
    private Optional<E[]> wrap(Work<E> e) throws ConvertionException {
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

    @NonNull
    @Override
    public Optional<E[]> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

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

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
