package ninja.javahacker.test.annotimpler.sql.conn;

import ninja.javahacker.test.NamedTest;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

public class ConnectionStartConfigTest {

    private static NamedTest n(String name, Executable ctx) {
        return new NamedTest(name, ctx);
    }

    private static record Config(Supplier<? extends Connector> conn, int port, String key) {
    }

    private static List<Config> stds() {
        return List.of(
                new Config(AccessConnector::std, 0, "access"),
                new Config(Db2Connector::std, 50000, "db2"),
                new Config(FirebirdConnector::std, 3050, "firebird"),
                new Config(H2Connector::std, 0, "h2"),
                new Config(HsqldbConnector::std, 0, "hsqldb"),
                new Config(MariaDbConnector::std, 3306, "mariadb"),
                new Config(MySqlConnector::std, 3306, "mysql"),
                new Config(OracleConnector::std, 1521, "oracle"),
                new Config(PostgreSqlConnector::std, 5432, "postgresql"),
                new Config(SqlServerConnector::std, 1433, "sqlserver"),
                new Config(SqliteConnector::std, 0, "sqlite"),
                new Config(SqliteMemoryConnector::std, 0, "sqlite-memory")
        );
    }

    private static Arguments testSame(Config x) {
        return n(x.key(), () -> Assertions.assertSame(x.conn(), x.conn())).args();
    }

    private static Stream<Arguments> stdFixedTest() {
        return stds().stream().map(ConnectionStartConfigTest::testSame);
    }

    @MethodSource
    @ParameterizedTest(name = "stdFixedTest {0}")
    public void stdFixedTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Arguments testPort(Config x) {
        return n(x.key(), () -> {
            var conn = x.conn().get();
            var port = (Integer) conn.getClass().getMethod("port").invoke(conn);
            Assertions.assertEquals(x.port(), port);
        }).args();
    }

    private static Stream<Arguments> stdPortTest() {
        return stds().stream().filter(e -> e.port() != 0).map(ConnectionStartConfigTest::testPort);
    }

    @MethodSource
    @ParameterizedTest(name = "stdPortTest {0}")
    public void stdPortTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }

    private static Arguments testString(Config x) {
        return n(x.key(), () -> {
            var key = x.conn().get().getClass().getAnnotation(ConnectorJsonKey.class).value();
            Assertions.assertEquals(x.key(), key);
        }).args();
    }

    private static Stream<Arguments> stdStringTest() {
        return stds().stream().map(ConnectionStartConfigTest::testString);
    }

    @MethodSource
    @ParameterizedTest(name = "stdPortTest {0}")
    public void stdStringTest(String name, Executable exec) throws Throwable {
        exec.execute();
    }
}
