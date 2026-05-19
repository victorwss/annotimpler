package ninja.javahacker.test.annotimpler.sql.factories;

import ninja.javahacker.test.ForTests;
import lombok.NonNull;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class FileSqlFactoryTest {

    private static final String LOREM_ISO_88591 = "Lorem ipsum dolor sit amet - áéíóúñçªº - Lorem ipsum dolor sit amet";

    private static final String LOREM_UTF_8 = "Lorem ipsum dolor sit amet 🤩😁🤩😁";

    @SqlFromFile("./test-files/lorem-utf-8.txt")
    private static void withSql1() {
        throw new AssertionError();
    }

    @SqlFromFile(value = "./test-files/lorem-utf-8.txt", encoding = CharsetSpec.Utf8.class)
    private static void withSql2() {
        throw new AssertionError();
    }

    @SqlFromFile(value = "./test-files/lorem-iso-8859-1.txt", encoding = CharsetSpec.Iso88591.class)
    private static void withSql3() {
        throw new AssertionError();
    }

    @SqlFromFile("./test-files/does-not-exist.txt")
    private static void withSqlX1() {
        throw new AssertionError();
    }

    @SqlFromFile(value = "./test-files/lorem-utf-8.txt", encoding = CharsetSpec.Iso88591.class)
    private static void withSqlX2() {
        throw new AssertionError();
    }

    @Test
    public void testFileSql() throws Exception {
        var m1 = Stream.of(FileSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql1")).findAny().get();
        var m2 = Stream.of(FileSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql2")).findAny().get();
        var m3 = Stream.of(FileSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql3")).findAny().get();
        var x = FileSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals(LOREM_UTF_8, x);
        var y = FileSqlFactory.INSTANCE.prepare(m2).get();
        Assertions.assertEquals(LOREM_UTF_8, y);
        var z = FileSqlFactory.INSTANCE.prepare(m3).get();
        Assertions.assertEquals(LOREM_ISO_88591, z);
    }

    @Test
    public void testFileSqlDoesNotExist() throws Exception {
        var mx = Stream.of(FileSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX1")).findAny().get();
        var ex = Assertions.assertThrows(FileNotFoundException.class, () -> FileSqlFactory.INSTANCE.prepare(mx));
        Assertions.assertEquals("./test-files/does-not-exist.txt", ex.getMessage());
    }

    @Test
    public void testFileSqlBadEncoding() throws Exception {
        var mx = Stream.of(FileSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX2")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> FileSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(UnmappableCharacterException.class, ex.getCause().getClass());
    }

    /*/@Test
    public void testBadSupplierClass() throws Exception {
        var mx = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX3")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ResourceSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass());
    }

    @Test
    public void testBadSupplierCtor() throws Exception {
        var mx = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX4")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ResourceSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass());
    }/*/
}
