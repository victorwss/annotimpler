package ninja.javahacker.annotimpler.magicfactory.cvt;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;

public abstract class RecordConverter<R extends Record> implements Converter<R> {
    /*private final MagicFactory<R> factory;
    private final Type inType;

    public RecordConverter(@NonNull Class<R> enumClass) throws ConstructionException {
        this.factory = MagicFactory.of(enumClass);
        if (this.factory.arity() != 1) throw new ConstructionException("", enumClass);
    }

    @Override
    public R from(byte in) {
        return from((int) in);
    }

    @Override
    public R from(short in) {
        return from((int) in);
    }

    @Override
    public R from(int in) {
        return enumClass.getEnumConstants()[in];
    }

    @Override
    public R from(long in) {
        return from(BigIntegerConverter.INSTANCE.from(in).intValueExact());
    }

    @Override
    public R from(float in) {
        return from(BigDecimalConverter.INSTANCE.from(in).intValueExact());
    }

    @Override
    public R from(double in) {
        return from(BigDecimalConverter.INSTANCE.from(in).intValueExact());
    }

    @Override
    public R from(@NonNull BigDecimal in) {
        return from(in.intValueExact());
    }

    @Override
    public R from(@NonNull String in) {
        return Enum.valueOf(enumClass, in);
    }*/
}
