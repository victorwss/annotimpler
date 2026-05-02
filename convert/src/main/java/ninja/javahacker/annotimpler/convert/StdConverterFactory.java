package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public interface StdConverterFactory extends ConverterFactory {

    public static final StdConverterFactory INSTANCE = start();

    private static StdConverterFactory start() {
        var map = rootMap();

        return new StdConverterFactory() {
            @NonNull
            @Override
            @SuppressWarnings("ReturnOfCollectionOrArrayField")
            public Map<Class<?>, Converter<?>> directMappings() {
                return map;
            }
        };
    }

    @NonNull
    @SuppressWarnings("element-type-mismatch")
    public static Map<Class<?>, Converter<?>> rootMap() {
        return Map.ofEntries(
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
    }

    @NonNull
    @Override
    public default Converter<?> get(@NonNull Type t) throws UnavailableConverterException {
        if (t instanceof Class<?> k) return getOf(k);
        if (t instanceof ParameterizedType p) return getOf(p);
        if (t instanceof WildcardType w) return getOf(w);
        if (t instanceof GenericArrayType g) return getOf(g);
        if (t instanceof TypeVariable<?> v) return getOf(v);
        return getOfUndetermined(t);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull ParameterizedType p) throws UnavailableConverterException {
        var args = p.getActualTypeArguments();
        if (args.length == 1 && args[0] instanceof Class<?>) {
            var raw = p.getRawType();
            if (raw == Collection.class) return makeCollection(p);
            if (raw == List.class) return makeList(p);
            if (raw == Set.class) return makeSet(p);
            if (raw == Optional.class) return makeOptional(p);
        }
        throw UnavailableConverterException.noConverterFor(p);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull GenericArrayType t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull WildcardType t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    @NonNull
    public default <E extends GenericDeclaration> Converter<E> getOf(@NonNull TypeVariable<E> t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    @NonNull
    public default <E extends GenericDeclaration> Converter<E> getOfUndetermined(@NonNull Type t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

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

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default <E> Optional<Converter<E>> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isArray()) return Optional.empty();
        var arg = klass.getComponentType();
        if (arg.isArray()) return Optional.empty();
        return Optional.of(new ArrayConverter(this, arg));
    }

    @NonNull
    public default <E extends Enum<E>> Optional<Converter<E>> makeEnum(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isEnum()) return Optional.empty();
        return Optional.of(new EnumConverter<>(klass));
    }

    @NonNull
    public default <E extends Record> Optional<Converter<E>> makeRecord(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isRecord()) return Optional.empty();
        return Optional.of(new RecordConverter<>(this, klass));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public default <E> Optional<Converter<E>> directMapping(@NonNull Class<E> klass) throws UnavailableConverterException {
        return Optional.ofNullable((Converter<E>) directMappings().get(klass));
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

    @NonNull
    public default Map<Class<?>, Converter<?>> directMappings() {
        return rootMap();
    }

    @NonNull
    public default <E> StdConverterFactory extend(@NonNull Class<E> klass, @NonNull Converter<E> cvt) {
        var temp = new HashMap<>(rootMap());
        temp.put(klass, cvt);
        var map = Map.copyOf(temp);
        return new StdConverterFactory() {
            @NonNull
            @Override
            @SuppressWarnings("ReturnOfCollectionOrArrayField")
            public Map<Class<?>, Converter<?>> directMappings() {
                return map;
            }
        };
    }
}