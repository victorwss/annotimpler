package ninja.javahacker.datetime;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
@SuppressWarnings("PMD.NonSerializableClass")
@SuppressFBWarnings("DRE_DECLARED_RUNTIME_EXCEPTION")
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

    /// The formatter which includes a time and a time zone. It is the same for all [MultiFormatters]' instance, so it is static.
    @NonNull
    private static final DateTimeFormatter FORMATTER_TIME_ZONE;

    /// The formatter which includes a time, but no time zone. It is the same for all [MultiFormatters]' instance, so it is static.
    @NonNull
    private static final DateTimeFormatter FORMATTER_TIME;

    static {
        var formatTimeZone = "HH':'mm':'ss'.'SSSSSSSSS' 'xxxxx";
        var formatTimeOnly = "HH':'mm':'ss'.'SSSSSSSSS";
        FORMATTER_TIME_ZONE = DateTimeFormatter.ofPattern(formatTimeZone).withResolverStyle(ResolverStyle.STRICT);
        FORMATTER_TIME = DateTimeFormatter.ofPattern(formatTimeOnly).withResolverStyle(ResolverStyle.STRICT);
    }

    /// The formatter which includes a date, a time and a time zone. It is specific for each [MultiFormatters]' instance.
    @NonNull
    private final DateTimeFormatter formatterDateTimeZone;

    /// The formatter which includes a date and a time, but no time zone. It is specific for each [MultiFormatters]' instance.
    @NonNull
    private final DateTimeFormatter formatterDateTime;

    /// The formatter which includes a date without time or time zone. It is specific for each [MultiFormatters]' instance.
    @NonNull
    private final DateTimeFormatter formatterDate;

    /// Internal object responsible for compiling date and time objects from strings in the format expected by this instance.
    @NonNull
    private final DateTimeGrammar parser;

    // The constructor of thsi class.
    private MultiFormatters() {
        var iso = name().contains("ISO");
        if (iso) {
            this.formatterDateTimeZone = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            this.formatterDateTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            this.formatterDate = DateTimeFormatter.ISO_LOCAL_DATE;
            this.parser = new DateTimeGrammar('-', 'T', DateTimeGrammar.DateFormat.YMD, true);
        } else {
            var ymd = name().contains("YMD");
            var dmy = name().contains("DMY");
            var dot = name().contains("DOT");
            var slash = name().contains("SLASH");
            var dateSeparatorPlain = dot ? '.' : slash ? '/' : '-';
            var format1 = (ymd ? "yyyy'-'MM'-'dd" : dmy ? "dd'-'MM'-'yyyy" : "MM'-'dd'-'yyyy").replace('-', dateSeparatorPlain);
            var format2 = format1 + "' 'HH':'mm':'ss'.'SSSSSSSSS";
            var format3 = format1 + "' 'HH':'mm':'ss'.'SSSSSSSSS' 'xxxxx";
            this.formatterDate = DateTimeFormatter.ofPattern(format1).withResolverStyle(ResolverStyle.STRICT);
            this.formatterDateTime = DateTimeFormatter.ofPattern(format2).withResolverStyle(ResolverStyle.STRICT);
            this.formatterDateTimeZone = DateTimeFormatter.ofPattern(format3).withResolverStyle(ResolverStyle.STRICT);
            var tt = ymd ? DateTimeGrammar.DateFormat.YMD : dmy ? DateTimeGrammar.DateFormat.DMY : DateTimeGrammar.DateFormat.MDY;
            this.parser = new DateTimeGrammar(dateSeparatorPlain, ' ', tt, false);
        }
    }

    /// Check if the input matches `NN:NN:NN.NNNNNNNNNN`. Fractions of seconds with at least one digit (at least ten overall).
    /// We used to use a regex before, but manually handling it give better results.
    /// @param input What to be checked.
    /// @return If it matches the pattern or not.
    private static boolean hasFractionsOfSeconds(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.

        var len = input.length();
        if (len > 18 || len < 10 || input.charAt(2) != ':' || input.charAt(5) != ':' || input.charAt(8) != '.') return false;
        for (var i = 0; i < len; i++) {
            if (i == 2 || i == 5 || i == 8) continue;
            var c = input.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    @NonNull
    private DateTimeFormatter formatterTZ() {
        return this == ISO_8601 ? DateTimeFormatter.ISO_OFFSET_TIME : FORMATTER_TIME_ZONE;
    }

    @NonNull
    private DateTimeFormatter formatterT() {
        return this == ISO_8601 ? DateTimeFormatter.ISO_LOCAL_TIME : FORMATTER_TIME;
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
        return parser.dateTimeZone(input);
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
        return parseOffsetDateTime(input).toZonedDateTime();
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
        return parser.dateTime(input);
    }

    /// Parses the given string as a [LocalDate].
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [LocalDate].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public LocalDate parseLocalDate(@NonNull String input) throws DateTimeParseException {
        return parser.date(input);
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
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public OffsetTime parseOffsetTime(@NonNull String input) throws DateTimeParseException {
        return parser.timeZone(input);
    }

    /// Parses the given string as a [LocalTime].
    ///
    /// @param input The string to parse; must not be `null`.
    /// @return The parsed [LocalTime].
    /// @throws DateTimeParseException If the input cannot be parsed according to this format.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public LocalTime parseLocalTime(@NonNull String input) throws DateTimeParseException {
        return parser.time(input);
    }

    @NonNull
    private static String removeExcessZerosPart(@NonNull String part) {
        checkNotNull(part); // Check recognized by lombok.
        if (!hasFractionsOfSeconds(part)) return part;
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
        return removeExcessZeros(formatterDateTimeZone.format(input));
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
        return removeExcessZeros(formatterDateTimeZone.format(input));
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
        return removeExcessZeros(formatterDateTime.format(input));
    }

    /// Formats a [LocalDate] as a string using this formatter's date notation.
    ///
    /// @param input The date to format; must not be `null`.
    /// @return The formatted date string.
    /// @throws IllegalArgumentException If `input` is `null`.
    @NonNull
    public String format(@NonNull LocalDate input) {
        return formatterDate.format(input);
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
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}