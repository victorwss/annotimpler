package ninja.javahacker.test.annotimpler.convert;

import lombok.NonNull;

import module java.base;
import module ninja.javahacker.annotimpler.convert;
import module org.junit.jupiter.api;

public class CompositeCustomConverterTest {

    public static class Foo {}

    public record Bar1(List<Foo> f) {}

    public record Bar2(Optional<Foo> f) {}

    public record Bar3(Foo[] f) {}

    public record Bar4(Foo f) {}

    public record Bad1(String f) {
        @Creator
        public static Bad1 foo1() {
            throw new AssertionError();
        }

        @Creator
        public static Bad1 foo2() {
            throw new AssertionError();
        }
    }

    public record Bad2(String f) {
        public Bad2 {
            throw new IllegalStateException("boo");
        }
    }

    @Test
    public void testRecordOfEmptyList1() throws Exception {

        var cvt = new Converter<List<Foo>>() {
            @NonNull
            @Override
            public Optional<List<Foo>> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.of(List.of());
            }
        };

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            public Optional<? extends Converter<? extends List<?>>> makeList(@NonNull ParameterizedType p) throws UnavailableConverterException {
                Assertions.assertEquals(List.class, p.getRawType());
                Assertions.assertEquals(Foo.class, p.getActualTypeArguments()[0]);
                return Optional.of(cvt);
            }
        };

        Assertions.assertTrue(cvtf.get(Bar1.class).from("xxx").isEmpty());
    }

    @Test
    public void testRecordOfEmptyList2() throws Exception {

        var cvt = new Converter<List<Foo>>() {
            @NonNull
            @Override
            public Optional<List<Foo>> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.empty();
            }
        };

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            public Optional<? extends Converter<? extends List<?>>> makeList(@NonNull ParameterizedType p) throws UnavailableConverterException {
                Assertions.assertEquals(List.class, p.getRawType());
                Assertions.assertEquals(Foo.class, p.getActualTypeArguments()[0]);
                return Optional.of(cvt);
            }
        };

        Assertions.assertTrue(cvtf.get(Bar1.class).from("xxx").isEmpty());
    }

    @Test
    public void testRecordOfEmptyOptional1() throws Exception {

        var cvt = new Converter<Optional<Foo>>() {
            @NonNull
            @Override
            public Optional<Optional<Foo>> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.of(Optional.empty());
            }
        };

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            public Optional<? extends Converter<? extends Optional<?>>> makeOptional(@NonNull ParameterizedType p) throws UnavailableConverterException {
                Assertions.assertEquals(Optional.class, p.getRawType());
                Assertions.assertEquals(Foo.class, p.getActualTypeArguments()[0]);
                return Optional.of(cvt);
            }
        };

        Assertions.assertTrue(cvtf.get(Bar2.class).from("xxx").isEmpty());
    }

    @Test
    public void testRecordOfEmptyOptional2() throws Exception {

        var cvt = new Converter<Optional<Foo>>() {
            @NonNull
            @Override
            public Optional<Optional<Foo>> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.empty();
            }
        };

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            public Optional<? extends Converter<? extends Optional<?>>> makeOptional(@NonNull ParameterizedType p) throws UnavailableConverterException {
                Assertions.assertEquals(Optional.class, p.getRawType());
                Assertions.assertEquals(Foo.class, p.getActualTypeArguments()[0]);
                return Optional.of(cvt);
            }
        };

        Assertions.assertTrue(cvtf.get(Bar2.class).from("xxx").isEmpty());
    }

    @Test
    public void testRecordOfEmptyArray1() throws Exception {

        var cvt = new Converter<Foo[]>() {
            @NonNull
            @Override
            public Optional<Foo[]> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.of(new Foo[0]);
            }
        };

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <E> Optional<? extends Converter<E>> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
                if (klass == Bar3.class) return Optional.empty();
                Assertions.assertEquals(Foo[].class, klass);
                return Optional.of((Converter<E>) cvt);
            }
        };

        Assertions.assertTrue(cvtf.get(Bar3.class).from("xxx").isEmpty());
    }

    @Test
    public void testRecordOfEmptyArray2() throws Exception {

        var cvt = new Converter<Foo[]>() {
            @NonNull
            @Override
            public Optional<Foo[]> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.empty();
            }
        };

        var cvtf = new StdConverterFactory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <E> Optional<? extends Converter<E>> makeArray(@NonNull Class<E> klass) throws UnavailableConverterException {
                if (klass == Bar3.class) return Optional.empty();
                Assertions.assertEquals(Foo[].class, klass);
                return Optional.of((Converter<E>) cvt);
            }
        };

        Assertions.assertTrue(cvtf.get(Bar3.class).from("xxx").isEmpty());
    }

    @TestFactory
    public Stream<DynamicTest> testEmptyRootObject() throws Exception {
        var cvt = new Converter<Foo>() {
            @NonNull
            @Override
            public Optional<Foo> from(@NonNull String in) throws ConvertionException {
                Assertions.assertEquals("xxx", in);
                return Optional.empty();
            }
        };

        var cvtf = StdConverterFactory.INSTANCE.extend(Foo.class, cvt);
        return Stream.of(
                DynamicTest.dynamicTest("[testEmptyRootObject] array"               , () -> Assertions.assertEquals(0, cvtf.getOf(Foo[].class).from("xxx").get().length)),
                DynamicTest.dynamicTest("[testEmptyRootObject] record with list"    , () -> Assertions.assertTrue(cvtf.get(Bar1.class).from("xxx").isEmpty())),
                DynamicTest.dynamicTest("[testEmptyRootObject] record with optional", () -> Assertions.assertTrue(cvtf.get(Bar2.class).from("xxx").isEmpty())),
                DynamicTest.dynamicTest("[testEmptyRootObject] record with array"   , () -> Assertions.assertTrue(cvtf.get(Bar3.class).from("xxx").isEmpty())),
                DynamicTest.dynamicTest("[testEmptyRootObject] record with object"  , () -> Assertions.assertTrue(cvtf.get(Bar4.class).from("xxx").isEmpty()))
        );
    }

    @Test
    public void testBadRecordConverter1() throws Exception {
        var uce = Assertions.assertThrows(UnavailableConverterException.class, () -> ConverterFactory.std().get(Bad1.class).from("xxx"));
        Assertions.assertAll(
                () -> Assertions.assertEquals(MagicFactory.CreatorSelectionException.class, uce.getCause().getClass()),
                () -> Assertions.assertEquals(uce.getCause().getMessage(), uce.getMessage()),
                () -> Assertions.assertEquals(Bad1.class, uce.getRoot()),
                () -> Assertions.assertEquals("Can't have @Creator more than once in class " + Bad1.class.getSimpleName() + ".", uce.getCause().getMessage()),
                () -> Assertions.assertEquals(Bad1.class, ((MagicFactory.CreatorSelectionException) uce.getCause()).getRoot())
        );
    }

    @Test
    public void testBadRecordConverter2() throws Exception {
        var ce = Assertions.assertThrows(ConvertionException.class, () -> ConverterFactory.std().get(Bad2.class).from("xxx"));
        Assertions.assertAll(
                () -> Assertions.assertEquals(MagicFactory.CreationException.class, ce.getCause().getClass()),
                () -> Assertions.assertEquals(ce.getCause().getMessage(), ce.getMessage()),
                () -> Assertions.assertEquals(String.class, ce.getIn()),
                () -> Assertions.assertEquals(Bad2.class, ce.getOut()),
                () -> Assertions.assertEquals("The instantiation of " + Bad2.class.getSimpleName() + " threw an exception.", ce.getCause().getMessage()),
                () -> Assertions.assertEquals(Bad2.class, ((MagicFactory.CreationException) ce.getCause()).getRoot()),
                () -> Assertions.assertEquals(IllegalStateException.class, ce.getCause().getCause().getClass()),
                () -> Assertions.assertEquals("boo", ce.getCause().getCause().getMessage())
        );
    }
}
