package ninja.javahacker.test.annotimpler.sql.stmt;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class SetNullParameterSatatementTest {

    @FunctionalInterface
    private interface ConnectionContext {
        public void doIt(Connection con) throws Exception;

        public default void onConnection() throws Exception {
            try (var con = H2Connector.std().withMemory(true).get()) {
                this.doIt(con);
            }
        }

        public default org.junit.jupiter.api.function.Executable wrap() {
            return this::onConnection;
        }
    }

    @FunctionalInterface
    private interface StatementContext {
        public void doIt(NamedParameterStatement con) throws Exception;
    }

    @Test
    public void testSetNull() throws Exception {
        var idx = Map.of("bar", List.of(1));
        var prepare = List.of(
                "CREATE TABLE foo(pk INT PRIMARY KEY, blah VARCHAR(4));",
                "INSERT INTO foo(pk, blah) VALUES (1, 'whoa');"
        );
        var sql = "INSERT INTO foo(pk, blah) VALUES ($X$, ?);";
        var select = "SELECT pk, blah FROM foo WHERE pk = 2";
        List<StatementContext> recvs = List.of(
                ps -> ps.setNull("bar", Types.VARCHAR),
                ps -> ps.setNull(1, Types.VARCHAR),
                ps -> ps.setNull("bar", Types.VARCHAR, String.class.getName()),
                ps -> ps.setNull(1, Types.VARCHAR, String.class.getName()),
                ps -> ps.setInt("bar", OptionalInt.empty()),
                ps -> ps.setInt(1, OptionalInt.empty()),
                ps -> ps.setLong("bar", OptionalLong.empty()),
                ps -> ps.setLong(1, OptionalLong.empty()),
                ps -> ps.setString("bar", null),
                ps -> ps.setString(1, null)
        );
        ConnectionContext ctx = con -> {
            for (var sqlp : prepare) {
                try (var ps = con.prepareStatement(sqlp)) {
                    ps.executeUpdate();
                }
            }
            var m = new int[] {2};
            var n = new int[] {2};
            recvs.forEach(recv -> {
                var sqln = sql.replace("$X$", "" + m[0]);
                m[0]++;
                try (var ps = NamedParameterStatement.prepareNamedStatement(con, sqln, idx)) {
                    recv.doIt(ps);
                    ps.executeUpdate();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });
            try (var ps = con.prepareStatement(select)) {
                try (var rs = ps.executeQuery()) {
                    Assertions.assertTrue(rs.next(), "Assert has 1st line");
                    Assertions.assertEquals(n[0], rs.getInt("pk"));
                    Assertions.assertEquals(n[0], rs.getInt(1));
                    Assertions.assertEquals(null, rs.getString("blah"));
                    Assertions.assertEquals(null, rs.getString(2));
                    Assertions.assertFalse(rs.next(), "Assert had only one line");
                    n[0]++;
                }
            }
        };
        ctx.onConnection();
    }
}
