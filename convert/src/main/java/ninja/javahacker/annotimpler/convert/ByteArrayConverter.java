package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum ByteArrayConverter implements Converter<byte[]> {
    INSTANCE;

    @NonNull
    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Optional<byte[]> fromNull() {
        return Optional.of(new byte[0]);
    }

    @NonNull
    @Override
    public Optional<byte[]> from(boolean in) {
        return Optional.of(new byte[] {in ? (byte) 1 : (byte) 0});
    }

    @NonNull
    @Override
    public Optional<byte[]> from(byte in) {
        return Optional.of(new byte[] {in});
    }

    @NonNull
    @Override
    public Optional<byte[]> from(short in) {
        return Optional.of(ByteBuffer.allocate(2).putShort(in).array());
    }

    @NonNull
    @Override
    public Optional<byte[]> from(int in) {
        return Optional.of(ByteBuffer.allocate(4).putInt(in).array());
    }

    @NonNull
    @Override
    public Optional<byte[]> from(long in) {
        return Optional.of(ByteBuffer.allocate(8).putLong(in).array());
    }

    @NonNull
    @Override
    public Optional<byte[]> from(float in) {
        return Optional.of(ByteBuffer.allocate(4).putFloat(in).array());
    }

    @NonNull
    @Override
    public Optional<byte[]> from(double in) {
        return Optional.of(ByteBuffer.allocate(8).putDouble(in).array());
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull String in) {
        return Optional.of(in.getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull byte[] in) {
        return Optional.of(in);
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull Blob in) throws ConvertionException {
        try {
            return Optional.of(in.getBinaryStream().readAllBytes());
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, Blob.class, byte[].class);
        }
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString().getBytes(StandardCharsets.UTF_8));
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, Clob.class, byte[].class);
        }
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(in.getCharacterStream().readAllAsString().getBytes(StandardCharsets.UTF_8));
        } catch (SQLException | IOException x) {
            throw new ConvertionException(x, NClob.class, byte[].class);
        }
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull SQLXML in) throws ConvertionException {
        try {
            return Optional.of(in.getString().getBytes(StandardCharsets.UTF_8));
        } catch (SQLException x) {
            throw new ConvertionException(x, SQLXML.class, byte[].class);
        }
    }

    @NonNull
    @Override
    public Optional<byte[]> from(@NonNull RowId in) {
        return Optional.of(in.getBytes());
    }
}
