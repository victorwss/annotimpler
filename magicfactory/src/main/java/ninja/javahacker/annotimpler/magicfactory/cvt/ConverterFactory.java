package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

@FunctionalInterface
public interface ConverterFactory {

    @NonNull
    public static final ConverterFactory STD = StdConverterFactory.INSTANCE;

    @NonNull
    public <E> Converter<E> get(@NonNull Type t) throws UnavailableConverterException;

    @NonNull
    public default <E> Converter<E> get(@NonNull Class<E> klass) throws UnavailableConverterException {
        return get((Type) klass);
    }

    @NonNull
    public static <E> Converter<E> stdGet(@NonNull Type t) throws UnavailableConverterException {
        return STD.get(t);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <E> Converter<E> stdGet(@NonNull Class<E> klass) throws UnavailableConverterException {
        return STD.get(klass);
    }

    public static class UnavailableConverterException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        @NonNull
        private final Type root;

        public UnavailableConverterException(@NonNull String message, @NonNull Type root) {
            List.of(message, root); // Force lombok put the null-checks before the constructor call.
            super(message);
            this.root = root;
        }

        public UnavailableConverterException(@NonNull String message, @NonNull Throwable cause, @NonNull Type root) {
            List.of(message, cause, root); // Force lombok put the null-checks before the constructor call.
            super(message, cause);
            this.root = root;
        }

        @NonNull
        public Type getRoot() {
            return root;
        }
    }
}