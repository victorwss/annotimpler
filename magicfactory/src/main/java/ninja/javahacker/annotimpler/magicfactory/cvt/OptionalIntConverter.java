package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public enum OptionalIntConverter implements Converter<OptionalInt> {
    INSTANCE;

    @Override
    public OptionalInt fromNull() {
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt from(boolean in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(byte in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(short in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(int in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(long in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(float in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(double in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(@NonNull BigDecimal in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }

    @Override
    public OptionalInt from(@NonNull String in) {
        return OptionalInt.of(IntegerConverter.INSTANCE.from(in));
    }
}
