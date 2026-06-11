package ninja.javahacker.test.annotimpler.convert;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.convert;

@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
public class NumericConverterRegressionTest {
    // -------------------------------------------------------------------------
    // Regression tests for FloatAndDouble.makeBig(float/double) with negative
    // large-magnitude values (absolute value >= 2^24 for float, 2^53 for double).
    //
    // Bug: the old code used "in >= threshold" which is always false for negatives,
    // causing large negative floats to go through Float.toString() → scientific
    // notation → BigDecimal that loses the last few units of precision.
    //
    // Example: -489876544F → Float.toString → "-4.8987654E8" → BigDecimal -489876540
    //          instead of the correct BigDecimal -489876544.
    // -------------------------------------------------------------------------

    /** A large negative float that is exactly representable and whose absolute value exceeds 2^24. */
    private static final float LARGE_NEG_FLOAT  = -489876544F;
    private static final BigDecimal LARGE_NEG_BD = new BigDecimal("-489876544");

    /** A large negative double that is exactly representable and whose absolute value exceeds 2^53. */
    private static final double LARGE_NEG_DOUBLE  = -9876543210987654D;
    private static final BigDecimal LARGE_NEG_DBD = new BigDecimal("-9876543210987654");

    @Test
    public void largeNegativeFloat_toBigDecimal_isExact() throws ConvertionException {
        var result = BigDecimalConverter.INSTANCE.from(LARGE_NEG_FLOAT).orElseThrow();
        Assertions.assertEquals(0, LARGE_NEG_BD.compareTo(result),
                "BigDecimalConverter.from(-489876544F) should produce exactly -489876544");
    }

    @Test
    public void largeNegativeBigDecimal_toFloat_isExact() throws ConvertionException {
        var result = FloatConverter.PRIMITIVE.from(LARGE_NEG_BD).orElseThrow();
        Assertions.assertEquals(LARGE_NEG_FLOAT, result,
                "FloatConverter.from(BigDecimal(\"-489876544\")) should round-trip back to -489876544F");
    }

    @Test
    public void largeNegativeFloat_toDouble_isExact() throws ConvertionException {
        var result = DoubleConverter.PRIMITIVE.from(LARGE_NEG_FLOAT).orElseThrow();
        Assertions.assertEquals((double) LARGE_NEG_FLOAT, result,
                "DoubleConverter.from(-489876544F) should produce exactly -489876544.0");
    }

    @Test
    public void largeNegativeDouble_toBigDecimal_isExact() throws ConvertionException {
        var result = BigDecimalConverter.INSTANCE.from(LARGE_NEG_DOUBLE).orElseThrow();
        Assertions.assertEquals(0, LARGE_NEG_DBD.compareTo(result),
                "BigDecimalConverter.from(-9876543210987654D) should produce exactly -9876543210987654");
    }

    @Test
    public void largeNegativeBigDecimal_toDouble_isExact() throws ConvertionException {
        var result = DoubleConverter.PRIMITIVE.from(LARGE_NEG_DBD).orElseThrow();
        Assertions.assertEquals(LARGE_NEG_DOUBLE, result,
                "DoubleConverter.from(BigDecimal(\"-9876543210987654\")) should round-trip back to -9876543210987654D");
    }
}
