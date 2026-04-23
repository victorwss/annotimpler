package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Array;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public class RecordConverter<R extends Record> implements Converter<R> {
    private final Class<R> recordClass;
    private final MagicFactory<R> factory;
    private final Type inType;
    private final Converter<?> inFactory;

    private static final ThreadLocal<Set<OngoingCreation>> ongoing = new ThreadLocal<>();

    private static record OngoingCreation(ConverterFactory a, Class<?> b) {
    }

    private static interface IntermediateProducer {
        @NonNull
        public Optional<?> produce() throws ConvertionException;
    }

    public RecordConverter(@NonNull ConverterFactory cvtf, @NonNull Class<R> recordClass) throws UnavailableConverterException {
        this.recordClass = recordClass;
        var o = new OngoingCreation(cvtf, recordClass);
        var n = ongoing.get();
        if (n == null) {
            n = new HashSet<>(5);
            ongoing.set(n);
        } else if (n.contains(o)) {
            throw new UnavailableConverterException("Recursive record class: " + recordClass.getName(), recordClass);
        }
        n.add(o);

        try {
            try {
                this.factory = MagicFactory.of(recordClass);
            } catch (MagicFactory.CreatorSelectionException e) {
                throw new UnavailableConverterException(e.getMessage(), e, recordClass);
            }
            if (this.factory.arity() != 1) {
                var msg = "Non-single value record class where single-valued was expected: " + recordClass.getName();
                throw new UnavailableConverterException(msg, recordClass);
            }
            this.inType = factory.getParameterTypes().get(0);
            this.inFactory = cvtf.get(inType);
        } finally {
            n.remove(o);
            if (n.isEmpty()) ongoing.remove();
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<R> getType() {
        return recordClass;
    }

    @NonNull
    private Optional<R> wrap(@NonNull Class<?> inClass, @NonNull IntermediateProducer p) throws ConvertionException {
        checkNotNull(inClass);
        checkNotNull(p);
        Optional<?> in2a;
        try {
            in2a = p.produce();
        } catch (ConvertionException e) {
            if (e.getMessage().contains("Unsupported ")) {
                throw new ConvertionException(e.getMessage(), e, e.getIn(), recordClass);
            }
            throw new ConvertionException(e, inClass, recordClass);
        }
        checkNotNull(in2a);
        if (in2a.isEmpty()) return Optional.empty();
        var in2b = in2a.get();
        if (in2b.getClass().isArray() && Array.getLength(in2b) == 0) return Optional.empty();
        if (in2b instanceof Collection<?> c && c.size() == 0) return Optional.empty();
        if (in2b instanceof Optional<?> c && c.isEmpty()) return Optional.empty();
        try {
            return Optional.of(factory.create(in2b));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, inClass, recordClass);
        }
    }

    @NonNull
    @Override
    public Optional<R> fromObj(@Nullable Object in) throws ConvertionException {
        if (in == null) return Optional.empty();
        return wrap(WrapperClass.unwrap(in.getClass()), () -> inFactory.fromObj(in));
    }

    @NonNull
    @Override
    public Optional<R> from(boolean in) throws ConvertionException {
        return wrap(boolean.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(byte in) throws ConvertionException {
        return wrap(byte.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(short in) throws ConvertionException {
        return wrap(short.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(int in) throws ConvertionException {
        return wrap(int.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(long in) throws ConvertionException {
        return wrap(long.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(float in) throws ConvertionException {
        return wrap(float.class, () -> inFactory.from(in));
    }

    @Override
    public Optional<R> from(double in) throws ConvertionException {
        return wrap(double.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull BigDecimal in) throws ConvertionException {
        return wrap(BigDecimal.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull byte[] in) throws ConvertionException {
        return wrap(byte[].class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull String in) throws ConvertionException {
        return wrap(String.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull LocalDate in) throws ConvertionException {
        return wrap(LocalDate.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull LocalTime in) throws ConvertionException {
        return wrap(LocalTime.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull LocalDateTime in) throws ConvertionException {
        return wrap(LocalDateTime.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull OffsetTime in) throws ConvertionException {
        return wrap(OffsetTime.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull OffsetDateTime in) throws ConvertionException {
        return wrap(OffsetDateTime.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull Blob in) throws ConvertionException {
        return wrap(Blob.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull Clob in) throws ConvertionException {
        return wrap(Clob.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull NClob in) throws ConvertionException {
        return wrap(NClob.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull SQLXML in) throws ConvertionException {
        return wrap(SQLXML.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull RowId in) throws ConvertionException {
        return wrap(RowId.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull Ref in) throws ConvertionException {
        return wrap(Ref.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull Struct in) throws ConvertionException {
        return wrap(Struct.class, () -> inFactory.from(in));
    }

    @NonNull
    @Override
    public Optional<R> from(@NonNull java.sql.Array in) throws ConvertionException {
        return wrap(java.sql.Array.class, () -> inFactory.from(in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
