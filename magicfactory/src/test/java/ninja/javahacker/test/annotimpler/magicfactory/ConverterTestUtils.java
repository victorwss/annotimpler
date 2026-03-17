package ninja.javahacker.test.annotimpler.magicfactory;

import java.lang.reflect.Type;
import java.util.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

public final class ConverterTestUtils {

    public static final Type COLLECTION_DATE;
    public static final Type LIST_STRING;
    public static final Type MAP_STRING_STRING;

    static {
        try {
            var mtd = ConverterTestUtils.class.getDeclaredMethod("noop", Collection.class, List.class, Map.class);
            COLLECTION_DATE = mtd.getParameters()[0].getParameterizedType();
            LIST_STRING = mtd.getParameters()[1].getParameterizedType();
            MAP_STRING_STRING = mtd.getParameters()[2].getParameterizedType();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unused")
    private static void noop(Collection<java.time.LocalDate> a, List<String> b, Map<String, String> c) {}

    public static Arguments args(String name, Executable exec) {
        return Arguments.of(name, exec);
    }

    public static void assertOptionalEquals(Object expected, Optional<?> opt) {
        if (expected == null) {
            Assertions.assertTrue(opt.isEmpty());
        } else {
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(expected, opt.get());
        }
    }

    public static void assertArrayEqualsSafe(Object expected, Object actual) {
        if (expected instanceof byte[] be) {
            Assertions.assertArrayEquals(be, (byte[]) actual);
        } else if (expected instanceof int[] ie) {
            Assertions.assertArrayEquals(ie, (int[]) actual);
        } else {
            Assertions.assertEquals(expected, actual);
        }
    }
}
