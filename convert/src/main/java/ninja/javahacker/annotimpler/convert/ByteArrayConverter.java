package ninja.javahacker.annotimpler.convert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Optional;
import lombok.NonNull;

/// A [Converter] for `byte[]` values.
///
/// Supported conversions: `boolean` (single byte 1/0), `byte` (single byte),
/// [String] (UTF-8), `byte[]`, [Blob]/[Clob]/[NClob]/[SQLXML] (read as bytes), [RowId] (raw bytes).
/// Returns an empty byte array for `null` input.
@SuppressFBWarnings("ENMI_ONE_ENUM_VALUE")
public enum ByteArrayConverter implements Converter<byte[]> {

    /// Singleton instance.
    INSTANCE;

    /// Returns `byte[].class`.
    ///
    /// @return `byte[].class`.
    @NonNull
    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

    /// Returns `Optional.of(new byte[0])` (an empty byte array).
    @NonNull
    @Override
    public Optional<byte[]> fromNull() {
        return Optional.of(new byte[0]);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(boolean in) {
        return Optional.of(new byte[] {in ? (byte) 1 : (byte) 0});
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(byte in) {
        return Optional.of(new byte[] {in});
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull String in) {
        return Optional.of(in.getBytes(StandardCharsets.UTF_8));
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull byte[] in) {
        return Optional.of(in);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull Blob in) throws ConvertionException {
        try {
            return Optional.of(in.getBinaryStream().readAllBytes());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, Blob.class, byte[].class);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString().getBytes(StandardCharsets.UTF_8));
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, Clob.class, byte[].class);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString().getBytes(StandardCharsets.UTF_8));
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, NClob.class, byte[].class);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull SQLXML in) throws ConvertionException {
        try {
            return Optional.of(in.getString().getBytes(StandardCharsets.UTF_8));
        } catch (SQLException x) {
            throw new ConvertionException(x, SQLXML.class, byte[].class);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull RowId in) {
        return Optional.of(in.getBytes());
    }
}
