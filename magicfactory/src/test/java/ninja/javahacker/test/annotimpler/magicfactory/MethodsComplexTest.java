package ninja.javahacker.test.annotimpler.magicfactory;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

import org.junit.jupiter.api.function.Executable;

public class MethodsComplexTest {

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

    @Test
    public void testParamMap() throws NoSuchMethodException {
        var c3poR2d2 = Something.class.getMethod("staticMethod2", int.class, String.class);
        var some = new Something();
        var simpsonsKids = Something.class.getMethod("instanceMethod2", String.class, int.class, Something.class);
        var simpsonsParents = Something.class.getConstructor(int.class, double.class);

        Assertions.assertAll(
            () -> Assertions.assertEquals(Map.of(), Methods.paramMap(String.class.getMethod("toString"))),
            () -> Assertions.assertEquals(Map.of(), Methods.paramMap(System.class.getMethod("nanoTime"))),
            () -> Assertions.assertEquals(Map.of(), Methods.paramMap(List.class.getMethod("of"))),
            () -> Assertions.assertEquals(Map.of(), Methods.paramMap(Something.class.getMethod("staticMethod1"))),
            () -> Assertions.assertEquals(Map.of(), Methods.paramMap(Something.class.getMethod("instanceMethod1"))),
            () -> Assertions.assertEquals(Map.of(), Methods.paramMap(Something.class.getConstructor())),

            () -> Assertions.assertEquals(
                    Map.of("c3po", 5, "r2d2", "bla"),
                    Methods.paramMap(c3poR2d2, 5, "bla", 2, 1)
            ),

            () -> Assertions.assertEquals(
                    Map.of("bart", "santa claus", "lisa", 999, "meggie", some),
                    Methods.paramMap(simpsonsKids, "santa claus", 999, some)
            ),

            () -> Assertions.assertEquals(
                    Map.of("homer", 35, "marge", 33.0),
                    Methods.paramMap(simpsonsParents, 35, 33.0)
            )
        );
    }

    @Test
    public void testReturnTypeMethod() throws NoSuchMethodException {
        Function<Method, Executable> func = m -> () -> Assertions.assertEquals(
                m.getGenericReturnType(),
                Methods.getReturnType(m)
        );
        var all = List.of(
            Object.class.getMethod("toString"),
            Map.class.getMethod("get", Object.class),
            Something.class.getMethod("instanceMethod2", String.class, int.class, Something.class),
            Weird.class.getMethod("crazy", Cloneable.class, Comparable.class, Thread.class)
        );
        Assertions.assertAll(all.stream().map(func).toList());
    }

    @Test
    public void testReturnTypeMethodExec() throws NoSuchMethodException {
        Function<Method, Executable> func = m -> () -> Assertions.assertEquals(
                m.getGenericReturnType(),
                Methods.getReturnType((java.lang.reflect.Executable) m)
        );
        var all = List.of(
            Object.class.getMethod("toString"),
            Map.class.getMethod("get", Object.class),
            Something.class.getMethod("instanceMethod2", String.class, int.class, Something.class),
            Weird.class.getMethod("crazy", Cloneable.class, Comparable.class, Thread.class)
        );
        Assertions.assertAll(all.stream().map(func).toList());
    }

    @Test
    public void testReturnTypeConstructor() throws NoSuchMethodException {
        Function<Constructor<?>, Executable> func = c -> () -> Assertions.assertEquals(
                c.getDeclaringClass(),
                Methods.getReturnType(c)
        );
        var all = List.of(
            Object.class.getConstructor(),
            Something.class.getConstructor(),
            Something.class.getConstructor(int.class, double.class),
            String.class.getConstructor(byte[].class)
        );
        Assertions.assertAll(all.stream().map(func).toList());
    }

    @Test
    public void testReturnTypeConstructorExec() throws NoSuchMethodException {
        Function<Constructor<?>, Executable> func = c -> () -> Assertions.assertEquals(
                c.getDeclaringClass(),
                Methods.getReturnType((java.lang.reflect.Executable) c)
        );
        var all = List.of(
            Object.class.getConstructor(),
            Something.class.getConstructor(),
            Something.class.getConstructor(int.class, double.class),
            String.class.getConstructor(byte[].class)
        );
        Assertions.assertAll(all.stream().map(func).toList());
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

    public record SomeRecord(String wendy, int butters, double mrGarrison) {
    }

    @Test
    public void testReturnTypeField() throws NoSuchFieldException {
        Function<Field, Executable> func = f -> () -> Assertions.assertEquals(f.getGenericType(), Methods.getReturnType(f));
        var all = List.of(
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
        Assertions.assertAll(all.stream().map(func).toList());
    }
}
