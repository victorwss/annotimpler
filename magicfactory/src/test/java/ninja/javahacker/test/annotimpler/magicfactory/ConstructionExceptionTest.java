package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class ConstructionExceptionTest {

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    private static Stream<Arguments> testAll() {
        return Stream.of(
                n("CreationException-2.msg-null", () -> ForTests.testNull("message", () -> new MagicFactory.CreationException(null, String.class))),
                n("CreationException-2.root-null", () -> ForTests.testNull("root", () -> new MagicFactory.CreationException("foo", null))),
                n("CreationException-3.msg-null", () -> ForTests.testNull("message", () -> new MagicFactory.CreationException(null, new Exception(), String.class))),
                n("CreationException-3.cause-null", () -> ForTests.testNull("cause", () -> new MagicFactory.CreationException("foo", null, String.class))),
                n("CreationException-3.root-null", () -> ForTests.testNull("root", () -> new MagicFactory.CreationException("foo", new Exception(), null))),
                n("CreationException-2.root-ok1", () -> Assertions.assertEquals(String.class, new MagicFactory.CreationException("foo", String.class).getRoot())),
                n("CreationException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new MagicFactory.CreationException("foo", Test.class).getRoot())),
                n("CreationException-3.root-ok1", () -> Assertions.assertEquals(String.class, new MagicFactory.CreationException("foo", new Exception(), String.class).getRoot())),
                n("CreationException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new MagicFactory.CreationException("foo", new Exception(), Test.class).getRoot())),

                n("CreatorSelectionException-2.msg-null", () -> ForTests.testNull("message", () -> new MagicFactory.CreatorSelectionException(null, String.class))),
                n("CreatorSelectionException-2.root-null", () -> ForTests.testNull("root", () -> new MagicFactory.CreatorSelectionException("foo", null))),
                n("CreatorSelectionException-3.msg-null", () -> ForTests.testNull("message", () -> new MagicFactory.CreatorSelectionException(null, new Exception(), String.class))),
                n("CreatorSelectionException-3.cause-null", () -> ForTests.testNull("cause", () -> new MagicFactory.CreatorSelectionException("foo", null, String.class))),
                n("CreatorSelectionException-3.root-null", () -> ForTests.testNull("root", () -> new MagicFactory.CreatorSelectionException("foo", new Exception(), null))),
                n("CreatorSelectionException-2.root-ok1", () -> Assertions.assertEquals(String.class, new MagicFactory.CreatorSelectionException("foo", String.class).getRoot())),
                n("CreatorSelectionException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new MagicFactory.CreatorSelectionException("foo", Test.class).getRoot())),
                n("CreatorSelectionException-3.root-ok1", () -> Assertions.assertEquals(String.class, new MagicFactory.CreatorSelectionException("foo", new Exception(), String.class).getRoot())),
                n("CreatorSelectionException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new MagicFactory.CreatorSelectionException("foo", new Exception(), Test.class).getRoot())),

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
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "{0}")
    public void testAll(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
