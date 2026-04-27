package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

@SuppressWarnings("unused")
public class InvalidConverterTest {

    public static interface Pointless<X> extends List<X> {}

    @SuppressWarnings("rawtypes")
    private static <E> void noop1(
            Map v1,
            Map<String, String> v2,
            Thread v3,
            Runtime v4,
            List v5,
            E v6,
            List<List<String>> list1,
            List<E> list2,
            List<? extends String> list3,
            List<E[]> list4,
            List<?> list5,
            Set<List<String>> set1,
            Set<E> set2,
            Set<? extends String> set3,
            Set<E[]> set4,
            Set<?> set5,
            Collection<List<String>> coll1,
            Collection<E> coll2,
            Collection<? extends String> coll3,
            Collection<E[]> coll4,
            Collection<?> coll5,
            Optional<List<String>> opt1,
            Optional<E> opt2,
            Optional<? extends String> opt3,
            Optional<E[]> opt4,
            Optional<?> opt5,
            List<String>[] arr1,
            E[] arr2,
            Pointless<String> p) {
        throw new AssertionError();
    }

    @SuppressWarnings("rawtypes")
    private static void noop2(List<Thread> v1, Thread[] v2, Collection<Runtime> v3, Optional<Exception> v4, List<List> v5) {
        throw new AssertionError();
    }

    @SuppressWarnings("rawtypes")
    private static void noop3(Map v4, Thread v6, Runtime v7, List v8) {
        throw new AssertionError();
    }

    @Test
    public void testMultidimensionalArrayUnavailable() throws Exception {
        var ex1 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(int[][].class));
        Assertions.assertEquals(int[][].class, ex1.getRoot());
        Assertions.assertEquals("No converter for multidimensional arrays.", ex1.getMessage());
    }

    @TestFactory
    public Stream<DynamicTest> testBadTypes() throws Exception {
        return Stream.of(InvalidConverterTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("noop1"))
                .map(Method::getParameters)
                .flatMap(Stream::of)
                .map(Parameter::getParameterizedType)
                .map(t -> DynamicTest.dynamicTest("[testBadTypes] " + t.getTypeName(), () -> {
                    var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(t));
                    Assertions.assertEquals(t, ex2.getRoot());
                    Assertions.assertEquals("No converter for " + t.getTypeName() + ".", ex2.getMessage());
                }));
    }

    @TestFactory
    public Stream<DynamicTest> testBadInnerTypes() throws Exception {
        return Stream.of(InvalidConverterTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("noop2"))
                .map(Method::getParameters)
                .flatMap(Stream::of)
                .map(Parameter::getParameterizedType)
                .map(t -> DynamicTest.dynamicTest("[testBadTypes] " + t.getTypeName(), () -> {
                    var base = t instanceof ParameterizedType p ? p.getActualTypeArguments()[0] : ((Class<?>) t).getComponentType();
                    var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(t));
                    Assertions.assertEquals(base, ex2.getRoot());
                    Assertions.assertEquals("No converter for " + base.getTypeName() + ".", ex2.getMessage());
                }));
    }

    @TestFactory
    public Stream<DynamicTest> testBadClasses() throws Exception {
        return Stream.of(InvalidConverterTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("noop3"))
                .map(Method::getParameters)
                .flatMap(Stream::of)
                .map(Parameter::getType)
                .map(t -> DynamicTest.dynamicTest("[testBadClasses] " + t.getTypeName(), () -> {
                    var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get((Class<?>) t));
                    Assertions.assertEquals(t, ex2.getRoot());
                    Assertions.assertEquals("No converter for " + t.getTypeName() + ".", ex2.getMessage());
                }));
    }

    public static record Recursive(Recursive r) {}

    @Test
    public void testRecursiveRecord() {
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(Recursive.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Recursive.class, ex.getRoot()),
                () -> Assertions.assertEquals("Recursive record class: " + Recursive.class.getName(), ex.getMessage())
        );
    }

    public static record Verbose(String bla, String blu) {}

    @Test
    public void testVerboseRecord() {
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.STD.get(Verbose.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Verbose.class, ex.getRoot()),
                () -> Assertions.assertEquals("Non-single value record class where single-valued was expected: " + Verbose.class.getName(), ex.getMessage())
        );
    }

    @TestFactory
    public DynamicNode testInvalidFromObj() throws Exception {
        return new HeavyConverterTestSupport().testInBad("[testInvalidFromObj]", TestTypes.CVT_CLASSES_WITH_ARRAYS);
    }
}
