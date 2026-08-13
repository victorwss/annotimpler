package ninja.javahacker.annotimpler.jdbc.stmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [ResultSet] wrapper that adds type-aware value retrieval, case-insensitive
/// column name lookup, and automatic mapping of result rows to Java `record` types.
///
/// All [ResultSet] methods are delegated to the underlying result set via Lombok
/// `@Delegate`. The additional methods provided by this class are:
///
/// - [#getTypedValue(int)] / [#getTypedValue(String)] — return the value at a column as the
///   most appropriate Java type for the JDBC type code.
/// - [#getTypedValue(int, Class)] / [#getTypedValue(String, Class)] — additionally convert
///   the value to a specific Java type via a [ConverterFactory].
/// - [#getMap()] and variants — collect all (or a subset of) columns into an unmodifiable map.
/// - [#getRecord(Class)] and variants — map one or more columns directly into a Java `record`.
///
/// Column name comparisons are always case-insensitive and locale-aware; the [Locale]
/// passed to the constructor governs locale-specific uppercasing (e.g. Turkish dotted-I).
@SuppressFBWarnings("EI_EXPOSE_REP2")
public interface SmartResultSet extends ResultSet {

    /// Returns the JDBC type code ([Types]) of the column at `columnIndex`.
    ///
    /// @param columnIndex The 1-based column index to look up.
    /// @return The JDBC type code of the column, as defined by [Types].
    /// @throws SQLException If a database access error occurs.
    public default int getColumnType(int columnIndex) throws SQLException {
        return getMetaData().getColumnType(columnIndex);
    }

    /// Returns the 1-based column index for the column named `columnLabel` (case-insensitive).
    ///
    /// @param columnLabel The case-insensitive column label to look up.
    /// @return The 1-based column index.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `columnLabel` is `null` or there is no such column.
    public int indexOf(@NonNull String columnLabel) throws SQLException;

    /// Returns the upper-cased label of the column at `columnIndex`, if it has a usable one.
    ///
    /// @param columnIndex The 1-based column index to look up.
    /// @return The upper-cased column label, or [Optional#empty()] if the column was
    ///         null-named, empty-named or a duplicate of another column's label.
    /// @throws SQLException If a database access error occurs.
    /// @throws IllegalArgumentException If `columnIndex` is out of range.
    @NonNull
    public Optional<String> labelOf(int columnIndex) throws SQLException;

    /// Creates a [SmartResultSet] wrapping the given [ResultSet] using the standard
    /// converter factory and the root locale.
    ///
    /// @param rs The [ResultSet] to wrap.
    /// @return The created [SmartResultSet].
    /// @throws SQLException If a database access error occurs while reading metadata.
    /// @throws IllegalArgumentException If `rs` is `null`.
    @NonNull
    public static SmartResultSet wrap(@NonNull ResultSet rs) throws SQLException {
        return new InternalSmartResultSet(rs, ConverterFactory.std(), Locale.ROOT);
    }

    /// Creates a [SmartResultSet] wrapping the given [ResultSet] with the specified
    /// converter factory and locale.
    ///
    /// @param rs The [ResultSet] to wrap.
    /// @param factory The converter factory used to convert column values to target Java types.
    /// @param localizer The locale used for case-insensitive column name matching.
    /// @return The created [SmartResultSet].
    /// @throws SQLException If a database access error occurs while reading metadata.
    /// @throws IllegalArgumentException If any argument is `null`.
    @NonNull
    public static SmartResultSet wrap(
            @NonNull ResultSet rs,
            @NonNull ConverterFactory factory,
            @NonNull Locale localizer)
            throws SQLException
    {
        return new InternalSmartResultSet(rs, factory, localizer);
    }

    @NonNull
    private int[] allFields() throws SQLException {
        return IntStream.rangeClosed(1, getMetaData().getColumnCount()).toArray();
    }

    /// Returns an unmodifiable map of all columns in the current row, keyed by upper-cased column label.
    ///
    /// Columns with null, empty, or duplicate (case-insensitive) labels are silently skipped.
    ///
    /// @return An unmodifiable map from upper-cased column label to its typed value.
    /// @throws SQLException If a database access error occurs.
    @NonNull
    public default Map<String, Object> getMap() throws SQLException {
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
    public default Map<String, Object> getMapByColumnNumbers(@NonNull int... fields) throws SQLException {
        var row = new HashMap<String, Object>(fields.length);

        for (var i : fields) {
            var columnName = labelOf(i);
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
    public default Map<String, Object> getMapByLabels(@NonNull String... fields) throws SQLException {
        var row = new HashMap<String, Object>(fields.length);

        for (var i : fields) {
            if (i == null) throw new IllegalArgumentException("Null-named columns are not allowed.");
            var columnIndex = indexOf(i);
            var columnNameOpt = labelOf(columnIndex); // Not necessarily equals to i, since it is not case-sensitive.
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
    public default <E> E getTypedValue(int columnIndex, @NonNull Class<E> target) throws SQLException {
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
    public default <E> E getTypedValue(@NonNull String columnLabel, @NonNull Class<E> target) throws SQLException {
        return getTypedValueOpt(columnLabel, target).orElse(null);
    }

    /// Reads the value at `columnIndex` and returns it as the most appropriate Java type for the
    /// column's JDBC type code.
    ///
    /// May return `null` for SQL `NULL`. The possible non-null return types are: [Long],
    /// [Integer], [Byte], [Short], [Float], [Double], [Boolean], [String], [BigDecimal],
    /// `byte[]`, [LocalDate], [LocalTime], [LocalDateTime],
    /// [OffsetDateTime], [OffsetTime], [Clob], [NClob],
    /// [Blob], [java.sql.Array], [Ref], [SQLXML], [RowId] or
    /// [Struct].
    ///
    /// @param columnIndex The 1-based column index to read.
    /// @return The column value mapped to the most appropriate Java type, or `null` for SQL `NULL`.
    /// @throws SQLException If a database access error occurs.
    @Nullable
    @SuppressFBWarnings("CC_CYCLOMATIC_COMPLEXITY")
    @SuppressWarnings({
        "checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad", "PMD.LawOfDemeter", "PMD.CyclomaticComplexity"
    })
    public default Object getTypedValue(int columnIndex) throws SQLException {
        var columnType = getColumnType(columnIndex);
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
    public default Object getTypedValue(@NonNull String columnLabel) throws SQLException {
        var idx = indexOf(columnLabel);
        return getTypedValue(idx);
    }

    /// Reads the value at `columnIndex`, converts it to type `E`, and wraps the result in an [Optional].
    ///
    /// Returns [Optional#empty()] if the SQL value is `NULL`.
    ///
    /// @param <E> The target Java type.
    /// @param columnIndex The 1-based column index to read.
    /// @param target The class of the target type.
    /// @return An [Optional] containing the converted value, or empty for SQL `NULL`.
    /// @throws SQLException If a database access error occurs or conversion fails.
    /// @throws IllegalArgumentException If `target` is `null`.
    @NonNull
    public <E> Optional<E> getTypedValueOpt(int columnIndex, @NonNull Class<E> target) throws SQLException;

    /// Reads the value at the column identified by `columnLabel` (case-insensitive), converts it
    /// to type `E`, and wraps the result in an [Optional].
    ///
    /// Returns [Optional#empty()] if the SQL value is `NULL`.
    ///
    /// @param <E> The target Java type.
    /// @param columnLabel The case-insensitive column label to read.
    /// @param target The class of the target type.
    /// @return An [Optional] containing the converted value, or empty for SQL `NULL`.
    /// @throws SQLException If a database access error occurs or conversion fails.
    /// @throws IllegalArgumentException If `columnLabel` or `target` is `null`, or the label is not found.
    @NonNull
    public default <E> Optional<E> getTypedValueOpt(@NonNull String columnLabel, @NonNull Class<E> target) throws SQLException {
        var idx = indexOf(columnLabel);
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
    public default <R extends Record> R getRecord(@NonNull Class<R> k) throws SQLException {
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
    public default <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
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
    public default <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull String... fields) throws SQLException {
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
    public default <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull Function<String, String> remapper) throws SQLException {
        return getRecord(k, remapper, allFields());
    }

    /// Maps the given column-name-to-value `map` to a record of type `R`, applying `remapper`
    /// to translate upper-cased column names to record component names before matching.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type.
    /// @param remapper A function that translates an upper-cased column name to the matching record component name.
    /// @param map The column-name-to-value map to convert, keyed by upper-cased column label.
    /// @return A new instance of `R` populated from `map`.
    /// @throws SQLException If a database access error occurs or the map cannot be mapped.
    /// @throws IllegalArgumentException If `k`, `remapper` or `map` is `null`.
    @NonNull
    public <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull Map<String, Object> map)
            throws SQLException;

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
    public default <R extends Record> R getRecord(
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
    public default <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull String... fields)
            throws SQLException
    {
        var map = getMapByLabels(fields);
        return getRecord(k, remapper, map);
    }

    /// Builds a remapper that converts column keys (stored uppercase by an internal column
    /// mapping, using the wrapper's configured locale) back to the exact record field names,
    /// enabling case-insensitive column-to-field matching.
    ///
    /// @param <R> The record type.
    /// @param k The class of the record type to build a remapper for.
    /// @return A function that translates an upper-cased column name to the matching record component name.
    /// @throws IllegalArgumentException If `k` is `null`.
    @NonNull
    public <R extends Record> Function<String, String> defaultRemapper(@NonNull Class<R> k);

    @Nullable
    private <E> E nully(@Nullable E r) throws SQLException {
        return wasNull() ? null : r;
    }
}
