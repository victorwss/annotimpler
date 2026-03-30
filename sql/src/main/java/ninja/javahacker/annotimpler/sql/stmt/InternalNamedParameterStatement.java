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

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x) throws SQLException {
        try {
            statement.setAsciiStream(parameterIndex, x);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        try {
            statement.setAsciiStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setAsciiStream(int parameterIndex, @Nullable InputStream x, long length) throws SQLException {
        try {
            statement.setAsciiStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x) throws SQLException {
        try {
            statement.setBinaryStream(parameterIndex, x);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        try {
            statement.setBinaryStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setBinaryStream(int parameterIndex, @Nullable InputStream x, long length) throws SQLException {
        try {
            statement.setBinaryStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        try {
            statement.setCharacterStream(parameterIndex, reader);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, int length) throws SQLException {
        try {
            statement.setCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        try {
            statement.setCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setNCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        try {
            statement.setNCharacterStream(parameterIndex, reader);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setNCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        try {
            statement.setNCharacterStream(parameterIndex, LimitedReader.wrapNullable(reader, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream) throws SQLException {
        try {
            statement.setBlob(parameterIndex, inputStream);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        try {
            statement.setBlob(parameterIndex, LimitedInputStream.wrapNullable(inputStream, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        try {
            statement.setClob(parameterIndex, reader);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        try {
            statement.setClob(parameterIndex, LimitedReader.wrapNullable(reader, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setNClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        try {
            statement.setNClob(parameterIndex, reader);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setNClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        try {
            statement.setNClob(parameterIndex, LimitedReader.wrapNullable(reader, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Override
    @Deprecated
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void setUnicodeStream(int parameterIndex, @Nullable InputStream x, int length) throws SQLException {
        try {
            statement.setUnicodeStream(parameterIndex, LimitedInputStream.wrapNullable(x, length), length);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLException(e);
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
