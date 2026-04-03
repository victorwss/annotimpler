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
        return Stream.of(
                n("ConvertionException-3.msg-null", () -> ForTests.testNull("message", () -> new ConvertionException(null, String.class, String.class))),
                n("ConvertionException-3.in-null", () -> ForTests.testNull("in", () -> new ConvertionException("foo", null, String.class))),
                n("ConvertionException-3.out-null", () -> ForTests.testNull("out", () -> new ConvertionException("foo", String.class, null))),
                n("ConvertionException-4.msg-null", () -> ForTests.testNull("message", () -> new ConvertionException(null, new Exception(), String.class, String.class))),
                n("ConvertionException-4.cause-null", () -> ForTests.testNull("cause", () -> new ConvertionException("foo", null, String.class, String.class))),
                n("ConvertionException-4.in-null", () -> ForTests.testNull("in", () -> new ConvertionException("foo", new Exception(), null, String.class))),
                n("ConvertionException-4.out-null", () -> ForTests.testNull("out", () -> new ConvertionException("foo", new Exception(), String.class, null))),
                n("ConvertionException-3.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException("foo", String.class, Float.class).getIn())),
                n("ConvertionException-3.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException("foo", Test.class, Thread.class).getIn())),
                n("ConvertionException-3.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException("foo", String.class, Float.class).getOut())),
                n("ConvertionException-3.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException("foo", Test.class, Thread.class).getOut())),
                n("ConvertionException-4.in-ok1", () -> Assertions.assertEquals(String.class, new ConvertionException("foo", new Exception(), String.class, Float.class).getIn())),
                n("ConvertionException-4.in-ok2", () -> Assertions.assertEquals(Test.class, new ConvertionException("foo", new Exception(), Test.class, Thread.class).getIn())),
                n("ConvertionException-4.out-ok1", () -> Assertions.assertEquals(Float.class, new ConvertionException("foo", new Exception(), String.class, Float.class).getOut())),
                n("ConvertionException-4.out-ok2", () -> Assertions.assertEquals(Thread.class, new ConvertionException("foo", new Exception(), Test.class, Thread.class).getOut())),

                n("UnavailableConverterException-2.msg-null", () -> ForTests.testNull("message", () -> new UnavailableConverterException(null, String.class))),
                n("UnavailableConverterException-2.root-null", () -> ForTests.testNull("root", () -> new UnavailableConverterException("foo", null))),
                n("UnavailableConverterException-3.msg-null", () -> ForTests.testNull("message", () -> new UnavailableConverterException(null, new Exception(), String.class))),
                n("UnavailableConverterException-3.cause-null", () -> ForTests.testNull("cause", () -> new UnavailableConverterException("foo", null, String.class))),
                n("UnavailableConverterException-3.root-null", () -> ForTests.testNull("root", () -> new UnavailableConverterException("foo", new Exception(), null))),
                n("UnavailableConverterException-2.root-ok1", () -> Assertions.assertEquals(String.class, new UnavailableConverterException("foo", String.class).getRoot())),
                n("UnavailableConverterException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new UnavailableConverterException("foo", Test.class).getRoot())),
                n("UnavailableConverterException-3.root-ok1", () -> Assertions.assertEquals(String.class, new UnavailableConverterException("foo", new Exception(), String.class).getRoot())),
                n("UnavailableConverterException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new UnavailableConverterException("foo", new Exception(), Test.class).getRoot()))
        );
    }
}
