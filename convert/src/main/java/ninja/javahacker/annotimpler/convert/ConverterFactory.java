package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

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
}