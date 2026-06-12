package ninja.javahacker.annotimpler.limited;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;

/// [InputStream] decorator that limits the maximum number of bytes that can be read,
/// with support for mark and reset operations.
public final class LimitedInputStream extends InputStream {

    @NonNull
    private final InputStream wrapped;

    private final long maxSize;
    private long position;
    private long markPosition;
    private long markLimit;
    private boolean closed;

    /// Creates a new `LimitedInputStream` that wraps the given [InputStream]
    /// and limits reading to the specified maximum number of bytes.
    ///
    /// @param wrapped The [InputStream] to wrap.
    /// @param maxSize The maximum number of bytes that can be read.
    /// @throws IllegalArgumentException If `wrapped` is `null` or if `maxSize` is negative.
    public LimitedInputStream(@NonNull InputStream wrapped, long maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException("Maximum size cannot be negative.");

        this.wrapped = wrapped;
        this.maxSize = maxSize;
        this.position = 0;
        this.markPosition = -1;
        this.markLimit = -1;
        this.closed = false;
    }

    /// Creates a new `LimitedInputStream` wrapping the given [InputStream],
    /// or returns `null` if the input is `null`.
    ///
    /// @param in The [InputStream] to wrap, or `null`.
    /// @param length The maximum number of bytes that can be read.
    /// @return A new `LimitedInputStream` wrapping `in` and limited to `length` bytes,
    ///         or `null` if `in` is `null`.
    @Nullable
    public static LimitedInputStream wrapNullable(@Nullable InputStream in, long length) {
        return in == null ? null : new LimitedInputStream(in, length);
    }

    /// Reads the next byte of data from this stream.
    ///
    /// Returns `-1` if the read limit has been reached or if the end of the wrapped
    /// stream has been reached.
    ///
    /// @return The next byte of data as an integer in the range `0` to `255`,
    ///         or `-1` if the read limit or the end of the stream has been reached.
    /// @throws IOException If this stream is closed or if an I/O error occurs.
    @Override
    public int read() throws IOException {
        checkClosed();

        if (position >= maxSize) return -1; // End of stream - limit reached.

        var result = wrapped.read();
        if (result != -1) position++;
        return result;
    }

    /// Reads up to `len` bytes from this stream into the given byte array, starting at offset
    /// `off`, reading at most as many bytes as allowed by the remaining read limit.
    ///
    /// @param b The buffer into which the data is read.
    /// @param off The start offset in the buffer at which to write the data.
    /// @param len The maximum number of bytes to read.
    /// @return The total number of bytes read into the buffer, or `-1` if the read limit
    ///         or the end of the stream has been reached.
    /// @throws NullPointerException If `b` is `null`.
    /// @throws IndexOutOfBoundsException If `off` is negative, `len` is negative,
    ///         or `off + len` exceeds the length of `b`.
    /// @throws IOException If this stream is closed or if an I/O error occurs.
    @Override
    public int read(/*@NonNull*/ byte[] b, int off, int len) throws IOException {
        if (b == null) throw new NullPointerException("Buffer cannot be null.");
        checkClosed();

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

    /// Skips up to `n` bytes from this stream, skipping at most as many bytes as
    /// allowed by the remaining read limit.
    ///
    /// @param n The number of bytes to skip.
    /// @return The actual number of bytes skipped, which may be zero if `n` is non-positive,
    ///         the read limit has been reached, or the wrapped stream has no more bytes to skip.
    /// @throws IOException If this stream is closed or if an I/O error occurs.
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

    /// Returns an estimate of the number of bytes that can be read without blocking,
    /// capped by the remaining read limit.
    ///
    /// @return An estimate of the number of bytes available without blocking,
    ///         or `0` if the read limit has already been reached.
    /// @throws IOException If this stream is closed or if an I/O error occurs.
    @Override
    public int available() throws IOException {
        checkClosed();

        if (position >= maxSize) return 0; // Limit reached.

        var available = wrapped.available();
        var remainingBytes = maxSize - position;

        return (int) Math.min(available, remainingBytes);
    }

    /// Marks the current read position, provided marking is supported by the wrapped stream.
    ///
    /// If this stream is already closed or the wrapped stream does not support marking,
    /// this method does nothing.
    ///
    /// @param readlimit The maximum number of bytes that may be read before the mark position
    ///                  becomes invalid.
    @Override
    public void mark(int readlimit) {
        if (closed || !wrapped.markSupported()) return; // Silently ignore if closed or not supported.

        wrapped.mark(readlimit);
        markPosition = position;
        markLimit = position + readlimit;
    }

    /// Returns whether this stream supports the [#mark(int)] and [#reset()] operations.
    ///
    /// @return `true` if the wrapped stream supports mark and reset, `false` otherwise.
    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    /// Repositions this stream to the position recorded by the last call to [#mark(int)].
    ///
    /// @throws IOException If this stream is closed, no mark has been set,
    ///         or an I/O error occurs in the wrapped stream.
    @Override
    public void reset() throws IOException {
        checkClosed();

        if (!isMarkSet()) throw new IOException("Mark not set.");

        wrapped.reset();
        position = markPosition;
    }

    /// Closes this stream and releases any system resources associated with it.
    ///
    /// Subsequent calls to read, skip, available, or other I/O methods on this stream
    /// will throw an [IOException]. Calling this method on an already-closed stream
    /// has no effect.
    ///
    /// @throws IOException If an I/O error occurs while closing the wrapped stream.
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            wrapped.close();
        }
    }

    /// Returns the number of bytes read so far.
    ///
    /// @return The number of bytes read so far.
    /// @throws IOException If this stream is closed.
    public long getPosition() throws IOException {
        checkClosed();
        return position;
    }

    /// Returns the maximum number of bytes that can be read.
    ///
    /// @return The maximum number of bytes.
    /// @throws IOException If this stream is closed.
    public long getMaxSize() throws IOException {
        checkClosed();
        return maxSize;
    }

    /// Returns the remaining number of bytes that can be read before the read limit is reached.
    ///
    /// @return The remaining number of bytes that can be read.
    /// @throws IOException If this stream is closed.
    public long getRemaining() throws IOException {
        checkClosed();
        return maxSize - position;
    }

    /// Checks if a mark has been set.
    ///
    /// @return `true` if a mark is currently set, `false` otherwise.
    /// @throws IOException If this stream is closed.
    public boolean isMarkSet() throws IOException {
        checkClosed();
        return markPosition >= 0;
    }

    /// Returns whether this stream has been closed.
    ///
    /// @return `true` if this stream has been closed, `false` otherwise.
    public boolean isClosed() {
        return closed;
    }

    private void checkClosed() throws IOException {
        if (closed) throw new IOException("Stream is closed.");
    }
}