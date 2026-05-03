package ninja.javahacker.test.annotimpler.convert;

import java.lang.reflect.Proxy;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.convert;

@SuppressWarnings("unused")
public class DefaultConverterMethodsTest {

    public DefaultConverterMethodsTest() {
    }

    private static class Foo {}

    @SuppressWarnings("unchecked")
    private <E> E mock(Class<E> mockClass) {
        return (E) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {mockClass }, (i, m, a) -> {
            throw new AssertionError(m.getName());
        });
    }

    private <E> DynamicNode testIn(Class<E> base, boolean isObj, Executable m) throws Exception {
        var msg = base == Foo.class ? "Unsupported Type: " + Foo.class.getName() + "." : "Unsupported " + base.getSimpleName() + ".";
        return DynamicTest.dynamicTest(
                "[testDefaultUnsupportedConvertions] Unsupported conversion " + (isObj ? "fromObj " : "from ") + base.getSimpleName() + ".",
                () -> {
                    var ex = Assertions.assertThrows(ConvertionException.class, m);
                    Assertions.assertEquals(msg, ex.getMessage());
                }
        );
    }

    @TestFactory
    public List<DynamicNode> testDefaultUnsupportedConvertions() throws Exception {
        var ld = LocalDate.of(2000, 1, 2);
        var lt = LocalTime.of(10, 15, 20);
        var ldt = ld.atTime(lt);
        var cvt = new Converter<Foo>() {};
        return List.of(
                testIn(BigDecimal    .class, false, () -> cvt.from(BigDecimal.ZERO)),
                testIn(BigDecimal    .class, true , () -> cvt.fromObj(BigDecimal.ZERO)),
                testIn(LocalDateTime .class, false, () -> cvt.from(ldt)),
                testIn(LocalDateTime .class, true , () -> cvt.fromObj(ldt)),
                testIn(LocalDate     .class, false, () -> cvt.from(ld)),
                testIn(LocalDate     .class, true , () -> cvt.fromObj(ld)),
                testIn(LocalTime     .class, false, () -> cvt.from(lt)),
                testIn(LocalTime     .class, true , () -> cvt.fromObj(lt)),
                testIn(OffsetDateTime.class, false, () -> cvt.from(ldt.atOffset(ZoneOffset.UTC))),
                testIn(OffsetDateTime.class, true , () -> cvt.fromObj(ldt.atOffset(ZoneOffset.UTC))),
                testIn(OffsetTime    .class, false, () -> cvt.from(lt.atOffset(ZoneOffset.UTC))),
                testIn(OffsetTime    .class, true , () -> cvt.fromObj(lt.atOffset(ZoneOffset.UTC))),
                testIn(Blob          .class, false, () -> cvt.from(mock(Blob.class))),
                testIn(Blob          .class, true , () -> cvt.fromObj(mock(Blob.class))),
                testIn(Clob          .class, false, () -> cvt.from(mock(Clob.class))),
                testIn(Clob          .class, true , () -> cvt.fromObj(mock(Clob.class))),
                testIn(NClob         .class, false, () -> cvt.from(mock(NClob.class))),
                testIn(NClob         .class, true , () -> cvt.fromObj(mock(NClob.class))),
                testIn(Ref           .class, false, () -> cvt.from(mock(Ref.class))),
                testIn(Ref           .class, true , () -> cvt.fromObj(mock(Ref.class))),
                testIn(RowId         .class, false, () -> cvt.from(mock(RowId.class))),
                testIn(RowId         .class, true , () -> cvt.fromObj(mock(RowId.class))),
                testIn(Struct        .class, false, () -> cvt.from(mock(Struct.class))),
                testIn(Struct        .class, true , () -> cvt.fromObj(mock(Struct.class))),
                testIn(SQLXML        .class, false, () -> cvt.from(mock(SQLXML.class))),
                testIn(SQLXML        .class, true , () -> cvt.fromObj(mock(SQLXML.class))),
                testIn(java.sql.Array.class, false, () -> cvt.from(mock(java.sql.Array.class))),
                testIn(java.sql.Array.class, true , () -> cvt.fromObj(mock(java.sql.Array.class))),
                testIn(String        .class, false, () -> cvt.from("xxx")),
                testIn(String        .class, true , () -> cvt.fromObj("xxx")),
                testIn(byte[]        .class, false, () -> cvt.from("xxx".getBytes())),
                testIn(byte[]        .class, true , () -> cvt.fromObj("xxx".getBytes())),
                testIn(Foo           .class, true , () -> cvt.fromObj(new Foo()))
        );
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testDefaultFromNullConvertion() throws Exception {
        var cvt = new Converter<Foo>() {};
        Assertions.assertTrue(cvt.fromNull().isEmpty());
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testDefaultFromNullObjConvertion() throws Exception {
        var cvt = new Converter<Foo>() {};
        Assertions.assertTrue(cvt.fromObj(null).isEmpty());
    }

    @TestFactory
    public Stream<DynamicNode> testStdGetType() throws Exception {
        return TestTypes.CVT_TYPES.stream().map(t -> DynamicTest.dynamicTest("[testStdGetType] " + TypeName.of(t), () -> {
            var cvt = ConverterFactory.STD.get(t);
            Assertions.assertEquals(t, cvt.getType());
        }));
    }

    private static <E> void noop(Foo a, Thread b, List<Runtime> c, Optional<?> d, Optional<String>[] e, Method f, Runtime h, Field i) {
        throw new AssertionError();
    }

    private static interface SomeInterface<X> {
    }

    @SuppressWarnings("serial")
    private static class Yyy1Converter implements SomeInterface<String>, Converter<Method>, Serializable {
    }

    @SuppressWarnings({"serial", "rawtypes"})
    private static class Yyy2Converter implements SomeInterface<String>, Converter, Serializable {
    }

    private static interface Indirect3 extends Converter<Runtime> {
    }

    @SuppressWarnings("serial")
    private static class Yyy3Converter implements SomeInterface<String>, Indirect3, Serializable {
    }

    private static interface Indirect4<E> extends Converter<E> {
    }

    @SuppressWarnings("serial")
    private static class Yyy4Converter implements SomeInterface<String>, Indirect4<Field>, Serializable {
    }

    @TestFactory
    public Stream<DynamicNode> testDefaultGetType() throws Exception {
        var cvts = List.of(
                new Converter<Foo>() {},
                new Converter<Thread>() {},
                new Converter<List<Runtime>>() {},
                new Converter<Optional<?>>() {},
                new Converter<Optional<String>[]>() {},
                new Yyy1Converter(),
                new Yyy3Converter(),
                new Yyy4Converter()
        );
        var ints = IntStream.range(0, cvts.size()).mapToObj(i -> i).toList();
        var typs = Stream.of(DefaultConverterMethodsTest.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("noop"))
                .flatMap(m -> Stream.of(m.getParameters()))
                .map(Parameter::getParameterizedType)
                .toList();
        return ints.stream().map(i -> DynamicTest.dynamicTest("[testDefaultGetType] " + TypeName.of(typs.get(i)), () -> {
            var cvt = cvts.get(i);
            Assertions.assertEquals(typs.get(i), cvt.getType());
        }));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testBadDefaultGetType1() throws Exception {
        var cvt = new Converter() {};
        var ex = Assertions.assertThrows(IllegalStateException.class, () -> cvt.getType());
        Assertions.assertEquals("Couldn't determine the type. Please, override this method.", ex.getMessage());
    }

    @Test
    public void testBadDefaultGetType2() throws Exception {
        var cvt = new Yyy2Converter();
        var ex = Assertions.assertThrows(IllegalStateException.class, () -> cvt.getType());
        Assertions.assertEquals("Couldn't determine the type. Please, override this method.", ex.getMessage());
    }

    @Test
    public void testDefaultGetTypeIsStillTooLimitedInSomeComplexCases() throws Exception {
        class XxxConverter<A, B, C> implements Converter<B> {}
        var cvt = new XxxConverter<Thread, List<String>, Thread>() {};
        var ex = Assertions.assertThrows(IllegalStateException.class, () -> cvt.getType());
        Assertions.assertEquals("Couldn't determine the type. Please, override this method.", ex.getMessage());
    }
}