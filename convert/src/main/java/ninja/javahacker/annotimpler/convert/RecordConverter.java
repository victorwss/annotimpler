package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Array;
import lombok.NonNull;
import lombok.ToString;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.magicfactory;

/// A [Converter] for single-field record types.
///
/// Only accepts record classes with exactly one field (arity == 1).
/// Uses `MagicFactory` to discover the canonical constructor, and obtains a [Converter]
/// for the single field's type from the given [ConverterFactory].
/// Detects recursive record definitions using a thread-local set of ongoing creations
/// and throws [UnavailableConverterException] if recursion is detected.
///
/// @param <R> The record type this converter targets.
@ToString(doNotUseGetters = true)
public final class RecordConverter<R extends Record> implements Converter<R> {

    @NonNull
    private final Class<R> recordClass;

    @NonNull
    private final MagicFactory<R> factory;

    @NonNull
    private final Type inType;

    @NonNull
    private final Converter<?> inFactory;

    private static final ThreadLocal<Set<OngoingCreation>> ongoing = new ThreadLocal<>();

    private static record OngoingCreation(ConverterFactory a, Class<?> b) {
    }

    private static interface IntermediateProducer {
        @NonNull
        public Optional<?> produce() throws ConvertionException;
    }

    /// Constructs a [RecordConverter] for the given record class using the given factory.
    ///
    /// The record class must have exactly one field; otherwise throws [UnavailableConverterException].
    /// Detects recursive definitions: if the same factory/class pair is already being constructed
    /// on the current thread, throws [UnavailableConverterException].
    ///
    /// @param cvtf The factory used to obtain the inner field converter.
    /// @param recordClass The record class to convert to.
    /// @throws UnavailableConverterException If the record is not single-field, if no converter
    ///         is available for the field type, or if a recursive record definition is detected.
    /// @throws IllegalArgumentException If `cvtf` or `recordClass` is `null`.
    public RecordConverter(@NonNull ConverterFactory cvtf, @NonNull Class<R> recordClass) throws UnavailableConverterException {
        this.recordClass = recordClass;
        var o = new OngoingCreation(cvtf, recordClass);
        var n = ongoing.get();
        if (n == null) {
            n = new HashSet<>(5);
            ongoing.set(n);
        } else if (n.contains(o)) {
            throw new UnavailableConverterException("Recursive record class: " + recordClass.getName(), recordClass);
        }
        n.add(o);

        try {
            try {
                this.factory = MagicFactory.of(recordClass);
            } catch (MagicFactory.CreatorSelectionException e) {
                throw new UnavailableConverterException(e.getMessage(), e, recordClass);
            }
            if (this.factory.arity() != 1) {
                var msg = "Non-single value record class where single-valued was expected: " + recordClass.getName();
                throw new UnavailableConverterException(msg, recordClass);
            }
            this.inType = factory.getParameterTypes().get(0);
            this.inFactory = cvtf.get(inType);
        } finally {
            n.remove(o);
            if (n.isEmpty()) ongoing.remove();
        }
    }

    /// Returns the record class `R` that this converter produces.
    ///
    /// @return The record class `R` that this converter produces.
    @NonNull
    @Override
    public Class<R> getType() {
        return recordClass;
    }

    @NonNull
    private Class<?> unwrap(@NonNull Class<?> inClass) {
        checkNotNull(inClass); // Check recognized by lombok.
        if (NClob.class.isAssignableFrom(inClass)) return NClob.class;
        if (Clob.class.isAssignableFrom(inClass)) return Clob.class;
        if (Blob.class.isAssignableFrom(inClass)) return Blob.class;
        if (SQLXML.class.isAssignableFrom(inClass)) return SQLXML.class;
        if (RowId.class.isAssignableFrom(inClass)) return RowId.class;
        if (Ref.class.isAssignableFrom(inClass)) return Ref.class;
        if (Struct.class.isAssignableFrom(inClass)) return Struct.class;
        if (java.sql.Array.class.isAssignableFrom(inClass)) return java.sql.Array.class;
        return WrapperClass.unwrap(inClass);
    }

    @NonNull
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING") // Special handling for Collection and Optional.
    private Optional<R> wrap(@NonNull Class<?> inClass, @NonNull IntermediateProducer p) throws ConvertionException {
        checkNotNull(inClass); // Check recognized by lombok.
        checkNotNull(p); // Check recognized by lombok.
        Optional<?> in2a;
        try {
            in2a = p.produce();
        } catch (ConvertionException e) {
            if (e.getMessage().contains("Unsupported ")) {
                throw new ConvertionException(e.getMessage(), e, inClass, recordClass);
            }
            throw new ConvertionException(e, inClass, recordClass);
        }
        checkNotNull(in2a);
        if (in2a.isEmpty()) return Optional.empty();
        var in2b = in2a.get();
        if (in2b.getClass().isArray() && Array.getLength(in2b) == 0) return Optional.empty();
        if (in2b instanceof Collection<?> c && c.isEmpty()) return Optional.empty();
        if (in2b instanceof Optional<?> c && c.isEmpty()) return Optional.empty();
        try {
            return Optional.of(factory.create(in2b));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, inClass, recordClass);
        }
    }

    /// Converts the input object to a record instance.
    ///
    /// Returns `Optional.empty()` if `in` is `null` (does not call [#fromNull()]).
    /// Otherwise delegates to the inner converter. If the inner result is empty,
    /// an empty collection, or an empty [Optional], returns `Optional.empty()`.
    ///
    /// @param in The input value, or `null`.
    /// @return An optional containing the converted record, or empty if input is `null` or inner result is empty.
    /// @throws ConvertionException If the inner conversion fails.
    @NonNull
    @Override
    public Optional<R> fromObj(@Nullable Object in) throws ConvertionException {
        if (in == null) return Optional.empty();
        return wrap(unwrap(in.getClass()), () -> inFactory.fromObj(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(boolean in) throws ConvertionException {
        return wrap(boolean.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(byte in) throws ConvertionException {
        return wrap(byte.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(short in) throws ConvertionException {
        return wrap(short.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(int in) throws ConvertionException {
        return wrap(int.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(long in) throws ConvertionException {
        return wrap(long.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(float in) throws ConvertionException {
        return wrap(float.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(double in) throws ConvertionException {
        return wrap(double.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(BigDecimal.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(byte[].class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull String in) throws ConvertionException {
        return wrap(String.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(LocalDate.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(LocalTime.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(LocalDateTime.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(OffsetTime.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(OffsetDateTime.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull Blob in) throws ConvertionException {
        return wrap(Blob.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull Clob in) throws ConvertionException {
        return wrap(Clob.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull NClob in) throws ConvertionException {
        return wrap(NClob.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(SQLXML.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull RowId in) throws ConvertionException {
        return wrap(RowId.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull Ref in) throws ConvertionException {
        return wrap(Ref.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull Struct in) throws ConvertionException {
        return wrap(Struct.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<R> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(java.sql.Array.class, () -> inFactory.from(in));
    }

    /// {@inheritDoc}
    @Override
    public int hashCode() {
        return Objects.hash(recordClass, inFactory);
    }

    /// {@inheritDoc}
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    public boolean equals(@Nullable Object other) {
        return other instanceof RecordConverter<?> ot
                && Objects.equals(this.recordClass, ot.recordClass)
                && Objects.equals(this.inFactory, ot.inFactory);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
