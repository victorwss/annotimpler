package ninja.javahacker.annotimpler.convert;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [Converter] that wraps an element [Converter] and produces `Set<E>`.
///
/// For `null` input, returns `Optional.of(Set.of())` (an empty set).
/// For any other non-null input, converts the element and returns `Set.of(element)`.
///
/// @param <E> The element type of the set.
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public final class SetConverter<E> implements Converter<Set<E>> {

    @NonNull
    private final Converter<E> cvt;

    @NonNull
    private final ParameterizedType baseType;

    /// Constructs a [SetConverter] for `Set<X>` where `X` is the raw type argument of `baseType`.
    ///
    /// `baseType` must be `Set<X>` for some concrete class `X`; otherwise throws [UnavailableConverterException].
    ///
    /// @param factory The factory used to obtain the element converter.
    /// @param baseType The parameterized type `Set<X>`.
    /// @throws UnavailableConverterException If `baseType` is not `Set<X>` for some class `X`,
    ///         or if no converter is available for the element type.
    /// @throws IllegalArgumentException If `factory` or `baseType` is `null`.
    @SuppressWarnings("unchecked")
    public SetConverter(
            @NonNull ConverterFactory factory,
            @NonNull ParameterizedType baseType)
            throws UnavailableConverterException
    {
        var baseClass = baseType.getActualTypeArguments()[0];
        if (baseType.getRawType() != Set.class || !(baseClass instanceof Class<?>)) {
            throw new UnavailableConverterException("The baseType must be a Set of some class.", baseClass);
        }
        this.baseType = baseType;
        this.cvt = factory.getOf((Class<E>) baseClass);
    }

    /// Returns the parameterized type `Set<E>` that this converter produces.
    ///
    /// @return the parameterized type `Set<E>` that this converter produces.
    @NonNull
    @Override
    public ParameterizedType getType() {
        return baseType;
    }

    @FunctionalInterface
    private interface Work<E> {

        @NonNull
        public Optional<E> work() throws ConvertionException;

        @NonNull
        public default Optional<Set<E>> rework(@NonNull Type baseType) throws ConvertionException {
            checkNotNull(baseType); // Check recognized by lombok.
            try {
                return Optional.of(work().map(Set::of).orElse(Set.of()));
            } catch (ConvertionException e) {
                if (e.getMessage().contains("Unsupported ")) {
                    throw new ConvertionException(e.getMessage(), e, e.getIn(), baseType);
                }
                throw new ConvertionException(e, e.getIn(), baseType);
            }
        }
    }

    @NonNull
    private Optional<Set<E>> wrap(@NonNull Work<E> e) throws ConvertionException {
        checkNotNull(e);
        return e.rework(baseType);
    }

    /// Returns `Optional.of(Set.of())` (an empty set).
    @NonNull
    @Override
    public Optional<Set<E>> fromNull() {
        return Optional.of(Set.of());
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull String in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
