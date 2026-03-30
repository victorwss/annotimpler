package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public final class SetConverter<E> implements Converter<Set<E>> {
    private final Converter<E> cvt;

    public SetConverter(@NonNull ConverterFactory factory, @NonNull Class<E> baseClass)
            throws ConverterFactory.UnavailableConverterException
    {
        this.cvt = factory.get(baseClass);
    }

    @NonNull
    @Override
    public Optional<Set<E>> fromNull() {
        return Optional.of(Set.of());
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(byte in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(short in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(int in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(long in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(float in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(double in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull String in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull byte[] in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Blob in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Clob in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull NClob in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull RowId in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Ref in) throws ConvertionException {
        return cvt.from(in).map(Set::of);
    }
}
