package ninja.javahacker.test.annotimpler.convert;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class ConstructionExceptionTest {

    private static Arguments n(String name, Executable ctx) {
        return Arguments.of(name, ctx);
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private static Stream<Arguments> testAll() {
        return Stream.of(
                n("ConvertionException-2.msg-null", () -> ForTests.testNull("message", () -> new Converter.ConvertionException(null, String.class))),
                n("ConvertionException-2.root-null", () -> ForTests.testNull("root", () -> new Converter.ConvertionException("foo", null))),
                n("ConvertionException-3.msg-null", () -> ForTests.testNull("message", () -> new Converter.ConvertionException(null, new Exception(), String.class))),
                n("ConvertionException-3.cause-null", () -> ForTests.testNull("cause", () -> new Converter.ConvertionException("foo", null, String.class))),
                n("ConvertionException-3.root-null", () -> ForTests.testNull("root", () -> new Converter.ConvertionException("foo", new Exception(), null))),
                n("ConvertionException-2.root-ok1", () -> Assertions.assertEquals(String.class, new Converter.ConvertionException("foo", String.class).getRoot())),
                n("ConvertionException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new Converter.ConvertionException("foo", Test.class).getRoot())),
                n("ConvertionException-3.root-ok1", () -> Assertions.assertEquals(String.class, new Converter.ConvertionException("foo", new Exception(), String.class).getRoot())),
                n("ConvertionException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new Converter.ConvertionException("foo", new Exception(), Test.class).getRoot())),

                n("UnavailableConverterException-2.msg-null", () -> ForTests.testNull("message", () -> new ConverterFactory.UnavailableConverterException(null, String.class))),
                n("UnavailableConverterException-2.root-null", () -> ForTests.testNull("root", () -> new ConverterFactory.UnavailableConverterException("foo", null))),
                n("UnavailableConverterException-3.msg-null", () -> ForTests.testNull("message", () -> new ConverterFactory.UnavailableConverterException(null, new Exception(), String.class))),
                n("UnavailableConverterException-3.cause-null", () -> ForTests.testNull("cause", () -> new ConverterFactory.UnavailableConverterException("foo", null, String.class))),
                n("UnavailableConverterException-3.root-null", () -> ForTests.testNull("root", () -> new ConverterFactory.UnavailableConverterException("foo", new Exception(), null))),
                n("UnavailableConverterException-2.root-ok1", () -> Assertions.assertEquals(String.class, new ConverterFactory.UnavailableConverterException("foo", String.class).getRoot())),
                n("UnavailableConverterException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new ConverterFactory.UnavailableConverterException("foo", Test.class).getRoot())),
                n("UnavailableConverterException-3.root-ok1", () -> Assertions.assertEquals(String.class, new ConverterFactory.UnavailableConverterException("foo", new Exception(), String.class).getRoot())),
                n("UnavailableConverterException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new ConverterFactory.UnavailableConverterException("foo", new Exception(), Test.class).getRoot()))
        );
    }

    @MethodSource
    @ParameterizedTest(name = "{0}")
    public void testAll(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
