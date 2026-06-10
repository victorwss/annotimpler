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
    public Stream<DynamicTest> testAll() throws Exception {
        var ct = this.getClass().getMethod("testAll").getGenericReturnType();
        var ex = new Exception();
        return Stream.of(
                n("2.msg-null"  , () -> ForTests.testNull("message", () -> new BadImplementationException(null , String.class))),
                n("2.root-null" , () -> ForTests.testNull("root"   , () -> new BadImplementationException("foo", null))),
                n("3.msg-null"  , () -> ForTests.testNull("message", () -> new BadImplementationException(null , new Exception(), String.class))),
                n("3.cause-null", () -> ForTests.testNull("cause"  , () -> new BadImplementationException("foo", null, String.class))),
                n("3.root-null" , () -> ForTests.testNull("root"   , () -> new BadImplementationException("foo", new Exception(), null))),
                n("2.root-ok1"  , () -> Assertions.assertSame  (String.class, new BadImplementationException("foo", String.class).getRoot())),
                n("2.root-ok2"  , () -> Assertions.assertSame  (Test  .class, new BadImplementationException("foo", Test  .class).getRoot())),
                n("2.root-ok3"  , () -> Assertions.assertSame  (ct          , new BadImplementationException("foo", ct          ).getRoot())),
                n("2.msg-ok1"   , () -> Assertions.assertEquals("foo"       , new BadImplementationException("foo", String.class).getMessage())),
                n("2.msg-ok2"   , () -> Assertions.assertEquals("bar"       , new BadImplementationException("bar", String.class).getMessage())),
                n("2.cause-ok"  , () -> Assertions.assertNull  (              new BadImplementationException("bar", String.class).getCause())),
                n("3.root-ok1"  , () -> Assertions.assertSame  (String.class, new BadImplementationException("foo", ex, String.class).getRoot())),
                n("3.root-ok2"  , () -> Assertions.assertSame  (Test  .class, new BadImplementationException("foo", ex, Test  .class).getRoot())),
                n("3.root-ok2"  , () -> Assertions.assertSame  (ct          , new BadImplementationException("foo", ex, ct          ).getRoot())),
                n("3.msg-ok1"   , () -> Assertions.assertEquals("foo"       , new BadImplementationException("foo", ex, String.class).getMessage())),
                n("3.msg-ok2"   , () -> Assertions.assertEquals("bar"       , new BadImplementationException("bar", ex, String.class).getMessage())),
                n("3.cause-ok1" , () -> Assertions.assertSame  (ex          , new BadImplementationException("foo", ex, String.class).getCause())),
                n("3.cause-ok2" , () -> Assertions.assertSame  (ex          , new BadImplementationException("bar", ex, String.class).getCause()))
        );
    }
}
