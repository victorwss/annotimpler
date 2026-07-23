package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Generated;
import lombok.NonNull;

import module java.base;
import module java.sql;

/// A thread-safe, lazily initialised [SqlSupplier] that caches its result globally after the
/// first successful resolution, and tracks which threads are currently waiting for that result.
///
/// On the first call to [get], the [Downstream] supplier is invoked (at most once, under
/// synchronization) to obtain the delegate [SqlSupplier] to cache. Subsequent calls skip
/// the initialization path entirely (fast path via a volatile read). While a thread is
/// blocked waiting for the result, it is registered in an internal set accessible via [has].
///
/// This class is useful for detecting dependency cycles during initialization: if a thread
/// is observed via [has] while it is itself attempting to resolve the same supplier, a cycle
/// can be detected and reported before a deadlock occurs.
///
/// @see SqlSupplier
/// @see Downstream
public final class ThreadTracerSqlSupplier implements SqlSupplier {

    /// Guards access to [#waiting], the set of threads currently blocked inside [#get].
    @NonNull
    private final ReentrantLock lockQueue;

    /// Guards the initialization critical section that resolves and caches [#result].
    @NonNull
    private final ReentrantLock lockRes;

    /// The downstream supplier called at most once to resolve the [SqlSupplier] to cache.
    @NonNull
    private final Downstream downstream;

    /// The set of threads currently blocked inside [#get] waiting for initialization to complete.
    @NonNull
    private final Set<Thread> waiting;

    /// The cached delegate [SqlSupplier], or `null` before the first successful resolution.
    @Nullable
    private volatile SqlSupplier result;

    /// Creates a new [ThreadTracerSqlSupplier] backed by the given [Downstream].
    ///
    /// The [Downstream] supplier will be called at most once, on the first invocation of [get].
    ///
    /// @param downstream The downstream supplier to call for initialization; must not be `null`.
    /// @throws IllegalArgumentException If `downstream` is `null`.
    @SuppressWarnings("PMD.NullAssignment")
    public ThreadTracerSqlSupplier(@NonNull Downstream downstream) {
        this.lockQueue = new ReentrantLock();
        this.lockRes = new ReentrantLock();
        this.waiting = new HashSet<>(10);
        this.downstream = downstream;
        this.result = null;
    }

    /// Returns the SQL string, initialising the delegate [SqlSupplier] on the first call.
    ///
    /// Fast path: if the result is already cached (volatile read), returns immediately
    /// without acquiring any lock.
    ///
    /// Slow path: registers the current thread in the waiting set, acquires the
    /// initialization lock, calls [Downstream#get] exactly once to resolve the delegate,
    /// caches it, and then removes the current thread from the waiting set.
    ///
    /// @return The SQL string produced by the cached delegate; never `null`.
    /// @throws SQLException If the downstream initialization or the delegate itself
    ///         throws a [SQLException].
    @NonNull
    @Override
    public String get() throws SQLException {
        if (result != null) return result.get();

        try {
            doWithLock(lockQueue, () -> waiting.add(Thread.currentThread()));
            return sqlGetWithLock(lockRes, () -> {
                @NonNull
                SqlSupplier intermediate = result == null ? downstream.get() : result;

                result = intermediate;
                return intermediate.get();
            });
        } finally {
            doWithLock(lockQueue, () -> waiting.remove(Thread.currentThread()));
        }
    }

    /// Returns `true` if thread `t` is currently blocked inside [get] waiting for initialization.
    ///
    /// @param t The thread to check; must not be `null`.
    /// @return `true` if `t` is currently inside the slow path of [get]; `false` otherwise.
    /// @throws IllegalArgumentException If `t` is `null`.
    public boolean has(@NonNull Thread t) {
        return getWithLock(lockQueue, () -> waiting.contains(t));
    }

    /// The downstream supplier that is called at most once to produce the [SqlSupplier] to cache.
    ///
    /// Implementations may perform expensive operations such as querying a registry or
    /// resolving a factory chain.  The result is cached by [ThreadTracerSqlSupplier] and
    /// this method will not be called again after a successful return.
    @FunctionalInterface
    public static interface Downstream {

        /// Produces the [SqlSupplier] to cache.
        ///
        /// Called at most once by [ThreadTracerSqlSupplier#get] under synchronization.
        ///
        /// @return The [SqlSupplier] to use for all subsequent [ThreadTracerSqlSupplier#get]
        ///         calls; never `null`.
        /// @throws SQLException If the supplier cannot be resolved.
        @NonNull
        public SqlSupplier get() throws SQLException;
    }

    /// A checked-exception-throwing supplier used internally to run a lock-guarded block of
    /// code that produces a value of type `E` while possibly throwing [SQLException].
    ///
    /// @param <E> The type of value produced.
    @FunctionalInterface
    private static interface InnerContext<E> {
        /// Produces the value, possibly throwing a [SQLException].
        ///
        /// @return The produced value; never `null`.
        /// @throws SQLException If the underlying operation fails.
        @NonNull
        public E get() throws SQLException;
    }

    @NonNull
    private static <E> E sqlGetWithLock(@NonNull ReentrantLock lock, @NonNull InnerContext<E> context) throws SQLException {
        checkNotNull(lock);
        checkNotNull(context);
        try {
            lock.lock();
            return context.get();
        } finally {
            lock.unlock();
        }
    }

    @NonNull
    private static <E> E getWithLock(@NonNull ReentrantLock lock, @NonNull Supplier<E> context) {
        checkNotNull(lock);
        checkNotNull(context);
        try {
            lock.lock();
            return context.get();
        } finally {
            lock.unlock();
        }
    }

    @NonNull
    private static void doWithLock(@NonNull ReentrantLock lock, @NonNull Runnable context) {
        checkNotNull(lock);
        checkNotNull(context);
        try {
            lock.lock();
            context.run();
        } finally {
            lock.unlock();
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
