package ninja.javahacker.test.annotimpler.sql.factories;

import ninja.javahacker.test.ForTests;
import lombok.NonNull;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class SupplierSqlFactoryTest {

    private static void empty() {
        throw new AssertionError();
    }

    public static class SomeSupplier1 implements SqlSupplier {

        public SomeSupplier1() {
        }

        @Override
        public String get() throws SQLException {
            return "abc123";
        }
    }

    @SqlFromClass(SomeSupplier1.class)
    private static void withSql1() {
        throw new AssertionError();
    }

    public static class SomeSupplier2 implements SqlSupplier {

        private final String key;

        public SomeSupplier2(@NonNull String key) {
            this.key = key;
        }

        @Override
        public String get() throws SQLException {
            return "xyz987" + key;
        }
    }

    @SqlFromClass(value = SomeSupplier2.class, key = "654321")
    private static void withSql2() {
        throw new AssertionError();
    }

    public static class SomeSupplierX1 implements SqlSupplier {

        public SomeSupplierX1(@NonNull String key) {
            Assertions.assertEquals("oops", key);
        }

        @Override
        public String get() throws SQLException {
            throw new SQLException("Sorry.");
        }
    }

    @SqlFromClass(value = SomeSupplierX1.class, key = "oops")
    private static void withSqlX1() {
        throw new AssertionError();
    }

    public static class SomeSupplierX2 implements SqlSupplier {

        public SomeSupplierX2(@NonNull String key) throws SQLException {
            throw new SQLException("My bad: " + key);
        }

        @Override
        public String get() throws SQLException {
            throw new AssertionError();
        }
    }

    @SqlFromClass(value = SomeSupplierX2.class, key = "duh")
    private static void withSqlX2() {
        throw new AssertionError();
    }

    public static abstract class SomeSupplierX3 implements SqlSupplier {
    }

    @SqlFromClass(value = SomeSupplierX3.class, key = "duh")
    private static void withSqlX3() {
        throw new AssertionError();
    }

    public static abstract class SomeSupplierX4 implements SqlSupplier {

        public SomeSupplierX4(int x) {
            throw new AssertionError();
        }

        @Override
        public String get() throws SQLException {
            throw new AssertionError();
        }
    }

    @SqlFromClass(value = SomeSupplierX4.class, key = "duh")
    private static void withSqlX4() {
        throw new AssertionError();
    }

    @Test
    public void testSupplierSql() throws Exception {
        var m1 = Stream.of(SupplierSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql1")).findAny().get();
        var m2 = Stream.of(SupplierSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql2")).findAny().get();
        var x = SupplierSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals("abc123", x);
        var y = SupplierSqlFactory.INSTANCE.prepare(m2).get();
        Assertions.assertEquals("xyz987654321", y);
        var z = SupplierSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals("abc123", z);
    }

    @Test
    public void testSupplierSqlException() throws Exception {
        var mx = Stream.of(SupplierSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX1")).findAny().get();
        var ex = Assertions.assertThrows(SQLException.class, () -> SupplierSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Sorry.", ex.getMessage());
    }

    @Test
    public void testSupplierSqlExceptionOnCtor() throws Exception {
        var mx = Stream.of(SupplierSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX2")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> SupplierSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreationException.class, ex.getCause().getClass());
        Assertions.assertEquals("My bad: duh", ex.getCause().getCause().getMessage());
        Assertions.assertEquals(SQLException.class, ex.getCause().getCause().getClass());
    }

    @Test
    public void testBadSupplierClass() throws Exception {
        var mx = Stream.of(SupplierSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX3")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> SupplierSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass());
    }

    @Test
    public void testBadSupplierCtor() throws Exception {
        var mx = Stream.of(SupplierSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSqlX4")).findAny().get();
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> SupplierSqlFactory.INSTANCE.prepare(mx).get());
        Assertions.assertEquals("Can't create a SqlSupplier.", ex.getMessage());
        Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass());
    }
}
