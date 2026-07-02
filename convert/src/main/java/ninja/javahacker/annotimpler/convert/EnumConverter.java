package ninja.javahacker.annotimpler.convert;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NonNull;
import lombok.ToString;

import module java.base;
import module java.sql;

/// A [Converter] for enum types.
///
/// Numeric types (`byte`, `short`, `int`, `long`, `float`, `double`, [BigDecimal]) are interpreted
/// as ordinal indices into the enum's constant array.
/// Strings: first tries `Enum.valueOf` by name; if that fails, tries parsing as an ordinal index;
/// if that also fails, throws [ConvertionException]. Empty string → `Optional.empty()`.
/// [Blob]/[Clob]/[NClob]: content is read as a string and then converted as a string.
/// The default `fromNull()` is inherited and returns `Optional.empty()`.
///
/// @param <E> The enum type this converter targets.
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public final class EnumConverter<E extends Enum<E>> implements Converter<E> {

    @NonNull
    private final Class<E> enumClass;

    @FunctionalInterface
    private interface Work<T> {
        public Optional<T> work() throws ConvertionException;
    }

    @NonNull
    private <T> Optional<T> rewrap(@NonNull Work<T> w) throws ConvertionException {
        checkNotNull(w); // Check recognized by lombok.
        try {
            return w.work();
        } catch (ConvertionException e) {
            throw new ConvertionException(e, e.getIn(), enumClass);
        }
    }

    /// Returns the enum class `E` that this converter targets.
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<E> getType() {
        return enumClass;
    }

    /// Constructs an [EnumConverter] for the given enum class.
    ///
    /// @param enumClass The enum class to convert to.
    /// @throws IllegalArgumentException If `enumClass` is `null`.
    public EnumConverter(@NonNull Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @NonNull
    private Optional<E> cvt(@NonNull Optional<Integer> opt) {
        checkNotNull(opt); // Check recognized by lombok.
        return at(assertPresentGet(opt));
    }

    @NonNull
    private Optional<E> at(int in) {
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
        checkNotNull(in); // Check recognized by lombok.
        checkNotNull(inType); // Check recognized by lombok.

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
