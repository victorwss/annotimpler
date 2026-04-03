package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public final class ListConverter<E> implements Converter<List<E>> {
    private final Converter<E> cvt;

    public ListConverter(@NonNull ConverterFactory factory, @NonNull Class<E> baseClass) throws UnavailableConverterException {
        this.cvt = factory.get(baseClass);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<List<E>> getType() {
        return (Class) List.class;
    }

    @NonNull
    @Override
    public Optional<List<E>> fromNull() {
        return Optional.of(List.of());
    }

    @NonNull
    @Override
    public Optional<List<E>> from(byte in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(short in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(int in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(long in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(float in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(double in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull String in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull byte[] in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Blob in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Clob in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull NClob in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull RowId in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Ref in) throws ConvertionException {
        return cvt.from(in).map(List::of);
    }
}
