package ninja.javahacker.annotimpler.sql.meta;

import lombok.NonNull;
import module java.base;
import module java.sql;

public final class ThreadTracerSqlSupplier implements SqlSupplier {

    private final Object lockQueue;
    private final Object lockRes;
    private final Downstream downstream;
    private final Set<Thread> waiting;
    private volatile SqlSupplier result;

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
