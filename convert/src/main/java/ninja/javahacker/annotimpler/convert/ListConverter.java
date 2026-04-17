package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public final class ListConverter<E> implements Converter<List<E>> {
    private final Converter<E> cvt;
    private final ParameterizedType p;

    public ListConverter(@NonNull ConverterFactory factory, @NonNull ParameterizedType p) throws UnavailableConverterException {
        var baseClass = p.getActualTypeArguments()[0];
        if (p.getRawType() != List.class) throw new IllegalArgumentException();
        if (!(baseClass instanceof Class<?>)) {
            throw new UnavailableConverterException("Only for List of class.", baseClass);
        }
        this.p = p;
        this.cvt = factory.get(baseClass);
    }

    @NonNull
    @Override
    public ParameterizedType getType() {
        return p;
    }

    @FunctionalInterface
    private interface Work<E> {

        @NonNull
        public Optional<E> work() throws ConvertionException;

        @NonNull
        public default Optional<List<E>> rework(@NonNull Type p) throws ConvertionException {
            checkNotNull(p);
            try {
                return Optional.of(work().map(List::of).orElse(List.of()));
            } catch (ConvertionException e) {
                if (e.getMessage().contains("Unsupported ")) {
                    throw new ConvertionException(e.getMessage(), e, e.getIn(), p);
                }
                throw new ConvertionException(e, e.getIn(), p);
            }
        }
    }

    @NonNull
    private Optional<List<E>> wrap(@NonNull Work<E> e) throws ConvertionException {
        checkNotNull(e);
        return e.rework(p);
    }

    @NonNull
    @Override
    public Optional<List<E>> fromNull() {
        return Optional.of(List.of());
    }

    @NonNull
    @Override
    public Optional<List<E>> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull String in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<List<E>> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
