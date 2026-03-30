package ninja.javahacker.test.annotimpler.magicfactory;

import org.junit.jupiter.api.function.Executable;
import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.magicfactory;
import module org.junit.jupiter.api;

@SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes", "AccessingNonPublicFieldOfAnotherObject"})
public class BadMagicFactoryTest {

    public static class LameException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public LameException() {
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testBadMagicOfNull() throws Exception {
        ForTests.testNull("klass", () -> MagicFactory.of(null));
    }

    @Test
    public void testBadMagicCreateNull() throws Exception {
        var magic = MagicFactory.of(BadExample0.class);
        ForTests.testNull("args", () -> magic.create((Object[]) null));
    }

    public static class BadExample0 {
        public BadExample0() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicPrivateConstructor() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample1.class));
        Assertions.assertEquals(BadExample1.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample1() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample1 {
        private BadExample1() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicProtectedConstructor() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample2.class));
        Assertions.assertEquals(BadExample2.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample2() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample2 {
        protected BadExample2() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicPackageConstructor() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample3.class));
        Assertions.assertEquals(BadExample3.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample3() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample3 {
        @PackagePrivate
        BadExample3() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicConstructorAbstract() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample4.class));
        Assertions.assertEquals(BadExample4.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample4() can't be a creator, the class is abstract.", ex.getMessage());
    }

    public static abstract class BadExample4 {
        public BadExample4() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicConstructorInterface() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample5.class));
        Assertions.assertEquals(BadExample5.class, ex.getRoot());
        Assertions.assertEquals("Failed to determine how to create an instance of BadExample5.", ex.getMessage());
    }

    public static interface BadExample5 {
    }

    @Test
    public void testBadMagicAmbiguousEnum() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample6.class));
        Assertions.assertEquals(BadExample6.class, ex.getRoot());
        Assertions.assertEquals("No preferred enum value for class BadExample6.", ex.getMessage());
    }

    public static enum BadExample6 {
        NOT_ME, NOR_ME;
    }

    @Test
    public void testBadMagicAmbiguousConstructor() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample7.class));
        Assertions.assertEquals(BadExample7.class, ex.getRoot());
        Assertions.assertEquals("Failed to determine how to create an instance of BadExample7.", ex.getMessage());
    }

    public static class BadExample7 {
        public BadExample7(int foo) {
            throw new AssertionError();
        }

        public BadExample7(String foo) {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicConstructorCreatorAbstract() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample8.class));
        Assertions.assertEquals(BadExample8.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample8() can't have @Creator, the class is abstract.", ex.getMessage());
    }

    public static abstract class BadExample8 {
        @Creator
        public BadExample8() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicConstructorMultipleCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample9.class));
        Assertions.assertEquals(BadExample9.class, ex.getRoot());
        Assertions.assertEquals("Can't have @Creator more than once in class BadExample9.", ex.getMessage());
    }

    public static class BadExample9 {
        @Creator
        public BadExample9(int x) {
            throw new AssertionError();
        }

        @Creator
        public BadExample9(String x) {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicEnumMultipleCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample10.class));
        Assertions.assertEquals(BadExample10.class, ex.getRoot());
        Assertions.assertEquals("Can't have @Creator more than once in class BadExample10.", ex.getMessage());
    }

    public static enum BadExample10 {
        @Creator
        FOO,

        @Creator
        BAR;
    }

    @Test
    public void testBadMagicMethodMultipleCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample11.class));
        Assertions.assertEquals(BadExample11.class, ex.getRoot());
        Assertions.assertEquals("Can't have @Creator more than once in class BadExample11.", ex.getMessage());
    }

    public static class BadExample11 {
        private BadExample11() {
            throw new AssertionError();
        }

        @Creator
        public static BadExample11 foo(int x) {
            throw new AssertionError();
        }

        @Creator
        public static BadExample11 bar(String x) {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicRecordConstructorMultipleCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample12.class));
        Assertions.assertEquals(BadExample12.class, ex.getRoot());
        Assertions.assertEquals("Can't have @Creator more than once in class BadExample12.", ex.getMessage());
    }

    public static record BadExample12(int a, String b) {
        @Creator
        public BadExample12(int x) {
            this(5, "");
            throw new AssertionError();
        }

        @Creator
        public BadExample12(String x) {
            this(6, "");
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicEnumMultipleMixedCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample13.class));
        Assertions.assertEquals(BadExample13.class, ex.getRoot());
        Assertions.assertEquals("Can't have @Creator more than once in class BadExample13.", ex.getMessage());
    }

    public static enum BadExample13 {
        @Creator
        FOO(5);

        private BadExample13(int x) {
        }

        @Creator
        private BadExample13(String x) {
            throw new AssertionError();
        }

        @Creator
        public static BadExample13 foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicRecordMultipleMixedCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample14.class));
        Assertions.assertEquals(BadExample14.class, ex.getRoot());
        Assertions.assertEquals("Can't have @Creator more than once in class BadExample14.", ex.getMessage());
    }

    public static record BadExample14(int a, String b) {
        @Creator
        public BadExample14(String x) {
            this(7, "m");
            throw new AssertionError();
        }

        @Creator
        public static BadExample14 foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicIncompatibleCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample15.class));
        Assertions.assertEquals(BadExample15.class, ex.getRoot());
        Assertions.assertEquals("Bad type for method String BadExample15.foo(). Should be BadExample15.", ex.getMessage());
    }

    public static class BadExample15 {
        private BadExample15() {
            throw new AssertionError();
        }

        @Creator
        public static String foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicExceptionConstructorCreator() throws Exception {
        var magic = MagicFactory.of(BadExample16.class);
        var ex = Assertions.assertThrows(MagicFactory.CreationException.class, () -> magic.create());
        Assertions.assertEquals(BadExample16.class, ex.getRoot());
        Assertions.assertEquals(LameException.class, ex.getCause().getClass());
        Assertions.assertEquals("The instantiation of BadExample16 threw an exception.", ex.getMessage());
    }

    public static class BadExample16 {
        public BadExample16() {
            throw new LameException();
        }
    }

    @Test
    public void testBadMagicExceptionMethodCreator() throws Exception {
        var magic = MagicFactory.of(BadExample17.class);
        var ex = Assertions.assertThrows(MagicFactory.CreationException.class, () -> magic.create());
        Assertions.assertEquals(BadExample17.class, ex.getRoot());
        Assertions.assertEquals(LameException.class, ex.getCause().getClass());
        Assertions.assertEquals("The instantiation of BadExample17 threw an exception.", ex.getMessage());
    }

    public static class BadExample17 {
        public BadExample17() {
            throw new AssertionError();
        }

        @Creator
        public static BadExample17 foo() {
            throw new LameException();
        }
    }

    @Test
    public void testBadMagicExceptionRecordConstructorCreator() throws Exception {
        var magic = MagicFactory.of(BadExample18.class);
        var ex = Assertions.assertThrows(MagicFactory.CreationException.class, () -> magic.create());
        Assertions.assertEquals(BadExample18.class, ex.getRoot());
        Assertions.assertEquals(LameException.class, ex.getCause().getClass());
        Assertions.assertEquals("The instantiation of BadExample18 threw an exception.", ex.getMessage());
    }

    public static record BadExample18(String a, float b) {
        @Creator
        public BadExample18() {
            this("u", 9.0f);
            throw new LameException();
        }
    }

    @Test
    public void testBadMagicExceptionRecordMethodCreator() throws Exception {
        var magic = MagicFactory.of(BadExample19.class);
        var ex = Assertions.assertThrows(MagicFactory.CreationException.class, () -> magic.create());
        Assertions.assertEquals(BadExample19.class, ex.getRoot());
        Assertions.assertEquals(LameException.class, ex.getCause().getClass());
        Assertions.assertEquals("The instantiation of BadExample19 threw an exception.", ex.getMessage());
    }

    public static record BadExample19(String a, float b) {
        public BadExample19() {
            this("u", 9.0f);
            throw new AssertionError();
        }

        @Creator
        public static BadExample19 foo() {
            throw new LameException();
        }
    }

    @Test
    public void testBadMagicInstanceCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample20.class));
        Assertions.assertEquals(BadExample20.class, ex.getRoot());
        Assertions.assertEquals("Instance method BadExample20 BadExample20.foo() can't have @Creator.", ex.getMessage());
    }

    public static class BadExample20 {
        private BadExample20() {
            throw new AssertionError();
        }

        @Creator
        public BadExample20 foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicPrivateCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample21.class));
        Assertions.assertEquals(BadExample21.class, ex.getRoot());
        Assertions.assertEquals("The method BadExample21 BadExample21.foo() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample21 {
        private BadExample21() {
            throw new AssertionError();
        }

        @Creator
        private static BadExample21 foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicProtectedCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample22.class));
        Assertions.assertEquals(BadExample22.class, ex.getRoot());
        Assertions.assertEquals("The method BadExample22 BadExample22.foo() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample22 {
        private BadExample22() {
            throw new AssertionError();
        }

        @Creator
        protected static BadExample22 foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicPackageCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample23.class));
        Assertions.assertEquals(BadExample23.class, ex.getRoot());
        Assertions.assertEquals("The method BadExample23 BadExample23.foo() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample23 {
        private BadExample23() {
            throw new AssertionError();
        }

        @Creator
        @PackagePrivate
        static BadExample23 foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicNullCreator() throws Exception {
        var magic = MagicFactory.of(BadExample24.class);
        var ex = Assertions.assertThrows(MagicFactory.CreationException.class, () -> magic.create());
        Assertions.assertEquals(BadExample24.class, ex.getRoot());
        Assertions.assertEquals("Creator of BadExample24 produced null.", ex.getMessage());
    }

    public static class BadExample24 {
        private BadExample24() {
            throw new AssertionError();
        }

        @Creator
        public static BadExample24 foo() {
            return null;
        }
    }

    @Test
    public void testBadMagicParameterMethodCreator() throws Exception {
        var magic = MagicFactory.of(BadExample25.class);
        var ex = Assertions.assertThrows(MagicFactory.CreationException.class, () -> magic.create("x"));
        Assertions.assertEquals(BadExample25.class, ex.getRoot());
        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        Assertions.assertEquals("Creator of BadExample25 was called with the wrong arguments.", ex.getMessage());
    }

    @TestFactory
    public Stream<DynamicTest> testBadArgsCreator() throws Exception {
        var magic25 = MagicFactory.of(BadExample25.class);
        var magic26 = MagicFactory.of(BadExample26.class);
        var magic27 = MagicFactory.of(BadExample27.class);
        var magic28 = MagicFactory.of(BadExample28.class);
        var magic29 = MagicFactory.of(BadExample29.class);

        record Named(String name, Executable exec, Class<?> c) {
        }

        var execs = Stream.of(
                new Named("25-arity-0", () -> magic25.create(         ), BadExample25.class),
                new Named("25-types"  , () -> magic25.create("x"      ), BadExample25.class),
                new Named("25-arity-2", () -> magic25.create(5, 7     ), BadExample25.class),
                new Named("26-arity-0", () -> magic26.create(         ), BadExample26.class),
                new Named("26-types"  , () -> magic26.create("x"      ), BadExample26.class),
                new Named("26-arity-2", () -> magic26.create(5, 7     ), BadExample26.class),
                new Named("27-arity-1", () -> magic27.create("x"      ), BadExample27.class),
                new Named("28-arity-0", () -> magic28.create(         ), BadExample28.class),
                new Named("28-arity-1", () -> magic28.create("x"      ), BadExample28.class),
                new Named("28-types"  , () -> magic28.create(5, "x"   ), BadExample28.class),
                new Named("28-arity-3", () -> magic28.create("x", 5, 7), BadExample28.class),
                new Named("29-arity-0", () -> magic29.create(         ), BadExample29.class),
                new Named("29-types"  , () -> magic29.create("x"      ), BadExample29.class),
                new Named("29-arity-2", () -> magic29.create(5, 7     ), BadExample29.class)
        );
        return execs.map(ex -> {
                Executable x2 = () -> {
                    var err = Assertions.assertThrows(MagicFactory.CreationException.class, ex.exec);
                    Assertions.assertEquals(ex.c, err.getRoot());
                    Assertions.assertEquals("Creator of " + ex.c.getSimpleName() + " was called with the wrong arguments.", err.getMessage());
                    Assertions.assertEquals(IllegalArgumentException.class, err.getCause().getClass());
                };
                return DynamicTest.dynamicTest(ex.name, x2);
        });
    }

    public static class BadExample25 {
        public BadExample25() {
            throw new AssertionError();
        }

        @Creator
        public static BadExample25 foo(int x) {
            throw new AssertionError();
        }
    }

    public static class BadExample26 {
        public BadExample26(int x) {
            throw new AssertionError();
        }
    }

    public static class BadExample27 {
        public BadExample27() {
            throw new AssertionError();
        }
    }

    public static class BadExample28 {
        public BadExample28(String x, int y) {
            throw new AssertionError();
        }
    }

    public static class BadExample29 {
        public BadExample29(String x) {
            throw new AssertionError();
        }

        @Creator
        public BadExample29(int y) {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicWildcardMethodCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample30.class));
        Assertions.assertEquals(BadExample30.class, ex.getRoot());
        Assertions.assertEquals("Bad type for method <E> E BadExample30.foo(). Should be BadExample30.", ex.getMessage());
    }

    public static class BadExample30 {
        public BadExample30() {
            throw new AssertionError();
        }

        @Creator
        public static <E> E foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicGenericArrayMethodCreator() throws Exception {
        var ex = Assertions.assertThrows(MagicFactory.CreatorSelectionException.class, () -> MagicFactory.of(BadExample31.class));
        Assertions.assertEquals(BadExample31.class, ex.getRoot());
        Assertions.assertEquals("Bad type for method <E> E[] BadExample31.foo(). Should be BadExample31.", ex.getMessage());
    }

    public static class BadExample31 {
        public BadExample31() {
            throw new AssertionError();
        }

        @Creator
        public static <E> E[] foo() {
            throw new AssertionError();
        }
    }
}
