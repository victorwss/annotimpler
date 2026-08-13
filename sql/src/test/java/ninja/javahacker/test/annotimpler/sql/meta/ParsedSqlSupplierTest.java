package ninja.javahacker.test.annotimpler.sql.meta;

import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

@SuppressWarnings("unused")
public class ParsedSqlSupplierTest {

    @SqlSource(StringSqlFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private static @interface AlsoSql {
        String value();
    }

    @SqlSource(BadFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private static @interface BadSql {
        String value();
    }

    public static final class BadFactory implements SqlFactory {
        private BadFactory(int x) {
            throw new AssertionError(x);
        }

        @Override
        public SqlSupplier prepare(Method m) {
            throw new AssertionError(m);
        }
    }

    @Sql("SELECT :x")
    private static void ok(String x) {
        throw new AssertionError();
    }

    @Sql("SELECT :x = :")
    private static void malformed(String x) {
        throw new AssertionError();
    }

    @Sql("SELECT :x")
    private static void mismatch(String y) {
        throw new AssertionError();
    }

    private static void noSql(String x) {
        throw new AssertionError();
    }

    @Sql("SELECT :x")
    @AlsoSql("SELECT :x")
    private static void multiSql(String x) {
        throw new AssertionError();
    }

    @BadSql("SELECT :x")
    private static void badFactory(String x) {
        throw new AssertionError();
    }

    private static Method m(String name) {
        return Stream.of(ParsedSqlSupplierTest.class.getDeclaredMethods())
                .filter(mm -> mm.getName().equals(name))
                .findFirst()
                .orElseThrow(AssertionError::new);
    }

    @Test
    public void testStrictOk() throws Exception {
        var sup = ParsedSqlSupplier.find(true, new ParameterSet(m("ok")));
        var pq = sup.get();
        Assertions.assertAll(
                () -> Assertions.assertEquals("SELECT :x", pq.original()),
                () -> Assertions.assertEquals("SELECT ?", pq.parsed()),
                () -> Assertions.assertEquals(java.util.Set.of("x"), pq.params().keySet()),
                () -> Assertions.assertFalse(pq.hasErrors())
        );
    }

    @Test
    public void testNonStrictAcceptsMalformedSql() throws Exception {
        var sup = ParsedSqlSupplier.find(false, new ParameterSet(m("malformed")));
        var pq = sup.get();
        Assertions.assertAll(
                () -> Assertions.assertTrue(pq.hasErrors()),
                () -> Assertions.assertTrue(pq.loneColons())
        );
    }

    @Test
    public void testStrictRejectsMalformedSql() throws Exception {
        var sup = ParsedSqlSupplier.find(true, new ParameterSet(m("malformed")));
        var ex = Assertions.assertThrows(SQLException.class, sup::get);
        Assertions.assertTrue(ex.getMessage().contains("Malformed SQL for "));
    }

    @Test
    public void testStrictRejectsParameterMismatch() throws Exception {
        var sup = ParsedSqlSupplier.find(true, new ParameterSet(m("mismatch")));
        var ex = Assertions.assertThrows(SQLException.class, sup::get);
        Assertions.assertTrue(ex.getMessage().contains("Method parameters mismatches SQL for "));
    }

    @Test
    public void testNoSqlAnnotation() throws Exception {
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ParsedSqlSupplier.find(true, new ParameterSet(m("noSql"))));
        Assertions.assertTrue(ex.getMessage().contains("No SQL annotation found on "));
    }

    @Test
    public void testMultipleSqlAnnotations() throws Exception {
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ParsedSqlSupplier.find(true, new ParameterSet(m("multiSql"))));
        Assertions.assertTrue(ex.getMessage().contains("More than one SQL annotation found on "));
    }

    @Test
    public void testFactoryInstantiationFailure() throws Exception {
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> ParsedSqlSupplier.find(true, new ParameterSet(m("badFactory"))));
        Assertions.assertAll(
                () -> Assertions.assertTrue(ex.getMessage().contains("Can't instantiate BadFactory to handle ")),
                () -> Assertions.assertEquals(ParsedSqlSupplierTest.class, ex.getRoot()),
                () -> Assertions.assertTrue(
                        ex.getCause() instanceof MagicFactory.CreatorSelectionException
                                || ex.getCause() instanceof MagicFactory.CreationException
                )
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testNulls() {
        ForTests.testNull("pset", () -> ParsedSqlSupplier.find(true, null));
    }
}
