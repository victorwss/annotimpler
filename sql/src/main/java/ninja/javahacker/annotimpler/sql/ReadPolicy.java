package ninja.javahacker.annotimpler.sql;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.sql;

public enum ReadPolicy {
    ON_STARTUP(ReadPolicy::onStartup),
    ON_FIRST_TIME_THAT_WORKS(ReadPolicy::onFirstTimeThatWorks),
    ON_FIRST_TIME_DONT_RETRY(ReadPolicy::onFirstTimeDontRetry),
    EVERY_TIME(ReadPolicy::everyTime);

    private final InImpl in;

    private ReadPolicy(InImpl in) {
        this.in = in;
    }

    private static interface InImpl {
        public <E> SqlSupplier go(Impl<E> impl, E in) throws BadImplementationException;
    }

    public static interface Impl<E> {
        public String read(E in) throws IOException, CharsetSpec.BadCharsetSpecException;
    }

    public <E> SqlSupplier prepare(@NonNull Impl<E> impl, @NonNull E inputData) throws BadImplementationException {
        return in.go(impl, inputData);
    }

    private static <E> SqlSupplier onStartup(@NonNull Impl<E> impl, @NonNull E inputData) throws BadImplementationException {
        checkNotNull(impl);
        checkNotNull(inputData);

        try {
            var out = impl.read(inputData);
            return () -> out;
        } catch (IOException e) {
            throw new BadImplementationException("Can't read from source.", e, ReadPolicy.class);
        }
    }

    private static <E> SqlSupplier everyTime(@NonNull Impl<E> impl, @NonNull E inputData) {
        checkNotNull(impl);
        checkNotNull(inputData);

        return () -> {
            try {
                return impl.read(inputData);
            } catch (IOException e) {
                throw new SQLException(e.getMessage(), e);
            }
        };
    }

    private static <E> SqlSupplier onFirstTimeThatWorks(@NonNull Impl<E> impl, @NonNull E inputData) {
        checkNotNull(impl);
        checkNotNull(inputData);

        var sync = new Object();
        var result = new AtomicReference<String>();

        return () -> {
            var a = result.get();
            if (a != null) return a;

            synchronized (sync) {
                a = result.get();
                if (a == null) {
                    try {
                        a = impl.read(inputData);
                    } catch (IOException e) {
                        throw new SQLException(e.getMessage(), e);
                    }
                    result.set(a);
                }
                return a;
            }
        };
    }

    private static <E> SqlSupplier onFirstTimeDontRetry(@NonNull Impl<E> impl, @NonNull E inputData) {
        checkNotNull(impl);
        checkNotNull(inputData);

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
                        a = impl.read(inputData);
                    } catch (IOException e) {
                        x = new SQLException(e.getMessage(), e);
                        ops.set(x);
                        throw x;
                    }
                    result.set(a);
                }
                return a;
            }
        };
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
