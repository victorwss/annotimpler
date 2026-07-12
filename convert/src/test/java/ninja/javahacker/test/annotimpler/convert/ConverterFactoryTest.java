package ninja.javahacker.test.annotimpler.convert;

import lombok.NonNull;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class ConverterFactoryTest {

    public static record Blah(int x) {
    }

    private static final TypeVariable<?> TV;
    private static final GenericArrayType GA;
    private static final ParameterizedType PT;
    private static final WildcardType WT;
    private static final Type UT;
    private static final ParameterizedType COL;
    private static final ParameterizedType SET;
    private static final ParameterizedType LST;
    private static final ParameterizedType OPT;
    private static final ParameterizedType MAP;
    private static final ParameterizedType PTWTF;
    private static final ParameterizedType COLX;
    private static final ParameterizedType SETX;
    private static final ParameterizedType LSTX;
    private static final ParameterizedType OPTX;

    static {
        var ps = Stream.of(ConverterFactoryTest.class.getDeclaredMethods())
                .filter(x -> x.getName().equals("noop"))
                .flatMap(x -> Stream.of(x.getParameters()))
                .map(Parameter::getParameterizedType)
                .toList();

        TV = (TypeVariable<?>) ps.get(0);
        GA = (GenericArrayType) ps.get(1);
        PT = (ParameterizedType) ps.get(2);
        WT = (WildcardType) PT.getActualTypeArguments()[0];
        COL = (ParameterizedType) ps.get(3);
        SET = (ParameterizedType) ps.get(4);
        LST = (ParameterizedType) ps.get(5);
        OPT = (ParameterizedType) ps.get(6);
        MAP = (ParameterizedType) ps.get(7);
        PTWTF = (ParameterizedType) ps.get(8);
        COLX = (ParameterizedType) ps.get(9);
        SETX = (ParameterizedType) ps.get(10);
        LSTX = (ParameterizedType) ps.get(11);
        OPTX = (ParameterizedType) ps.get(12);

        UT = new Type() {
            @Override
            public String toString() {
                return "xxx";
            }
        };
    }

    public static Set<Class<?>> BASICS = Set.of(
            boolean.class, byte.class, char.class     , short.class, int    .class, long.class, float.class, double.class,
            Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigInteger.class, BigDecimal.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
            char[].class, byte[].class, String.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetDateTime.class, OffsetTime.class, ZonedDateTime.class, Instant.class,
            Calendar.class, GregorianCalendar.class, java.util.Date.class,
            java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class,
            RowId.class, Struct.class, Ref.class
    );

    @SuppressWarnings("unused")
    private static <E> void noop(
            E a,
            E[] b,
            List<?> c,
            Collection<Blah> d,
            Set<Blah> e,
            List<Blah> f,
            Optional<Blah> g,
            Map<Blah, String> h,
            Supplier<Thread> i,
            Collection<E> j,
            Set<E> k,
            List<E> l,
            Optional<E> m)
    {
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testGetOfOverride() throws Exception {
        record Foo(int x) {}

        var cvtCl = new Converter<Foo>() {};
        var cvtTv = new Converter<Foo>() {};
        var cvtGa = new Converter<Foo>() {};
        var cvtPt = new Converter<Foo>() {};
        var cvtWt = new Converter<Foo>() {};
        var cvtUt = new Converter<Foo>() {};

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <E> Converter<E> getOf(@NonNull Class<E> t) throws UnavailableConverterException {
                Assertions.assertEquals(Foo.class, t);
                return (Converter<E>) cvtCl;
            }

            @NonNull
            @Override
            public Converter<Foo> getOf(@NonNull TypeVariable<?> t) throws UnavailableConverterException {
                Assertions.assertEquals(TV, t);
                return cvtTv;
            }

            @NonNull
            @Override
            public Converter<Foo> getOf(@NonNull GenericArrayType t) throws UnavailableConverterException {
                Assertions.assertEquals(GA, t);
                return cvtGa;
            }

            @NonNull
            @Override
            public Converter<Foo> getOf(@NonNull ParameterizedType t) throws UnavailableConverterException {
                Assertions.assertEquals(PT, t);
                return cvtPt;
            }

            @NonNull
            @Override
            public Converter<Foo> getOf(@NonNull WildcardType t) throws UnavailableConverterException {
                Assertions.assertEquals(WT, t);
                return cvtWt;
            }

            @NonNull
            @Override
            public Converter<Foo> getOfUndetermined(@NonNull Type t) throws UnavailableConverterException {
                Assertions.assertEquals(UT, t);
                return cvtUt;
            }
        };

        return Stream.of(
            DynamicTest.dynamicTest("[testGetOfOverride] Class"            , () -> Assertions.assertEquals(cvtCl, cvtf.get(Foo.class))),
            DynamicTest.dynamicTest("[testGetOfOverride] TypeVariable"     , () -> Assertions.assertEquals(cvtTv, cvtf.get(TV))),
            DynamicTest.dynamicTest("[testGetOfOverride] ParameterizedType", () -> Assertions.assertEquals(cvtPt, cvtf.get(PT))),
            DynamicTest.dynamicTest("[testGetOfOverride] GenericArrayType" , () -> Assertions.assertEquals(cvtGa, cvtf.get(GA))),
            DynamicTest.dynamicTest("[testGetOfOverride] WildcardType"     , () -> Assertions.assertEquals(cvtWt, cvtf.get(WT))),
            DynamicTest.dynamicTest("[testGetOfOverride] UndeterminedType" , () -> Assertions.assertEquals(cvtUt, cvtf.get(UT)))
        );
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked"})
    public Stream<DynamicTest> testClassOverrides() throws Exception {
        record Foo(int x) {
        }

        enum Bar {
            BAR;
        }

        record Goo(int x) {
        }

        var cvtAr = new Converter<Foo[]>() {};
        var cvtEn = new Converter<Bar>() {};
        var cvtRc = new Converter<Foo>() {};
        var cvtDm = new Converter<Goo>() {};

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            public <E> Optional<Converter<E>> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
                if (Foo[].class != klass) return Optional.empty();
                return Optional.of((Converter<E>) cvtAr);
            }

            @NonNull
            @Override
            public <E extends Enum<E>> Optional<Converter<E>> makeEnum(@NonNull Class<E> klass) throws UnavailableConverterException {
                if (Bar.class != klass) return Optional.empty();
                return Optional.of((Converter<E>) cvtEn);
            }

            @NonNull
            @Override
            public <E extends Record> Optional<Converter<E>> makeRecord(@NonNull Class<E> klass) throws UnavailableConverterException {
                if (Foo.class != klass) return Optional.empty();
                return Optional.of((Converter<E>) cvtRc);
            }

            @NonNull
            @Override
            public <E> Optional<Converter<E>> directMapping(@NonNull Class<E> klass) throws UnavailableConverterException {
                if (Goo.class != klass) return Optional.empty();
                return Optional.of((Converter<E>) cvtDm);
            }
        };

        return Stream.of(
            DynamicTest.dynamicTest("[testClassOverrides] Array" , () -> Assertions.assertEquals(cvtAr, cvtf.get(Foo[].class))),
            DynamicTest.dynamicTest("[testClassOverrides] Enum  ", () -> Assertions.assertEquals(cvtEn, cvtf.get(Bar.class))),
            DynamicTest.dynamicTest("[testClassOverrides] Record", () -> Assertions.assertEquals(cvtRc, cvtf.get(Foo.class))),
            DynamicTest.dynamicTest("[testClassOverrides] Direct", () -> Assertions.assertEquals(cvtDm, cvtf.get(Goo.class)))
        );
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked"})
    public Stream<DynamicTest> testParameterizedTypeOverrides() throws Exception {
        var ps = Stream.of(ConverterFactoryTest.class.getDeclaredMethods())
                .filter(x -> x.getName().equals("noop"))
                .flatMap(x -> Stream.of(x.getParameters()))
                .map(Parameter::getParameterizedType)
                .toList();

        var cvtCol = new Converter<Collection<Blah>>() {};
        var cvtSet = new Converter<Set<Blah>>() {};
        var cvtLst = new Converter<List<Blah>>() {};
        var cvtOpt = new Converter<Optional<Blah>>() {};
        var cvtMap = new Converter<Map<Blah, String>>() {};

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            public Optional<? extends Converter<? extends Collection<?>>> makeCollection(@NonNull ParameterizedType p) throws UnavailableConverterException {
                if (COL != p) return Optional.empty();
                return Optional.of(cvtCol);
            }

            @NonNull
            @Override
            public Optional<? extends Converter<? extends Set<?>>> makeSet(@NonNull ParameterizedType p) throws UnavailableConverterException {
                if (SET != p) return Optional.empty();
                return Optional.of(cvtSet);
            }

            @NonNull
            @Override
            public Optional<? extends Converter<? extends List<?>>> makeList(@NonNull ParameterizedType p) throws UnavailableConverterException {
                if (LST != p) return Optional.empty();
                return Optional.of(cvtLst);
            }

            @NonNull
            @Override
            public Optional<? extends Converter<? extends Optional<?>>> makeOptional(@NonNull ParameterizedType p) throws UnavailableConverterException {
                if (OPT != p) return Optional.empty();
                return Optional.of(cvtOpt);
            }

            @NonNull
            @Override
            public Optional<? extends Converter<? extends Map<?, ?>>> makeMap(@NonNull ParameterizedType p) throws UnavailableConverterException {
                if (MAP != p) return Optional.empty();
                return Optional.of(cvtMap);
            }
        };

        return Stream.of(
            DynamicTest.dynamicTest("[testParameterizedTypeOverrides] Collection", () -> Assertions.assertEquals(cvtCol, cvtf.get(COL))),
            DynamicTest.dynamicTest("[testParameterizedTypeOverrides] Set"       , () -> Assertions.assertEquals(cvtSet, cvtf.get(SET))),
            DynamicTest.dynamicTest("[testParameterizedTypeOverrides] List"      , () -> Assertions.assertEquals(cvtLst, cvtf.get(LST))),
            DynamicTest.dynamicTest("[testParameterizedTypeOverrides] Optional"  , () -> Assertions.assertEquals(cvtOpt, cvtf.get(OPT))),
            DynamicTest.dynamicTest("[testParameterizedTypeOverrides] Map"       , () -> Assertions.assertEquals(cvtMap, cvtf.get(MAP)))
        );
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked", "null"})
    public Stream<DynamicTest> testNulls() throws Exception {
        var std = new StdConverterFactory() {};
        return Stream.of(
                DynamicTest.dynamicTest("[testNulls] get"                     , () -> ForTests.testNull("t"          , () -> std.get(null))),
                DynamicTest.dynamicTest("[testNulls] getOf(Class)"            , () -> ForTests.testNull("klass"      , () -> std.getOf((Class<?>) null))),
                DynamicTest.dynamicTest("[testNulls] getOf(ParameterizedType)", () -> ForTests.testNull("t"          , () -> std.getOf((ParameterizedType) null))),
                DynamicTest.dynamicTest("[testNulls] getOf(WildcardType)"     , () -> ForTests.testNull("t"          , () -> std.getOf((WildcardType) null))),
                DynamicTest.dynamicTest("[testNulls] getOf(GenericArrayType)" , () -> ForTests.testNull("t"          , () -> std.getOf((GenericArrayType) null))),
                DynamicTest.dynamicTest("[testNulls] getOf(TypeVariable)"     , () -> ForTests.testNull("t"          , () -> std.getOf((TypeVariable<?>) null))),
                DynamicTest.dynamicTest("[testNulls] getOfUndetermined"       , () -> ForTests.testNull("t"          , () -> std.getOfUndetermined(null))),
                DynamicTest.dynamicTest("[testNulls] makeArray"               , () -> ForTests.testNull("klass"      , () -> std.makeArray(null))),
                DynamicTest.dynamicTest("[testNulls] makeEnum"                , () -> ForTests.testNull("klass"      , () -> std.makeEnum(null))),
                DynamicTest.dynamicTest("[testNulls] makeRecord"              , () -> ForTests.testNull("klass"      , () -> std.makeRecord(null))),
                DynamicTest.dynamicTest("[testNulls] directMapping"           , () -> ForTests.testNull("klass"      , () -> std.directMapping(null))),
                DynamicTest.dynamicTest("[testNulls] makeCollection"          , () -> ForTests.testNull("p"          , () -> std.makeCollection(null))),
                DynamicTest.dynamicTest("[testNulls] makeSet"                 , () -> ForTests.testNull("p"          , () -> std.makeSet(null))),
                DynamicTest.dynamicTest("[testNulls] makeList"                , () -> ForTests.testNull("p"          , () -> std.makeList(null))),
                DynamicTest.dynamicTest("[testNulls] makeOptional"            , () -> ForTests.testNull("p"          , () -> std.makeOptional(null))),
                DynamicTest.dynamicTest("[testNulls] makeMap"                 , () -> ForTests.testNull("p"          , () -> std.makeMap(null))),
                DynamicTest.dynamicTest("[testNulls] extend-klass"            , () -> ForTests.testNull("klass"      , () -> std.extend(null, new Converter<String>() {}))),
                DynamicTest.dynamicTest("[testNulls] extend-cvt"              , () -> ForTests.testNull("cvt"        , () -> std.extend(String.class, null))),
                DynamicTest.dynamicTest("[testNulls] mapToRecord-map"         , () -> ForTests.testNull("map"        , () -> std.mapToRecord(null, Blah.class))),
                DynamicTest.dynamicTest("[testNulls] mapToRecord-recordClass" , () -> ForTests.testNull("recordClass", () -> std.mapToRecord(new HashMap<>(1), null)))
        );
    }

    private static DynamicTest testException(String testName, Executable e, Type root, String name) {
        return DynamicTest.dynamicTest(testName, () -> {
            var x = Assertions.assertThrows(UnavailableConverterException.class, e);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(root, x.getRoot()),
                    () -> Assertions.assertEquals("No converter for " + name + ".", x.getMessage())
            );
        });
    }

    private static DynamicTest testException2(String testName, Executable e, Type root) {
        return DynamicTest.dynamicTest(testName, () -> {
            var x = Assertions.assertThrows(UnavailableConverterException.class, e);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(root, x.getRoot()),
                    () -> Assertions.assertEquals("No converter for multidimensional arrays.", x.getMessage())
            );
        });
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked", "null"})
    public Stream<DynamicTest> testStdExceptions() throws Exception {
        class Ugh {}
        var std = new StdConverterFactory() {};
        return Stream.of(
                testException2("[testStdExceptions] getOf(Class)[][]"        , () -> std.getOf(int[][].class), int[][].class),
                testException ("[testStdExceptions] getOf(Class)"            , () -> std.getOf(Ugh.class), Ugh.class, Ugh.class.getName()),
                testException ("[testStdExceptions] getOf(TypeVariable)"     , () -> std.getOf(TV), TV, TV.getName()),
                testException ("[testStdExceptions] getOf(GenericArrayType)" , () -> std.getOf(GA), GA, GA.getTypeName()),
                testException ("[testStdExceptions] getOf(WildcardType)"     , () -> std.getOf(WT), WT, WT.getTypeName()),
                testException ("[testStdExceptions] getOf(ParameterizedType)", () -> std.getOf(PTWTF), PTWTF, PTWTF.getTypeName()),
                testException ("[testStdExceptions] getOfUndetermined"       , () -> std.getOfUndetermined(UT), UT, "xxx"),
                testException ("[testStdExceptions] get(Undetermined)"       , () -> std.get(UT), UT, "xxx")
        );
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked", "null"})
    public Stream<DynamicTest> testEmptyConverterCreation() throws Exception {
        class Ugh {}
        var std = new StdConverterFactory() {};
        return Stream.of(
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeArray not array"     , () -> Assertions.assertTrue(std.makeArray     (        Ugh.class).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeArray multdim"       , () -> Assertions.assertTrue(std.makeArray     (int[][]    .class).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeCollection wrong raw", () -> Assertions.assertTrue(std.makeCollection(PTWTF            ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeCollection wrong arg", () -> Assertions.assertTrue(std.makeCollection(COLX             ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeSet wrong raw"       , () -> Assertions.assertTrue(std.makeSet       (PTWTF            ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeSet wrong arg"       , () -> Assertions.assertTrue(std.makeSet       (SETX             ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeList wrong raw"      , () -> Assertions.assertTrue(std.makeList      (PTWTF            ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeList wrong arg"      , () -> Assertions.assertTrue(std.makeList      (LSTX             ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeMap"                 , () -> Assertions.assertTrue(std.makeMap       (PTWTF            ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeOptional wrong raw"  , () -> Assertions.assertTrue(std.makeOptional  (PTWTF            ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeOptional wrong arg"  , () -> Assertions.assertTrue(std.makeOptional  (OPTX             ).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeEnum wrong"          , () -> Assertions.assertTrue(std.makeEnum      ((Class) Ugh.class).isEmpty())),
                DynamicTest.dynamicTest("[testEmptyConverterCreation] makeRecord wrong"        , () -> Assertions.assertTrue(std.makeRecord    ((Class) Ugh.class).isEmpty()))
        );
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked", "null"})
    public Stream<DynamicTest> testDirectMappings() throws Exception {
        class Ugh {}
        var a1 = StdConverterFactory.rootMap();
        var a2 = StdConverterFactory.rootMap();
        var b1 = StdConverterFactory.INSTANCE.directMappings();
        var b2 = StdConverterFactory.INSTANCE.directMappings();
        var copy = Map.copyOf(a1);

        return Stream.of(
                DynamicTest.dynamicTest("[testDirectMappings] public fields", () -> Assertions.assertSame(StdConverterFactory.INSTANCE, ConverterFactory.std())),
                DynamicTest.dynamicTest("[testDirectMappings] root map 1", () -> Assertions.assertSame(a1, a2)),
                DynamicTest.dynamicTest("[testDirectMappings] root map 2", () -> Assertions.assertSame(b1, b2)),
                DynamicTest.dynamicTest("[testDirectMappings] root map 3", () -> Assertions.assertSame(a1, b1)),
                DynamicTest.dynamicTest("[testDirectMappings] root map keys", () -> Assertions.assertEquals(BASICS, a1.keySet())),
                DynamicTest.dynamicTest(
                        "[testDirectMappings] root map unmodifiable to add",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> a1.put(Ugh.class, new Converter<Ugh>() {}));
                            Assertions.assertEquals(copy, a1);
                        }
                ),
                DynamicTest.dynamicTest(
                        "[testDirectMappings] root map unmodifiable to clear",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> a1.clear());
                            Assertions.assertEquals(copy, a1);
                        }
                ),
                DynamicTest.dynamicTest(
                        "[testDirectMappings] root map unmodifiable to remove",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> a1.remove(int.class));
                            Assertions.assertEquals(copy, a1);
                        }
                ),
                DynamicTest.dynamicTest(
                        "[testDirectMappings] root map unmodifiable to replace",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> a1.put(int.class, new Converter<Integer>() {}));
                            Assertions.assertEquals(copy, a1);
                        }
                )
        );
    }

    @TestFactory
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "unchecked", "null"})
    public Stream<DynamicTest> testExtends() throws Exception {
        class Ugh {}
        class Vuh {}
        var root = StdConverterFactory.rootMap();
        var cvt1 = new Converter<Ugh>() {};
        var cvt2 = new Converter<Vuh>() {};
        var extend1 = StdConverterFactory.INSTANCE.extend(Ugh.class, cvt1);
        var extended = extend1.extend(Vuh.class, cvt2);
        var maps = extended.directMappings();
        var copy = Map.copyOf(maps);
        Map<Class<?>, Converter<?>> temp = new HashMap<>(maps);
        temp.put(Ugh.class, cvt1);
        temp.put(Vuh.class, cvt2);
        var newMap = Map.copyOf(temp);

        return Stream.of(
                DynamicTest.dynamicTest("[testExtends] not INSTANCE 1", () -> Assertions.assertNotEquals(StdConverterFactory.INSTANCE, extend1)),
                DynamicTest.dynamicTest("[testExtends] not STD 1", () -> Assertions.assertNotEquals(ConverterFactory.std(), extend1)),
                DynamicTest.dynamicTest("[testExtends] not INSTANCE 2", () -> Assertions.assertNotEquals(StdConverterFactory.INSTANCE, extended)),
                DynamicTest.dynamicTest("[testExtends] not STD 2", () -> Assertions.assertNotEquals(ConverterFactory.std(), extended)),
                DynamicTest.dynamicTest("[testExtends] 1 is not 2", () -> Assertions.assertNotEquals(extend1, extended)),
                DynamicTest.dynamicTest("[testExtends] not root", () -> Assertions.assertNotEquals(root, maps)),
                DynamicTest.dynamicTest("[testExtends] map structure", () -> Assertions.assertEquals(newMap, maps)),
                DynamicTest.dynamicTest("[testExtends] root map repeat", () -> Assertions.assertSame(maps, extended.directMappings())),
                DynamicTest.dynamicTest(
                        "[testExtends] unmodifiable to add",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> maps.put(Ugh.class, new Converter<Ugh>() {}));
                            Assertions.assertEquals(copy, maps);
                        }
                ),
                DynamicTest.dynamicTest(
                        "[testExtends] unmodifiable to clear",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> maps.clear());
                            Assertions.assertEquals(copy, maps);
                        }
                ),
                DynamicTest.dynamicTest(
                        "[testExtends] unmodifiable to remove",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> maps.remove(int.class));
                            Assertions.assertEquals(copy, maps);
                        }
                ),
                DynamicTest.dynamicTest(
                        "[testExtends] unmodifiable to replace",
                        () -> {
                            Assertions.assertThrows(UnsupportedOperationException.class, () -> maps.put(int.class, new Converter<Integer>() {}));
                            Assertions.assertEquals(copy, maps);
                        }
                ),
                DynamicTest.dynamicTest("[testExtends] works 1", () -> Assertions.assertSame(cvt1, extended.get(Ugh.class))),
                DynamicTest.dynamicTest("[testExtends] works 2", () -> Assertions.assertSame(cvt2, extended.get(Vuh.class))),
                DynamicTest.dynamicTest("[testExtends] works 3", () -> Assertions.assertSame(StdConverterFactory.INSTANCE.get(LocalDate.class), extended.get(LocalDate.class)))
        );
    }

    @Test
    public void testNonStd() throws Exception {
        var a = new Converter<String>() {};
        var b = new Converter<Integer>() {};
        ConverterFactory nonStd = t -> {
            if (t == String.class) return a;
            if (t == Integer.class) return b;
            throw new AssertionError();
        };
        Assertions.assertAll(
                () -> Assertions.assertSame(a, nonStd.getOf(String.class)),
                () -> Assertions.assertSame(b, nonStd.getOf(Integer.class))
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testNonStdWithNull() throws Exception {
        ConverterFactory nonStd = t -> {
            throw new AssertionError();
        };
        ForTests.testNull("klass", () -> nonStd.getOf(null));
    }
}
