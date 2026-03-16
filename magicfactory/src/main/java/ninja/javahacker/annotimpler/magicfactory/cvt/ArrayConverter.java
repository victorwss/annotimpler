package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class ArrayConverter<E> implements Converter<E[]> {

    @NonNull
    private final Class<E> baseClass;

    @NonNull
    private final Converter<E> cvt;

    public ArrayConverter(@NonNull ConverterFactory factory, @NonNull Class<E> baseClass)
            throws ConverterFactory.UnavailableConverterException
    {
        this.baseClass = baseClass;
        this.cvt = factory.get(baseClass);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private Optional<E[]> wrap(Optional<E> e) {
        var e2 = (Optional<Object>) e;
        var r = e2.map(c -> {
            var ret = java.lang.reflect.Array.newInstance(baseClass, 1);
            java.lang.reflect.Array.set(ret, 0, c);
            return ret;
        });
        return (Optional<E[]>) (Optional<?>) r;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Optional<E[]> fromNull() {
        var array = java.lang.reflect.Array.newInstance(baseClass, 0);
        return (Optional<E[]>) (Optional<?>) Optional.of(array);
    }

    @NonNull
    @Override
    public Optional<E[]> from(boolean in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(byte in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(short in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(int in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(long in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(float in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(double in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull String in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Blob in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Clob in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull NClob in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull RowId in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<E[]> from(@NonNull Ref in) throws ConvertionException {
        return wrap(cvt.from(in));
    }
}
