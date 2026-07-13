package ninja.javahacker.annotimpler.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NonNull;
import lombok.ToString;

/// A [Converter] that wraps an element [Converter] and produces `List<E>`.
///
/// For `null` input, returns `Optional.of(List.of())` (an empty list).
/// For any other non-null input, converts the element and returns `List.of(element)`.
///
/// @param <E> The element type of the list.
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public final class ListConverter<E> implements Converter<List<E>> {

    @NonNull
    private final Converter<E> cvt;

    @NonNull
    private final ParameterizedType baseType;

    /// Constructs a [ListConverter] for `List<X>` where `X` is the raw type argument of `baseType`.
    ///
    /// `baseType` must be `List<X>` for some concrete class `X`; otherwise throws [UnavailableConverterException].
    ///
    /// @param factory The factory used to obtain the element converter.
    /// @param baseType The parameterized type `List<X>`.
    /// @throws UnavailableConverterException If `baseType` is not `List<X>` for some class `X`,
    ///         or if no converter is available for the element type.
    /// @throws IllegalArgumentException If `factory` or `baseType` is `null`.
    @SuppressWarnings("unchecked")
    public ListConverter(
            @NonNull ConverterFactory factory,
            @NonNull ParameterizedType baseType)
            throws UnavailableConverterException
    {
        var baseClass = baseType.getActualTypeArguments()[0];
        if (baseType.getRawType() != List.class || !(baseClass instanceof Class<?>)) {
            throw new UnavailableConverterException("The baseType must be a List of some class.", baseClass);
        }
        this.baseType = baseType;
        this.cvt = factory.getOf((Class<E>) baseClass);
    }

    /// Returns the parameterized type `List<E>` that this converter produces.
    ///
    /// @return The parameterized type `List<E>` that this converter produces.
    @NonNull
    @Override
    public ParameterizedType getType() {
        return baseType;
    }

    @FunctionalInterface
    private interface InternalWork<E> {

        @NonNull
        public Optional<E> work() throws ConvertionException;

        @NonNull
        public default Optional<List<E>> rework(@NonNull Type baseType) throws ConvertionException {
            checkNotNull(baseType); // Check recognized by lombok.
            try {
                return Optional.of(work().map(List::of).orElse(List.of()));
            } catch (ConvertionException e) {
                if (e.getMessage().contains("Unsupported ")) {
                    throw new ConvertionException(e.getMessage(), e, e.getIn(), baseType);
                }
                throw new ConvertionException(e, e.getIn(), baseType);
            }
        }
    }

    @NonNull
    private Optional<List<E>> wrap(@NonNull InternalWork<E> e) throws ConvertionException {
        checkNotNull(e); // Check recognized by lombok.
        return e.rework(baseType);
    }

    /// Returns `Optional.of(List.of())` (an empty list).
    @NonNull
    @Override
    public Optional<List<E>> fromNull() {
        return Optional.of(List.of());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull String in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
