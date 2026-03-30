package ninja.javahacker.annotimpler.limited;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;

/**
 * LimitedReader with mark/reset support.
 */
public final class LimitedReader extends Reader {

    private final Reader wrapped;
    private final long maxSize;
    private long position;
    private long markPosition;
    private long markLimit;
    private boolean closed;

    /**
     * Creates a new {@code LimitedReader} that wraps the given {@link Reader}
     * and limits reading to the specified maximum number of bytes.
     *
     * @param wrapped the {@link Reader} to wrap.
     * @param maxSize the maximum number of characters that can be read.
     * @throws IllegalArgumentException if {@code maxSize} is negative or if {@code wrapped} is {@code null}.
     */
    public LimitedReader(@NonNull Reader wrapped, long maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException("Maximum size cannot be negative.");

        this.wrapped = wrapped;
        this.maxSize = maxSize;
        this.position = 0;
        this.markPosition = -1;
        this.markLimit = -1;
        this.closed = false;
    }

    @Nullable
    public static LimitedReader wrapNullable(@Nullable Reader in, long length) {
        return in == null ? null : new LimitedReader(in, length);
    }

    @Override
    public int read() throws IOException {
        checkClosed();

        if (position >= maxSize) return -1; // End of reader - limit reached.

        var result = wrapped.read();
        if (result != -1) position++;
        return result;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        checkClosed();

        if (cbuf == null) throw new NullPointerException("Buffer cannot be null.");
        if (off < 0 || len < 0 || off + len > cbuf.length) throw new IndexOutOfBoundsException("Invalid offset or length.");
        if (len == 0) return 0;
        if (position >= maxSize) return -1; // End of reader - limit reached.

        // Calculate how many chars we can actually read.
        var remaining = maxSize - position;
        var toRead = (int) Math.min(len, remaining);

        var actuallyRead = wrapped.read(cbuf, off, toRead);
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

        // Calculate how many characters we can actually skip.
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
    public boolean ready() throws IOException {
        checkClosed();
        return position == maxSize || wrapped.ready();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        checkClosed();
        if (!wrapped.markSupported()) throw new IOException("Wrapped reader does not support mark.");

        wrapped.mark(readAheadLimit); // Might throw IOException.
        markPosition = position;
        markLimit = position + readAheadLimit;
    }

    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    @Override
    public void reset() throws IOException {
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
     * Returns the number of characters read so far.
     *
     * @return the number of characters read.
     * @throws IOException If closed.
     */
    public long getPosition() throws IOException {
        checkClosed();
        return position;
    }

    /**
     * Returns the maximum number of characters that can be read.
     *
     * @return the maximum number of characters.
     * @throws IOException If closed.
     */
    public long getMaxSize() throws IOException {
        checkClosed();
        return maxSize;
    }

    /**
     * Returns the remaining number of characters that can be read.
     *
     * @return the remaining characters.
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
        if (closed) throw new IOException("Reader is closed.");
    }
}