package ninja.javahacker.annotimpler.convert;

import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// Utility methods for converting `float` and `double` primitive values to [BigDecimal], choosing between
/// the string-based constructor (to preserve human-intended decimal values) and the binary-exact constructor
/// (for magnitudes where every representable value is an integer anyway).
@PackagePrivate
final class FloatAndDouble {

    /// The magnitude threshold (2^24) above which every representable `float` is an integer.
    private static final float MAX_FLOAT_WITH_INT_PRECISION = 16777216F; // 2^24

    /// The magnitude threshold (2^53) above which every representable `double` is an integer.
    private static final double MAX_DOUBLE_WITH_INT_PRECISION = 9007199254740992D; // 2^53

    @Generated
    private FloatAndDouble() {
        throw new AssertionError();
    }

    @NonNull
    private static BigDecimal normalize(@NonNull BigDecimal in) {
        checkNotNull(in); // Check recognized by lombok.
        var in2 = in.stripTrailingZeros();
        if (in2.scale() < 0) in2 = in2.setScale(0);
        return in2;
    }

    /// Converts a finite `float` to its "simplest" [BigDecimal] representation.
    ///
    /// For values whose absolute magnitude is below [#MAX_FLOAT_WITH_INT_PRECISION] (2^24),
    /// the string-based constructor is used so that human-intended values such as `0.078`
    /// are preserved exactly. For larger magnitudes, where every representable `float` is an
    /// integer, the binary-exact constructor is used.
    ///
    /// @param in A finite `float` value (infinities and NaN are not accepted).
    /// @return The [BigDecimal] equivalent, with trailing zeros stripped.
    @NonNull
    @SuppressWarnings("PMD.AvoidDecimalLiteralsInBigDecimalConstructor") // Because we test for safe cases.
    public static BigDecimal makeBig(float in) {
        assertFloatOk(in);
        return normalize(Math.abs(in) >= MAX_FLOAT_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal(Float.toString(in)));
    }

    /// Converts a finite `double` to its "simplest" [BigDecimal] representation.
    ///
    /// For values whose absolute magnitude is below [#MAX_DOUBLE_WITH_INT_PRECISION] (2^53),
    /// the string-based constructor is used so that human-intended values such as `0.078`
    /// are preserved exactly. For larger magnitudes, where every representable `double` is an
    /// integer, the binary-exact constructor is used.
    ///
    /// @param in A finite `double` value (infinities and NaN are not accepted).
    /// @return The [BigDecimal] equivalent, with trailing zeros stripped.
    @NonNull
    @SuppressWarnings("PMD.AvoidDecimalLiteralsInBigDecimalConstructor") // Because we test for safe cases.
    public static BigDecimal makeBig(double in) {
        assertDoubleOk(in);
        return normalize(Math.abs(in) >= MAX_DOUBLE_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal(Double.toString(in)));
    }

    /// Converts a finite `float` to its "simplest" [BigDecimal] representation, throwing
    /// [ConvertionException] for non-finite values.
    ///
    /// @param in The `float` to convert.
    /// @param target The target type reported in the exception, if thrown.
    /// @return The [BigDecimal] equivalent, with trailing zeros stripped.
    /// @throws ConvertionException If `in` is infinite or NaN.
    /// @throws IllegalArgumentException If `target` is `null`.
    @NonNull
    @SuppressWarnings("PMD.AvoidDecimalLiteralsInBigDecimalConstructor") // Because we test for safe cases.
    public static BigDecimal makeBig(float in, @NonNull Class<?> target) throws ConvertionException {
        checkNotNull(target); // Check recognized by lombok.
        if (in == Float.POSITIVE_INFINITY || in == Float.NEGATIVE_INFINITY || Float.isNaN(in)) {
            throw new ConvertionException(float.class, target);
        }
        return normalize(Math.abs(in) >= MAX_FLOAT_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal(Float.toString(in)));
    }

    /// Converts a finite `double` to its "simplest" [BigDecimal] representation, throwing
    /// [ConvertionException] for non-finite values.
    ///
    /// @param in The `double` to convert.
    /// @param target The target type reported in the exception, if thrown.
    /// @return The [BigDecimal] equivalent, with trailing zeros stripped.
    /// @throws ConvertionException If `in` is infinite or NaN.
    /// @throws IllegalArgumentException If `target` is `null`.
    @NonNull
    @SuppressWarnings("PMD.AvoidDecimalLiteralsInBigDecimalConstructor") // Because we test for safe cases.
    public static BigDecimal makeBig(double in, @NonNull Class<?> target) throws ConvertionException {
        checkNotNull(target); // Check recognized by lombok.
        if (in == Double.POSITIVE_INFINITY || in == Double.NEGATIVE_INFINITY || Double.isNaN(in)) {
            throw new ConvertionException(double.class, target);
        }
        return normalize(Math.abs(in) >= MAX_DOUBLE_WITH_INT_PRECISION ? new BigDecimal(in) : new BigDecimal(Double.toString(in)));
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
