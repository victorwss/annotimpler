package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum CharArrayConverter implements Converter<char[]> {
    INSTANCE;

    @FunctionalInterface
    private interface Work {
        public Optional<char[]> work() throws ConvertionException;
    }

    @NonNull
    private Optional<char[]> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), char[].class);
        }
    }

    @NonNull
    @Override
    public Class<char[]> getType() {
        return char[].class;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Optional<char[]> fromNull() {
        return Optional.of(new char[0]);
    }

    @NonNull
    @Override
    public Optional<char[]> from(boolean in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(byte in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(short in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(int in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(long in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(float in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(double in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull byte[] in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull Blob in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull Clob in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull NClob in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull SQLXML in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @NonNull
    @Override
    public Optional<char[]> from(@NonNull RowId in) throws ConvertionException {
        return rewrap(() -> StringConverter.INSTANCE.from(in).map(String::toCharArray));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
