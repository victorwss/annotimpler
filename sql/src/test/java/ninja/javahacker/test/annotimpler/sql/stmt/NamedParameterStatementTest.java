package ninja.javahacker.test.annotimpler.sql.stmt;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class NamedParameterStatementTest {

    @FunctionalInterface
    public interface ConnectionContext {
        public void doIt(Connection con) throws Exception;
    }

    private void withConnection(ConnectionContext recv) throws Exception {
        try (var con = SqliteMemoryConnector.std().get()) {
            try (var ps = con.prepareStatement("CREATE TABLE foo(pk INT PRIMARY KEY, blah TEXT);")) {
                ps.executeUpdate();
            }
            try (var ps = con.prepareStatement("INSERT INTO foo(pk, blah) VALUES (1, 'whoa');")) {
                ps.executeUpdate();
            }
            try (var ps = con.prepareStatement("INSERT INTO foo(pk, blah) VALUES (2, 'lol');")) {
                ps.executeUpdate();
            }
            recv.doIt(con);
        }
    }

    @Test
    public void testBasic() throws Exception {
        var idx = Map.of("bar", List.of(1));
        withConnection(con -> {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, "SELECT pk, blah FROM foo WHERE pk = :bar", idx)) {
                ps.setInt("bar", 2);
                try (var rs = ps.executeQuery()) {
                    rs.next();
                    Assertions.assertEquals(2, rs.getInt("pk"));
                    Assertions.assertEquals(2, rs.getInt(1));
                    Assertions.assertEquals("lol", rs.getString("blah"));
                    Assertions.assertEquals("lol", rs.getString(2));
                }
            }
        });
    }
}
