package ninja.javahacker.test.annotimpler.sql.factories;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.sql;

@SuppressWarnings("unused")
public class FileSqlFactoryLoadTest {

    public FileSqlFactoryLoadTest() {
    }

    @SqlFromFile(value = "./test-files/lorem-1.txt", policy = ReadPolicy.ON_FIRST_TIME_THAT_WORKS)
    private static void withSqlOnFirstTimeThatWorks() {
        throw new AssertionError();
    }

    @SqlFromFile(value = "./test-files/lorem-2.txt", policy = ReadPolicy.ON_STARTUP)
    private static void withSqlOnStartup() {
        throw new AssertionError();
    }

    @SqlFromFile(value = "./test-files/lorem-3.txt", policy = ReadPolicy.EVERY_TIME)
    private static void withSqlOnEveryTime() {
        throw new AssertionError();
    }

    @SqlFromFile(value = "./test-files/lorem-4.txt", policy = ReadPolicy.ON_FIRST_TIME_DONT_RETRY)
    private static void withSqlOnFirstTimeDontRetry() {
        throw new AssertionError();
    }

    private static Path absolute(String fileName) {
        return Path.of(fileName).normalize().toAbsolutePath();
    }

    private static void testFails(SqlSupplier s, String fileName) throws Exception {
        var p = absolute(fileName);
        p.toFile().delete();
        var ex = Assertions.assertThrows(SQLException.class, () -> s.get());
        Assertions.assertTrue(ex.getCause() instanceof IOException);
        Assertions.assertTrue(ex.getMessage().replace("\\", "/").endsWith(fileName.substring(1)));
    }

    private static void createFile(File p, int workValue) throws Exception {
        try (var o = new FileWriter(p)) {
            o.write("xyz" + workValue);
        }
    }

    private static void testOk(SqlSupplier s, int workValue) throws Exception {
        Assertions.assertEquals("xyz" + workValue, s.get());
    }

    private static Method mtd(String name) {
        return Stream.of(FileSqlFactoryLoadTest.class.getDeclaredMethods()).filter(e -> e.getName().equals(name)).findAny().get();
    }

    @Test
    public void sanityTest() {
        var p = absolute("./test-files/lorem-utf-8.txt");
        Assertions.assertTrue(p.toFile().exists());
    }

    @Test
    public void testOnStartupSuccess() throws Exception {
        var a = "./test-files/lorem-2.txt";
        var p = absolute(a).toFile();
        try {
            var m = mtd("withSqlOnStartup");
            createFile(p, 13);
            var f = FileSqlFactory.INSTANCE.prepare(m);
            p.delete();
            testOk(f, 13);
            testOk(f, 13);
        } finally {
            p.delete();
        }
    }

    @Test
    public void testOnStartupError() throws Exception {
        var a = "./test-files/lorem-2.txt";
        var p = absolute(a).toFile();
        p.delete();
        var m = mtd("withSqlOnStartup");
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> FileSqlFactory.INSTANCE.prepare(m));
        Assertions.assertAll(
                () -> Assertions.assertEquals("Can't read from source.", ex.getMessage()),
                () -> Assertions.assertEquals(NoSuchFileException.class, ex.getCause().getClass())
        );
    }

    @Test
    public void testEveryTime() throws Exception {
        var a = "./test-files/lorem-3.txt";
        var p = absolute(a).toFile();
        try {
            var m = mtd("withSqlOnEveryTime");
            p.delete();
            var f = FileSqlFactory.INSTANCE.prepare(m);
            p.delete();
            testFails(f, a);
            createFile(p, 5);
            testOk(f, 5);
            p.delete();
            testFails(f, a);
            createFile(p, 13);
            testOk(f, 13);
            testOk(f, 13);
            createFile(p, 7);
            testOk(f, 7);
        } finally {
            p.delete();
        }
    }

    @Test
    public void testOnFirstTimeThatWorks() throws Exception {
        var a = "./test-files/lorem-1.txt";
        var p = absolute(a).toFile();
        try {
            var m = mtd("withSqlOnFirstTimeThatWorks");
            p.delete();
            var f = FileSqlFactory.INSTANCE.prepare(m);
            p.delete();
            testFails(f, a);
            testFails(f, a);
            createFile(p, 13);
            testOk(f, 13);
            p.delete();
            testOk(f, 13);
        } finally {
            p.delete();
        }
    }

    @Test
    public void testOnFirstTimeDontRetrySuccess() throws Exception {
        var a = "./test-files/lorem-4.txt";
        var p = absolute(a).toFile();
        try {
            var m = mtd("withSqlOnFirstTimeDontRetry");
            p.delete();
            var f = FileSqlFactory.INSTANCE.prepare(m);
            createFile(p, 13);
            testOk(f, 13);
            p.delete();
            testOk(f, 13);
        } finally {
            p.delete();
        }
    }

    @Test
    public void testOnFirstTimeDontRetryError() throws Exception {
        var a = "./test-files/lorem-4.txt";
        var p = absolute(a).toFile();
        try {
            var m = mtd("withSqlOnFirstTimeDontRetry");
            p.delete();
            var f = FileSqlFactory.INSTANCE.prepare(m);
            p.delete();
            testFails(f, a);
            createFile(p, 13);
            testFails(f, a);
            p.delete();
            testFails(f, a);
        } finally {
            p.delete();
        }
    }
}
