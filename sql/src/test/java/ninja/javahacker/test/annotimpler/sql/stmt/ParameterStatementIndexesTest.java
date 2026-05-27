package ninja.javahacker.test.annotimpler.sql.stmt;

import java.lang.reflect.Proxy;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class ParameterStatementIndexesTest {

    public ParameterStatementIndexesTest() {
    }

    private static void testBad(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt);
        Assertions.assertEquals("Parameter not found: " + paramName, ex.getMessage());
    }

    private static PreparedStatement mockPs() {
        var mock = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {PreparedStatement.class}, (i, m, a) -> {
            throw new AssertionError();
        });
        return (PreparedStatement) mock;
    }

    @Test
    @SuppressWarnings("null")
    public void testWrapNullMap() throws Exception {
        ForTests.testNull("indexes", () -> NamedParameterStatement.wrap(mockPs(), null));
    }

    @Test
    @SuppressWarnings("null")
    public void testGetNullIndexes() throws Exception {
        var map = Map.of("a", List.of(1, 3), "b", List.of(2, 4));
        ForTests.testNull("name", () -> NamedParameterStatement.wrap(mockPs(), map).getIndexes(null));
    }

    @Test
    @SuppressWarnings("null")
    public void testGetIndexes() throws Exception {
        var map = Map.of("a", List.of(1, 3), "b", List.of(2, 4));
        var wrap = NamedParameterStatement.wrap(mockPs(), map);
        Assertions.assertAll(
                () -> Assertions.assertEquals(List.of(1, 3), wrap.getIndexes("a")),
                () -> Assertions.assertEquals(List.of(2, 4), wrap.getIndexes("b")),
                () -> testBad("", () -> wrap.getIndexes("")),
                () -> testBad("c", () -> wrap.getIndexes("c"))
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testGetIndexesImmutability() throws Exception {
        var map = Map.of("a", List.of(1, 3), "b", List.of(2, 4));
        var wrap = NamedParameterStatement.wrap(mockPs(), map);
        var z = wrap.getIndexes("a");
        Assertions.assertAll(
                () -> Assertions.assertThrows(UnsupportedOperationException.class, () -> z.add(5)),
                () -> Assertions.assertEquals(List.of(1, 3), z),
                () -> Assertions.assertEquals(List.of(1, 3), wrap.getIndexes("a"))
        );
    }
}
