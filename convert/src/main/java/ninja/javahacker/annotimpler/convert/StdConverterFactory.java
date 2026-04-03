package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

@PackagePrivate
enum StdConverterFactory implements ConverterFactory {
    INSTANCE;

    private static final Map<Class<?>, Converter<?>> MAP = Map.ofEntries(
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
            Map.entry(java.sql.Array.class, SqlArrayConverter.INSTANCE),
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

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <E> Converter<E> get(@NonNull Type t) throws UnavailableConverterException {
        if (t instanceof Class<?> k) return (Converter<E>) get(k);
        if (t instanceof ParameterizedType p) {
            var args = p.getActualTypeArguments();
            if (args.length == 1) {
                if (p.getRawType() == Collection.class && args[0] instanceof Class<?> k) {
                    return (Converter<E>) new ListConverter<>(this, k);
                }
                if (p.getRawType() == List.class && args[0] instanceof Class<?> k) {
                    return (Converter<E>) new ListConverter<>(this, k);
                }
                if (p.getRawType() == Set.class && args[0] instanceof Class<?> k) {
                    return (Converter<E>) new SetConverter<>(this, k);
                }
                if (p.getRawType() == Optional.class && args[0] instanceof Class<?> k) {
                    return (Converter<E>) new OptionalConverter<>(this, k);
                }
            }
        }
        throw new UnavailableConverterException("No converter for " + t.getTypeName(), t);
    }

    @NonNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E> Converter<E> get(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (klass.isEnum()) return (Converter<E>) enums((Class) klass);
        if (klass.isRecord()) return (Converter<E>) records((Class) klass);
        if (klass.isArray() && klass != byte[].class) {
            var arg = klass.getComponentType();
            if (arg.isArray()) throw new UnavailableConverterException("No converter for multidimensional arrays.", klass);
            return (Converter<E>) array(arg);
        }
        var s = MAP.get(klass);
        if (s == null) throw new UnavailableConverterException("No converter for " + klass.getName(), klass);
        return (Converter<E>) s;
    }

    @NonNull
    private <E> ArrayConverter<E> array(@NonNull Class<E> klass) throws UnavailableConverterException {
        checkNotNull(klass);
        return new ArrayConverter<>(this, klass);
    }

    @NonNull
    private <E extends Enum<E>> EnumConverter<E> enums(@NonNull Class<E> klass) {
        checkNotNull(klass);
        return new EnumConverter<>(klass);
    }

    @NonNull
    private <E extends Record> RecordConverter<E> records(@NonNull Class<E> klass) throws UnavailableConverterException {
        checkNotNull(klass);
        return new RecordConverter<>(this, klass);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}