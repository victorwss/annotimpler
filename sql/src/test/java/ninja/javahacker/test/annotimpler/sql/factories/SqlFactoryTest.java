package ninja.javahacker.test.annotimpler.sql.factories;

import ninja.javahacker.test.ForTests;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class SqlFactoryTest {

    private static void empty() {
        throw new AssertionError();
    }

    @Sql("abc123")
    private static void withSql1() {
        throw new AssertionError();
    }

    @Sql("xyz987")
    private static void withSql2() {
        throw new AssertionError();
    }

    private void assertRefusesEmpty(SqlFactory f) throws Exception {
        var m = Stream.of(SqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("empty")).findAny().get();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> f.prepare(m));
    }

    @TestFactory
    public Stream<DynamicTest> testMissingSqls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("StringSqlFactory", () -> assertRefusesEmpty(StringSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("ResourceSqlFactory", () -> assertRefusesEmpty(ResourceSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("FileSqlFactory", () -> assertRefusesEmpty(FileSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("SupplierSqlFactory", () -> assertRefusesEmpty(SupplierSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("UrlSqlFactory", () -> assertRefusesEmpty(UrlSqlFactory.INSTANCE))
        );
    }

    @TestFactory
    public Stream<DynamicTest> testNulls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("StringSqlFactory", () -> ForTests.testNull("m", () -> StringSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("ResourceSqlFactory", () -> ForTests.testNull("m", () -> ResourceSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("FileSqlFactory", () -> ForTests.testNull("m", () -> FileSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("SupplierSqlFactory", () -> ForTests.testNull("m", () -> SupplierSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("UrlSqlFactory", () -> ForTests.testNull("m", () -> UrlSqlFactory.INSTANCE.prepare(null)))
        );
    }

    @Test
    public void testSimpleStringSql() throws Exception {
        var m1 = Stream.of(SqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql1")).findAny().get();
        var m2 = Stream.of(SqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql2")).findAny().get();
        var x = StringSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals("abc123", x);
        var y = StringSqlFactory.INSTANCE.prepare(m2).get();
        Assertions.assertEquals("xyz987", y);
        var z = StringSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals("abc123", z);
    }
}
