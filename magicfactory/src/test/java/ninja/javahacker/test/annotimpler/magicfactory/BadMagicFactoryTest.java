package ninja.javahacker.test.annotimpler.magicfactory;

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
    public void testBadMagicNull() throws ConstructionException {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, () -> MagicFactory.of(null));
        Assertions.assertEquals("klass is marked non-null but is null", ex.getMessage());
    }

    @Test
    public void testBadMagicPrivateConstructor() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample1.class));
        Assertions.assertEquals(BadExample1.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample1() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample1 {
        private BadExample1() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicProtectedConstructor() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample2.class));
        Assertions.assertEquals(BadExample2.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample2() can't have @Creator, it isn't public.", ex.getMessage());
    }

    public static class BadExample2 {
        protected BadExample2() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicPackageConstructor() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample3.class));
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
    public void testBadMagicConstructorAbstract() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample4.class));
        Assertions.assertEquals(BadExample4.class, ex.getRoot());
        Assertions.assertEquals("The constructor BadExample4() can't be a creator, the class is abstract.", ex.getMessage());
    }

    public static abstract class BadExample4 {
        public BadExample4() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicConstructorInterface() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample5.class));
        Assertions.assertEquals(BadExample5.class, ex.getRoot());
        Assertions.assertEquals("Failed to determine how to create an instance of BadExample5.", ex.getMessage());
    }

    public static interface BadExample5 {
    }

    @Test
    public void testBadMagicAmbiguousEnum() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample6.class));
        Assertions.assertEquals(BadExample6.class, ex.getRoot());
        Assertions.assertEquals("No preferred enum value for class BadExample6.", ex.getMessage());
    }

    public static enum BadExample6 {
        NOT_ME, NOR_ME;
    }

    @Test
    public void testBadMagicAmbiguousConstructor() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample7.class));
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
    public void testBadMagicConstructorCreatorAbstract() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample8.class));
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
    public void testBadMagicConstructorMultipleCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample9.class));
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
    public void testBadMagicEnumMultipleCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample10.class));
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
    public void testBadMagicMethodMultipleCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample11.class));
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
    public void testBadMagicRecordConstructorMultipleCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample12.class));
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
    public void testBadMagicEnumMultipleMixedCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample13.class));
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
    public void testBadMagicRecordMultipleMixedCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample14.class));
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
    public void testBadMagicIncompatibleCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample15.class));
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
    public void testBadMagicExceptionConstructorCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample16.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create());
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
    public void testBadMagicExceptionMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample17.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create());
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
    public void testBadMagicExceptionRecordConstructorCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample18.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create());
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
    public void testBadMagicExceptionRecordMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample19.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create());
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
    public void testBadMagicInstanceCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample20.class));
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
    public void testBadMagicPrivateCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample21.class));
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
    public void testBadMagicProtectedCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample22.class));
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
    public void testBadMagicPackageCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample23.class));
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
    public void testBadMagicNullCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample24.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create());
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
    public void testBadMagicParameterMethodCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample25.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create("x"));
        Assertions.assertEquals(BadExample25.class, ex.getRoot());
        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        Assertions.assertEquals("Creator of BadExample25 doesn't work.", ex.getMessage());
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

    @Test
    public void testBadMagicParameterConstructorCreator() throws ConstructionException {
        var magic = MagicFactory.of(BadExample26.class);
        var ex = Assertions.assertThrows(ConstructionException.class, () -> magic.create("x"));
        Assertions.assertEquals(BadExample26.class, ex.getRoot());
        Assertions.assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        Assertions.assertEquals("Creator of BadExample26 doesn't work.", ex.getMessage());
    }

    public static class BadExample26 {
        public BadExample26(int x) {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicWildcardMethodCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample27.class));
        Assertions.assertEquals(BadExample27.class, ex.getRoot());
        Assertions.assertEquals("Bad type for method <E> E BadExample27.foo(). Should be BadExample27.", ex.getMessage());
    }

    public static class BadExample27 {
        public BadExample27() {
            throw new AssertionError();
        }

        @Creator
        public static <E> E foo() {
            throw new AssertionError();
        }
    }

    @Test
    public void testBadMagicGenericArrayMethodCreator() throws ConstructionException {
        var ex = Assertions.assertThrows(ConstructionException.class, () -> MagicFactory.of(BadExample28.class));
        Assertions.assertEquals(BadExample28.class, ex.getRoot());
        Assertions.assertEquals("Bad type for method <E> E[] BadExample28.foo(). Should be BadExample28.", ex.getMessage());
    }

    public static class BadExample28 {
        public BadExample28() {
            throw new AssertionError();
        }

        @Creator
        public static <E> E[] foo() {
            throw new AssertionError();
        }
    }
}
