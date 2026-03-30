package ninja.javahacker.test.annotimpler.core;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module org.junit.jupiter.api;

@SuppressWarnings("missing-explicit-ctor")
public class PropertyBagTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    public static record TestKey1(int x, String a, int b) implements KeyProperty<String> {
        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }

    public static record TestKey2(String a) implements KeyProperty<Integer> {
        @Override
        public Class<Integer> valueType() {
            return Integer.class;
        }
    }

    public static record TestKey3(float x, BigInteger y) implements KeyProperty<String> {
        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }

    @TestFactory
    @SuppressWarnings({"unchecked", "rawtypes", "ObjectEqualsNull", "AssertEqualsBetweenInconvertibleTypes"})
    public Stream<DynamicTest> testBagAddRemoveGetEqualsToStringHashCode() {
        var ka = new TestKey1(1, "a", 4);
        var kb = new TestKey1(3, "b", 9);
        var kc = new TestKey2("x");
        var kd = new TestKey2("y");
        var ke = new TestKey3(5.0f, BigInteger.ZERO);
        var kf = new TestKey3(7.0f, BigInteger.ONE);

        var props1a = PropertyBag.root();
        var props2a = props1a.add(ka, "A");
        var props2b = props1a.add(ka, "A");
        var props3a = props2a.add(kb, "B");
        var props3b = props2b.add(kb, "B");
        var props2c = props3b.remove(kb);
        var props2d = props2c.remove(kf);
        var props1b = props2d.remove(ka);

        var props4 = props3a.add(kc, 5).add(kd, 9).add(ke, "qa").add(kf, "qb");

        return Stream.of(
                n("a", () -> Assertions.assertSame(PropertyBag.root(), props1a)),
                n("b", () -> Assertions.assertSame(props1a, props1b)),

                n("c", () -> Assertions.assertEquals(props2a, props2b)),
                n("d", () -> Assertions.assertEquals(props2a, props2c)),
                n("e", () -> Assertions.assertEquals(props2a, props2d)),
                n("f", () -> Assertions.assertEquals(props2a.hashCode(), props2b.hashCode())),
                n("g", () -> Assertions.assertEquals(props2a.toString(), props2b.toString())),

                n("h", () -> Assertions.assertEquals(props3a, props3b)),
                n("i", () -> Assertions.assertEquals(props3a.hashCode(), props3b.hashCode())),
                n("j", () -> Assertions.assertEquals(props3a.toString(), props3b.toString())),

                n("k", () -> Assertions.assertEquals("A", props4.get(ka))),
                n("l", () -> Assertions.assertEquals("B", props4.get(kb))),
                n("m", () -> Assertions.assertEquals(5, props4.get(kc))),
                n("n", () -> Assertions.assertEquals(9, props4.get(kd))),
                n("o", () -> Assertions.assertEquals("qa", props4.get(ke))),
                n("p", () -> Assertions.assertEquals("qb", props4.get(kf))),

                n("q", () -> Assertions.assertFalse(props1a.equals(null))),
                n("r", () -> Assertions.assertNotEquals(props1a, props2a)),
                n("s", () -> Assertions.assertNotEquals(props1a, props3a)),
                n("t", () -> Assertions.assertNotEquals(props1a, props4)),
                n("u", () -> Assertions.assertNotEquals(props2a, props3a)),
                n("v", () -> Assertions.assertNotEquals(props2a, props4)),
                n("w", () -> Assertions.assertNotEquals(props3a, props4)),

                n("x", () -> {
                    var ex = Assertions.assertThrows(PropertyBag.PropertyNotFoundException.class, () -> props3b.get(kf));
                    Assertions.assertSame(kf, ex.getProperty());
                }),
                n("y", () -> {
                    var kk = (KeyProperty) kc;
                    var ex = Assertions.assertThrows(PropertyBag.IllegalPropertyValueException.class, () -> props3b.add(kk, "x"));
                    Assertions.assertSame(kc, ex.getProperty());
                })
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                n("add-key", () -> ForTests.testNull("key", () -> PropertyBag.root().add(null, "x"))),
                n("add-value", () -> ForTests.testNull("value", () -> PropertyBag.root().add(new TestKey2("x"), null))),
                n("remove-key", () -> ForTests.testNull("key", () -> PropertyBag.root().remove(null))),
                n("get-key", () -> ForTests.testNull("key", () -> PropertyBag.root().get(null))),
                n("pnfe-ctor", () -> ForTests.testNull("property", () -> new PropertyBag.PropertyNotFoundException(null))),
                n("ipve-ctor", () -> ForTests.testNull("property", () -> new PropertyBag.IllegalPropertyValueException(null)))
        );
    }
}
