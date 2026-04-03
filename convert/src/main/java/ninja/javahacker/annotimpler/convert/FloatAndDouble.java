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
        var in2 = in.stripTrailingZeros();
        if (in2.scale() < 0) in2 = in2.setScale(0);
        return in2;
    }

    public static BigDecimal makeBig(float in) {
        return normalize(in >= MAX_FLOAT_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal("" + in));
    }

    public static BigDecimal makeBig(double in) {
        return normalize(in >= MAX_DOUBLE_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal("" + in));
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}
