package ninja.javahacker.annotimpler.sql.stmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.experimental.Delegate;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public final class SmartResultSet implements ResultSet {

    private final ConverterFactory factory;

    @NonNull
    @Delegate(types = ResultSet.class)
    private final ResultSet rs;

    @NonNull
    private final ResultSetMetaData metaData;

    public SmartResultSet(@NonNull ResultSet rs) throws SQLException {
        this.rs = rs;
        this.factory = ConverterFactory.STD;
        this.metaData = rs.getMetaData();
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + rs + "]";
    }

    @NonNull
    public Map<String, Object> getMap() throws SQLException {
        return getMap(IntStream.rangeClosed(1, metaData.getColumnCount()).toArray());
    }

    @NonNull
    public Map<String, Object> getMap(@NonNull int... fields) throws SQLException {
        Map<String, Object> row = new HashMap<>(fields.length);

        for (var i : fields) {
            var columnName = metaData.getColumnLabel(i);
            if (columnName == null || columnName.isEmpty()) {
                columnName = metaData.getColumnName(i);
            }

            var value = getTypedValue(i);
            row.put(columnName, value);
        }

        return row;
    }

    @Nullable
    @SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad"})
    public <E> E getTypedValue(int columnIndex, @NonNull Class<E> target) throws SQLException {
        return getTypedValueOpt(columnIndex, target).orElse(null);
    }

    @NonNull
    @SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad"})
    public <E> Optional<E> getTypedValueOpt(int columnIndex, @NonNull Class<E> target) throws SQLException {
        try {
            var raw = getTypedValue(columnIndex);
            return factory.get(target).from(raw);
        } catch (Converter.ConvertionException | ConverterFactory.UnavailableConverterException e) {
            throw new SQLException(e);
        }
    }

    @Nullable
    private <E> E nully(@Nullable E r) throws SQLException {
        return wasNull() ? null : r;
    }

    // Pode retornar null, Long, Integer, Byte, Short, Float, Double, Boolean,
    // String, BigDecimal, byte[], LocalDate, LocalTime, LocalDateTime, OffsetDateTime, OffsetTime,
    // Clob, NClob, Blob, Array, Ref, SQLXML, RowId ou Struct.
    @Nullable
    @SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:ParamPad", "checkstyle:ParenPad"})
    public Object getTypedValue(int columnIndex) throws SQLException {
        var columnType = metaData.getColumnType(columnIndex);
        return switch (columnType) {
            case Types.NULL -> null;
            case Types.DATE      -> Optional.ofNullable(getDate     (columnIndex)).map(java.sql.Date     ::toLocalDate    ).orElse(null);
            case Types.TIMESTAMP -> Optional.ofNullable(getTimestamp(columnIndex)).map(java.sql.Timestamp::toLocalDateTime).orElse(null);
            case Types.TIME      -> Optional.ofNullable(getTime     (columnIndex)).map(java.sql.Time     ::toLocalTime    ).orElse(null);
            case Types.TIMESTAMP_WITH_TIMEZONE -> getObject(columnIndex, OffsetDateTime.class);
            case Types.TIME_WITH_TIMEZONE      -> getObject(columnIndex, OffsetTime    .class);
            case Types.BIGINT             -> nully(getLong  (columnIndex));
            case Types.INTEGER            -> nully(getInt   (columnIndex));
            case Types.TINYINT            -> nully(getByte  (columnIndex));
            case Types.SMALLINT           -> nully(getShort (columnIndex));
            case Types.FLOAT, Types.REAL  -> nully(getFloat (columnIndex));
            case Types.DOUBLE             -> nully(getDouble(columnIndex));
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
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR , Types.DISTINCT -> getString    (columnIndex);
            case Types.REF_CURSOR -> throw new UnsupportedOperationException();
            // Types.DATALINK, Types.JAVA_OBJECT, Types.OTHER,
            default                                                 -> getString    (columnIndex);
        };
    }

    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull int... campos) throws SQLException {
        try {
            return new RecordMapper(factory).mapToRecord(getMap(campos), k);
        } catch (Converter.ConvertionException | MagicFactory.CreationException | MagicFactory.CreatorSelectionException | ConverterFactory.UnavailableConverterException e) {
            throw new SQLException(e);
        }
    }
}