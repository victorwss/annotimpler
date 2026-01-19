package ninja.javahacker.test.annotimpler.sql.conn;

import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class ConnectionStartConfigTest {

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

    private static Executable testSame(Config x) {
        return () -> Assertions.assertSame(x.conn(), x.conn());
    }

    @Test
    public void stdFixedTest() {
        Assertions.assertAll(stds().stream().map(ConnectionStartConfigTest::testSame));
    }

    private static Executable testPort(Config x) {
        return () -> {
            var conn = x.conn().get();
            var port = (Integer) conn.getClass().getMethod("port").invoke(conn);
            Assertions.assertEquals(x.port(), port);
        };
    }

    @Test
    public void stdPortTest() {
        Assertions.assertAll(stds().stream().filter(e -> e.port() != 0).map(ConnectionStartConfigTest::testPort));
    }

    private static Executable testString(Config x) {
        return () -> {
            var key = x.conn().get().getClass().getAnnotation(ConnectorJsonKey.class).value();
            Assertions.assertEquals(x.key(), key);
        };
    }

    @Test
    public void stdStringTest() {
        Assertions.assertAll(stds().stream().map(ConnectionStartConfigTest::testString));
    }
}
