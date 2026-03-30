package ninja.javahacker.test.annotimpler.magicfactory;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "AccessingNonPublicFieldOfAnotherObject"})
public class MagicFactoryTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    private static Stream<DynamicTest> testSignature(String name, MagicFactory<?> fac, Type returnType, List<String> names, List<Type> expectedTypes) {
        List<DynamicTest> exec = new ArrayList<>(2 * names.size() + 4);
        exec.add(n(name + "-arity", () -> Assertions.assertEquals(names.size(), fac.arity())));
        exec.add(n(name + "-return", () -> Assertions.assertEquals(returnType, fac.getReturnType())));
        exec.add(n(name + "-types", () -> Assertions.assertEquals(expectedTypes, fac.getParameterTypes())));
        exec.add(n(name + "-size", () -> Assertions.assertEquals(fac.getParameters().size(), names.size())));
        for (var i = 0; i < names.size(); i++) {
            var j = i;
            exec.add(n(name + "-" + j + "-name", () -> Assertions.assertEquals(names.get(j), fac.getParameters().get(j).getName())));
            exec.add(n(name + "-" + j + "-type", () -> Assertions.assertEquals(expectedTypes.get(j), fac.getParameters().get(j).getType())));
        }
        return exec.stream();
    }

    @TestFactory
    public Stream<DynamicTest> testMagicEmptyBeanConstructor() throws Exception {
        var magic = MagicFactory.of(Example1.class);
        var a = Stream.of(n("Example1-result", () -> Assertions.assertEquals(Example1.class, magic.create().getClass())));
        var b = testSignature("Example1", magic, Example1.class,  List.of(), List.of());
        return Stream.concat(a, b);
    }

    public static class Example1 {
        public Example1() {
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMagicSimpleBeanConstructor() throws Exception {
        var magic = MagicFactory.of(Example2.class);
        var a = Stream.of(
            n("Example2-result", () -> {
                var obj = magic.create("pineapple");
                Assertions.assertAll(
                        () -> Assertions.assertEquals(Example2.class, obj.getClass()),
                        () -> Assertions.assertEquals("pineapple", obj.foo123)
                );
        }));
        var b = testSignature("Example2", magic, Example2.class,  List.of("foo123"), List.of(String.class));
        return Stream.concat(a, b);
    }

    public static class Example2 {
        private final String foo123;
        public Example2(String foo123) {
            this.foo123 = foo123;
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMagicComplexBeanConstructor() throws Exception {
        var magic = MagicFactory.of(Example3.class);
        var a = Stream.of(
            n("Example3-result", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example3.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            })
        );
        var b = testSignature("Example3", magic, Example3.class,  List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static class Example3 {
        private final String foo123;

        private final int bar456;

        public Example3(String foo123, int bar456) {
            this.foo123 = foo123;
            this.bar456 = bar456;
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMagicComplexBeanConstructorCreator() throws Exception {
        var magic = MagicFactory.of(Example4.class);
        var a = Stream.of(
            n("Example4-result", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example4.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            })
        );
        var b = testSignature("Example4", magic, Example4.class,  List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
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

    @TestFactory
    public Stream<DynamicTest> testMagicComplexBeanMethodCreator() throws Exception {
        var magic = MagicFactory.of(Example5.class);
        var a = Stream.of(
            n("Example5-result", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example5.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456),
                    () -> Assertions.assertEquals(777, obj.baz)
                );
            })
        );
        var b = testSignature("Example5", magic, Example5.class,  List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
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

    @TestFactory
    public Stream<DynamicTest> testMagicEnum() throws Exception {
        var magic = MagicFactory.of(Example6.class);
        var a = Stream.of(n("Example6-result", () -> Assertions.assertEquals(Example6.FOO, magic.create())));
        var b = testSignature("Example6", magic, Example6.class, List.of(), List.of());
        return Stream.concat(a, b);
    }

    public static enum Example6 {
        FOO;
    }

    @TestFactory
    public Stream<DynamicTest> testMagicEnumCreator() throws Exception {
        var magic = MagicFactory.of(Example7.class);
        var a = Stream.of(n("Example7-result", () -> Assertions.assertEquals(Example7.BAR, magic.create())));
        var b = testSignature("Example7", magic, Example7.class, List.of(), List.of());
        return Stream.concat(a, b);
    }

    public static enum Example7 {
        FOO,
        @Creator BAR,
        MOO,
        XXX;
    }

    @TestFactory
    public Stream<DynamicTest> testMagicEmptyRecord() throws Exception {
        var magic = MagicFactory.of(Example8.class);
        var a = Stream.of(n("Example8-result-class", () -> Assertions.assertEquals(Example8.class, magic.create().getClass())));
        var b = testSignature("Example8", magic, Example8.class, List.of(), List.of());
        return Stream.concat(a, b);
    }

    public static record Example8() {
    }

    @TestFactory
    public Stream<DynamicTest> testMagicSimpleRecord() throws Exception {
        var magic = MagicFactory.of(Example9.class);
        var a = Stream.of(
            n("Example9-result-class", () -> {
                var obj = magic.create("pineapple");
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example9.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123)
                );
            })
        );
        var b = testSignature("Example9", magic, Example9.class, List.of("foo123"), List.of(String.class));
        return Stream.concat(a, b);
    }

    public static record Example9(String foo123) {
    }

    @TestFactory
    public Stream<DynamicTest> testMagicComplexRecord() throws Exception {
        var magic = MagicFactory.of(Example10.class);
        var a = Stream.of(
            n("Example10-result-class", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example10.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            })
        );
        var b = testSignature("Example10", magic, Example10.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static record Example10(String foo123, int bar456) {
    }

    @TestFactory
    public Stream<DynamicTest> testMagicComplexRecordCanonicalConstructorCreator() throws Exception {
        var magic = MagicFactory.of(Example11.class);
        var a = Stream.of(
            n("Example11-result-class", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example11.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456)
                );
            })
        );
        var b = testSignature("Example11", magic, Example11.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static record Example11(String foo123, int bar456) {
        public Example11() {
            this(null, 123);
            throw new AssertionError();
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMagicComplexRecordNonCanonicalConstructorCreator() throws Exception {
        var magic = MagicFactory.of(Example12.class);
        var a = Stream.of(
            n("Example12-result-class", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example12.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456),
                    () -> Assertions.assertEquals(777, obj.baz)
                );
            })
        );
        var b = testSignature("Example12", magic, Example12.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static record Example12(String foo123, int bar456, int baz) {
        @Creator
        public Example12(String foo123, int bar456) {
            this(foo123, bar456, 777);
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMagicComplexRecordMethodCreator() throws Exception {
        var magic = MagicFactory.of(Example13.class);
        var a = Stream.of(
            n("Example13-result-class", () -> {
                var obj = magic.create("pineapple", 555);
                Assertions.assertAll(
                    () -> Assertions.assertEquals(Example13.class, obj.getClass()),
                    () -> Assertions.assertEquals("pineapple", obj.foo123),
                    () -> Assertions.assertEquals(555, obj.bar456),
                    () -> Assertions.assertEquals(777, obj.baz)
                );
            })
        );
        var b = testSignature("Example13", magic, Example13.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static record Example13(String foo123, int bar456, int baz) {
        @Creator
        public static Example13 fubaz(String foo123, int bar456) {
            return new Example13(foo123, bar456, 777);
        }
    }

    @TestFactory
    public Stream<DynamicTest> testMagicInterfaceMethodCreator() throws Exception {
        var magic = MagicFactory.of(Example14.class);
        var a = Stream.of(n("Example14-result-class", () -> Assertions.assertEquals(Example14A.class, magic.create("pineapple", 555).getClass())));
        var b = testSignature("Example14", magic, Example14.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static interface Example14 {
        @Creator
        public static Example14 fubaz(String foo123, int bar456) {
            return new Example14A();
        }
    }

    public static class Example14A implements Example14 {
    }

    @TestFactory
    public Stream<DynamicTest> testMagicInterfaceCovariantMethodCreator() throws Exception {
        var magic = MagicFactory.of(Example15.class);
        var a = Stream.of(n("Example15-result-class", () -> Assertions.assertEquals(Example15A.class, magic.create("pineapple", 555).getClass())));
        var b = testSignature("Example15", magic, Example15.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static interface Example15 {
        @Creator
        public static Example15A fubaz(String foo123, int bar456) {
            return new Example15A();
        }
    }

    public static class Example15A implements Example15 {
    }

    @TestFactory
    public Stream<DynamicTest> testMagicInterfaceGenericCovariantMethodCreator() throws Exception {
        var magic = MagicFactory.of(Example16.class);
        var a = Stream.of(n("Example16-result-class", () -> Assertions.assertEquals(Example16A.class, magic.create("pineapple", 555).getClass())));
        var b = testSignature("Example16", magic, Example16.class, List.of("foo123", "bar456"), List.of(String.class, int.class));
        return Stream.concat(a, b);
    }

    public static interface Example16<X> {
        @Creator
        public static Example16A<String> fubaz(String foo123, int bar456) {
            return new Example16A<>();
        }
    }

    public static class Example16A<X> implements Example16<X> {
    }

    @TestFactory
    public Stream<DynamicTest> testDefaultConstructorWithinMultiple() throws Exception {
        var magic = MagicFactory.of(Example17.class);
        var a = Stream.of(n("Example17-result", () -> Assertions.assertEquals(Example17.class, magic.create().getClass())));
        var b = testSignature("Example17", magic, Example17.class,  List.of(), List.of());
        return Stream.concat(a, b);
    }

    public static class Example17 {
        public Example17() {
        }

        public Example17(int x) {
            throw new AssertionError();
        }
    }
}
