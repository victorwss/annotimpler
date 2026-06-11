package ninja.javahacker.datetime;

import lombok.Generated;
import lombok.NonNull;

import module java.base;

/// Provides flexible date and time parsing and formatting for multiple date notation styles.
///
/// Each constant represents a date notation defined by **field order** (year, month, day position)
/// and **separator character** used between date components. [ISO_8601] follows the ISO 8601
/// standard and uses `T` as the date-time separator and `Z` or `±HH:MM` for timezone offset;
/// all other constants use a space as the date-time separator and `±HH:MM[:SS]` for timezone.
///
/// ## Supported date notations
///
/// | Constant    | Date format example | Separator |
/// |-------------|---------------------|-----------|
/// | [YMD_DASH]  | `2025-12-31`        | `-`       |
/// | [DMY_DASH]  | `31-12-2025`        | `-`       |
/// | [MDY_DASH]  | `12-31-2025`        | `-`       |
/// | [YMD_SLASH] | `2025/12/31`        | `/`       |
/// | [DMY_SLASH] | `31/12/2025`        | `/`       |
/// | [MDY_SLASH] | `12/31/2025`        | `/`       |
/// | [YMD_DOT]   | `2025.12.31`        | `.`       |
/// | [DMY_DOT]   | `31.12.2025`        | `.`       |
/// | [MDY_DOT]   | `12.31.2025`        | `.`       |
/// | [ISO_8601]  | `2025-12-31`        | `-` / `T` |
///
/// ## Parsing
///
/// Parse methods accept flexible inputs where missing parts are filled in with defaults:
///
/// - The **time component** is optional when parsing date-time types; if absent, midnight is assumed.
/// - **Seconds and sub-seconds** within the time component are optional.
/// - The **timezone** is optional when parsing offset or zoned types; if absent, UTC is assumed.
///
/// ## Formatting
///
/// Format methods produce strings in the notation of this constant.
/// Trailing zeros in sub-second fractions are stripped from the output
/// (e.g., `12:34:56.100000000` is written as `12:34:56.1`).
/// [format(Instant)] converts the instant to UTC and formats it as a local date-time string
/// without a timezone indicator.
public enum MultiFormatters {

    /// Year-month-day order with dash separators (e.g., `2025-12-31`).
    YMD_DASH,

    /// Day-month-year order with dash separators (e.g., `31-12-2025`).
    DMY_DASH,

    /// Month-day-year order with dash separators (e.g., `12-31-2025`).
    MDY_DASH,

    /// Year-month-day order with slash separators (e.g., `2025/12/31`).
    YMD_SLASH,

    /// Day-month-year order with slash separators (e.g., `31/12/2025`).
    DMY_SLASH,

    /// Month-day-year order with slash separators (e.g., `12/31/2025`).
    MDY_SLASH,

    /// Year-month-day order with dot separators (e.g., `2025.12.31`).
    YMD_DOT,

    /// Day-month-year order with dot separators (e.g., `31.12.2025`).
    DMY_DOT,

    /// Month-day-year order with dot separators (e.g., `12.31.2025`).
    MDY_DOT,

    /// ISO 8601 standard notation, using `T` as the date-time separator
    /// and `Z` or `±HH:MM` as the timezone offset.
    ISO_8601;

    @NonNull
    private static final String TIME_REGEX = "[0-9]{2}\\:[0-9]{2}(?:\\:[0-9]{2}(?:\\.[0-9]{1,9})?)?";

    @NonNull
    private static final String ZONE_REGEX = "(?:\\+|\\-)[0-9]{2}\\:[0-9]{2}(?:\\:[0-9]{2})?";

    @NonNull
    private static final String ISO_ZONE_REGEX = "(?:Z|" + ZONE_REGEX + ")";

    @NonNull
    private static final Pattern PATTERN_TZ_DEF = Pattern.compile("^" + TIME_REGEX + " " + ZONE_REGEX + "$");

    @NonNull
    private static final Pattern PATTERN_TZ_ISO = Pattern.compile("^" + TIME_REGEX + ISO_ZONE_REGEX + "$");

    @NonNull
    private static final Pattern PATTERN_T = Pattern.compile("^" + TIME_REGEX + "$");

    @NonNull
    private static final Pattern PATTERN_TS = Pattern.compile("^[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\.[0-9]{1,9}$");

    @NonNull
    private static final DateTimeFormatter FORMATTER_TZ_DEF;

    @NonNull
    private static final DateTimeFormatter FORMATTER_T_DEF;

    static {
        var formatTZ = "HH':'mm':'ss'.'SSSSSSSSS' 'xxxxx";
        var formatT = "HH':'mm':'ss'.'SSSSSSSSS";
        FORMATTER_TZ_DEF = DateTimeFormatter.ofPattern(formatTZ).withResolverStyle(ResolverStyle.STRICT);
        FORMATTER_T_DEF = DateTimeFormatter.ofPattern(formatT).withResolverStyle(ResolverStyle.STRICT);
    }

    @NonNull
    private final Pattern patternDTZ;

    @NonNull
    private final Pattern patternDT;

    @NonNull
    private final Pattern patternD;

    @NonNull
    private final DateTimeFormatter formatterDTZ;

    @NonNull
    private final DateTimeFormatter formatterDT;

    @NonNull
    private final DateTimeFormatter formatterD;

    private MultiFormatters() {
        var dot = name().contains("DOT");
        var slash = name().contains("SLASH");
        var iso = name().contains("ISO");
        var ymd = name().contains("YMD");
        var dmy = name().contains("DMY");
        var digit4 = "[0-9]{4}";
        var digit2 = "[0-9]{2}";
        var dateSeparatorRegex = dot ? "\\." : slash ? "/" : "\\-";
        var dateSeparatorPlain = dot ? '.' : slash ? '/' : '-';
        var dateRegex = ymd || iso
                ? digit4 + dateSeparatorRegex + digit2 + dateSeparatorRegex + digit2
                : digit2 + dateSeparatorRegex + digit2 + dateSeparatorRegex + digit4;
        var zoneRegex = iso ? ISO_ZONE_REGEX : ZONE_REGEX;
        var fieldSeparator1 = iso ? "T" : " ";
        var fieldSeparator2 = iso ? "" : " ";
        this.patternDTZ = Pattern.compile("^" + dateRegex + fieldSeparator1 + TIME_REGEX + fieldSeparator2 + zoneRegex + "$");
        this.patternDT = Pattern.compile("^" + dateRegex + fieldSeparator1 + TIME_REGEX + "$");
        this.patternD = Pattern.compile("^" + dateRegex + "$");
        if (iso) {
            this.formatterDTZ = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            this.formatterDT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            this.formatterD = DateTimeFormatter.ISO_LOCAL_DATE;
        } else {
            var formatD = (ymd ? "uuuu'-'MM'-'dd" : dmy ? "dd'-'MM'-'uuuu" : "MM'-'dd'-'uuuu").replace('-', dateSeparatorPlain);
            var formatDTZ = formatD + "' 'HH':'mm':'ss'.'SSSSSSSSS' 'xxxxx";
            var formatDT = formatD + "' 'HH':'mm':'ss'.'SSSSSSSSS";
            this.formatterDTZ = DateTimeFormatter.ofPattern(formatDTZ).withResolverStyle(ResolverStyle.STRICT);
            this.formatterDT = DateTimeFormatter.ofPattern(formatDT).withResolverStyle(ResolverStyle.STRICT);
            this.formatterD = DateTimeFormatter.ofPattern(formatD).withResolverStyle(ResolverStyle.STRICT);
        }
    }

    @NonNull
    private static String fillUpToNanoSeconds(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        if (!PATTERN_T.asPredicate().test(input)) return input;
        var complement = "00:00:00.000000000";
        return input + complement.substring(input.length());
    }

    @NonNull
    private Pattern patternTZ() {
        return this == ISO_8601 ? PATTERN_TZ_ISO : PATTERN_TZ_DEF;
    }

    @NonNull
    private DateTimeFormatter formatterTZ() {
        return this == ISO_8601 ? DateTimeFormatter.ISO_OFFSET_TIME : FORMATTER_TZ_DEF;
    }

    @NonNull
    private DateTimeFormatter formatterT() {
        return this == ISO_8601 ? DateTimeFormatter.ISO_LOCAL_TIME : FORMATTER_T_DEF;
    }

    @NonNull
    private <E> E parse(
            @NonNull String input,
            @NonNull BiFunction<CharSequence, DateTimeFormatter, E> func,
            boolean mustHaveDate)
            throws DateTimeParseException
    {
        checkNotNull(input); // Check recognized by lombok.
        checkNotNull(func); // Check recognized by lombok.

        var pTZ = patternTZ();

        var dtz = patternDTZ.asPredicate().test(input);
        var dt = patternDT.asPredicate().test(input);
        var d = patternD.asPredicate().test(input);
        var tz = pTZ.asPredicate().test(input);
        var t = PATTERN_T.asPredicate().test(input);

        var howMany = List.of(dtz, dt, d, tz, t).stream().filter(x -> x).count();
        assertLE(howMany, 1L);

        var input2 = Stream.of(input.split(" ")).map(MultiFormatters::fillUpToNanoSeconds).collect(Collectors.joining(" "));

        var format = dtz || (mustHaveDate && tz) ? formatterDTZ
                : dt || (mustHaveDate && t) ? formatterDT
                : d ? formatterD
                : tz ? formatterTZ()
                : formatterT();

        var pattern = dtz || (mustHaveDate && tz) ? patternDTZ
                : dt || (mustHaveDate && t) ? patternDT
                : d ? patternD
                : tz ? pTZ
                : PATTERN_T;

        try {
            return func.apply(input2, format);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException(
                    e.getMessage() + " - " + this.name() + " [" + pattern + "]",
                    e.getParsedString(),
                    e.getErrorIndex()
            );
        }
    }

    /// Parses the given string as an [Instant].
    ///
    /// The input is first parsed as an [OffsetDateTime] (with UTC assumed when no timezone is present),
    /// then converted to an [Instant].
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [Instant].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public Instant parseInstant(@NonNull String input) throws DateTimeParseException {
        return parseOffsetDateTime(input).toInstant();
    }

    /// Parses the given string as an [OffsetDateTime].
    ///
    /// If the input contains an explicit timezone offset, it is used directly.
    /// If no timezone is present, the input is parsed as a local date-time and UTC offset is assumed.
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [OffsetDateTime].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public OffsetDateTime parseOffsetDateTime(@NonNull String input) throws DateTimeParseException {
        try {
            return parse(input, OffsetDateTime::parse, true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDateTime(input).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    /// Parses the given string as a [ZonedDateTime].
    ///
    /// If the input contains an explicit timezone offset, it is used directly.
    /// If no timezone is present, the input is parsed as a local date-time and UTC zone is assumed.
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [ZonedDateTime].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public ZonedDateTime parseZonedDateTime(@NonNull String input) throws DateTimeParseException {
        try {
            return parse(input, ZonedDateTime::parse, true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDateTime(input).atZone(ZoneOffset.UTC);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    /// Parses the given string as a [LocalDateTime].
    ///
    /// If the input contains only a date (no time component), midnight ([LocalTime#MIN]) is assumed.
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [LocalDateTime].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public LocalDateTime parseLocalDateTime(@NonNull String input) throws DateTimeParseException {
        try {
            return parse(input, LocalDateTime::parse, true);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalDate(input).atTime(LocalTime.MIN);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    /// Parses the given string as a [LocalDate].
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [LocalDate].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public LocalDate parseLocalDate(@NonNull String input) throws DateTimeParseException {
        return parse(input, LocalDate::parse, true);
    }

    /// Parses the given string as an [OffsetTime].
    ///
    /// If the input contains an explicit timezone offset, it is used directly.
    /// If no timezone is present, the input is parsed as a local time and UTC offset is assumed.
    ///
    /// @param input the string to parse; must not be `null`.
    /// @return the parsed [OffsetTime].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public OffsetTime parseOffsetTime(@NonNull String input) throws DateTimeParseException {
        try {
            return parse(input, OffsetTime::parse, false);
        } catch (DateTimeParseException a) {
            try {
                return parseLocalTime(input).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException b) {
                // Ignore.
            }
            throw a;
        }
    }

    /// Parses the given string as a [LocalTime].
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [LocalTime].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public LocalTime parseLocalTime(@NonNull String input) throws DateTimeParseException {
        return parse(input, LocalTime::parse, false);
    }

    @NonNull
    private static String removeExcessZerosPart(@NonNull String part) {
        checkNotNull(part); // Check recognized by lombok.
        if (!PATTERN_TS.asPredicate().test(part)) return part;
        var t = part.length();
        while (part.charAt(t - 1) == '0') {
            t--;
        }
        if (part.charAt(t - 1) == '.') {
            t--;
        }
        return part.substring(0, t);
    }

    @NonNull
    private String removeExcessZeros(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        var parts = input.splitWithDelimiters("(?: |\\+|\\-|T)", -1);
        for (var i = 0; i < parts.length; i++) {
            parts[i] = removeExcessZerosPart(parts[i]);
        }
        return String.join("", parts);
    }

    /// Formats an [Instant] as a local date-time string using this formatter's notation.
    ///
    /// The instant is converted to [LocalDateTime] at UTC before formatting.
    /// The output does **not** include a timezone indicator.
    ///
    /// @param input The instant to format; must not be `null`.
    /// @return The formatted local date-time string in UTC.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull Instant input) {
        return format(LocalDateTime.ofInstant(input, ZoneOffset.UTC));
    }

    /// Formats an [OffsetDateTime] as a string using this formatter's date-time-with-timezone notation.
    ///
    /// Trailing zeros in sub-second fractions are stripped from the output.
    ///
    /// @param input The date-time to format; must not be `null`.
    /// @return The formatted date-time-with-timezone string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull OffsetDateTime input) {
        return removeExcessZeros(formatterDTZ.format(input));
    }

    /// Formats a [ZonedDateTime] as a string using this formatter's date-time-with-timezone notation.
    ///
    /// Only the UTC offset at the given instant is included in the output; the zone-region ID is not.
    /// Trailing zeros in sub-second fractions are stripped from the output.
    ///
    /// @param input The date-time to format; must not be `null`.
    /// @return The formatted date-time-with-timezone string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull ZonedDateTime input) {
        return removeExcessZeros(formatterDTZ.format(input));
    }

    /// Formats a [LocalDateTime] as a string using this formatter's date-time notation.
    ///
    /// Trailing zeros in sub-second fractions are stripped from the output.
    ///
    /// @param input The date-time to format; must not be `null`.
    /// @return The formatted date-time string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull LocalDateTime input) {
        return removeExcessZeros(formatterDT.format(input));
    }

    /// Formats a [LocalDate] as a string using this formatter's date notation.
    ///
    /// @param input The date to format; must not be `null`.
    /// @return The formatted date string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull LocalDate input) {
        return formatterD.format(input);
    }

    /// Formats an [OffsetTime] as a string using this formatter's time-with-timezone notation.
    ///
    /// Trailing zeros in sub-second fractions are stripped from the output.
    ///
    /// @param input The time to format; must not be `null`.
    /// @return The formatted time-with-timezone string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull OffsetTime input) {
        return removeExcessZeros(formatterTZ().format(input));
    }

    /// Formats a [LocalTime] as a string using this formatter's time notation.
    ///
    /// Trailing zeros in sub-second fractions are stripped from the output.
    ///
    /// @param input The time to format; must not be `null`.
    /// @return The formatted time string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull LocalTime input) {
        return removeExcessZeros(formatterT().format(input));
    }

    @Generated
    private static void assertLE(long a, long b) {
        if (a > b) throw new AssertionError(a + ":" + b);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}