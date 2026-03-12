package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public class RecordConverter<R extends Record> implements Converter<R> {
    private final MagicFactory<R> factory;
    private final Type inType;
    private final Converter<?> inFactory;

    private static final ThreadLocal<Set<OngoingCreation>> ongoing = new ThreadLocal<>();

    private static record OngoingCreation(ConverterFactory a, Class<?> b) {
    }

    public RecordConverter(@NonNull ConverterFactory cvtf, @NonNull Class<R> recordClass) throws ConvertionException {
        var o = new OngoingCreation(cvtf, recordClass);
        var n = ongoing.get();
        if (n == null) {
            n = new HashSet<>(5);
            ongoing.set(n);
        } else if (n.contains(o)) {
            throw new ConvertionException("Recursive record class: " + recordClass.getName(), recordClass);
        }
        n.add(o);

        try {
            try {
                this.factory = MagicFactory.of(recordClass);
            } catch (MagicFactory.CreatorSelectionException e) {
                throw new ConvertionException(e.getMessage(), e, recordClass);
            }
            if (this.factory.arity() != 1) {
                var msg = "Non-single value record class where single-valued was expected: " + recordClass.getName();
                throw new ConvertionException(msg, recordClass);
            }
            this.inType = recordClass.getRecordComponents()[0].getGenericType();
            try {
                this.inFactory = cvtf.get(inType);
            } catch (ConverterFactory.UnavailableConverterException e) {
                throw new ConvertionException(e.getMessage(), e, recordClass);
            }
        } finally {
            n.remove(o);
            if (n.isEmpty()) ongoing.remove();
        }
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
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(byte in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(short in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(int in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(long in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(float in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(double in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull byte[] in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull String in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull LocalDate in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull LocalTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull LocalDateTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull OffsetTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull OffsetDateTime in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull Blob in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull Clob in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull NClob in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull RowId in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull Ref in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }

    @Override
    public Optional<R> from(@NonNull java.sql.Array in) throws ConvertionException {
        try {
            return Optional.of(factory.create(inFactory.from(in).orElse(null)));
        } catch (MagicFactory.CreationException x) {
            throw new ConvertionException(x.getMessage(), x, factory.getReturnType());
        }
    }
}
