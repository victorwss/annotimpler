package ninja.javahacker.annotimpler.sql.stmt;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.sql;

@PackagePrivate
@SuppressWarnings("deprecation")
final class InternalNamedParameterStatement implements NamedParameterStatement {

    @Delegate(types = PreparedStatement.class)
    private final PreparedStatement statement;

    @NonNull
    private final Map<String, List<Integer>> paramMap;

    public InternalNamedParameterStatement(@NonNull PreparedStatement statement, @NonNull Map<String, List<Integer>> params) {
        checkNotNull(statement);
        checkNotNull(params);
        var copy = params.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> List.copyOf(e.getValue())));
        this.statement = statement;
        this.paramMap = copy;
    }

    @NonNull
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // É garantido imutável.
    public Map<String, List<Integer>> getIndexes() {
        return paramMap;
    }

    @FunctionalInterface
    private static interface HandleVoid {
        public void doIt() throws Throwable;
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public static void handle(@NonNull HandleVoid h) throws SQLException {
        try {
            h.doIt();
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x) throws SQLException {
        handle(() -> statement.setAsciiStream(parameterIndex, x));
    }

    @Override
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        handle(() -> statement.setAsciiStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    @Override
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x, long length) throws SQLException {
        handle(() -> statement.setAsciiStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x) throws SQLException {
        handle(() -> statement.setBinaryStream(parameterIndex, x));
    }

    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        handle(() -> statement.setBinaryStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x, long length) throws SQLException {
        handle(() -> statement.setBinaryStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length));
    }

    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setCharacterStream(parameterIndex, reader));
    }

    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, int length) throws SQLException {
        handle(() -> statement.setCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    @Override
    public void setNCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setNCharacterStream(parameterIndex, reader));
    }

    @Override
    public void setNCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setNCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    @Override
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream) throws SQLException {
        handle(() -> statement.setBlob(parameterIndex, inputStream));
    }

    @Override
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        handle(() -> statement.setBlob(parameterIndex, LimitedInputStream.wrapNullable(inputStream, length), length));
    }

    @Override
    public void setClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setClob(parameterIndex, reader));
    }

    @Override
    public void setClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setClob(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

    @Override
    public void setNClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        handle(() -> statement.setNClob(parameterIndex, reader));
    }

    @Override
    public void setNClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        handle(() -> statement.setNClob(parameterIndex, LimitedReader.wrapNullable(reader, length), length));
    }

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
