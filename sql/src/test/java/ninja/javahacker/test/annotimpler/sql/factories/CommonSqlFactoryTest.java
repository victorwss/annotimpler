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

    public CommonSqlFactoryTest() {
    }

    private void assertRefusesEmpty(SqlFactory f) throws Exception {
        var m = Stream.of(CommonSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("empty")).findAny().get();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> f.prepare(m));
    }

    @TestFactory
    public Stream<DynamicTest> testMissingSqls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("[testMissingSqls] StringSqlFactory"  , () -> assertRefusesEmpty(StringSqlFactory  .INSTANCE)),
                DynamicTest.dynamicTest("[testMissingSqls] ResourceSqlFactory", () -> assertRefusesEmpty(ResourceSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("[testMissingSqls] FileSqlFactory"    , () -> assertRefusesEmpty(FileSqlFactory    .INSTANCE)),
                DynamicTest.dynamicTest("[testMissingSqls] SupplierSqlFactory", () -> assertRefusesEmpty(SupplierSqlFactory.INSTANCE)),
                DynamicTest.dynamicTest("[testMissingSqls] UrlSqlFactory"     , () -> assertRefusesEmpty(UrlSqlFactory     .INSTANCE))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("[testNulls] StringSqlFactory"  , () -> ForTests.testNull("m", () -> StringSqlFactory  .INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("[testNulls] ResourceSqlFactory", () -> ForTests.testNull("m", () -> ResourceSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("[testNulls] FileSqlFactory"    , () -> ForTests.testNull("m", () -> FileSqlFactory    .INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("[testNulls] SupplierSqlFactory", () -> ForTests.testNull("m", () -> SupplierSqlFactory.INSTANCE.prepare(null))),
                DynamicTest.dynamicTest("[testNulls] UrlSqlFactory"     , () -> ForTests.testNull("m", () -> UrlSqlFactory     .INSTANCE.prepare(null)))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testReadPolicyNulls() throws Exception {
        return Stream.of(
                DynamicTest.dynamicTest("[testReadPolicyNulls] impl"     , () -> ForTests.testNull("impl"     , () -> ReadPolicy.ON_STARTUP.prepare(null, "foo"))),
                DynamicTest.dynamicTest("[testReadPolicyNulls] inputData", () -> ForTests.testNull("inputData", () -> ReadPolicy.ON_STARTUP.prepare(a -> a, (String) null)))
        );
    }
}
