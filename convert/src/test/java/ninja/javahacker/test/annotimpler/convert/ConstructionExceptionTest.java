package ninja.javahacker.test.annotimpler.convert;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class ConstructionExceptionTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    private static class Inner {
        private class Inner2 {
        }
    }

    @SuppressWarnings("unused")
    private static <E> void noop(
            Float a,
            Thread b,
            int c,
            List<String> d,
            List<List<Thread>> e,
            Map<List<String>, Set<Integer>> f,
            E g,
            List<E> h,
            List<?> i,
            List<? extends Number> j,
            E[] k,
            E[][] l,
            List<E[]> m,
            int[] n,
            Inner.Inner2 o)
    {
        throw new AssertionError();
    }

    private static final List<Type> TYPES = Stream
            .of(ConstructionExceptionTest.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("noop"))
            .map(m -> m.getParameters())
            .flatMap(Stream::of)
            .map(Parameter::getParameterizedType)
            .toList();

    private static final List<String> TYPE_NAMES = List.of(
            "Float", "Thread", "int", "List<String>", "List<List<Thread>>", "Map<List<String>, Set<Integer>>", "E", "List<E>", "List<?>",
            "List<? extends Number>", "E[]", "E[][]", "List<E[]>", "int[]", "Inner2"
    );

    private static final Map<Type, String> TYPE_MAP = new HashMap<>();

    static {
        for (var i = 0; i < TYPES.size(); i++) {
            TYPE_MAP.put(TYPES.get(i), TYPE_NAMES.get(i));
        }
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testConvertionException() {
        var xxx = new Exception();
        var yyy = new Exception();
        return Stream.of(
                n("2.in-null", () -> ForTests.testNull("in", () -> new ConvertionException(null, String.class))),
                n("2.out-null", () -> ForTests.testNull("out", () -> new ConvertionException(String.class, null))),
                n("3M.msg-null", () -> ForTests.testNull("message", () -> new ConvertionException((String) null, String.class, String.class))),
                n("3M.in-null", () -> ForTests.testNull("in", () -> new ConvertionException("foo", null, String.class))),
                n("3M.out-null", () -> ForTests.testNull("out", () -> new ConvertionException("foo", String.class, null))),
                n("3C.cause-null", () -> ForTests.testNull("cause", () -> new ConvertionException((Throwable) null, String.class, String.class))),
                n("3C.in-null", () -> ForTests.testNull("in", () -> new ConvertionException(new Exception(), null, String.class))),
                n("3C.out-null", () -> ForTests.testNull("out", () -> new ConvertionException(new Exception(), String.class, null))),
                n("4.msg-null", () -> ForTests.testNull("message", () -> new ConvertionException(null, new Exception(), String.class, String.class))),
                n("4.cause-null", () -> ForTests.testNull("cause", () -> new ConvertionException("foo", null, String.class, String.class))),
                n("4.in-null", () -> ForTests.testNull("in", () -> new ConvertionException("foo", new Exception(), null, String.class))),
                n("4.out-null", () -> ForTests.testNull("out", () -> new ConvertionException("foo", new Exception(), String.class, null))),

                n("2.msg-ok1", () -> Assertions.assertEquals("Can't read value as Float.", new ConvertionException(String.class, Float.class).getMessage())),
                n("2.msg-ok2", () -> Assertions.assertEquals("Can't read value as Thread.", new ConvertionException(Test.class, Thread.class).getMessage())),
                n("2.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException(String.class, Float.class).getIn())),
                n("2.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException(Test.class, Thread.class).getIn())),
                n("2.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException(String.class, Float.class).getOut())),
                n("2.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException(Test.class, Thread.class).getOut())),

                n("3M.msg-ok1", () -> Assertions.assertEquals("foo", new ConvertionException("foo", String.class, Float.class).getMessage())),
                n("3M.msg-ok2", () -> Assertions.assertEquals("goo", new ConvertionException("goo", Test.class, Thread.class).getMessage())),
                n("3M.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException("foo", String.class, Float.class).getIn())),
                n("3M.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException("foo", Test.class, Thread.class).getIn())),
                n("3M.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException("foo", String.class, Float.class).getOut())),
                n("3M.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException("foo", Test.class, Thread.class).getOut())),

                n("3C.msg-ok1", () -> Assertions.assertEquals("Can't read value as Float.", new ConvertionException(new Exception(), String.class, Float.class).getMessage())),
                n("3C.msg-ok2", () -> Assertions.assertEquals("Can't read value as Thread.", new ConvertionException(new Exception(), Test.class, Thread.class).getMessage())),
                n("3C.cause-ok1", () -> Assertions.assertSame(xxx, new ConvertionException(xxx, String.class, Float.class).getCause())),
                n("3C.cause-ok2", () -> Assertions.assertSame(yyy, new ConvertionException(yyy, Test.class, Thread.class).getCause())),
                n("3C.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException(new Exception(), String.class, Float.class).getIn())),
                n("3C.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException(new Exception(), Test.class, Thread.class).getIn())),
                n("3C.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException(new Exception(), String.class, Float.class).getOut())),
                n("3C.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException(new Exception(), Test.class, Thread.class).getOut())),

                n("4.msg-ok1", () -> Assertions.assertEquals("foo", new ConvertionException("foo", new Exception(), String.class, Float.class).getMessage())),
                n("4.msg-ok2", () -> Assertions.assertEquals("goo", new ConvertionException("goo", new Exception(), Test.class, Thread.class).getMessage())),
                n("4.cause-ok1", () -> Assertions.assertSame(xxx, new ConvertionException("foo", xxx, String.class, Float.class).getCause())),
                n("4.cause-ok2", () -> Assertions.assertSame(yyy, new ConvertionException("goo", yyy, Test.class, Thread.class).getCause())),
                n("4.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException("foo", new Exception(), String.class, Float.class).getIn())),
                n("4.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException("foo", new Exception(), Test.class, Thread.class).getIn())),
                n("4.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException("foo", new Exception(), String.class, Float.class).getOut())),
                n("4.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException("foo", new Exception(), Test.class, Thread.class).getOut()))
        );
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testConvertionExceptionOut() {
        return TYPES.stream().map(t -> n("" + t, () -> Assertions.assertEquals(t, new ConvertionException(String.class, t).getOut())));
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testConvertionExceptionTypeNames() {
        return TYPES.stream().map(t -> n("" + t, () -> Assertions.assertEquals("Can't read value as " + TYPE_MAP.get(t) + ".", new ConvertionException(String.class, t).getMessage())));
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testUnavailableConverterException() {
        var xxx = new Exception();
        var yyy = new Exception();
        return Stream.of(
                n("2.msg-null", () -> ForTests.testNull("message", () -> new UnavailableConverterException(null, String.class))),
                n("2.root-null", () -> ForTests.testNull("root", () -> new UnavailableConverterException("foo", null))),
                n("3.msg-null", () -> ForTests.testNull("message", () -> new UnavailableConverterException(null, new Exception(), String.class))),
                n("3.cause-null", () -> ForTests.testNull("cause", () -> new UnavailableConverterException("foo", null, String.class))),
                n("3.root-null", () -> ForTests.testNull("root", () -> new UnavailableConverterException("foo", new Exception(), null))),

                n("2.msg-ok1", () -> Assertions.assertEquals("foo", new UnavailableConverterException("foo", String.class).getMessage())),
                n("2.msg-ok2", () -> Assertions.assertEquals("goo", new UnavailableConverterException("goo", Test.class).getMessage())),
                n("2.root-ok1", () -> Assertions.assertEquals(String.class, new UnavailableConverterException("foo", String.class).getRoot())),
                n("2.root-ok2", () -> Assertions.assertEquals(Test.class, new UnavailableConverterException("foo", Test.class).getRoot())),

                n("3.msg-ok1", () -> Assertions.assertEquals("foo", new UnavailableConverterException("foo", new Exception(), String.class).getMessage())),
                n("3.msg-ok2", () -> Assertions.assertEquals("goo", new UnavailableConverterException("goo", new Exception(), Test.class).getMessage())),
                n("3.cause-ok1", () -> Assertions.assertSame(xxx, new UnavailableConverterException("foo", xxx, String.class).getCause())),
                n("3.cause-ok2", () -> Assertions.assertSame(yyy, new UnavailableConverterException("goo", yyy, Test.class).getCause())),
                n("3.root-ok1", () -> Assertions.assertEquals(String.class, new UnavailableConverterException("foo", new Exception(), String.class).getRoot())),
                n("3.root-ok2", () -> Assertions.assertEquals(Test.class, new UnavailableConverterException("foo", new Exception(), Test.class).getRoot()))
        );
    }
}
