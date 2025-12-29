package ninja.javahacker.sqlplus.stmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.NonNull;
import lombok.experimental.Delegate;
import ninja.javahacker.magicfactory.ConstructionException;
import ninja.javahacker.magicfactory.RecordMapper;

@SuppressFBWarnings("EI_EXPOSE_REP2")
public final class SmartResultSet implements ResultSet {

    @NonNull
    @Delegate(types = ResultSet.class)
    private final ResultSet rs;

    @NonNull
    private final ResultSetMetaData metaData;

    public SmartResultSet(@NonNull ResultSet rs) throws SQLException {
        this.rs = rs;
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
    public Map<String, Object> getMap(@NonNull int... campos) throws SQLException {
        Map<String, Object> row = new HashMap<>(campos.length);

        for (var i : campos) {
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
    public Object getTypedValue(int columnIndex) throws SQLException {
        var columnType = metaData.getColumnType(columnIndex);
        return switch (columnType) {
            case Types.DATE ->
                Optional.ofNullable(getDate     (columnIndex)).map(java.sql.Date     ::toLocalDate    ).orElse(null);
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
                Optional.ofNullable(getTimestamp(columnIndex)).map(java.sql.Timestamp::toLocalDateTime).orElse(null);
            case Types.TIME, Types.TIME_WITH_TIMEZONE ->
                Optional.ofNullable(getTime     (columnIndex)).map(java.sql.Time     ::toLocalTime    ).orElse(null);
            case Types.BIGINT -> {
                var r = getLong(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.INTEGER -> {
                var r = getInt(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.TINYINT -> {
                var r = getByte(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.SMALLINT -> {
                var r = getShort(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.FLOAT, Types.REAL -> {
                var r = getFloat(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.DOUBLE -> {
                var r = getDouble(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.BOOLEAN, Types.BIT -> {
                var r = getBoolean(columnIndex);
                yield wasNull() ? null : r;
            }
            case Types.DECIMAL, Types.NUMERIC                       -> getBigDecimal(columnIndex);
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> getBytes     (columnIndex);
            case Types.CLOB                                         -> getClob      (columnIndex);
            case Types.BLOB                                         -> getBlob      (columnIndex);
            case Types.ARRAY                                        -> getArray     (columnIndex);
            case Types.REF                                          -> getRef       (columnIndex);
            case Types.NVARCHAR, Types.NCHAR, Types.LONGNVARCHAR    -> getNString   (columnIndex);
            case Types.SQLXML                                       -> getSQLXML    (columnIndex);
            case Types.ROWID                                        -> getRowId     (columnIndex);
            // Inclui Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR
            default                                                 -> getString    (columnIndex);
        };
    }

    public <R extends Record> R getRecord(@NonNull Class<R> k, @NonNull int... campos) throws ConstructionException, SQLException {
        return RecordMapper.forMap(getMap(campos), k);
    }
}