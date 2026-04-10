package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

@PackagePrivate
final class FloatAndDouble {

    private static final float MAX_FLOAT_WITH_INT_PRECISION = 16777216F; // 2 ** 24
    private static final double MAX_DOUBLE_WITH_INT_PRECISION = 9007199254740992D; // 2 ** 53

    @Generated
    private FloatAndDouble() {
        throw new AssertionError();
    }

    @NonNull
    private static BigDecimal normalize(@NonNull BigDecimal in) {
        checkNotNull(in);
        var in2 = in.stripTrailingZeros();
        if (in2.scale() < 0) in2 = in2.setScale(0);
        return in2;
    }

    @NonNull
    public static BigDecimal makeBig(float in) {
        assertFloatOk(in);
        return normalize(in >= MAX_FLOAT_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal("" + in));
    }

    @NonNull
    public static BigDecimal makeBig(double in) {
        assertDoubleOk(in);
        return normalize(in >= MAX_DOUBLE_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal("" + in));
    }

    @NonNull
    public static BigDecimal makeBig(float in, @NonNull Class<?> target) throws ConvertionException {
        checkNotNull(target);
        if (in == Float.POSITIVE_INFINITY || in == Float.NEGATIVE_INFINITY || Float.isNaN(in)) {
            throw new ConvertionException(float.class, target);
        }
        return normalize(in >= MAX_FLOAT_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal("" + in));
    }

    @NonNull
    public static BigDecimal makeBig(double in, @NonNull Class<?> target) throws ConvertionException {
        checkNotNull(target);
        if (in == Double.POSITIVE_INFINITY || in == Double.NEGATIVE_INFINITY || Double.isNaN(in)) {
            throw new ConvertionException(double.class, target);
        }
        return normalize(in >= MAX_DOUBLE_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal("" + in));
    }

    @Generated
    private static void assertFloatOk(float in) {
        if (in == Float.POSITIVE_INFINITY || in == Float.NEGATIVE_INFINITY || Float.isNaN(in)) {
            throw new AssertionError();
        }
    }

    @Generated
    private static void assertDoubleOk(double in) {
        if (in == Double.POSITIVE_INFINITY || in == Double.NEGATIVE_INFINITY || Double.isNaN(in)) {
            throw new AssertionError();
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
