package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

public enum ReadPolicy {
    ON_STARTUP(ReadPolicy::onStartup),
    ON_FIRST_TIME(ReadPolicy::everyTime),
    ON_FIRST_TIME_DONT_RETRY(ReadPolicy::onFirstTime),
    EVERY_TIME(ReadPolicy::onFirstTimeDontRetry);

    private final InImpl in;

    private ReadPolicy(InImpl in) {
        this.in = in;
    }

    public static interface InImpl {
        public <E> SqlSupplier go(Impl<E> impl, E in) throws BadImplementationException;
    }

    public static interface Impl<E> {
        public String read(E in) throws IOException;
    }

    public <E> SqlSupplier prepare(@NonNull Impl<E> impl, @NonNull E value) throws BadImplementationException {
        return in.go(impl, value);
    }

    private static <E> SqlSupplier onStartup(Impl<E> impl, E value) throws BadImplementationException {
        try {
            var out = impl.read(value);
            return () -> out;
        } catch (IOException e) {
            throw new BadImplementationException("Can't read from source.", e, ReadPolicy.class);
        }
    }

    private static <E> SqlSupplier everyTime(Impl<E> impl, E value) {
        return () -> {
            try {
                return impl.read(value);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        };
    }

    private static <E> SqlSupplier onFirstTime(Impl<E> impl, E value) {
        var sync = new Object();
        var result = new AtomicReference<String>();
        return () -> {
            var a = result.get();
            if (a != null) return a;
            synchronized (sync) {
                a = result.get();
                if (a == null) {
                    try {
                        a = impl.read(value);
                    } catch (IOException e) {
                        throw new SQLException(e);
                    }
                    result.set(a);
                }
                return a;
            }
        };
    }

    private static <E> SqlSupplier onFirstTimeDontRetry(Impl<E> impl, E value) {
        var sync = new Object();
        var result = new AtomicReference<String>();
        var ops = new AtomicReference<SQLException>();
        return () -> {
            var x = ops.get();
            if (x != null) throw x;
            var a = result.get();
            if (a != null) return a;
            synchronized (sync) {
                x = ops.get();
                if (x != null) throw x;
                a = result.get();
                if (a == null) {
                    try {
                        a = impl.read(value);
                    } catch (IOException e) {
                        x = new SQLException(e);
                        ops.set(x);
                        throw x;
                    }
                    result.set(a);
                }
                return a;
            }
        };
    }
}
