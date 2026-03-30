package ninja.javahacker.test.limited;

import module java.base;

/**
 * {@link Reader} that throws {@link AssertionError} if read operations exceed the allowed limit.
 * Also tracks all read operations for verification.
 */
public final class AssertionReader extends Reader {
    private final String data;
    private int position;
    private int markPosition;
    private int markLimit;
    private final int maxAllowedReads;
    private final List<ReadOperation> readOperations;
    private final boolean allowMark;
    private boolean closed;

    public static enum OperationType {
        READ, SKIP, MARK, RESET;
    }

    public static final class ReadOperation {
        public final long position;
        public final long requestedLength;
        public final long actualLength;
        public final OperationType type;

        public ReadOperation(long position, long requestedLength, long actualLength, OperationType type) {
            this.position = position;
            this.requestedLength = requestedLength;
            this.actualLength = actualLength;
            this.type = type;
        }

        @Override
        public String toString() {
            return type + "[requested=" + requestedLength + ", actual=" + actualLength + "]";
        }
    }

    public AssertionReader(String data, int maxAllowedReads, boolean allowMark) {
        if (maxAllowedReads > data.length()) throw new AssertionError();
        this.data = data;
        this.maxAllowedReads = maxAllowedReads;
        this.position = 0;
        this.readOperations = new ArrayList<>();
        this.markPosition = -1;
        this.markLimit = -1;
        this.allowMark = allowMark;
        this.closed = false;
    }

    @Override
    public int read() throws IOException {
        if (closed) throw new IOException();
        var buffer = new char[1];
        read(buffer, 0, 1);
        return buffer[0];
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (closed) throw new IOException();
        readOperations.add(new ReadOperation(position, len, 0, OperationType.READ));

        if (len > 0 && position + len > maxAllowedReads) {
            throw new AssertionError(
                String.format(
                        "Attempted to read beyond limit! Already read %d chars, max allowed is %d. Attempting to read %d more chars.",
                        position,
                        maxAllowedReads,
                        len
                )
            );
        }

        data.getChars(position, position + len, cbuf, off);
        position += len;

        // Update the actual chars read in the last operation.
        readOperations.set(readOperations.size() - 1, new ReadOperation(position, len, len, OperationType.READ));

        if (position > markLimit) {
            markPosition = -1;
            markLimit = -1;
        }

        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        if (closed) throw new IOException();
        // Register a skip attempt.
        readOperations.add(new ReadOperation(position, n, 0, OperationType.SKIP));

        if (n <= 0) return 0;
        if (position >= maxAllowedReads) return 0;

        var toSkip = Math.min(n, maxAllowedReads - position);
        toSkip = Math.min(toSkip, data.length() - position);

        if (toSkip <= 0) return 0;

        position += (int) toSkip;

        // Update the actual chars skipped in the last operation.
        readOperations.set(readOperations.size() - 1, new ReadOperation(position, n, toSkip, OperationType.SKIP));

        if (position > markLimit) {
            markPosition = -1;
            markLimit = -1;
        }

        return toSkip;
    }

    @Override
    public boolean ready() throws IOException {
        if (closed) throw new IOException();
        return true;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public boolean markSupported() {
        return allowMark;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (closed || !allowMark) throw new IOException();
        readOperations.add(new ReadOperation(position, readAheadLimit, readAheadLimit, OperationType.MARK));
        markPosition = position;
        markLimit = markPosition + readAheadLimit;
    }

    @Override
    public void reset() throws IOException {
        if (closed || markPosition == -1) throw new IOException();
        readOperations.add(new ReadOperation(position, 0, 0, OperationType.RESET));
        position = markPosition;
    }

    public List<ReadOperation> getReadOperations() {
        return List.copyOf(readOperations);
    }

    public void resetReadOperations() {
        readOperations.clear();
    }

    public int getPosition() {
        return position;
    }

    public void resetState() {
        position = 0;
        markPosition = -1;
        markLimit = -1;
        readOperations.clear();
    }

    public boolean isClosed() {
        return closed;
    }
}
