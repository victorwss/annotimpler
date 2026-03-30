package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public final class OptionalConverter<E> implements Converter<Optional<E>> {
    private final Converter<E> cvt;

    public OptionalConverter(@NonNull ConverterFactory factory, @NonNull Class<E> baseClass)
            throws ConverterFactory.UnavailableConverterException
    {
        this.cvt = factory.get(baseClass);
    }

    @NonNull
    @Override
    public Optional<Optional<E>> fromNull() {
        return Optional.of(Optional.empty());
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(byte in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(short in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(int in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(long in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(float in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(double in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull String in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull byte[] in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Blob in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Clob in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull NClob in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull RowId in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Optional<E>> from(@NonNull Ref in) throws ConvertionException {
        return Optional.of(cvt.from(in));
    }
}
