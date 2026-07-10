package ninja.javahacker.annotimpler.sql.jdbcstmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

/// Implementation of [NamedParameterStatement] that works as an decorator over a standard [PreparedStatement].
@PackagePrivate
@SuppressWarnings("deprecation")
final class InternalNamedParameterStatement implements NamedParameterStatement {

    /// The wrapped [PreparedStatement] that does the real job.
    @Delegate(types = PreparedStatement.class)
    private final PreparedStatement statement;

    /// The wrapped [PreparedStatement] that does the real job.
    @NonNull
    private final Map<String, List<Integer>> paramMap;

    /// Sole constructor.
    /// @param statement The [PreparedStatement] that should be decorated.
    /// @param params The mapping of names to positional indexes that should be applied.
    /// @throws IllegalArgumentException If `name` or `params` is `null`.
    public InternalNamedParameterStatement(@NonNull PreparedStatement statement, @NonNull Map<String, List<Integer>> params) {
        checkNotNull(statement); // Check recognized by lombok.
        checkNotNull(params); // Check recognized by lombok.
        var copy = params.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> List.copyOf(e.getValue())));
        this.statement = statement;
        this.paramMap = copy;
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // It is guaranteed immutable.
    @SuppressFBWarnings("EI_EXPOSE_REP") // It is guaranteed immutable.
    public Map<String, List<Integer>> getIndexes() {
        return paramMap;
    }

    /// The equivalent of a [Runnable] that might throw any exception.
    @FunctionalInterface
    private static interface HandleVoid {

        /// The action that are wrapped and presented by this object.
        @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_THROWABLE") // The only purpose of this is exactly the throws Throwable.
        public void doIt() throws Throwable;
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    private static void handle(@NonNull HandleVoid h) throws SQLException {
        checkNotNull(h); // Check recognized by lombok.
        try {
            h.doIt();
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    /// {@inheritDoc}
    @Override
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x) throws SQLException {
        handle(() -> statement.setAsciiStream(parameterIndex, x));
    }

    /// {@inheritDoc}
    @Override
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        handle(() -> statement.setAsciiStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x, long length) throws SQLException {
        handle(() -> statement.setAsciiStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x) throws SQLException {
        handle(() -> statement.setBinaryStream(parameterIndex, x));
    }

    /// {@inheritDoc}
    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        handle(() -> statement.setBinaryStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x, long length) throws SQLException {
        handle(() -> statement.setBinaryStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setCharacterStream(parameterIndex, reader));
    }

    /// {@inheritDoc}
    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, int length) throws SQLException {
        handle(() -> statement.setCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setNCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setNCharacterStream(parameterIndex, reader));
    }

    /// {@inheritDoc}
    @Override
    public void setNCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setNCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream) throws SQLException {
        handle(() -> statement.setBlob(parameterIndex, inputStream));
    }

    /// {@inheritDoc}
    @Override
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        handle(() -> statement.setBlob(parameterIndex, LimitedInputStream.wrapNullable(inputStream, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setClob(parameterIndex, reader));
    }

    /// {@inheritDoc}
    @Override
    public void setClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setClob(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    /// {@inheritDoc}
    @Override
    public void setNClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setNClob(parameterIndex, reader));
    }

    /// {@inheritDoc}
    @Override
    public void setNClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setNClob(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    /// {@inheritDoc}
    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        handle(() -> statement.setUnicodeStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
