package ninja.javahacker.annotimpler.limited;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

import module java.base;

/// [Reader] decorator that limits the maximum number of characters that can be read,
/// with support for mark and reset operations.
@SuppressWarnings("PMD.TooManyMethods")
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public final class LimitedReader extends Reader {

    /// The [Reader] instance that was wrapped. I.e., the one that is being limited.
    @NonNull
    private final Reader wrapped;

    /// The limited maximum size of this reader.
    private final long maxSize;

    /// The position where this reader is being read.
    private long position;

    /// The position of the mark.
    private long markPosition;

    /// The `readAheadLimit` limit of the mark.
    private long markLimit;

    /// If this instance was closed.
    private boolean closed;

    /// Creates a new `LimitedReader` that wraps the given [Reader]
    /// and limits reading to the specified maximum number of bytes.
    ///
    /// @param wrapped The [Reader] to wrap.
    /// @param maxSize The maximum number of characters that can be read.
    /// @throws IllegalArgumentException If `wrapped` is `null` or if `maxSize` is negative.
    public LimitedReader(@NonNull Reader wrapped, long maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException("Maximum size cannot be negative.");

        this.wrapped = wrapped;
        this.maxSize = maxSize;
        this.position = 0;
        this.markPosition = -1;
        this.markLimit = -1;
        this.closed = false;
    }

    /// Creates a new `LimitedReader` wrapping the given [Reader],
    /// or returns `null` if the input is `null`.
    ///
    /// @param in The [Reader] to wrap, or `null`.
    /// @param length The maximum number of characters that can be read.
    /// @return A new `LimitedReader` wrapping `in` and limited to `length` characters,
    ///         or `null` if `in` is `null`.
    @Nullable
    public static LimitedReader wrapNullable(@Nullable Reader in, long length) {
        return in == null ? null : new LimitedReader(in, length);
    }

    /// Reads a single character from this reader.
    ///
    /// Returns `-1` if the read limit has been reached or if the end of the
    /// wrapped reader has been reached.
    ///
    /// @return The character read as an integer in the range `0` to `65535`,
    ///         or `-1` if the read limit or the end of the reader has been reached.
    /// @throws IOException If this reader is closed or if an I/O error occurs.
    @Override
    public int read() throws IOException {
        checkClosed();

        if (position >= maxSize) return -1; // End of reader - limit reached.

        var result = wrapped.read();
        if (result != -1) position++;
        return result;
    }

    /// Reads up to `len` characters from this reader into the given array, starting at offset
    /// `off`, reading at most as many characters as allowed by the remaining read limit.
    ///
    /// @param cbuf The buffer into which the data is read.
    /// @param off The start offset in the buffer at which to write the data.
    /// @param len The maximum number of characters to read.
    /// @return The total number of characters read into the buffer, or `-1` if the read limit
    ///         or the end of the reader has been reached.
    /// @throws NullPointerException If `cbuf` is `null`.
    /// @throws IndexOutOfBoundsException If `off` is negative, `len` is negative,
    ///         or `off + len` exceeds the length of `cbuf`.
    /// @throws IOException If this reader is closed or if an I/O error occurs.
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.AvoidThrowingNullPointerException"})
    public int read(/*@NonNull*/ char[] cbuf, int off, int len) throws IOException {
        if (cbuf == null) throw new NullPointerException("Buffer cannot be null.");
        checkClosed();

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

    /// Skips up to `n` characters from this reader, skipping at most as many characters
    /// as allowed by the remaining read limit.
    ///
    /// @param n The number of characters to skip.
    /// @return The actual number of characters skipped, which may be zero if `n` is non-positive,
    ///         the read limit has been reached, or the wrapped reader has no more characters to skip.
    /// @throws IOException If this reader is closed or if an I/O error occurs.
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

    /// Returns whether this reader is ready to be read without blocking.
    ///
    /// Returns `true` if the read limit has already been reached, since such a read
    /// would return `-1` immediately. Otherwise, delegates to the wrapped reader.
    ///
    /// @return `true` if the read limit has been reached or if the wrapped reader is ready,
    ///         `false` otherwise.
    /// @throws IOException If this reader is closed or if an I/O error occurs.
    @Override
    public boolean ready() throws IOException {
        checkClosed();
        return position >= maxSize || wrapped.ready();
    }

    /// Marks the current read position in this reader.
    ///
    /// @param readAheadLimit The maximum number of characters that may be read before the mark
    ///                       position becomes invalid.
    /// @throws IOException If this reader is closed, the wrapped reader does not support marking,
    ///         or an I/O error occurs.
    @Override
    public void mark(int readAheadLimit) throws IOException {
        checkClosed();
        if (!wrapped.markSupported()) throw new IOException("Wrapped reader does not support mark.");

        wrapped.mark(readAheadLimit); // Might throw IOException.
        markPosition = position;
        markLimit = position + readAheadLimit;
    }

    /// Returns whether this reader supports the [#mark(int)] and [#reset()] operations.
    ///
    /// @return `true` if the wrapped reader supports mark and reset, `false` otherwise.
    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    /// Repositions this reader to the position recorded by the last call to [#mark(int)].
    ///
    /// @throws IOException If this reader is closed, no mark has been set,
    ///         or an I/O error occurs in the wrapped reader.
    @Override
    public void reset() throws IOException {
        checkClosed();

        if (!isMarkSet()) throw new IOException("Mark not set.");

        wrapped.reset();
        position = markPosition;
    }

    /// Closes this reader and releases any system resources associated with it.
    ///
    /// Subsequent calls to read, skip, ready, or other I/O methods on this reader
    /// will throw an [IOException]. Calling this method on an already-closed reader
    /// has no effect.
    ///
    /// @throws IOException If an I/O error occurs while closing the wrapped reader.
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            wrapped.close();
        }
    }

    /// Returns the number of characters read so far.
    ///
    /// @return The number of characters read so far.
    /// @throws IOException If this reader is closed.
    public long getPosition() throws IOException {
        checkClosed();
        return position;
    }

    /// Returns the maximum number of characters that can be read.
    ///
    /// @return The maximum number of characters.
    /// @throws IOException If this reader is closed.
    public long getMaxSize() throws IOException {
        checkClosed();
        return maxSize;
    }

    /// Returns the remaining number of characters that can be read before the read limit is reached.
    ///
    /// @return The remaining number of characters that can be read.
    /// @throws IOException If this reader is closed.
    public long getRemaining() throws IOException {
        checkClosed();
        return maxSize - position;
    }

    /// Checks if a mark has been set.
    ///
    /// @return `true` if a mark is currently set, `false` otherwise.
    /// @throws IOException If this reader is closed.
    public boolean isMarkSet() throws IOException {
        checkClosed();
        return markPosition >= 0;
    }

    /// Returns whether this reader has been closed.
    ///
    /// @return `true` if this reader has been closed, `false` otherwise.
    public boolean isClosed() {
        return closed;
    }

    private void checkClosed() throws IOException {
        if (closed) throw new IOException("Reader is closed.");
    }
}