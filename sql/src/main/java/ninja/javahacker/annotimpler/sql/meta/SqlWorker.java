package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

public final class SqlWorker {

    @NonNull
    private final Connection con;

    @NonNull
    private final ParameterSet.ParameterSetWithValues ppq;

    @NonNull
    private final ConverterFactory factory;

    @NonNull
    private final Locale localizer;

    public SqlWorker(
            @NonNull Connection con,
            @NonNull ParameterSet.ParameterSetWithValues ppq,
            @NonNull ConverterFactory factory,
            @NonNull Locale localizer)
    {
        this.con = con;
        this.ppq = ppq;
        this.factory = factory;
        this.localizer = localizer;
    }

    @NonNull
    private static int[] defaultRange(@NonNull Class<?> k) {
        checkNotNull(k);
        return IntStream.rangeClosed(1, k.isRecord() ? k.getRecordComponents().length : 1).toArray();
    }

    @NonNull
    private NamedParameterStatement open() throws SQLException {
        var ps = con.prepareStatement(ppq.parsed());
        return NamedParameterStatement.wrap(ps, ppq.params());
    }

    @NonNull
    private NamedParameterStatement openGenerate() throws SQLException {
        var ps = con.prepareStatement(ppq.parsed(), Statement.RETURN_GENERATED_KEYS);
        return NamedParameterStatement.wrap(ps, ppq.params());
    }

    @NonNull
    private <R extends Record> Optional<R> readRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        checkNotNull(k);
        checkNotNull(fields);

        try (var ps = open()) {
            ppq.fillIn(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getRecord(k, fields));
            }
        }
    }

    @NonNull
    private <R> Optional<R> readSimple(@NonNull Class<R> k, int field) throws SQLException {
        checkNotNull(k);

        try (var ps = open()) {
            ppq.fillIn(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getTypedValue(field, k));
            }
        }
    }

    @NonNull
    public <R> Optional<R> read(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        var fieldsFinal = fields.length == 0 ? defaultRange(k) : fields;
        if (k.isRecord()) return readRecord(k.asSubclass(Record.class), fieldsFinal).map(k::cast);
        if (fieldsFinal.length != 1) throw new UnsupportedOperationException();
        return readSimple(k, fieldsFinal[0]);
    }

    @NonNull
    public <R> Optional<R> read(@NonNull Class<R> k) throws SQLException {
        return read(k, defaultRange(k));
    }

    @NonNull
    private <R extends Record> List<R> listRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        checkNotNull(k);
        checkNotNull(fields);

        try (var ps = open()) {
            List<R> t = new ArrayList<>(10);
            ppq.fillIn(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                while (rs.next()) {
                    t.add(rs.getRecord(k, fields));
                }
                return t;
            }
        }
    }

    @NonNull
    private <R> List<R> listSimple(@NonNull Class<R> k, int field) throws SQLException {
        checkNotNull(k);

        try (var ps = open()) {
            ppq.fillIn(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                List<R> t = new ArrayList<>(10);
                while (rs.next()) {
                    t.add(rs.getTypedValue(field, k));
                }
                return t;
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <R> List<R> list(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        var fieldsFinal = fields.length == 0 ? defaultRange(k) : fields;
        if (k.isRecord()) return (List<R>) listRecord(k.asSubclass(Record.class), fieldsFinal);
        if (fieldsFinal.length != 1) throw new UnsupportedOperationException();
        return listSimple(k, fieldsFinal[0]);
    }

    @NonNull
    public <R> List<R> list(@NonNull Class<R> k) throws SQLException {
        return list(k, defaultRange(k));
    }

    public long execute() throws SQLException {
        try (var ps = open()) {
            ppq.fillIn(ps);
            return ps.executeLargeUpdate();
        }
    }

    @NonNull
    public OptionalInt generate() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.fillIn(ps);
            var qtd = ps.executeLargeUpdate();
            if (qtd > 1L) throw new SQLException("More than one result.");
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) return OptionalInt.empty();
                return OptionalInt.of(rs.getInt(1));
            }
        }
    }

    @NonNull
    public List<Integer> generateList() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.fillIn(ps);
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
    public OptionalLong generateLong() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.fillIn(ps);
            var qtd = ps.executeLargeUpdate();
            if (qtd > 1L) throw new SQLException("More than one result.");
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) return OptionalLong.empty();
                return OptionalLong.of(rs.getLong(1));
            }
        }
    }

    @NonNull
    public List<Long> generateLongList() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.fillIn(ps);
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

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
