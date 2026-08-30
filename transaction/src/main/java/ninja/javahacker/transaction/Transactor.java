package ninja.javahacker.transaction;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Proxy;
import lombok.NonNull;

import module java.base;
import module ninja.javahacker.magicfactory;

/// Wraps an object's method calls in transactions, ensuring that each top-level call either
/// commits on success or rolls back on failure.
///
/// A `Transactor` holds a [TransactionFactory] used to begin new [Transaction]s, and a
/// [Supplier]`<String>` used to generate unique transaction IDs.  Each distinct
/// top-level transactional invocation begins one transaction, runs the operation, and then
/// commits or rolls back.  Nested calls (i.e., calls that occur while a transaction is already
/// active on the current thread) reuse the existing transaction and do not commit/rollback —
/// that responsibility remains with the outermost call.
///
/// The type parameter `E` is the type of the underlying resource wrapped by each [Transaction]
/// (e.g. a JDBC `Connection` or a JPA `EntityManager`), so the same `Transactor` implementation
/// can be reused regardless of the underlying persistence technology; only the
/// [TransactionFactory] and [Transaction] implementations need to be specific to it.
///
/// Use [#transact(Object)] to obtain a transactional proxy for any object:
///
/// ```java
/// MyDao dao = ...
/// MyDao txDao = transactor.transact(dao);
/// txDao.insertFoo(...); // runs inside a transaction
/// ```
///
/// To get the transaction data, use the [#transaction()] method:
///
/// ```java
/// var tx = transactor.transaction();
/// var resource = tx.unwrap(); // or transactor.resource()
/// ```
///
/// @param <E> The type of the underlying resource wrapped by each [Transaction].
///            Typically a JDBC `Connection` or a JPA `EntityManager`.
public final class Transactor<E> {

    /// The object (possibly a lambda or method reference) that begins new [Transaction]s.
    @NonNull
    private final TransactionFactory<E> factory;

    /// Stores the current transaction, if open.
    /// This is thread-local, since different threads can't and shouldn't share a transaction.
    @NonNull
    @SuppressFBWarnings("PMB_INSTANCE_BASED_THREAD_LOCAL") // We really intentionally want a ThreadLocal per instance.
    private final ThreadLocal<Transaction<E>> local = new ThreadLocal<>();

    /// The object (likely a lambda or method reference) that produces new transaction IDs.
    @NonNull
    private final Supplier<String> generateIds;

    /// Creates a new `Transactor` backed by the given transaction factory and ID generator.
    ///
    /// @param factory The factory used to begin a new [Transaction] for each top-level transactional call.
    /// @param generateIds A supplier that produces a unique string ID for each new transaction.
    /// @throws IllegalArgumentException If `factory` or `generateIds` is `null`.
    public Transactor(@NonNull TransactionFactory<E> factory, @NonNull Supplier<String> generateIds) {
        this.factory = factory;
        this.generateIds = generateIds;
    }

    /// A supplier that may throw any [Throwable].
    ///
    /// @param <E> The type of the supplied value.
    @FunctionalInterface
    public static interface XSupplier<E> {

        /// Returns a value, potentially throwing any throwable.
        ///
        /// @return The supplied value, or `null`.
        /// @throws Throwable If any error occurs.
        @Nullable
        @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_THROWABLE") // The only purpose of this is exactly the throws Throwable.
        public E get() throws Throwable;
    }

    /// Signals that a transaction operation failed.
    ///
    /// This checked exception is the common exception type that implementations of
    /// [Transaction#commit()], [Transaction#rollback()] and [Transaction#close()] must use
    /// when the underlying persistence mechanism reports a failure.  [Transactor] unwraps it
    /// and rethrows its cause to the caller of the transactional operation.
    ///
    /// @see Transaction
    public static class TransactionException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates an exception for a failure reported by the underlying transaction resource.
        ///
        /// @param cause The original failure; must not be `null`.
        /// @throws IllegalArgumentException If `cause` is `null`.
        public TransactionException(@NonNull Throwable cause) {
            List.of(cause);
            super(cause);
        }

        /// Disabled. Should not be used. Does nothing.
        ///
        /// This method exists with the sole purpose of fixing SpotBugs' CT_CONSTRUCTOR_THROW
        /// by disabling the ability to override the `finalize()` method that should not even exist to start with.
        ///
        /// @deprecated Finalization was deprecated. This method is intentionally unused, unusable and disabled.
        @Deprecated
        @Generated
        @SuppressWarnings({"all", "removal"})
        protected final void finalize() {
            // Do nothing.
        }
    }

    /// Represents one active top-level transaction, wrapping the underlying resource of type
    /// `E` (e.g. a JDBC `Connection` or a JPA `EntityManager`) together with its unique
    /// transaction ID.
    ///
    /// Implementations are produced by a [TransactionFactory] and are `AutoCloseable`; closing a
    /// `Transaction` should release the underlying resource, regardless of whether [#commit()]
    /// or [#rollback()] was called beforehand.
    ///
    /// @param <E> The type of the underlying resource wrapped by this transaction.
    public static interface Transaction<E> extends AutoCloseable {

        /// Returns the unique identifier assigned to this transaction.
        ///
        /// @return The transaction id; never `null`.
        @NonNull
        public String uniqueId();

        /// Commits this transaction, making its changes permanent.
        ///
        /// @throws TransactionException If a failure occurs while committing.
        public void commit() throws TransactionException;

        /// Rolls back this transaction, discarding its changes.
        ///
        /// @throws TransactionException If a failure occurs while rolling back.
        public void rollback() throws TransactionException;

        /// Releases the underlying resource held by this transaction.
        ///
        /// @throws TransactionException If a failure occurs while closing the underlying resource.
        @Override
        public void close() throws TransactionException;

        /// Returns the underlying resource wrapped by this transaction.
        ///
        /// @return The wrapped resource (e.g. a JDBC `Connection` or a JPA `EntityManager`); never `null`.
        @NonNull
        public E unwrap();
    }

    /// Begins new [Transaction]s identified by a caller-supplied unique ID.
    ///
    /// @param <E> The type of the underlying resource wrapped by the transactions this factory begins.
    @FunctionalInterface
    public interface TransactionFactory<E> {

        /// Begins a new transaction identified by the given ID.
        ///
        /// @param id The unique string identifier assigned to the new transaction.
        /// @return The newly-begun [Transaction]; never `null`.
        /// @throws TransactionException If a failure occurs while beginning the transaction.
        /// @throws IllegalArgumentException If `id` is `null`.
        @NonNull
        public Transaction<E> begin(@NonNull String id) throws TransactionException;
    }

    @NonNull
    private <T> XSupplier<T> operate(@NonNull XSupplier<T> operation) {
        checkNotNull(operation); // Check recognized by lombok.
        return () -> {
            var alreadyHas = local.get() != null;
            if (alreadyHas) return operation.get();

            try (var trans = factory.begin(generateIds.get())) {
                local.set(trans);
                var ok = false;
                try {
                    var ret = operation.get();
                    ok = true;
                    return ret;
                } finally {
                    if (ok) {
                        trans.commit();
                    } else {
                        trans.rollback();
                    }
                }
            } catch (TransactionException x) {
                throw x.getCause();
            } finally {
                local.remove();
            }
        };
    }

    @Nullable
    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE") // It is intentional here.
    private static <A> A unwrap(@NonNull XSupplier<A> input) throws Throwable {
        checkNotNull(input); // Check recognized by lombok.
        try {
            return input.get();
        } catch (InvocationTargetException | UndeclaredThrowableException e) {
            throw e.getCause();
        }
    }

    /// Returns a transactional proxy for the given object.
    ///
    /// Each method call on the returned proxy that is not `toString`, `hashCode`,
    /// `equals`, `finalize`, or `clone` is executed within a transaction:
    /// if no transaction is already active on the current thread, a new connection is opened,
    /// the method runs, and the connection is committed on success or rolled back on failure.
    /// Nested calls reuse the existing connection without committing or rolling back.
    ///
    /// The returned proxy implements all interfaces of `impl` plus the marker interface
    /// [Marker], which can be used to detect whether an object is already a transactional
    /// proxy.
    ///
    /// @param <E> The interface type of the returned proxy.
    /// @param <F> The concrete type of the wrapped object (must extend `E`).
    /// @param impl The object to wrap.
    /// @return A transactional proxy implementing the same interfaces as `impl`; never `null`.
    /// @throws IllegalArgumentException If `impl` is already a transactional proxy (i.e.,
    ///         already implements [Marker]) or if `impl` is `null`.
    @NonNull
    @SuppressWarnings("unchecked")
    public <E, F extends E> E transact(@NonNull F impl) {
        if (impl instanceof Marker) throw new IllegalArgumentException("Can't doubly transact an object.");

        InvocationHandler ih = (p, m, a) -> {
            if (Methods.isToString(m) || Methods.isHashCode(m) || Methods.isEquals(m) || Methods.isFinalize(m) || Methods.isClone(m)) {
                return unwrap(() -> m.invoke(impl, a));
            }
            return operate(() -> unwrap(() -> m.invoke(impl, a))).get();
        };

        var cl = Thread.currentThread().getContextClassLoader();
        var ifs = impl.getClass().getInterfaces();
        var ifs2 = new Class<?>[ifs.length + 1];
        ifs2[0] = Marker.class;
        System.arraycopy(ifs, 0, ifs2, 1, ifs.length);

        return (E) Proxy.newProxyInstance(cl, ifs2, ih);
    }

    /// Marker interface implemented by all transactional proxies created by [#transact(Object)].
    ///
    /// This interface carries no methods; its sole purpose is to allow detection of whether an
    /// object is already wrapped in a transactional proxy.
    public static interface Marker {}

    /// Returns the transaction currently active on this thread.
    ///
    /// @return The active [Transaction]; never `null`.
    /// @throws IllegalStateException If no transaction is active on the current thread.
    @NonNull
    public Transaction<E> transaction() {
        var ret = local.get();
        if (ret == null) throw new IllegalStateException("No active transaction.");
        return ret;
    }

    /// Returns the underlying resource of the transaction currently active on this thread.
    ///
    /// @return The active transaction's wrapped resource (e.g. a JDBC `Connection` or a JPA
    ///         `EntityManager`); never `null`.
    /// @throws IllegalStateException If no transaction is active on the current thread.
    @NonNull
    public E resource() {
        return transaction().unwrap();
    }

    /// Returns the transaction id of the transaction currently active on this thread.
    ///
    /// @return The active transaction id; never `null`.
    /// @throws IllegalStateException If no transaction is active on the current thread.
    @NonNull
    public String transactionId() {
        return transaction().uniqueId();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
