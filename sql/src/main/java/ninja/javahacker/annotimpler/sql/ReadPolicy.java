package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

/// Controls when and how often a SQL string is loaded from an external source such as a
/// file or a URL.
///
/// Each constant corresponds to a different caching strategy. More eager strategies provide
/// better runtime performance at the cost of detecting load errors later or not retrying on
/// transient failures.
public enum ReadPolicy {

    /// Reads the SQL string once, eagerly, at the time the annotated interface is first
    /// processed (i.e., at `create()` time).
    ///
    /// Any [java.io.IOException] encountered during reading causes a
    /// [BadImplementationException] to be thrown immediately. This is the safest
    /// option when the source is expected to be available at startup.
    ON_STARTUP(ReadPolicy::onStartup),

    /// Reads the SQL string lazily on the first successful call from any thread, then caches
    /// the result globally for all subsequent calls.
    ///
    /// If the read attempt fails, the error is propagated as a [java.sql.SQLException] and
    /// the result is not cached — the next call (from any thread) will retry.
    /// Once any call succeeds, the result is cached and all further calls return it instantly.
    ON_FIRST_TIME_THAT_WORKS(ReadPolicy::onFirstTimeThatWorks),

    /// Reads the SQL string lazily on the very first call from any thread, then caches the
    /// outcome globally — whether it is a success or a failure.
    ///
    /// Unlike [#ON_FIRST_TIME_THAT_WORKS], a failed read is also cached: all subsequent
    /// calls (from any thread) will keep throwing the same [java.sql.SQLException] without
    /// ever retrying.
    ON_FIRST_TIME_DONT_RETRY(ReadPolicy::onFirstTimeDontRetry),

    /// Reads the SQL string on every method invocation, with no caching.
    ///
    /// Changes to the source are picked up immediately, at the cost of an I/O operation on
    /// each call. Any [java.io.IOException] is wrapped in a [java.sql.SQLException].
    EVERY_TIME(ReadPolicy::everyTime);

    /// The method reference of the actual behaviour wrapped by this object.
    private final ReadPolicyStrategy strategy;

    /// Sole constructor.
    ///
    /// @param strategy The behaviour of the constructed object.
    private ReadPolicy(@NonNull ReadPolicyStrategy strategy) {
        checkNotNull(strategy); // Check recognized by lombok.
        this.strategy = strategy;
    }

    /// Wraps a method reference for the actual internal behaviour of a [ReadPolicy] instance about the [#prepare(Impl, E)] method.
    /// Refer to the Strategy design pattern.
    @FunctionalInterface
    private static interface ReadPolicyStrategy {

        /// The behaviour of some [ReadPolicy] instance. Creates some implementation of a [SqlSupplier].
        /// @param <E> The type of the input data consumed by `impl`.
        /// @param impl The reader that extracts the SQL string from `inputData`.
        /// @param inputData The data passed to the reader (e.g., an annotation instance).
        /// @return A [SqlSupplier] that delivers the SQL string according to this policy; never `null`.
        /// @throws BadImplementationException If this policy reads eagerly and the source cannot be read.
        /// @throws IllegalArgumentException If `impl` or `inputData` is `null`.
        public <E> SqlSupplier apply(StringExtractor<E> impl, E in) throws BadImplementationException;
    }

    /// A reader that extracts a SQL string from an input of type `E`.
    ///
    /// @param <E> The type of the input (e.g., an annotation instance).
    @FunctionalInterface
    public static interface StringExtractor<E> {

        /// Reads and returns the SQL string from the given input.
        ///
        /// @param in The input to read from.
        /// @return The SQL string.
        /// @throws IOException If an I/O error occurs while reading.
        /// @throws CharsetSpec.BadCharsetSpecException If the content cannot be decoded with the
        ///         configured character set.
        public String read(E in) throws IOException, CharsetSpec.BadCharsetSpecException;
    }

    /// Applies this read policy to produce a [SqlSupplier] for the given reader and input data.
    ///
    /// @param <E> The type of the input data consumed by `impl`.
    /// @param impl The reader that extracts the SQL string from `inputData`.
    /// @param inputData The data passed to the reader (e.g., an annotation instance).
    /// @return A [SqlSupplier] that delivers the SQL string according to this policy; never `null`.
    /// @throws BadImplementationException If this policy reads eagerly and the source cannot be read.
    /// @throws IllegalArgumentException If `impl` or `inputData` is `null`.
    @NonNull
    public <E> SqlSupplier prepare(@NonNull StringExtractor<E> impl, @NonNull E inputData) throws BadImplementationException {
        return strategy.apply(impl, inputData);
    }

    /// The behaviour of the [#ON_STARTUP] element.
    /// @param <E> The type of the input data consumed by `impl`.
    /// @param impl The reader that extracts the SQL string from `inputData`.
    /// @param inputData The data passed to the reader (e.g., an annotation instance).
    /// @return A [SqlSupplier] that delivers the SQL string according to this policy; never `null`.
    /// @throws BadImplementationException If this policy reads eagerly and the source cannot be read.
    /// @throws IllegalArgumentException If `impl` or `inputData` is `null`.
    @NonNull
    private static <E> SqlSupplier onStartup(@NonNull StringExtractor<E> impl, @NonNull E inputData) throws BadImplementationException {
        checkNotNull(impl); // Check recognized by lombok.
        checkNotNull(inputData); // Check recognized by lombok.

        try {
            var out = impl.read(inputData);
            return () -> out;
        } catch (FileNotFoundException | NoSuchFileException e) {
            throw new BadImplementationException("Can't read from source.", e, ReadPolicy.class);
        } catch (IOException e) {
            throw new BadImplementationException(e.getMessage(), e, ReadPolicy.class);
        }
    }

    /// The behaviour of the [#EVERY_TIME] element.
    /// @param <E> The type of the input data consumed by `impl`.
    /// @param impl The reader that extracts the SQL string from `inputData`.
    /// @param inputData The data passed to the reader (e.g., an annotation instance).
    /// @return A [SqlSupplier] that delivers the SQL string according to this policy; never `null`.
    /// @throws BadImplementationException If this policy reads eagerly and the source cannot be read.
    /// @throws IllegalArgumentException If `impl` or `inputData` is `null`.
    @NonNull
    private static <E> SqlSupplier everyTime(@NonNull StringExtractor<E> impl, @NonNull E inputData) {
        checkNotNull(impl); // Check recognized by lombok.
        checkNotNull(inputData); // Check recognized by lombok.

        return () -> {
            try {
                return impl.read(inputData);
            } catch (IOException e) {
                throw new SQLException(e.getMessage(), e);
            }
        };
    }

    /// The behaviour of the [#ON_FIRST_TIME_THAT_WORKS] element.
    /// @param <E> The type of the input data consumed by `impl`.
    /// @param impl The reader that extracts the SQL string from `inputData`.
    /// @param inputData The data passed to the reader (e.g., an annotation instance).
    /// @return A [SqlSupplier] that delivers the SQL string according to this policy; never `null`.
    /// @throws BadImplementationException If this policy reads eagerly and the source cannot be read.
    /// @throws IllegalArgumentException If `impl` or `inputData` is `null`.
    @NonNull
    private static <E> SqlSupplier onFirstTimeThatWorks(@NonNull StringExtractor<E> impl, @NonNull E inputData) {
        checkNotNull(impl); // Check recognized by lombok.
        checkNotNull(inputData); // Check recognized by lombok.

        return new ThreadTracerSqlSupplier(() -> {
            try {
                var a = impl.read(inputData);
                return () -> a;
            } catch (IOException e) {
                throw new SQLException(e.getMessage(), e);
            }
        });
    }

    /// The behaviour of the [#ON_FIRST_TIME_DONT_RETRY] element.
    /// @param <E> The type of the input data consumed by `impl`.
    /// @param impl The reader that extracts the SQL string from `inputData`.
    /// @param inputData The data passed to the reader (e.g., an annotation instance).
    /// @return A [SqlSupplier] that delivers the SQL string according to this policy; never `null`.
    /// @throws BadImplementationException If this policy reads eagerly and the source cannot be read.
    /// @throws IllegalArgumentException If `impl` or `inputData` is `null`.
    @NonNull
    private static <E> SqlSupplier onFirstTimeDontRetry(@NonNull StringExtractor<E> impl, @NonNull E inputData) {
        checkNotNull(impl); // Check recognized by lombok.
        checkNotNull(inputData); // Check recognized by lombok.

        return new ThreadTracerSqlSupplier(() -> {
            try {
                var a = impl.read(inputData);
                return () -> a;
            } catch (IOException e) {
                return () -> {
                    throw new SQLException(e.getMessage(), e);
                };
            }
        });
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
