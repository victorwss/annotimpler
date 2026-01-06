package ninja.javahacker.test.annotimpler.core;

import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module org.junit.jupiter.api;

public class PropertyBagTest {

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

    @Test
    @SuppressWarnings("unchecked")
    public void testBagAddRemoveGetEqualsToStringHashCode() {
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

        Assertions.assertAll(
                () -> Assertions.assertSame(PropertyBag.root(), props1a),
                () -> Assertions.assertSame(props1a, props1b),

                () -> Assertions.assertEquals(props2a, props2b),
                () -> Assertions.assertEquals(props2a, props2c),
                () -> Assertions.assertEquals(props2a, props2d),
                () -> Assertions.assertEquals(props2a.hashCode(), props2b.hashCode()),
                () -> Assertions.assertEquals(props2a.toString(), props2b.toString()),

                () -> Assertions.assertEquals(props3a, props3b),
                () -> Assertions.assertEquals(props3a.hashCode(), props3b.hashCode()),
                () -> Assertions.assertEquals(props3a.toString(), props3b.toString()),

                () -> Assertions.assertEquals("A", props4.get(ka)),
                () -> Assertions.assertEquals("B", props4.get(kb)),
                () -> Assertions.assertEquals(5, props4.get(kc)),
                () -> Assertions.assertEquals(9, props4.get(kd)),
                () -> Assertions.assertEquals("qa", props4.get(ke)),
                () -> Assertions.assertEquals("qb", props4.get(kf)),

                () -> Assertions.assertFalse(props1a.equals(null)),
                () -> Assertions.assertNotEquals(props1a, props2a),
                () -> Assertions.assertNotEquals(props1a, props3a),
                () -> Assertions.assertNotEquals(props1a, props4),
                () -> Assertions.assertNotEquals(props2a, props3a),
                () -> Assertions.assertNotEquals(props2a, props4),
                () -> Assertions.assertNotEquals(props3a, props4),

                () -> {
                    var ex = Assertions.assertThrows(PropertyBag.PropertyNotFoundException.class, () -> props3b.get(kf));
                    Assertions.assertSame(kf, ex.getProperty());
                },
                () -> {
                    var kk = (KeyProperty) kc;
                    var ex = Assertions.assertThrows(PropertyBag.IllegalPropertyValueException.class, () -> props3b.add(kk, "x"));
                    Assertions.assertSame(kc, ex.getProperty());
                }
        );
    }

    @Test
    public void testNulls() {
        Assertions.assertAll(
                () -> ForTests.testNull("key", () -> PropertyBag.root().add(null, "x"), "add-key"),
                () -> ForTests.testNull("value", () -> PropertyBag.root().add(new TestKey2("x"), null), "add-value"),
                () -> ForTests.testNull("key", () -> PropertyBag.root().remove(null), "remove-key"),
                () -> ForTests.testNull("key", () -> PropertyBag.root().get(null), "get-key"),
                () -> ForTests.testNull("property", () -> new PropertyBag.PropertyNotFoundException(null), "pnfe-ctor"),
                () -> ForTests.testNull("property", () -> new PropertyBag.IllegalPropertyValueException(null), "ipve-ctor")
        );
    }
}
