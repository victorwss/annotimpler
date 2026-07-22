package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class ToStringEqualsHashCodeTest {

    private static final List<Converter<?>> SINGLETONS = List.of(
            BigDecimalConverter.INSTANCE,
            BigIntegerConverter.INSTANCE,
            ByteArrayConverter.INSTANCE,
            CalendarConverter.INSTANCE,
            CharArrayConverter.INSTANCE,
            DateConverter.INSTANCE,
            GregorianCalendarConverter.INSTANCE,
            InstantConverter.INSTANCE,
            LocalDateConverter.INSTANCE,
            LocalDateTimeConverter.INSTANCE,
            LocalTimeConverter.INSTANCE,
            OffsetDateTimeConverter.INSTANCE,
            OffsetTimeConverter.INSTANCE,
            OptionalDoubleConverter.INSTANCE,
            OptionalIntConverter.INSTANCE,
            OptionalLongConverter.INSTANCE,
            RefConverter.INSTANCE,
            RowIdConverter.INSTANCE,
            SqlDateConverter.INSTANCE,
            SqlTimeConverter.INSTANCE,
            SqlTimestampConverter.INSTANCE,
            StringConverter.INSTANCE,
            StructConverter.INSTANCE,
            ZonedDateTimeConverter.INSTANCE
    );

    private static final List<Converter<?>> WRAPPERS = List.of(
            BooleanConverter.WRAPPER, ByteConverter.WRAPPER, CharacterConverter.WRAPPER, DoubleConverter.WRAPPER,
            FloatConverter.WRAPPER, IntegerConverter.WRAPPER, LongConverter.WRAPPER, ShortConverter.WRAPPER
    );

    private static final List<Converter<?>> PRIMITIVES = List.of(
            BooleanConverter.PRIMITIVE, ByteConverter.PRIMITIVE, CharacterConverter.PRIMITIVE, DoubleConverter.PRIMITIVE,
            FloatConverter.PRIMITIVE, IntegerConverter.PRIMITIVE, LongConverter.PRIMITIVE, ShortConverter.PRIMITIVE
    );

    private static ParameterizedType params(int i) {
        return (ParameterizedType) Stream
            .of(ToStringEqualsHashCodeTest.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("foo"))
            .findFirst()
            .get()
            .getParameters()[i]
            .getParameterizedType();
    }

    private static void foo(List<String> a, List<Integer> b, Set<String> c, Set<Integer> d, Collection<String> e, Collection<Integer> f, Optional<String> g, Optional<Integer> h) {
        throw new AssertionError();
    }

    private static final ParameterizedType LIST_STRING = params(0);

    private static final ParameterizedType LIST_INTEGER = params(1);

    private static final ParameterizedType SET_STRING = params(2);

    private static final ParameterizedType SET_INTEGER = params(3);

    private static final ParameterizedType COLLECTION_STRING = params(4);

    private static final ParameterizedType COLLECTION_INTEGER = params(5);

    private static final ParameterizedType OPTIONAL_STRING = params(6);

    private static final ParameterizedType OPTIONAL_INTEGER = params(7);

    @TestFactory
    public Stream<DynamicNode> testSingletonsToString() throws Exception {
        var a = SINGLETONS
                .stream()
                .map(s -> DynamicTest.dynamicTest(
                        "[testSingletonsToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals("[" + s.getClass().getSimpleName() + "]", s.toString())
                ));
        var b = WRAPPERS
                .stream()
                .map(s -> DynamicTest.dynamicTest(
                        "[testSingletonsToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals("[" + s.getClass().getSimpleName() + "-WRAPPER]", s.toString())
                ));
        var c = PRIMITIVES
                .stream()
                .map(s -> DynamicTest.dynamicTest(
                        "[testSingletonsToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals("[" + s.getClass().getSimpleName() + "-PRIMITIVE]", s.toString())
                ));
        return Stream.of(a, b, c).flatMap(s -> s);
    }

    @TestFactory
    public Stream<DynamicNode> testSingletonness() throws Exception {
        return Stream.of(SINGLETONS, WRAPPERS, PRIMITIVES)
                .flatMap(List::stream)
                .map(s -> DynamicTest.dynamicTest(
                        "[testSingletonness] " + s.toString(),
                        () -> Assertions.assertTrue(s instanceof Enum<?>)
                ));
    }

    public static enum Color {
        RED, GREEN, YELLOW, BLUE;
    }

    public static enum Flavor {
        SALTY, SWEET, SOUR, BITTER;
    }

    public static enum Season {
        SUMMER, AUTUMN, WINTER, SPRING;
    }

    public static record Blah(Color a) {
    }

    public static record Bleh(Integer b) {
    }

    private static final ConverterFactory SPECIAL = t -> {
        var x = StdConverterFactory.INSTANCE.get(t);
        if (x == IntegerConverter.WRAPPER) return IntegerConverter.PRIMITIVE;
        return x;
    };

    private static final ConverterFactory EMPTY_WRAP = t -> StdConverterFactory.INSTANCE.get(t);

    @TestFactory
    public Stream<DynamicNode> testEnumConverterToString() throws Exception {
        var a = new EnumConverter<>(Color.class);
        var b = new EnumConverter<>(Flavor.class);
        return Stream.of(a, b)
                .map(s -> DynamicTest.dynamicTest(
                        "[testEnumConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals("EnumConverter[" + s.getType().getName() + "]", s.toString())
                ));
    }

    @TestFactory
    public Stream<DynamicNode> testRecordConverterToString() throws Exception {
        var a = new RecordConverter<>(StdConverterFactory.INSTANCE, Bleh.class);
        var b = new RecordConverter<>(StdConverterFactory.INSTANCE, Blah.class);
        var c = new RecordConverter<>(SPECIAL, Blah.class);
        var i1 = IntegerConverter.WRAPPER;
        var i2 = new EnumConverter<>(Color.class);

        return Stream.of(a, b, c)
                .map(s -> DynamicTest.dynamicTest(
                        "[testRecordConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals(
                                "RecordConverter[recordClass=" + s.getType().getName() + ", cvt=" + (s == a ? i1 : i2) + "]",
                                s.toString()
                        )
                ));
    }

    @TestFactory
    public Stream<DynamicNode> testArrayConverterToString() throws Exception {
        var a = new ArrayConverter<>(StdConverterFactory.INSTANCE, String.class);
        var b = new ArrayConverter<>(StdConverterFactory.INSTANCE, Integer.class);
        var c = new ArrayConverter<>(EMPTY_WRAP, Integer.class);
        var i1 = StringConverter.INSTANCE;
        var i2 = IntegerConverter.WRAPPER;
        return Stream.of(a, b, c)
                .map(s -> DynamicTest.dynamicTest(
                        "[testArrayConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals(
                                "ArrayConverter[baseType=" + s.getType().getComponentType().getName() + ", cvt=" + (s == a ? i1 : i2) + "]",
                                s.toString()
                        )
                ));
    }

    @TestFactory
    public Stream<DynamicNode> testListConverterToString() throws Exception {
        var a = new ListConverter<>(StdConverterFactory.INSTANCE, LIST_STRING);
        var b = new ListConverter<>(StdConverterFactory.INSTANCE, LIST_INTEGER);
        var c = new ListConverter<>(EMPTY_WRAP, LIST_INTEGER);
        var i1 = StringConverter.INSTANCE;
        var i2 = IntegerConverter.WRAPPER;
        return Stream.of(a, b, c)
                .map(s -> DynamicTest.dynamicTest(
                        "[testListConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals(
                                "ListConverter[baseType=" + TypeName.of(s.getType()) + ", cvt=" + (s == a ? i1 : i2) + "]",
                                s.toString()
                        )
                ));
    }

    @TestFactory
    public Stream<DynamicNode> testCollectionConverterToString() throws Exception {
        var a = new CollectionConverter<>(StdConverterFactory.INSTANCE, COLLECTION_STRING);
        var b = new CollectionConverter<>(StdConverterFactory.INSTANCE, COLLECTION_INTEGER);
        var c = new CollectionConverter<>(EMPTY_WRAP, COLLECTION_INTEGER);
        var i1 = StringConverter.INSTANCE;
        var i2 = IntegerConverter.WRAPPER;
        return Stream.of(a, b, c)
                .map(s -> DynamicTest.dynamicTest(
                        "[testCollectionConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals(
                                "CollectionConverter[baseType=" + TypeName.of(s.getType()) + ", cvt=" + (s == a ? i1 : i2) + "]",
                                s.toString()
                        )
                ));
    }

    @TestFactory
    public Stream<DynamicNode> testSetConverterToString() throws Exception {
        var a = new SetConverter<>(StdConverterFactory.INSTANCE, SET_STRING);
        var b = new SetConverter<>(StdConverterFactory.INSTANCE, SET_INTEGER);
        var c = new SetConverter<>(EMPTY_WRAP, SET_INTEGER);
        var i1 = StringConverter.INSTANCE;
        var i2 = IntegerConverter.WRAPPER;
        return Stream.of(a, b, c)
                .map(s -> DynamicTest.dynamicTest(
                        "[testSetConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals(
                                "SetConverter[baseType=" + TypeName.of(s.getType()) + ", cvt=" + (s == a ? i1 : i2) + "]",
                                s.toString()
                        )
                ));
    }

    @TestFactory
    public Stream<DynamicNode> testOptionalConverterToString() throws Exception {
        var a = new OptionalConverter<>(StdConverterFactory.INSTANCE, OPTIONAL_STRING);
        var b = new OptionalConverter<>(StdConverterFactory.INSTANCE, OPTIONAL_INTEGER);
        var c = new OptionalConverter<>(EMPTY_WRAP, OPTIONAL_INTEGER);
        var i1 = StringConverter.INSTANCE;
        var i2 = IntegerConverter.WRAPPER;
        return Stream.of(a, b, c)
                .map(s -> DynamicTest.dynamicTest(
                        "[testOptionalConverterToString] " + s.getClass().getSimpleName(),
                        () -> Assertions.assertEquals(
                                "OptionalConverter[baseType=" + TypeName.of(s.getType()) + ", cvt=" + (s == a ? i1 : i2) + "]",
                                s.toString()
                        )
                ));
    }

    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    private Stream<DynamicNode> testConvertersEqualsHashCode(
            String name, Converter<?> a1, Converter<?> b1, Converter<?> c1, Converter<?> a2, Converter<?> b2, Converter<?> c2)
            throws Exception
    {
        var e1a = DynamicTest.dynamicTest(name + " 1a", () -> Assertions.assertTrue(a1.equals(a2)));
        var e1b = DynamicTest.dynamicTest(name + " 1b", () -> Assertions.assertTrue(a2.equals(a1)));
        var e1c = DynamicTest.dynamicTest(name + " 1c", () -> Assertions.assertTrue(a1.equals(a1)));
        var e1d = DynamicTest.dynamicTest(name + " 1d", () -> Assertions.assertEquals(a1.hashCode(), a2.hashCode()));

        var e2a = DynamicTest.dynamicTest(name + " 2a", () -> Assertions.assertTrue(b1.equals(b2)));
        var e2b = DynamicTest.dynamicTest(name + " 2b", () -> Assertions.assertTrue(b2.equals(b1)));
        var e2c = DynamicTest.dynamicTest(name + " 2c", () -> Assertions.assertTrue(b1.equals(b1)));
        var e2d = DynamicTest.dynamicTest(name + " 2d", () -> Assertions.assertEquals(b1.hashCode(), b2.hashCode()));

        var e3a = DynamicTest.dynamicTest(name + " 3a", () -> Assertions.assertTrue(c1.equals(c2)));
        var e3b = DynamicTest.dynamicTest(name + " 3b", () -> Assertions.assertTrue(c2.equals(c1)));
        var e3c = DynamicTest.dynamicTest(name + " 3c", () -> Assertions.assertTrue(c1.equals(c1)));
        var e3d = DynamicTest.dynamicTest(name + " 3d", () -> Assertions.assertEquals(c1.hashCode(), c2.hashCode()));

        var e4a = DynamicTest.dynamicTest(name + " 4a", () -> Assertions.assertFalse(a1.equals(b1)));
        var e4b = DynamicTest.dynamicTest(name + " 4b", () -> Assertions.assertFalse(a1.equals(c1)));
        var e4c = DynamicTest.dynamicTest(name + " 4c", () -> Assertions.assertFalse(b1.equals(c1)));

        var e5a = DynamicTest.dynamicTest(name + " 5a", () -> Assertions.assertFalse(a1.equals(null)));
        var e5b = DynamicTest.dynamicTest(name + " 5b", () -> Assertions.assertFalse(b1.equals(null)));
        var e5c = DynamicTest.dynamicTest(name + " 5c", () -> Assertions.assertFalse(c1.equals(null)));
        var e5d = DynamicTest.dynamicTest(name + " 5d", () -> Assertions.assertFalse(a1.equals("x")));

        return Stream.of(e1a, e1b, e1c, e1d, e2a, e2b, e2c, e2d, e3a, e3b, e3c, e3d, e4a, e4b, e4c, e5a, e5b, e5c, e5d);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testEnumConverterEqualsHashCode() throws Exception {
        var a1 = new EnumConverter<>(Flavor.class);
        var b1 = new EnumConverter<>(Color.class);
        var c1 = new EnumConverter<>(Season.class);
        var a2 = new EnumConverter<>(Flavor.class);
        var b2 = new EnumConverter<>(Color.class);
        var c2 = new EnumConverter<>(Season.class);
        return testConvertersEqualsHashCode("[testEnumConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testRecordConverterEqualsHashCode() throws Exception {
        var a1 = new RecordConverter<>(StdConverterFactory.INSTANCE, Blah.class);
        var b1 = new RecordConverter<>(StdConverterFactory.INSTANCE, Bleh.class);
        var c1 = new RecordConverter<>(SPECIAL, Bleh.class);
        var a2 = new RecordConverter<>(StdConverterFactory.INSTANCE, Blah.class);
        var b2 = new RecordConverter<>(StdConverterFactory.INSTANCE, Bleh.class);
        var c2 = new RecordConverter<>(SPECIAL, Bleh.class);
        return testConvertersEqualsHashCode("[testRecordConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testArrayConverterEqualsHashCode() throws Exception {
        var a1 = new ArrayConverter<>(StdConverterFactory.INSTANCE, String.class);
        var b1 = new ArrayConverter<>(StdConverterFactory.INSTANCE, Integer.class);
        var c1 = new ArrayConverter<>(SPECIAL, Integer.class);
        var a2 = new ArrayConverter<>(StdConverterFactory.INSTANCE, String.class);
        var b2 = new ArrayConverter<>(StdConverterFactory.INSTANCE, Integer.class);
        var c2 = new ArrayConverter<>(SPECIAL, Integer.class);
        return testConvertersEqualsHashCode("[testArrayConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testListConverterEqualsHashCode() throws Exception {
        var a1 = new ListConverter<>(StdConverterFactory.INSTANCE, LIST_STRING);
        var b1 = new ListConverter<>(StdConverterFactory.INSTANCE, LIST_INTEGER);
        var c1 = new ListConverter<>(SPECIAL, LIST_INTEGER);
        var a2 = new ListConverter<>(StdConverterFactory.INSTANCE, LIST_STRING);
        var b2 = new ListConverter<>(StdConverterFactory.INSTANCE, LIST_INTEGER);
        var c2 = new ListConverter<>(SPECIAL, LIST_INTEGER);
        return testConvertersEqualsHashCode("[testListConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testSetConverterEqualsHashCode() throws Exception {
        var a1 = new SetConverter<>(StdConverterFactory.INSTANCE, SET_STRING);
        var b1 = new SetConverter<>(StdConverterFactory.INSTANCE, SET_INTEGER);
        var c1 = new SetConverter<>(SPECIAL, SET_INTEGER);
        var a2 = new SetConverter<>(StdConverterFactory.INSTANCE, SET_STRING);
        var b2 = new SetConverter<>(StdConverterFactory.INSTANCE, SET_INTEGER);
        var c2 = new SetConverter<>(SPECIAL, SET_INTEGER);
        return testConvertersEqualsHashCode("[testSetConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testCollectionConverterEqualsHashCode() throws Exception {
        var a1 = new CollectionConverter<>(StdConverterFactory.INSTANCE, COLLECTION_STRING);
        var b1 = new CollectionConverter<>(StdConverterFactory.INSTANCE, COLLECTION_INTEGER);
        var c1 = new CollectionConverter<>(SPECIAL, COLLECTION_INTEGER);
        var a2 = new CollectionConverter<>(StdConverterFactory.INSTANCE, COLLECTION_STRING);
        var b2 = new CollectionConverter<>(StdConverterFactory.INSTANCE, COLLECTION_INTEGER);
        var c2 = new CollectionConverter<>(SPECIAL, COLLECTION_INTEGER);
        return testConvertersEqualsHashCode("[testCollectionConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicNode> testOptionalConverterEqualsHashCode() throws Exception {
        var a1 = new OptionalConverter<>(StdConverterFactory.INSTANCE, OPTIONAL_STRING);
        var b1 = new OptionalConverter<>(StdConverterFactory.INSTANCE, OPTIONAL_INTEGER);
        var c1 = new OptionalConverter<>(SPECIAL, OPTIONAL_INTEGER);
        var a2 = new OptionalConverter<>(StdConverterFactory.INSTANCE, OPTIONAL_STRING);
        var b2 = new OptionalConverter<>(StdConverterFactory.INSTANCE, OPTIONAL_INTEGER);
        var c2 = new OptionalConverter<>(SPECIAL, OPTIONAL_INTEGER);
        return testConvertersEqualsHashCode("[testOptionalConverterEqualsHashCode]", a1, b1, c1, a2, b2, c2);
    }
}
