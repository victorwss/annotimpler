package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class FileSqlFactoryTest {

    private static final String LOREM_ISO_88591 = "Lorem ipsum dolor sit amet - áéíóúñçªº - Lorem ipsum dolor sit amet\n";

    private static final String LOREM_UTF_8 = "Lorem ipsum dolor sit amet 🤩😁🤩😁\n";

    public FileSqlFactoryTest() {
    }

    // Tests if it works and defaults to UTF-8.
    @SqlFromFile(value = "./test-files/lorem-utf-8.txt")
    private static void withSql1() {
        throw new AssertionError();
    }

    // Tests if it works with UTF-8.
    @SqlFromFile(value = "./test-files/lorem-utf-8.txt", encoding = CharsetSpec.Utf8.class)
    private static void withSql2() {
        throw new AssertionError();
    }

    // Tests if it works with ISO-8859-1.
    @SqlFromFile(value = "./test-files/lorem-iso-8859-1.txt", encoding = CharsetSpec.Iso88591Strict.class)
    private static void withSql3() {
        throw new AssertionError();
    }

    // Tests for 404.
    @SqlFromFile(value = "./test-files/does-not-exist.txt")
    private static void withSqlX1() {
        throw new AssertionError();
    }

    // Test if wrong encoding makes it crash.
    @SqlFromFile(value = "./test-files/lorem-iso-8859-1.txt", encoding = CharsetSpec.Utf8.class)
    private static void withSqlX2() {
        throw new AssertionError();
    }

    private static Method mtd(String name) {
        return Stream.of(FileSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    @TestFactory
    public Stream<DynamicTest> testFileSql() throws Exception {
        var pf = "[testFileSql] ";
        var a = Stream.of("withSql1", "withSql2")
                .map(m -> DynamicTest.dynamicTest(pf + m, () -> Assertions.assertEquals(LOREM_UTF_8, FileSqlFactory.INSTANCE.prepare(mtd(m)).get())));
        var b = Stream.of("withSql3")
                .map(m -> DynamicTest.dynamicTest(pf + m, () -> Assertions.assertEquals(LOREM_ISO_88591, FileSqlFactory.INSTANCE.prepare(mtd(m)).get())));
        return Stream.concat(a, b);
    }

    @Test
    public void testFileSqlDoesNotExist() throws Exception {
        var m = "withSqlX1";
        var ex = Assertions.assertThrows(SQLException.class, () -> FileSqlFactory.INSTANCE.prepare(mtd(m)).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertTrue(ex.getMessage().replace("\\", "/").endsWith("/test-files/does-not-exist.txt"));
    }

    @TestFactory
    public Stream<DynamicTest> testFileSqlBadEncoding() throws Exception {
        var pf = "[testFileSqlBadEncoding] ";
        return Stream.of("withSqlX2").map(m -> DynamicTest.dynamicTest(pf + m, () -> {
            var ex = Assertions.assertThrows(SQLException.class, () -> FileSqlFactory.INSTANCE.prepare(mtd(m)).get());
            Assertions.assertTrue(ex.getCause() instanceof IOException);
            Assertions.assertEquals("String can't be coded as UTF-8.", ex.getMessage());
        }));
    }
}
