package ninja.javahacker.test.annotimpler.convert;

import org.junit.jupiter.api.function.Executable;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

@SuppressWarnings("unused")
public class InvalidConverterTest {

    public static interface Pointless<X> extends List<X> {}

    public static record Foo(int x) {
    }

    public static enum Bar {
    }

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

    private static void noop4(List<String> v1, Set<String> v2, Collection<String> v3, Optional<String> v4, List<List<String>> v5, Set<List<String>> v6, Collection<List<String>> v7, Optional<List<String>> v8) {
        throw new AssertionError();
    }

    @Test
    public void testMultidimensionalArrayUnavailable() throws Exception {
        var ex1 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get(int[][].class));
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
                    var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get(t));
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
                    var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get(t));
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
                    var ex2 = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get((Class<?>) t));
                    Assertions.assertEquals(t, ex2.getRoot());
                    Assertions.assertEquals("No converter for " + t.getTypeName() + ".", ex2.getMessage());
                }));
    }

    public static record Recursive(Recursive r) {}

    @Test
    public void testRecursiveRecord() {
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get(Recursive.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Recursive.class, ex.getRoot()),
                () -> Assertions.assertEquals("Recursive record class: " + Recursive.class.getName(), ex.getMessage())
        );
    }

    public static record Verbose(String bla, String blu) {}

    @Test
    public void testVerboseRecord() {
        var ex = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get(Verbose.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Verbose.class, ex.getRoot()),
                () -> Assertions.assertEquals("Non-single value record class where single-valued was expected: " + Verbose.class.getName(), ex.getMessage())
        );
    }

    @TestFactory
    public DynamicNode testInvalidFromObj() throws Exception {
        return new HeavyConverterTestSupport().testInBad("[testInvalidFromObj]", TestTypes.CVT_CLASSES_WITH_ARRAYS);
    }

    private void testBadType(Class<?> expectedRaw, ParameterizedType pt, Executable exec) {
        var msg = "The baseType must be " + (expectedRaw == Optional.class ? "an " : "a ") + expectedRaw.getSimpleName() + " of some class.";
        var ex = Assertions.assertThrows(UnavailableConverterException.class, exec);
        Assertions.assertAll(
                () -> Assertions.assertEquals(msg, ex.getMessage()),
                () -> Assertions.assertEquals(pt.getActualTypeArguments()[0], ex.getRoot())
        );
    }

    @TestFactory
    public List<DynamicTest> testBadInstantiations() throws Exception {
        var pt = Stream.of(InvalidConverterTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("noop4"))
                .map(Method::getParameters)
                .flatMap(Stream::of)
                .map(Parameter::getParameterizedType)
                .map(t -> (ParameterizedType) t)
                .toList();

        return List.of(
                DynamicTest.dynamicTest("[testBadInstantiations] RecordConverter-cvtf"        , () -> ForTests.testNull("cvtf"   , () -> new RecordConverter    <>(null, Foo.class))),
                DynamicTest.dynamicTest("[testBadInstantiations] ArrayConverter-factory"      , () -> ForTests.testNull("factory", () -> new ArrayConverter     <>(null, String.class))),
                DynamicTest.dynamicTest("[testBadInstantiations] ListConverter-factory"       , () -> ForTests.testNull("factory", () -> new ListConverter      <>(null, pt.get(0)))),
                DynamicTest.dynamicTest("[testBadInstantiations] SetConverter-factory"        , () -> ForTests.testNull("factory", () -> new SetConverter       <>(null, pt.get(1)))),
                DynamicTest.dynamicTest("[testBadInstantiations] CollectionConverter-factory" , () -> ForTests.testNull("factory", () -> new CollectionConverter<>(null, pt.get(2)))),
                DynamicTest.dynamicTest("[testBadInstantiations] OptionalConverter-factory"   , () -> ForTests.testNull("factory", () -> new OptionalConverter  <>(null, pt.get(3)))),

                DynamicTest.dynamicTest("[testBadInstantiations] EnumConverter-enumClass"     , () -> ForTests.testNull("enumClass"  , () -> new EnumConverter      <Bar   >(null))),
                DynamicTest.dynamicTest("[testBadInstantiations] RecordConverter-recordClass" , () -> ForTests.testNull("recordClass", () -> new RecordConverter    <Foo   >(ConverterFactory.std(), null))),
                DynamicTest.dynamicTest("[testBadInstantiations] ArrayConverter-baseClass"    , () -> ForTests.testNull("baseClass"  , () -> new ArrayConverter     <Object>(ConverterFactory.std(), null))),
                DynamicTest.dynamicTest("[testBadInstantiations] ListConverter-baseType"      , () -> ForTests.testNull("baseType"   , () -> new ListConverter      <Object>(ConverterFactory.std(), null))),
                DynamicTest.dynamicTest("[testBadInstantiations] SetConverter-baseType"       , () -> ForTests.testNull("baseType"   , () -> new SetConverter       <Object>(ConverterFactory.std(), null))),
                DynamicTest.dynamicTest("[testBadInstantiations] CollectionConverter-baseType", () -> ForTests.testNull("baseType"   , () -> new CollectionConverter<Object>(ConverterFactory.std(), null))),
                DynamicTest.dynamicTest("[testBadInstantiations] OptionalConverter-baseType"  , () -> ForTests.testNull("baseType"   , () -> new OptionalConverter  <Object>(ConverterFactory.std(), null))),

                DynamicTest.dynamicTest("[testBadInstantiations] ListConverter-bad-raw"       , () -> testBadType(List      .class, pt.get(1), () -> new ListConverter      <>(ConverterFactory.std(), pt.get(1)))),
                DynamicTest.dynamicTest("[testBadInstantiations] SetConverter-bad-raw"        , () -> testBadType(Set       .class, pt.get(2), () -> new SetConverter       <>(ConverterFactory.std(), pt.get(2)))),
                DynamicTest.dynamicTest("[testBadInstantiations] CollectionConverter-bad-raw" , () -> testBadType(Collection.class, pt.get(3), () -> new CollectionConverter<>(ConverterFactory.std(), pt.get(3)))),
                DynamicTest.dynamicTest("[testBadInstantiations] OptionalConverter-bad-raw"   , () -> testBadType(Optional  .class, pt.get(0), () -> new OptionalConverter  <>(ConverterFactory.std(), pt.get(0)))),

                DynamicTest.dynamicTest("[testBadInstantiations] ListConverter-bad-arg"       , () -> testBadType(List      .class, pt.get(4), () -> new ListConverter      <>(ConverterFactory.std(), pt.get(4)))),
                DynamicTest.dynamicTest("[testBadInstantiations] SetConverter-bad-arg"        , () -> testBadType(Set       .class, pt.get(5), () -> new SetConverter       <>(ConverterFactory.std(), pt.get(5)))),
                DynamicTest.dynamicTest("[testBadInstantiations] CollectionConverter-bad-arg" , () -> testBadType(Collection.class, pt.get(6), () -> new CollectionConverter<>(ConverterFactory.std(), pt.get(6)))),
                DynamicTest.dynamicTest("[testBadInstantiations] OptionalConverter-bad-arg"   , () -> testBadType(Optional  .class, pt.get(7), () -> new OptionalConverter  <>(ConverterFactory.std(), pt.get(7))))
        );
    }
}
