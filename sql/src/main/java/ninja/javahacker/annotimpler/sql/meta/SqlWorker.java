package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public final class SqlWorker {
    @NonNull
    private final Connection con;

    @NonNull
    private final ParameterSet.ParameterSetWithValues args;

    public SqlWorker(@NonNull Connection con, @NonNull ParameterSet.ParameterSetWithValues args) {
        this.con = con;
        this.args = args;
    }

    @NonNull
    private static int[] defaultRange(@NonNull Class<?> k) {
        return IntStream.rangeClosed(1, k.isRecord() ? k.getRecordComponents().length : 1).toArray();
    }

    private NamedParameterStatement open() throws SQLException {
        return NamedParameterStatement.prepareNamedStatement(con, args.getQuery().parsed(), args.getQuery().params());
    }

    private NamedParameterStatement openGenerate() throws SQLException {
        return NamedParameterStatement
                .prepareNamedStatement(con, args.getQuery().parsed(), args.getQuery().params(), Statement.RETURN_GENERATED_KEYS);
    }

    @NonNull
    private <R extends Record> Optional<R> lerRecord(@NonNull Class<R> k, @NonNull int... campos)
            throws SQLException, ConstructionException
    {
        try (var ps = open()) {
            args.preencher(ps);
            try (var rs = new SmartResultSet(ps.executeQuery())) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getRecord(k, campos));
            }
        }
    }

    @NonNull
    private <R> Optional<R> lerSimples(@NonNull Class<R> k, int campo) throws SQLException, ConstructionException {
        try (var ps = open()) {
            args.preencher(ps);
            try (var rs = new SmartResultSet(ps.executeQuery())) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getTypedValue(campo, k));
            }
        }
    }

    @NonNull
    public <R> Optional<R> ler(@NonNull Class<R> k, @NonNull int... campos) throws SQLException, ConstructionException {
        var camposFinal = campos.length == 0 ? defaultRange(k) : campos;
        if (k.isRecord()) return lerRecord(k.asSubclass(Record.class), camposFinal).map(k::cast);
        if (camposFinal.length != 1) throw new UnsupportedOperationException();
        return lerSimples(k, camposFinal[0]);
    }

    @NonNull
    public <R> Optional<R> ler(@NonNull Class<R> k) throws SQLException, ConstructionException {
        return ler(k, defaultRange(k));
    }

    @NonNull
    private <R extends Record> List<R> listarRecord(@NonNull Class<R> k, @NonNull int... campos)
            throws SQLException, ConstructionException
    {
        try (var ps = open()) {
            List<R> t = new ArrayList<>(10);
            args.preencher(ps);
            try (var rs = new SmartResultSet(ps.executeQuery())) {
                while (rs.next()) {
                    t.add(rs.getRecord(k, campos));
                }
                return t;
            }
        }
    }

    @NonNull
    private <R> List<R> listarSimples(@NonNull Class<R> k, int campo) throws SQLException, ConstructionException {
        try (var ps = open()) {
            args.preencher(ps);
            try (var rs = new SmartResultSet(ps.executeQuery())) {
                List<R> t = new ArrayList<>(10);
                while (rs.next()) {
                    t.add(rs.getTypedValue(campo, k));
                }
                return t;
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <R> List<R> listar(@NonNull Class<R> k, @NonNull int... campos) throws SQLException, ConstructionException {
        var camposFinal = campos.length == 0 ? defaultRange(k) : campos;
        if (k.isRecord()) return (List<R>) listarRecord(k.asSubclass(Record.class), camposFinal);
        if (camposFinal.length != 1) throw new UnsupportedOperationException();
        return listarSimples(k, camposFinal[0]);
    }

    @NonNull
    public <R> List<R> listar(@NonNull Class<R> k) throws SQLException, ConstructionException {
        return listar(k, defaultRange(k));
    }

    public long executar() throws SQLException {
        try (var ps = open()) {
            args.preencher(ps);
            return ps.executeLargeUpdate();
        }
    }

    @NonNull
    public OptionalInt gerar() throws SQLException {
        try (var ps = openGenerate()) {
            args.preencher(ps);
            var qtd = ps.executeLargeUpdate();
            if (qtd > 1L) throw new SQLException("Mais de um resultado.");
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) return OptionalInt.empty();
                return OptionalInt.of(rs.getInt(1));
            }
        }
    }

    @NonNull
    public List<Integer> gerarLista() throws SQLException {
        try (var ps = openGenerate()) {
            args.preencher(ps);
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
    public OptionalLong gerarLong() throws SQLException {
        try (var ps = openGenerate()) {
            args.preencher(ps);
            var qtd = ps.executeLargeUpdate();
            if (qtd > 1L) throw new SQLException("Mais de um resultado.");
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) return OptionalLong.empty();
                return OptionalLong.of(rs.getLong(1));
            }
        }
    }

    @NonNull
    public List<Long> gerarListaLong() throws SQLException {
        try (var ps = openGenerate()) {
            args.preencher(ps);
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
