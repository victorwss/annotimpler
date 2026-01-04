package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "AccessingNonPublicFieldOfAnotherObject"})
public class MagicFactoryTest {

    private void testParam(Parameter p, String name, Type expectedType) {
        Assertions.assertAll(
            () -> Assertions.assertEquals(name, p.getName()),
            () -> Assertions.assertEquals(expectedType, p.getType())
        );
    }

    @Test
    public void testMagicEmptyBeanConstructor() throws ConstructionException {
        var magic = MagicFactory.of(Example1.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, magic.arity()),
            () -> Assertions.assertEquals(Example1.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(), magic.getParameterTypes()),
            () -> Assertions.assertEquals(List.of(), magic.getParameters()),
            () -> {
                var obj = magic.create();
                Assertions.assertEquals(Example1.class, obj.getClass());
            }
        );
    }

    public static class Example1 {
        public Example1() {
        }
    }

    @Test
    public void testMagicSimpleBeanConstructor() throws ConstructionException {
        var magic = MagicFactory.of(Example2.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(1, magic.arity()),
            () -> Assertions.assertEquals(Example2.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(1, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple");
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example2.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123)
                );
            }
        );
    }

    public static class Example2 {
        private final String foo123;
        public Example2(String foo123) {
            this.foo123 = foo123;
        }
    }

    @Test
    public void testMagicComplexBeanConstructor() throws ConstructionException {
        var magic = MagicFactory.of(Example3.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example3.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example3.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            }
        );
    }

    public static class Example3 {
        private final String foo123;

        private final int bar456;

        public Example3(String foo123, int bar456) {
            this.foo123 = foo123;
            this.bar456 = bar456;
        }
    }

    @Test
    public void testMagicComplexBeanConstructorCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example4.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example4.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example4.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            }
        );
    }

    public static class Example4 {
        private final String foo123;

        private final int bar456;

        @Creator
        public Example4(String foo123, int bar456) {
            this.foo123 = foo123;
            this.bar456 = bar456;
        }

        public Example4() {
            throw new AssertionError();
        }
    }

    @Test
    public void testMagicComplexBeanMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example5.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example5.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example5.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456),
                    () -> Assertions.assertEquals(777, obj.baz)
                );
            }
        );
    }

    public static class Example5 {
        private final String foo123;

        private final int bar456;

        private final int baz;

        public Example5(String foo123, int bar456, int baz) {
            this.foo123 = foo123;
            this.bar456 = bar456;
            this.baz = baz;
        }

        @Creator
        public static Example5 fubaz(String foo123, int bar456) {
            return new Example5(foo123, bar456, 777);
        }

        public Example5() {
            throw new AssertionError();
        }
    }

    @Test
    public void testMagicEnum() throws ConstructionException {
        var magic = MagicFactory.of(Example6.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, magic.arity()),
            () -> Assertions.assertEquals(Example6.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(), magic.getParameterTypes()),
            () -> Assertions.assertEquals(0, magic.getParameters().size()),
            () -> Assertions.assertEquals(Example6.FOO, magic.create())
        );
    }

    public static enum Example6 {
        FOO;
    }

    @Test
    public void testMagicEnumCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example7.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, magic.arity()),
            () -> Assertions.assertEquals(Example7.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(), magic.getParameterTypes()),
            () -> Assertions.assertEquals(0, magic.getParameters().size()),
            () -> Assertions.assertEquals(Example7.BAR, magic.create())
        );
    }

    public static enum Example7 {
        FOO,
        @Creator BAR,
        MOO,
        XXX;
    }

    @Test
    public void testMagicEmptyRecord() throws ConstructionException {
        var magic = MagicFactory.of(Example8.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(0, magic.arity()),
            () -> Assertions.assertEquals(Example8.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(), magic.getParameterTypes()),
            () -> Assertions.assertEquals(0, magic.getParameters().size()),
            () -> Assertions.assertEquals(Example8.class, magic.create().getClass())
        );
    }

    public static record Example8() {
    }

    @Test
    public void testMagicSimpleRecord() throws ConstructionException {
        var magic = MagicFactory.of(Example9.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(1, magic.arity()),
            () -> Assertions.assertEquals(Example9.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(1, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple");
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example9.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123)
                );
            }
        );
    }

    public static record Example9(String foo123) {
    }

    @Test
    public void testMagicComplexRecord() throws ConstructionException {
        var magic = MagicFactory.of(Example10.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example10.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example10.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            }
        );
    }

    public static record Example10(String foo123, int bar456) {
    }

    @Test
    public void testMagicComplexRecordCanonicalConstructorCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example11.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example11.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example11.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            }
        );
    }

    public static record Example11(String foo123, int bar456) {
        public Example11() {
            this(null, 123);
            throw new AssertionError();
        }
    }

    @Test
    public void testMagicComplexRecordNonCanonicalConstructorCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example12.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example12.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example12.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456),
                    () -> Assertions.assertEquals(777, obj.baz)
                );
            }
        );
    }

    public static record Example12(String foo123, int bar456, int baz) {
        @Creator
        public Example12(String foo123, int bar456) {
            this(foo123, bar456, 777);
        }
    }

    @Test
    public void testMagicComplexRecordMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example13.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example13.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example13.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456),
                    () -> Assertions.assertEquals(777, obj.baz)
                );
            }
        );
    }

    public static record Example13(String foo123, int bar456, int baz) {
        @Creator
        public static Example13 fubaz(String foo123, int bar456) {
            return new Example13(foo123, bar456, 777);
        }
    }

    @Test
    public void testMagicInterfaceMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example14.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example14.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> Assertions.assertEquals(Example14A.class, magic.create("pineapple", 555).getClass())
        );
    }

    public static interface Example14 {
        @Creator
        public static Example14 fubaz(String foo123, int bar456) {
            return new Example14A();
        }
    }

    public static class Example14A implements Example14 {
    }

    @Test
    public void testMagicInterfaceCovariantMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example15.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example15.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> Assertions.assertEquals(Example15A.class, magic.create("pineapple", 555).getClass())
        );
    }

    public static interface Example15 {
        @Creator
        public static Example15A fubaz(String foo123, int bar456) {
            return new Example15A();
        }
    }

    public static class Example15A implements Example15 {
    }

    @Test
    public void testMagicInterfaceGenericCovariantMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(Example16.class);
        Assertions.assertAll(
            () -> Assertions.assertEquals(2, magic.arity()),
            () -> Assertions.assertEquals(Example16.class, magic.getReturnType()),
            () -> Assertions.assertEquals(List.of(String.class, int.class), magic.getParameterTypes()),
            () -> {
                var ps = magic.getParameters();
                Assertions.assertAll(
                    () -> Assertions.assertEquals(2, ps.size()),
                    () -> testParam(ps.getFirst(), "foo123", String.class),
                    () -> testParam(ps.get(1), "bar456", int.class)
                );
            },
            () -> Assertions.assertEquals(Example16A.class, magic.create("pineapple", 555).getClass())
        );
    }

    public static interface Example16<X> {
        @Creator
        public static Example16A<String> fubaz(String foo123, int bar456) {
            return new Example16A<>();
        }
    }

    public static class Example16A<X> implements Example16<X> {
    }
}
