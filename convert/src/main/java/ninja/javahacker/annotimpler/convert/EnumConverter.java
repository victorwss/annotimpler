package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

public final class EnumConverter<E extends Enum<E>> implements Converter<E> {

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

    @NonNull
    private Optional<E> cvt(@NonNull Optional<Integer> opt) throws ConvertionException  {
        checkNotNull(opt);
        return opt.isEmpty() ? Optional.empty() : at(opt.get());
    }

    private Optional<E> at(int in) throws ConvertionException {
        return Optional.of(enumClass.getEnumConstants()[in]);
    }

    @NonNull
    @Override
    public Optional<E> from(byte in) throws ConvertionException {
        try {
            return at(in);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, byte.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(short in) throws ConvertionException {
        try {
            return at(in);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, short.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(int in) throws ConvertionException {
        try {
            return at(in);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, int.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(long in) throws ConvertionException {
        try {
            return cvt(IntegerConverter.PRIMITIVE.from(in));
        } catch (ConvertionException | ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, long.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(float in) throws ConvertionException {
        try {
            return cvt(IntegerConverter.PRIMITIVE.from(in));
        } catch (ConvertionException | ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, float.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(double in) throws ConvertionException {
        try {
            return cvt(IntegerConverter.PRIMITIVE.from(in));
        } catch (ConvertionException | ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, double.class, enumClass);
        }
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull BigDecimal in) throws ConvertionException {
        try {
            return cvt(IntegerConverter.PRIMITIVE.from(in));
        } catch (ConvertionException | ArrayIndexOutOfBoundsException e) {
            throw new ConvertionException(e, BigDecimal.class, enumClass);
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
                return at(Integer.parseInt(in));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e2) {
                throw new ConvertionException(e1, String.class, enumClass);
            }
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
