package ninja.javahacker.test.annotimpler.core;

import lombok.NonNull;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

@SuppressWarnings({"missing-explicit-ctor", "AssertEqualsBetweenInconvertibleTypes"})
public class AnnotimplerTest {

    @ImplementedBy(TestImpl1.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnno1 {
    }

    public static interface TestIface1 {
        @TestAnno1
        public String foo();
    }

    public static class TestImpl1 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestIface1.class, iface),
                    () -> Assertions.assertEquals(TestIface1.class.getMethod("foo"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return (e, a) -> {
                Assertions.assertAll(
                        () -> Assertions.assertTrue(iface.isInstance(e)),
                        () -> Assertions.assertEquals(0, a.length)
                );
                return "foo-test";
            };
        }
    }

    @Test
    public void testSingleImpl1() {
        var impl = AnnotationsImplementor.implement(TestIface1.class);
        Assertions.assertEquals("foo-test", impl.foo());
    }

    @Test
    public void testSingleImpl2() {
        var impl = AnnotationsImplementor.implement(TestIface1.class, null);
        Assertions.assertEquals("foo-test", impl.foo());
    }

    @Test
    public void testSingleImpl3() {
        var impl = AnnotationsImplementor.implement(TestIface1.class, PropertyBag.root());
        Assertions.assertEquals("foo-test", impl.foo());
    }

    @ImplementedBy(TestImpl2.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnno2 {
    }

    @ImplementedBy(TestImpl3.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnno3 {
    }

    @ImplementedBy(TestImpl4.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnno4 {
    }

    @ImplementedBy(TestImpl5.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnno5 {
    }

    public static class FooException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static class WtfException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static interface TestIface2 {
        @TestAnno2
        public String foo(int x, String y);

        @TestAnno2
        public int bar(String z, String y, String x, int a, float b);

        @TestAnno3
        public void baz();

        @TestAnno4
        public void oops() throws FooException;

        @TestAnno5
        public void wtf() throws FooException;
    }

    public static class TestImpl2 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestIface2.class, iface),
                    () -> Assertions.assertTrue(List.of("foo", "bar").contains(m.getName()))
            );
            if ("foo".equals(m.getName())) {
                Assertions.assertArrayEquals(new Class<?>[] {int.class, String.class}, m.getParameterTypes());
                return (e, a) -> {
                    Assertions.assertAll(
                            () -> Assertions.assertTrue(iface.isInstance(e)),
                            () -> Assertions.assertArrayEquals(new Object[] {5, "wha"}, a)
                    );
                    return "foo2-test";
                };
            } else if ("bar".equals(m.getName())) {
                var arr = new Class<?>[] {String.class, String.class, String.class, int.class, float.class};
                Assertions.assertArrayEquals(arr, m.getParameterTypes());
                return (e, a) -> {
                    Assertions.assertAll(
                            () -> Assertions.assertTrue(iface.isInstance(e)),
                            () -> Assertions.assertArrayEquals(new Object[] {"pa", "pe", "pi", 12, 13.0f}, a)
                    );
                    return 42;
                };
            } else {
                throw new AssertionError();
            }
        }
    }

    public static class TestImpl3 implements Implementation {
        public static boolean CALLED = false;

        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestIface2.class, iface),
                    () -> Assertions.assertEquals(TestIface2.class.getMethod("baz"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return (e, a) -> {
                Assertions.assertAll(
                        () -> Assertions.assertTrue(iface.isInstance(e)),
                        () -> Assertions.assertEquals(0, a.length)
                );
                CALLED = true;
                return null;
            };
        }
    }

    public static class TestImpl4 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestIface2.class, iface),
                    () -> Assertions.assertEquals(TestIface2.class.getMethod("oops"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return (e, a) -> {
                Assertions.assertAll(
                        () -> Assertions.assertTrue(iface.isInstance(e)),
                        () -> Assertions.assertEquals(0, a.length)
                );
                throw new FooException();
            };
        }
    }

    public static class TestImpl5 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestIface2.class, iface),
                    () -> Assertions.assertEquals(TestIface2.class.getMethod("wtf"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return (e, a) -> {
                Assertions.assertAll(
                        () -> Assertions.assertTrue(iface.isInstance(e)),
                        () -> Assertions.assertEquals(0, a.length)
                );
                throw new WtfException();
            };
        }
    }

    @Test
    public void testParamImpl() {
        TestImpl3.CALLED = false;
        var impl = AnnotationsImplementor.implement(TestIface2.class);
        Assertions.assertFalse(TestImpl3.CALLED);
        Assertions.assertAll(
                () -> Assertions.assertEquals("foo2-test", impl.foo(5, "wha")),
                () -> Assertions.assertEquals(42, impl.bar("pa", "pe", "pi", 12, 13.0f)),
                () -> Assertions.assertDoesNotThrow(impl::baz),
                () -> Assertions.assertThrows(FooException.class, impl::oops)
        );
        Assertions.assertTrue(TestImpl3.CALLED);
    }

    @Test
    public void testParamImplWtf() {
        var impl = AnnotationsImplementor.implement(TestIface2.class);
        var ex = Assertions.assertThrows(UndeclaredThrowableException.class, impl::wtf);
        Assertions.assertEquals(WtfException.class, ex.getCause().getClass());
    }

    @ImplementedBy(TestBadImpl1.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno1 {
    }

    @ImplementedBy(TestBadImpl2.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno2 {
    }

    public static interface TestBadIface1 {
        @TestBadAnno1
        public String foo();

        @TestBadAnno2
        public String crash();
    }

    public static class TestBadImpl1 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestBadIface1.class, iface),
                    () -> Assertions.assertEquals(TestBadIface1.class.getMethod("foo"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return (e, a) -> {
                throw new AssertionError();
            };
        }
    }

    public static class TestBadImpl2 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props)
                throws ConstructionException
        {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestBadIface1.class, iface),
                    () -> Assertions.assertEquals(TestBadIface1.class.getMethod("crash"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            throw new ConstructionException("bad", iface);
        }
    }

    @Test
    public void testBadImpl1And2() {
        var c = XSupplier.ImplementationFailedException.class;
        var ex = Assertions.assertThrows(c, () -> AnnotationsImplementor.implement(TestBadIface1.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(ConstructionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals(TestBadIface1.class, ((ConstructionException) ex.getCause()).getRoot()),
                () -> Assertions.assertEquals("bad", ex.getCause().getMessage())
        );
    }

    public static class TestBadImpl3 implements Implementation {
        @NonNull
        @Override
        @SuppressWarnings("null")
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestBadIface3.class, iface),
                    () -> Assertions.assertEquals(TestBadIface3.class.getMethod("crash"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return null;
        }
    }

    @ImplementedBy(TestBadImpl3.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno3 {
    }

    public static interface TestBadIface3 {
        @TestBadAnno3
        public String crash();
    }

    @Test
    public void testBadImpl3() {
        var c = XSupplier.ImplementationFailedException.class;
        var msg = "Implementation was null on: TestBadIface3/String TestBadIface3.crash()";
        var ex = Assertions.assertThrows(c, () -> AnnotationsImplementor.implement(TestBadIface3.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(ConstructionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals(TestBadIface3.class, ((ConstructionException) ex.getCause()).getRoot()),
                () -> Assertions.assertEquals(msg, ex.getCause().getMessage())
        );
    }

    public static interface TestBadIface4 {
        public String crash();
    }

    @Test
    public void testBadImpl4() {
        var c = XSupplier.ImplementationFailedException.class;
        var msg = "Method String TestBadIface4.crash() lacks annotation-defined implementation.";
        var ex = Assertions.assertThrows(c, () -> AnnotationsImplementor.implement(TestBadIface4.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(ConstructionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals(TestBadIface4.class, ((ConstructionException) ex.getCause()).getRoot()),
                () -> Assertions.assertEquals(msg, ex.getCause().getMessage())
        );
    }

    @Test
    public void testBadImpl5() {
        var ex = Assertions.assertThrows(UnsupportedOperationException.class, () -> AnnotationsImplementor.implement(String.class));
        Assertions.assertEquals(null, ex.getMessage());
    }

    @ImplementedBy(TestImpl6.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnno6 {
    }

    public static interface TestStdIface6 {
        public default int foo(int x) {
            Assertions.assertEquals(55, x);
            return 92;
        }

        @TestAnno6
        public default String wtf() {
            throw new AssertionError();
        }
    }

    public static class TestImpl6 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestStdIface6.class, iface),
                    () -> Assertions.assertEquals(TestStdIface6.class.getMethod("wtf"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            return (e, a) -> {
                Assertions.assertAll(
                        () -> Assertions.assertTrue(iface.isInstance(e)),
                        () -> Assertions.assertEquals(0, a.length)
                );
                return "whoot";
            };
        }
    }

    @Test
    public void testStdImpl6() {
        Assertions.assertEquals(92, AnnotationsImplementor.implement(TestStdIface6.class).foo(55));
    }

    @Test
    public void testStdImpl6Override() {
        Assertions.assertEquals("whoot", AnnotationsImplementor.implement(TestStdIface6.class).wtf());
    }

    @ImplementedBy(TestBadImpl7.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno7 {
    }

    public static class TestBadImpl7 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    @ImplementedBy(TestBadImpl8.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno8 {
    }

    public static class TestBadImpl8 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    public static interface TestBadIface7 {
        @TestBadAnno7
        @TestBadAnno8
        public String crash();
    }

    @Test
    public void testBadImpl7() {
        var c = XSupplier.ImplementationFailedException.class;
        var msg = "Too many implementations by annotations on: TestBadIface7/String TestBadIface7.crash()";
        var ex = Assertions.assertThrows(c, () -> AnnotationsImplementor.implement(TestBadIface7.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(ConstructionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals(TestBadIface7.class, ((ConstructionException) ex.getCause()).getRoot()),
                () -> Assertions.assertEquals(msg, ex.getCause().getMessage())
        );
    }

    @Test
    public void testSingleImplEqualsHashCodeToString() {
        var a = AnnotationsImplementor.implement(TestIface1.class);
        var b = AnnotationsImplementor.implement(TestIface1.class);
        var c = AnnotationsImplementor.implement(TestIface2.class);
        var ha = a.hashCode();
        var hb = b.hashCode();
        var hc = c.hashCode();
        Assertions.assertAll(
                () -> Assertions.assertNotEquals(a, b),
                () -> Assertions.assertNotEquals(a, c),
                () -> Assertions.assertNotEquals(b, c),
                () -> Assertions.assertNotEquals(a, null),
                () -> Assertions.assertNotEquals(b, null),
                () -> Assertions.assertNotEquals(c, null),
                () -> Assertions.assertEquals(a, a),
                () -> Assertions.assertEquals(b, b),
                () -> Assertions.assertEquals(c, c),
                () -> Assertions.assertEquals("impl[" + TestIface1.class.getName() + "]-" + ha, a.toString()),
                () -> Assertions.assertEquals("impl[" + TestIface1.class.getName() + "]-" + hb, b.toString()),
                () -> Assertions.assertEquals("impl[" + TestIface2.class.getName() + "]-" + hc, c.toString())
        );
    }

    @Test
    @SuppressWarnings("null")
    public void testNulls() {
        Assertions.assertAll(
                () -> ForTests.testNull("iface", () -> AnnotationsImplementor.implement(null), "simple-null"),
                () -> ForTests.testNull("iface", () -> AnnotationsImplementor.implement(null, null), "double-null"),
                () -> ForTests.testNull("iface", () -> AnnotationsImplementor.implement(null, PropertyBag.root()), "simple-null-2"),
                () -> ForTests.testNull("cause", () -> new XSupplier.ImplementationFailedException(null), "ex")
        );
    }

    @Test
    public void testEx() {
        var a = new Exception();
        Assertions.assertEquals(a, new XSupplier.ImplementationFailedException(a).getCause());
    }

    @Test
    public void testNoInstance() {
        ForTests.testNonInstantiable(AnnotationsImplementor.class);
    }

    @ImplementedBy(TestImplKeys1.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnnoKeys1 {
    }

    public static interface TestIfaceKeys1 {
        @TestAnnoKeys1
        public String foo();
    }

    private static record TestKey(String a, int b) implements KeyProperty<String> {
        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }

    private static final TestKey KEY_A = new TestKey("x", 5);

    private static final TestKey KEY_B = new TestKey("y", 6);

    public static class TestImplKeys1 implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestIfaceKeys1.class, iface),
                    () -> Assertions.assertEquals(TestIfaceKeys1.class.getMethod("foo"), m),
                    () -> Assertions.assertEquals("a", props.get(KEY_A)),
                    () -> Assertions.assertEquals("b", props.get(KEY_B))
            );
            return (e, a) -> {
                Assertions.assertAll(
                        () -> Assertions.assertTrue(iface.isInstance(e)),
                        () -> Assertions.assertEquals(0, a.length)
                );
                return "foo-test";
            };
        }
    }

    @Test
    public void testKeys() {
        var props = PropertyBag.root().add(KEY_A, "a").add(KEY_B, "b");
        Assertions.assertEquals("foo-test", AnnotationsImplementor.implement(TestIfaceKeys1.class, props).foo());
    }
}
