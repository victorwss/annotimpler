package ninja.javahacker.annotimpler.sql.jdbcstmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.experimental.Delegate;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [java.sql.ResultSet] wrapper that adds type-aware value retrieval, case-insensitive
/// column name lookup, and automatic mapping of result rows to Java `record` types.
///
/// All [java.sql.ResultSet] methods are delegated to the underlying result set via Lombok
/// `@Delegate`.  The additional methods provided by this class are:
///
/// - [#getTypedValue(int)] / [#getTypedValue(String)] — return the value at a column as the
///   most appropriate Java type for the JDBC type code.
/// - [#getTypedValue(int, Class)] / [#getTypedValue(String, Class)] — additionally convert
///   the value to a specific Java type via a [ConverterFactory].
/// - [#getMap()] and variants — collect all (or a subset of) columns into an unmodifiable map.
/// - [#getRecord(Class)] and variants — map one or more columns directly into a Java `record`.
///
/// Column name comparisons are always case-insensitive and locale-aware; the [java.util.Locale]
/// passed to the constructor governs locale-specific uppercasing (e.g. Turkish dotted-I).
@SuppressFBWarnings("EI_EXPOSE_REP2")
public final class SmartResultSet implements ResultSet {

    @NonNull
    private final ConverterFactory factory;

    @NonNull
    @Delegate(types = ResultSet.class)
    private final ResultSet rs;

    @NonNull
    private final ResultSetMetaData metaData;

    @NonNull
    private final ColumnMapping mappings;

    @NonNull
    private final Locale localizer;

    /// Creates a [SmartResultSet] wrapping the given [java.sql.ResultSet] using the standard
    /// converter factory and the root locale.
    ///
    /// @param rs The [java.sql.ResultSet] to wrap.
    /// @throws SQLException If a database access error occurs while reading metadata.
    /// @throws IllegalArgumentException If `rs` is `null`.
    public SmartResultSet(@NonNull ResultSet rs) throws SQLException {
        List.of(rs); // Force lombok do the check before the constructor call.
        this(rs, ConverterFactory.STD, Locale.ROOT);
    }

    /// Creates a [SmartResultSet] wrapping the given [java.sql.ResultSet] with the specified
    /// converter factory and locale.
    ///
    /// @param rs The [java.sql.ResultSet] to wrap.
    /// @param factory The converter factory used to convert column values to target Java types.
    /// @param localizer The locale used for case-insensitive column name matching.
    /// @throws SQLException If a database access error occurs while reading metadata.
    /// @throws IllegalArgumentException If any argument is `null`.
    public SmartResultSet(@NonNull ResultSet rs, @NonNull ConverterFactory factory, @NonNull Locale localizer) throws SQLException {
        this.rs = rs;
        this.factory = factory;
        this.metaData = rs.getMetaData();
        this.localizer = localizer;
        this.mappings = new ColumnMapping(metaData, localizer);
    }

    /// Returns a brief string identifying this wrapper and the underlying result set.
    ///
    /// @return A string of the form `SmartResultSet[<underlying>]`.
    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + rs + "]";
    }

    @NonNull
    private int[] allFields() throws SQLException {
        return IntStream.rangeClosed(1, metaData.getColumnCount()).toArray();
    }

    private static final class ColumnMapping {
        @NonNull
        private final List<Optional<String>> columnNames;

        @NonNull
        private final Map<String, Integer> columnIndexes;

        @NonNull
        private final Locale localizer;

        public ColumnMapping(@NonNull ResultSetMetaData rsmd, @NonNull Locale localizer) throws SQLException {
            checkNotNull(rsmd); // Check recognized by lombok.
            checkNotNull(localizer); // Check recognized by lombok.

            var count = rsmd.getColumnCount();
            var keys = new ArrayList<Optional<String>>(count);
            var idx = new HashMap<String, Integer>(count);

            for (int i = 1; i <= count; i++) {
                var columnName = rsmd.getColumnLabel(i);

                // A null column label should never happen in sane JDBC implementations, but we defend against it anyway.
                if (columnName == null) columnName = "";

                // Column names that are duplicated or that vary only by capitalization should not happen either.
                // Use localizer due to the Turkish/Azerbaijani dotted vs dotless I problem (e.g. "i".toUpperCase(TURKISH) = "İ").
                columnName = columnName.toUpperCase(localizer);

                /* Should never fail in sane JDBC implementations, which should not contain columns that are:
                   a) Null-named;
                   b) Empty-named;
                   c) Duplicated;
                   d) Varying only by capitalization;
                   e) Varying in name only due to the use of Turkish/Azerbaijani dotted vs dotless I.
                   When it fails due to any of those things happening, the field is simply ommited.
                */
                if (!columnName.isEmpty() && !idx.containsKey(columnName)) {
                    idx.put(columnName, i);
                    keys.add(Optional.of(columnName));
                } else {
                    keys.add(Optional.empty());
                }
            }

            this.columnNames = List.copyOf(keys);
            this.columnIndexes = Map.copyOf(idx);
            this.localizer = localizer;
        }

        public int getColumnCount() {
            return columnNames.size();
        }

        public int indexOf(@NonNull String columnName) {
            checkNotNull(columnName); // Check recognized by lombok.
            var name = columnName.toUpperCase(localizer);
            var v = columnIndexes.get(name);
            if (v == null) {
                throw new IllegalArgumentException("There is no column \"" + columnName + "\".");
            }
            return v;
        }

        @NonNull
        public Optional<String> valueOf(int columnIndex) {
            if (columnIndex < 1 || columnIndex > getColumnCount()) {
                throw new IllegalArgumentException("There is no column " + columnIndex + ".");
            }
            return columnNames.get(columnIndex - 1);
        }
    }

    /// Returns an unmodifiable map of all columns in the current row, keyed by upper-cased column label.
    ///
    /// Columns with null, empty, or duplicate (case-insensitive) labels are silently skipped.
    ///
    /// @return An unmodifiable map from upper-cased column label to its typed value.
    /// @throws SQLException If a database access error occurs.
    @NonNull
    public Map<String, Object> getMap() throws SQLException {
        return getMapByColumnNumbers(allFields());
    }

    /// Returns an unmodifiable map of the specified columns in the current row, keyed by
    /// upper-cased column label.
    ///
    /// Columns with null, empty, or duplicate (case-insensitive) labels among the given indices
    /// are silently skipped.
    ///
    /// @param fields The 1-based column indices to include.
    /// @return An unmodifiable map from upper-cased column label to its typed value.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If any index is out of range.
    @NonNull
    public Map<String, Object> getMapByColumnNumbers(@NonNull int... fields) throws SQLException {
        var row = new HashMap<String, Object>(fields.length);

        for (var i : fields) {
            var columnName = mappings.valueOf(i);
            if (columnName.isEmpty() || row.containsKey(columnName.get())) continue;
            var value = getTypedValue(i);
            row.put(columnName.get(), value);
        }

        return Map.copyOf(row);
    }

    /// Returns an unmodifiable map of the specified columns in the current row, keyed by
    /// upper-cased column label, looking up each column by its case-insensitive label.
    ///
    /// @param fields The column labels to include; must not contain `null` elements.
    /// @return An unmodifiable map from upper-cased column label to its typed value.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If any element of `fields` is `null` or not found.
    @NonNull
    public Map<String, Object> getMapByLabels(@NonNull String... fields) throws SQLException {
        var row = new HashMap<String, Object>(fields.length);

        for (var i : fields) {
            if (i == null) throw new IllegalArgumentException("Null-named columns are not allowed.");
            var columnIndex = mappings.indexOf(i);
            var columnNameOpt = mappings.valueOf(columnIndex); // Not necessarily equals to i, since it is not case-sensitive.
            var columnName = columnNameOpt.orElseThrow(AssertionError::new);
            if (row.containsKey(columnName)) continue;
            var value = getTypedValue(i);
            row.put(columnName, value);
        }

        return Map.copyOf(row);
    }

    /// Reads the value at `columnIndex` and converts it to type `E` via the [ConverterFactory].
    ///
    /// Returns `null` if the SQL value is `NULL`.
    ///
    /// @param <E> The target Java type.
    /// @param columnIndex The 1-based column index to read.
    /// @param target The class of the target type.
    /// @return The converted value, or `null` for SQL `NULL`.
    /// @throws SQLException If a database access error occurs or conversion fails.
    /// @throws IllegalArgumentException If `target` is `null`.
    @Nullable
    public <E> E getTypedValue(int columnIndex, @NonNull Class<E> target) throws SQLException {
        return getTypedValueOpt(columnIndex, target).orElse(null);
    }

    /// Reads the value at the column identified by `columnLabel` (case-insensitive) and converts
    /// it to type `E` via the [ConverterFactory].
    ///
    /// Returns `null` if the SQL value is `NULL`.
    ///
    /// @param <E> The target Java type.
    /// @param columnLabel The case-insensitive column label to read.
    /// @param target The class of the target type.
    /// @return The converted value, or `null` for SQL `NULL`.
    /// @throws SQLException If a database access error occurs or conversion fails.
    /// @throws IllegalArgumentException If `columnLabel` or `target` is `null`, or the label is
    ///                                  not found.
    @Nullable
    public <E> E getTypedValue(@NonNull String columnLabel, @NonNull Class<E> target) throws SQLException {
        return getTypedValueOpt(columnLabel, target).orElse(null);
    }

    /// Reads the value at `columnIndex` and returns it as the most appropriate Java type for the
    /// column's JDBC type code.
    ///
    /// May return `null` for SQL `NULL`. The possible non-null return types are: [Long],
    /// [Integer], [Byte], [Short], [Float], [Double], [Boolean], [String], [java.math.BigDecimal],
    /// `byte[]`, [java.time.LocalDate], [java.time.LocalTime], [java.time.LocalDateTime],
    /// [java.time.OffsetDateTime], [java.time.OffsetTime], [java.sql.Clob], [java.sql.NClob],
    /// [java.sql.Blob], [java.sql.Array], [java.sql.Ref], [java.sql.SQLXML], [java.sql.RowId] or
    /// [java.sql.Struct].
    ///
    /// @param columnIndex The 1-based column index to read.
    /// @return The column value mapped to the most appropriate Java type, or `null` for SQL `NULL`.
    /// @throws SQLException If a database access error occurs.
    @Nullable
    @SuppressFBWarnings("CC_CYCLOMATIC_COMPLEXITY")
    @SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad"})
    public Object getTypedValue(int columnIndex) throws SQLException {
        var columnType = metaData.getColumnType(columnIndex);
        return switch (columnType) {
            case Types.REF_CURSOR -> throw new UnsupportedOperationException();
            case Types.NULL -> null;
            case Types.DATE                    -> getObject(columnIndex, LocalDate     .class);
            case Types.TIMESTAMP               -> getObject(columnIndex, LocalDateTime .class);
            case Types.TIME                    -> getObject(columnIndex, LocalTime     .class);
            case Types.TIMESTAMP_WITH_TIMEZONE -> getObject(columnIndex, OffsetDateTime.class);
            case Types.TIME_WITH_TIMEZONE      -> getObject(columnIndex, OffsetTime    .class);
            case Types.BIGINT             -> nully(getLong   (columnIndex));
            case Types.INTEGER            -> nully(getInt    (columnIndex));
            case Types.TINYINT            -> nully(getByte   (columnIndex));
            case Types.SMALLINT           -> nully(getShort  (columnIndex));
            case Types.FLOAT, Types.REAL  -> nully(getFloat  (columnIndex));
            case Types.DOUBLE             -> nully(getDouble (columnIndex));
            case Types.BOOLEAN, Types.BIT -> nully(getBoolean(columnIndex));
            case Types.DECIMAL, Types.NUMERIC                       -> getBigDecimal(columnIndex);
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> getBytes     (columnIndex);
            case Types.CLOB                                         -> getClob      (columnIndex);
            case Types.NCLOB                                        -> getNClob     (columnIndex);
            case Types.BLOB                                         -> getBlob      (columnIndex);
            case Types.ARRAY                                        -> getArray     (columnIndex);
            case Types.REF                                          -> getRef       (columnIndex);
            case Types.NVARCHAR, Types.NCHAR, Types.LONGNVARCHAR    -> getNString   (columnIndex);
            case Types.SQLXML                                       -> getSQLXML    (columnIndex);
            case Types.ROWID                                        -> getRowId     (columnIndex);
            case Types.STRUCT                              -> (Struct) getObject    (columnIndex);
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.DISTINCT, Types.DATALINK,
                    Types.JAVA_OBJECT, Types.OTHER                  -> getString    (columnIndex);
            default                                                 -> getString    (columnIndex);
        };
    }

    /// Reads the value at the column identified by `columnLabel` (case-insensitive) and returns
    /// it as the most appropriate Java type for the column's JDBC type code.
    ///
    /// May return `null` for SQL `NULL`. The possible non-null return types are the same as
    /// for [#getTypedValue(int)].
    ///
    /// @param columnLabel The case-insensitive column label to read.
    /// @return The column value mapped to the most appropriate Java type, or `null` for SQL `NULL`.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `columnLabel` is `null` or not found in the result set.
    @Nullable
    @SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad"})
    public Object getTypedValue(@NonNull String columnLabel) throws SQLException {
        var idx = this.mappings.indexOf(columnLabel);
        return getTypedValue(idx);
    }

    /// Reads the value at `columnIndex`, converts it to type `E`, and wraps the result in an [java.util.Optional].
    ///
    /// Returns [java.util.Optional#empty()] if the SQL value is `NULL`.
    ///
    /// @param <E> The target Java type.
    /// @param columnIndex The 1-based column index to read.
    /// @param target The class of the target type.
    /// @return An [java.util.Optional] containing the converted value, or empty for SQL `NULL`.
    /// @throws SQLException If a database access error occurs or conversion fails.
    /// @throws IllegalArgumentException If `target` is `null`.
    @NonNull
    public <E> Optional<E> getTypedValueOpt(int columnIndex, @NonNull Class<E> target) throws SQLException {
        try {
            var raw = getTypedValue(columnIndex);
            return factory.getOf(target).fromObj(raw);
        } catch (ConvertionException | UnavailableConverterException e) {
            throw new SQLException(e);
        }
    }

    /// Reads the value at the column identified by `columnLabel` (case-insensitive), converts it
    /// to type `E`, and wraps the result in an [java.util.Optional].
    ///
    /// Returns [java.util.Optional#empty()] if the SQL value is `NULL`.
    ///
    /// @param <E> The target Java type.
    /// @param columnLabel The case-insensitive column label to read.
    /// @param target The class of the target type.
    /// @return An [java.util.Optional] containing the converted value, or empty for SQL `NULL`.
    /// @throws SQLException If a database access error occurs or conversion fails.
    /// @throws IllegalArgumentException If `columnLabel` or `target` is `null`, or the label is not found.
    @NonNull
    public <E> Optional<E> getTypedValueOpt(@NonNull String columnLabel, @NonNull Class<E> target) throws SQLException {
        var idx = this.mappings.indexOf(columnLabel);
        return getTypedValueOpt(idx, target);
    }

    /// Maps all columns of the current row to a record of type `R` using default column order
    /// and case-insensitive component name matching.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @return A new instance of `R` populated from the current row.
    /// @throws SQLException If a database access error occurs or the row cannot be mapped.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    public <R extends Record> R getRecord(@NonNull Class<R> k) throws SQLException {
        return getRecord(k, defaultRemapper(k), allFields());
    }

    /// Maps the specified columns (by 1-based index) of the current row to a record of type `R`.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @param fields The 1-based column indices to map, in component order.
    /// @return A new instance of `R` populated from the specified columns.
    /// @throws SQLException If a database access error occurs or the row cannot be mapped.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        return getRecord(k, defaultRemapper(k), fields);
    }

    /// Maps the specified columns (by case-insensitive label) of the current row to a record of type `R`.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @param fields The column labels to map, in component order; must not contain `null`.
    /// @return A new instance of `R` populated from the specified columns.
    /// @throws SQLException If a database access error occurs or the row cannot be mapped.
    /// @throws IllegalArgumentException If `k` is `null`, or any label is `null` or not found.
    @NonNull
    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull String... fields) throws SQLException {
        return getRecord(k, defaultRemapper(k), fields);
    }

    /// Maps all columns of the current row to a record of type `R`, applying `remapper` to
    /// translate upper-cased column names to record component names before matching.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @param remapper A function that translates an upper-cased column name to the matching record component name.
    /// @return A new instance of `R` populated from the current row.
    /// @throws SQLException If a database access error occurs or the row cannot be mapped.
    /// @throws IllegalArgumentException If `k` or `remapper` is `null`.
    @NonNull
    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull Function<String, String> remapper) throws SQLException {
        return getRecord(k, remapper, allFields());
    }

    // Builds a remapper that converts column keys (stored uppercase by ColumnMapping using localizer)
    // back to the exact record field names, enabling case-insensitive column-to-field matching.
    // The same locale used by ColumnMapping is applied here so that locale-specific uppercasing
    // (e.g., Turkish dotted 'İ' vs dotless 'I') is handled consistently on both sides.
    @NonNull
    private <R extends Record> Function<String, String> defaultRemapper(@NonNull Class<R> k) {
        var components = k.getRecordComponents();
        var mapping = new HashMap<String, String>(components.length);
        for (var rc : components) {
            mapping.put(rc.getName().toUpperCase(localizer), rc.getName());
        }
        return key -> mapping.getOrDefault(key, key);
    }

    @NonNull
    private <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull Map<String, Object> map)
            throws SQLException
    {
        checkNotNull(k); // Check recognized by lombok.
        checkNotNull(remapper); // Check recognized by lombok.
        checkNotNull(map); // Check recognized by lombok.
        try {
            var remappedMap = map.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(e -> remapper.apply(e.getKey()), Map.Entry::getValue));
            return factory.mapToRecord(remappedMap, k);
        } catch (ConvertionException
                | MagicFactory.CreationException
                | MagicFactory.CreatorSelectionException
                | UnavailableConverterException e)
        {
            throw new SQLException(e);
        }
    }

    /// Maps the specified columns (by 1-based index) of the current row to a record of type `R`,
    /// applying `remapper` to translate upper-cased column names to record component names.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @param remapper A function that translates an upper-cased column name to the matching record component name.
    /// @param fields The 1-based column indices to map, in component order.
    /// @return A new instance of `R` populated from the specified columns.
    /// @throws SQLException If a database access error occurs or the row cannot be mapped.
    /// @throws IllegalArgumentException If `k` or `remapper` is `null`.
    @NonNull
    public <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull int... fields)
            throws SQLException
    {
        var map = getMapByColumnNumbers(fields);
        return getRecord(k, remapper, map);
    }

    /// Maps the specified columns (by case-insensitive label) of the current row to a record of
    /// type `R`, applying `remapper` to translate upper-cased column names to record component
    /// names.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @param remapper A function that translates an upper-cased column name to the matching
    ///                 record component name.
    /// @param fields The column labels to map; must not contain `null`.
    /// @return A new instance of `R` populated from the specified columns.
    /// @throws SQLException If a database access error occurs or the row cannot be mapped.
    /// @throws IllegalArgumentException If `k` or `remapper` is `null`, or any label is `null` or not found.
    @NonNull
    public <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull String... fields)
            throws SQLException
    {
        var map = getMapByLabels(fields);
        return getRecord(k, remapper, map);
    }

    @Nullable
    private <E> E nully(@Nullable E r) throws SQLException {
        return wasNull() ? null : r;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}