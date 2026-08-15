package ninja.test.javahacker.typeser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import ninja.javahacker.typeser.TypeRef;
import ninja.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.api;

@SuppressWarnings({"unused", "unchecked"})
public class TypeserTest {

    // ── Helper types for non-null owner-type test ────────────────────────────

    private static class Outer<T> {
        class Inner<U> {}
    }

    // ── Sample fields — never instantiated, only used for reflection ──────────

    @SuppressWarnings("unused")
    private static final class Sample {
        private String someClass;
        private List<String> list;
        private List<Integer> integerList;
        private List<? extends Number> upper;
        private List<? super Integer> lower;
        private List<?> any;
        private List<String>[] genericArray;
        private List<Integer>[] integerGenericArray;
        private Map<String, List<Integer>> nested;
        private Map.Entry<String, Integer> entry;
        private Outer<String>.Inner<Integer> inner;
        private Outer<Integer>.Inner<Integer> inner2;
    }

    // ── Round-trip helper (tests TypeRef.write + TypeRef.read) ───────────────

    private static Type roundTrip(Type type) throws Exception {
        return readFromBytes(writeToBytes(type));
    }

    private static byte[] writeToBytes(Type type) throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            TypeRef.write(oos, type);
        }
        return baos.toByteArray();
    }

    private static Type readFromBytes(byte[] content) throws Exception {
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(content))) {
            return TypeRef.read(ois);
        }
    }

    private static TypeRef roundTripRef(Type type) throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(TypeRef.wrap(type));
        }
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            return (TypeRef) ois.readObject();
        }
    }

    /// Returns the actual `WildcardType` used as the sole type argument of the `List<...>`-typed `Sample` field
    /// named `fieldName` (e.g. `"upper"`, `"lower"`, `"any"`). The field's own `getGenericType()` is the
    /// enclosing `ParameterizedType` (`List<...>`), not the wildcard itself, so the wildcard must be extracted
    /// from its actual type arguments.
    private static WildcardType wildcardArgOf(String fieldName) throws Exception {
        var listType = (ParameterizedType) Sample.class.getDeclaredField(fieldName).getGenericType();
        return (WildcardType) listType.getActualTypeArguments()[0];
    }

    private static byte[] replaceAsciiOnce(byte[] source, String before, String after) {
        Assertions.assertEquals(before.length(), after.length(), "Replacement strings must have same length.");
        var from = before.getBytes(StandardCharsets.UTF_8);
        var to = after.getBytes(StandardCharsets.UTF_8);
        var out = source.clone();
        int hits = 0;
        for (int i = 0; i <= out.length - from.length; i++) {
            boolean match = true;
            for (int j = 0; j < from.length; j++) {
                if (out[i + j] != from[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                System.arraycopy(to, 0, out, i, to.length);
                hits++;
                i += from.length - 1;
            }
        }
        Assertions.assertEquals(1, hits, "Expected exactly one serialized occurrence of \"" + before + "\".");
        return out;
    }

    private static Type roundTrip(Type type, String before, String after) throws Exception {
        var bytes = writeToBytes(type);
        var changed = replaceAsciiOnce(bytes, before, after);
        return readFromBytes(changed);
    }

    // ── Tests: serialisation round-trip ─────────────────────────────────────

    @TestFactory
    public Stream<DynamicTest> testSerials() throws Exception {
        var pf = "[testSerials] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    var copy = roundTrip(original);
                    Assertions.assertEquals(original, copy);
                }));
    }

    // ── Tests: TypeRef.wrap() and type() without serialisation ───────────────

    @TestFactory
    public Stream<DynamicTest> testWrap() throws Exception {
        var pf = "[testWrap] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    Assertions.assertEquals(original, TypeRef.wrap(original).type());
                }));
    }

    // ── Tests: toString()/getTypeName() of the reconstructed Type itself ──────

    /// Verifies that, after an actual serialization round-trip, `toString()` and `getTypeName()` invoked directly
    /// on the reconstructed `Type` (not on the wrapping [TypeRef]) match what the original, genuinely-reflected
    /// `Type` produces. This exercises the synthetic `ParameterizedType`/`WildcardType`/`GenericArrayType`
    /// implementations created by `SerializableType.toType()`, since those are the objects whose `toString()`
    /// would otherwise silently fall back to `Object`'s identity-based default.
    @TestFactory
    public Stream<DynamicTest> testReconstructedTypeStringMethods() throws Exception {
        var pf = "[testReconstructedTypeStringMethods] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    var copy = roundTrip(original);
                    Assertions.assertEquals(original.toString(), copy.toString(), field.getName());
                    Assertions.assertEquals(original.getTypeName(), copy.getTypeName(), field.getName());
                }));
    }

    // ── Tests: equals()/hashCode() of the reconstructed Type itself ───────────

    /// Verifies that, after an actual serialization round-trip, `equals()` and `hashCode()` invoked directly on
    /// the reconstructed `Type` (not on the wrapping [TypeRef] nor on the internal `SerializableType` surrogate)
    /// behave consistently with the original, genuinely-reflected `Type`, in both directions.
    ///
    /// Checking only `original.equals(copy)` would not be enough: the real JDK `Type` implementations happen to
    /// compare structurally against any object implementing the right interface, so that direction alone could
    /// pass even if the reconstructed `Type`'s own `equals()`/`hashCode()` were still the default, identity-based
    /// ones inherited from `Object` (which was, in fact, a real bug: the synthetic `ParameterizedType`,
    /// `WildcardType` and `GenericArrayType` returned by `SerializableType.toType()` did not override `equals()`
    /// nor `hashCode()` at all). Hence both `copy.equals(original)` and matching `hashCode()`s must be checked too.
    @TestFactory
    public Stream<DynamicTest> testReconstructedTypeEqualsAndHashCode() throws Exception {
        var pf = "[testReconstructedTypeEqualsAndHashCode] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    var copy = roundTrip(original);
                    Assertions.assertEquals(original, copy, field.getName());
                    Assertions.assertEquals(copy, original, field.getName());
                    Assertions.assertEquals(original.hashCode(), copy.hashCode(), field.getName());
                }));
    }

    /// Verifies that two independently-reconstructed copies of the same type are mutually equal (and share a
    /// hash code) even when neither of them is the original, genuinely-reflected `Type`. This rules out any
    /// implementation that would only work by accident whenever a genuinely-reflected `Type` happens to be one
    /// side of the comparison.
    @TestFactory
    public Stream<DynamicTest> testReconstructedTypeEqualsBetweenTwoCopies() throws Exception {
        var pf = "[testReconstructedTypeEqualsBetweenTwoCopies] ";
        return Stream.of(Sample.class.getDeclaredFields())
                .map(field -> DynamicTest.dynamicTest(pf + field.getName(), () -> {
                    var original = field.getGenericType();
                    var copy1 = roundTrip(original);
                    var copy2 = roundTrip(original);
                    Assertions.assertEquals(copy1, copy2, field.getName());
                    Assertions.assertEquals(copy2, copy1, field.getName());
                    Assertions.assertEquals(copy1.hashCode(), copy2.hashCode(), field.getName());
                }));
    }

    @Test
    public void testNestedOwnerTypeToString() throws Exception {
        var inner = Sample.class.getDeclaredField("inner").getGenericType();
        var copy = roundTrip(inner);
        Assertions.assertEquals(
                "ninja.test.javahacker.typeser.TypeserTest$Outer<java.lang.String>$Inner<java.lang.Integer>",
                inner.toString()
        );
        Assertions.assertEquals(inner.toString(), copy.toString());
        Assertions.assertEquals(inner.getTypeName(), copy.getTypeName());
    }

    @Test
    public void testWildcardAndGenericArrayToStringAfterRoundTrip() throws Exception {
        var upper = Sample.class.getDeclaredField("upper").getGenericType();
        var lower = Sample.class.getDeclaredField("lower").getGenericType();
        var any = Sample.class.getDeclaredField("any").getGenericType();
        var array = Sample.class.getDeclaredField("genericArray").getGenericType();

        Assertions.assertEquals("java.util.List<? extends java.lang.Number>", upper.toString());
        Assertions.assertEquals("java.util.List<? super java.lang.Integer>", lower.toString());
        Assertions.assertEquals("java.util.List<?>", any.toString());
        Assertions.assertEquals("java.util.List<java.lang.String>[]", array.toString());

        for (var type : List.of(upper, lower, any, array)) {
            var copy = roundTrip(type);
            Assertions.assertEquals(type.toString(), copy.toString());
            Assertions.assertEquals(type.getTypeName(), copy.getTypeName());
        }
    }

    @Test
    public void testTypeRefObjectMethods() throws Exception {
        var stringType = Sample.class.getDeclaredField("someClass").getGenericType();
        var listType = Sample.class.getDeclaredField("list").getGenericType();
        var ref = TypeRef.wrap(stringType);
        var equalRef = TypeRef.wrap(stringType);
        var deserializedRef = roundTripRef(listType);

        Assertions.assertEquals(ref, equalRef);
        Assertions.assertEquals(ref.hashCode(), equalRef.hashCode());
        Assertions.assertNotEquals(ref, TypeRef.wrap(listType));
        Assertions.assertNotEquals(ref, stringType);
        Assertions.assertNotEquals(ref, null);
        Assertions.assertEquals(listType, deserializedRef.type());
        Assertions.assertEquals(TypeRef.wrap(listType), deserializedRef);
        Assertions.assertEquals(TypeRef.wrap(listType).hashCode(), deserializedRef.hashCode());
        Assertions.assertEquals(stringType.toString(), ref.toString());
        Assertions.assertEquals(stringType.getTypeName(), ref.getTypeName());
        Assertions.assertEquals(listType.toString(), deserializedRef.toString());
        Assertions.assertEquals(listType.getTypeName(), deserializedRef.getTypeName());
    }

    @Test
    public void testTypeRefObjectMethodsForFields() throws Exception {
        for (var fieldName : List.of("upper", "lower", "any", "genericArray", "nested", "entry", "inner")) {
            var type = Sample.class.getDeclaredField(fieldName).getGenericType();
            var original = TypeRef.wrap(type);
            var copy = roundTripRef(type);
            Assertions.assertEquals(original, copy, fieldName);
            Assertions.assertEquals(original.hashCode(), copy.hashCode(), fieldName);
            Assertions.assertEquals(original.toString(), copy.toString(), fieldName);
            Assertions.assertEquals(original.getTypeName(), copy.getTypeName(), fieldName);
        }
    }

    @Test
    public void testTypeRefObjectMethodsForTypeParameters() throws Exception {
        var typeVariable = List.class.getTypeParameters()[0];
        var originalVariable = TypeRef.wrap(typeVariable);
        var copiedVariable = roundTripRef(typeVariable);
        Assertions.assertEquals(originalVariable, copiedVariable);
        Assertions.assertEquals(originalVariable.hashCode(), copiedVariable.hashCode());
        Assertions.assertEquals(originalVariable.toString(), copiedVariable.toString());
        Assertions.assertEquals(originalVariable.getTypeName(), copiedVariable.getTypeName());
    }

    @Test
    public void testTypeRefObjectMethodVariantsSimpleCases() throws Exception {
        var list = TypeRef.wrap(Sample.class.getDeclaredField("list").getGenericType());
        var integerList = TypeRef.wrap(Sample.class.getDeclaredField("integerList").getGenericType());
        var upper = TypeRef.wrap(Sample.class.getDeclaredField("upper").getGenericType());
        var lower = TypeRef.wrap(Sample.class.getDeclaredField("lower").getGenericType());
        var array = TypeRef.wrap(Sample.class.getDeclaredField("genericArray").getGenericType());
        var integerArray = TypeRef.wrap(Sample.class.getDeclaredField("integerGenericArray").getGenericType());

        Assertions.assertNotEquals(list, integerList);
        Assertions.assertNotEquals(upper, lower);
        Assertions.assertNotEquals(array, integerArray);
        Assertions.assertNotEquals(list, upper);
        Assertions.assertNotEquals(upper, array);
        Assertions.assertNotEquals(array, TypeRef.wrap(String.class));
    }

    @Test
    public void testTypeRefObjectMethodVariantsBaseMethods() throws Exception {
        var methods = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("genericMethod"))
                .map(m -> m.getTypeParameters()[0])
                .toList();
        Assertions.assertNotEquals(TypeRef.wrap(methods.get(0)), TypeRef.wrap(methods.get(1)));
    }

    @Test
    public void testTypeRefObjectMethodVariantsMethodOverloads() throws Exception {
        var baseMethod = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .filter(m -> m.getParameterCount() == 1)
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        Assertions.assertEquals(TypeRef.wrap(baseMethod), roundTripRef(baseMethod));
        Assertions.assertEquals(TypeRef.wrap(baseMethod).hashCode(), roundTripRef(baseMethod).hashCode());
        var overloadedMethod = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .filter(m -> m.getParameterCount() == 2)
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        Assertions.assertNotEquals(TypeRef.wrap(baseMethod), TypeRef.wrap(overloadedMethod));
    }

    @Test
    public void testTypeRefObjectMethodVariantsGenericConstructor() throws Exception {
        var constructors = List.of(
                Stream.of(GenericConstructor.class.getDeclaredConstructors())
                        .filter(c -> c.getParameterCount() == 1)
                        .findFirst()
                        .orElseThrow()
                        .getTypeParameters()[0],
                GenericConstructor2.class.getDeclaredConstructors()[0].getTypeParameters()[0]
        );
        Assertions.assertNotEquals(TypeRef.wrap(constructors.get(0)), TypeRef.wrap(constructors.get(1)));
        Assertions.assertEquals(TypeRef.wrap(constructors.get(0)), roundTripRef(constructors.get(0)));
        Assertions.assertEquals(TypeRef.wrap(constructors.get(0)).hashCode(), roundTripRef(constructors.get(0)).hashCode());
        var overloadedConstructor = Stream.of(GenericConstructor.class.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 2)
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        Assertions.assertNotEquals(TypeRef.wrap(constructors.get(0)), TypeRef.wrap(overloadedConstructor));
    }

    @Test
    public void testTypeRefObjectMethodVariantsTypeParameters() throws Exception {
        var classVariables = List.of(List.class.getTypeParameters()[0], Map.class.getTypeParameters()[0]);
        Assertions.assertNotEquals(TypeRef.wrap(classVariables.get(0)), TypeRef.wrap(classVariables.get(1)));
        Assertions.assertEquals(
                TypeRef.wrap(classVariables.get(0)).hashCode(),
                TypeRef.wrap(classVariables.get(0)).hashCode()
        );
    }

    private static final Object NULLY = new Object();

    @SuppressWarnings("unchecked")
    private static <E> E badImplementation(Class<E> iface, Map<String, Object> working) {
        return (E) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { iface }, (i, m, a) -> {
            var what = working.get(m.getName());
            if (what == NULLY) return null;
            if (what != null) return what;
            throw new AssertionError(m.getName());
        });
    }

    @Test
    public void testMinimalParameterizedType() throws Exception {
        var emptyParameterized = badImplementation(
                ParameterizedType.class,
                Map.of("getActualTypeArguments", new Type[0], "getRawType", List.class, "getOwnerType", NULLY)
        );
        var emptyParameterizedRef = TypeRef.wrap(emptyParameterized);
        Assertions.assertAll(
                () -> Assertions.assertEquals(emptyParameterizedRef, TypeRef.wrap(emptyParameterized)),
                () -> Assertions.assertEquals(emptyParameterizedRef.hashCode(), TypeRef.wrap(emptyParameterized).hashCode()),
                () -> Assertions.assertEquals("java.util.List", emptyParameterizedRef.toString()),
                () -> Assertions.assertEquals("java.util.List", emptyParameterizedRef.getTypeName())
        );
    }

    @Test
    public void testMinimalTypeVariable() throws Exception {
        var method = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .findFirst()
                .orElseThrow();
        var emptyVariable = badImplementation(TypeVariable.class, Map.of("getGenericDeclaration", method, "getName", "T"));
        var emptyVariableRef = TypeRef.wrap(emptyVariable);
        Assertions.assertAll(
                () -> Assertions.assertEquals(emptyVariableRef, TypeRef.wrap(emptyVariable)),
                () -> Assertions.assertEquals(emptyVariableRef.hashCode(), TypeRef.wrap(emptyVariable).hashCode()),
                () -> Assertions.assertEquals("T", emptyVariableRef.toString()),
                () -> Assertions.assertEquals("T", emptyVariableRef.getTypeName()),
                () -> Assertions.assertTrue(emptyVariableRef.equals(TypeRef.wrap(emptyVariable))),
                () -> Assertions.assertTrue(TypeRef.wrap(emptyVariable).equals(emptyVariableRef))
        );
    }

    @Test
    public void testMinimalWildcard() throws Exception {
        var emptyWildcard = badImplementation(WildcardType.class, Map.of("getUpperBounds", new Type[0], "getLowerBounds", new Type[0]));
        var emptyWildcardRef = TypeRef.wrap(emptyWildcard);
        Assertions.assertAll(
                () -> Assertions.assertEquals(emptyWildcardRef, TypeRef.wrap(emptyWildcard)),
                () -> Assertions.assertEquals(emptyWildcardRef.hashCode(), TypeRef.wrap(emptyWildcard).hashCode()),
                () -> Assertions.assertEquals("?", emptyWildcardRef.toString()),
                () -> Assertions.assertEquals("?", emptyWildcardRef.getTypeName()),
                () -> Assertions.assertTrue(emptyWildcardRef.equals(TypeRef.wrap(emptyWildcard))),
                () -> Assertions.assertTrue(TypeRef.wrap(emptyWildcard).equals(emptyWildcardRef))
        );
    }

    @Test
    public void testMinimalGenericArray() throws Exception {
        var componentType = badImplementation(Type.class, Map.of());
        var emptyGenericArray = badImplementation(GenericArrayType.class, Map.of("getGenericComponentType", componentType));
        var emptyGenericArrayRef = TypeRef.wrap(emptyGenericArray);
        Assertions.assertAll(
                () -> Assertions.assertEquals(emptyGenericArrayRef, TypeRef.wrap(emptyGenericArray)),
                () -> Assertions.assertEquals(emptyGenericArrayRef.hashCode(), TypeRef.wrap(emptyGenericArray).hashCode()),
                () -> Assertions.assertEquals("UnknownType[]", emptyGenericArrayRef.toString()),
                () -> Assertions.assertEquals("UnknownType[]", emptyGenericArrayRef.getTypeName()),
                () -> Assertions.assertTrue(emptyGenericArrayRef.equals(TypeRef.wrap(emptyGenericArray))),
                () -> Assertions.assertTrue(TypeRef.wrap(emptyGenericArray).equals(emptyGenericArrayRef))
        );
    }

    @Test
    public void testMinimalUnknownType() throws Exception {
        var emptyUnknown = badImplementation(Type.class, Map.of());
        var unknownRef = TypeRef.wrap(emptyUnknown);
        Assertions.assertAll(
                () -> Assertions.assertEquals(unknownRef, TypeRef.wrap(emptyUnknown)),
                () -> Assertions.assertEquals(unknownRef.hashCode(), TypeRef.wrap(emptyUnknown).hashCode()),
                () -> Assertions.assertEquals("UnknownType", unknownRef.toString()),
                () -> Assertions.assertEquals("UnknownType", unknownRef.getTypeName()),
                () -> Assertions.assertTrue(unknownRef.equals(TypeRef.wrap(emptyUnknown))),
                () -> Assertions.assertTrue(TypeRef.wrap(emptyUnknown).equals(unknownRef))
        );
    }

    // ── Tests: TypeVariable support ───────────────────────────────────────────

    private static <T> T genericMethod(T arg) {
        return arg;
    }

    private static <T> T genericMethod2(T arg) {
        return arg;
    }

    private static <T> T genericMethod(T arg, int other) {
        return arg;
    }

    private static <T, U> void pairMethod(T t, U u) {
    }

    private static final class GenericConstructor {
        private <C> GenericConstructor(C arg) {
        }

        private <C> GenericConstructor(C arg, int other) {
        }
    }

    private static final class GenericConstructor2 {
        private <C> GenericConstructor2(C arg) {
        }
    }

    private static final class AltGenericCtor0000 {
        private AltGenericCtor0000() {
        }
    }

    /// Two unrelated classes each declaring a method/constructor with the very same name and erased parameter
    /// types, used to exercise the `declaringClass` mismatch branch of `MethodDeclarationSer.equals(Object)` and
    /// `ConstructorDeclarationSer.equals(Object)` (i.e. same method/constructor name and parameter types, but a
    /// different declaring class).
    private static final class Twin1 {
        private static <T> T probe(T arg) {
            return arg;
        }

        private <T> Twin1(T arg) {
        }
    }

    private static final class Twin2 {
        private static <T> T probe(T arg) {
            return arg;
        }

        private <T> Twin2(T arg) {
        }
    }

    @TestFactory
    public Stream<DynamicTest> testTypeVariable() throws Exception {
        var pf = "[testTypeVariable] ";
        var classVar = List.class.getTypeParameters()[0];
        var methodVar = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        var constructorVar = GenericConstructor.class.getDeclaredConstructors()[0].getTypeParameters()[0];
        return Stream.of(classVar, methodVar, constructorVar)
                .map(typeVar -> DynamicTest.dynamicTest(pf + typeVar.getGenericDeclaration(), () -> {
                    Assertions.assertEquals(typeVar, TypeRef.wrap(typeVar).type());
                    Assertions.assertEquals(typeVar, roundTrip(typeVar));
                }));
    }

    /// Exercises every branch of `ClassDeclarationSer.equals(Object)`, `MethodDeclarationSer.equals(Object)`,
    /// `ConstructorDeclarationSer.equals(Object)` and `TypeVariableSer.equals(Object)` (the internal surrogates for
    /// `GenericDeclaration` and `TypeVariable`), which are only reachable indirectly through [TypeRef#equals(Object)]
    /// since those surrogate types are package-private.
    ///
    /// In particular this checks: two independently-obtained `TypeVariable`s declared by the very same `Class`
    /// compare equal (the `instanceof`-true/`clazz.equals`-true branches); type variables declared by a `Class`,
    /// a `Method` and a `Constructor` are pairwise unequal in both directions (the `instanceof`-false branch of
    /// each declaration surrogate's `equals`); two unrelated classes each declaring a method/constructor with the
    /// same name and parameter types compare unequal (the `declaringClass`-mismatch branch); and two type
    /// variables declared by the very same method but with different names compare unequal (the `name`-mismatch
    /// branch of `TypeVariableSer.equals`, reached only when the declarations themselves are equal).
    @Test
    public void testGenericDeclarationSerEqualsBranches() throws Exception {
        var classVar = List.class.getTypeParameters()[0];
        var sameClassVar = List.class.getTypeParameters()[0];
        var methodVar = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .filter(m -> m.getParameterCount() == 1)
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        var constructorVar = GenericConstructor.class.getDeclaredConstructors()[0].getTypeParameters()[0];

        // instanceof-true, clazz.equals-true: same class declaration, obtained independently.
        Assertions.assertEquals(TypeRef.wrap(classVar), TypeRef.wrap(sameClassVar));

        // instanceof-false: cross-kind declaration comparisons, in both directions.
        Assertions.assertNotEquals(TypeRef.wrap(classVar), TypeRef.wrap(methodVar));
        Assertions.assertNotEquals(TypeRef.wrap(methodVar), TypeRef.wrap(classVar));
        Assertions.assertNotEquals(TypeRef.wrap(methodVar), TypeRef.wrap(constructorVar));
        Assertions.assertNotEquals(TypeRef.wrap(constructorVar), TypeRef.wrap(methodVar));
        Assertions.assertNotEquals(TypeRef.wrap(classVar), TypeRef.wrap(constructorVar));
        Assertions.assertNotEquals(TypeRef.wrap(constructorVar), TypeRef.wrap(classVar));

        // declaringClass mismatch: same method/constructor name and parameter types, different declaring class.
        var twin1MethodVar = Stream.of(Twin1.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("probe"))
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        var twin2MethodVar = Stream.of(Twin2.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("probe"))
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        Assertions.assertNotEquals(TypeRef.wrap(twin1MethodVar), TypeRef.wrap(twin2MethodVar));

        var twin1CtorVar = Twin1.class.getDeclaredConstructors()[0].getTypeParameters()[0];
        var twin2CtorVar = Twin2.class.getDeclaredConstructors()[0].getTypeParameters()[0];
        Assertions.assertNotEquals(TypeRef.wrap(twin1CtorVar), TypeRef.wrap(twin2CtorVar));

        // name mismatch: two type variables declared by the very same method, hence with equal declarations.
        var pairMethod = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("pairMethod"))
                .findFirst()
                .orElseThrow();
        Assertions.assertNotEquals(TypeRef.wrap(pairMethod.getTypeParameters()[0]), TypeRef.wrap(pairMethod.getTypeParameters()[1]));

        // name mismatch: same declaring class and (erased) parameter types, different method name.
        var genericMethod = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod"))
                .filter(m -> m.getParameterCount() == 1)
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        var genericMethod2 = Stream.of(TypeserTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("genericMethod2"))
                .findFirst()
                .orElseThrow()
                .getTypeParameters()[0];
        Assertions.assertNotEquals(TypeRef.wrap(genericMethod), TypeRef.wrap(genericMethod2));

        // instanceof-false at the TypeVariableSer level itself: compared against a wholly different surrogate kind.
        Assertions.assertNotEquals(TypeRef.wrap(classVar), TypeRef.wrap(String.class));
        Assertions.assertNotEquals(TypeRef.wrap(String.class), TypeRef.wrap(classVar));
    }

    /// Exercises the remaining branches of `ParameterizedTypeSer.equals(Object)` (the internal surrogate for
    /// `ParameterizedType`), only reachable indirectly through [TypeRef#equals(Object)]: a raw-type mismatch
    /// (`raw.equals` false) and every combination of a present/absent/differing owner type (`Objects.equals`
    /// on `owner`, both null-vs-null, null-vs-non-null in both directions, and non-null-vs-differing-non-null).
    @Test
    public void testParameterizedTypeSerEqualsRawAndOwnerBranches() throws Exception {
        var listParam = badImplementation(
                ParameterizedType.class,
                Map.of("getActualTypeArguments", new Type[0], "getRawType", List.class, "getOwnerType", NULLY)
        );
        var setParam = badImplementation(
                ParameterizedType.class,
                Map.of("getActualTypeArguments", new Type[0], "getRawType", Set.class, "getOwnerType", NULLY)
        );

        // raw.equals-false: same (empty) args and owner, different raw type.
        Assertions.assertNotEquals(TypeRef.wrap(listParam), TypeRef.wrap(setParam));
        Assertions.assertNotEquals(TypeRef.wrap(setParam), TypeRef.wrap(listParam));

        var innerType = (ParameterizedType) Sample.class.getDeclaredField("inner").getGenericType();
        var innerRaw = innerType.getRawType();
        var innerArgs = innerType.getActualTypeArguments();
        var innerOwner = innerType.getOwnerType();

        var withOwner = badImplementation(
                ParameterizedType.class,
                Map.of("getActualTypeArguments", innerArgs, "getRawType", innerRaw, "getOwnerType", innerOwner)
        );
        var withoutOwner = badImplementation(
                ParameterizedType.class,
                Map.of("getActualTypeArguments", innerArgs, "getRawType", innerRaw, "getOwnerType", NULLY)
        );

        // owner Objects.equals-true: same raw, args and owner (real vs. hand-crafted).
        Assertions.assertEquals(TypeRef.wrap(withOwner), TypeRef.wrap(innerType));

        // owner Objects.equals-false: null-vs-non-null, in both directions.
        Assertions.assertNotEquals(TypeRef.wrap(withOwner), TypeRef.wrap(withoutOwner));
        Assertions.assertNotEquals(TypeRef.wrap(withoutOwner), TypeRef.wrap(withOwner));

        // owner Objects.equals-false: non-null-vs-differing-non-null, same raw and args.
        var inner2Type = Sample.class.getDeclaredField("inner2").getGenericType();
        Assertions.assertNotEquals(TypeRef.wrap(innerType), TypeRef.wrap(inner2Type));

        // Same as above, but calling equals() directly on the reconstructed anonymous ParameterizedType (as
        // opposed to on the wrapping TypeRef), to hit ParameterizedTypeSer's anonymous toType() implementation's
        // own getRawType() mismatch branch (owner equal-null on both sides, so only raw differs). Uses an actual
        // serialization round-trip (not TypeRef#type()) since the latter would just return the original, still
        // same-JVM-alive proxy instead of forcing reconstruction from the surrogate.
        var listParamCopy = roundTrip(listParam);
        var setParamCopy = roundTrip(setParam);
        Assertions.assertNotEquals(listParamCopy, setParamCopy);
        Assertions.assertNotEquals(setParamCopy, listParamCopy);
    }

    /// Exercises the remaining branches of `WildcardTypeSer.equals(Object)` (the internal surrogate for
    /// `WildcardType`), only reachable indirectly through [TypeRef#equals(Object)]: a lower-bound mismatch while
    /// the upper bounds match (`? extends Object` implicitly, both for an unbounded wildcard and for a
    /// lower-bounded one), which is otherwise never reached because the upper-bound comparison is checked first
    /// and short-circuits whenever it already differs; and the `instanceof`-false branch, reached by comparing a
    /// wildcard against a completely unrelated surrogate kind.
    @Test
    public void testWildcardTypeSerEqualsLowerBoundBranch() throws Exception {
        var any = wildcardArgOf("any");
        var lower = wildcardArgOf("lower");
        Assertions.assertNotEquals(TypeRef.wrap(any), TypeRef.wrap(lower));
        Assertions.assertNotEquals(TypeRef.wrap(lower), TypeRef.wrap(any));
        Assertions.assertNotEquals(TypeRef.wrap(any), TypeRef.wrap(String.class));
        Assertions.assertNotEquals(TypeRef.wrap(String.class), TypeRef.wrap(any));
    }

    /// Exercises the surrogate types that can only ever be reconstructed via a maliciously-crafted `Type`
    /// implementation: a `ParameterizedType` whose `getRawType()` itself returns another `ParameterizedType`
    /// (instead of the `Class` that any genuinely-reflected raw type always is). This is what drives `raw` to be
    /// a `ParameterizedTypeSer` rather than a `ClassSer` in `ParameterizedTypeSer.toString()`, hitting the
    /// `else` branch of both ternaries there (`raw instanceof ClassSer<?> cs ? ... : raw.getTypeName()`), for
    /// both the owner-present and owner-absent cases.
    @Test
    public void testParameterizedTypeSerToStringWithNonClassRaw() throws Exception {
        var fakeRaw = new LinkedParameterizedType();
        fakeRaw.raw = List.class;

        var noOwner = new LinkedParameterizedType();
        noOwner.raw = fakeRaw;
        Assertions.assertEquals("java.util.List", TypeRef.wrap(noOwner).toString());

        var withOwner = new LinkedParameterizedType();
        withOwner.raw = fakeRaw;
        withOwner.owner = fakeRaw;
        Assertions.assertEquals("java.util.List$java.util.List", TypeRef.wrap(withOwner).toString());
    }

    /// Exercises the remaining branches of the anonymous `ParameterizedType`/`WildcardType`/`GenericArrayType`
    /// implementations produced by `SerializableType.toType()`: `this == other` (reflexivity), the `instanceof`
    /// false branch (compared against an unrelated object, including `null`), and a field-mismatch false branch
    /// for each compared component (owner/raw/args for `ParameterizedType`, upper/lower bounds for `WildcardType`,
    /// component type for `GenericArrayType`). These are only reachable by calling `equals()` directly on the
    /// reconstructed `Type` itself (as opposed to on the wrapping [TypeRef], which instead exercises the
    /// record-level `equals()` of the internal surrogate).
    @Test
    public void testReconstructedAnonymousTypeEqualsBranches() throws Exception {
        var listCopy = roundTrip(Sample.class.getDeclaredField("list").getGenericType());
        var integerListCopy = roundTrip(Sample.class.getDeclaredField("integerList").getGenericType());
        var entryCopy = roundTrip(Sample.class.getDeclaredField("entry").getGenericType());
        var innerCopy = roundTrip(Sample.class.getDeclaredField("inner").getGenericType());
        var inner2Copy = roundTrip(Sample.class.getDeclaredField("inner2").getGenericType());
        var upperCopy = roundTrip(wildcardArgOf("upper"));
        var anyCopy = roundTrip(wildcardArgOf("any"));
        var lowerCopy = roundTrip(wildcardArgOf("lower"));
        var arrayCopy = roundTrip(Sample.class.getDeclaredField("genericArray").getGenericType());
        var integerArrayCopy = roundTrip(Sample.class.getDeclaredField("integerGenericArray").getGenericType());

        // ParameterizedType: this == other, instanceof-false, args mismatch, raw mismatch, owner mismatch.
        Assertions.assertEquals(listCopy, listCopy);
        Assertions.assertNotEquals(listCopy, "not a type");
        Assertions.assertFalse(listCopy.equals(null));
        Assertions.assertNotEquals(listCopy, integerListCopy);
        Assertions.assertNotEquals(integerListCopy, listCopy);
        Assertions.assertNotEquals(listCopy, entryCopy);
        Assertions.assertNotEquals(innerCopy, inner2Copy);
        Assertions.assertNotEquals(inner2Copy, innerCopy);

        // WildcardType: this == other, instanceof-false, upper mismatch, lower mismatch.
        Assertions.assertEquals(upperCopy, upperCopy);
        Assertions.assertNotEquals(upperCopy, "not a type");
        Assertions.assertFalse(upperCopy.equals(null));
        Assertions.assertNotEquals(upperCopy, anyCopy);
        Assertions.assertNotEquals(anyCopy, upperCopy);
        Assertions.assertNotEquals(anyCopy, lowerCopy);
        Assertions.assertNotEquals(lowerCopy, anyCopy);

        // GenericArrayType: this == other, instanceof-false, component mismatch.
        Assertions.assertEquals(arrayCopy, arrayCopy);
        Assertions.assertNotEquals(arrayCopy, "not a type");
        Assertions.assertFalse(arrayCopy.equals(null));
        Assertions.assertNotEquals(arrayCopy, integerArrayCopy);
        Assertions.assertNotEquals(integerArrayCopy, arrayCopy);
    }

    // ── Tests: malformed/malicious `Type` implementations ────────────────────
    //
    // These types can never come from real reflection, but nothing stops a hand-written or
    // maliciously-crafted `ParameterizedType`/`WildcardType`/`GenericArrayType`/`TypeVariable`
    // implementation from violating their documented contracts (e.g. returning `null` where an
    // array or component is expected, or forming a cycle). `TypeRef` must not misbehave (crash
    // with a raw `NullPointerException`/`StackOverflowError`, corrupt state, hang, etc.) when fed
    // such implementations; it must fail predictably with `IllegalArgumentException`.

    /// Asserts that `exec` throws an exception of the given `type` whose message contains `expectedSubstring`.
    ///
    /// Checking the message (not just the exception type) matters here because several completely different
    /// malformed-type scenarios (a `null` sub-component, a cyclic type graph, an oversized type graph, and so
    /// on) all surface as the very same {@link IllegalArgumentException} class. Without pinning down the
    /// message, a test for one scenario (say, a cycle) could pass for the wrong reason (say, because the
    /// "too deeply nested" check tripped instead), silently letting a regression slip through.
    /// @param <T> The expected exception type.
    /// @param type The expected exception type.
    /// @param expectedSubstring A substring that must be present in the thrown exception's message.
    /// @param exec The code expected to throw.
    /// @return The thrown exception, in case further assertions are needed.
    private static <T extends Throwable> T assertThrowsContaining(Class<T> type, String expectedSubstring, Executable exec) {
        var ex = Assertions.assertThrows(type, exec);
        Assertions.assertNotNull(ex.getMessage(), () -> "Expected a non-null message on " + ex);
        Assertions.assertTrue(
                ex.getMessage().contains(expectedSubstring),
                () -> "Expected message to contain \"" + expectedSubstring + "\" but was \"" + ex.getMessage() + "\""
        );
        return ex;
    }

    private static final class LinkedParameterizedType implements ParameterizedType {
        private Type raw;
        private Type[] args = new Type[0];
        private Type owner;

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return owner;
        }
    }

    private static final class SelfComponentArrayType implements GenericArrayType {
        @Override
        public Type getGenericComponentType() {
            return this;
        }
    }

    private abstract static class AbstractFakeTypeVariable implements TypeVariable<GenericDeclaration> {
        @Override
        public Type[] getBounds() {
            return new Type[0];
        }

        @Override
        public String getName() {
            return "T";
        }

        @Override
        public AnnotatedType[] getAnnotatedBounds() {
            throw new AssertionError();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

    private static final class ForeignGenericDeclaration implements GenericDeclaration {
        @Override
        public TypeVariable<?>[] getTypeParameters() {
            return new TypeVariable<?>[0];
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }
    }

    private static final class UnsupportedType implements Type {
        @Override
        public String toString() {
            return "UnsupportedType";
        }
    }

    // ── Tests: DoS protection against oversized/maliciously-exploding type graphs ────────────

    /// Builds a chain of {@code depth} distinct (non-cyclic!) {@link ParameterizedType}s linked through
    /// {@link ParameterizedType#getOwnerType()}, where each link is a brand new object. Since every object is
    /// distinct, this never triggers the cycle detector, yet a sufficiently long chain represents an unboundedly
    /// deep type graph, exactly like a maliciously-crafted {@code getOwnerType()} that keeps fabricating new
    /// owner instances instead of reflecting a real enclosing type.
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private static LinkedParameterizedType deepOwnerChain(int depth) {
        LinkedParameterizedType previous = null;
        for (int i = 0; i < depth; i++) {
            var current = new LinkedParameterizedType();
            current.raw = List.class;
            current.owner = previous;
            previous = current;
        }
        return previous;
    }

    /// Builds a single {@link ParameterizedType} with {@code width} distinct type arguments, each of which is
    /// itself a small but distinct {@link ParameterizedType}. This represents a maliciously wide (as opposed to
    /// deep) type graph, exactly like a {@code getActualTypeArguments()} fabricating hundreds of forged
    /// sub-types instead of reflecting real type arguments.
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private static LinkedParameterizedType wideTypeArguments(int width) {
        var top = new LinkedParameterizedType();
        top.raw = List.class;
        var args = new Type[width];
        for (int i = 0; i < width; i++) {
            var leaf = new LinkedParameterizedType();
            leaf.raw = Object.class;
            args[i] = leaf;
        }
        top.args = args;
        return top;
    }

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testTooLargeType() {
        var pf = "[testTooLargeType] ";
        return Stream.of(
                DynamicTest.dynamicTest(
                        pf + "moderately deep but legitimate-sized owner chain succeeds",
                        () -> Assertions.assertDoesNotThrow(() -> TypeRef.wrap(deepOwnerChain(30)))
                ),
                DynamicTest.dynamicTest(
                        pf + "moderately wide but legitimate-sized type-argument list succeeds",
                        () -> Assertions.assertDoesNotThrow(() -> TypeRef.wrap(wideTypeArguments(30)))
                ),
                DynamicTest.dynamicTest(
                        pf + "unboundedly deep owner-type chain is rejected",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Too deeply nested type graph detected involving",
                                () -> TypeRef.wrap(deepOwnerChain(10_000))
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "excessively wide type-argument list is rejected",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Too deeply nested type graph detected involving",
                                () -> TypeRef.wrap(wideTypeArguments(10_000))
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "the type graph size limit is tracked per call, not accumulated across calls",
                        () -> {
                            // Trigger the size limit a few times in a row...
                            for (int i = 0; i < 5; i++) {
                                assertThrowsContaining(
                                        IllegalArgumentException.class,
                                        "Too deeply nested type graph detected involving",
                                        () -> TypeRef.wrap(deepOwnerChain(10_000))
                                );
                            }
                            // ...and then confirm that ordinary, legitimately-sized types still serialize fine
                            // afterwards, proving that no leftover state from the rejected oversized attempts
                            // is leaking into (and permanently poisoning) later, unrelated calls.
                            Assertions.assertDoesNotThrow(() -> TypeRef.wrap(deepOwnerChain(10)));
                            Assertions.assertEquals(String.class, TypeRef.wrap(String.class).type());
                        }
                )
        );
    }

    @TestFactory
    @SuppressWarnings({"ThrowableResultIgnored", "AccessingNonPublicFieldOfAnotherObject"})
    public Stream<DynamicTest> testMalformedTypes() {
        var nullArgsArray = new LinkedParameterizedType();
        nullArgsArray.raw = List.class;
        nullArgsArray.args = null;

        var nullArgsElement = new LinkedParameterizedType();
        nullArgsElement.raw = List.class;
        nullArgsElement.args = new Type[] {String.class, null};

        var nullRawType = new LinkedParameterizedType();
        nullRawType.raw = null;

        var selfRawType = new LinkedParameterizedType();
        selfRawType.raw = selfRawType;

        var mutualA = new LinkedParameterizedType();
        var mutualB = new LinkedParameterizedType();
        mutualA.raw = List.class;
        mutualA.args = new Type[] {mutualB};
        mutualB.raw = mutualA;

        WildcardType nullUpperBounds = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return null;
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };

        WildcardType nullLowerBoundsElement = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[] {Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[] {null};
            }
        };

        WildcardType nullLowerBoundsArray = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[] {Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return null;
            }
        };

        GenericArrayType nullComponent = () -> null;
        var selfComponent = new SelfComponentArrayType();

        var nullDeclaration = new AbstractFakeTypeVariable() {
            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };

        var foreignDeclaration = new AbstractFakeTypeVariable() {
            @Override
            public GenericDeclaration getGenericDeclaration() {
                return new ForeignGenericDeclaration();
            }
        };

        var missingTypeVariable = new AbstractFakeTypeVariable() {
            @Override
            public GenericDeclaration getGenericDeclaration() {
                return List.class;
            }

            @Override
            public String getName() {
                return "THIS_NAME_DOES_NOT_EXIST";
            }
        };

        var unknownType = new UnsupportedType();

        var pf = "[testMalformedTypes] ";
        return Stream.of(
                DynamicTest.dynamicTest(
                        pf + "parameterized type with null type-argument array",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                ".getActualTypeArguments() returned null.",
                                () -> TypeRef.wrap(nullArgsArray)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "parameterized type with a null type argument",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullArgsElement))
                ),
                DynamicTest.dynamicTest(
                        pf + "parameterized type with null raw type",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullRawType))
                ),
                DynamicTest.dynamicTest(
                        pf + "parameterized type whose raw type is itself",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Cyclic type graph detected involving",
                                () -> TypeRef.wrap(selfRawType)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "two parameterized types cyclically referencing each other",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Cyclic type graph detected involving",
                                () -> TypeRef.wrap(mutualA)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with null upper-bounds array",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                ".getUpperBounds() returned null.",
                                () -> TypeRef.wrap(nullUpperBounds)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with null lower-bounds array",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                ".getLowerBounds() returned null.",
                                () -> TypeRef.wrap(nullLowerBoundsArray)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "wildcard type with a null lower bound",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullLowerBoundsElement))
                ),
                DynamicTest.dynamicTest(
                        pf + "generic array type with null component",
                        () -> ForTests.testNull("type", () -> TypeRef.wrap(nullComponent))
                ),
                DynamicTest.dynamicTest(
                        pf + "generic array type whose component is itself",
                        () -> assertThrowsContaining(
                                IllegalArgumentException.class,
                                "Cyclic type graph detected involving",
                                () -> TypeRef.wrap(selfComponent)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable with null generic declaration",
                        () -> ForTests.testNull("declaration", () -> TypeRef.wrap(nullDeclaration))
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable declared by a foreign GenericDeclaration",
                        () -> assertThrowsContaining(
                                UnsupportedOperationException.class,
                                "Unsupported GenericDeclaration:",
                                () -> TypeRef.wrap(foreignDeclaration)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "type variable whose name is missing in declaration",
                        () -> assertThrowsContaining(
                                IllegalStateException.class,
                                "Type variable \"THIS_NAME_DOES_NOT_EXIST\" not found in",
                                () -> roundTrip(missingTypeVariable)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "unknown type implementation is preserved before serialization",
                        () -> Assertions.assertSame(unknownType, TypeRef.wrap(unknownType).type())
                ),
                DynamicTest.dynamicTest(
                        pf + "unknown type implementation cannot be reconstructed after serialization",
                        () -> assertThrowsContaining(
                                UnsupportedOperationException.class,
                                "Unknown type.",
                                () -> roundTrip(unknownType)
                        )
                ),
                DynamicTest.dynamicTest(
                        pf + "stale method declaration in serialized stream",
                        () -> {
                            var methodVar = Stream.of(TypeserTest.class.getDeclaredMethods())
                                    .filter(m -> m.getName().equals("genericMethod"))
                                    .findFirst()
                                    .orElseThrow()
                                    .getTypeParameters()[0];
                            assertThrowsContaining(
                                    IllegalStateException.class,
                                    "Method \"missingMethod\" not found in",
                                    () -> roundTrip(methodVar, "genericMethod", "missingMethod")
                            );
                        }
                ),
                DynamicTest.dynamicTest(
                        pf + "stale constructor declaration in serialized stream",
                        () -> {
                            var constructorVar = GenericConstructor.class.getDeclaredConstructors()[0].getTypeParameters()[0];
                            assertThrowsContaining(
                                    IllegalStateException.class,
                                    "Constructor not found in class ninja.test.javahacker.typeser.TypeserTest$AltGenericCtor0000.",
                                    () -> roundTrip(
                                            constructorVar,
                                            "GenericConstructor",
                                            "AltGenericCtor0000"
                                    )
                            );
                        }
                )
        );
    }

    // ── Tests: @NonNull violations → IllegalArgumentException ───────────────

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() throws Exception {
        var pf = "[testNulls] ";
        var out = new ObjectOutputStream(new ByteArrayOutputStream());
        return Stream.of(
            DynamicTest.dynamicTest(pf + "a", () -> ForTests.testNull("type", () -> new TypeRef(null))),
            DynamicTest.dynamicTest(pf + "b", () -> ForTests.testNull("type", () -> TypeRef.wrap(null))),
            DynamicTest.dynamicTest(pf + "c", () -> ForTests.testNull("in", () -> TypeRef.read(null))),
            DynamicTest.dynamicTest(pf + "d", () -> ForTests.testNull("out", () -> TypeRef.write(null, String.class))),
            DynamicTest.dynamicTest(pf + "e", () -> ForTests.testNull("type", () -> TypeRef.write(out, null)))
        );
    }
}
