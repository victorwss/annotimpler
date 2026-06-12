package ninja.javahacker.annotimpler.sql.jdbcstmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.sql.Timestamp;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// A [PreparedStatement] extension that supports named parameters in SQL queries.
///
/// Named parameters are written as `:name` tokens in the SQL string (produced by
/// [ParsedQuery#parse(String)]).  This interface wraps a standard [PreparedStatement] and
/// provides named-parameter overloads of all `setXxx` methods.
///
/// When the same parameter name appears more than once in the SQL, all positional occurrences
/// are set simultaneously.  Passing a stream or reader to a parameter that maps to more than
/// one occurrence throws [java.sql.SQLException], because streams cannot be consumed twice
/// reliably.
///
/// Use [#wrap(PreparedStatement, Map)] to obtain an instance.
@SuppressWarnings({"PMD.ReplaceJavaUtilCalendar", "PMD.ReplaceJavaUtilDate"})
public interface NamedParameterStatement extends PreparedStatement, ParameterReceiver {

    /// Error message used when a non-null [java.io.InputStream] is passed to a named parameter
    /// that appears more than once in the SQL statement.
    public static final String INPUT_STREAM_MESSAGE = "Can't reliably or safely use the same InputStream more than once.";

    /// Error message used when a non-null [java.io.Reader] is passed to a named parameter
    /// that appears more than once in the SQL statement.
    public static final String READER_MESSAGE = "Can't reliably or safely use the same Reader more than once.";

    /// Wraps a [PreparedStatement] with a name-to-index mapping to create a
    /// [NamedParameterStatement].
    ///
    /// @param ps The underlying [PreparedStatement] to wrap.
    /// @param indexes The mapping from each parameter name to its list of 1-based positional
    ///                indices; typically produced by [ParsedQuery#params()].
    /// @return A new [NamedParameterStatement] backed by `ps`.
    /// @throws IllegalArgumentException If any argument is `null`.
    @NonNull
    public static NamedParameterStatement wrap(@NonNull PreparedStatement ps, @NonNull Map<String, List<Integer>> indexes) {
        return new InternalNamedParameterStatement(ps, indexes);
    }

    /// Returns the mapping from each parameter name to its list of 1-based positional indices
    /// in the underlying prepared statement.
    ///
    /// @return An unmodifiable map of parameter names to their positional indices.
    @NonNull
    public Map<String, List<Integer>> getIndexes();

    /// Returns the list of 1-based positional indices for the given parameter name.
    ///
    /// @param name The named parameter to look up.
    /// @return A non-empty list of 1-based positional indices for `name`.
    /// @throws IllegalArgumentException If `name` is `null` or not present in this statement.
    @NonNull
    public default List<Integer> getIndexes(@NonNull String name) {
        var indexes = getIndexes().get(name);
        if (indexes == null) throw new IllegalArgumentException("Parameter not found: " + name);
        return indexes;
    }

    /// Sets the named parameter `name` to the given [java.sql.Array] value.
    public default void setArray(@NonNull String name, @Nullable java.sql.Array x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setArray(index, x);
        }
    }

    /// Sets the named parameter `name` to the given ASCII [java.io.InputStream].
    public default void setAsciiStream(@NonNull String name, @Nullable InputStream x) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setAsciiStream(index, x);
        }
    }

    /// Sets the named parameter `name` to the given ASCII [java.io.InputStream] with the specified byte length.
    public default void setAsciiStream(@NonNull String name, @Nullable InputStream x, int length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setAsciiStream(index, x, length);
        }
    }

    /// Sets the named parameter `name` to the given ASCII [java.io.InputStream] with the specified long byte length.
    public default void setAsciiStream(@NonNull String name, @Nullable InputStream x, long length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setAsciiStream(index, x, length);
        }
    }

    /// Sets the named parameter `name` to the given [java.math.BigDecimal] value.
    public default void setBigDecimal(@NonNull String name, @Nullable BigDecimal x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBigDecimal(index, x);
        }
    }

    /// Sets the named parameter `name` to the given binary [java.io.InputStream].
    public default void setBinaryStream(@NonNull String name, @Nullable InputStream x) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1)throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBinaryStream(index, x);
        }
    }

    /// Sets the named parameter `name` to the given binary [java.io.InputStream] with the specified byte length.
    public default void setBinaryStream(@NonNull String name, @Nullable InputStream x, int length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBinaryStream(index, x, length);
        }
    }

    /// Sets the named parameter `name` to the given binary [java.io.InputStream] with the specified long byte length.
    public default void setBinaryStream(@NonNull String name, @Nullable InputStream x, long length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBinaryStream(index, x, length);
        }
    }

    /// Sets the named parameter `name` to the given [java.sql.Blob] value.
    public default void setBlob(@NonNull String name, @Nullable Blob x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBlob(index, x);
        }
    }

    /// Sets the named parameter `name` to a [java.sql.Blob] whose content is read from the given [java.io.InputStream].
    public default void setBlob(@NonNull String name, @Nullable InputStream inputStream) throws SQLException {
        var all = getIndexes(name);
        if (inputStream != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBlob(index, inputStream);
        }
    }

    /// Sets the named parameter `name` to a [java.sql.Blob] whose content is read from the given [java.io.InputStream] up to `length` bytes.
    public default void setBlob(@NonNull String name, @Nullable InputStream inputStream, long length) throws SQLException {
        var all = getIndexes(name);
        if (inputStream != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setBlob(index, inputStream, length);
        }
    }

    /// Sets the named parameter `name` to the given `boolean` value.
    public default void setBoolean(@NonNull String name, boolean x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBoolean(index, x);
        }
    }

    /// Sets the named parameter `name` to the given `byte` value.
    public default void setByte(@NonNull String name, byte x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setByte(index, x);
        }
    }

    /// Sets the named parameter `name` to the given `byte[]` value.
    public default void setBytes(@NonNull String name, @Nullable byte[] x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setBytes(index, x);
        }
    }

    /// Sets the named parameter `name` to the given character [java.io.Reader].
    public default void setCharacterStream(@NonNull String name, @Nullable Reader reader) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setCharacterStream(index, reader);
        }
    }

    /// Sets the named parameter `name` to the given character [java.io.Reader] with the specified character length.
    public default void setCharacterStream(@NonNull String name, @Nullable Reader reader, int length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setCharacterStream(index, reader, length);
        }
    }

    /// Sets the named parameter `name` to the given character [java.io.Reader] with the specified long character length.
    public default void setCharacterStream(@NonNull String name, @Nullable Reader reader, long length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setCharacterStream(index, reader, length);
        }
    }

    /// Sets the named parameter `name` to the given [java.sql.Clob] value.
    public default void setClob(@NonNull String name, @Nullable Clob x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setClob(index, x);
        }
    }

    /// Sets the named parameter `name` to a [java.sql.Clob] whose content is read from the given [java.io.Reader].
    public default void setClob(@NonNull String name, @Nullable Reader reader) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setClob(index, reader);
        }
    }

    /// Sets the named parameter `name` to a [java.sql.Clob] whose content is read from the given [java.io.Reader] up to `length` characters.
    public default void setClob(@NonNull String name, @Nullable Reader reader, long length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setClob(index, reader, length);
        }
    }

    /// {@inheritDoc}
    /// @param parameterIndex {@inheritDoc}
    /// @param x {@inheritDoc}
    /// @throws SQLException If `parameterIndex` does not correspond to a parameter marker in the SQL statement, if a database access error
    /// occurs or this method is called on a closed [PreparedStatement].
    /// @deprecated The class [java.sql.Date] is very badly designed and should never be used anymore.
    /// Prefer the [#setLocalDate(int, LocalDate)] method or the [#setObject(int, Object)] method passing a
    /// [LocalDate] as the second parameter.
    /// @see #setLocalDate(int, LocalDate)
    /// @see #setObject(int, Object)
    @Override
    @Deprecated
    public void setDate(int parameterIndex, @Nullable java.sql.Date x) throws SQLException;

    /// Sets the named parameter `name` to the given deprecated [java.sql.Date] value.
    @Deprecated
    public default void setDate(@NonNull String name, @Nullable java.sql.Date x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDate(index, x);
        }
    }

    /// {@inheritDoc}
    /// @param parameterIndex {@inheritDoc}
    /// @param x {@inheritDoc}
    /// @param cal {@inheritDoc}
    /// @throws SQLException If `parameterIndex` does not correspond to a parameter marker in the SQL statement, if a database access error
    /// occurs or this method is called on a closed [PreparedStatement].
    /// @deprecated The class [java.sql.Date] is very badly designed and should never be used anymore.
    /// Having a companion [Calendar] as a timezone makes things still more confusing and fragile.
    /// Further, having a date without time but with a timezone in non-sense.
    /// Prefer the [#setLocalDate(int, LocalDate)] method or the [#setObject(int, Object)] method passing a
    /// [LocalDate] as the second parameter.
    /// @see #setLocalDate(int, LocalDate)
    /// @see #setLocalDateTime(int, LocalDateTime)
    /// @see #setOffsetDateTime(int, OffsetDateTime)
    /// @see #setZonedDateTime(int, ZonedDateTime)
    /// @see #setInstant(int, Instant)
    /// @see #setObject(int, Object)
    @Override
    @Deprecated
    public void setDate(int parameterIndex, @Nullable java.sql.Date x, @Nullable Calendar cal) throws SQLException;

    /// Sets the named parameter `name` to the given deprecated [java.sql.Date] value using the given [java.util.Calendar].
    @Deprecated
    public default void setDate(@NonNull String name, @Nullable java.sql.Date x, @Nullable Calendar cal) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDate(index, x, cal);
        }
    }

    /// Sets the named parameter `name` to the given `double` value.
    public default void setDouble(@NonNull String name, double x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDouble(index, x);
        }
    }

    /// Sets the positional parameter at `index` to `null` if `x` is empty, or to the contained
    /// `double` value otherwise.
    public default void setDouble(int index, @NonNull OptionalDouble x) throws SQLException {
        if (x.isEmpty()) {
            this.setNull(index, Types.DOUBLE);
        } else {
            this.setDouble(index, x.getAsDouble());
        }
    }

    /// Sets the named parameter `name` to `null` if `x` is empty, or to the contained `double` value otherwise.
    public default void setDouble(@NonNull String name, @NonNull OptionalDouble x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setDouble(index, x);
        }
    }

    /// Sets the named parameter `name` to the given `float` value.
    public default void setFloat(@NonNull String name, float x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setFloat(index, x);
        }
    }

    /// Sets the named parameter `name` to the given `int` value.
    public default void setInt(@NonNull String name, int x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setInt(index, x);
        }
    }

    /// Sets the positional parameter at `index` to `null` if `x` is empty, or to the contained
    /// `int` value otherwise.
    public default void setInt(int index, @NonNull OptionalInt x) throws SQLException {
        if (x.isEmpty()) {
            this.setNull(index, Types.INTEGER);
        } else {
            this.setInt(index, x.getAsInt());
        }
    }

    /// Sets the named parameter `name` to `null` if `x` is empty, or to the contained `int` value otherwise.
    public default void setInt(@NonNull String name, @NonNull OptionalInt x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setInt(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.LocalDate] value.
    public default void setLocalDate(int index, @Nullable LocalDate x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.LocalDate] value.
    public default void setLocalDate(@NonNull String name, @Nullable LocalDate x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLocalDate(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.LocalDateTime] value.
    public default void setLocalDateTime(int index, @Nullable LocalDateTime x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.LocalDateTime] value.
    public default void setLocalDateTime(@NonNull String name, @Nullable LocalDateTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLocalDateTime(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.LocalTime] value.
    public default void setLocalTime(int index, @Nullable LocalTime x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.LocalTime] value.
    public default void setLocalTime(@NonNull String name, @Nullable LocalTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLocalTime(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.OffsetTime] value.
    public default void setOffsetTime(int index, @Nullable OffsetTime x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.OffsetTime] value.
    public default void setOffsetTime(@NonNull String name, @Nullable OffsetTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setOffsetTime(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.OffsetDateTime] value.
    public default void setOffsetDateTime(int index, @Nullable OffsetDateTime x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.OffsetDateTime] value.
    public default void setOffsetDateTime(@NonNull String name, @Nullable OffsetDateTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setOffsetDateTime(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.ZonedDateTime] value.
    public default void setZonedDateTime(int index, @Nullable ZonedDateTime x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.ZonedDateTime] value.
    public default void setZonedDateTime(@NonNull String name, @Nullable ZonedDateTime x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setZonedDateTime(index, x);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.time.Instant] value.
    public default void setInstant(int index, @Nullable Instant x) throws SQLException {
        this.setObject(index, x);
    }

    /// Sets the named parameter `name` to the given [java.time.Instant] value.
    public default void setInstant(@NonNull String name, @Nullable Instant x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setInstant(index, x);
        }
    }

    /// Sets the named parameter `name` to the given `long` value.
    public default void setLong(@NonNull String name, long x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLong(index, x);
        }
    }

    /// Sets the positional parameter at `index` to `null` if `x` is empty, or to the contained
    /// `long` value otherwise.
    public default void setLong(int index, @NonNull OptionalLong x) throws SQLException {
        if (x.isEmpty()) {
            this.setNull(index, Types.BIGINT);
        } else {
            this.setLong(index, x.getAsLong());
        }
    }

    /// Sets the named parameter `name` to `null` if `x` is empty, or to the contained `long` value otherwise.
    public default void setLong(@NonNull String name, @NonNull OptionalLong x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setLong(index, x);
        }
    }

    /// Sets the named parameter `name` to the given national character [java.io.Reader].
    public default void setNCharacterStream(@NonNull String name, @Nullable Reader value) throws SQLException {
        var all = getIndexes(name);
        if (value != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNCharacterStream(index, value);
        }
    }

    /// Sets the named parameter `name` to the given national character [java.io.Reader] with the specified character length.
    public default void setNCharacterStream(@NonNull String name, @Nullable Reader value, long length) throws SQLException {
        var all = getIndexes(name);
        if (value != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNCharacterStream(index, value, length);
        }
    }

    /// Sets the named parameter `name` to the given [java.sql.NClob] value.
    public default void setNClob(@NonNull String name, @Nullable NClob value) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNClob(index, value);
        }
    }

    /// Sets the named parameter `name` to a [java.sql.NClob] whose content is read from the given [java.io.Reader].
    public default void setNClob(@NonNull String name, @Nullable Reader reader) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNClob(index, reader);
        }
    }

    /// Sets the named parameter `name` to a [java.sql.NClob] whose content is read from the given [java.io.Reader] up to `length` characters.
    public default void setNClob(@NonNull String name, @Nullable Reader reader, long length) throws SQLException {
        var all = getIndexes(name);
        if (reader != null && all.size() > 1) throw new SQLException(READER_MESSAGE);
        for (var index : all) {
            this.setNClob(index, reader, length);
        }
    }

    /// Sets the named parameter `name` to the given national [java.lang.String] value.
    public default void setNString(@NonNull String name, @Nullable String value) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNString(index, value);
        }
    }

    /// Sets the named parameter `name` to SQL `NULL` using the given SQL type code.
    public default void setNull(@NonNull String name, int sqlType) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNull(index, sqlType);
        }
    }

    /// Sets the named parameter `name` to SQL `NULL` using the given SQL type code and type name.
    public default void setNull(@NonNull String name, int sqlType, @Nullable String typeName) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setNull(index, sqlType, typeName);
        }
    }

    /// Sets the named parameter `name` to the given object value, letting the driver infer the SQL type.
    public default void setObject(@NonNull String name, @Nullable Object x) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x);
        }
    }

    /// Sets the named parameter `name` to the given object value, converting it to the given [java.sql.SQLType].
    public default void setObject(@NonNull String name, @Nullable Object x, @Nullable SQLType targetType) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType);
        }
    }

    /// Sets the named parameter `name` to the given object value, converting it to the given JDBC type code.
    public default void setObject(@NonNull String name, @Nullable Object x, int targetType) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType);
        }
    }

    /// Sets the named parameter `name` to the given object value, converting it to the given [java.sql.SQLType] with scale or length.
    public default void setObject(@NonNull String name, @Nullable Object x, @Nullable SQLType targetType, int scaleOrLength) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType, scaleOrLength);
        }
    }

    /// Sets the named parameter `name` to the given object value, converting it to the given JDBC type code with scale or length.
    public default void setObject(@NonNull String name, @Nullable Object x, int targetType, int scaleOrLength) throws SQLException {
        var all = getIndexes(name);
        if (x instanceof Reader && all.size() > 1) throw new SQLException(READER_MESSAGE);
        if (x instanceof InputStream && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setObject(index, x, targetType, scaleOrLength);
        }
    }

    /// Sets the named parameter `name` to the given [java.sql.Ref] value.
    public default void setRef(@NonNull String name, @Nullable Ref x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setRef(index, x);
        }
    }

    /// Sets the named parameter `name` to the given [java.sql.RowId] value.
    public default void setRowId(@NonNull String name, @Nullable RowId x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setRowId(index, x);
        }
    }

    /// Sets the named parameter `name` to the given [java.sql.SQLXML] value.
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public default void setSQLXML(@NonNull String name, @Nullable SQLXML xmlObject) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setSQLXML(index, xmlObject);
        }
    }

    /// Sets the positional parameter at `index` to the given [java.sql.Struct] value.
    public default void setStruct(int index, @Nullable Struct x) throws SQLException {
        // This serves two purposes: 1 - Test if createStruct works. 2 - Ensure that the Struct instance passed downwards is compatible.
        var x2 = x == null
                ? this.getConnection().createStruct("test", new Object[] {"test"})
                : this.getConnection().createStruct(x.getSQLTypeName(), x.getAttributes());

        this.setObject(index, x == null ? null : x2);
    }

    /// Sets the named parameter `name` to the given [java.sql.Struct] value.
    public default void setStruct(@NonNull String name, @Nullable Struct x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setStruct(index, x);
        }
    }

    /// Sets the named parameter `name` to the given `short` value.
    public default void setShort(@NonNull String name, short x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setShort(index, x);
        }
    }

    /// Sets the named parameter `name` to the given [java.lang.String] value.
    public default void setString(@NonNull String name, @Nullable String x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setString(index, x);
        }
    }

    /// {@inheritDoc}
    /// @param parameterIndex {@inheritDoc}
    /// @param x {@inheritDoc}
    /// @throws SQLException If `parameterIndex` does not correspond to a parameter marker in the SQL statement, if a database access error
    /// occurs or this method is called on a closed [PreparedStatement].
    /// @deprecated The class [java.sql.Time] is very badly designed and should never be used anymore.
    /// Prefer the [#setLocalTime(int, LocalTime)] method or the [#setObject(int, Object)] method passing a
    /// [LocalTime] as the second parameter.
    /// @see #setLocalTime(int, LocalTime)
    /// @see #setObject(int, Object)
    @Override
    @Deprecated
    public void setTime(int parameterIndex, @Nullable java.sql.Time x) throws SQLException;

    /// Sets the named parameter `name` to the given deprecated [java.sql.Time] value.
    @Deprecated
    public default void setTime(@NonNull String name, @Nullable java.sql.Time x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTime(index, x);
        }
    }

    /// {@inheritDoc}
    /// @param parameterIndex {@inheritDoc}
    /// @param x {@inheritDoc}
    /// @param cal {@inheritDoc}
    /// @throws SQLException If `parameterIndex` does not correspond to a parameter marker in the SQL statement, if a database access error
    /// occurs or this method is called on a closed [PreparedStatement].
    /// @deprecated The class [java.sql.Time] is very badly designed and should never be used anymore.
    /// Having a companion [Calendar] as a timezone makes things still more confusing and fragile.
    /// Prefer the [#setOffsetTime(int, OffsetTime)] method or the [#setObject(int, Object)] method passing a
    /// [OffsetTime] as the second parameter.
    /// @see #setOffsetTime(int, OffsetTime)
    /// @see #setObject(int, Object)
    @Override
    @Deprecated
    public void setTime(int parameterIndex, @Nullable java.sql.Time x, @Nullable Calendar cal) throws SQLException;

    /// Sets the named parameter `name` to the given deprecated [java.sql.Time] value using the given [java.util.Calendar].
    @Deprecated
    public default void setTime(@NonNull String name, @Nullable java.sql.Time x, @Nullable Calendar cal) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTime(index, x, cal);
        }
    }

    /// {@inheritDoc}
    /// @param parameterIndex {@inheritDoc}
    /// @param x {@inheritDoc}
    /// @throws SQLException If `parameterIndex` does not correspond to a parameter marker in the SQL statement, if a database access error
    /// occurs or this method is called on a closed [PreparedStatement].
    /// @deprecated The class [Timestamp] is very badly designed and should never be used anymore.
    /// Prefer the [#setLocalDateTime(int, LocalDateTime)] method or the [#setObject(int, Object)] method passing a
    /// [LocalDateTime] as the second parameter.
    /// @see #setLocalDateTime(int, LocalDateTime)
    /// @see #setObject(int, Object)
    @Override
    @Deprecated
    public void setTimestamp(int parameterIndex, @Nullable Timestamp x) throws SQLException;

    /// Sets the named parameter `name` to the given deprecated [java.sql.Timestamp] value.
    @Deprecated
    public default void setTimestamp(@NonNull String name, @Nullable Timestamp x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTimestamp(index, x);
        }
    }

    /// {@inheritDoc}
    /// @param parameterIndex {@inheritDoc}
    /// @param x {@inheritDoc}
    /// @param cal {@inheritDoc}
    /// @throws SQLException if parameterIndex does not correspond to a parameter
    /// marker in the SQL statement; if a database access error occurs or
    /// this method is called on a closed [PreparedStatement]
    /// @deprecated The class [Timestamp] is very badly designed and should never be used anymore. Having a companion [Calendar]
    /// as a timezone makes things still more confusing and fragile.
    /// Prefer one of the [#setOffsetDateTime(int, OffsetDateTime)], [#setZonedDateTime(int, ZonedDateTime)],
    /// [#setInstant(int, Instant)] methods or perhaps the [#setObject(int, Object)] method passing a
    /// [OffsetDateTime], [ZonedDateTime] or [Instant] as the second parameter.
    /// @see #setOffsetDateTime(int, OffsetDateTime)
    /// @see #setZonedDateTime(int, ZonedDateTime)
    /// @see #setInstant(int, Instant)
    /// @see #setObject(int, Object)
    @Override
    @Deprecated
    public void setTimestamp(int parameterIndex, @Nullable Timestamp x, @Nullable Calendar cal) throws SQLException;

    /// Sets the named parameter `name` to the given deprecated [java.sql.Timestamp] value using the given [java.util.Calendar].
    @Deprecated
    public default void setTimestamp(@NonNull String name, @Nullable Timestamp x, @Nullable Calendar cal) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setTimestamp(index, x, cal);
        }
    }

    /// Sets the named parameter `name` to the given [java.net.URL] value.
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public default void setURL(@NonNull String name, @Nullable URL x) throws SQLException {
        for (var index : getIndexes(name)) {
            this.setURL(index, x);
        }
    }

    /// Sets the named parameter `name` to the given deprecated unicode [java.io.InputStream] with the specified byte length.
    @Deprecated
    public default void setUnicodeStream(@NonNull String name, @Nullable InputStream x, int length) throws SQLException {
        var all = getIndexes(name);
        if (x != null && all.size() > 1) throw new SQLException(INPUT_STREAM_MESSAGE);
        for (var index : all) {
            this.setUnicodeStream(index, x, length);
        }
    }

    /// {@inheritDoc}
    @Override
    public default void receiveNull(@NonNull String name, @NonNull Class<?> type) throws SQLException {
        var nothing = NamedParameterStatementHandler.EMPTY.get(type);
        receiveIn(name, type, nothing);
    }

    /// {@inheritDoc}
    @Override
    public default void receive(@NonNull String name, @NonNull Object value) throws SQLException {
        receiveIn(name, value.getClass(), value);
    }

    private <K> void receiveIn(@NonNull String name, @NonNull Class<K> k, @Nullable Object value) throws SQLException {
        checkNotNull(name);
        checkNotNull(k);
        NamedParameterStatementHandler.forClass(k).handle(NamedParameterStatement.this, name, k.cast(value));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}