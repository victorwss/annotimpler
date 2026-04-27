package ninja.javahacker.annotimpler.convert;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import lombok.Generated;
import lombok.NonNull;

import module java.base;

public final class EnumConverter<E extends Enum<E>> implements Converter<E> {

    @NonNull
    private final Class<E> enumClass;

    @FunctionalInterface
    public interface Work<T> {
        public Optional<T> work() throws ConvertionException;
    }

    @NonNull
    private <T> Optional<T> rewrap(@NonNull Work<T> w) throws ConvertionException {
        checkNotNull(w);
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), enumClass);
        }
    }

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
        return at(assertPresentGet(opt));
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

    private Optional<E> from(@NonNull String in, @NonNull Class<?> inType) throws ConvertionException {
        checkNotNull(in);
        checkNotNull(inType);

        if (in.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Enum.valueOf(enumClass, in));
        } catch (IllegalArgumentException e1) {
            try {
                return at(Integer.parseInt(in));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e2) {
                throw new ConvertionException(e1, inType, enumClass);
            }
        }
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull String in) throws ConvertionException {
        return from(in, String.class);
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull Blob in) throws ConvertionException {
        var a = rewrap(() -> StringConverter.INSTANCE.from(in));
        return from(assertPresentGet(a), Blob.class);
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull Clob in) throws ConvertionException {
        var a = rewrap(() -> StringConverter.INSTANCE.from(in));
        return from(assertPresentGet(a), Clob.class);
    }

    @NonNull
    @Override
    public Optional<E> from(@NonNull NClob in) throws ConvertionException {
        var a = rewrap(() -> StringConverter.INSTANCE.from(in));
        return from(assertPresentGet(a), NClob.class);
    }

    @Generated
    private static <E> E assertPresentGet(Optional<E> opt) {
        if (opt.isEmpty()) throw new AssertionError();
        return opt.get();
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
