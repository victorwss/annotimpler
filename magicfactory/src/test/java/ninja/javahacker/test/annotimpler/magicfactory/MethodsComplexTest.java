package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;

import org.junit.jupiter.api.function.Executable;

public class MethodsComplexTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

    public static class Something {

        public Something() {
        }

        public Something(int homer, double marge) {
            throw new AssertionError();
        }

        public static void staticMethod1() {
            throw new AssertionError();
        }

        public static int staticMethod2(int c3po, String r2d2) {
            throw new AssertionError();
        }

        public Something instanceMethod1() {
            throw new AssertionError();
        }

        public Something instanceMethod2(String bart, int lisa, Something meggie) {
            throw new AssertionError();
        }
    }

    public interface Weird<A, B, C extends Serializable & Cloneable, D extends Cloneable, E extends Enum<E>, F extends Comparable<F>> {
        public <U extends Thread> Map<A, Map<C[], Map<String, ? extends List<? super E>>>> crazy(D param, F foo, U uh);
    }

    @TestFactory
    public Stream<DynamicTest> testParamMap() throws NoSuchMethodException {
        var c3poR2d2 = Something.class.getMethod("staticMethod2", int.class, String.class);
        var some = new Something();
        var other = new Something();
        var simpsonsKids = Something.class.getMethod("instanceMethod2", String.class, int.class, Something.class);
        var simpsonsParents = Something.class.getConstructor(int.class, double.class);

        return Stream.of(
            n("a", () -> Assertions.assertEquals(Map.of("this", "x"), Methods.paramMap(String.class.getMethod("toString"), "x"))),
            n("b", () -> Assertions.assertEquals(Map.of(), Methods.paramMap(System.class.getMethod("nanoTime")))),
            n("c", () -> Assertions.assertEquals(Map.of(), Methods.paramMap(List.class.getMethod("of")))),
            n("d", () -> Assertions.assertEquals(Map.of(), Methods.paramMap(Something.class.getMethod("staticMethod1")))),
            n("e", () -> Assertions.assertEquals(Map.of("this", some), Methods.paramMap(Something.class.getMethod("instanceMethod1"), some))),
            n("f", () -> Assertions.assertEquals(Map.of(), Methods.paramMap(Something.class.getConstructor()))),

            n("g", () -> Assertions.assertEquals(
                    Map.of("c3po", 5, "r2d2", "bla"),
                    Methods.paramMap(c3poR2d2, 5, "bla")
            )),

            n("h", () -> Assertions.assertEquals(
                    Map.of("this", other, "bart", "santa claus", "lisa", 999, "meggie", some),
                    Methods.paramMap(simpsonsKids, other, "santa claus", 999, some)
            )),

            n("i", () -> Assertions.assertEquals(
                    Map.of("homer", 35, "marge", 33.0),
                    Methods.paramMap(simpsonsParents, 35, 33.0)
            ))
        );
    }

    public record SomeRecord(String wendy, int butters, double mrGarrison) {
    }

    public enum SomeEnum {
        RED, GREEN, YELLOW, BLUE;
    }

    public class SomeClass {
        public static final String stan = "";
        private int kyle;
        private String cartman;
        private Thread kenny;
    }

    @TestFactory
    public Stream<DynamicTest> testReturnType() throws Exception {
        Function<Constructor<?>, DynamicTest> func1 = c -> n("Exec " + c.getName(), () -> Assertions.assertEquals(
                c.getDeclaringClass(),
                Methods.getReturnType((java.lang.reflect.Executable) c)
        ));
        Function<Constructor<?>, DynamicTest> func2 = c -> n("Ctor " + c.getName(), () -> Assertions.assertEquals(
                c.getDeclaringClass(),
                Methods.getReturnType(c)
        ));
        Function<Method, DynamicTest> func3 = m -> n("Exec " + m.getName(), () -> Assertions.assertEquals(
                m.getGenericReturnType(),
                Methods.getReturnType((java.lang.reflect.Executable) m)
        ));
        Function<Method, DynamicTest> func4 = m -> n("Meth " + m.getName(), () -> Assertions.assertEquals(
                m.getGenericReturnType(),
                Methods.getReturnType(m)
        ));
        Function<Field, DynamicTest> func5 = f -> n(
                f.getName(),
                () -> Assertions.assertEquals(f.getGenericType(), Methods.getReturnType(f))
        );
        var ctors = List.of(
            Object.class.getConstructor(),
            Something.class.getConstructor(),
            Something.class.getConstructor(int.class, double.class),
            String.class.getConstructor(byte[].class)
        );
        var meths = List.of(
            Object.class.getMethod("toString"),
            Map.class.getMethod("get", Object.class),
            Something.class.getMethod("instanceMethod2", String.class, int.class, Something.class),
            Weird.class.getMethod("crazy", Cloneable.class, Comparable.class, Thread.class)
        );
        var fields = List.of(
            SomeEnum.class.getField("RED"),
            SomeEnum.class.getField("GREEN"),
            SomeEnum.class.getField("YELLOW"),
            SomeEnum.class.getField("BLUE"),
            SomeClass.class.getField("stan"),
            SomeClass.class.getDeclaredField("kyle"),
            SomeClass.class.getDeclaredField("cartman"),
            SomeClass.class.getDeclaredField("kenny"),
            SomeRecord.class.getDeclaredField("wendy"),
            SomeRecord.class.getDeclaredField("butters"),
            SomeRecord.class.getDeclaredField("mrGarrison")
        );
        var t1 = ctors.stream().map(func1);
        var t2 = ctors.stream().map(func2);
        var t3 = meths.stream().map(func3);
        var t4 = meths.stream().map(func4);
        var t5 = fields.stream().map(func5);
        return Stream.of(t1, t2, t3, t4, t5).flatMap(x -> x);
    }
}
