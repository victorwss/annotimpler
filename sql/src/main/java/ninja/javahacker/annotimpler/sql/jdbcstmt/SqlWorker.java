package ninja.javahacker.annotimpler.sql.jdbcstmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.sql;

/// Orchestrates the execution of a single SQL operation against a JDBC [Connection].
///
/// A [SqlWorker] prepares a [NamedParameterStatement] from a [ParsedQuery], binds the
/// parameters supplied by a [ParameterReceiver.Acceptor2], and returns the results mapped to
/// the requested Java type via a [ConverterFactory].
///
/// Instances are **not** designed to be reusable across invocations and can only be reused in a few corner cases;
/// it is much easier to just create a fresh [SqlWorker] for each SQL operation instead.
public final class SqlWorker {

    /// The JDBC connection to prepare the statement on.
    @NonNull
    private final Connection con;

    /// The already-bound parameter acceptor used to bind parameter values onto the prepared statement.
    @NonNull
    private final ParameterReceiver.Acceptor2 ppq;

    /// The parsed SQL query to execute.
    @NonNull
    private final ParsedQuery pq;

    /// The converter factory used to map result column values to Java types.
    @NonNull
    private final ConverterFactory factory;

    /// The locale used for case-insensitive column name matching.
    @NonNull
    private final Locale localizer;

    /// Creates a new [SqlWorker] for executing one SQL operation.
    ///
    /// @param con The JDBC connection to use.
    /// @param ppq The already-bound parameter acceptor produced by `ParameterSet.withValues(...)`.
    /// @param pq The parsed SQL query, typically produced by `ParsedQuery.parse(...)`.
    /// @param factory The converter factory used to map result column values to Java types.
    /// @param localizer The locale used for case-insensitive column name matching.
    /// @throws IllegalArgumentException If any parameter is `null`.
    @SuppressFBWarnings("EI_EXPOSE_REP2") // SpotBugs don't like wrapping the connection, but there is no problem at all.
    public SqlWorker(
            @NonNull Connection con,
            @NonNull ParameterReceiver.Acceptor2 ppq,
            @NonNull ParsedQuery pq,
            @NonNull ConverterFactory factory,
            @NonNull Locale localizer)
    {
        this.con = con;
        this.ppq = ppq;
        this.pq = pq;
        this.factory = factory;
        this.localizer = localizer;
    }

    @NonNull
    private static int[] defaultRange(@NonNull Class<?> k) {
        checkNotNull(k); // Check recognized by lombok.
        return IntStream.rangeClosed(1, k.isRecord() ? k.getRecordComponents().length : 1).toArray();
    }

    @NonNull
    @SuppressFBWarnings({ // We are intentionally trusting ParsedQuery, but SpotBugs don't know that.
        "SQL_INJECTION_JDBC",
        "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING"
    })
    private NamedParameterStatement open() throws SQLException {
        var ps = con.prepareStatement(pq.parsed());
        return NamedParameterStatement.wrap(ps, pq.params());
    }

    @NonNull
    @SuppressFBWarnings({ // We are intentionally trusting ParsedQuery, but SpotBugs don't know that.
        "SQL_INJECTION_JDBC",
        "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING"
    })
    private NamedParameterStatement openGenerate() throws SQLException {
        var ps = con.prepareStatement(pq.parsed(), Statement.RETURN_GENERATED_KEYS);
        return NamedParameterStatement.wrap(ps, pq.params());
    }

    @NonNull
    private <R extends Record> Optional<R> readRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(fields); // Check recognized by lombok.

        try (var ps = open()) {
            ppq.accept(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getRecord(k, fields));
            }
        }
    }

    @NonNull
    private <R> Optional<R> readSimple(@NonNull Class<R> k, int field) throws SQLException {
        checkNotNull(k); // Check recognized by lombok.

        try (var ps = open()) {
            ppq.accept(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getTypedValue(field, k));
            }
        }
    }

    /// Executes a `SELECT` statement and returns the first row mapped to `R`, or
    /// [Optional#empty()] if the result set is empty.
    ///
    /// When `fields` is empty the method defaults to columns `1..N` for record types, or
    /// column `1` for scalar types.  When `R` is a `record`, `fields[i]` is the column index
    /// mapped to the *i*-th record component.
    ///
    /// @param <R> The target Java type.
    /// @param k The class of the target type.
    /// @param fields The 1-based column indices to map; may be empty to use the default range.
    /// @return An [Optional] containing the first row mapped to `R`, or
    ///         [Optional#empty()] if the result set is empty.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    public <R> Optional<R> read(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        var fieldsFinal = fields.length == 0 ? defaultRange(k) : fields;
        if (k.isRecord()) return readRecord(k.asSubclass(Record.class), fieldsFinal).map(k::cast);
        if (fieldsFinal.length != 1) throw new UnsupportedOperationException();
        return readSimple(k, fieldsFinal[0]);
    }

    /// Executes a `SELECT` statement and returns the first row mapped to `R`, using the
    /// default column range.
    ///
    /// Equivalent to calling `read(k, defaultRange(k))`.
    ///
    /// @param <R> The target Java type.
    /// @param k The class of the target type.
    /// @return An [Optional] containing the first row mapped to `R`, or
    ///         [Optional#empty()] if the result set is empty.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    public <R> Optional<R> read(@NonNull Class<R> k) throws SQLException {
        return read(k, defaultRange(k));
    }

    @NonNull
    private <R extends Record> List<R> listRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(fields); // Check recognized by lombok.

        try (var ps = open()) {
            List<R> t = new ArrayList<>(10);
            ppq.accept(ps);
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
        checkNotNull(k); // Check recognized by lombok.

        try (var ps = open()) {
            ppq.accept(ps);
            try (var rs = new SmartResultSet(ps.executeQuery(), factory, localizer)) {
                List<R> t = new ArrayList<>(10);
                while (rs.next()) {
                    t.add(rs.getTypedValue(field, k));
                }
                return t;
            }
        }
    }

    /// Executes a `SELECT` statement and returns all rows mapped to `R`.
    ///
    /// The `fields` and `R` semantics are identical to [#read(Class, int...)].
    ///
    /// @param <R> The target Java type.
    /// @param k The class of the target type.
    /// @param fields The 1-based column indices to map; may be empty to use the default range.
    /// @return A [List] of all rows mapped to `R`; empty if the result set is empty.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    @SuppressWarnings("unchecked")
    public <R> List<R> list(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        var fieldsFinal = fields.length == 0 ? defaultRange(k) : fields;
        if (k.isRecord()) return (List<R>) listRecord(k.asSubclass(Record.class), fieldsFinal);
        if (fieldsFinal.length != 1) throw new UnsupportedOperationException();
        return listSimple(k, fieldsFinal[0]);
    }

    /// Executes a `SELECT` statement and returns all rows mapped to `R`, using the default
    /// column range.
    ///
    /// Equivalent to calling `list(k, defaultRange(k))`.
    ///
    /// @param <R> The target Java type.
    /// @param k The class of the target type.
    /// @return A [List] of all rows mapped to `R`; empty if the result set is empty.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    public <R> List<R> list(@NonNull Class<R> k) throws SQLException {
        return list(k, defaultRange(k));
    }

    /// Executes a DML statement (`INSERT`, `UPDATE`, or `DELETE`) and returns the number of
    /// affected rows.
    ///
    /// @return The number of rows affected by the statement.
    /// @throws SQLException If a database access error occurs.
    public long execute() throws SQLException {
        try (var ps = open()) {
            ppq.accept(ps);
            return ps.executeLargeUpdate();
        }
    }

    /// Executes an `INSERT` statement and returns the first auto-generated key as an
    /// [OptionalInt].
    ///
    /// Returns [OptionalInt#empty()] if no key was generated. Throws
    /// [SQLException] if more than one row is affected.
    ///
    /// @return An [OptionalInt] containing the generated key, or
    ///         [OptionalInt#empty()] if no key was generated.
    /// @throws SQLException If a database access error occurs or more than one row is affected.
    /// @see #generate()
    /// @see #generateOrNull()
    /// @see #generateList()
    /// @see #generateOptionalLong()
    /// @see #generateLong()
    /// @see #generateLongOrNull()
    /// @see #generateLongList()
    @NonNull
    public OptionalInt generateOptional() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.accept(ps);
            var qtd = ps.executeLargeUpdate();
            if (qtd > 1L) throw new SQLException("More than one result.");
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) return OptionalInt.empty();
                return OptionalInt.of(rs.getInt(1));
            }
        }
    }

    /// Executes an `INSERT` statement and returns the first auto-generated key as an `int`.
    ///
    /// Throws [SQLException] if more than one row is affected or no key was generated.
    ///
    /// @return The generated key.
    /// @throws SQLException If a database access error occurs, more than one row is
    ///         affected or no key was generated.
    /// @see #generateOptional()
    /// @see #generateOrNull()
    /// @see #generateList()
    /// @see #generateOptionalLong()
    /// @see #generateLong()
    /// @see #generateLongOrNull()
    /// @see #generateLongList()
    @NonNull
    public int generate() throws SQLException {
        try {
            return generateOptional().getAsInt();
        } catch (NoSuchElementException ex) {
            throw new SQLException(ex);
        }
    }

    /// Executes an `INSERT` statement and returns the first auto-generated key as an [Integer].
    ///
    /// Returns `null` if no key was generated. Throws
    /// [SQLException] if more than one row is affected.
    ///
    /// @return The generated key, or `null` if no key was generated.
    /// @throws SQLException If a database access error occurs or more than one row is affected.
    /// @see #generateOptional()
    /// @see #generate()
    /// @see #generateList()
    /// @see #generateOptionalLong()
    /// @see #generateLong()
    /// @see #generateLongOrNull()
    /// @see #generateLongList()
    @Nullable
    public Integer generateOrNull() throws SQLException {
        return getOrNull(generateOptional());
    }

    /// Executes an `INSERT` statement and returns all auto-generated keys as a
    /// [List] of [Integer].
    ///
    /// @return A [List] of all generated keys; empty if none were generated.
    /// @throws SQLException If a database access error occurs.
    /// @see #generateOptional()
    /// @see #generate()
    /// @see #generateOrNull()
    /// @see #generateOptionalLong()
    /// @see #generateLong()
    /// @see #generateLongOrNull()
    /// @see #generateLongList()
    @NonNull
    public List<Integer> generateList() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.accept(ps);
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

    /// Executes an `INSERT` statement and returns the first auto-generated key as an
    /// [OptionalLong].
    ///
    /// Returns [OptionalLong#empty()] if no key was generated. Throws
    /// [SQLException] if more than one row is affected.
    ///
    /// @return An [OptionalLong] containing the generated key, or
    ///         [OptionalLong#empty()] if no key was generated.
    /// @throws SQLException If a database access error occurs or more than one row is affected.
    /// @see #generateOptional()
    /// @see #generate()
    /// @see #generateOrNull()
    /// @see #generateList()
    /// @see #generateLong()
    /// @see #generateLongOrNull()
    /// @see #generateLongList()
    @NonNull
    public OptionalLong generateOptionalLong() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.accept(ps);
            var qtd = ps.executeLargeUpdate();
            if (qtd > 1L) throw new SQLException("More than one result.");
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) return OptionalLong.empty();
                return OptionalLong.of(rs.getLong(1));
            }
        }
    }

    /// Executes an `INSERT` statement and returns the first auto-generated key as a `long`.
    ///
    /// Throws [SQLException] if more than one row is affected or no key was generated.
    ///
    /// @return The generated key.
    /// @throws SQLException If a database access error occurs, more than one row is
    ///         affected or no key was generated.
    /// @see #generateOptional()
    /// @see #generate()
    /// @see #generateOrNull()
    /// @see #generateList()
    /// @see #generateOptionalLong()
    /// @see #generateLongOrNull()
    /// @see #generateLongList()
    public long generateLong() throws SQLException {
        try {
            return generateOptionalLong().getAsLong();
        } catch (NoSuchElementException ex) {
            throw new SQLException(ex);
        }
    }

    /// Executes an `INSERT` statement and returns the first auto-generated key as a [Long].
    ///
    /// Returns `null` if no key was generated. Throws
    /// [SQLException] if more than one row is affected.
    ///
    /// @return The generated key, or `null` if no key was generated.
    /// @throws SQLException If a database access error occurs or more than one row is affected.
    /// @see #generateOptional()
    /// @see #generate()
    /// @see #generateOrNull()
    /// @see #generateList()
    /// @see #generateOptionalLong()
    /// @see #generateLong()
    /// @see #generateLongList()
    @Nullable
    public Long generateLongOrNull() throws SQLException {
        return getOrNull(generateOptionalLong());
    }

    /// Executes an `INSERT` statement and returns all auto-generated keys as a
    /// [List] of [Long].
    ///
    /// @return A [List] of all generated keys; empty if none were generated.
    /// @throws SQLException If a database access error occurs.
    /// @see #generateOptional()
    /// @see #generate()
    /// @see #generateOrNull()
    /// @see #generateList()
    /// @see #generateOptionalLong()
    /// @see #generateLong()
    /// @see #generateLongOrNull()
    @NonNull
    public List<Long> generateLongList() throws SQLException {
        try (var ps = openGenerate()) {
            ppq.accept(ps);
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

    @Nullable
    private static Integer getOrNull(@NonNull OptionalInt opt) {
        checkNotNull(opt); // Check recognized by lombok.
        return opt.isEmpty() ? null : opt.getAsInt();
    }

    @Nullable
    private static Long getOrNull(@NonNull OptionalLong opt) {
        checkNotNull(opt); // Check recognized by lombok.
        return opt.isEmpty() ? null : opt.getAsLong();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
