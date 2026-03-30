package ninja.javahacker.test.annotimpler.core;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module org.junit.jupiter.api;

public class BadImplementationExceptionTest {

    public BadImplementationExceptionTest() {
    }

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testAll() {
        return Stream.of(
                n("2.msg-null", () -> ForTests.testNull("message", () -> new BadImplementationException(null, String.class))),
                n("2.root-null", () -> ForTests.testNull("root", () -> new BadImplementationException("foo", null))),
                n("3.msg-null", () -> ForTests.testNull("message", () -> new BadImplementationException(null, new Exception(), String.class))),
                n("3.cause-null", () -> ForTests.testNull("cause", () -> new BadImplementationException("foo", null, String.class))),
                n("3.root-null", () -> ForTests.testNull("root", () -> new BadImplementationException("foo", new Exception(), null))),
                n("2.root-ok1", () -> Assertions.assertEquals(String.class, new BadImplementationException("foo", String.class).getRoot())),
                n("2.root-ok2", () -> Assertions.assertEquals(Test.class, new BadImplementationException("foo", Test.class).getRoot())),
                n("3.root-ok1", () -> Assertions.assertEquals(String.class, new BadImplementationException("foo", new Exception(), String.class).getRoot())),
                n("3.root-ok2", () -> Assertions.assertEquals(Test.class, new BadImplementationException("foo", new Exception(), Test.class).getRoot()))
        );
    }
}
