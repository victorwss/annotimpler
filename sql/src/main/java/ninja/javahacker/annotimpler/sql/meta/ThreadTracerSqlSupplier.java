package ninja.javahacker.annotimpler.sql.meta;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;

import module java.base;
import module java.sql;

public final class ThreadTracerSqlSupplier implements SqlSupplier {

    @NonNull
    private final Object lockQueue;

    @NonNull
    private final Object lockRes;

    @NonNull
    private final Downstream downstream;

    @NonNull
    private final Set<Thread> waiting;

    @Nullable
    private volatile SqlSupplier result;

    @SuppressWarnings("PMD.NullAssignment")
    public ThreadTracerSqlSupplier(@NonNull Downstream downstream) {
        this.lockQueue = new Object();
        this.lockRes = new Object();
        this.waiting = new HashSet<>(10);
        this.downstream = downstream;
        this.result = null;
    }

    @Override
    public String get() throws SQLException {
        if (result != null) return result.get();
        synchronized (lockQueue) {
            waiting.add(Thread.currentThread());
        }
        try {
            synchronized (lockRes) {
                if (result == null) result = downstream.get();
            }
            return result.get();
        } finally {
            synchronized (lockQueue) {
                waiting.remove(Thread.currentThread());
            }
        }
    }

    public boolean has(@NonNull Thread t) {
        synchronized (lockQueue) {
            return waiting.contains(t);
        }
    }

    @FunctionalInterface
    public static interface Downstream {
        public SqlSupplier get() throws SQLException;
    }
}
