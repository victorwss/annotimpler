package ninja.javahacker.test.annotimpler.core;

import lombok.NonNull;
import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.core;
import module org.junit.jupiter.api;

@SuppressWarnings({"missing-explicit-ctor", "AssertEqualsBetweenInconvertibleTypes"})
public class AnnotimplerTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

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
    public void testSingleImpl1() throws Exception {
        var impl = AnnotationsImplementor.implement(TestIface1.class);
        Assertions.assertEquals("foo-test", impl.foo());
    }

    @Test
    public void testSingleImpl2() throws Exception {
        var impl = AnnotationsImplementor.implement(TestIface1.class, null);
        Assertions.assertEquals("foo-test", impl.foo());
    }

    @Test
    public void testSingleImpl3() throws Exception {
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
    public void testParamImpl() throws Exception {
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
    public void testParamImplWtf() throws Exception {
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
                throws BadImplementationException
        {
            var iface = m.getDeclaringClass();
            Assertions.assertAll(
                    () -> Assertions.assertEquals(TestBadIface1.class, iface),
                    () -> Assertions.assertEquals(TestBadIface1.class.getMethod("crash"), m),
                    () -> Assertions.assertEquals(PropertyBag.root(), props)
            );
            throw new BadImplementationException("bad", iface);
        }
    }

    @Test
    public void testBadImpl1And2() {
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface1.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface1.class, ex.getRoot()),
                () -> Assertions.assertEquals("bad", ex.getMessage())
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
        var msg = "Implementation was null on: TestBadIface3/String TestBadIface3.crash()";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface3.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface3.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static interface TestBadIface4 {
        public String crash();
    }

    @Test
    public void testBadImpl4() {
        var msg = "Method String TestBadIface4.crash() lacks annotation-defined implementation.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface4.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface4.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
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
    public void testStdImpl6() throws Exception {
        Assertions.assertEquals(92, AnnotationsImplementor.implement(TestStdIface6.class).foo(55));
    }

    @Test
    public void testStdImpl6Override() throws Exception {
        Assertions.assertEquals("whoot", AnnotationsImplementor.implement(TestStdIface6.class).wtf());
    }

    @ImplementedBy(TestVeryBadImpl.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestVeryBadAnno {
    }

    public static class TestVeryBadImpl implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    @ImplementedBy(TestReallyBadImpl.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestReallyBadAnno {
    }

    public static class TestReallyBadImpl implements Implementation {
        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    public static interface TestBadIface7 {
        @TestVeryBadAnno
        @TestReallyBadAnno
        public String crash();
    }

    @Test
    public void testBadImpl7() {
        var msg = "Too many implementations by annotations on: TestBadIface7/String TestBadIface7.crash()";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface7.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface7.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static class TestBadImpl9 implements Implementation {
        public TestBadImpl9() throws FooException {
            throw new FooException();
        }

        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    @ImplementedBy(TestBadImpl9.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno9 {
    }

    public static interface TestBadIface9 {
        @TestBadAnno9
        public String crash();
    }

    @Test
    public void testBadImpl9() {
        var msg = "The instantiation of TestBadImpl9 threw an exception.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface9.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface9.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage()),
                () -> Assertions.assertEquals(MagicFactory.CreationException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals(msg, ex.getCause().getMessage()),
                () -> Assertions.assertEquals(FooException.class, ex.getCause().getCause().getClass())
        );
    }

    public static class TestBadImpl10 implements Implementation {
        public TestBadImpl10(String foo, int bar) throws FooException {
            throw new AssertionError();
        }

        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    @ImplementedBy(TestBadImpl10.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno10 {
    }

    public static interface TestBadIface10 {
        @TestBadAnno10
        public String crash();
    }

    @Test
    public void testBadImpl10() {
        var msg = "Don't know how to build TestBadImpl10 with no arguments.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface10.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface10.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static class TestBadImpl11 implements Implementation {
        public TestBadImpl11(String foo, int bar) throws FooException {
            throw new AssertionError();
        }

        public TestBadImpl11(String foo) throws FooException {
            throw new AssertionError();
        }

        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    @ImplementedBy(TestBadImpl11.class)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestBadAnno11 {
    }

    public static interface TestBadIface11 {
        @TestBadAnno11
        public String crash();
    }

    @Test
    public void testBadImpl11() {
        var msg = "Failed to determine how to create an instance of TestBadImpl11.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface11.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface11.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage()),
                () -> Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, ex.getCause().getClass()),
                () -> Assertions.assertEquals(msg, ex.getCause().getMessage())
        );
    }

    public static class TestBadImpl12 implements Implementation {
        public TestBadImpl12(String foo, int bar) throws FooException {
            throw new AssertionError();
        }

        @NonNull
        @Override
        public <E> CallContext<E> prepare(@NonNull Method m, @NonNull PropertyBag props) {
            throw new AssertionError();
        }
    }

    public static interface TestBadIface12 {
        @TestVeryBadAnno
        public static String crash() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadImpl12() {
        var msg = "Can't use @TestVeryBadAnno annotation on static methods.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface12.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface12.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static interface TestBadIface13 {
        @TestVeryBadAnno
        private String crash() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadImpl13() {
        var msg = "Can't use @TestVeryBadAnno annotation on private methods.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface13.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface13.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static interface TestBadIface14 {
        @Override
        @TestVeryBadAnno
        public String toString();
    }

    @Test
    public void testBadImpl14() {
        var msg = "Can't use @TestVeryBadAnno annotation on toString() method.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface14.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface14.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static interface TestBadIface15 {
        @Override
        @TestVeryBadAnno
        public int hashCode();
    }

    @Test
    public void testBadImpl15() {
        var msg = "Can't use @TestVeryBadAnno annotation on hashCode() method.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface15.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface15.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static interface TestBadIface16 {
        @Override
        @TestVeryBadAnno
        public boolean equals(Object x);
    }

    @Test
    public void testBadImpl16() {
        var msg = "Can't use @TestVeryBadAnno annotation on equals(Object) method.";
        var ex = Assertions.assertThrows(BadImplementationException.class, () -> AnnotationsImplementor.implement(TestBadIface16.class));
        Assertions.assertAll(
                () -> Assertions.assertEquals(TestBadIface16.class, ex.getRoot()),
                () -> Assertions.assertEquals(msg, ex.getMessage())
        );
    }

    public static interface TestSilly1 {
    }

    @Test
    public void testSilly1() throws Exception {
        AnnotationsImplementor.implement(TestSilly1.class);
    }

    public static interface TestSilly2 {
        private void foo1() {
            throw new AssertionError();
        }

        public static void foo2() {
            throw new AssertionError();
        }

        private static void foo3() {
            throw new AssertionError();
        }

        @Override
        public int hashCode();

        @Override
        public String toString();

        @Override
        public boolean equals(Object other);
    }

    @Test
    public void testSilly2() throws Exception {
        AnnotationsImplementor.implement(TestSilly2.class);
    }

    @TestFactory
    public Stream<DynamicTest> testSingleImplEqualsHashCodeToString() throws Exception {
        var a = AnnotationsImplementor.implement(TestIface1.class);
        var b = AnnotationsImplementor.implement(TestIface1.class);
        var c = AnnotationsImplementor.implement(TestIface2.class);
        var ha = a.hashCode();
        var hb = b.hashCode();
        var hc = c.hashCode();
        return Stream.of(
                n("a-a", () -> Assertions.assertEquals(a, a)),
                n("b-b", () -> Assertions.assertEquals(b, b)),
                n("c-c", () -> Assertions.assertEquals(c, c)),
                n("a-b", () -> Assertions.assertNotEquals(a, b)),
                n("a-c", () -> Assertions.assertNotEquals(a, c)),
                n("b-c", () -> Assertions.assertNotEquals(b, c)),
                n("a-n", () -> Assertions.assertNotEquals(a, null)),
                n("b-n", () -> Assertions.assertNotEquals(b, null)),
                n("c-n", () -> Assertions.assertNotEquals(c, null)),
                n("a-x", () -> Assertions.assertNotEquals(a, "x")),
                n("b-x", () -> Assertions.assertNotEquals(b, "x")),
                n("c-x", () -> Assertions.assertNotEquals(c, "x")),
                n("a-s", () -> Assertions.assertEquals("impl[" + TestIface1.class.getName() + "]-" + ha, a.toString())),
                n("b-s", () -> Assertions.assertEquals("impl[" + TestIface1.class.getName() + "]-" + hb, b.toString())),
                n("c-s", () -> Assertions.assertEquals("impl[" + TestIface2.class.getName() + "]-" + hc, c.toString()))
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                n("simple-null", () -> ForTests.testNull("iface", () -> AnnotationsImplementor.implement(null))),
                n("double-null", () -> ForTests.testNull("iface", () -> AnnotationsImplementor.implement(null, null))),
                n("simple-null-2", () -> ForTests.testNull("iface", () -> AnnotationsImplementor.implement(null, PropertyBag.root())))
        );
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
    public void testKeys() throws Exception {
        var props = PropertyBag.root().add(KEY_A, "a").add(KEY_B, "b");
        Assertions.assertEquals("foo-test", AnnotationsImplementor.implement(TestIfaceKeys1.class, props).foo());
    }
}
