package ninja.javahacker.annotimpler.sql;

import java.lang.reflect.Proxy;
import lombok.NonNull;

import module java.base;
import module java.sql;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;

/// Wraps an object's method calls in database transactions, ensuring that each top-level call
/// either commits on success or rolls back on failure.
///
/// A `Transactor` holds a [ConnectionFactory] used to open connections, and a
/// [Supplier]`<String>` used to generate unique transaction IDs.  Each distinct
/// top-level transactional invocation opens one connection, runs the operation, and then
/// commits or rolls back.  Nested calls (i.e., calls that occur while a transaction is already
/// active on the current thread) reuse the existing connection and do not commit/rollback —
/// that responsibility remains with the outermost call.
///
/// Use [#transact(Object)] to obtain a transactional proxy for any object:
///
/// ```java
/// MyDao dao = factory.create(MyDao.class);
/// MyDao txDao = transactor.transact(dao);
/// txDao.insertFoo(...); // runs inside a transaction
/// ```
public final class Transactor {

    private final ConnectionFactory factory;
    private final ThreadLocal<Transaction> local = new ThreadLocal<>();
    private final Supplier<String> generateIds;

    /// Creates a new `Transactor` backed by the given connection factory and ID generator.
    ///
    /// @param factory The factory used to open database connections for each top-level transaction.
    /// @param generateIds A supplier that produces a unique string ID for each new transaction.
    /// @throws IllegalArgumentException If `factory` or `generateIds` is `null`.
    public Transactor(@NonNull ConnectionFactory factory, @NonNull Supplier<String> generateIds) {
        this.factory = factory;
        this.generateIds = generateIds;
    }

    /// Holds the active database connection and its unique identifier for one transaction.
    ///
    /// @param connection The open database connection for the current transaction.
    /// @param id The unique string identifier assigned to this transaction.
    public static record Transaction(@NonNull Connection connection, @NonNull String id) {

        /// Creates a `Transaction` with the given connection and identifier.
        ///
        /// @param connection The open database connection for the current transaction.
        /// @param id The unique string identifier assigned to this transaction.
        /// @throws IllegalArgumentException If `connection` or `id` is `null`.
        public Transaction {}
    }

    /// A supplier that may throw any [Throwable].
    ///
    /// @param <E> The type of the supplied value.
    @FunctionalInterface
    public static interface XSupplier<E> {

        /// Returns a value, potentially throwing any throwable.
        ///
        /// @return The supplied value.
        /// @throws Throwable If any error occurs.
        public E get() throws Throwable;
    }

    private <T> XSupplier<T> operate(@NonNull XSupplier<T> operation) {
        checkNotNull(operation);
        return () -> {
            var alreadyHas = local.get() != null;
            if (alreadyHas) return operation.get();

            try (var con = factory.get()) {
                local.set(new Transaction(con, generateIds.get()));
                var ok = false;
                try {
                    var ret = operation.get();
                    ok = true;
                    return ret;
                } finally {
                    if (ok) {
                        con.commit();
                    } else {
                        con.rollback();
                    }
                }
            } finally {
                local.remove();
            }
        };
    }

    private static <A> A unwrap(@NonNull XSupplier<A> input) throws Throwable {
        checkNotNull(input);
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
    /// @return A non-null transactional proxy implementing the same interfaces as `impl`.
    /// @throws IllegalArgumentException If `impl` is already a transactional proxy (i.e.,
    ///         already implements [Marker]) or if `impl` is `null`.
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

    /// Returns the database connection of the transaction currently active on this thread.
    ///
    /// This is a convenience shortcut for `currentTransaction().connection()`.
    ///
    /// @return The non-null active [Connection].
    /// @throws IllegalStateException If no transaction is active on the current thread.
    public Connection connection() {
        return currentTransaction().connection();
    }

    /// Returns the transaction currently active on this thread.
    ///
    /// @return The non-null active [Transaction].
    /// @throws IllegalStateException If no transaction is active on the current thread.
    public Transaction currentTransaction() {
        var ret = local.get();
        if (ret == null) throw new IllegalStateException("No active transaction.");
        return ret;
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
