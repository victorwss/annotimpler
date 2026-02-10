package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum OptionalLongConverter implements Converter<OptionalLong> {
    INSTANCE;

    @Override
    public OptionalLong fromNull() {
        return OptionalLong.empty();
    }

    @Override
    public OptionalLong from(boolean in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(byte in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(short in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(int in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(long in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(float in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(double in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(@NonNull BigDecimal in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalLong from(@NonNull String in) {
        return OptionalLong.of(LongConverter.INSTANCE.from(in));
    }
}
