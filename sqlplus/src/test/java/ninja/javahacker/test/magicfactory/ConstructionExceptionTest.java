package ninja.javahacker.test.magicfactory;

import ninja.javahacker.test.ForTests;

import module ninja.javahacker.sqlplus;
import module org.junit.jupiter.api;

public class ConstructionExceptionTest {

    @Test
    public void testConstructionExceptionNulls() {
        Assertions.assertAll(
                () -> ForTests.testNull("message", () -> new ConstructionException(null, String.class), "ConstructionException-2.msg"),
                () -> ForTests.testNull("root", () -> new ConstructionException("foo", null), "ConstructionException-2.root"),
                () -> ForTests.testNull("message", () -> new ConstructionException(null, new Exception(), String.class), "ConstructionException-3.msg"),
                () -> ForTests.testNull("cause", () -> new ConstructionException("foo", null, String.class), "ConstructionException-3.cause"),
                () -> ForTests.testNull("root", () -> new ConstructionException("foo", new Exception(), null), "ConstructionException-3.root")
        );
    }

    @Test
    public void testGetter() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(String.class, new ConstructionException("foo", String.class).getRoot()),
                () -> Assertions.assertEquals(Test.class, new ConstructionException("foo", Test.class).getRoot())
        );
    }
}
