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

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testAll() {
        var xxx = new Exception();
        var yyy = new Exception();
        return Stream.of(
                n("ConvertionException-2.in-null", () -> ForTests.testNull("in", () -> new ConvertionException(null, String.class))),
                n("ConvertionException-2.out-null", () -> ForTests.testNull("out", () -> new ConvertionException(String.class, null))),
                n("ConvertionException-3M.msg-null", () -> ForTests.testNull("message", () -> new ConvertionException((String) null, String.class, String.class))),
                n("ConvertionException-3M.in-null", () -> ForTests.testNull("in", () -> new ConvertionException("foo", null, String.class))),
                n("ConvertionException-3M.out-null", () -> ForTests.testNull("out", () -> new ConvertionException("foo", String.class, null))),
                n("ConvertionException-3C.cause-null", () -> ForTests.testNull("cause", () -> new ConvertionException((Throwable) null, String.class, String.class))),
                n("ConvertionException-3C.in-null", () -> ForTests.testNull("in", () -> new ConvertionException(new Exception(), null, String.class))),
                n("ConvertionException-3C.out-null", () -> ForTests.testNull("out", () -> new ConvertionException(new Exception(), String.class, null))),
                n("ConvertionException-4.msg-null", () -> ForTests.testNull("message", () -> new ConvertionException(null, new Exception(), String.class, String.class))),
                n("ConvertionException-4.cause-null", () -> ForTests.testNull("cause", () -> new ConvertionException("foo", null, String.class, String.class))),
                n("ConvertionException-4.in-null", () -> ForTests.testNull("in", () -> new ConvertionException("foo", new Exception(), null, String.class))),
                n("ConvertionException-4.out-null", () -> ForTests.testNull("out", () -> new ConvertionException("foo", new Exception(), String.class, null))),
                n("ConvertionException-2.msg-ok1", () -> Assertions.assertEquals("Can't read value as Float.", new ConvertionException(String.class, Float.class).getMessage())),
                n("ConvertionException-2.msg-ok2", () -> Assertions.assertEquals("Can't read value as Thread.", new ConvertionException(Test.class, Thread.class).getMessage())),
                n("ConvertionException-2.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException(String.class, Float.class).getIn())),
                n("ConvertionException-2.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException(Test.class, Thread.class).getIn())),
                n("ConvertionException-2.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException(String.class, Float.class).getOut())),
                n("ConvertionException-2.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException(Test.class, Thread.class).getOut())),
                n("ConvertionException-3M.msg-ok1", () -> Assertions.assertEquals("foo", new ConvertionException("foo", String.class, Float.class).getMessage())),
                n("ConvertionException-3M.msg-ok2", () -> Assertions.assertEquals("goo", new ConvertionException("goo", Test.class, Thread.class).getMessage())),
                n("ConvertionException-3M.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException("foo", String.class, Float.class).getIn())),
                n("ConvertionException-3M.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException("foo", Test.class, Thread.class).getIn())),
                n("ConvertionException-3M.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException("foo", String.class, Float.class).getOut())),
                n("ConvertionException-3M.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException("foo", Test.class, Thread.class).getOut())),
                n("ConvertionException-3C.msg-ok1", () -> Assertions.assertEquals("Can't read value as Float.", new ConvertionException(new Exception(), String.class, Float.class).getMessage())),
                n("ConvertionException-3C.msg-ok2", () -> Assertions.assertEquals("Can't read value as Thread.", new ConvertionException(new Exception(), Test.class, Thread.class).getMessage())),
                n("ConvertionException-3C.cause-ok1", () -> Assertions.assertSame(xxx, new ConvertionException(xxx, String.class, Float.class).getCause())),
                n("ConvertionException-3C.cause-ok2", () -> Assertions.assertSame(yyy, new ConvertionException(yyy, Test.class, Thread.class).getCause())),
                n("ConvertionException-3C.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException(new Exception(), String.class, Float.class).getIn())),
                n("ConvertionException-3C.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException(new Exception(), Test.class, Thread.class).getIn())),
                n("ConvertionException-3C.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException(new Exception(), String.class, Float.class).getOut())),
                n("ConvertionException-3C.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException(new Exception(), Test.class, Thread.class).getOut())),
                n("ConvertionException-4.msg-ok1", () -> Assertions.assertEquals("foo", new ConvertionException("foo", new Exception(), String.class, Float.class).getMessage())),
                n("ConvertionException-4.msg-ok2", () -> Assertions.assertEquals("goo", new ConvertionException("goo", new Exception(), Test.class, Thread.class).getMessage())),
                n("ConvertionException-4.cause-ok1", () -> Assertions.assertSame(xxx, new ConvertionException("foo", xxx, String.class, Float.class).getCause())),
                n("ConvertionException-4.cause-ok2", () -> Assertions.assertSame(yyy, new ConvertionException("goo", yyy, Test.class, Thread.class).getCause())),
                n("ConvertionException-4.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException("foo", new Exception(), String.class, Float.class).getIn())),
                n("ConvertionException-4.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException("foo", new Exception(), Test.class, Thread.class).getIn())),
                n("ConvertionException-4.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException("foo", new Exception(), String.class, Float.class).getOut())),
                n("ConvertionException-4.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException("foo", new Exception(), Test.class, Thread.class).getOut())),

                n("UnavailableConverterException-2.msg-null", () -> ForTests.testNull("message", () -> new UnavailableConverterException(null, String.class))),
                n("UnavailableConverterException-2.root-null", () -> ForTests.testNull("root", () -> new UnavailableConverterException("foo", null))),
                n("UnavailableConverterException-3.msg-null", () -> ForTests.testNull("message", () -> new UnavailableConverterException(null, new Exception(), String.class))),
                n("UnavailableConverterException-3.cause-null", () -> ForTests.testNull("cause", () -> new UnavailableConverterException("foo", null, String.class))),
                n("UnavailableConverterException-3.root-null", () -> ForTests.testNull("root", () -> new UnavailableConverterException("foo", new Exception(), null))),
                n("UnavailableConverterException-2.msg-ok1", () -> Assertions.assertEquals("foo", new UnavailableConverterException("foo", String.class).getMessage())),
                n("UnavailableConverterException-2.msg-ok2", () -> Assertions.assertEquals("goo", new UnavailableConverterException("goo", Test.class).getMessage())),
                n("UnavailableConverterException-2.root-ok1", () -> Assertions.assertEquals(String.class, new UnavailableConverterException("foo", String.class).getRoot())),
                n("UnavailableConverterException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new UnavailableConverterException("foo", Test.class).getRoot())),
                n("UnavailableConverterException-3.msg-ok1", () -> Assertions.assertEquals("foo", new UnavailableConverterException("foo", new Exception(), String.class).getMessage())),
                n("UnavailableConverterException-3.msg-ok2", () -> Assertions.assertEquals("goo", new UnavailableConverterException("goo", new Exception(), Test.class).getMessage())),
                n("UnavailableConverterException-3.cause-ok1", () -> Assertions.assertSame(xxx, new UnavailableConverterException("foo", xxx, String.class).getCause())),
                n("UnavailableConverterException-3.cause-ok2", () -> Assertions.assertSame(yyy, new UnavailableConverterException("goo", yyy, Test.class).getCause())),
                n("UnavailableConverterException-3.root-ok1", () -> Assertions.assertEquals(String.class, new UnavailableConverterException("foo", new Exception(), String.class).getRoot())),
                n("UnavailableConverterException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new UnavailableConverterException("foo", new Exception(), Test.class).getRoot()))
        );
    }
}
