package ninja.javahacker.test.annotimpler.convert;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class HeavyConverterTestSupport {

    private static final List<?> BADS = List.of(
            Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN
    );

    private static final List<Class<?>> NUMERIC = List.of(
            byte.class, short.class, int.class, long.class, float.class, double.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigDecimal.class, BigInteger.class
    );

    private static final Map<Class<?>, Class<?>> TEMPORAL_BACK = Map.of(
            java.util.Date.class, Instant.class,
            Calendar.class, ZonedDateTime.class,
            GregorianCalendar.class, ZonedDateTime.class,
            java.sql.Date.class, LocalDate.class,
            java.sql.Time.class, LocalTime.class,
            java.sql.Timestamp.class, LocalDateTime.class,
            OptionalInt.class, Integer.class,
            OptionalLong.class, Long.class,
            OptionalDouble.class, Double.class
    );

    public static interface MethodSpec<E> {
        public Optional<?> receive(Converter<?> cvt, E in) throws Exception;
    }

    public static interface MethodSpecNull {
        public void receive(Converter<?> cvt) throws Exception;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public record Elements<E>(Class<E> k, List<E> data) {
        public <X> Elements<X> map(Class<X> k2, Function<E, X> f) {
            return new Elements<>(k2, data.stream().map(x -> x == null ? null : f.apply(x)).toList());
        }
    }

    public <E> Elements<E> e(Class<E> k, List<E> data) {
        return new Elements<>(k, data);
    }

    public Long lo(BigInteger x) {
        if (x == null) return null;
        var max = BigInteger.valueOf(Long.MAX_VALUE);
        var min = BigInteger.valueOf(Long.MIN_VALUE);
        return x.compareTo(min) < 0 || x.compareTo(max) > 0 ? null : x.longValue();
    }

    public Integer i(Long x) {
        return x == null || x < Integer.MIN_VALUE || x > Integer.MAX_VALUE ? null : x.intValue();
    }

    public Character c(Long x) {
        return x == null || x < Character.MIN_VALUE || x > Character.MAX_VALUE ? null : (char) x.intValue();
    }

    public Short s(Long x) {
        return x == null || x < Short.MIN_VALUE || x > Short.MAX_VALUE ? null : x.shortValue();
    }

    public Byte b(Long x) {
        return x == null || x < Byte.MIN_VALUE || x > Byte.MAX_VALUE ? null : x.byteValue();
    }

    public BigInteger bi(String x) {
        return x == null || List.of("q", "xxx", "RED", "NaN", "Infinity", "-Infinity").contains(x) || x.contains(".") ? null : new BigInteger(x);
    }

    public BigDecimal bd(String x) {
        return x == null || List.of("q", "xxx", "RED", "NaN", "Infinity", "-Infinity").contains(x) ? null : new BigDecimal(x);
    }

    public OffsetDateTime odt(String x) {
        return x == null || x.contains("xxx") ? null : OffsetDateTime.parse(x, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][ xxxxx]").withResolverStyle(ResolverStyle.STRICT));
    }

    private static <E> MethodSpec<E> spec(Class<E> k) {
        if (k == boolean       .class) return (cvt, in) -> cvt.from((boolean       ) in);
        if (k == byte          .class) return (cvt, in) -> cvt.from((byte          ) in);
        if (k == short         .class) return (cvt, in) -> cvt.from((short         ) in);
        if (k == int           .class) return (cvt, in) -> cvt.from((int           ) in);
        if (k == long          .class) return (cvt, in) -> cvt.from((long          ) in);
        if (k == float         .class) return (cvt, in) -> cvt.from((float         ) in);
        if (k == double        .class) return (cvt, in) -> cvt.from((double        ) in);
        if (k == BigDecimal    .class) return (cvt, in) -> cvt.from((BigDecimal    ) in);
        if (k == String        .class) return (cvt, in) -> cvt.from((String        ) in);
        if (k == byte[]        .class) return (cvt, in) -> cvt.from((byte[]        ) in);
        if (k == LocalDate     .class) return (cvt, in) -> cvt.from((LocalDate     ) in);
        if (k == LocalDateTime .class) return (cvt, in) -> cvt.from((LocalDateTime ) in);
        if (k == LocalTime     .class) return (cvt, in) -> cvt.from((LocalTime     ) in);
        if (k == OffsetDateTime.class) return (cvt, in) -> cvt.from((OffsetDateTime) in);
        if (k == OffsetTime    .class) return (cvt, in) -> cvt.from((OffsetTime    ) in);
        if (k == Blob          .class) return (cvt, in) -> cvt.from((Blob          ) in);
        if (k == Clob          .class) return (cvt, in) -> cvt.from((Clob          ) in);
        if (k == NClob         .class) return (cvt, in) -> cvt.from((NClob         ) in);
        if (k == Ref           .class) return (cvt, in) -> cvt.from((Ref           ) in);
        if (k == RowId         .class) return (cvt, in) -> cvt.from((RowId         ) in);
        if (k == Struct        .class) return (cvt, in) -> cvt.from((Struct        ) in);
        if (k == SQLXML        .class) return (cvt, in) -> cvt.from((SQLXML        ) in);
        if (k == java.sql.Array.class) return (cvt, in) -> cvt.from((java.sql.Array) in);
        throw new AssertionError();
    }

    private enum ExceptionType {
        CONVERTION, UNSUPPORTED_VALUE, UNSUPPORTED_TYPE;
    }

    @SuppressWarnings({"element-type-mismatch", "AssertEqualsBetweenInconvertibleTypes"})
    private static void checkException(ConvertionException ce, ExceptionType support, Type inputType, Type target, Object in) {
        var parts = checkExceptionIn(ce, support, inputType, target, in);
        Assertions.assertAll(parts);
    }

    @SuppressWarnings({"element-type-mismatch", "AssertEqualsBetweenInconvertibleTypes"})
    private static List<Executable> checkExceptionIn(ConvertionException ce, ExceptionType support, Type inputType, Type target, Object in) {
        var e1a = "Can't read value as " + TypeName.of(target) + ".";
        var e2 = "Unsupported " + name(inputType) + ".";
        var e3 = "Unsupported Type: " + inputType.getTypeName() + ".";
        var errStr = Map.of(ExceptionType.CONVERTION, e1a, ExceptionType.UNSUPPORTED_VALUE, e2, ExceptionType.UNSUPPORTED_TYPE, e3).get(support);

        var n = 1;
        for (Throwable k = ce; k.getCause() != null; k = k.getCause()) {
            n++;
        }
        if (n > 4) throw new AssertionError();

        List<Executable> parts = new ArrayList<>(20);
        parts.add(() -> Assertions.assertEquals(errStr, ce.getMessage()));
        parts.add(() -> Assertions.assertEquals(inputType, ce.getIn()));
        parts.add(() -> Assertions.assertEquals(target, ce.getOut()));
        if (n == 1) return parts;
        var next = ce.getCause();

        if (target instanceof ParameterizedType p && List.of(List.class, Set.class, Collection.class, Optional.class).contains(p.getRawType())) {
            parts.addAll(checkExceptionIn((ConvertionException) next, support, inputType, p.getActualTypeArguments()[0], in));
            return parts;
        }

        var targetClass = (Class<?>) target;

        if (targetClass.isRecord()) {
            var t2 = targetClass == TestTypes.R4RecordDeeper.class
                    ? TestTypes.VERY_SPECIAL
                    : targetClass.getRecordComponents()[0].getGenericType();
            parts.addAll(checkExceptionIn((ConvertionException) next, support, inputType, t2, in));
            return parts;
        }

        if (targetClass.isArray()) {
            parts.addAll(checkExceptionIn((ConvertionException) next, support, inputType, targetClass.getComponentType(), in));
            return parts;
        }

        if (n != 2 && n != 3) throw new AssertionError(n + " - " + TypeName.of(targetClass));

        var isDerived = TEMPORAL_BACK.containsKey(target);
        if (isDerived) {
            parts.addAll(checkExceptionIn((ConvertionException) next, support, inputType, TEMPORAL_BACK.get(target), in));
            return parts;
        }

        if (targetClass.isEnum()) {
            var badl = in instanceof Long   v && (v > Integer.MAX_VALUE);
            var badf = in instanceof Float  v && (v > Integer.MAX_VALUE || v % 1F != 0F);
            var badd = in instanceof Double v && (v > Integer.MAX_VALUE || v % 1D != 0D);
            if (n == 3 || badl || badf || badd || BADS.contains(in)) {
                parts.add(() -> Assertions.assertEquals(ConvertionException.class, next.getClass()));
                parts.add(() -> Assertions.assertEquals("Can't read value as int.", next.getMessage()));
                parts.add(() -> Assertions.assertEquals(inputType, ((ConvertionException) next).getIn()));
                parts.add(() -> Assertions.assertEquals(int.class, ((ConvertionException) next).getOut()));
            } else {
                var ex = List.of(String.class, Blob.class, Clob.class, NClob.class).contains(inputType)
                        ? IllegalArgumentException.class
                        : ArrayIndexOutOfBoundsException.class;
                parts.add(() -> Assertions.assertEquals(ex, next.getClass()));
            }
            if (n == 3) {
                var last = next.getCause();
                parts.add(() -> Assertions.assertEquals(ArithmeticException.class, last.getClass()));
            }
            return parts;
        }

        if (n != 2) throw new AssertionError(n + " - " + TypeName.of(targetClass));

        if (NUMERIC.contains(targetClass)) {
            var ex = inputType == String.class ? NumberFormatException.class : ArithmeticException.class;
            parts.add(() -> Assertions.assertEquals(ex, next.getClass()));
        } else {
            parts.add(() -> Assertions.assertEquals(ConvertionException.class, next.getClass()));
            parts.add(() -> Assertions.assertEquals(errStr, next.getMessage()));
            parts.add(() -> Assertions.assertEquals(inputType, ((ConvertionException) next).getIn()));
            parts.add(() -> Assertions.assertEquals(targetClass, ((ConvertionException) next).getOut()));
        }

        return parts;
    }

    private static String name(Type t) {
        return TypeName.of(t, Set.of(java.sql.Date.class, java.util.Date.class));
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public <E> DynamicNode testIn(
            String prefix,
            List<? extends Class<?>> typesForConverters,
            Elements<E> inputs,
            List<? extends Elements<?>> lists)
            throws Exception
    {
        var inputType = inputs.k();
        var m = spec(inputType);
        var nb = name(inputType);
        Map<Class<?>, List<?>> mappings = new HashMap<>(lists.size());
        for (var a1 : lists) {
            mappings.put(a1.k(), a1.data());
        }

        List<DynamicNode> nodes1 = new ArrayList<>(typesForConverters.size());

        for (var k1 : typesForConverters) {
            if (k1 == null) throw new AssertionError();

            var v1 = mappings.get(WrapperClass.unwrap(k1));
            List<DynamicNode> nodes2 = new ArrayList<>(12 * inputs.data().size());

            for (var i = 0; i < inputs.data().size(); i++) {
                var in = inputs.data().get(i);
                if (in == null) continue;
                var out = v1 == null || i >= v1.size() ? null : v1.get(i);

                var inStr = in instanceof Blob ? ""
                        : in instanceof NClob ? ""
                        : in instanceof Clob ? ""
                        : in instanceof SQLXML ? ""
                        : in instanceof RowId ? ""
                        : in instanceof byte[] x ? " - (byte[]) " + new String(x)
                        : in instanceof char[] x ? " - (char[]) " + new String(x)
                        : in instanceof Object[] ? ""
                        : " - " + in;

                var k2all = TestTypes.others(k1);
                for (var k2 : k2all) {
                    if (k2 == byte[].class && k1 != k2) continue;
                    if (k2 == char[].class && k1 != k2) continue;
                    var o2 = TestTypes.wrap(out, k2);
                    var unsupported = v1 == null && (inputType != String.class || TestTypes.SPECIALS.contains(k1));
                    var ok = !unsupported && out != null;
                    var res = ok ? " - should be ok." : unsupported ? " - should be unsupported." : " - should throw ConvertionException.";
                    var errorType = unsupported ? ExceptionType.UNSUPPORTED_VALUE : ExceptionType.CONVERTION;

                    Executable nd1Ok = () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        TestTypes.compare(o2, m.receive(cvt, in).get());
                    };
                    Executable nd2Ok = () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        TestTypes.compare(o2, cvt.fromObj(in).get());
                    };
                    Executable nd1Err = () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> m.receive(cvt, in));
                        checkException(ce, errorType, inputType, k2, in);
                    };
                    Executable nd2Err = () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.fromObj(in));
                        checkException(ce, errorType, inputType, k2, in);
                    };

                    nodes2.add(DynamicTest.dynamicTest(prefix + " Converter for " + name(k2) + " from "    + nb + inStr + res, ok ? nd1Ok : nd1Err));
                    nodes2.add(DynamicTest.dynamicTest(prefix + " Converter for " + name(k2) + " fromObj " + nb + inStr + res, ok ? nd2Ok : nd2Err));
                }
                if (k2all.isEmpty()) {
                    var nf = DynamicTest.dynamicTest(prefix + " No types found for " + name(k1) + " from " + nb + ".", () -> { throw new AssertionError(); });
                    nodes2.add(nf);
                }
            }
            if (nodes2.isEmpty()) {
                var nf = DynamicTest.dynamicTest(prefix + " No tests for " + name(k1) + " from " + nb + ".", () -> { throw new AssertionError(); });
                nodes2.add(nf);
            }
            nodes1.add(DynamicContainer.dynamicContainer(prefix + " Test convertions for " + name(k1) + " from " + nb + ".", nodes2));
        }
        return DynamicContainer.dynamicContainer(prefix + " Test convertions from " + nb + ".", nodes1);
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public <E> DynamicNode testInBad(
            String prefix,
            List<? extends Class<?>> typesForConverters)
            throws Exception
    {
        class Foo {
        }

        enum Bar {
            SPLOINK, BLEVERS;
        }

        record Pointless(int foo) {
        }

        List<DynamicNode> nodes1 = new ArrayList<>(typesForConverters.size());

        var inputs = List.of(Thread.currentThread(), Runtime.getRuntime(), new Foo(), Bar.SPLOINK, new Pointless(5));
        for (var k1 : typesForConverters) {
            if (k1 == null) throw new AssertionError();

            List<DynamicNode> nodes2 = new ArrayList<>(12 * inputs.size());
            for (var in : inputs) {

                var inputType = in.getClass();
                var inStr = inputType.getSimpleName();
                var k2all = TestTypes.others(k1);
                for (var k2 : k2all) {
                    var res = " - should be unsupported.";

                    var nd2 = DynamicTest.dynamicTest(prefix + " Converter for " + name(k2) + " fromObj " + inStr + res, () -> {
                        var cvt = ConverterFactory.STD.get(k2);
                        var ce = Assertions.assertThrows(ConvertionException.class, () -> cvt.fromObj(in));
                        checkException(ce, ExceptionType.UNSUPPORTED_TYPE, inputType, k2, in);
                    });
                    nodes2.add(nd2);
                }
            }
            nodes1.add(DynamicContainer.dynamicContainer(prefix + " Test convertions for " + name(k1) + " from invalid.", nodes2));
        }
        return DynamicContainer.dynamicContainer(prefix + " Test convertions from invalid.", nodes1);
    }
}
