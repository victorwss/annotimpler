package ninja.javahacker.annotimpler.sql.stmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.sql.Timestamp;
import lombok.NonNull;

import module java.base;
import module java.sql;

@SuppressWarnings({"PMD.ReplaceJavaUtilCalendar", "PMD.ReplaceJavaUtilDate"})
public interface NamedParameterStatement extends PreparedStatement {

    public static final String INPUT_STREAM_MESSAGE = "Can't reliably or safely use the same InputStream more than once.";
    public static final String READER_MESSAGE = "Can't reliably or safely use the same Reader more than once.";

    @NonNull
    public static NamedParameterStatement wrap(@NonNull PreparedStatement ps, @NonNull Map<String, List<Integer>> indexes) {
        return new InternalNamedParameterStatement(ps, indexes);
    }

    @NonNull
    public Map<String, List<Integer>> getIndexes();

    @NonNull
    public default List<Integer> getIndexes(@NonNull String name) {
        var indexes = getIndexes().get(name);
        if (indexes == null) throw new IllegalArgumentException("Parameter not found: " + name);
        return indexes;
    }

    public default void setArray(@NonNull String name, @Nullable java.sql.Array x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setArray(index, x);
        }
    }

    public default void setAsciiStream(@NonNull String name, @Nullable InputStream x) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setAsciiStream(index, x);
        }
    }

    public default void setAsciiStream(@NonNull String name, @Nullable InputStream x, int length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setAsciiStream(index, x, length);
        }
    }

    public default void setAsciiStream(@NonNull String name, @Nullable InputStream x, long length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setAsciiStream(index, x, length);
        }
    }

    public default void setBigDecimal(@NonNull String name, @Nullable BigDecimal x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBigDecimal(index, x);
        }
    }

    public default void setBinaryStream(@NonNull String name, @Nullable InputStream x) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1)throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBinaryStream(index, x);
        }
    }

    public default void setBinaryStream(@NonNull String name, @Nullable InputStream x, int length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBinaryStream(index, x, length);
        }
    }

    public default void setBinaryStream(@NonNull String name, @Nullable InputStream x, long length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBinaryStream(index, x, length);
        }
    }

    public default void setBlob(@NonNull String name, @Nullable Blob x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBlob(index, x);
        }
    }

    public default void setBlob(@NonNull String name, @Nullable InputStream inputStream) throws SQLException {
        var all = getIndexes(name);
        if (inputStream != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBlob(index, inputStream);
        }
    }

    public default void setBlob(@NonNull String name, @Nullable InputStream inputStream, long length) throws SQLException {
        var all = getIndexes(name);
        if (inputStream != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBlob(index, inputStream, length);
        }
    }

    public default void setBoolean(@NonNull String name, boolean x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBoolean(index, x);
        }
    }

    public default void setByte(@NonNull String name, byte x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setByte(index, x);
        }
    }

    public default void setBytes(@NonNull String name, @Nullable byte[] x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBytes(index, x);
        }
    }

    public default void setCharacterStream(@NonNull String name, @Nullable Reader reader) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setCharacterStream(index, reader);
        }
    }

    public default void setCharacterStream(@NonNull String name, @Nullable Reader reader, int length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setCharacterStream(index, reader, length);
        }
    }

    public default void setCharacterStream(@NonNull String name, @Nullable Reader reader, long length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setCharacterStream(index, reader, length);
        }
    }

    public default void setClob(@NonNull String name, @Nullable Clob x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setClob(index, x);
        }
    }

    public default void setClob(@NonNull String name, @Nullable Reader reader) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setClob(index, reader);
        }
    }

    public default void setClob(@NonNull String name, @Nullable Reader reader, long length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setClob(index, reader, length);
        }
    }

    /**
     * {@inheritDoc}
     * @param parameterIndex {@inheritDoc}
     * @param x {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @deprecated The class {@link java.sql.Date} is very badly designed and should never be used.
     * Prefer the {@link #setLocalDate(int, LocalDate)} method or the {@link #setObject(int, Object)} method passing a
     * {@link LocalDate} as the second parameter.
     * @see #setLocalDate(int, LocalDate)
     * @see #setObject(int, Object)
     */
    @Override
    @Deprecated
    public void setDate(int parameterIndex, @Nullable java.sql.Date x) throws SQLException;

    @Deprecated
    public default void setDate(@NonNull String name, @Nullable java.sql.Date x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDate(index, x);
        }
    }

    /**
     * {@inheritDoc}
     * @param parameterIndex {@inheritDoc}
     * @param x {@inheritDoc}
     * @param cal {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @deprecated The class {@link java.sql.Date} is very badly designed and should never be used. Having a companion {@link Calendar}
     * as a timezone makes things still more confusing and fragile. Further, having a date without time but with a timezone in non-sense.
     * Prefer the {@link #setLocalDate(int, LocalDate)} method or the {@link #setObject(int, Object)} method passing a
     * {@link LocalDate} as the second parameter.
     * @see #setLocalDate(int, LocalDate)
     * @see #setLocalDateTime(int, LocalDateTime)
     * @see #setOffsetDateTime(int, OffsetDateTime)
     * @see #setZonedDateTime(int, ZonedDateTime)
     * @see #setInstant(int, Instant)
     * @see #setObject(int, Object)
     */
    @Override
    @Deprecated
    public void setDate(int parameterIndex, @Nullable java.sql.Date x, @Nullable Calendar cal) throws SQLException;

    @Deprecated
    public default void setDate(@NonNull String name, @Nullable java.sql.Date x, @Nullable Calendar cal) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDate(index, x, cal);
        }
    }

    public default void setDouble(@NonNull String name, double x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDouble(index, x);
        }
    }

    public default void setDouble(int index, @NonNull OptionalDouble x) throws SQLException {
        if (x.isEmpty()) {
            this.setNull(index, Types.DOUBLE);
        } else {
            this.setDouble(index, x.getAsDouble());
        }
    }

    public default void setDouble(@NonNull String name, @NonNull OptionalDouble x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDouble(index, x);
        }
    }

    public default void setFloat(@NonNull String name, float x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setFloat(index, x);
        }
    }

    public default void setInt(@NonNull String name, int x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setInt(index, x);
        }
    }

    public default void setInt(int index, @NonNull OptionalInt x) throws SQLException {
        if (x.isEmpty()) {
            this.setNull(index, Types.INTEGER);
        } else {
            this.setInt(index, x.getAsInt());
        }
    }

    public default void setInt(@NonNull String name, @NonNull OptionalInt x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setInt(index, x);
        }
    }

    public default void setLocalDate(int index, @Nullable LocalDate x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setLocalDate(@NonNull String name, @Nullable LocalDate x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLocalDate(index, x);
        }
    }

    public default void setLocalDateTime(int index, @Nullable LocalDateTime x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setLocalDateTime(@NonNull String name, @Nullable LocalDateTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLocalDateTime(index, x);
        }
    }

    public default void setLocalTime(int index, @Nullable LocalTime x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setLocalTime(@NonNull String name, @Nullable LocalTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLocalTime(index, x);
        }
    }

    public default void setOffsetTime(int index, @Nullable OffsetTime x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setOffsetTime(@NonNull String name, @Nullable OffsetTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setOffsetTime(index, x);
        }
    }

    public default void setOffsetDateTime(int index, @Nullable OffsetDateTime x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setOffsetDateTime(@NonNull String name, @Nullable OffsetDateTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setOffsetDateTime(index, x);
        }
    }

    public default void setZonedDateTime(int index, @Nullable ZonedDateTime x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setZonedDateTime(@NonNull String name, @Nullable ZonedDateTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setZonedDateTime(index, x);
        }
    }

    public default void setInstant(int index, @Nullable Instant x) throws SQLException {
        this.setObject(index, x);
    }

    public default void setInstant(@NonNull String name, @Nullable Instant x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setInstant(index, x);
        }
    }

    public default void setLong(@NonNull String name, long x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLong(index, x);
        }
    }

    public default void setLong(int index, @NonNull OptionalLong x) throws SQLException {
        if (x.isEmpty()) {
            this.setNull(index, Types.BIGINT);
        } else {
            this.setLong(index, x.getAsLong());
        }
    }

    public default void setLong(@NonNull String name, @NonNull OptionalLong x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLong(index, x);
        }
    }

    public default void setNCharacterStream(@NonNull String name, @Nullable Reader value) throws SQLException {
        var all = getIndexes(name);
        if (value != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNCharacterStream(index, value);
        }
    }

    public default void setNCharacterStream(@NonNull String name, @Nullable Reader value, long length) throws SQLException {
        var all = getIndexes(name);
        if (value != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNCharacterStream(index, value, length);
        }
    }

    public default void setNClob(@NonNull String name, @Nullable NClob value) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNClob(index, value);
        }
    }

    public default void setNClob(@NonNull String name, @Nullable Reader reader) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNClob(index, reader);
        }
    }

    public default void setNClob(@NonNull String name, @Nullable Reader reader, long length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNClob(index, reader, length);
        }
    }

    public default void setNString(@NonNull String name, @Nullable String value) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNString(index, value);
        }
    }

    public default void setNull(@NonNull String name, int sqlType) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNull(index, sqlType);
        }
    }

    public default void setNull(@NonNull String name, int sqlType, @Nullable String typeName) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNull(index, sqlType, typeName);
        }
    }

    public default void setObject(@NonNull String name, @Nullable Object x) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x);
        }
    }

    public default void setObject(@NonNull String name, @Nullable Object x, @Nullable SQLType targetType) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType);
        }
    }

    public default void setObject(@NonNull String name, @Nullable Object x, int targetType) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType);
        }
    }

    public default void setObject(@NonNull String name, @Nullable Object x, @Nullable SQLType targetType, int scaleOrLength) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType, scaleOrLength);
        }
    }

    public default void setObject(@NonNull String name, @Nullable Object x, int targetType, int scaleOrLength) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType, scaleOrLength);
        }
    }

    public default void setRef(@NonNull String name, @Nullable Ref x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setRef(index, x);
        }
    }

    public default void setRowId(@NonNull String name, @Nullable RowId x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setRowId(index, x);
        }
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public default void setSQLXML(@NonNull String name, @Nullable SQLXML xmlObject) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setSQLXML(index, xmlObject);
        }
    }

    public default void setStruct(int index, @Nullable Struct x) throws SQLException {
        // This serves two purposes: 1 - Test if createStruct works. 2 - Ensure that the Struct instance passed downwards is compatible.
        var x2 = x == null
                ? this.getConnection().createStruct("test", new Object[] {"test"})
                : this.getConnection().createStruct(x.getSQLTypeName(), x.getAttributes());

        this.setObject(index, x == null ? null : x2);
    }

    public default void setStruct(@NonNull String name, @Nullable Struct x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setStruct(index, x);
        }
    }

    public default void setShort(@NonNull String name, short x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setShort(index, x);
        }
    }

    public default void setString(@NonNull String name, @Nullable String x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setString(index, x);
        }
    }

    /**
     * {@inheritDoc}
     * @param parameterIndex {@inheritDoc}
     * @param x {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @deprecated The class {@link java.sql.Time} is very badly designed and should never be used.
     * Prefer the {@link #setLocalTime(int, LocalTime)} method or the {@link #setObject(int, Object)} method passing a
     * {@link LocalTime} as the second parameter.
     * @see #setLocalTime(int, LocalTime)
     * @see #setObject(int, Object)
     */
    @Override
    @Deprecated
    public void setTime(int parameterIndex, @Nullable java.sql.Time x) throws SQLException;

    @Deprecated
    public default void setTime(@NonNull String name, @Nullable java.sql.Time x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTime(index, x);
        }
    }

    /**
     * {@inheritDoc}
     * @param parameterIndex {@inheritDoc}
     * @param x {@inheritDoc}
     * @param cal {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @deprecated The class {@link java.sql.Time} is very badly designed and should never be used. Having a companion {@link Calendar}
     * as a timezone makes things still more confusing and fragile.
     * Prefer the {@link #setOffsetTime(int, OffsetTime)} method or the {@link #setObject(int, Object)} method passing a
     * {@link OffsetTime} as the second parameter.
     * @see #setOffsetTime(int, OffsetTime)
     * @see #setObject(int, Object)
     */
    @Override
    @Deprecated
    public void setTime(int parameterIndex, @Nullable java.sql.Time x, @Nullable Calendar cal) throws SQLException;

    @Deprecated
    public default void setTime(@NonNull String name, @Nullable java.sql.Time x, @Nullable Calendar cal) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTime(index, x, cal);
        }
    }

    /**
     * {@inheritDoc}
     * @param parameterIndex {@inheritDoc}
     * @param x {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @deprecated The class {@link Timestamp} is very badly designed and should never be used.
     * Prefer the {@link #setLocalDateTime(int, LocalDateTime)} method or the {@link #setObject(int, Object)} method passing a
     * {@link LocalDateTime} as the second parameter.
     * @see #setLocalDateTime(int, LocalDateTime)
     * @see #setObject(int, Object)
     */
    @Override
    @Deprecated
    public void setTimestamp(int parameterIndex, @Nullable Timestamp x) throws SQLException;

    @Deprecated
    public default void setTimestamp(@NonNull String name, @Nullable Timestamp x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTimestamp(index, x);
        }
    }

    /**
     * {@inheritDoc}
     * @param parameterIndex {@inheritDoc}
     * @param x {@inheritDoc}
     * @param cal {@inheritDoc}
     * @throws SQLException {@inheritDoc}
     * @deprecated The class {@link Timestamp} is very badly designed and should never be used. Having a companion {@link Calendar}
     * as a timezone makes things still more confusing and fragile.
     * Prefer one of the {@link #setOffsetDateTime(int, OffsetDateTime)}, {@link #setZonedDateTime(int, ZonedDateTime)},
     * {@link #setInstant(int, Instant)} methods or perhaps the {@link #setObject(int, Object)} method passing a
     * {@link OffsetDateTime}, {@link ZonedDateTime} or {@link Instant} as the second parameter.
     * @see #setOffsetDateTime(int, OffsetDateTime)
     * @see #setZonedDateTime(int, ZonedDateTime)
     * @see #setInstant(int, Instant)
     * @see #setObject(int, Object)
     */
    @Override
    @Deprecated
    public void setTimestamp(int parameterIndex, @Nullable Timestamp x, @Nullable Calendar cal) throws SQLException;

    @Deprecated
    public default void setTimestamp(@NonNull String name, @Nullable Timestamp x, @Nullable Calendar cal) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTimestamp(index, x, cal);
        }
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public default void setURL(@NonNull String name, @Nullable URL x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setURL(index, x);
        }
    }

    @Deprecated
    public default void setUnicodeStream(@NonNull String name, @Nullable InputStream x, int length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setUnicodeStream(index, x, length);
        }
    }
}