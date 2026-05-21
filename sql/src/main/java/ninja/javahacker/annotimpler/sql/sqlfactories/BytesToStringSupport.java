package ninja.javahacker.annotimpler.sql.sqlfactories;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public final class BytesToStringSupport {

    private BytesToStringSupport() {
        throw new UnsupportedOperationException();
    }

    public static String make(@NonNull byte[] input, @NonNull Charset charset) throws IOException {
        var buf = ByteBuffer.wrap(input);
        try {
            var cb = charset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).decode(buf);
            return new String(cb.array(), 0, cb.length());
        } catch (IOException e) {
            throw new IOException("String can't be coded as " + charset.displayName(Locale.ROOT) + ".");
        }
    }
}