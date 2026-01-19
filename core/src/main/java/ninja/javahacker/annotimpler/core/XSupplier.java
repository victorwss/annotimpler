package ninja.javahacker.annotimpler.core;

import lombok.NonNull;

import module java.base;

@FunctionalInterface
public interface XSupplier<E> {
    public E get() throws Throwable;

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.AvoidCatchingThrowable"})
    public static <E> Supplier<E> wrap(@NonNull XSupplier<E> x) {
        if (x == null) throw new AssertionError();
        return () -> {
            try {
                return x.get();
            } catch (Throwable t) {
                throw new ImplementationFailedException(t);
            }
        };
    }

    public static class ImplementationFailedException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public ImplementationFailedException(@NonNull Throwable cause) {
            List.of(cause); // Force lombok put the null-checks before the constructor call.
            super(cause);
        }
    }
}
