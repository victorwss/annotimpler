package ninja.javahacker.test.annotimpler.sql.factories;

import ninja.javahacker.test.ForTests;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class CommonSqlFactoryTest {

    private static void empty() {
        throw new AssertionError();
    }

    private void assertRefusesEmpty(SqlFactory f) throws Exception {
        var m = Stream.of(CommonSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("empty")).findAny().get();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> f.prepare(m));
    }

    @TestFactory
    public Stream<DynamicTest> testMissingSqls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("StringSqlFactory"  , () -> assertRefusesEmpty(StringSqlFactory  .INSTANCE)),
                DynamicTest.dynamicTest("ResourceSqlFactory", () -> assertRefusesEmpty(ResourceSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("FileSqlFactory"    , () -> assertRefusesEmpty(FileSqlFactory    .INSTANCE)),
                DynamicTest.dynamicTest("SupplierSqlFactory", () -> assertRefusesEmpty(SupplierSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("UrlSqlFactory"     , () -> assertRefusesEmpty(UrlSqlFactory     .INSTANCE))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("StringSqlFactory"  , () -> ForTests.testNull("m", () -> StringSqlFactory  .INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("ResourceSqlFactory", () -> ForTests.testNull("m", () -> ResourceSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("FileSqlFactory"    , () -> ForTests.testNull("m", () -> FileSqlFactory    .INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("SupplierSqlFactory", () -> ForTests.testNull("m", () -> SupplierSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("UrlSqlFactory"     , () -> ForTests.testNull("m", () -> UrlSqlFactory     .INSTANCE.prepare(null)))
        );
    }
}
