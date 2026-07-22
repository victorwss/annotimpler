package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;
import ninja.javahacker.annotimpler.magicfactory.TypeName;

/// A [Converter] that wraps an element [Converter] and produces `Collection<E>` (implemented as a list).
///
/// For `null` input, returns `Optional.of(List.of())` (an empty list).
/// For any other non-null input, converts the element and returns `List.of(element)`.
///
/// @param <E> The element type of the collection.
public final class CollectionConverter<E> implements Converter<Collection<E>> {

    @NonNull
    private final Converter<E> cvt;

    @NonNull
    private final ParameterizedType baseType;

    /// Constructs a [CollectionConverter] for `Collection<X>` where `X` is the raw type argument of `baseType`.
    ///
    /// `baseType` must be `Collection<X>` for some concrete class `X`; otherwise throws [UnavailableConverterException].
    ///
    /// @param factory The factory used to obtain the element converter.
    /// @param baseType The parameterized type `Collection<X>`.
    /// @throws UnavailableConverterException If `baseType` is not `Collection<X>` for some class `X`,
    ///         or if no converter is available for the element type.
    /// @throws IllegalArgumentException If `factory` or `baseType` is `null`.
    @SuppressWarnings("unchecked")
    public CollectionConverter(
            @NonNull ConverterFactory factory,
            @NonNull ParameterizedType baseType)
            throws UnavailableConverterException
    {
        var baseClass = baseType.getActualTypeArguments()[0];
        if (baseType.getRawType() != Collection.class || !(baseClass instanceof Class<?>)) {
            throw new UnavailableConverterException("The baseType must be a Collection of some class.", baseClass);
        }
        this.baseType = baseType;
        this.cvt = factory.getOf((Class<E>) baseClass);
    }

    /// Returns the parameterized type `Collection<E>` that this converter produces.
    ///
    /// @return The parameterized type `Collection<E>` that this converter produces.
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
        public default Optional<Collection<E>> rework(@NonNull Type baseType) throws ConvertionException {
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
    private Optional<Collection<E>> wrap(@NonNull InternalWork<E> e) throws ConvertionException {
        checkNotNull(e); // Check recognized by lombok.
        return e.rework(baseType);
    }

    /// Returns `Optional.of(List.of())` (an empty list as the collection value).
    @NonNull
    @Override
    public Optional<Collection<E>> fromNull() {
        return Optional.of(List.of());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull String in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Collection<E>> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return Objects.hash(getClass(), baseType, cvt);
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(@Nullable Object other) {
        return other instanceof CollectionConverter<?> ot
                && Objects.equals(this.baseType, ot.baseType)
                && Objects.equals(this.cvt, ot.cvt);
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return "CollectionConverter[baseType=" + TypeName.of(baseType) + ", cvt=" + cvt.toString() + "]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
