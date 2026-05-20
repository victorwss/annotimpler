package ninja.javahacker.test.annotimpler.sql.factories;

import ninja.javahacker.test.ForTests;
import lombok.NonNull;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class ResourceSqlFactoryTest {

    private static final String LOREM_ISO_88591 = "Lorem ipsum dolor sit amet - áéíóúñçªº - Lorem ipsum dolor sit amet";

    private static final String LOREM_UTF_8 = "Lorem ipsum dolor sit amet 🤩😁🤩😁";

    @SqlFromResource("/ninja/javahacker/test/lorem-utf-8.txt")
    private static void withSql1() {
        throw new AssertionError();
    }

    @SqlFromResource(value = "/ninja/javahacker/test/lorem-utf-8.txt", encoding = CharsetSpec.Utf8.class)
    private static void withSql2() {
        throw new AssertionError();
    }

    @SqlFromResource(value = "/ninja/javahacker/test/lorem-iso-8859-1.txt", encoding = CharsetSpec.Iso88591.class)
    private static void withSql3() {
        throw new AssertionError();
    }

    @SqlFromResource("/ninja/javahacker/test/does-not-exist.txt")
    private static void withSqlX1() {
        throw new AssertionError();
    }

    @SqlFromResource(value = "/ninja/javahacker/test/lorem-utf-8.txt", encoding = CharsetSpec.Iso88591.class)
    private static void withSqlX2() {
        throw new AssertionError();
    }

    @Test
    public void testResourceSql() throws Exception {
        var m1 = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql1")).findAny().get();
        var m2 = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql2")).findAny().get();
        var m3 = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql3")).findAny().get();
        var x = ResourceSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals(LOREM_UTF_8, x);
        var y = ResourceSqlFactory.INSTANCE.prepare(m2).get();
        Assertions.assertEquals(LOREM_UTF_8, y);
        var z = ResourceSqlFactory.INSTANCE.prepare(m3).get();
        Assertions.assertEquals(LOREM_ISO_88591, z);
    }

    @Test
    public void testResourceSqlDoesNotExist() throws Exception {
        var mx = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX1")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ResourceSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertEquals("Can't read from source.", ex.getMessage());
    }

    @Test
    public void testResourceSqlBadEncoding() throws Exception {
        var mx = Stream.of(ResourceSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX2")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ResourceSqlFactory.INSTANCE.prepare(mx).get());
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
