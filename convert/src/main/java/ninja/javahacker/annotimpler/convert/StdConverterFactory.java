package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public interface StdConverterFactory extends ConverterFactory {

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
        return INSTANCE.directMappings();
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

    @NonNull
    public default Converter<?> getOf(@NonNull GenericArrayType t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull WildcardType t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    @NonNull
    public default Converter<?> getOf(@NonNull TypeVariable<?> t) throws UnavailableConverterException {
        throw UnavailableConverterException.noConverterFor(t);
    }

    @NonNull
    public default Converter<?> getOfUndetermined(@NonNull Type t) throws UnavailableConverterException {
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
    public default <E> Optional<? extends Converter<E>> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isArray()) return Optional.empty();
        var arg = klass.getComponentType();
        if (arg.isArray()) return Optional.empty();
        return Optional.of(new ArrayConverter(this, arg));
    }

    @NonNull
    public default <E extends Enum<E>> Optional<? extends Converter<E>> makeEnum(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isEnum()) return Optional.empty();
        return Optional.of(new EnumConverter<>(klass));
    }

    @NonNull
    public default <E extends Record> Optional<? extends Converter<E>> makeRecord(@NonNull Class<E> klass) throws UnavailableConverterException {
        if (!klass.isRecord()) return Optional.empty();
        return Optional.of(new RecordConverter<>(this, klass));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public default <E> Optional<? extends Converter<E>> directMapping(@NonNull Class<E> klass) throws UnavailableConverterException {
        return Optional.ofNullable((Converter<E>) directMappings().get(klass));
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends Collection<?>>> makeCollection(@NonNull ParameterizedType p) throws UnavailableConverterException {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != Collection.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new CollectionConverter(this, p));
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends Set<?>>> makeSet(@NonNull ParameterizedType p) throws UnavailableConverterException {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != Set.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new SetConverter(this, p));
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends List<?>>> makeList(@NonNull ParameterizedType p) throws UnavailableConverterException {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != List.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new ListConverter(this, p));
    }

    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public default Optional<? extends Converter<? extends Optional<?>>> makeOptional(@NonNull ParameterizedType p) throws UnavailableConverterException {
        var args = p.getActualTypeArguments();
        if (p.getRawType() != Optional.class) return Optional.empty();
        assertEquals(args.length, 1);
        if (!(args[0] instanceof Class<?>)) return Optional.empty();
        return Optional.of(new OptionalConverter(this, p));
    }

    @NonNull
    public default Optional<? extends Converter<? extends Map<?, ?>>> makeMap(@NonNull ParameterizedType p) throws UnavailableConverterException {
        return Optional.empty();
    }

    @NonNull
    public default Map<Class<?>, Converter<?>> directMappings() {
        return rootMap();
    }

    @NonNull
    public default <E> StdConverterFactory extend(@NonNull Class<E> klass, @NonNull Converter<E> cvt) {
        var temp = new HashMap<>(directMappings());
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

    @Generated
    private static void assertEquals(int a, int b) {
        if (a != b) throw new AssertionError();
    }
}