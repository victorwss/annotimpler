package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.Struct;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import lombok.Generated;
import lombok.NonNull;

/// An extended [ConverterFactory] with built-in support for all standard Java types.
///
/// Supports primitives, wrappers, date/time types, SQL types, enums, records, arrays,
/// and parameterized collections ([List], [Set], [Collection], [Optional]).
/// The singleton instance is [#INSTANCE].
///
/// Additional mappings can be registered with [#extend(Class, Converter)]
/// or by directly extending this interface.
public interface StdConverterFactory extends ConverterFactory {

    /// The singleton standard converter factory pre-populated with all built-in converters.
    public static final StdConverterFactory INSTANCE = start();

    private static StdConverterFactory start() {
        Map<Class<?>, Converter<?>> map = Map.ofEntries(
                Map.entry(boolean.class, BooleanConverter.PRIMITIVE),
                Map.entry(Boolean.class, BooleanConverter.WRAPPER),
                Map.entry(byte.class, ByteConverter.PRIMITIVE),
                Map.entry(Byte.class, ByteConverter.WRAPPER),
                Map.entry(short.class, ShortConverter.PRIMITIVE),
                Map.entry(Short.class, ShortConverter.WRAPPER),
                Map.entry(int.class, IntegerConverter.PRIMITIVE),
                Map.entry(Integer.class, IntegerConverter.WRAPPER),
                Map.entry(long.class, LongConverter.PRIMITIVE),
                Map.entry(Long.class, LongConverter.WRAPPER),
                Map.entry(float.class, FloatConverter.PRIMITIVE),
                Map.entry(Float.class, FloatConverter.WRAPPER),
                Map.entry(double.class, DoubleConverter.PRIMITIVE),
                Map.entry(Double.class, DoubleConverter.WRAPPER),
                Map.entry(char.class, CharacterConverter.PRIMITIVE),
                Map.entry(Character.class, CharacterConverter.WRAPPER),

                Map.entry(BigInteger.class, BigIntegerConverter.INSTANCE),
                Map.entry(BigDecimal.class, BigDecimalConverter.INSTANCE),
                Map.entry(OptionalInt.class, OptionalIntConverter.INSTANCE),
                Map.entry(OptionalLong.class, OptionalLongConverter.INSTANCE),
                Map.entry(OptionalDouble.class, OptionalDoubleConverter.INSTANCE),

                Map.entry(String.class, StringConverter.INSTANCE),
                Map.entry(byte[].class, ByteArrayConverter.INSTANCE),
                Map.entry(char[].class, CharArrayConverter.INSTANCE),
                Map.entry(Ref.class, RefConverter.INSTANCE),
                Map.entry(RowId.class, RowIdConverter.INSTANCE),
                Map.entry(Struct.class, StructConverter.INSTANCE),

                Map.entry(LocalDate.class, LocalDateConverter.INSTANCE),
                Map.entry(LocalTime.class, LocalTimeConverter.INSTANCE),
                Map.entry(LocalDateTime.class, LocalDateTimeConverter.INSTANCE),
                Map.entry(OffsetTime.class, OffsetTimeConverter.INSTANCE),
                Map.entry(OffsetDateTime.class, OffsetDateTimeConverter.INSTANCE),
                Map.entry(ZonedDateTime.class, ZonedDateTimeConverter.INSTANCE),
                Map.entry(Instant.class, InstantConverter.INSTANCE),

                Map.entry(Time.class, SqlTimeConverter.INSTANCE),
                Map.entry(java.sql.Date.class, SqlDateConverter.INSTANCE),
                Map.entry(java.sql.Timestamp.class, SqlTimestampConverter.INSTANCE),
                Map.entry(java.util.Date.class, DateConverter.INSTANCE),
                Map.entry(Calendar.class, CalendarConverter.INSTANCE),
                Map.entry(GregorianCalendar.class, GregorianCalendarConverter.INSTANCE)
        );

        return new StdConverterFactory() {

            /// {@inheritDoc}
            @NonNull
            @Override
            @SuppressWarnings("ReturnOfCollectionOrArrayField") // Safe because the returned map is immutable.
            @SuppressFBWarnings("EI_EXPOSE_REP") // Safe because the returned map is immutable.
            public Map<Class<?>, Converter<?>> directMappings() {
                return map;
            }
        };
    }

    /// Returns the direct class-to-converter map of [#INSTANCE].
    ///
    /// @return The map of [Class] to [Converter] for all pre-registered types.
    @NonNull
    public static Map<Class<?>, Converter<?>> rootMap() {
        return INSTANCE.directMappings();
    }

    /// Dispatches the type `t` to the appropriate typed `getOf` overload.
    ///
    /// Dispatches as follows: [Class] → [#getOf(Class)],
    /// [ParameterizedType] → [#getOf(ParameterizedType)],
    /// [WildcardType] → [#getOf(WildcardType)],
    /// [GenericArrayType] → [#getOf(GenericArrayType)],
    /// [TypeVariable] → [#getOf(TypeVariable)],
    /// other → [#getOfUndetermined(Type)].
    ///
    /// @param t The type to look up.
    /// @return A [Converter] for the given type.
    /// @throws UnavailableConverterException If no converter is available for `t`.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    @Override
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    public default Converter<?> get(@NonNull Type t) throws UnavailableConverterException {
        if (t instanceof Class<?> k) return getOf(k);
        if (t instanceof ParameterizedType p) return getOf(p);
        if (t instanceof WildcardType w) return getOf(w);
        if (t instanceof GenericArrayType g) return getOf(g);
        if (t instanceof TypeVariable<?> v) return getOf(v);
        return getOfUndetermined(t);
    }

    /// Returns a typed converter for the given undetermined type.
    ///
    /// The default implementation does not supports this operation and always throws an
    /// [UnavailableConverterException], but overriden implementations may do otherwise.
    ///
    /// @param t The undetermined type.
    /// @return A [Converter] typed to the given type.
    /// @throws UnavailableConverterException If no converter is available for `t` or if this operation is not supported.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    public default Converter<?> getOfUndetermined(@NonNull Type t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    /// Returns a converter for the parameterized type `t`.
    ///
    /// The default implementation tries [#makeList(ParameterizedType)], [#makeSet(ParameterizedType)],
    /// [#makeCollection(ParameterizedType)], [#makeOptional(ParameterizedType)],
    /// and [#makeMap(ParameterizedType)] in order; throws if none match.
    /// Overriden implementations might do something different.
    ///
    /// @param t The parameterized type to look up.
    /// @return A [Converter] for the given parameterized type.
    /// @throws UnavailableConverterException If no converter is available for `t`.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    public default Converter<?> getOf(@NonNull ParameterizedType t) throws UnavailableConverterException {
        Optional<? extends Converter<?>> x = makeList(t);
        if (x.isPresent()) return x.get();

        x = makeSet(t);
        if (x.isPresent()) return x.get();

        x = makeCollection(t);
        if (x.isPresent()) return x.get();

        x = makeOptional(t);
        if (x.isPresent()) return x.get();

        x = makeMap(t);
        if (x.isPresent()) return x.get();

        throw UnavailableConverterException.noConverterFor(t);
    }

    /// Returns a typed converter for the given generic array type.
    ///
    /// The default implementation does not supports this operation and always throws an
    /// [UnavailableConverterException], but overriden implementations may do otherwise.
    ///
    /// @param t The generic array type.
    /// @return A [Converter] typed to the given type.
    /// @throws UnavailableConverterException If no converter is available for `t` or if this operation is not supported.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    public default Converter<?> getOf(@NonNull GenericArrayType t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    /// Returns a typed converter for the given wildcard type.
    ///
    /// The default implementation does not supports this operation and always throws an
    /// [UnavailableConverterException], but overriden implementations may do otherwise.
    ///
    /// @param t The wildcard type.
    /// @return A [Converter] typed to the given type.
    /// @throws UnavailableConverterException If no converter is available for `t` or if this operation is not supported.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    public default Converter<?> getOf(@NonNull WildcardType t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    /// Returns a typed converter for the given type variable.
    ///
    /// The default implementation does not supports this operation and always throws an
    /// [UnavailableConverterException], but overriden implementations may do otherwise.
    ///
    /// @param t The type variable.
    /// @return A [Converter] typed to the given type.
    /// @throws UnavailableConverterException If no converter is available for `t` or if this operation is not supported.
    /// @throws IllegalArgumentException If `t` is `null`.
    @NonNull
    public default Converter<?> getOf(@NonNull TypeVariable<?> t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    /// Returns a typed converter for the given class.
    ///
    /// Checks direct mappings first, then tries [#makeArray(Class)], [#makeEnum(Class)],
    /// and [#makeRecord(Class)] in order; throws if none match.
    ///
    /// @param <E> The target type.
    /// @param klass The class to look up.
    /// @return A [Converter] typed to `E`.
    /// @throws UnavailableConverterException If no converter is available for `klass`.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default <E> Converter<E> getOf(@NonNull Class<E> klass) throws UnavailableConverterException {
        var x = directMapping(klass);
        if (x.isPresent()) return x.get();

        x = makeArray(klass);
        if (x.isPresent()) return x.get();

        x = makeEnum((Class) klass);
        if (x.isPresent()) return x.get();

        x = makeRecord((Class) klass);
        if (x.isPresent()) return x.get();

        throw UnavailableConverterException.noConverterFor(klass);
    }

    /// Creates a converter for the given class if it is a non-multidimensional array class.
    ///
    /// The default implementation always returns a [ArrayConverter], but overriden implementations may do otherwise.
    ///
    /// @param <E> The array type.
    /// @param klass The class to inspect.
    /// @return An optional [ArrayConverter] if `klass` is a single-dimensional array class; empty otherwise.
    /// @throws UnavailableConverterException If no converter is available for the element type.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default <E> Optional<? extends Converter<E>> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isArray()) return Optional.empty();
        var arg = klass.getComponentType();
        if (arg.isArray()) return Optional.empty();
        return Optional.of(new ArrayConverter(this, arg));
    }

    /// Creates a converter for the given class if it is an enum class.
    ///
    /// The default implementation always returns a [EnumConverter], but overriden implementations may do otherwise.
    ///
    /// @param <E> The enum type.
    /// @param klass The class to inspect.
    /// @return An optional [EnumConverter] if `klass` is an enum class; empty otherwise.
    /// @throws UnavailableConverterException If converter creation fails.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    public default <E extends Enum<E>> Optional<? extends Converter<E>> makeEnum(@NonNull Class<E> klass)
            throws UnavailableConverterException
    {
        if (!klass.isEnum()) return Optional.empty();
        return Optional.of(new EnumConverter<>(klass));
    }

    /// Creates a converter for the given class if it is a record class.
    ///
    /// The default implementation always returns a [RecordConverter], but overriden implementations may do otherwise.
    ///
    /// @param <E> The record type.
    /// @param klass The class to inspect.
    /// @return An optional [RecordConverter] if `klass` is a record class; empty otherwise.
    /// @throws UnavailableConverterException If converter creation fails or recursive record definition is detected.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    public default <E extends Record> Optional<? extends Converter<E>> makeRecord(@NonNull Class<E> klass)
            throws UnavailableConverterException
    {
        if (!klass.isRecord()) return Optional.empty();
        return Optional.of(new RecordConverter<>(this, klass));
    }

    /// Looks up the given class in the direct mappings and returns its converter if present.
    ///
    /// @param <E> The target type.
    /// @param klass The class to look up.
    /// @return An optional converter for `klass`; empty if not found in the direct mappings.
    /// @throws UnavailableConverterException If lookup fails.
    /// @throws IllegalArgumentException If `klass` is `null`.
    @NonNull
    @SuppressWarnings("unchecked")
    public default <E> Optional<? extends Converter<E>> directMapping(@NonNull Class<E> klass)
            throws UnavailableConverterException
    {
        return Optional.ofNullable((Converter<E>) directMappings().get(klass));
    }

    /// If `p` is `Collection<X>` for some concrete class `X`, creates a converter.
    ///
    /// The default implementation always returns a [CollectionConverter], but overriden implementations may do otherwise.
    ///
    /// @param p The parameterized type to inspect.
    /// @return An optional converter for `p`; empty if `p` is not `Collection<X>`.
    ///         The default implementation produces [CollectionConverter].
    /// @throws UnavailableConverterException If converter creation fails.
    /// @throws IllegalArgumentException If `p` is `null`.
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends Collection<?>>> makeCollection(@NonNull ParameterizedType p)
            throws UnavailableConverterException
    {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != Collection.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new CollectionConverter(this, p));
    }

    /// If `p` is `Set<X>` for some concrete class `X`, creates a converter.
    ///
    /// The default implementation always returns a [SetConverter], but overriden implementations may do otherwise.
    ///
    /// @param p The parameterized type to inspect.
    /// @return An optional converter for `p`; empty if `p` is not `Set<X>`.
    ///         The default implementation produces [SetConverter].
    /// @throws UnavailableConverterException If converter creation fails.
    /// @throws IllegalArgumentException If `p` is `null`.
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends Set<?>>> makeSet(@NonNull ParameterizedType p)
            throws UnavailableConverterException
    {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != Set.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new SetConverter(this, p));
    }

    /// If `p` is `List<X>` for some concrete class `X`, creates a converter.
    ///
    /// The default implementation always returns a [ListConverter], but overriden implementations may do otherwise.
    ///
    /// @param p The parameterized type to inspect.
    /// @return An optional converter for `p`; empty if `p` is not `List<X>`.
    ///         The default implementation produces [ListConverter].
    /// @throws UnavailableConverterException If converter creation fails.
    /// @throws IllegalArgumentException If `p` is `null`.
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends List<?>>> makeList(@NonNull ParameterizedType p)
            throws UnavailableConverterException
    {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != List.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new ListConverter(this, p));
    }

    /// If `p` is `Optional<X>` for some concrete class `X`, creates a converter.
    ///
    /// The default implementation always returns an [OptionalConverter], but overriden implementations may do otherwise.
    ///
    /// @param p The parameterized type to inspect.
    /// @return An optional converter for `p`; empty if `p` is not `Optional<X>`.
    ///         The default implementation produces [OptionalConverter].
    /// @throws UnavailableConverterException If converter creation fails.
    /// @throws IllegalArgumentException If `p` is `null`.
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends Optional<?>>> makeOptional(@NonNull ParameterizedType p)
            throws UnavailableConverterException
    {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != Optional.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new OptionalConverter(this, p));
    }

    /// If `p` is `Map<X, Y>` for some concrete classes `X` and `Y`, creates a converter.
    ///
    /// However, this operation is not supported in the default implementation, that always returns empty.
    /// Overriden implementations might do otherwise.
    ///
    /// @param p The parameterized type (ignored).
    /// @return Always [Optional#empty()].
    /// @throws UnavailableConverterException Never thrown.
    /// @throws IllegalArgumentException If `p` is `null`.
    @NonNull
    public default Optional<? extends Converter<? extends Map<?, ?>>> makeMap(@NonNull ParameterizedType p)
            throws UnavailableConverterException
    {
        return Optional.empty();
    }

    /// Returns the map of class-to-converter direct mappings for this factory instance.
    ///
    /// @return An immutable map from [Class] to [Converter] for all directly registered types.
    @NonNull
    public default Map<Class<?>, Converter<?>> directMappings() {
        return rootMap();
    }

    /// Creates a new [StdConverterFactory] with the given class-to-converter mapping added to this factory's mappings.
    ///
    /// @param <E> The type of the additional converter.
    /// @param klass The class to register the converter for.
    /// @param cvt The converter to register.
    /// @return A new [StdConverterFactory] instance that includes the additional mapping.
    /// @throws IllegalArgumentException If `klass` is `null`.
    /// @throws IllegalArgumentException If `cvt` is `null`.
    @NonNull
    public default <E> StdConverterFactory extend(@NonNull Class<E> klass, @NonNull Converter<E> cvt) {
        var temp = new HashMap<>(directMappings());
        temp.put(klass, cvt);
        var map = Map.copyOf(temp);
        return new StdConverterFactory() {
            @NonNull
            @Override
            @SuppressWarnings("ReturnOfCollectionOrArrayField") // Safe because the returned map is immutable.
            @SuppressFBWarnings("EI_EXPOSE_REP") // Safe because the returned map is immutable.
            public Map<Class<?>, Converter<?>> directMappings() {
                return map;
            }
        };
    }

    @Generated
    private static void assertEquals(int a, int b) {
        if (a != b) throw new AssertionError();
    }
}