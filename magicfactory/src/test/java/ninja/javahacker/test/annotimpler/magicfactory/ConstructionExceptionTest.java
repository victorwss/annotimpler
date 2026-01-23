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

    private static Stream<Arguments> testAll() {
        return Stream.of(
                n("ConstructionException-2.msg-null", () -> ForTests.testNull("message", () -> new ConstructionException(null, String.class))),
                n("ConstructionException-2.root-null", () -> ForTests.testNull("root", () -> new ConstructionException("foo", null))),
                n("ConstructionException-3.msg-null", () -> ForTests.testNull("message", () -> new ConstructionException(null, new Exception(), String.class))),
                n("ConstructionException-3.cause-null", () -> ForTests.testNull("cause", () -> new ConstructionException("foo", null, String.class))),
                n("ConstructionException-3.root-null", () -> ForTests.testNull("root", () -> new ConstructionException("foo", new Exception(), null))),
                n("ConstructionException-2.root-ok1", () -> Assertions.assertEquals(String.class, new ConstructionException("foo", String.class).getRoot())),
                n("ConstructionException-2.root-ok2", () -> Assertions.assertEquals(Test.class, new ConstructionException("foo", Test.class).getRoot())),
                n("ConstructionException-3.root-ok1", () -> Assertions.assertEquals(String.class, new ConstructionException("foo", new Exception(), String.class).getRoot())),
                n("ConstructionException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new ConstructionException("foo", new Exception(), Test.class).getRoot()))
        ).map(NamedTest::args);
    }

    @MethodSource
    @ParameterizedTest(name = "{0}")
    public void testAll(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
