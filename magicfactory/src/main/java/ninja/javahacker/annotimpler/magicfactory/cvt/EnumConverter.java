package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public final class EnumConverter<E extends Enum<E>> implements Converter<E> {
    private final Class<E> enumClass;

    public EnumConverter(@NonNull Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public E from(byte in) {
        return from((int) in);
    }

    @Override
    public E from(short in) {
        return from((int) in);
    }

    @Override
    public E from(int in) {
        return enumClass.getEnumConstants()[in];
    }

    @Override
    public E from(long in) {
        return from(BigIntegerConverter.INSTANCE.from(in).intValueExact());
    }

    @Override
    public E from(float in) {
        return from(BigDecimalConverter.INSTANCE.from(in).intValueExact());
    }

    @Override
    public E from(double in) {
        return from(BigDecimalConverter.INSTANCE.from(in).intValueExact());
    }

    @Override
    public E from(@NonNull BigDecimal in) {
        return from(in.intValueExact());
    }

    @Override
    public E from(@NonNull String in) {
        return Enum.valueOf(enumClass, in);
    }
}
