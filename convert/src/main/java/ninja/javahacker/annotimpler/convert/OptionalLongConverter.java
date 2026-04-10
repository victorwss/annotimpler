package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

public enum OptionalLongConverter implements Converter<OptionalLong> {
    INSTANCE;

    @FunctionalInterface
    public interface Work {
        public Optional<OptionalLong> work() throws ConvertionException;
    }

    @NonNull
    private Optional<OptionalLong> rewrap(@NonNull Work w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), OptionalLong.class);
        }
    }

    @NonNull
    @Override
    public Class<OptionalLong> getType() {
        return OptionalLong.class;
    }

    @NonNull
    @Override
    public Optional<OptionalLong> fromNull() {
        return Optional.of(OptionalLong.empty());
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(boolean in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(byte in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(short in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(int in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(long in) {
        return LongConverter.WRAPPER.from(in).map(OptionalLong::of);
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(float in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(double in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull BigDecimal in) throws ConvertionException {
        return rewrap(() -> LongConverter.WRAPPER.from(in).map(OptionalLong::of));
    }

    @NonNull
    @Override
    public Optional<OptionalLong> from(@NonNull String in) throws ConvertionException {
        return rewrap(() -> Optional.of(LongConverter.WRAPPER.from(in).map(OptionalLong::of).orElse(OptionalLong.empty())));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
