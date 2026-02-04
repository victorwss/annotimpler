package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class ByteArrayConverter implements Converter<byte[]> {

    public ByteArrayConverter() {
    }

    @Override
    public byte[] from(boolean in) {
        return new byte[] {in ? (byte) 1 : (byte) 0};
    }

    @Override
    public byte[] from(byte in) {
        return new byte[] {in};
    }

    @Override
    public byte[] from(short in) {
        return ByteBuffer.allocate(2).putShort(in).array();
    }

    @Override
    public byte[] from(int in) {
        return ByteBuffer.allocate(4).putInt(in).array();
    }

    @Override
    public byte[] from(long in) {
        return ByteBuffer.allocate(8).putLong(in).array();
    }

    @Override
    public byte[] from(float in) {
        return ByteBuffer.allocate(4).putFloat(in).array();
    }

    @Override
    public byte[] from(double in) {
        return ByteBuffer.allocate(8).putDouble(in).array();
    }

    @Override
    public byte[] from(@NonNull String in) {
        return in.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] from(@NonNull byte[] in) {
        return in;
    }

    @Override
    public byte[] from(@NonNull Blob in) {
        try {
            return in.getBinaryStream().readAllBytes();
        } catch (SQLException | IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public byte[] from(@NonNull Clob in) {
        try {
            return in.getCharacterStream().readAllAsString().getBytes(StandardCharsets.UTF_8);
        } catch (SQLException | IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public byte[] from(@NonNull NClob in) {
        try {
            return in.getCharacterStream().readAllAsString().getBytes(StandardCharsets.UTF_8);
        } catch (SQLException | IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public byte[] from(@NonNull SQLXML in) {
        try {
            return in.getString().getBytes(StandardCharsets.UTF_8);
        } catch (SQLException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public byte[] from(@NonNull RowId in) {
        return in.getBytes();
    }
}
