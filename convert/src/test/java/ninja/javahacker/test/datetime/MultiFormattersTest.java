package ninja.javahacker.test.datetime;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.annotimpler.convert;

import org.junit.jupiter.api.function.Executable;

public class MultiFormattersTest {

    @FunctionalInterface
    public static interface Work<A, B> {
        public B work(A input) throws DateTimeParseException;
    }

    private static String ymd2dmy(String in) {
        if (in.length() < 10 || in.charAt(4) != '-') return in;
        var s = in.substring(4, 5);
        var y = in.substring(0, 4);
        var m = in.substring(5, 6);
        var d = in.substring(7, 8);
        var r = in.substring(8);
        return d + s + m + s + y + r;
    }

    private static String ymd2mdy(String in) {
        if (in.length() < 10 || in.charAt(4) != '-') return in;
        var s = in.substring(4, 5);
        var y = in.substring(0, 4);
        var m = in.substring(5, 6);
        var d = in.substring(7, 8);
        var r = in.substring(8);
        return m + s + d + s + y + r;
    }

    private static String ymd2fmt(String in, MultiFormatters fmt) {
        return switch (fmt) {
            case DMY_DASH -> in;
            case DMY_DOT -> in.replace("-", ".");
            case DMY_SLASH -> in.replace("-", "/");
            case DMY_JOINED -> in.replace("-", "");
            case YMD_DASH -> ymd2dmy(in);
            case YMD_DOT -> ymd2dmy(in.replace("-", "."));
            case YMD_SLASH -> ymd2dmy(in.replace("-", "/"));
            case YMD_JOINED -> ymd2dmy(in.replace("-", ""));
            case MDY_DASH -> ymd2mdy(in);
            case MDY_DOT -> ymd2mdy(in.replace("-", "."));
            case MDY_SLASH -> ymd2mdy(in.replace("-", "/"));
            case MDY_JOINED -> ymd2mdy(in.replace("-", ""));
        };
    }

    private static void testNull(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt);
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getMessage());
    }

    // ---------- Falhas. ----------

    @TestFactory
    public Stream<DynamicTest> testParseInvalidThrowsConvertionException() {
        record Case(Class<?> klass, MultiFormatters fmt, Executable parse) {}
        var s = "not-a-date";
        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case(Instant.class       , fmt, () -> fmt.parseInstant       (s)),
                new Case(LocalDate.class     , fmt, () -> fmt.parseLocalDate     (s)),
                new Case(LocalDateTime.class , fmt, () -> fmt.parseLocalDateTime (s)),
                new Case(LocalTime.class     , fmt, () -> fmt.parseLocalTime     (s)),
                new Case(ZonedDateTime.class , fmt, () -> fmt.parseZonedDateTime (s)),
                new Case(OffsetDateTime.class, fmt, () -> fmt.parseOffsetDateTime(s)),
                new Case(OffsetTime.class    , fmt, () -> fmt.parseOffsetTime    (s))
        )).map(c -> DynamicTest.dynamicTest(
                "parse: " + c.klass().getSimpleName() + " (" + c.fmt().name() + ")",
                () -> Assertions.assertThrows(DateTimeParseException.class, c.parse())
        ));
    }

    // ---------- Formatação e parsing bem-sucedidos. ----------

    @TestFactory
    public Stream<DynamicNode> testFormat() {
        record Case<E>(E input, String output, MultiFormatters fmt, Work<E, String> formatter, Work<String, E> parser) {
            public DynamicNode workIt() {
                var format = DynamicTest.dynamicTest(
                    "format: " + input.getClass().getSimpleName() + " - " + input + " (" + fmt.name() + ")",
                    () -> Assertions.assertEquals(ymd2fmt(output, fmt), formatter.work(input))
                );
                var parse = DynamicTest.dynamicTest(
                    "parse: " + input.getClass().getSimpleName() + " - " + input + " (" + fmt.name() + ")",
                    () -> Assertions.assertEquals(input, parser.work(output))
                );
                return DynamicContainer.dynamicContainer("Parse and format " + output + " (" + fmt.name() + ")", Stream.of(format, parse));
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

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case<>(odt1.toInstant()      , "2024-03-10 12:34:56.123"                , fmt, s -> fmt.format(s), fmt::parseInstant),
                new Case<>(odt1.toLocalDateTime(), "2024-03-10 12:34:56.123"                , fmt, s -> fmt.format(s), fmt::parseLocalDateTime),
                new Case<>(odt1.toLocalDate()    , "2024-03-10"                             , fmt, s -> fmt.format(s), fmt::parseLocalDate),
                new Case<>(odt1.toLocalTime()    ,            "12:34:56.123"                , fmt, s -> fmt.format(s), fmt::parseLocalTime),
                new Case<>(odt1.toZonedDateTime(), "2024-03-10 12:34:56.123 +00:00"         , fmt, s -> fmt.format(s), fmt::parseZonedDateTime),
                new Case<>(odt1                  , "2024-03-10 12:34:56.123 +00:00"         , fmt, s -> fmt.format(s), fmt::parseOffsetDateTime),
                new Case<>(odt1.toOffsetTime()   ,            "12:34:56.123 +00:00"         , fmt, s -> fmt.format(s), fmt::parseOffsetTime),
                new Case<>(odt2.toInstant()      , "2025-04-11 19:57:48.987654321"          , fmt, s -> fmt.format(s), fmt::parseInstant),
                new Case<>(odt2.toLocalDateTime(), "2025-04-11 14:25:36.987654321"          , fmt, s -> fmt.format(s), fmt::parseLocalDateTime),
                new Case<>(odt2.toLocalDate()    , "2025-04-11"                             , fmt, s -> fmt.format(s), fmt::parseLocalDate),
                new Case<>(odt2.toLocalTime()    ,            "14:25:36.987654321"          , fmt, s -> fmt.format(s), fmt::parseLocalTime),
                new Case<>(odt2.toZonedDateTime(), "2025-04-11 14:25:36.987654321 -05:32:12", fmt, s -> fmt.format(s), fmt::parseZonedDateTime),
                new Case<>(odt2                  , "2025-04-11 14:25:36.987654321 -05:32:12", fmt, s -> fmt.format(s), fmt::parseOffsetDateTime),
                new Case<>(odt2.toOffsetTime()   ,            "14:25:36.987654321 -05:32:12", fmt, s -> fmt.format(s), fmt::parseOffsetTime),
                new Case<>(odt3.toInstant()      , "2026-05-12 11:52:28"                    , fmt, s -> fmt.format(s), fmt::parseInstant),
                new Case<>(odt3.toLocalDateTime(), "2026-05-12 13:57:28"                    , fmt, s -> fmt.format(s), fmt::parseLocalDateTime),
                new Case<>(odt3.toLocalDate()    , "2026-05-12"                             , fmt, s -> fmt.format(s), fmt::parseLocalDate),
                new Case<>(odt3.toLocalTime()    ,            "13:57:28"                    , fmt, s -> fmt.format(s), fmt::parseLocalTime),
                new Case<>(odt3.toZonedDateTime(), "2026-05-12 13:57:28 +02:05"             , fmt, s -> fmt.format(s), fmt::parseZonedDateTime),
                new Case<>(odt3                  , "2026-05-12 13:57:28 +02:05"             , fmt, s -> fmt.format(s), fmt::parseOffsetDateTime),
                new Case<>(odt3.toOffsetTime()   ,            "13:57:28 +02:05"             , fmt, s -> fmt.format(s), fmt::parseOffsetTime)
        )).map(Case::workIt);
    }

    // ---------- Null safety. ----------

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testParseNull() {
        record Case(Class<?> which, MultiFormatters fmt, Executable withNull) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case(LocalDate     .class, fmt, () -> fmt.parseLocalDate     (null)),
                new Case(LocalDateTime .class, fmt, () -> fmt.parseLocalDateTime (null)),
                new Case(LocalTime     .class, fmt, () -> fmt.parseLocalTime     (null)),
                new Case(OffsetDateTime.class, fmt, () -> fmt.parseOffsetDateTime(null)),
                new Case(OffsetTime    .class, fmt, () -> fmt.parseOffsetTime    (null)),
                new Case(ZonedDateTime .class, fmt, () -> fmt.parseZonedDateTime (null)),
                new Case(Instant       .class, fmt, () -> fmt.parseInstant       (null))
        )).map(c -> DynamicTest.dynamicTest(
                "parse: " + c.which().getSimpleName() + " (" + c.fmt().name() + ")",
                () -> testNull("s", c.withNull())
        ));
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testFormatNull() {
        record Case(Class<?> which, MultiFormatters fmt, Executable withNull) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case(LocalDate     .class, fmt, () -> fmt.format((LocalDate     ) null)),
                new Case(LocalDateTime .class, fmt, () -> fmt.format((LocalDateTime ) null)),
                new Case(LocalTime     .class, fmt, () -> fmt.format((LocalTime     ) null)),
                new Case(OffsetDateTime.class, fmt, () -> fmt.format((OffsetDateTime) null)),
                new Case(OffsetTime    .class, fmt, () -> fmt.format((OffsetTime    ) null)),
                new Case(ZonedDateTime .class, fmt, () -> fmt.format((ZonedDateTime ) null)),
                new Case(Instant       .class, fmt, () -> fmt.format((Instant       ) null))
        )).map(c -> DynamicTest.dynamicTest(
                "format: " + c.which().getSimpleName() + " (" + c.fmt().name() + ")",
                () -> testNull("s", c.withNull())
        ));
    }
}