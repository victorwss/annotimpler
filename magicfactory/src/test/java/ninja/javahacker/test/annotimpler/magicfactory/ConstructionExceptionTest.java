package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

public class ConstructionExceptionTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    @TestFactory
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public Stream<DynamicTest> testAll() {
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
                n("CreatorSelectionException-3.root-ok2", () -> Assertions.assertEquals(Test.class, new MagicFactory.CreatorSelectionException("foo", new Exception(), Test.class).getRoot()))        );
    }
}
