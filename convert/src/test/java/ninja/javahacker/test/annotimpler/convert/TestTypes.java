package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

@Disabled
@SuppressWarnings("unused")
public class TestTypes {

    private TestTypes() {
        throw new AssertionError();
    }

    public static final List<Type> CVT_TYPES = Stream
            .of(TestTypes.class.getDeclaredMethods())
            .filter(m -> "noop".equals(m.getName()))
            .flatMap(m -> Stream.of(m.getParameters()))
            .map(p -> p.getParameterizedType())
            .toList();

    @SuppressWarnings("unchecked")
    public static final List<Class<?>> CVT_CLASSES = (List<Class<?>>) CVT_TYPES
            .stream()
            .filter(t -> t instanceof Class<?> tt && !tt.isArray())
            .map(t -> (Class<?>) t)
            .toList();

    public static final List<Class<?>> SPECIALS = List.of(Struct.class, RowId.class, Ref.class, java.sql.Array.class);

    private static void noop(
            boolean x1, byte x2, short x3, int     x4, long x5, float x6, double x7,
            Boolean y1, Byte y2, Short y3, Integer y4, Long y5, Float y6, Double y7,
            BigDecimal z1, BigInteger z2,
            OptionalInt w1, OptionalLong w2, OptionalDouble w3,
            Calendar v1, GregorianCalendar v2, java.util.Date v3, java.sql.Date v4, Time v5, java.sql.Timestamp v6,
            LocalDate u1, LocalDateTime u2, LocalTime u3, OffsetDateTime u4, OffsetTime u5, ZonedDateTime u6, Instant u7,
            Ref t1, RowId t2, Struct t3, java.sql.Array t4)
    {
        throw new AssertionError();
    }

    private static void noop(
            boolean[] x1, byte[] x2, short[] x3, int[]     x4, long[] x5, float[] x6, double[] x7,
            Boolean[] y1, Byte[] y2, Short[] y3, Integer[] y4, Long[] y5, Float[] y6, Double[] y7,
            BigDecimal[] z1, BigInteger[] z2,
            OptionalInt[] w1, OptionalLong[] w2, OptionalDouble[] w3,
            Calendar[] v1, GregorianCalendar[] v2, java.util.Date[] v3, java.sql.Date[] v4, Time[] v5, java.sql.Timestamp[] v6,
            LocalDate[] u1, LocalDateTime[] u2, LocalTime[] u3, OffsetDateTime[] u4, OffsetTime[] u5, ZonedDateTime[] u6, Instant[] u7,
            Ref[] t1, RowId[] t2, Struct[] t3, java.sql.Array[] t4)
    {
        throw new AssertionError();
    }

    private static void noop(
            Collection<Boolean> y1, Collection<Byte> y2, Collection<Short> y3, Collection<Integer> y4, Collection<Long> y5, Collection<Float> y6, Collection<Double> y7,
            Collection<BigDecimal> z1, //Collection<BigInteger> z2,
            Collection<OptionalInt> w1, //Collection<OptionalLong> w2, Collection<OptionalDouble> w3,
            //Collection<Calendar> v1, Collection<GregorianCalendar> v2,Collection< java.util.Date> v3, Collection<java.sql.Date> v4, Collection<Time> v5, Collection<java.sql.Timestamp> v6,
            Collection<LocalDate> u1, //Collection<LocalDateTime> u2, Collection<LocalTime> u3, Collection<OffsetDateTime> u4, Collection<OffsetTime> u5, Collection<ZonedDateTime> u6, Collection<Instant> u7,
            Collection<Ref> t1, Collection<RowId> t2, Collection<Struct> t3, Collection<java.sql.Array> t4)
    {
        throw new AssertionError();
    }

    private static void noop(
            List<Boolean> y1, List<Byte> y2, List<Short> y3, List<Integer> y4, List<Long> y5, List<Float> y6, List<Double> y7,
            List<BigDecimal> z1, //List<BigInteger> z2,
            List<OptionalInt> w1, //List<OptionalLong> w2, List<OptionalDouble> w3,
            //List<Calendar> v1, List<GregorianCalendar> v2,List< java.util.Date> v3, List<java.sql.Date> v4, List<Time> v5, List<java.sql.Timestamp> v6,
            List<LocalDate> u1, //List<LocalDateTime> u2, List<LocalTime> u3, List<OffsetDateTime> u4, List<OffsetTime> u5, List<ZonedDateTime> u6, List<Instant> u7,
            List<Ref> t1, List<RowId> t2, List<Struct> t3, List<java.sql.Array> t4)
    {
        throw new AssertionError();
    }

    private static void noop(
            Set<Boolean> y1, Set<Byte> y2, Set<Short> y3, Set<Integer> y4, Set<Long> y5, Set<Float> y6, Set<Double> y7,
            Set<BigDecimal> z1, //Set<BigInteger> z2,
            Set<OptionalInt> w1, //Set<OptionalLong> w2, Set<OptionalDouble> w3,
            //Set<Calendar> v1, Set<GregorianCalendar> v2,Set< java.util.Date> v3, Set<java.sql.Date> v4, Set<Time> v5, Set<java.sql.Timestamp> v6,
            Set<LocalDate> u1, //Set<LocalDateTime> u2, Set<LocalTime> u3, Set<OffsetDateTime> u4, Set<OffsetTime> u5, Set<ZonedDateTime> u6, Set<Instant> u7,
            Set<Ref> t1, Set<RowId> t2, Set<Struct> t3, Set<java.sql.Array> t4)
    {
        throw new AssertionError();
    }

    private static void noop(
            Optional<Boolean> y1, Optional<Byte> y2, Optional<Short> y3, Optional<Integer> y4, Optional<Long> y5, Optional<Float> y6, Optional<Double> y7,
            Optional<BigDecimal> z1, //Optional<BigInteger> z2,
            Optional<OptionalInt> w1, //Optional<OptionalLong> w2, Optional<OptionalDouble> w3,
            //Optional<Calendar> v1, Optional<GregorianCalendar> v2,Optional< java.util.Date> v3, Optional<java.sql.Date> v4, Optional<Time> v5, Optional<java.sql.Timestamp> v6,
            Optional<LocalDate> u1, //Optional<LocalDateTime> u2, Optional<LocalTime> u3, Optional<OffsetDateTime> u4, Optional<OffsetTime> u5, Optional<ZonedDateTime> u6, Optional<Instant> u7,
            Optional<Ref> t1, Optional<RowId> t2, Optional<Struct> t3, Optional<java.sql.Array> t4)
    {
        throw new AssertionError();
    }

    public static List<Type> others(Class<?> s) {
        return CVT_TYPES
                .stream()
                .filter(x -> (x instanceof ParameterizedType pt && pt.getActualTypeArguments()[0] == s) || (x instanceof Class<?> k && (k == s || (k.isArray() && k.getComponentType() == s))))
                .toList();
    }

    public static String name(Type t) {
        if (t instanceof Class<?> k) return k.getSimpleName();
        if (t instanceof ParameterizedType p) {
            var a = ((Class<?>) p.getRawType()).getSimpleName();
            var b = ((Class<?>) p.getActualTypeArguments()[0]).getSimpleName();
            return a + "<" + b + ">";
        }
        throw new AssertionError();
    }

}
