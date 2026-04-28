package ninja.javahacker.test.annotimpler.convert;

import ninja.javahacker.test.ForTests;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.convert;

import org.junit.jupiter.api.function.Executable;

public class MultiFormattersTest {

    @FunctionalInterface
    public static interface Work<A, B> {
        public B work(A input) throws Exception;
    }

    // ---------- Parsing bem-sucedido ----------

    @Test
    public void parseInstant_withOffset() throws Exception {
        var s = "2024-03-10 12:34:56 +00:00";
        var result = MultiFormatters.parseInstant(s);
        Assertions.assertEquals(Instant.parse("2024-03-10T12:34:56Z"), result);
    }

    @Test
    public void parseInstant_withoutOffset_fallbackUTC() throws Exception {
        var s = "2024-03-10 12:34:56";
        var result = MultiFormatters.parseInstant(s);
        Assertions.assertEquals(Instant.parse("2024-03-10T12:34:56Z"), result);
    }

    @Test
    public void parseOffsetDateTime_withOffset() throws Exception {
        var s = "2024-03-10 12:34:56 +02:00";
        var result = MultiFormatters.parseOffsetDateTime(s);
        Assertions.assertEquals(ZoneOffset.ofHours(2), result.getOffset());
    }

    @Test
    public void parseOffsetDateTime_withoutOffset_fallbackUTC() throws Exception {
        var s = "2024-03-10 12:34:56";
        var result = MultiFormatters.parseOffsetDateTime(s);
        Assertions.assertEquals(ZoneOffset.UTC, result.getOffset());
    }

    @Test
    public void parseLocalDateTime_fromDateOnly() throws Exception {
        var s = "2024-03-10";
        var result = MultiFormatters.parseLocalDateTime(s);
        Assertions.assertEquals(LocalDateTime.of(2024, 3, 10, 0, 0), result);
    }

    @Test
    public void parseOffsetTime_withOffset() throws Exception {
        var s = "12:34:56 +03:00";
        var result = MultiFormatters.parseOffsetTime(s);
        Assertions.assertEquals(ZoneOffset.ofHours(3), result.getOffset());
    }

    @Test
    public void parseOffsetTime_withoutOffset_fallbackUTC() throws Exception {
        var s = "12:34:56";
        var result = MultiFormatters.parseOffsetTime(s);
        Assertions.assertEquals(ZoneOffset.UTC, result.getOffset());
    }

    // ---------- Falhas ----------

    @TestFactory
    public Stream<DynamicTest> testParseInvalidThrowsConvertionException() {
        record Case(Class<?> klass, Executable parse) {}
        var s = "not-a-date";
        return Stream.of(
                new Case(Instant.class       , () -> MultiFormatters.parseInstant       (s)),
                new Case(LocalDate.class     , () -> MultiFormatters.parseLocalDate     (s)),
                new Case(LocalDateTime.class , () -> MultiFormatters.parseLocalDateTime (s)),
                new Case(LocalTime.class     , () -> MultiFormatters.parseLocalTime     (s)),
                new Case(ZonedDateTime.class , () -> MultiFormatters.parseZonedDateTime (s)),
                new Case(OffsetDateTime.class, () -> MultiFormatters.parseOffsetDateTime(s)),
                new Case(OffsetTime.class    , () -> MultiFormatters.parseOffsetTime    (s))
        ).map(c -> DynamicTest.dynamicTest("parse: " + c.klass().getSimpleName(), () -> {
            var ex = Assertions.assertThrows(ConvertionException.class, c.parse());
            Assertions.assertTrue(ex.getSuppressed().length > 0);
        }));
    }

    // ---------- Formatação ----------

    @TestFactory
    public Stream<DynamicTest> testFormat() {
        record Case<E>(E input, String output, Work<E, String> work) {
            public DynamicTest workIt() {
                return DynamicTest.dynamicTest(
                    "parse: " + input.getClass().getSimpleName() + " - " + input,
                    () -> Assertions.assertEquals(output(), work.work(input()))
                );
            }
        }

        var odt1 = OffsetDateTime.of(
                2024, 3, 10,
                12, 34, 56, 123_000_000,
                ZoneOffset.UTC
        );

        var odt2 = OffsetDateTime.of(
                2025, 4, 11,
                14, 25, 36, 987_654_321,
                ZoneOffset.ofHoursMinutesSeconds(-5, -32, -12)
        );

        var odt3 = OffsetDateTime.of(
                2026, 5, 12,
                13, 57, 28, 0,
                ZoneOffset.ofHoursMinutes(2, 5)
        );

        return Stream.of(
                new Case<>(odt1.toInstant()       , "2024-03-10 12:34:56.123"                , s -> MultiFormatters.format(s)),
                new Case<>(odt1.toLocalDateTime() , "2024-03-10 12:34:56.123"                , s -> MultiFormatters.format(s)),
                new Case<>(odt1.toLocalDate()     , "2024-03-10"                             , s -> MultiFormatters.format(s)),
                new Case<>(odt1.toLocalTime()     ,            "12:34:56.123"                , s -> MultiFormatters.format(s)),
                new Case<>(odt1.toZonedDateTime() , "2024-03-10 12:34:56.123 +00:00"         , s -> MultiFormatters.format(s)),
                new Case<>(odt1                   , "2024-03-10 12:34:56.123 +00:00"         , s -> MultiFormatters.format(s)),
                new Case<>(odt1.toOffsetTime()    ,            "12:34:56.123 +00:00"         , s -> MultiFormatters.format(s)),
                new Case<>(odt2.toInstant()       , "2025-04-11 19:57:48.987654321"          , s -> MultiFormatters.format(s)),
                new Case<>(odt2.toLocalDateTime() , "2025-04-11 14:25:36.987654321"          , s -> MultiFormatters.format(s)),
                new Case<>(odt2.toLocalDate()     , "2025-04-11"                             , s -> MultiFormatters.format(s)),
                new Case<>(odt2.toLocalTime()     ,            "14:25:36.987654321"          , s -> MultiFormatters.format(s)),
                new Case<>(odt2.toZonedDateTime() , "2025-04-11 14:25:36.987654321 -05:32:12", s -> MultiFormatters.format(s)),
                new Case<>(odt2                   , "2025-04-11 14:25:36.987654321 -05:32:12", s -> MultiFormatters.format(s)),
                new Case<>(odt2.toOffsetTime()    ,            "14:25:36.987654321 -05:32:12", s -> MultiFormatters.format(s)),
                new Case<>(odt3.toInstant()       , "2026-05-12 11:52:28"                    , s -> MultiFormatters.format(s)),
                new Case<>(odt3.toLocalDateTime() , "2026-05-12 13:57:28"                    , s -> MultiFormatters.format(s)),
                new Case<>(odt3.toLocalDate()     , "2026-05-12"                             , s -> MultiFormatters.format(s)),
                new Case<>(odt3.toLocalTime()     ,            "13:57:28"                    , s -> MultiFormatters.format(s)),
                new Case<>(odt3.toZonedDateTime() , "2026-05-12 13:57:28 +02:05"             , s -> MultiFormatters.format(s)),
                new Case<>(odt3                   , "2026-05-12 13:57:28 +02:05"             , s -> MultiFormatters.format(s)),
                new Case<>(odt3.toOffsetTime()    ,            "13:57:28 +02:05"             , s -> MultiFormatters.format(s))
        ).map(Case::workIt);
    }

    // ---------- Testes dinâmicos para múltiplos formatos ----------

    @TestFactory
    public Stream<DynamicTest> parseVariousValidDateTimes() {
        record Case(String input, LocalDateTime expected) {}

        var cases = List.of(
                new Case("2024-03-10 12:34:56", LocalDateTime.of(2024, 3, 10, 12, 34, 56)),
                new Case("2024-03-10 12:34"   , LocalDateTime.of(2024, 3, 10, 12, 34    )),
                new Case("2024-03-10"         , LocalDateTime.of(2024, 3, 10,  0,  0    ))
        );

        return cases.stream().map(c ->
                DynamicTest.dynamicTest("parse: " + c.input, () -> {
                    var result = MultiFormatters.parseLocalDateTime(c.input);
                    Assertions.assertEquals(c.expected, result);
                })
        );
    }

    @TestFactory
    public Stream<DynamicTest> parseVariousTimes() {
        var inputs = List.of(
                "12:34:56",
                "12:34",
                "12:34:56.123"
        );

        return inputs.stream().map(input ->
                DynamicTest.dynamicTest("parse time: " + input, () -> {
                    var result = MultiFormatters.parseLocalTime(input);
                    Assertions.assertNotNull(result);
                })
        );
    }

    // ---------- Null safety ----------

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testParseNull() {
        record Case(Class<?> which, Executable withNull) {}

        return Stream.of(
                new Case(LocalDate     .class, () -> MultiFormatters.parseLocalDate     (null)),
                new Case(LocalDateTime .class, () -> MultiFormatters.parseLocalDateTime (null)),
                new Case(LocalTime     .class, () -> MultiFormatters.parseLocalTime     (null)),
                new Case(OffsetDateTime.class, () -> MultiFormatters.parseOffsetDateTime(null)),
                new Case(OffsetTime    .class, () -> MultiFormatters.parseOffsetTime    (null)),
                new Case(ZonedDateTime .class, () -> MultiFormatters.parseZonedDateTime (null)),
                new Case(Instant       .class, () -> MultiFormatters.parseInstant       (null))
        ).map(c -> DynamicTest.dynamicTest(
                "parse: " + c.which().getSimpleName(),
                () -> ForTests.testNull("s", c.withNull())
        ));
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testFormatNull() {
        record Case(Class<?> which, Executable withNull) {}

        return Stream.of(
                new Case(LocalDate     .class, () -> MultiFormatters.format((LocalDate     ) null)),
                new Case(LocalDateTime .class, () -> MultiFormatters.format((LocalDateTime ) null)),
                new Case(LocalTime     .class, () -> MultiFormatters.format((LocalTime     ) null)),
                new Case(OffsetDateTime.class, () -> MultiFormatters.format((OffsetDateTime) null)),
                new Case(OffsetTime    .class, () -> MultiFormatters.format((OffsetTime    ) null)),
                new Case(ZonedDateTime .class, () -> MultiFormatters.format((ZonedDateTime ) null)),
                new Case(Instant       .class, () -> MultiFormatters.format((Instant       ) null))
        ).map(c -> DynamicTest.dynamicTest(
                "format: " + c.which().getSimpleName(),
                () -> ForTests.testNull("s", c.withNull())
        ));
    }
}