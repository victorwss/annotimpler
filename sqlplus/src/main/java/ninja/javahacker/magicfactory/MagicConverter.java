package ninja.javahacker.magicfactory;

import module java.base;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import lombok.NonNull;

public final class MagicConverter {

    private static final DateTimeFormatter FORMATTER_DT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATTER_D = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATTER_T = DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    private static final Set<Class<?>> NUMBERS_OUT = Set.of(
            Boolean.class, Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class,
            boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class,
            BigInteger.class, BigDecimal.class,
            OptionalInt.class, OptionalLong.class, OptionalDouble.class
    );

    private static final Set<Class<?>> NUMBERS_IN = Set.of(
            Boolean.class, Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class,
            boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class,
            BigInteger.class, BigDecimal.class
    );

    private static final Set<Class<?>> TEMPORALS = Set.of(
            java.util.Date.class, java.sql.Date.class, Timestamp.class, Calendar.class, GregorianCalendar.class,
            LocalDate.class, LocalDateTime.class, Instant.class, OffsetDateTime.class, ZonedDateTime.class
    );

    private static final Set<Class<?>> CLOCKS = Set.of(
            java.sql.Time.class, LocalTime.class
    );

    private static final Set<Class<?>> NO_CLOCKS = Set.of(
            java.sql.Date.class, LocalDate.class
    );

    private static final Set<Class<?>> UNSTRINGFIABLES = Set.of(
            Boolean.class, Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class,
            boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class,
            BigInteger.class, BigDecimal.class,
            OptionalInt.class, OptionalLong.class, OptionalDouble.class,

            java.util.Date.class, java.sql.Date.class, Timestamp.class, Calendar.class, GregorianCalendar.class,
            LocalDate.class, LocalDateTime.class, Instant.class, OffsetDateTime.class, ZonedDateTime.class,

            LocalTime.class, java.sql.Time.class, String.class
    );

    private static final ThreadLocal<Set<Type>> onStack = new ThreadLocal<>();

    private MagicConverter() {
        throw new UnsupportedOperationException();
    }

    private static AssertionError newAssertionError() {
        throw new AssertionError();
    }

    @NonNull
    private static <T extends Record> T valueToRecord(@NonNull Object value, @NonNull Class<T> recordClass) throws ConstructionException {
        // Protege contra StackOverflowError.
        var gots = onStack.get();
        var wasNull = false;
        if (gots == null) {
            gots = new HashSet<>(10);
            onStack.set(gots);
            wasNull = true;
        }
        if (gots.contains(recordClass)) throw new ConstructionException("Recursive record class.", recordClass);
        gots.add(recordClass);

        try {
            // Seleciona o construtor ou método mais adequado.
            var exec = MagicFactory.of(recordClass);

            // Verifica se o construtor ou método só tem um campo.
            var params = exec.getParameterTypes();
            if (params.size() != 1) {
                throw new ConstructionException("Non-single value record class where single-valued was expected.", recordClass);
            }

            // Converte o valor para o tipo esperado pelo construtor ou método.
            Object v2 = forValue(value, params.getFirst());

            // Instancia o record.
            return exec.create(v2);
        } finally {
            gots.remove(recordClass);
            if (wasNull) onStack.remove();
        }
    }

    @Nullable
    private static <E extends Enum<E>> E valueToEnum(@NonNull Object value, @NonNull Class<E> enumClass) throws ConstructionException {
        if ("".equals(value)) return null;
        if (enumClass.isInstance(value)) return enumClass.cast(value);
        if (NUMBERS_IN.contains(value.getClass())) return enumClass.getEnumConstants()[convertNumber(value, Integer.class)];
        if (value instanceof String v2) {
            return Stream.of(enumClass.getEnumConstants()).filter(e -> Objects.equals(v2, e.name())).findAny().orElseThrow();
        }
        throw new ConstructionException("Can't read value as enum class.", enumClass);
    }

    @Nullable
    public static Object forValue(@Nullable Object value, @NonNull Type target) throws ConstructionException {
        if (target instanceof ParameterizedType p && p.getRawType() instanceof Class<?> r) {
            var p1 = p.getActualTypeArguments()[0];
            if (r.equals(Optional.class)) {
                return Optional.ofNullable(forValue(value, p1));
            }
            if (r.isAssignableFrom(Collection.class)) {
                var e = forValue(value, p1);
                if (e == null) return zero(r);
                return singleton(e, r.asSubclass(Collection.class));
            }
        }

        if (!(target instanceof Class<?> targetClass)) {
            throw new ConstructionException("Can't convert " + value + " to " + target.getTypeName(), target);
        }

        return forValue(value, targetClass);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T forValue(@Nullable Object value, @NonNull Class<T> target) throws ConstructionException {
        if (value == null) return target.cast(zero(target));
        var from = value.getClass();

        // Se o tipo já é compatível, retorna direto.
        if (target.isInstance(value)) return target.cast(value);

        if (target.isArray()) {
            var comp = target.getComponentType();
            var out = forValue(value, comp);
            var array = (Object[]) Array.newInstance(comp, 1);
            Array.set(array, 0, out);
            return target.cast(array);
        }

        if (target.isRecord()) return target.cast(valueToRecord(value, target.asSubclass(Record.class)));
        if ("".equals(value)) return target.cast(zero(target));
        if (target == Optional.class) return target.cast(Optional.of(value));
        if (target.isAssignableFrom(Collection.class)) {
            return target.cast(singleton(value, target.asSubclass(Collection.class)));
        }

        // Records e enums.
        if (target.isEnum()) return target.cast(valueToEnum(value, target.asSubclass(Enum.class)));

        // Se o valor é String, mas o tipo é outra coisa, tenta converter.
        if (value instanceof String vs) return target.cast(unstringify(vs, target));

        // Tenta ajustar se forem tipos numéricos diferentes.
        if (NUMBERS_OUT.contains(target) && NUMBERS_IN.contains(from)) return convertNumber(value, target);

        // Tenta ajustar se forem tipos temporais diferentes.
        if (TEMPORALS.contains(target) && TEMPORALS.contains(from)) return convertTemporal(value, target);

        // Tenta ajustar se forem tipos de horários diferentes.
        if (CLOCKS.contains(target) && CLOCKS.contains(from)) return convertClock(value, target);

        // Tenta ajustar se for extrair as horas de uma data e horário.
        if (CLOCKS.contains(target) && TEMPORALS.contains(from) && !NO_CLOCKS.contains(from)) {
            return convertClock(LocalTime.ofInstant(convertTemporal(value, Instant.class), ZoneOffset.UTC), target);
        }

        throw new ConstructionException("Can't convert " + value + " to " + target.getSimpleName(), target);
    }

    private static char charValueExact(BigDecimal bd) {
        long num = bd.intValueExact();     // will check decimal part
        if ((char) num != num) throw new ArithmeticException("Overflow");
        return (char) num;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E> E convertNumber(@NonNull Object value, @NonNull Class<E> target) {
        if (!NUMBERS_OUT.contains(target) || !NUMBERS_IN.contains(value.getClass())) {
            throw new IllegalArgumentException("Can't convert " + value + " to " + target.getSimpleName());
        }
        return (E) convertNumber2(value, wrap(target));
    }

    @NonNull
    private static Object convertNumber2(@NonNull Object value, @NonNull Class<?> target) {
        if (target == value.getClass()) return value;
        BigDecimal v2 = switch (value) {
            case Boolean v -> v ? BigDecimal.ONE : BigDecimal.ZERO;
            case Byte v -> BigDecimal.valueOf(v);
            case Short v -> BigDecimal.valueOf(v);
            case Character v -> BigDecimal.valueOf(v);
            case Integer v -> BigDecimal.valueOf(v);
            case Long v -> BigDecimal.valueOf(v);
            case Float v -> BigDecimal.valueOf(v);
            case Double v -> BigDecimal.valueOf(v);
            case BigInteger v -> new BigDecimal(v);
            case BigDecimal v -> v;
            default -> throw newAssertionError();
        };
        if (target == Boolean.class) return v2.signum() != 0;
        if (target == Byte.class) return v2.byteValueExact();
        if (target == Short.class) return v2.shortValueExact();
        if (target == Character.class) return charValueExact(v2);
        if (target == Integer.class) return v2.intValueExact();
        if (target == Long.class) return v2.longValueExact();
        if (target == Float.class) return v2.floatValue();
        if (target == Double.class) return v2.doubleValue();
        if (target == BigInteger.class) return v2.toBigIntegerExact();
        if (target == BigDecimal.class) return v2;
        if (target == OptionalInt.class) return OptionalInt.of(v2.intValueExact());
        if (target == OptionalLong.class) return OptionalLong.of(v2.longValueExact());
        if (target == OptionalDouble.class) return OptionalDouble.of(v2.doubleValue());
        throw newAssertionError();
    }

    @NonNull
    public static <E> E convertTemporal(@NonNull Object value, @NonNull Class<E> target) {
        if (!TEMPORALS.contains(target) || !TEMPORALS.contains(value.getClass())) {
            throw new IllegalArgumentException("Can't convert " + value + " to " + target.getSimpleName());
        }
        return target.cast(convertTemporal2(value, target));
    }

    @NonNull
    private static Object convertTemporal2(@NonNull Object value, @NonNull Class<?> target) {
        if (target == value.getClass()) return value;
        Instant v2 = switch (value) {
            case LocalDate v -> v.atTime(LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC).toInstant();
            case LocalDateTime v -> v.atZone(ZoneOffset.UTC).toInstant();
            case ZonedDateTime v -> v.toInstant();
            case OffsetDateTime v -> v.toInstant();
            case java.util.Date v -> v.toInstant();
            case Calendar v -> v.toInstant();
            case Instant v -> v;
            default -> throw newAssertionError();
        };
        if (target == LocalDate.class) return LocalDate.ofInstant(v2, ZoneOffset.UTC);
        if (target == LocalDateTime.class) return LocalDateTime.ofInstant(v2, ZoneOffset.UTC);
        if (target == ZonedDateTime.class) return ZonedDateTime.ofInstant(v2, ZoneOffset.UTC);
        if (target == OffsetDateTime.class) return OffsetDateTime.ofInstant(v2, ZoneOffset.UTC);
        if (target == java.util.Date.class) return java.util.Date.from(v2);
        if (target == java.sql.Date.class) return java.sql.Date.from(v2);
        if (target == Timestamp.class) return Timestamp.from(v2);
        if (target == GregorianCalendar.class) return GregorianCalendar.from(ZonedDateTime.ofInstant(v2, ZoneOffset.UTC));
        if (target == Calendar.class) return GregorianCalendar.from(ZonedDateTime.ofInstant(v2, ZoneOffset.UTC));
        if (target == Instant.class) return v2;
        throw newAssertionError();
    }

    @NonNull
    public static <E> E convertClock(@NonNull Object value, @NonNull Class<E> target) {
        if (!CLOCKS.contains(target) || !CLOCKS.contains(value.getClass())) {
            throw new IllegalArgumentException("Can't convert " + value + " to " + target.getSimpleName());
        }
        return target.cast(convertClock2(value, target));
    }

    @NonNull
    private static Object convertClock2(@NonNull Object value, @NonNull Class<?> target) {
        if (target == value.getClass()) return value;
        LocalTime v2 = switch (value) {
            case LocalTime v -> v;
            case java.sql.Time v -> v.toLocalTime();
            default -> throw newAssertionError();
        };
        if (target == LocalTime.class) return v2;
        if (target == java.sql.Time.class) return java.sql.Time.valueOf(v2);
        throw newAssertionError();
    }

    @NonNull
    private static Class<?> wrap(@NonNull Class<?> target) {
        return WRAPPERS.getOrDefault(target, target);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E> E unstringify(@NonNull String value, @NonNull Class<E> target) {
        if (!UNSTRINGFIABLES.contains(target)) {
            throw new IllegalArgumentException("Can't convert " + value + " to " + target.getSimpleName());
        }
        if (value.isEmpty()) return null;
        return (E) unstringify2(value, wrap(target));
    }

    @NonNull
    private static <E> E unstringify2(@NonNull String value, @NonNull Class<E> target) {
        return target.cast(unstringify3(value, target));
    }

    @NonNull
    private static Object unstringify3(@NonNull String value, @NonNull Class<?> target) {
        Supplier<IllegalArgumentException> xxx = () ->
                new IllegalArgumentException("Can't convert " + value + " to " + target.getSimpleName());
        try {
            if (target == OptionalInt   .class) return OptionalInt.of(Integer.parseInt(value));
            if (target == Integer       .class) return Integer.valueOf(value);
            if (target == OptionalLong  .class) return OptionalLong.of(Long.parseLong(value));
            if (target == Long          .class) return Long.valueOf(value);
            if (target == OptionalDouble.class) return OptionalDouble.of(Double.parseDouble(value));
            if (target == Double        .class) return Double.valueOf(value);
            if (target == Byte          .class) return Byte.valueOf(value);
            if (target == Short         .class) return Short.valueOf(value);
            if (target == Float         .class) return Float.valueOf(value);
            if (target == BigInteger    .class) return new BigInteger(value);
            if (target == BigDecimal    .class) return new BigDecimal(value);
            if (target == LocalDate     .class) return LocalDate    .parse(value, FORMATTER_D );
            if (target == LocalDateTime .class) return LocalDateTime.parse(value, FORMATTER_DT);
            if (target == LocalTime     .class) return LocalTime    .parse(value, FORMATTER_T );
            if (target == java.sql.Date .class) return java.sql.Date .valueOf(unstringify2(value, LocalDate.class));
            if (target == Timestamp     .class) return Timestamp     .valueOf(unstringify2(value, LocalDateTime.class));
            if (target == java.util.Date.class) return java.util.Date.from(unstringify2(value, Instant.class));
            if (target == Instant       .class) return unstringify2(value, LocalDateTime.class).atZone(ZoneOffset.UTC).toInstant();
            if (target == Calendar      .class || target == GregorianCalendar.class) {
                var ins = unstringify2(value, Instant.class);
                return GregorianCalendar.from(ZonedDateTime.ofInstant(ins, ZoneOffset.UTC));
            }
            if (target == Boolean       .class && "true" .equals(value)) return Boolean.TRUE;
            if (target == Boolean       .class && "false".equals(value)) return Boolean.FALSE;
            if (target == Character     .class) {
                char c = value.charAt(0);
                var b = String.valueOf(c);
                if (!Objects.equals(value, b)) throw xxx.get();
                return c;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException | DateTimeParseException x) {
            throw new IllegalArgumentException(xxx.get().getMessage(), x);
        }
        throw xxx.get();
    }

    @Nullable
    public static Object zero(@NonNull Class<?> target) {
        if (target.isArray()) {
            var comp = target.getComponentType();
            var array = Array.newInstance(comp, 0);
            return target.cast(array);
        }
        if (target == boolean       .class) return false;
        if (target == byte          .class) return (byte) 0;
        if (target == char          .class) return (char) 0;
        if (target == short         .class) return (short) 0;
        if (target == int           .class) return 0;
        if (target == long          .class) return 0L;
        if (target == float         .class) return 0.0f;
        if (target == double        .class) return 0.0;
        if (target == OptionalInt   .class) return OptionalInt.empty();
        if (target == OptionalLong  .class) return OptionalLong.empty();
        if (target == OptionalDouble.class) return OptionalDouble.empty();
        if (target == Optional      .class) return Optional.empty();
        if (target == List          .class) return List.of();
        if (target == Set           .class) return Set.of();
        if (target == Collection    .class) return List.of();
        if (target == SortedSet     .class) return Collections.emptySortedSet();
        if (target == NavigableSet  .class) return Collections.emptyNavigableSet();
        if (target == ArrayList     .class) return new ArrayList<Object>(0);
        if (target == LinkedList    .class) return new LinkedList<Object>();
        if (target == HashSet       .class) return new HashSet<Object>(0);
        if (target == LinkedHashSet .class) return new LinkedHashSet<Object>(0);
        if (target == TreeSet       .class) return new TreeSet<Object>();
        return null;
    }

    @NonNull
    public static <E> Collection<E> singleton(@NonNull E element, @NonNull Class<?> target) {
        Class<?> t = target;
        if (t == List          .class) return List.of(element);
        if (t == Set           .class) return Set.of(element);
        if (t == Collection    .class) return List.of(element);
        if (t == SortedSet     .class) return new TreeSet<>(List.of(element));
        if (t == NavigableSet  .class) return new TreeSet<>(List.of(element));
        if (t == ArrayList     .class) return new ArrayList<>(List.of(element));
        if (t == LinkedList    .class) return new LinkedList<>(List.of(element));
        if (t == HashSet       .class) return new HashSet<>(List.of(element));
        if (t == LinkedHashSet .class) return new LinkedHashSet<>(List.of(element));
        if (t == TreeSet       .class) return new TreeSet<>(List.of(element));
        throw new IllegalArgumentException("Can't use " + target.getName() + " as a singleton collection.");
    }
}