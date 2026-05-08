package ninja.javahacker.annotimpler.limited;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;

/**
 * {@link InputStream} decorator that limits the maximum number of bytes that can be read,
 * with support for mark and reset operations.
 */
public final class LimitedInputStream extends InputStream {

    private final InputStream wrapped;
    private final long maxSize;
    private long position;
    private long markPosition;
    private long markLimit;
    private boolean closed;

    /**
     * Creates a new {@code LimitedInputStream} that wraps the given {@link InputStream}
     * and limits reading to the specified maximum number of bytes.
     *
     * @param wrapped the {@link InputStream} to wrap.
     * @param maxSize the maximum number of bytes that can be read.
     * @throws IllegalArgumentException if {@code maxSize} is negative or if {@code wrapped} is {@code null}.
     */
    public LimitedInputStream(@NonNull InputStream wrapped, long maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException("Maximum size cannot be negative.");

        this.wrapped = wrapped;
        this.maxSize = maxSize;
        this.position = 0;
        this.markPosition = -1;
        this.markLimit = -1;
        this.closed = false;
    }

    @Nullable
    public static LimitedInputStream wrapNullable(@Nullable InputStream in, long length) {
        return in == null ? null : new LimitedInputStream(in, length);
    }

    @Override
    public int read() throws IOException {
        checkClosed();

        if (position >= maxSize) return -1; // End of stream - limit reached.

        var result = wrapped.read();
        if (result != -1) position++;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();

        if (b == null) throw new NullPointerException("Buffer cannot be null.");
        if (off < 0 || len < 0 || off + len > b.length) throw new IndexOutOfBoundsException("Invalid offset or length.");
        if (len == 0) return 0;
        if (position >= maxSize) return -1; // End of stream - limit reached.

        // Calculate how many bytes we can actually read.
        var remaining = maxSize - position;
        var toRead = (int) Math.min(len, remaining);

        var actuallyRead = wrapped.read(b, off, toRead);
        position += Math.max(0L, actuallyRead);
        if (position > markLimit) {
            markPosition = -1;
            markLimit = -1;
        }
        return actuallyRead;
    }

    @Override
    public long skip(long n) throws IOException {
        checkClosed();

        if (n <= 0) return 0;
        if (position >= maxSize) return 0; // Limit already reached.

        // Calculate how many bytes we can actually skip.
        var remaining = maxSize - position;
        var toSkip = Math.min(n, remaining);

        var actuallySkipped = wrapped.skip(toSkip);
        position += Math.max(0L, actuallySkipped);
        if (position > markLimit) {
            markPosition = -1;
            markLimit = -1;
        }
        return actuallySkipped;
    }

    @Override
    public int available() throws IOException {
        checkClosed();

        if (position >= maxSize) return 0; // Limit reached.

        var available = wrapped.available();
        var remainingBytes = maxSize - position;

        return (int) Math.min(available, remainingBytes);
    }

    @Override
    public void mark(int readlimit) {
        if (closed || !wrapped.markSupported()) return; // Silently ignore if closed or not supported.

        wrapped.mark(readlimit);
        markPosition = position;
        markLimit = position + readlimit;
    }

    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    @Override
    public void reset() throws IOException {
        //throw new IOException("Mark/reset not supported.");
        checkClosed();

        if (!isMarkSet()) throw new IOException("Mark not set.");

        wrapped.reset();
        position = markPosition;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            wrapped.close();
        }
    }

    /**
     * Returns the number of bytes read so far.
     *
     * @return the number of bytes read.
     * @throws IOException If closed.
     */
    public long getPosition() throws IOException {
        checkClosed();
        return position;
    }

    /**
     * Returns the maximum number of bytes that can be read.
     *
     * @return the maximum number of bytes.
     * @throws IOException If closed.
     */
    public long getMaxSize() throws IOException {
        checkClosed();
        return maxSize;
    }

    /**
     * Returns the remaining number of bytes that can be read.
     *
     * @return the remaining bytes.
     * @throws IOException If closed.
     */
    public long getRemaining() throws IOException {
        checkClosed();
        return maxSize - position;
    }

    /**
     * Checks if a mark has been set.
     *
     * @return {@code true} if a mark is set, {@code false} otherwise.
     * @throws IOException If closed.
     */
    public boolean isMarkSet() throws IOException {
        checkClosed();
        return markPosition >= 0;
    }

    /**
     * Checks if the stream was closed.
     *
     * @return {@code true} if this was closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

    private void checkClosed() throws IOException {
        if (closed) throw new IOException("Stream is closed.");
    }
}