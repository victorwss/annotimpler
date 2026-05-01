package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public interface StdConverterFactory extends ConverterFactory {

    public static final StdConverterFactory INSTANCE = new StdConverterFactory() {};

    public static final ConverterFactory SIMPLE = simple();

    @SuppressWarnings("element-type-mismatch")
    private static ConverterFactory simple() {
        var map = Map.<Class<?>, Converter<?>>ofEntries(
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
        return t -> Optional
                .ofNullable(map.get(t))
                .orElseThrow(() -> new UnavailableConverterException("No converter for " + t.getTypeName() + ".", t));
    }

    public default Converter<?> getOf(@NonNull ParameterizedType p) throws UnavailableConverterException {
        var args = p.getActualTypeArguments();
        if (args.length == 1 && args[0] instanceof Class<?>) {
            var raw = p.getRawType();
            if (raw == Collection.class) return makeCollection(p);
            if (raw == List.class) return makeList(p);
            if (raw == Set.class) return makeSet(p);
            if (raw == Optional.class) return makeOptional(p);
        }
        throw new UnavailableConverterException("No converter for " + p.getTypeName() + ".", p);
    }

    @NonNull
    @Override
    public default Converter<?> get(@NonNull Type t) throws UnavailableConverterException {
        if (t instanceof Class<?> k) return getOf(k);
        if (t instanceof ParameterizedType p) return getOf(p);
        if (t instanceof WildcardType w) return getOf(w);
        if (t instanceof GenericArrayType g) return getOf(g);
        if (t instanceof TypeVariable<?> V) return getOf(V);
        throw new UnavailableConverterException("No converter for " + t.getTypeName() + ".", t);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull GenericArrayType t) throws UnavailableConverterException {
        throw new UnavailableConverterException("No converter for " + t.getTypeName() + ".", t);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull WildcardType t) throws UnavailableConverterException {
        throw new UnavailableConverterException("No converter for " + t.getTypeName() + ".", t);
    }

    @NonNull
    public default <E extends GenericDeclaration> Converter<E> getOf(@NonNull TypeVariable<E> t) throws UnavailableConverterException {
        throw new UnavailableConverterException("No converter for " + TypeName.of(t) + ".", t);
    }

    @NonNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default <E> Converter<E> getOf(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (klass.isEnum()) return makeEnum((Class) klass);
        if (klass.isRecord()) return makeRecord((Class) klass);
        if (klass.isArray() && klass != byte[].class && klass != char[].class) return (Converter<E>) makeArray(klass);
        return simple(klass);
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default <E> Converter<E[]> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isArray()) throw new IllegalArgumentException();
        var arg = klass.getComponentType();
        if (arg.isArray()) throw new UnavailableConverterException("No converter for multidimensional arrays.", klass);
        return new ArrayConverter(this, arg);
    }

    @NonNull
    public default <E extends Enum<E>> Converter<E> makeEnum(@NonNull Class<E> klass) {
        if (!klass.isEnum()) throw new IllegalArgumentException();
        return new EnumConverter<>(klass);
    }

    @NonNull
    public default <E extends Record> Converter<E> makeRecord(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isRecord()) throw new IllegalArgumentException();
        return new RecordConverter<>(this, klass);
    }

    @NonNull
    public default <E> Converter<E> simple(@NonNull Class<E> klass) throws UnavailableConverterException {
        return SIMPLE.getOf(klass);
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Converter<Collection<?>> makeCollection(@NonNull ParameterizedType p) throws UnavailableConverterException {
        if (p.getRawType() != Collection.class) throw new IllegalArgumentException();
        return new CollectionConverter(this, p);
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Converter<Collection<?>> makeSet(@NonNull ParameterizedType p) throws UnavailableConverterException {
        if (p.getRawType() != Set.class) throw new IllegalArgumentException();
        return new SetConverter(this, p);
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Converter<Collection<?>> makeList(@NonNull ParameterizedType p) throws UnavailableConverterException {
        if (p.getRawType() != List.class) throw new IllegalArgumentException();
        return new ListConverter(this, p);
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Converter<Collection<?>> makeOptional(@NonNull ParameterizedType p) throws UnavailableConverterException {
        if (p.getRawType() != Optional.class) throw new IllegalArgumentException();
        return new OptionalConverter(this, p);
    }
}