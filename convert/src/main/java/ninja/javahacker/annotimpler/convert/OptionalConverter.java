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
import java.util.Objects;
import java.util.Optional;
import lombok.Generated;
import lombok.NonNull;
import ninja.javahacker.annotimpler.magicfactory.TypeName;

/// A [Converter] that wraps an element [Converter] and produces `Optional<E>`.
///
/// For `null` input, returns `Optional.of(Optional.empty())` (the outer [Optional] is always present;
/// the inner [Optional] is empty for `null` input).
/// For non-null input, converts the element: if the inner result is present, returns
/// `Optional.of(Optional.of(element))`; otherwise returns `Optional.of(Optional.empty())`.
///
/// @param <E> The element type of the inner optional.
public final class OptionalConverter<E> implements Converter<Optional<E>> {

    /// The element converter used to convert values before wrapping them into an inner [Optional].
    @NonNull
    private final Converter<E> cvt;

    /// The parameterized type `Optional<E>` that this converter produces.
    @NonNull
    private final ParameterizedType baseType;

    /// Constructs an [OptionalConverter] for `Optional<X>` where `X` is the raw type argument of `baseType`.
    ///
    /// `baseType` must be `Optional<X>` for some concrete class `X`; otherwise throws [UnavailableConverterException].
    ///
    /// @param factory The factory used to obtain the element converter.
    /// @param baseType The parameterized type `Optional<X>`.
    /// @throws UnavailableConverterException If `baseType` is not `Optional<X>` for some class `X`,
    ///         or if no converter is available for the element type.
    /// @throws IllegalArgumentException If `factory` is `null`.
    /// @throws IllegalArgumentException If `baseType` is `null`.
    @SuppressWarnings("unchecked")
    public OptionalConverter(@NonNull ConverterFactory factory, @NonNull ParameterizedType baseType) throws UnavailableConverterException {
        var baseClass = baseType.getActualTypeArguments()[0];
        if (baseType.getRawType() != Optional.class || !(baseClass instanceof Class<?>)) {
            throw new UnavailableConverterException("The baseType must be an Optional of some class.", baseClass);
        }
        this.baseType = baseType;
        this.cvt = factory.getOf((Class<E>) baseClass);
    }

    /// Returns the parameterized type `Optional<E>` that this converter produces.
    ///
    /// @return The parameterized type `Optional<E>` that this converter produces.
    @NonNull
    @Override
    public ParameterizedType getType() {
        return baseType;
    }

    /// A single conversion operation for the inner optional's element, used to derive per-element conversion
    /// errors that reference the target `Optional<E>` type instead of the element type.
    ///
    /// @param <E> The element type of the inner optional.
    @FunctionalInterface
    private interface InternalWork<E> {

        /// Performs the conversion of the single element.
        ///
        /// @return The converted element, wrapped in [Optional], or empty if there is no value to convert.
        /// @throws ConvertionException If the conversion fails.
        @NonNull
        public Optional<E> work() throws ConvertionException;

        /// Performs [#work()] and wraps the resulting element into a present or empty inner [Optional], rewriting
        /// any resulting [ConvertionException] so it references `baseType` instead of the element type.
        ///
        /// @param baseType The parameterized type `Optional<E>` to report as the target type in case of failure.
        /// @return `Optional.of(Optional.of(element))` if [#work()] returns a present value,
        ///         or `Optional.of(Optional.empty())` otherwise.
        /// @throws ConvertionException If the conversion fails.
        /// @throws IllegalArgumentException If `baseType` is `null`.
        @NonNull
        public default Optional<Optional<E>> rework(@NonNull Type baseType) throws ConvertionException {
            checkNotNull(baseType); // Check recognized by lombok.
            try {
                return Optional.of(work().map(Optional::of).orElseGet(Optional::empty));
            } catch (ConvertionException e) {
                if (e.getMessage().contains("Unsupported ")) {
                    throw new ConvertionException(e.getMessage(), e, e.getIn(), baseType);
                }
                throw new ConvertionException(e, e.getIn(), baseType);
            }
        }
    }

    @NonNull
    private Optional<Optional<E>> wrap(@NonNull InternalWork<E> e) throws ConvertionException {
        checkNotNull(e); // Check recognized by lombok.
        return e.rework(baseType);
    }

    /// Returns `Optional.of(Optional.empty())` (the outer optional is always present; the inner is empty for `null` input).
    @NonNull
    @Override
    public Optional<Optional<E>> fromNull() {
        return Optional.of(Optional.empty());
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull String in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Ref in) throws ConvertionException {
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
        return other instanceof OptionalConverter<?> ot
                && Objects.equals(this.baseType, ot.baseType)
                && Objects.equals(this.cvt, ot.cvt);
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return "OptionalConverter[baseType=" + TypeName.of(baseType) + ", cvt=" + cvt.toString() + "]";
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
