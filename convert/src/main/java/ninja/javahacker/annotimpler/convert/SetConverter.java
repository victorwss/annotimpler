package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public final class SetConverter<E> implements Converter<Set<E>> {
    private final Converter<E> cvt;
    private final ParameterizedType p;

    public SetConverter(@NonNull ConverterFactory factory, @NonNull ParameterizedType p) throws UnavailableConverterException {
        var baseClass = p.getActualTypeArguments()[0];
        if (p.getRawType() != Set.class) throw new IllegalArgumentException();
        if (!(baseClass instanceof Class<?>)) {
            throw new UnavailableConverterException("Only for Set of class.", baseClass);
        }
        this.p = p;
        this.cvt = factory.get(baseClass);
    }

    @NonNull
    @Override
    public Type getType() {
        return p;
    }

    @FunctionalInterface
    private interface Work<E> {

        @NonNull
        public Optional<E> work() throws ConvertionException;

        @NonNull
        public default Optional<Set<E>> rework(@NonNull Type p) throws ConvertionException {
            checkNotNull(p);
            try {
                return Optional.of(work().map(Set::of).orElse(Set.of()));
            } catch (ConvertionException e) {
                if (e.getMessage().contains("Unsupported ")) {
                    throw new ConvertionException(e.getMessage(), e, e.getIn(), p);
                }
                throw new ConvertionException(e, e.getIn(), p);
            }
        }
    }

    @NonNull
    private Optional<Set<E>> wrap(Work<E> e) throws ConvertionException {
        return e.rework(p);
    }

    @NonNull
    @Override
    public Optional<Set<E>> fromNull() {
        return Optional.of(Set.of());
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(boolean in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(byte in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(short in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(int in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(long in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(float in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(double in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull String in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Blob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Clob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull NClob in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull RowId in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Struct in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @NonNull
    @Override
    public Optional<Set<E>> from(@NonNull Ref in) throws ConvertionException {
        return wrap(() -> cvt.from(in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
