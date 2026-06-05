package ninja.javahacker.annotimpler.sql.stmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.experimental.Delegate;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

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

    public SmartResultSet(@NonNull ResultSet rs) throws SQLException {
        List.of(rs); // Force lombok do the check before the constructor call.
        this(rs, ConverterFactory.STD, Locale.ROOT);
    }

    public SmartResultSet(@NonNull ResultSet rs, @NonNull ConverterFactory factory, @NonNull Locale localizer) throws SQLException {
        this.rs = rs;
        this.factory = factory;
        this.metaData = rs.getMetaData();
        this.mappings = new ColumnMapping(metaData, localizer);
    }

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
            checkNotNull(rsmd);
            checkNotNull(localizer);

            var count = rsmd.getColumnCount();
            var keys = new ArrayList<Optional<String>>(count);
            var idx = new HashMap<String, Integer>(count);

            for (int i = 1; i <= count; i++) {
                var columnName = rsmd.getColumnLabel(i);

                // A null column label should never happen in sane JDBC implementations, but we defend against it anyway.
                if (columnName == null) columnName = "";

                // Column names that are duplicated or that vary only by capitalization should not happen either.
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
            checkNotNull(columnName);
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

    @NonNull
    public Map<String, Object> getMap() throws SQLException {
        return getMapByColumnNumbers(allFields());
    }

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

    @Nullable
    public <E> E getTypedValue(int columnIndex, @NonNull Class<E> target) throws SQLException {
        return getTypedValueOpt(columnIndex, target).orElse(null);
    }

    @Nullable
    public <E> E getTypedValue(@NonNull String columnLabel, @NonNull Class<E> target) throws SQLException {
        return getTypedValueOpt(columnLabel, target).orElse(null);
    }

    @NonNull
    public <E> Optional<E> getTypedValueOpt(int columnIndex, @NonNull Class<E> target) throws SQLException {
        try {
            var raw = getTypedValue(columnIndex);
            return factory.getOf(target).fromObj(raw);
        } catch (ConvertionException | UnavailableConverterException e) {
            throw new SQLException(e);
        }
    }

    @NonNull
    public <E> Optional<E> getTypedValueOpt(@NonNull String columnLabel, @NonNull Class<E> target) throws SQLException {
        var idx = this.mappings.indexOf(columnLabel);
        return getTypedValueOpt(idx, target);
    }

    @Nullable
    private <E> E nully(@Nullable E r) throws SQLException {
        return wasNull() ? null : r;
    }

    // May return null, Long, Integer, Byte, Short, Float, Double, Boolean,
    // String, BigDecimal, byte[], LocalDate, LocalTime, LocalDateTime, OffsetDateTime, OffsetTime,
    // Clob, NClob, Blob, Array, Ref, SQLXML, RowId or Struct.
    @Nullable
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
            case Types.VARCHAR, Types.CHAR,
                 Types.LONGVARCHAR, Types.DISTINCT, Types.DATALINK,
                 Types.JAVA_OBJECT, Types.OTHER                     -> getString    (columnIndex);
            default                                                 -> getString    (columnIndex);
        };
    }

    // May return null, Long, Integer, Byte, Short, Float, Double, Boolean,
    // String, BigDecimal, byte[], LocalDate, LocalTime, LocalDateTime, OffsetDateTime, OffsetTime,
    // Clob, NClob, Blob, Array, Ref, SQLXML, RowId or Struct.
    @Nullable
    @SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad"})
    public Object getTypedValue(@NonNull String columnLabel) throws SQLException {
        var idx = this.mappings.indexOf(columnLabel);
        return getTypedValue(idx);
    }

    public <R extends Record> R getRecord(@NonNull Class<R> k) throws SQLException {
        return getRecord(k, x -> x, allFields());
    }

    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull int... fields) throws SQLException {
        return getRecord(k, x -> x, fields);
    }

    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull String... fields) throws SQLException {
        return getRecord(k, x -> x, fields);
    }

    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull Function<String, String> remapper) throws SQLException {
        return getRecord(k, remapper, allFields());
    }

    private <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull Map<String, Object> map)
            throws SQLException
    {
        checkNotNull(k);
        checkNotNull(remapper);
        checkNotNull(map);
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

    public <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull int... fields)
            throws SQLException
    {
        var map = getMapByColumnNumbers(fields);
        return getRecord(k, remapper, map);
    }

    public <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull String... fields)
            throws SQLException
    {
        var map = getMapByLabels(fields);
        return getRecord(k, remapper, map);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}