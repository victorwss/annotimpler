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
        var end = in.indexOf(' ');
        if (end < 0) end = in.length();

        var p = in.substring(0, end);
        var r = in.substring(end);

        var s1 = p.indexOf('_');
        if (s1 < 0) return in;

        var y = p.substring(0, s1);
        var s2 = p.indexOf('_', s1 + 1);

        if (s2 < 0) {
            var md = p.substring(s1 + 1);
            return md + "_" + y + r;
        }

        var m = p.substring(s1 + 1, s2);
        var d = p.substring(s2 + 1, end);

        return d + "_" + m + "_" + y + r;
    }

    private static String ymd2mdy(String in) {
        var end = in.indexOf(' ');
        if (end < 0) end = in.length();

        var p = in.substring(0, end);
        var r = in.substring(end);

        var s1 = p.indexOf('_');
        if (s1 < 0) return in;

        var y = p.substring(0, s1);
        var s2 = p.indexOf('_', s1 + 1);

        if (s2 < 0) {
            var md = p.substring(s1 + 1);
            return md + "_" + y + r;
        }

        var m = p.substring(s1 + 1, s2);
        var d = p.substring(s2 + 1, end);

        return m + "_" + d + "_" + y + r;
    }

    private static String ymd2iso(String in) {
        var end = in.indexOf(' ');
        if (end < 0) end = in.length();

        var p = in.substring(0, end);
        var r = in.substring(end);

        var s1 = p.indexOf('_');
        if (s1 < 0) return in.replace(" +", "+").replace(" -", "-").replace("+00:00", "Z").replace(" ", "T");

        var y = p.substring(0, s1);
        var s2 = p.indexOf('_', s1 + 1);

        if (s2 < 0) {
            var md = p.substring(s1 + 1);
            return md + "_" + y + r.replace(" +", "+").replace(" -", "-").replace("+00:00", "Z").replace(" ", "T");
        }

        var m = p.substring(s1 + 1, s2);
        var d = p.substring(s2 + 1, end);

        return y + "_" + m + "_" + d + r.replace(" +", "+").replace(" -", "-").replace("+00:00", "Z").replace(" ", "T");
    }

    private static String ymd2fmt(String in, MultiFormatters fmt) {
        return switch (fmt) {
            case YMD_DASH -> in.replace("_", "-");
            case YMD_DOT -> in.replace("_", ".");
            case YMD_SLASH -> in.replace("_", "/");
            case DMY_DASH -> ymd2dmy(in).replace("_", "-");
            case DMY_DOT -> ymd2dmy(in).replace("_", ".");
            case DMY_SLASH -> ymd2dmy(in).replace("_", "/");
            case MDY_DASH -> ymd2mdy(in).replace("_", "-");
            case MDY_DOT -> ymd2mdy(in).replace("_", ".");
            case MDY_SLASH -> ymd2mdy(in).replace("_", "/");
            case ISO_8601 -> ymd2iso(in).replace("_", "-");
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
    public Stream<DynamicNode> testParseAndFormat() {
        record Case<E>(E obj, String text, MultiFormatters fmt, Work<E, String> formatter, Work<String, E> parser) {
            public DynamicNode workIt() {
                var textOk = ymd2fmt(text, fmt);
                var format = DynamicTest.dynamicTest(
                    "format: " + obj.getClass().getSimpleName() + " - " + obj + " (" + fmt.name() + ")",
                    () -> Assertions.assertEquals(textOk, formatter.work(obj))
                );
                var parse = DynamicTest.dynamicTest(
                    "parse: " + obj.getClass().getSimpleName() + " - " + text + " (" + fmt.name() + ")",
                    () -> Assertions.assertEquals(obj, parser.work(textOk))
                );
                return DynamicContainer.dynamicContainer("Parse and format " + textOk + " (" + fmt.name() + ")", Stream.of(format, parse));
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
                new Case<>(odt1.toInstant()      , "2024_03_10 12:34:56.123"                , fmt, s -> fmt.format(s), fmt::parseInstant),
                new Case<>(odt1.toLocalDateTime(), "2024_03_10 12:34:56.123"                , fmt, s -> fmt.format(s), fmt::parseLocalDateTime),
                new Case<>(odt1.toLocalDate()    , "2024_03_10"                             , fmt, s -> fmt.format(s), fmt::parseLocalDate),
                new Case<>(odt1.toLocalTime()    ,            "12:34:56.123"                , fmt, s -> fmt.format(s), fmt::parseLocalTime),
                new Case<>(odt1.toZonedDateTime(), "2024_03_10 12:34:56.123 +00:00"         , fmt, s -> fmt.format(s), fmt::parseZonedDateTime),
                new Case<>(odt1                  , "2024_03_10 12:34:56.123 +00:00"         , fmt, s -> fmt.format(s), fmt::parseOffsetDateTime),
                new Case<>(odt1.toOffsetTime()   ,            "12:34:56.123 +00:00"         , fmt, s -> fmt.format(s), fmt::parseOffsetTime),
                new Case<>(odt2.toInstant()      , "2025_04_11 19:57:48.987654321"          , fmt, s -> fmt.format(s), fmt::parseInstant),
                new Case<>(odt2.toLocalDateTime(), "2025_04_11 14:25:36.987654321"          , fmt, s -> fmt.format(s), fmt::parseLocalDateTime),
                new Case<>(odt2.toLocalDate()    , "2025_04_11"                             , fmt, s -> fmt.format(s), fmt::parseLocalDate),
                new Case<>(odt2.toLocalTime()    ,            "14:25:36.987654321"          , fmt, s -> fmt.format(s), fmt::parseLocalTime),
                new Case<>(odt2.toZonedDateTime(), "2025_04_11 14:25:36.987654321 -05:32:12", fmt, s -> fmt.format(s), fmt::parseZonedDateTime),
                new Case<>(odt2                  , "2025_04_11 14:25:36.987654321 -05:32:12", fmt, s -> fmt.format(s), fmt::parseOffsetDateTime),
                new Case<>(odt2.toOffsetTime()   ,            "14:25:36.987654321 -05:32:12", fmt, s -> fmt.format(s), fmt::parseOffsetTime),
                new Case<>(odt3.toInstant()      , "2026_05_12 11:52:28"                    , fmt, s -> fmt.format(s), fmt::parseInstant),
                new Case<>(odt3.toLocalDateTime(), "2026_05_12 13:57:28"                    , fmt, s -> fmt.format(s), fmt::parseLocalDateTime),
                new Case<>(odt3.toLocalDate()    , "2026_05_12"                             , fmt, s -> fmt.format(s), fmt::parseLocalDate),
                new Case<>(odt3.toLocalTime()    ,            "13:57:28"                    , fmt, s -> fmt.format(s), fmt::parseLocalTime),
                new Case<>(odt3.toZonedDateTime(), "2026_05_12 13:57:28 +02:05"             , fmt, s -> fmt.format(s), fmt::parseZonedDateTime),
                new Case<>(odt3                  , "2026_05_12 13:57:28 +02:05"             , fmt, s -> fmt.format(s), fmt::parseOffsetDateTime),
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
                () -> testNull("input", c.withNull())
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
                () -> testNull("input", c.withNull())
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testInvalidRangesDateAndTimeLimits() {

        record Case(String name, String input, MultiFormatters fmt, Executable exec) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
            new Case("day 0"               , "2024_01_00 12:34:56"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_00 12:34:56"           , fmt))),
            new Case("day 32"              , "2024_01_32 12:34:56"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_32 12:34:56"           , fmt))),
            new Case("month 0"             , "2024_00_10 12:34:56"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_00_10 12:34:56"           , fmt))),
            new Case("month 13"            , "2024_13_10 12:34:56"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_13_10 12:34:56"           , fmt))),
            new Case("hour 24"             , "2024_01_10 24:00:00"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 24:00:00"           , fmt))),
            new Case("minute 60"           , "2024_01_10 12:60:00"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12:60:00"           , fmt))),
            new Case("second 60"           , "2024_01_10 12:34:60"           , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12:34:60"           , fmt))),
            new Case("nano > 9 digits"     , "2024_01_10 12:34:56.1234567890", fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12:34:56.1234567890", fmt))),
            new Case("dot without fraction", "2024_01_10 12:34:56."          , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12:34:56."          , fmt)))
        )).map(c -> DynamicTest.dynamicTest(
                c.name + " (" + c.fmt.name() + ")",
                () -> Assertions.assertThrows(DateTimeParseException.class, c.exec)
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testInvalidCalendarDates() {

        record Case(String name, String input, MultiFormatters fmt) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
            new Case("31 feb"     , "2024_02_31", fmt),
            new Case("31 apr"     , "2024_04_31", fmt),
            new Case("31 jun"     , "2024_06_31", fmt),
            new Case("31 sep"     , "2024_09_31", fmt),
            new Case("31 nov"     , "2024_11_31", fmt),
            new Case("30 feb"     , "2024_02_30", fmt),
            new Case("29 feb 2023", "2023_02_29", fmt),
            new Case("29 feb 1900", "1900_02_29", fmt),
            new Case("29 feb 2100", "2100_02_29", fmt)
        )).map(c -> DynamicTest.dynamicTest(
                c.name + " (" + c.fmt.name() + ")",
                () -> Assertions.assertThrows(DateTimeParseException.class, () -> c.fmt.parseLocalDate(ymd2fmt(c.input, c.fmt)))
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testValidLeapYear() {
        return Stream.of(MultiFormatters.values()).map(fmt ->
            DynamicTest.dynamicTest("29 feb 2024 valid (" + fmt.name() + ")", () -> {
                var parsed = fmt.parseLocalDate(ymd2fmt("2024_02_29", fmt));
                Assertions.assertEquals(29, parsed.getDayOfMonth());
            })
        );
    }

    private static List<String> mutatePartsIn(String input) {
        var dot = input.indexOf('.');
        var spc = input.indexOf(' ', dot + 1);
        if (spc < 0) spc = input.length();
        var mutations = new ArrayList<String>();
        for (int k = 1; k <= 5; k++) {
            for (int i = 0; i < input.length() - k; i++) {
                if (dot > 0 && i > dot && i + k <= spc) continue;
                mutations.add(input.substring(0, i) + input.substring(i + k));
            }
        }
        for (int i = 0; i < input.length(); i++) {
            var a = input.substring(0, i);
            var b = input.substring(i);
            mutations.add(a + "0" + b);
            mutations.add(a + " " + b);
            mutations.add(a + ":" + b);
            mutations.add(a + "_" + b);
        }
        return mutations;
    }

    private static Set<String> mutateParts(String... input) {
        return Stream.of(input).map(MultiFormattersTest::mutatePartsIn).flatMap(List::stream).collect(Collectors.toSet());
    }

    @TestFactory
    public Stream<DynamicTest> testIncompleteInputs() {
        record Case(MultiFormatters fmt, String input, String n, BiFunction<MultiFormatters, String, ?> recv) {
            public DynamicTest test() {
                return DynamicTest.dynamicTest(
                        "incomplete: " + input + " (" + fmt.name() + " - " + n + ")",
                        () -> Assertions.assertThrows(DateTimeParseException.class, () -> recv.apply(fmt, ymd2fmt(input, fmt)))
                );
            }
        }

        var inputsDTZ = mutateParts(
                "2024_11_10 12:34:56.123456789 +00:00", "2024_11_10 12:34:56.123456789", "12:34:56.123456789 +00:00",
                "12:34:56.123456789", "2024_11_10"
        );

        Map<String, BiFunction<MultiFormatters, String, ?>> funcs = Map.of(
                "LocalTime", MultiFormatters::parseLocalTime,
                "LocalDate", MultiFormatters::parseLocalDate,
                "LocalDateTime", MultiFormatters::parseLocalDateTime,
                "Instant", MultiFormatters::parseInstant,
                "OffsetDateTime", MultiFormatters::parseOffsetDateTime,
                "ZonedDateTime", MultiFormatters::parseZonedDateTime,
                "OffsetTime", MultiFormatters::parseOffsetTime
        );

        return Stream.of(MultiFormatters.values())
                .flatMap(fmt -> inputsDTZ
                        .stream()
                        .flatMap(input -> funcs
                                .entrySet()
                                .stream()
                                .map(e -> new Case(fmt, input, e.getKey(), e.getValue()))
                        )
                )
                .filter(c -> c.fmt != MultiFormatters.ISO_8601
                        || (!c.input.contains("9+0") && !c.input.contains("8+0") && !c.input.contains("7+0") && !c.input.contains("6+0") && !c.input.contains("5+0")))
                .map(Case::test);
    }
}