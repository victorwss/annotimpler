package ninja.javahacker.test.annotimpler.convert;

import lombok.SneakyThrows;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.convert;

@Disabled
public class CustomConverterFactoryTest {

    public static class Foo {
        private final int number;

        public Foo(int number) {
            this.number = number;
        }

        public int getNumber() {
            return this.number;
        }
    }

    private static final Type FOO_TYPE = (new Supplier<Type>() {
        @Override
        @SneakyThrows
        public Type get() {
            return ((ParameterizedType) FooMapConverter.class.getMethod("fromNull").getGenericReturnType()).getActualTypeArguments()[0];
        }
    }).get();

    private static class FooMapConverter implements Converter<Map<String, Foo>> {

        @Override
        public Optional<Map<String, Foo>> fromObj(Object in) throws ConvertionException {
            return Optional.of(Map.of("y", new Foo(25)));
        }

        @Override
        public Type getType() {
            return FOO_TYPE;
        }

        @Override
        public Optional<Map<String, Foo>> fromNull() throws ConvertionException {
            return Optional.of(Map.of("aa", new Foo(1)));
        }

        @Override
        public Optional<Map<String, Foo>> from(boolean in) throws ConvertionException {
            return Optional.of(Map.of("ab", new Foo(2)));
        }

        @Override
        public Optional<Map<String, Foo>> from(byte in) throws ConvertionException {
            return Optional.of(Map.of("ac", new Foo(3)));
        }

        @Override
        public Optional<Map<String, Foo>> from(short in) throws ConvertionException {
            return Optional.of(Map.of("ad", new Foo(4)));
        }

        @Override
        public Optional<Map<String, Foo>> from(int in) throws ConvertionException {
            return Optional.of(Map.of("ae", new Foo(5)));
        }

        @Override
        public Optional<Map<String, Foo>> from(long in) throws ConvertionException {
            return Optional.of(Map.of("af", new Foo(6)));
        }

        @Override
        public Optional<Map<String, Foo>> from(float in) throws ConvertionException {
            return Optional.of(Map.of("ag", new Foo(7)));
        }

        @Override
        public Optional<Map<String, Foo>> from(double in) throws ConvertionException {
            return Optional.of(Map.of("ah", new Foo(8)));
        }

        @Override
        public Optional<Map<String, Foo>> from(BigDecimal in) throws ConvertionException {
            return Optional.of(Map.of("ai", new Foo(9)));
        }

        @Override
        public Optional<Map<String, Foo>> from(LocalDate in) throws ConvertionException {
            return Optional.of(Map.of("aj", new Foo(10)));
        }

        @Override
        public Optional<Map<String, Foo>> from(LocalTime in) throws ConvertionException {
            return Optional.of(Map.of("ak", new Foo(11)));
        }

        @Override
        public Optional<Map<String, Foo>> from(LocalDateTime in) throws ConvertionException {
            return Optional.of(Map.of("al", new Foo(12)));
        }

        @Override
        public Optional<Map<String, Foo>> from(OffsetTime in) throws ConvertionException {
            return Optional.of(Map.of("am", new Foo(13)));
        }

        @Override
        public Optional<Map<String, Foo>> from(OffsetDateTime in) throws ConvertionException {
            return Optional.of(Map.of("an", new Foo(14)));
        }

        @Override
        public Optional<Map<String, Foo>> from(String in) throws ConvertionException {
            return Optional.of(Map.of("ao", new Foo(15)));
        }

        @Override
        public Optional<Map<String, Foo>> from(byte[] in) throws ConvertionException {
            return Optional.of(Map.of("ap", new Foo(16)));
        }

        @Override
        public Optional<Map<String, Foo>> from(Blob in) throws ConvertionException {
            return Optional.of(Map.of("ba", new Foo(101)));
        }

        @Override
        public Optional<Map<String, Foo>> from(Clob in) throws ConvertionException {
            return Optional.of(Map.of("bb", new Foo(102)));
        }

        @Override
        public Optional<Map<String, Foo>> from(NClob in) throws ConvertionException {
            return Optional.of(Map.of("bc", new Foo(103)));
        }

        @Override
        public Optional<Map<String, Foo>> from(SQLXML in) throws ConvertionException {
            return Optional.of(Map.of("bd", new Foo(104)));
        }

        @Override
        public Optional<Map<String, Foo>> from(RowId in) throws ConvertionException {
            return Optional.of(Map.of("be", new Foo(105)));
        }

        @Override
        public Optional<Map<String, Foo>> from(java.sql.Array in) throws ConvertionException {
            return Optional.of(Map.of("bf", new Foo(106)));
        }

        @Override
        public Optional<Map<String, Foo>> from(Struct in) throws ConvertionException {
            return Optional.of(Map.of("bg", new Foo(107)));
        }

        @Override
        public Optional<Map<String, Foo>> from(Ref in) throws ConvertionException {
            return Optional.of(Map.of("bh", new Foo(108)));
        }
    }
}
