package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

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
            this.inType = recordClass.getRecordComponents()[0].getGenericType();
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

    /*@Override
    public Optional<R> fromNull() throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.fromNull().orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }*/

    @Override
    public Optional<R> from(boolean in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, boolean.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(byte in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, byte.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(short in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, short.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(int in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, int.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(long in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, long.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(float in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, float.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(double in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, double.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, BigDecimal.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull byte[] in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, byte[].class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull String in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, String.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull LocalDate in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, LocalDate.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull LocalTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, LocalTime.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull LocalDateTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, LocalDateTime.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull OffsetTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, OffsetTime.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull OffsetDateTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, OffsetDateTime.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull Blob in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, Blob.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, Clob.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, NClob.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull RowId in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, RowId.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull Ref in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, Ref.class, recordClass);
        }
    }

    @Override
    public Optional<R> from(@NonNull java.sql.Array in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, java.sql.Array.class, recordClass);
        }
    }
}
