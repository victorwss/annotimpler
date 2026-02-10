package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum OptionalDoubleConverter implements Converter<OptionalDouble> {
    INSTANCE;

    @Override
    public OptionalDouble fromNull() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble from(boolean in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(byte in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(short in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(int in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(long in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(float in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(double in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(@NonNull BigDecimal in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalDouble from(@NonNull String in) {
        return OptionalDouble.of(DoubleConverter.INSTANCE.from(in));
    }
}
