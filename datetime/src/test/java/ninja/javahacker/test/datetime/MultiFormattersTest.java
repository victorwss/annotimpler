package ninja.javahacker.test.datetime;

import module java.base;
import module org.junit.jupiter.api;
import module ninja.javahacker.datetime;

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
            var md = p.substring(s1 + 1).replace("_", "-");
            return md + "-" + y + r.replace(" +", "+").replace(" -", "-").replace("+00:00", "Z").replace(" ", "T");
        }

        var m = p.substring(s1 + 1, s2);
        var d = p.substring(s2 + 1, end);

        return y + "-" + m + "-" + d + r.replace(" +", "+").replace(" -", "-").replace("+00:00", "Z").replace(" ", "T");
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
            case ISO_8601 -> ymd2iso(in);
        };
    }

    private static void testNull(String paramName, Executable runIt) {
        var ex = Assertions.assertThrows(IllegalArgumentException.class, runIt);
        Assertions.assertEquals(paramName + " is marked non-null but is null", ex.getMessage());
    }

    // ---------- Failures. ----------

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
                "parse: " + c.klass().getSimpleName() + " - not-a-date (" + c.fmt().name() + ")",
                () -> Assertions.assertThrows(DateTimeParseException.class, c.parse())
        ));
    }

    // ---------- Successful formating and parsing. ----------

    private static final OffsetDateTime ODT1 = OffsetDateTime.of(
            2024, 3, 10,
            12, 34, 56, 123_000_000,
            ZoneOffset.UTC
    );

    private static final OffsetDateTime ODT2 = OffsetDateTime.of(
            2025, 4, 11,
            14, 25, 36, 987_654_321,
            ZoneOffset.ofHoursMinutesSeconds(-5, -32, -12)
    );

    private static final OffsetDateTime ODT3 = OffsetDateTime.of(
            2026, 5, 12,
            13, 57, 28, 0,
            ZoneOffset.ofHoursMinutes(2, 5)
    );

    record ParseFormatCase<E>(E obj, String textParse, String textFormat, MultiFormatters fmt, Work<E, String> formatter, Work<String, E> parser) {
        public DynamicNode workIt() {
            var textOkFormat = ymd2fmt(textFormat, fmt);
            var textOkParse = ymd2fmt(textParse, fmt);
            var k = obj.getClass().getSimpleName();
            var format = DynamicTest.dynamicTest(
                "format: " + k + " - " + obj + " (" + fmt.name() + ")",
                () -> Assertions.assertEquals(textOkFormat, formatter.work(obj))
            );
            var parse = DynamicTest.dynamicTest(
                "parse: " + k + " - " + textOkParse + " (" + fmt.name() + ")",
                () -> Assertions.assertEquals(obj, parser.work(textOkParse))
            );
            return DynamicContainer.dynamicContainer("Parse and format: " + k + " - " + textOkParse + " (" + fmt.name() + ")", Stream.of(format, parse));
        }

        public ParseFormatCase(E obj, String text, MultiFormatters fmt, Work<E, String> formatter, Work<String, E> parser) {
            this(obj, text, text, fmt, formatter, parser);
        }
    }

    @TestFactory
    public Stream<DynamicNode> testParseAndFormat() {
        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new ParseFormatCase<>(ODT1.toLocalDate()    , "2024_03_10"                             , fmt, fmt::format, fmt::parseLocalDate     ),
                new ParseFormatCase<>(ODT2.toLocalDate()    , "2025_04_11"                             , fmt, fmt::format, fmt::parseLocalDate     ),
                new ParseFormatCase<>(ODT3.toLocalDate()    , "2026_05_12"                             , fmt, fmt::format, fmt::parseLocalDate     ),
                new ParseFormatCase<>(ODT1.toLocalDateTime(), "2024_03_10 12:34:56.123"                , fmt, fmt::format, fmt::parseLocalDateTime ),
                new ParseFormatCase<>(ODT2.toLocalDateTime(), "2025_04_11 14:25:36.987654321"          , fmt, fmt::format, fmt::parseLocalDateTime ),
                new ParseFormatCase<>(ODT3.toLocalDateTime(), "2026_05_12 13:57:28"                    , fmt, fmt::format, fmt::parseLocalDateTime ),
                new ParseFormatCase<>(ODT1.toInstant()      , "2024_03_10 12:34:56.123"                , fmt, fmt::format, fmt::parseInstant       ),
                new ParseFormatCase<>(ODT2.toInstant()      , "2025_04_11 19:57:48.987654321"          , fmt, fmt::format, fmt::parseInstant       ),
                new ParseFormatCase<>(ODT3.toInstant()      , "2026_05_12 11:52:28"                    , fmt, fmt::format, fmt::parseInstant       ),
                new ParseFormatCase<>(ODT1                  , "2024_03_10 12:34:56.123 +00:00"         , fmt, fmt::format, fmt::parseOffsetDateTime),
                new ParseFormatCase<>(ODT2                  , "2025_04_11 14:25:36.987654321 -05:32:12", fmt, fmt::format, fmt::parseOffsetDateTime),
                new ParseFormatCase<>(ODT3                  , "2026_05_12 13:57:28 +02:05"             , fmt, fmt::format, fmt::parseOffsetDateTime),
                new ParseFormatCase<>(ODT1.toZonedDateTime(), "2024_03_10 12:34:56.123 +00:00"         , fmt, fmt::format, fmt::parseZonedDateTime ),
                new ParseFormatCase<>(ODT2.toZonedDateTime(), "2025_04_11 14:25:36.987654321 -05:32:12", fmt, fmt::format, fmt::parseZonedDateTime ),
                new ParseFormatCase<>(ODT3.toZonedDateTime(), "2026_05_12 13:57:28 +02:05"             , fmt, fmt::format, fmt::parseZonedDateTime ),
                new ParseFormatCase<>(ODT1.toLocalTime()    ,            "12:34:56.123"                , fmt, fmt::format, fmt::parseLocalTime     ),
                new ParseFormatCase<>(ODT2.toLocalTime()    ,            "14:25:36.987654321"          , fmt, fmt::format, fmt::parseLocalTime     ),
                new ParseFormatCase<>(ODT3.toLocalTime()    ,            "13:57:28"                    , fmt, fmt::format, fmt::parseLocalTime     ),
                new ParseFormatCase<>(ODT1.toOffsetTime()   ,            "12:34:56.123 +00:00"         , fmt, fmt::format, fmt::parseOffsetTime    ),
                new ParseFormatCase<>(ODT2.toOffsetTime()   ,            "14:25:36.987654321 -05:32:12", fmt, fmt::format, fmt::parseOffsetTime    ),
                new ParseFormatCase<>(ODT3.toOffsetTime()   ,            "13:57:28 +02:05"             , fmt, fmt::format, fmt::parseOffsetTime    )
        )).map(ParseFormatCase::workIt);
    }

    @TestFactory
    public Stream<DynamicNode> testParseAndFormatDifferent() {
        var odt4 = ODT2.toLocalDateTime().atOffset(ZoneOffset.UTC);
        var odt5 = ODT2.toLocalDate().atStartOfDay(ZoneOffset.UTC);
        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new ParseFormatCase<>(odt4.toLocalDate()    , "2025_04_11 14:25:36.987654321"          , "2025_04_11"                             , fmt, fmt::format, fmt::parseLocalDate     ),
                new ParseFormatCase<>(ODT2.toLocalDate()    , "2025_04_11 14:25:36.987654321 -05:32:12", "2025_04_11"                             , fmt, fmt::format, fmt::parseLocalDate     ),
                new ParseFormatCase<>(ODT2.toLocalDateTime(), "2025_04_11 14:25:36.987654321 -05:32:12", "2025_04_11 14:25:36.987654321"          , fmt, fmt::format, fmt::parseLocalDateTime ),
                new ParseFormatCase<>(ODT2.toInstant()      , "2025_04_11 14:25:36.987654321 -05:32:12", "2025_04_11 19:57:48.987654321"          , fmt, fmt::format, fmt::parseInstant       ),
                new ParseFormatCase<>(ODT2.toLocalTime()    , "2025_04_11 14:25:36.987654321 -05:32:12",            "14:25:36.987654321"          , fmt, fmt::format, fmt::parseLocalTime     ),
                new ParseFormatCase<>(odt4.toLocalTime()    , "2025_04_11 14:25:36.987654321"          ,            "14:25:36.987654321"          , fmt, fmt::format, fmt::parseLocalTime     ),
                new ParseFormatCase<>(ODT2.toLocalTime()    ,            "14:25:36.987654321 -05:32:12",            "14:25:36.987654321"          , fmt, fmt::format, fmt::parseLocalTime     ),
                new ParseFormatCase<>(ODT2.toOffsetTime()   , "2025_04_11 14:25:36.987654321 -05:32:12",            "14:25:36.987654321 -05:32:12", fmt, fmt::format, fmt::parseOffsetTime    ),

                new ParseFormatCase<>(odt4.toOffsetTime()   , "2025_04_11 14:25:36.987654321"          ,            "14:25:36.987654321 +00:00"   , fmt, fmt::format, fmt::parseOffsetTime    ),
                new ParseFormatCase<>(odt4.toOffsetTime()   ,            "14:25:36.987654321"          ,            "14:25:36.987654321 +00:00"   , fmt, fmt::format, fmt::parseOffsetTime    ),
                new ParseFormatCase<>(odt5.toLocalDateTime(), "2025_04_11"                             , "2025_04_11 00:00:00"                    , fmt, fmt::format, fmt::parseLocalDateTime ),
                new ParseFormatCase<>(odt5.toInstant()      , "2025_04_11"                             , "2025_04_11 00:00:00"                    , fmt, fmt::format, fmt::parseInstant       ),
                new ParseFormatCase<>(odt4                  , "2025_04_11 14:25:36.987654321"          , "2025_04_11 14:25:36.987654321 +00:00"   , fmt, fmt::format, fmt::parseOffsetDateTime),
                new ParseFormatCase<>(odt4.toZonedDateTime(), "2025_04_11 14:25:36.987654321"          , "2025_04_11 14:25:36.987654321 +00:00"   , fmt, fmt::format, fmt::parseZonedDateTime )
        )).map(ParseFormatCase::workIt);
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
                "parse: " + c.which().getSimpleName() + " - [null] (" + c.fmt().name() + ")",
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

    // ---------- Invalid data tests. ----------

    @TestFactory
    public Stream<DynamicTest> testInvalidRangesDateAndTimeLimits() {

        record Case(String name, MultiFormatters fmt, Executable exec) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case("d day 0"                  , fmt, () -> fmt.parseLocalDate     (ymd2fmt( "2024_01_00"                           , fmt))),
                new Case("dt day 0"                 , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_00 12:34:56"                  , fmt))),
                new Case("dtz day 0"                , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_00 12:34:56 +10:50"           , fmt))),

                new Case("d day 32"                 , fmt, () -> fmt.parseLocalDate     (ymd2fmt( "2024_01_32"                           , fmt))),
                new Case("dt day 32"                , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_32 12:34:56"                  , fmt))),
                new Case("dtz day 32"               , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_32 12:34:56 +10:50"           , fmt))),

                new Case("d month 0"                , fmt, () -> fmt.parseLocalDate     (ymd2fmt( "2024_00_10"                           , fmt))),
                new Case("dt month 0"               , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_00_10 12:34:56"                  , fmt))),
                new Case("dtz month 0"              , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_00_10 12:34:56 +10:50"           , fmt))),

                new Case("d month 13"               , fmt, () -> fmt.parseLocalDate     (ymd2fmt( "2024_13_10"                           , fmt))),
                new Case("dt month 13"              , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_13_10 12:34:56"                  , fmt))),
                new Case("dtz month 13"             , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_13_10 12:34:56 +10:50"           , fmt))),

                new Case("d negative year"          , fmt, () -> fmt.parseLocalDate     (ymd2fmt("-2024_01_10"                           , fmt))),
                new Case("dt negative year"         , fmt, () -> fmt.parseLocalDateTime (ymd2fmt("-2024_01_10 12:34:56"                  , fmt))),
                new Case("dtz negative year"        , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt("-2024_01_10 12:34:56 +10:50"           , fmt))),

                new Case("t hour 24"                , fmt, () -> fmt.parseLocalTime     (ymd2fmt(            "24:00:00"                  , fmt))),
                new Case("dt hour 24"               , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_10 24:00:00"                  , fmt))),
                new Case("dtz hour 24"              , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_10 24:00:00 +10:50"           , fmt))),
                new Case("tz hour 24"               , fmt, () -> fmt.parseOffsetTime    (ymd2fmt(            "24:00:00 +10:50"           , fmt))),

                new Case("t minute 60"              , fmt, () -> fmt.parseLocalTime     (ymd2fmt(            "12:60:00"                  , fmt))),
                new Case("dt minute 60"             , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_10 12:60:00"                  , fmt))),
                new Case("dtz minute 60"            , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_10 12:60:00 +10:50"           , fmt))),
                new Case("tz minute 60"             , fmt, () -> fmt.parseOffsetTime    (ymd2fmt(            "12:60:00 +10:50"           , fmt))),

                new Case("t second 60"              , fmt, () -> fmt.parseLocalTime     (ymd2fmt(            "12:34:60"                  , fmt))),
                new Case("dt second 60"             , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_10 12:34:60"                  , fmt))),
                new Case("dtz second 60"            , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_10 12:34:60 +10:50"           , fmt))),
                new Case("tz second 60"             , fmt, () -> fmt.parseOffsetTime    (ymd2fmt(            "12:34:60 +10:50"           , fmt))),

                new Case("t nano > 9 digits"        , fmt, () -> fmt.parseLocalTime     (ymd2fmt(            "12:34:56.1234567890"       , fmt))),
                new Case("dt nano > 9 digits"       , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_10 12:34:56.1234567890"       , fmt))),
                new Case("dtz nano > 9 digits"      , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_10 12:34:56.1234567890 +10:50", fmt))),
                new Case("tz nano > 9 digits"       , fmt, () -> fmt.parseOffsetTime    (ymd2fmt(            "12:34:56.1234567890 +10:50", fmt))),

                new Case("t dot without fraction"   , fmt, () -> fmt.parseLocalTime     (ymd2fmt(            "12:34:56."                 , fmt))),
                new Case("dt dot without fraction"  , fmt, () -> fmt.parseLocalDateTime (ymd2fmt( "2024_01_10 12:34:56."                 , fmt))),
                new Case("dtz dot without fraction" , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt( "2024_01_10 12:34:56. +10:50"          , fmt))),
                new Case("tz dot without fraction"  , fmt, () -> fmt.parseOffsetTime    (ymd2fmt(            "12:34:56. +10:50"          , fmt)))
        )).map(c -> DynamicTest.dynamicTest(
                c.name + " (" + c.fmt.name() + ")",
                () -> Assertions.assertThrows(DateTimeParseException.class, c.exec)
        ));
    }

    @TestFactory
    public Stream<DynamicTest> testIncompleteInvalidFormats() {

        record Case(String name, MultiFormatters fmt, Executable exec) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case("ldt missing date"        , fmt, () -> fmt.parseLocalDateTime (ymd2fmt(            "12:34:56.123456789"       , fmt))),
                new Case("ldt missing date (+zone)", fmt, () -> fmt.parseLocalDateTime (ymd2fmt(            "12:34:56.123456789 +10:50", fmt))),
                new Case("ins missing date"        , fmt, () -> fmt.parseInstant       (ymd2fmt(            "12:34:56.123456789"       , fmt))),
                new Case("ins missing date (+zone)", fmt, () -> fmt.parseInstant       (ymd2fmt(            "12:34:56.123456789 +10:50", fmt))),
                new Case("odt missing date+zone"   , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt(            "12:34:56.123456789"       , fmt))),
                new Case("odt missing date"        , fmt, () -> fmt.parseOffsetDateTime(ymd2fmt(            "12:34:56.123456789 +10:50", fmt))),
                new Case("zdt missing date+zone"   , fmt, () -> fmt.parseZonedDateTime (ymd2fmt(            "12:34:56.123456789"       , fmt))),
                new Case("zdt missing date"        , fmt, () -> fmt.parseZonedDateTime (ymd2fmt(            "12:34:56.123456789 +10:50", fmt))),
                new Case("lt only with date"       , fmt, () -> fmt.parseLocalTime     (ymd2fmt( "2024_01_10"                          , fmt))),
                new Case("ld only with time"       , fmt, () -> fmt.parseLocalDate     (ymd2fmt(            "12:34:56.123456789"       , fmt))),
                new Case("ld only with time+zone"  , fmt, () -> fmt.parseLocalDate     (ymd2fmt(            "12:34:56.123456789 +10:50", fmt)))
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

    // ---------- Invalid data tests. ----------

    @TestFactory
    public Stream<DynamicTest> testInvalidFormats() {

        record Case(String name, String input, MultiFormatters fmt, Executable exec) {}

        return Stream.of(MultiFormatters.values()).flatMap(fmt -> Stream.of(
                new Case("1st dt", "2024#01_10 12:34:56"          , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024#01_10 12:34:56"          , fmt))),
                new Case("2nd dt", "2024_01#10 12:34:56"          , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01#10 12:34:56"          , fmt))),
                new Case("3rd dt", "2024_01_10#12:34:56"          , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10#12:34:56"          , fmt))),
                new Case("4th dt", "2024_01_10 12#34:56"          , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12#34:56"          , fmt))),
                new Case("5th dt", "2024_01_10 12:34#56"          , fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12:34#56"          , fmt))),
                new Case("6th dt", "2024_01_10 12:34:56#123456789", fmt, () -> fmt.parseLocalDateTime(ymd2fmt("2024_01_10 12:34:56#123456789", fmt))),
                new Case("1st d" , "2024#01_10"                   , fmt, () -> fmt.parseLocalDate    (ymd2fmt("2024#01_10"                   , fmt))),
                new Case("2nd d" , "2024_01#10"                   , fmt, () -> fmt.parseLocalDate    (ymd2fmt("2024_01#10"                   , fmt))),
                new Case("1st t" ,            "12#34:56"          , fmt, () -> fmt.parseLocalTime    (ymd2fmt(           "12#34:56"          , fmt))),
                new Case("2nd t" ,            "12:34#56"          , fmt, () -> fmt.parseLocalTime    (ymd2fmt(           "12:34#56"          , fmt))),
                new Case("3rd t" ,            "12:34:56#123456789", fmt, () -> fmt.parseLocalTime    (ymd2fmt(           "12:34:56#123456789", fmt))),

                new Case("stress 1", "12:34:56 1234567890", fmt, () -> fmt.parseLocalTime("12:34:56 1234567890")),
                new Case("stress 2", "12:34:56 12345X789" , fmt, () -> fmt.parseLocalTime("12:34:56 12345X789" )),
                new Case("stress 3", "12:"                , fmt, () -> fmt.parseLocalTime("12:"                ))
        )).map(c -> DynamicTest.dynamicTest(
                c.name + " (" + c.fmt.name() + ")",
                () -> Assertions.assertThrows(DateTimeParseException.class, c.exec)
        ));
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

    private static Collection<String> mutateParts(String... input) {
        var regex = Pattern.compile("[0-9]");
        return Stream.of(input)
                .map(MultiFormattersTest::mutatePartsIn)
                .flatMap(List::stream)
                .collect(Collectors.toMap(e -> regex.matcher(e).replaceAll("x"), e -> e, (a, b) -> a))
                .values();
    }

    @TestFactory
    public Stream<DynamicTest> testIncompleteInputs() {
        record Case(MultiFormatters fmt, String input, String n, BiFunction<MultiFormatters, String, ?> recv) {
            public DynamicTest test() {
                return DynamicTest.dynamicTest(
                        "incomplete: " + ymd2fmt(input, fmt) + " (" + fmt.name() + " - " + n + ")",
                        () -> Assertions.assertThrows(DateTimeParseException.class, () -> recv.apply(fmt, ymd2fmt(input, fmt)))
                );
            }
        }

        var inputsDTZ = mutateParts(
                "2024_11_10 12:34:56.123456789 +00:00",
                "2024_11_10 12:34:56.123456789",
                           "12:34:56.123456789 +00:00",
                           "12:34:56.123456789",
                "2024_11_10"
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

    // ---------- Leap year test. ----------

    @TestFactory
    public Stream<DynamicTest> testValidLeapYear() {
        var a = Stream.of(MultiFormatters.values()).map(fmt ->
            DynamicTest.dynamicTest("29 feb 2024 valid (" + fmt.name() + ")", () -> {
                var parsed = fmt.parseLocalDate(ymd2fmt("2024_02_29", fmt));
                Assertions.assertEquals(29, parsed.getDayOfMonth());
            })
        );
        var b = Stream.of(MultiFormatters.values()).map(fmt ->
            DynamicTest.dynamicTest("29 feb 2000 valid (" + fmt.name() + ")", () -> {
                var parsed = fmt.parseLocalDate(ymd2fmt("2000_02_29", fmt));
                Assertions.assertEquals(29, parsed.getDayOfMonth());
            })
        );
        return Stream.concat(a, b);
    }

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testInvalidLeapYear() {
        var a = Stream.of(MultiFormatters.values()).map(fmt ->
            DynamicTest.dynamicTest("29 feb 2023 invalid (" + fmt.name() + ")", () -> {
                Assertions.assertThrows(DateTimeParseException.class, () -> fmt.parseLocalDate(ymd2fmt("2023_02_29", fmt)));
            })
        );
        var b = Stream.of(MultiFormatters.values()).map(fmt ->
            DynamicTest.dynamicTest("29 feb 1900 invalid (" + fmt.name() + ")", () -> {
                Assertions.assertThrows(DateTimeParseException.class, () -> fmt.parseLocalDate(ymd2fmt("1900_02_29", fmt)));
            })
        );
        return Stream.concat(a, b);
    }
}