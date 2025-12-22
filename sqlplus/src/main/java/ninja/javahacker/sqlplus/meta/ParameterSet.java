package ninja.javahacker.sqlplus.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.NonNull;
import ninja.javahacker.magicfactory.ConstructionException;
import ninja.javahacker.sqlplus.Flat;
import ninja.javahacker.sqlplus.stmt.NamedParameterStatement;
import ninja.javahacker.sqlplus.stmt.SmartResultSet;

public final class ParameterSet {

    @NonNull
    private final Map<String, SqlNamedParameter> parameters;

    public ParameterSet(@NonNull Map<String, SqlNamedParameter> parameters) {
        this.parameters = Map.copyOf(parameters);
        this.parameters.forEach((k, v) -> v.check(k));
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + parameters + "]";
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof ParameterSet ps && Objects.equals(this.parameters, ps.parameters);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    public static final class ParameterSetWithValues {

        @NonNull
        private final Set<Map.Entry<String, SqlNamedParameter.SqlNamedParameterWithValue>> parameters;

        @NonNull
        public ParameterSetWithValues(@NonNull Map<String, SqlNamedParameter.SqlNamedParameterWithValue> parameters) {
            this.parameters = Map.copyOf(parameters).entrySet();
        }

        @NonNull
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[" + parameters + "]";
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof ParameterSetWithValues ps && Objects.equals(this.parameters, ps.parameters);
        }

        @Override
        public int hashCode() {
            return parameters.hashCode();
        }

        @NonNull
        public void preencher(@NonNull NamedParameterStatement ps) throws SQLException {
            for (var p : parameters) {
                p.getValue().handle(ps, p.getKey());
            }
        }

        @NonNull
        private static int[] defaultRange(@NonNull Class<?> k) {
            return IntStream.rangeClosed(1, k.isRecord() ? k.getRecordComponents().length : 1).toArray();
        }

        @NonNull
        private <R extends Record> Optional<R> lerRecord(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql,
                @NonNull int... campos
        ) throws SQLException, ConstructionException
        {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql)) {
                this.preencher(ps);
                try (var rs = new SmartResultSet(ps.executeQuery())) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(rs.getRecord(k, campos));
                }
            }
        }

        @NonNull
        private <R> Optional<R> lerSimples(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql,
                int campo
        ) throws SQLException, ConstructionException
        {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql)) {
                this.preencher(ps);
                try (var rs = new SmartResultSet(ps.executeQuery())) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(k.cast(rs.getTypedValue(campo)));
                }
            }
        }

        @NonNull
        public <R> Optional<R> ler(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql,
                @NonNull int... campos
        ) throws SQLException, ConstructionException
        {
            var camposFinal = campos.length == 0 ? defaultRange(k) : campos;
            if (k.isRecord()) return lerRecord(con, k.asSubclass(Record.class), sql, camposFinal).map(k::cast);
            if (camposFinal.length != 1) throw new UnsupportedOperationException();
            return lerSimples(con, k, sql, camposFinal[0]);
        }

        @NonNull
        public <R> Optional<R> ler(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql
        ) throws SQLException, ConstructionException
        {
            return ler(con, k, sql, defaultRange(k));
        }

        @NonNull
        private <R extends Record> List<R> listarRecord(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql,
                @NonNull int... campos
        ) throws SQLException, ConstructionException
        {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql)) {
                List<R> t = new ArrayList<>(10);
                this.preencher(ps);
                try (var rs = new SmartResultSet(ps.executeQuery())) {
                    while (rs.next()) {
                        t.add(rs.getRecord(k, campos));
                    }
                    return t;
                }
            }
        }

        @NonNull
        private <R> List<R> listarSimples(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql,
                int campo
        ) throws SQLException, ConstructionException
        {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql)) {
                this.preencher(ps);
                try (var rs = new SmartResultSet(ps.executeQuery())) {
                    List<R> t = new ArrayList<>(10);
                    while (rs.next()) {
                        t.add(k.cast(rs.getTypedValue(campo)));
                    }
                    return t;
                }
            }
        }

        @NonNull
        @SuppressWarnings("unchecked")
        public <R> List<R> listar(
                @NonNull Connection con,
                @NonNull Class<R> k,
                @NonNull String sql,
                @NonNull int... campos
        ) throws SQLException, ConstructionException
        {
            var camposFinal = campos.length == 0 ? defaultRange(k) : campos;
            if (k.isRecord()) return (List<R>) listarRecord(con, k.asSubclass(Record.class), sql, camposFinal);
            if (camposFinal.length != 1) throw new UnsupportedOperationException();
            return listarSimples(con, k, sql, camposFinal[0]);
        }

        @NonNull
        public <R> List<R> listar(@NonNull Connection con, @NonNull Class<R> k, @NonNull String sql)
                throws SQLException, ConstructionException
        {
            return listar(con, k, sql, defaultRange(k));
        }

        public long executar(@NonNull Connection con, @NonNull String sql) throws SQLException {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql)) {
                this.preencher(ps);
                return ps.executeLargeUpdate();
            }
        }

        @NonNull
        public OptionalInt gerar(@NonNull Connection con, @NonNull String sql) throws SQLException {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql, Statement.RETURN_GENERATED_KEYS)) {
                this.preencher(ps);
                var qtd = ps.executeLargeUpdate();
                if (qtd > 1L) throw new SQLException("Mais de um resultado.");
                try (var rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) return OptionalInt.empty();
                    return OptionalInt.of(rs.getInt(1));
                }
            }
        }

        @NonNull
        public List<Integer> gerarLista(@NonNull Connection con, @NonNull String sql) throws SQLException {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql, Statement.RETURN_GENERATED_KEYS)) {
                this.preencher(ps);
                var qtd = ps.executeUpdate();
                List<Integer> r = new ArrayList<>(qtd);
                try (var rs = ps.getGeneratedKeys()) {
                    while (rs.next()) {
                        r.add(rs.getInt(1));
                    }
                    return r;
                }
            }
        }

        @NonNull
        public OptionalLong gerarLong(@NonNull Connection con, @NonNull String sql) throws SQLException {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql, Statement.RETURN_GENERATED_KEYS)) {
                this.preencher(ps);
                var qtd = ps.executeLargeUpdate();
                if (qtd > 1L) throw new SQLException("Mais de um resultado.");
                try (var rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) return OptionalLong.empty();
                    return OptionalLong.of(rs.getLong(1));
                }
            }
        }

        @NonNull
        public List<Long> gerarListaLong(@NonNull Connection con, @NonNull String sql) throws SQLException {
            try (var ps = NamedParameterStatement.prepareNamedStatement(con, sql, Statement.RETURN_GENERATED_KEYS)) {
                this.preencher(ps);
                var qtd = ps.executeUpdate();
                try (var rs = ps.getGeneratedKeys()) {
                    List<Long> r = new ArrayList<>(qtd);
                    while (rs.next()) {
                        r.add(rs.getLong(1));
                    }
                    return r;
                }
            }
        }
    }

    @NonNull
    public ParameterSetWithValues associar(@NonNull Map<String, Object> valores) {
        for (var k : valores.keySet()) {
            if (!parameters.containsKey(k)) throw new IllegalArgumentException();
        }
        var m = new HashMap<String, SqlNamedParameter.SqlNamedParameterWithValue>(parameters.size());
        for (var e : parameters.entrySet()) {
            var k = e.getKey();
            var v = e.getValue();
            m.put(k, v.withValue(valores.get(k)));
        }
        return new ParameterSetWithValues(m);
    }

    @NonNull
    public static MethodParameterSet parameters(@NonNull ParsedQuery pq, @NonNull Method m) {
        return new MethodParameterSet(pq, m);
    }

    public static final class MethodParameterSet {
        @Getter
        private final ParsedQuery query;

        @Getter
        private final Method method;

        @Getter
        private final ParameterSet parameters;

        public MethodParameterSet(@NonNull ParsedQuery query, @NonNull Method m) {
            this.query = query;
            var pp = m.getParameters();
            var count = pp.length;
            Map<String, SqlNamedParameter> ret = new HashMap<>(count);
            for (var p : pp) {
                ret.put(p.getName(), new SqlNamedParameter(p.getParameterizedType(), p.isAnnotationPresent(Flat.class)));
            }
            this.method = m;
            this.parameters = new ParameterSet(ret);
            for (var p : pp) {
                if (!parameters.parameters.containsKey(p.getName())) throw new IllegalArgumentException();
            }
            var map = new HashMap<String, SqlNamedParameter.SqlNamedParameterWithValue>(count);
            for (var i = 0; i < count; i++) {
                var p = pp[i];
                var ss = new SqlNamedParameter.SqlNamedParameterWithValue(
                        p.getParameterizedType(),
                        null,
                        p.isAnnotationPresent(Flat.class)
                );
                map.put(p.getName(), ss);
            }
        }

        @NonNull
        public ParameterSetWithValues associar(@NonNull Object... args) {
            var pp = method.getParameters();
            if (args.length != pp.length) throw new IllegalArgumentException();
            var map = new HashMap<String, SqlNamedParameter.SqlNamedParameterWithValue>(args.length);
            for (var i = 0; i < args.length; i++) {
                var p = pp[i];
                var ss = new SqlNamedParameter.SqlNamedParameterWithValue(
                        p.getParameterizedType(),
                        args[i],
                        p.isAnnotationPresent(Flat.class)
                );
                map.put(p.getName(), ss);
            }
            return new ParameterSetWithValues(map);
        }
    }
}
