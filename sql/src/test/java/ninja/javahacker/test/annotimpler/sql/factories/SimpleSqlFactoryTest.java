package ninja.javahacker.test.annotimpler.sql.factories;

import ninja.javahacker.test.ForTests;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class SimpleSqlFactoryTest {

    public SimpleSqlFactoryTest() {
    }

    @Sql("abc123")
    private static void withSql1() {
        throw new AssertionError();
    }

    @Sql("xyz987")
    private static void withSql2() {
        throw new AssertionError();
    }

    @Test
    public void testSimpleStringSql() throws Exception {
        var m1 = Stream.of(SimpleSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql1")).findAny().get();
        var m2 = Stream.of(SimpleSqlFactoryTest.class.getDeclaredMethods()).filter(e -> e.getName().equals("withSql2")).findAny().get();
        var x = StringSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals("abc123", x);
        var y = StringSqlFactory.INSTANCE.prepare(m2).get();
        Assertions.assertEquals("xyz987", y);
        var z = StringSqlFactory.INSTANCE.prepare(m1).get();
        Assertions.assertEquals("abc123", z);
    }
}
