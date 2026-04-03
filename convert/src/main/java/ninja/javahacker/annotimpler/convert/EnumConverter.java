package ninja.javahacker.annotimpler.convert;

import lombok.NonNull;

import module java.base;

public final class EnumConverter<E extends Enum<E>> implements Converter<E> {

    private static final String BAD = "Can't read value as an enum object.";

    @NonNull
    private final Class<E> enumClass;

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<E> getType() {
        return enumClass;
    }

    public EnumConverter(@NonNull Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    public static <E extends Enum<E>> EnumConverter<E> instance(@NonNull Class<E> enumClass) {
        return new EnumConverter<>(enumClass);
    }

    @NonNull
    @Override
    public Optional<E> from(byte in) throws ConvertionException {
        return from((int) in);
    }

    @NonNull
    @Override
    public Optional<E> from(short in) throws ConvertionException {
        return from((int) in);
    }

    private Optional<E> cvt(Optional<Integer> opt) throws ConvertionException  {
        return opt.isEmpty() ? Optional.empty() : Optional.of(at(opt.get()));
    }

    private E at(int in) throws ConvertionException {
        try {
            return enumClass.getEnumConstants()[in];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(BAD, e, int.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(int in) throws ConvertionException {
        return Optional.of(at(in));
    }

    @NonNull
    @Override
    public Optional<E> from(long in) throws ConvertionException {
        try {
            return cvt(BigDecimalConverter.INSTANCE.from(in).map(BigDecimal::intValueExact));
        } catch (ArithmeticException e) {
            throw new ConvertionException(BAD, e, long.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(float in) throws ConvertionException {
        try {
            return cvt(BigDecimalConverter.INSTANCE.from(in).map(BigDecimal::intValueExact));
        } catch (ArithmeticException e) {
            throw new ConvertionException(BAD, e, float.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(double in) throws ConvertionException {
        try {
            return cvt(BigDecimalConverter.INSTANCE.from(in).map(BigDecimal::intValueExact));
        } catch (ArithmeticException e) {
            throw new ConvertionException(BAD, e, double.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return from(in.intValueExact());
        } catch (ArithmeticException e) {
            throw new ConvertionException(BAD, e, BigDecimal.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull String in) throws ConvertionException {
        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Enum.valueOf(enumClass, in));
        } catch (IllegalArgumentException e1) {
            try {
                return from(Integer.parseInt(in));
            } catch (NumberFormatException e2) {
                throw new ConvertionException(BAD, e1, String.class, enumClass);
            }
        }
    }
}
